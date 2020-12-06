package com.homeclimatecontrol.esphome2influxdb;

import java.util.Map;

public class Climate extends Device {

    public Climate(String topicPrefix, Map<String, String> tags) {
        super(topicPrefix, tags);
    }

    @Override
    public Type getType() {
        return Type.CLIMATE;
    }
}
