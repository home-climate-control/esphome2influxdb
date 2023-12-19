package com.homeclimatecontrol.esphome2influxdb.k

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import java.util.concurrent.CountDownLatch

class MqttReaderTest {
    @Disabled("Only run this test if it matches your infrastructure")
    @Test
    fun testTopicMatch() {

        val e = MqttEndpoint()
        val devices = LinkedHashMap<String, Device>()

        val topicPrefix = "/same/mqtt/topic"

        val s0 = Sensor()
        val s1 = Sensor()

        s0.topicPrefix = topicPrefix
        s1.topicPrefix = topicPrefix

        s0.source = "room-0-temperature"
        s1.source = "room-0-temperature-1wire"

        val key0 = topicPrefix + "/" + s0.source
        val key1 = topicPrefix + "/" + s1.source

        s0.verify()
        s1.verify()

        devices[key0] = s0
        devices[key1] = s1

        val stopGate = CountDownLatch(1)
        val stoppedGate = CountDownLatch(1)
        val r = MqttReader(e, devices.values, false, stopGate, stoppedGate)
        val writers = LinkedHashSet<InfluxDbWriter>()
        val topic0 = "$key0/state"
        val topic1 = "$key1/state"
        val w = Mockito.mock(InfluxDbWriter::class.java)

        val deviceCaptor = com.nhaarman.mockitokotlin2.argumentCaptor<Device>()
        val payloadCaptor = com.nhaarman.mockitokotlin2.argumentCaptor<String>()

        Mockito.doNothing().`when`(w).consume(
            ArgumentMatchers.isA(
                Long::class.java
            ), deviceCaptor.capture(), payloadCaptor.capture()
        )

        writers.add(w)

        val payload0 = "0"
        val payload1 = "1"

        // Take one
        for (d in devices.entries) {

            // Only the first match is considered, any other way doesn't make sense
            if (r.consume(d, topic0, payload0, writers)) {
                break
            }
        }

        Assertions.assertSame(s0, deviceCaptor.lastValue)

        // Take two
        for (d in devices.entries) {

            // Only the first match is considered, any other way doesn't make sense
            if (r.consume(d, topic1, payload1, writers)) {
                break
            }
        }

        // This assertion will fail until https://github.com/home-climate-control/esphome2influxdb/issues/1 is not fixed
        Assertions.assertSame(s1, deviceCaptor.lastValue)
    }
}
