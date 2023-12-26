package com.homeclimatecontrol.esphome2influxdb.k.adapter.v1.`in`

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class AutodiscoverTest {
    @Test
    fun sensorMatch() {
        val m = MqttReader.patternSensor.matcher("/esphome/7AC96F/sensor/1wire-bedroom-master-temperature/state")
        Assertions.assertThat(m.matches()).isTrue
        Assertions.assertThat(m.group(1)).isEqualTo("/esphome/7AC96F")
        Assertions.assertThat(m.group(2)).isEqualTo("1wire-bedroom-master-temperature")
    }
}
