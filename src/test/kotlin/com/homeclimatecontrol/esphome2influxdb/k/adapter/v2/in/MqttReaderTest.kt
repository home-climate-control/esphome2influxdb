package com.homeclimatecontrol.esphome2influxdb.k.adapter.v2.`in`

import com.homeclimatecontrol.esphome2influxdb.k.adapter.v2.common.Sample
import com.homeclimatecontrol.esphome2influxdb.k.config.v1.Device
import com.homeclimatecontrol.esphome2influxdb.k.config.v1.MqttEndpoint
import com.homeclimatecontrol.esphome2influxdb.k.config.v1.Sensor
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.take
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import java.util.concurrent.atomic.AtomicInteger

@EnabledIfEnvironmentVariable(
    named = "TEST_ESPHOME2INFLUXDB",
    matches = "safe",
    disabledReason = "Only execute this test if a suitable MQTT broker and InfluxDB database are available"
)
class MqttReaderTest {

    private val logger: Logger = LogManager.getLogger()
    private val ioScope = CoroutineScope(Dispatchers.IO)

    /**
     * Make sure <a href=https://github.com/home-climate-control/esphome2influxdb/issues/1">#1</a> stays fixed.
     *
     * If one MQTT topic is a subset of another, and both represent valid devices, they must be recognized as distinct.
     */
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

        val reader = MqttReader(e, devices.values, true)
        val topic0 = "$key0/state"
        val topic1 = "$key1/state"

        val payload0 = "0"
        val payload1 = "1"

        runBlocking {

            ioScope.launch {

                // Send

                // Take one
                for (d in devices.entries) {

                    // Only the first match is considered, any other way doesn't make sense
                    if (reader.consume(d, topic0, payload0)) {
                        break
                    }
                }

                // Take two
                for (d in devices.entries) {

                    // Only the first match is considered, any other way doesn't make sense
                    if (reader.consume(d, topic1, payload1)) {
                        break
                    }
                }
            }

            val count = AtomicInteger(0)
            val buffer = mutableListOf<Sample>()

            val readerJob = ioScope.launch {

                // Receive

                reader
                    .read()
                    .collect {

                        logger.info("offset=${count.get()}, entry=$it")

                        count.incrementAndGet()
                        buffer.add(it)

                        // No other way to stop the receiver with SharedFlow
                        // since there is no data coming except for two fabricated values above.
                        // take(2) will not work because of race conditions.

                        if (count.get() == 2) {
                            reader.close()
                            cancel()
                        }
                    }
            }

            readerJob.join()

            assertThat(buffer[0].device).isEqualTo(s0)
            assertThat(buffer[1].device).isEqualTo(s1)
        }
    }

    @Test
    fun take5() {

        val endpoint = MqttEndpoint()

        endpoint.host = "mqtt-esphome"
        val reader = MqttReader(endpoint, listOf())

        runBlocking {

            reader
                .read()
                .take(5)
                .collect { logger.info("received: $it") }

            logger.info("done")
        }
    }
}
