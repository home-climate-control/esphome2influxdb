package com.homeclimatecontrol.esphome2influxdb.k

import MQTTClient
import mqtt.MQTTVersion
import mqtt.Subscription
import mqtt.packets.Qos
import mqtt.packets.mqtt.MQTTPublish
import mqtt.packets.mqttv5.SubscriptionOptions
import okio.internal.commonToUtf8String
import org.apache.logging.log4j.ThreadContext
import java.time.Clock
import java.util.TreeMap
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.regex.Pattern
import kotlin.collections.LinkedHashMap
import kotlin.collections.LinkedHashSet

/**
 * MQTT reader.
 *
 * @param e Endpoint configuration to connect to.
 * @param devices Set of previously configured devices to render the feed for.
 * @param autodiscover `true` if newly discovered devices get their own feed automatically.
 * @param stopGate Semaphore to listen to to initiate shutdown.
 * @param stoppedGate Semaphore to count down when the shutdown is complete.
 */
@OptIn(ExperimentalUnsignedTypes::class)
class MqttReader(
    e: MqttEndpoint,
    devices: Collection<Device>,
    autodiscover: Boolean,
    stopGate: CountDownLatch,
    stoppedGate: CountDownLatch
) : Worker<MqttEndpoint>(e, stoppedGate) {


    private val clock = Clock.systemUTC()
    private val autodiscover: Boolean

    /**
     * The latch indicating the need to stop operation.
     */
    private val stopGate: CountDownLatch

    /**
     * Devices to listen to.
     *
     * The key is the topic, the value is the device descriptor.
     */
    private val devices: MutableMap<String, Device>
    private val writers: MutableSet<InfluxDbWriter> = LinkedHashSet()

    /**
     * VT: FIXME: Provide an ability to generate and keep a persistent UUID
     */
    val clientId = UUID.randomUUID().toString()
    private var client: MQTTClient? = null

    init {
        this.devices = parseTopic(devices)
        this.autodiscover = autodiscover
        this.stopGate = stopGate
        try {

            val (username, password) = parseCredentials(endpoint.username, endpoint.password)

            client = MQTTClient(
                MQTTVersion.MQTT5,
                endpoint.host!!,
                endpoint.port,
                null,
                userName = username,
                password = password?.toByteArray()?.toUByteArray()
            ) {
                receive(it)
            }

            client!!.subscribe(listOf(Subscription(endpoint.topic, SubscriptionOptions(Qos.EXACTLY_ONCE))))

        } catch (ex: Exception) {
            throw IllegalStateException("Failed to create a client for $endpoint")
        }
    }

    private fun parseCredentials(username: String?, password: String?): Array<String?>  {

        // Only authenticate if both credentials are present
        if (endpoint.username != null && endpoint.password != null) {
            return arrayOf(username, password)
        }

        logger.warn("one of (username, password) is null or missing, connecting unauthenticated - THIS IS A BAD IDEA")

        return arrayOf(null, null)
    }

    private fun parseTopic(source: Collection<Device>): MutableMap<String, Device> {
        val result: MutableMap<String, Device> = LinkedHashMap()
        for (d: Device in source) {
            result[d.topicPrefix + "/" + d.getType().literal + "/" + d.source] = d
        }
        return result
    }

    override suspend fun run() {
        ThreadContext.push("run")
        try {
            logger.info("Started endpoint: $endpoint")
            client!!.run()

            // VT: FIXME: Unreachable until refactored
            logger.info("Stopped")
        } catch (ex: InterruptedException) {
            logger.error("Interrupted, terminating", ex)
            Thread.currentThread().interrupt()
        } finally {
            stoppedGate.countDown()
            logger.info("Shut down")
            ThreadContext.pop()
        }
    }

    private fun receive(message: MQTTPublish) {
        ThreadContext.push("messageArrived")
        try {
            val payload = message.payload?.asByteArray()?.commonToUtf8String() ?: ""
            logger.debug("topic={}, message={}", message.topicName, payload)
            if (!consume(message.topicName, payload)) {
                autodiscover(message.topicName, payload)
            }
        } catch (ex: Throwable) {
            logger.error("Huh?", ex)
        } finally {
            ThreadContext.pop()
        }
    }

    /**
     * Consume an MQTT message.
     *
     * @param topic MQTT topic.
     * @param payload MQTT message payload.
     *
     * @return `true` if the message was consumed.
     */
    private fun consume(topic: String, payload: String): Boolean {
        for (d: Map.Entry<String, Device> in devices.entries) {

            // Only the first match is considered, any other way doesn't make sense
            if (consume(d, topic, payload, writers)) {
                return true
            }
        }
        return false
    }

    /**
     * Consume an MQTT message if the device matches.
     *
     * @param d Device descriptor.
     * @param topic MQTT topic.
     * @param payload MQTT message payload.
     * @param writers InfluxDB writers to pass the message to.
     *
     * @return `true` if the message was consumed.
     */
    fun consume(d: Map.Entry<String, Device>, topic: String, payload: String, writers: Set<InfluxDbWriter>): Boolean {

        // Save ourselves extra memory allocation
        if (!topic.startsWith(d.key)) {
            return false
        }

        // Dodge https://github.com/home-climate-control/esphome2influxdb/issues/1
        // The price is one memory allocation per matching substring per message
        if (topic != d.key + "/state") {
            // Close, but no cigar
            return false
        }
        logger.debug("match: {}", d.value.name)

        // Let's generate the timestamp once so that several writers get the same
        val timestamp = clock.instant().toEpochMilli()
        for (w: InfluxDbWriter in writers) {
            w.consume(timestamp, d.value, payload)
        }
        return true
    }

    var knownTopics: MutableSet<String> = LinkedHashSet()
    var autodiscovered: MutableMap<String, String> = TreeMap()
    var autodiscoveredDevices: Set<Device> = LinkedHashSet()

    /**
     * Autodiscover devices not specified in the configuration.
     *
     * Note: autodiscovery will always be performed, but newly discovered devices will
     * only get their feed created if [autodiscover] is `true`.
     *
     * @param topic MQTT topic.
     * @param payload MQTT message payload (VT: FIXME: unused now,
     * but will be used later when autodiscovered devices will be activated immediately)
     */
    private fun autodiscover(topic: String, payload: String) {
        ThreadContext.push("autodiscover")
        try {
            if (knownTopics.contains(topic)) {
                // No sense mulling it over again
                return
            }
            knownTopics.add(topic)
            logger.debug("candidate: {}", topic)

            // VT: FIXME: Just the sensor for now
            val m = patternSensor.matcher(topic)
            if (m.matches()) {
                val topicPrefix = m.group(1)
                val name = m.group(2)
                if (!autodiscovered.containsKey(name)) {
                    logger.info("Found sensor {} at {}", name, topicPrefix)
                    autodiscovered[name] = topicPrefix
                    if (autodiscover) {
                        val s = Sensor(topicPrefix, name)
                        s.verify()
                        devices["$topicPrefix/sensor/$name"] = s
                        renderSensorConfiguration(
                            "Starting the feed. You will still have to provide configuration for extra tags, snippet",
                            name,
                            topicPrefix
                        )
                    } else {
                        renderSensorConfiguration(
                            "Autodiscovery is disabled, not creating a feed. Add this snippet to the configuration to create it",
                            name,
                            topicPrefix
                        )
                    }
                }
            }
        } finally {
            ThreadContext.pop()
        }
    }

    /**
     * Render a YAML configuration snippet for the given source and topic prefix into the log.
     *
     * @param message Log message to provide.
     * @param source Sensor source.
     * @param topicPrefix Sensor topic prefix.
     */
    private fun renderSensorConfiguration(message: String, source: String, topicPrefix: String) {

        // It's simpler to just dump a string literal into the log then to fiddle with YAML here.
        logger.info(
            "{}:\n"
                    + "  - type: sensor\n"
                    + "    topicPrefix: {}\n"
                    + "    source: {}\n"
                    + "    tags: {} # put your tags here",
            message, topicPrefix, source
        )
    }

    fun attach(writer: InfluxDbWriter) {
        writers.add(writer)
    }

    companion object {
        var patternClimate = Pattern.compile("(.*)/climate/(.*)/mode/state")
        var patternSensor = Pattern.compile("(.*)/sensor/(.*)/state")
        var patternSwitch = Pattern.compile("(.*)/switch/(.*)/state")
    }
}
