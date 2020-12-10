package com.homeclimatecontrol.esphome2influxdb;

import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Device implements Verifiable {

    protected final Logger logger = LogManager.getLogger();

    public enum Type {
        CLIMATE("climate"),
        SENSOR("sensor"),
        SWITCH("switch");

        public final String literal;

        private Type(String literal) {
            this.literal = literal;
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
     * Source device name. Either derived from the {@link #topic MQTT topic}, or specified explicitly.
     */
    public String source;

    /**
     * Human readable device name. Either derived from the {@link #topic MQTT topic}, or specified explicitly.
     */
    public String name;

    /**
     * Get device type.
     *
     * @return Device type. Defines how the sample is rendered.
     */
    public abstract Type getType();

    /**
     * Resolve device name from the topic.
     *
     * @param topic MQTT topic, slash separated.
     * @return The last token from the source argument.
     */
    private String resolveName(String topic) {

        if (topic == null) {
            throw new IllegalArgumentException("Can't accept null topic here");
        }

        String[] tokens = topic.split("/");
        String result = tokens[tokens.length - 1];

        if ("".equals(result)) {
            // The topic must've been specified with a trailing slash, no big deal
            result = tokens[tokens.length - 2];
        }

        return result;
    }

    @Override
    public void verify() {

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

        logger.warn("verify() not implemented for {}", getClass().getName());
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append("{");

        sb.append("class=").append(getClass().getName());

        sb.append("}");

        return sb.toString();
    }
}
