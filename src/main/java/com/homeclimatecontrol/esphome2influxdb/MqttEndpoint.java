package com.homeclimatecontrol.esphome2influxdb;

/**
 * MQTT broker endpoint.
 */
public class MqttEndpoint extends Endpoint {

    public MqttEndpoint() {
        setPort(1883);
    }
}
