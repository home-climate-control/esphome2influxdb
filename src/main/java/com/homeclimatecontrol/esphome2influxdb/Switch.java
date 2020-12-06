package com.homeclimatecontrol.esphome2influxdb;

import java.util.Map;

public class Switch extends Device {

    public Switch(String topicPrefix, Map<String, String> tags) {
        super(topicPrefix, tags);
    }

    @Override
    public Type getType() {
        return Type.SWITCH;
    }
}
