package com.homeclimatecontrol.esphome2influxdb.k

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.ThreadContext
import java.util.*

/**
 * @param topicPrefix MQTT topic prefix to read updates from. Unlike [MqttEndpoint.topic] filter which is passed to [MqttReader], this filter will be applied locally.
 * @param source Source device name. Either derived from the [MQTT topic][topicPrefix], or specified explicitly.
 */
abstract class Device(
    var topicPrefix: String? = null,
    var source: String? = null
) : Verifiable {

    protected val logger: Logger = LogManager.getLogger()

    enum class Type(val literal: String, val cls: Class<*>) {
        CLIMATE("climate", Climate::class.java),
        SENSOR("sensor", Sensor::class.java),
        SWITCH("switch", Switch::class.java)
    }

    /**
     * Tags.
     */
    var tags: Map<String, String> = TreeMap()

    /**
     * Human readable device name. Either derived from the [MQTT topic][topicPrefix], or specified explicitly.
     */
    var name: String? = null

    /**
     * Get device type.
     *
     * @return Device type. Defines how the sample is rendered.
     */
    abstract fun getType(): Type

    override fun verify() {

        ThreadContext.push("verify")

        try {

            // By this time, all the properties were already set, and they either click together, or blow up

            // There are three parts that depend on each other and need to be sorted out in this order:
            //
            // 1. Topic prefix
            // 2. Source
            // 3. Human readable name
            //
            // If the topic prefix contains the device type as the next to last token, then we can derive the source,
            // ...unless it's been specified elsewhere in the configuration, then we blow up,
            // ...but if the topic doesn't contain the source, and the source is not specified, we blow up anyway,
            // ...and only set the name to default (being same as source) if it is not explicitly provided.
            val result: Array<String> = resolve(topicPrefix)

            // Topic prefix may mutate
            topicPrefix = result[0]
            source = result[1]
            name = result[2]
            logger.trace("topic={}", topicPrefix)
            logger.trace("source={}", source)
            logger.trace("name={}", name)
            logger.trace("tags={}", tags)

            // If we made it this far without throwing an exception, everything's good
        } finally {
            ThreadContext.pop()
        }
    }

    /**
     * Resolve the actual topic prefix, the source, and the name, from the topic prefix given.
     *
     * @return Array of [`actual topic prefix`, `source`, `name`].
     */
    private fun resolve(topic: String?): Array<String> {
        requireNotNull(topic) { "Can't accept null topic here" }
        return if (!topic.contains(getType().literal)) {

            // This means that the device type prefix wasn't specified, and that the source must be specified explicitly
            resolveShort(topic)
        } else resolveLong(topic)
    }

    /**
     * Resolve a short topic name (no device type qualifier) into the
     * [`actual topic prefix`, `source`, `name`] array.
     *
     * @param topic Topic to resolve.
     *
     * @return Array of [`actual topic prefix`, `source`, `name`].
     */
    private fun resolveShort(topic: String): Array<String> {
        logger.trace("short topic: {}", topic)

        // The topic doesn't contain the source name, hence the source must be provided
        requireNotNull(source) { "Short topic provided, must specify the source" }
        if (name == null) {
            name = source
        }
        return arrayOf(topic, source!!, name as String)
    }

    /**
     * Resolve a long topic name (including device type qualifier) into the
     * [`actual topic prefix`, `source`, `name`] array.
     *
     * @param topic Topic to resolve.
     *
     * @return Array of [`actual topic prefix`, `source`, `name`].
     */
    private fun resolveLong(topic: String): Array<String> {
        logger.trace("long topic: {}", topic)

        // The topic contains the source name, hence the source must not be provided
        require(source == null) { "Long topic provided, must not specify the source" }
        source = resolveSource(topic)
        if (name == null) {
            name = source
        }
        return arrayOf(topic, source!!, name as String)
    }

    /**
     * Resolve device source from the topic.
     *
     * @param topic Long MQTT topic (the one that includes the device type qualifier), slash separated.
     *
     * @return The last token from the source argument.
     */
    private fun resolveSource(topic: String?): String {
        requireNotNull(topic) { "Can't accept null topic here" }
        val tokens = topic.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var result = tokens[tokens.size - 1]
        var deviceOffset = 2
        if ("" == result) {
            // The topic must've been specified with a trailing slash, no big deal
            result = tokens[tokens.size - 2]
            deviceOffset++
        }

        // Make sure that we're working with the right device
        val type = tokens[tokens.size - deviceOffset]

        require(getType().literal == type) {
            ("Wrong topic: expecting '" + getType().literal
                    + "' as a part of the topic, received '" + type + "' instead")
        }

        return result
    }
    override fun toString() =
        "{class="
            .plus(javaClass.name)
            .plus(",topic=$topicPrefix,source=$source,name=$name,type=")
            .plus(getType().literal)
            .plus(",tags=$tags")
}
