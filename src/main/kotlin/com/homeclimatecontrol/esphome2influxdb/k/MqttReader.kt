package com.homeclimatecontrol.esphome2influxdb.k

import org.apache.logging.log4j.ThreadContext
import org.eclipse.paho.client.mqttv3.*
import java.time.Clock
import java.util.*
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
class MqttReader(
    e: MqttEndpoint,
    devices: Collection<Device>,
    autodiscover: Boolean,
    stopGate: CountDownLatch,
    stoppedGate: CountDownLatch
) :
    Worker<MqttEndpoint>(e, stoppedGate),
    MqttCallback {


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
    private var client: IMqttClient? = null

    init {
        this.devices = parseTopic(devices)
        this.autodiscover = autodiscover
        this.stopGate = stopGate
        try {
            // Only authenticate if both credentials are present
            if (endpoint.username != null && endpoint.password != null) {
                client = MqttClient(
                    "tcp://" + endpoint.username + ":" + endpoint.password + "@" + endpoint.host + ":" + endpoint.port,
                    clientId
                )
            } else {
                if (endpoint.username != null) {
                    // Bad idea to have no password
                    logger.warn("Missing MQTT password, connecting unauthenticated. This behavior will not be allowed in future releases.")
                }
                client = MqttClient("tcp://" + endpoint.host + ":" + endpoint.port, clientId)
            }
        } catch (ex: MqttException) {
            throw IllegalStateException("Failed to create a client for $endpoint")
        }
    }

    private fun parseTopic(source: Collection<Device>): MutableMap<String, Device> {
        val result: MutableMap<String, Device> = LinkedHashMap()
        for (d: Device in source) {
            result[d.topicPrefix + "/" + d.getType().literal + "/" + d.source] = d
        }
        return result
    }

    override fun run() {
        ThreadContext.push("run")
        try {
            logger.info("Started")
            connect()
            stopGate.await()
            logger.info("Stopped")
        } catch (ex: InterruptedException) {
            logger.error("Interrupted, terminating", ex)
            Thread.currentThread().interrupt()
        } catch (ex: MqttException) {
            logger.fatal("MQTT problem", ex)
        } finally {
            stoppedGate.countDown()
            logger.info("Shut down")
            ThreadContext.pop()
        }
    }

    @Throws(MqttException::class)
    private fun connect() {
        val options = MqttConnectOptions()
        options.isAutomaticReconnect = true
        options.isCleanSession = true
        options.connectionTimeout = 10
        options.userName = endpoint.username

        // https://github.com/eclipse/paho.mqtt.java/issues/804
        // https://github.com/home-climate-control/dz/issues/148
        if (endpoint.password != null) {
            options.password = endpoint.password!!.toCharArray()
        }
        client!!.setCallback(this)
        client!!.connect(options)
        client!!.subscribe(endpoint.topic, 0)
    }

    override fun connectionLost(cause: Throwable) {
        logger.error("Lost connection", cause)
        logger.info("Attempting to reconnect")
        try {
            // VT: NOTE: This may not be enough, let's see how reliable this is
            connect()
        } catch (ex: MqttException) {
            logger.fatal("Reconnect failed, giving up", ex)
        }
    }

    @Throws(Exception::class)
    override fun messageArrived(topic: String, message: MqttMessage) {
        ThreadContext.push("messageArrived")
        try {
            val payload = message.toString()
            logger.debug("topic={}, message={}", topic, payload)
            if (!consume(topic, payload)) {
                autodiscover(topic, payload)
            }
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

    override fun deliveryComplete(token: IMqttDeliveryToken) {
        // VT: NOTE: Nothing to do here, we're not sending anything
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
