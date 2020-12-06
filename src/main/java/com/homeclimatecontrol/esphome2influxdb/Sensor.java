package com.homeclimatecontrol.esphome2influxdb;

import java.util.Map;

public class Sensor extends Device {

    public Sensor(String topicPrefix, Map<String, String> tags) {
        super(topicPrefix, tags);
    }

    @Override
    public Type getType() {
        return Type.SENSOR;
    }
}
