package com.homeclimatecontrol.esphome2influxdb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * TCP endpoint.
 */
public class Endpoint implements Verifiable {

    protected final Logger logger = LogManager.getLogger();

    public String name;
    public String host = "localhost";
    public int port;
    public String username;
    public String password;

    @Override
    public void verify() {
        logger.warn("verify() not implemented for {}", getClass().getName());
    }
}
