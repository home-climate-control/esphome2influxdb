package com.homeclimatecontrol.esphome2influxdb;

/**
 * MQTT broker endpoint.
 */
public class MqttEndpoint extends Endpoint {

    public MqttEndpoint() {
        setPort(1883);
    }

    @Override
    public void verify() {

        super.verify();
        logger.warn("verify() not implemented for {}", getClass().getName());
    }
}
