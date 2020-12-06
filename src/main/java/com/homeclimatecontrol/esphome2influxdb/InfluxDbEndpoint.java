package com.homeclimatecontrol.esphome2influxdb;

/**
 * InfluxDB host.
 */
public class InfluxDbEndpoint extends Endpoint {

    public InfluxDbEndpoint(String name, String host, int port) {
        super(name, host, port);
    }

    public InfluxDbEndpoint(String name, String host, int port, String username, String password) {
        super(name, host, port, username, password);
    }
}
