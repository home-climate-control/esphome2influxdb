package com.homeclimatecontrol.esphome2influxdb;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * TCP endpoint.
 */
public class Endpoint implements Verifiable {

    protected final Logger logger = LogManager.getLogger();

    private String name;
    public String host = "localhost";
    private int port;
    public String username;
    public String password;


    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void verify() {

        List<String> messages = new ArrayList<>();

        if (host == null || "".equals(host)) {
            messages.add("host can't be null or empty");
        }

        if (port <= 0) {
            messages.add("port can't be negative (" + port + " provided)");
        }

        if (!messages.isEmpty()) {

            String message = "Invalid state:";

            for (String s : messages) {
                message += "\n" + s;
            }

            throw new IllegalArgumentException(message);
        }
    }

    public String getName() {

        if (name != null) {
            return name;
        }

        return host + ":" + port;
    }

    public void setName(String name) {
        this.name = name;
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
        sb.append(",name=").append(getName());
        sb.append(",host:port=").append(host).append(":").append(port);

        if (username != null || password != null) {
            sb.append(",username:password").append(username).append(":").append(password);
        }
    }
}
