package com.homeclimatecontrol.esphome2influxdb;

/**
 * TCP endpoint.
 */
public class Endpoint {

    public final String name;
    public final String host;
    public final int port;
    public final String username;
    public final String password;

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
}
