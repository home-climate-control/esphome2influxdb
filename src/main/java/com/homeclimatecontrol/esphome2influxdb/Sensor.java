package com.homeclimatecontrol.esphome2influxdb;

public class Sensor extends Device {

    public Sensor() {}

    public Sensor(String topicPrefix, String source) {
        this.topicPrefix = topicPrefix;
        this.source = source;
    }

    @Override
    public Type getType() {
        return Type.SENSOR;
    }
}
