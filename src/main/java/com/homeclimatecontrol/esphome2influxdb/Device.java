package com.homeclimatecontrol.esphome2influxdb;

import java.util.Map;

public abstract class Device {

    public enum Type {
        CLIMATE,
        SENSOR,
        SWITCH
    }

    /**
     * MQTT topic prefix to read updates from.
     */
    public final String topicPrefix;

    /**
     * Tags.
     */
    public final Map<String, String> tags;

    /**
     * Source device name. Either derived from the {@link #topic MQTT topic}, or specified explicitly.
     */
    public final String source;

    /**
     * Human readable device name. Either derived from the {@link #topic MQTT topic}, or specified explicitly.
     */
    public final String name;

    public Device(String topicPrefix, Map<String, String> tags) {

        this.topicPrefix = topicPrefix;
        this.tags = tags;

        this.source = resolveName(topicPrefix);
        this.name = this.source;
    }

    public Device(String topicPrefix, Map<String, String> tags, String source) {

        this.topicPrefix = topicPrefix;
        this.tags = tags;

        this.source = source == null ? resolveName(topicPrefix) : source;
        this.name = this.source;
    }

    public Device(String topicPrefix, Map<String, String> tags, String source, String name) {

        this.topicPrefix = topicPrefix;
        this.tags = tags;

        this.source = source == null ? resolveName(topicPrefix) : source;
        this.name = name == null ? resolveName(topicPrefix) : name;
    }

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
}
