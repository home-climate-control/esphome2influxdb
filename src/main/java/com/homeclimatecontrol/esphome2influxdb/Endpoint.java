package com.homeclimatecontrol.esphome2influxdb;

/**
 * TCP endpoint.
 */
public class Endpoint {

    private String name;
    private String host;
    private int port;
    private String username;
    private String password;

    public Endpoint() {
        // Make SnakeYAML happy
    }

    public Endpoint(String name, String host, int port) {
        this(name, host, port, null, null);
    }

    public Endpoint(String name, String host, int port, String username, String password) {

        this.name = name;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
