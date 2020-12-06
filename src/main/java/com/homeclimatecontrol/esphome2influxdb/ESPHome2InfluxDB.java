package com.homeclimatecontrol.esphome2influxdb;

import java.util.Map;

/**
 * Main configuration class.
 */
public class ESPHome2InfluxDB {

    public final Map<String, MqttEndpoint> sources;
    public final Map<String, InfluxDbEndpoint> targets;
    public final Map<String, Device> devices;

    public ESPHome2InfluxDB(
            Map<String, MqttEndpoint> sources,
            Map<String, InfluxDbEndpoint> targets,
            Map<String, Device> devices) {

        this.sources = sources;
        this.targets = targets;
        this.devices = devices;
    }
}
