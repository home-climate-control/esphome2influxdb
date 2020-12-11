package com.homeclimatecontrol.esphome2influxdb;

import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

public abstract class Device implements Verifiable {

    protected final Logger logger = LogManager.getLogger();

    public enum Type {
        CLIMATE("climate", Climate.class),
        SENSOR("sensor", Sensor.class),
        SWITCH("switch", Switch.class);

        public final String literal;
        public final Class cls;

        private Type(String literal, Class cls) {
            this.literal = literal;
            this.cls = cls;
        }
    }

    /**
     * MQTT topic prefix to read updates from.
     */
    public String topicPrefix;

    /**
     * Tags.
     */
    public Map<String, String> tags = new TreeMap<>();

    /**
     * Source device name. Either derived from the {@link #topicPrefix MQTT topic}, or specified explicitly.
     */
    public String source;

    /**
     * Human readable device name. Either derived from the {@link #topicPrefix MQTT topic}, or specified explicitly.
     */
    public String name;

    /**
     * Get device type.
     *
     * @return Device type. Defines how the sample is rendered.
     */
    public abstract Type getType();

    @Override
    public void verify() {

        ThreadContext.push("verify");

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

            String[] result = resolve(topicPrefix);

            // Topic prefix may mutate
            topicPrefix = result[0];
            source = result[1];
            name = result[2];

            logger.trace("topic={}", topicPrefix);
            logger.trace("source={}", source);
            logger.trace("name={}", name);

            logger.trace("tags={}", tags);

            // If we made it this far without throwing an exception, everything's good

        } finally {
            ThreadContext.pop();
        }
    }

    /**
     * Resolve the actual topic prefix, the source, and the name, from the topic prefix given.
     *
     * @return Array of [{@code actual topic prefix}, {@code source}, {@code name}].
     */
    private String[] resolve(String topic) {

        if (topic == null) {
            throw new IllegalArgumentException("Can't accept null topic here");
        }

        if (!topic.contains(getType().literal)) {

            // This means that the device type prefix wasn't specified, and that the source must be specified explicitly
            return resolveShort(topic);
        }

        return resolveLong(topic);
    }

    /**
     * Resolve a short topic name (no device type qualifier) into the
     * [{@code actual topic prefix}, {@code source}, {@code name}] array.
     *
     * @param topic Topic to resolve.
     *
     * @return Array of [{@code actual topic prefix}, {@code source}, {@code name}].
     */
    private String[] resolveShort(String topic) {

        logger.trace("short topic: {}", topic);

        // The topic doesn't contain the source name, hence the source must be provided

        if (source == null) {
            throw new IllegalArgumentException("Short topic provided, must specify the source");
        }

        if (name == null) {

            name = source;
        }

        return new String[] { topic, source, name };
    }

    /**
     * Resolve a long topic name (including device type qualifier) into the
     * [{@code actual topic prefix}, {@code source}, {@code name}] array.
     *
     * @param topic Topic to resolve.
     *
     * @return Array of [{@code actual topic prefix}, {@code source}, {@code name}].
     */
    private String[] resolveLong(String topic) {

        logger.trace("long topic: {}", topic);

        // The topic contains the source name, hence the source must not be provided

        if (source != null) {
            throw new IllegalArgumentException("Long topic provided, must not specify the source");
        }

        source = resolveSource(topic);

        if (name == null) {

            name = source;
        }

        return new String[] { topic, source, name };
    }

    /**
     * Resolve device source from the topic.
     *
     * @param topic Long MQTT topic (the one that includes the device type qualifier), slash separated.
     *
     * @return The last token from the source argument.
     */
    private String resolveSource(String topic) {

        if (topic == null) {
            throw new IllegalArgumentException("Can't accept null topic here");
        }

        String[] tokens = topic.split("/");
        String result = tokens[tokens.length - 1];
        int deviceOffset = 2;

        if ("".equals(result)) {
            // The topic must've been specified with a trailing slash, no big deal
            result = tokens[tokens.length - 2];
            deviceOffset++;
        }

        // Make sure that we're working with the right device
        String type = tokens[tokens.length - deviceOffset];

        if (!getType().literal.equals(type)) {

            throw new IllegalArgumentException(
                    "Wrong topic: expecting '" + getType().literal
                    + "' as a part of the topic, received '" + type + "' instead");
        }

        return result;
    }


    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append("{");

        render(sb);

        sb.append("}");

        return sb.toString();
    }

    protected void render(StringBuilder sb) {

        sb.append("class=").append(getClass().getName());
        sb.append(",topic=").append(topicPrefix);
        sb.append(",source=").append(source);
        sb.append(",name=").append(name);
        sb.append(",type=").append(getType().literal);
        sb.append(",tags=").append(tags);
    }
}
