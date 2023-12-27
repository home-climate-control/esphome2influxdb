package com.homeclimatecontrol.esphome2influxdb.k.adapter.v1.`in`

import com.homeclimatecontrol.esphome2influxdb.k.adapter.v1.out.InfluxDbWriter
import com.homeclimatecontrol.esphome2influxdb.k.adapter.v1.out.Writer
import com.homeclimatecontrol.esphome2influxdb.k.config.v1.Device
import com.homeclimatecontrol.esphome2influxdb.k.config.v1.Endpoint
import com.homeclimatecontrol.esphome2influxdb.k.config.v1.MqttEndpoint
import com.homeclimatecontrol.esphome2influxdb.k.config.v1.Sensor
import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import java.util.concurrent.CountDownLatch

@EnabledIfEnvironmentVariable(
    named = "TEST_ESPHOME2INFLUXDB",
    matches = "safe",
    disabledReason = "Only execute this test if a suitable MQTT broker and InfluxDB database are available"
)
class MqttReaderTest {

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

        val stoppedGate = CountDownLatch(1)
        val r = MqttReader(e, devices.values, false, stoppedGate)
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

        assertSame(s0, deviceCaptor.lastValue)

        // Take two
        for (d in devices.entries) {

            // Only the first match is considered, any other way doesn't make sense
            if (r.consume(d, topic1, payload1, writers)) {
                break
            }
        }

        // This assertion will fail until https://github.com/home-climate-control/esphome2influxdb/issues/1 is not fixed
        assertSame(s1, deviceCaptor.lastValue)
    }

    @Test
    fun take5() {

        val endpoint = MqttEndpoint()

        endpoint.host = "mqtt-esphome"
        val reader = MqttReader(endpoint, listOf())
        val writer = TestWriter(Endpoint(), reader)
        reader.attach(writer)

        CoroutineScope(Dispatchers.IO).launch { reader.run() }

        runBlocking {

            while (writer.running) {
                delay(100)
            }
        }
    }

    private class TestWriter(
        e: Endpoint,
        val r: MqttReader
    ): Writer<Endpoint>(e, CountDownLatch(1)) {

        private var count: Int = 0
        var running: Boolean = true

        override fun consume(timestamp: Long, device: Device, payload: String) {
            logger.info("consume: count=$count, device=$device, payload=$payload")

            if (count++ >= 5) {
                logger.warn("stopping")
                running = false
                runBlocking { r.stop() }
            }
        }

        override suspend fun run() {
            // Do nothing
        }

        override suspend fun stop() {
            // Do nothing
        }

    }
}
