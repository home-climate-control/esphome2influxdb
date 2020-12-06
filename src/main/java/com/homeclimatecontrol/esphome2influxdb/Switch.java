package com.homeclimatecontrol.esphome2influxdb;

public class Switch extends Device {

    @Override
    public Type getType() {
        return Type.SWITCH;
    }
}
