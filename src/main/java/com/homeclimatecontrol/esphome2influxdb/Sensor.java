package com.homeclimatecontrol.esphome2influxdb;

public class Sensor extends Device {

    @Override
    public Type getType() {
        return Type.SENSOR;
    }
}
