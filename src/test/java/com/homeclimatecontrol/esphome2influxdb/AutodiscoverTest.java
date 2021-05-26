package com.homeclimatecontrol.esphome2influxdb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Matcher;

import org.junit.jupiter.api.Test;

class AutodiscoverTest {

    @Test
    void sensorMatch() {

        var m = MqttReader.patternSensor.matcher("/esphome/7AC96F/sensor/1wire-bedroom-master-temperature/state");

        assertTrue(m.matches());
        assertEquals("/esphome/7AC96F", m.group(1));
        assertEquals("1wire-bedroom-master-temperature", m.group(2));
    }
}
