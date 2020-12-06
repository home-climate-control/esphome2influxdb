package com.homeclimatecontrol.esphome2influxdb;

/**
 * InfluxDB host.
 */
public class InfluxDbEndpoint extends Endpoint {

    public InfluxDbEndpoint() {

        setPort(8086);
    }

    @Override
    public void verify() {

        super.verify();
        logger.warn("verify() not implemented for {}", getClass().getName());
    }
}
