package com.homeclimatecontrol.esphome2influxdb;

public class Gateway {
    public String getGreeting() {
        return "Hello world.";
    }

    public static void main(String[] args) {
        System.out.println(new Gateway().getGreeting());
    }
}
