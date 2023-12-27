package com.homeclimatecontrol.esphome2influxdb.k.adapter.v2.`in`

import MQTTClient
import com.homeclimatecontrol.esphome2influxdb.k.adapter.v1.`in`.MqttReader
import com.homeclimatecontrol.esphome2influxdb.k.adapter.v2.common.Sample
import com.homeclimatecontrol.esphome2influxdb.k.adapter.v2.common.Worker
import com.homeclimatecontrol.esphome2influxdb.k.config.v1.Device
import com.homeclimatecontrol.esphome2influxdb.k.config.v1.MqttEndpoint
import com.homeclimatecontrol.esphome2influxdb.k.config.v1.Sensor
import com.homeclimatecontrol.esphome2influxdb.k.runtime.InstanceIdProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import mqtt.MQTTVersion
import mqtt.Subscription
import mqtt.packets.Qos
import mqtt.packets.mqtt.MQTTPublish
import mqtt.packets.mqttv5.ReasonCode
import mqtt.packets.mqttv5.SubscriptionOptions
import okio.internal.commonToUtf8String
import org.apache.logging.log4j.ThreadContext
import java.time.Instant
import java.util.TreeMap

@OptIn(ExperimentalUnsignedTypes::class)
class MqttReader(
    e: MqttEndpoint,
    devices: Collection<Device>,
    private val autodiscover: Boolean = true,
): Worker<MqttEndpoint>(e), Reader {

    /**
     * Devices to listen to.
     *
     * The key is the topic, the value is the device descriptor.
     */
    private val devices: MutableMap<String, Device>

    val clientId = InstanceIdProvider.getId()
    private lateinit var client: MQTTClient

    private val ioScope = CoroutineScope(Dispatchers.IO)
    private val messageSink = MutableSharedFlow<MQTTPublish>()
    private val sampleSink = MutableSharedFlow<Sample>()

    init {
        this.devices = parseTopic(devices)

        listen()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun listen() {

        ioScope.launch {

            flowOf(0)
                .flowOn(Dispatchers.IO)
                .flatMapConcat { getClient() }
                .collect {
                    subscribe(it)
                    logger.debug("running until closed")
                    it.run()
                }
        }

        ioScope.launch {
        messageSink
            .asSharedFlow()
            .map { receive(it) }
            .collect {
                // Nothing to do here
            }
        }
    }

    private fun subscribe(client: MQTTClient) {
        client.subscribe(listOf(Subscription(endpoint.topic, SubscriptionOptions(Qos.EXACTLY_ONCE))))
        logger.info("$endpoint: subscribed")
        this.client = client
    }

    private fun getClient(): Flow<MQTTClient> {

        try {

            val (username, password) = parseCredentials(endpoint.username, endpoint.password)
            val client = MQTTClient(
                MQTTVersion.MQTT5,
                endpoint.host!!,
                endpoint.port,
                null,
                clientId = clientId.toString(),
                userName = username,
                password = password?.toByteArray()?.toUByteArray()
            ) {

                // Unless this is done in a blocking way, we're going to end up with seriously out of order messages,
                // especially upon connecting, and especially if processing is heavy
                runBlocking(Dispatchers.IO) {
                    enqueue(it)
                }
            }

            logger.info("$endpoint: created client")
            return flowOf(client)

        } catch (ex: Exception) {
            throw IllegalStateException("Failed to create a client for $endpoint")
        }
    }

    private suspend fun enqueue(message: MQTTPublish) {
        messageSink.emit(message)
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

    private suspend fun receive(message: MQTTPublish) {
        ThreadContext.push("messageArrived")
        try {

            // VT: NOTE: This will dump ALL the message, side effect being transforming the payload twice - performance hit.
            // Uncomment only if there's *real* trouble.

            //logger.trace("message: ${message.dump()}")

            val payload = message.payload?.asByteArray()?.commonToUtf8String() ?: ""
            logger.trace("topic={}, message={}", message.topicName, payload)
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
    private suspend fun consume(topic: String, payload: String): Boolean {
        for (d: Map.Entry<String, Device> in devices.entries) {

            // Only the first match is considered, any other way doesn't make sense
            if (consume(d, topic, payload)) {
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
     *
     * @return `true` if the message was consumed.
     */
    suspend fun consume(d: Map.Entry<String, Device>, topic: String, payload: String): Boolean {

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
        sampleSink.emit(Sample(clock.instant(), d.value, payload))

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
    private suspend fun autodiscover(topic: String, payload: String) {
        ThreadContext.push("autodiscover")
        try {
            if (knownTopics.contains(topic)) {
                // No sense mulling it over again
                return
            }
            knownTopics.add(topic)
            logger.debug("candidate: {}", topic)

            // VT: FIXME: Just the sensor for now
            val m = MqttReader.patternSensor.matcher(topic)
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

                        // And now that we are aware of it, consume the first signal
                        consume(topic, payload)

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

    override fun read(): Flow<Sample> {
        return sampleSink
    }

    override fun close() {
        logger.warn("$endpoint: closed")

        // VT: FIXME: MutableSharedFlow can't be complete()d like a Flux or a channel, will need a fix for that (up to replacing it)

        // It is possible that close() is called while the client is still starting; let's wait a bit

        var disconnected = false

        for (retryCount in 1..5) {
            try {

                client.disconnect(ReasonCode.DISCONNECT_WITH_WILL_MESSAGE)
                logger.info("$endpoint: disconnected")
                disconnected = true
                break

            } catch (ex: UninitializedPropertyAccessException) {

                // This is possible if the client is closed very early into its life, say, if there are unrelated problems and everything shuts down
                logger.warn("MQTT client is close()d before initialized, retry $retryCount/5")

                runBlocking { delay(500) }
            }
        }

        if (!disconnected) {
            logger.error("failed to disconnect from $endpoint, are we hung?")
        }
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
fun MQTTPublish.dump(): String {
    return "${this::class.simpleName}(retain=$retain, qos=$qos, dup=$dup, topic=$topicName, packetId=$packetId, payload=${payload?.asByteArray()?.commonToUtf8String()}, timestamp=${Instant.ofEpochMilli(timestamp)})"
}
