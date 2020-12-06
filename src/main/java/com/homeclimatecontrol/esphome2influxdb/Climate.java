package com.homeclimatecontrol.esphome2influxdb;

public class Climate extends Device {

    @Override
    public Type getType() {
        return Type.CLIMATE;
    }
}
