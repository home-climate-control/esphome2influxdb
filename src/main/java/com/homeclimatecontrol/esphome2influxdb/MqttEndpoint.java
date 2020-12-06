package com.homeclimatecontrol.esphome2influxdb;

/**
 * MQTT broker endpoint.
 */
public class MqttEndpoint extends Endpoint {

    public MqttEndpoint(String name, String host, int port) {
        super(name, host, port);
    }

    public MqttEndpoint(String name, String host, int port, String username, String password) {
        super(name, host, port, username, password);
    }
}
