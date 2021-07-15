package com.homeclimatecontrol.esphome2influxdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Matcher;

import org.junit.jupiter.api.Test;

class AutodiscoverTest {

    @Test
    void sensorMatch() {

        var m = MqttReader.patternSensor.matcher("/esphome/7AC96F/sensor/1wire-bedroom-master-temperature/state");

        assertThat(m.matches()).isTrue();
        assertThat(m.group(1)).isEqualTo("/esphome/7AC96F");
        assertThat(m.group(2)).isEqualTo("1wire-bedroom-master-temperature");
    }
}
