package com.homeclimatecontrol.esphome2influxdb.k.adapter.v1.out

import com.homeclimatecontrol.esphome2influxdb.k.config.v1.InfluxDbEndpoint
import com.homeclimatecontrol.esphome2influxdb.k.config.v1.Sensor
import org.apache.logging.log4j.ThreadContext
import org.assertj.core.api.Assertions
import org.influxdb.InfluxDB
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.time.Clock
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue

class InfluxDbWriterTest {
    @Test
    fun flush0() {
        ThreadContext.push("flush0")
        try {
            val e = InfluxDbEndpoint()
            val stoppedGate = CountDownLatch(1)
            val w = InfluxDbWriter(e, HashSet(), stoppedGate)
            val db = Mockito.mock(InfluxDB::class.java)
            val queue = LinkedBlockingQueue<InfluxDbWriter.Sample>()
            val s = Sensor("topic", "source")
            s.verify()
            queue.add(InfluxDbWriter.Sample(Clock.systemUTC().instant().toEpochMilli(), s, "19.84"))
            w.flush(db, queue)
            Assertions.assertThat(queue).isEmpty()
        } finally {
            ThreadContext.pop()
        }
    }

    @Test
    fun flush1() {
        ThreadContext.push("flush1")
        try {
            val e = InfluxDbEndpoint()
            val stoppedGate = CountDownLatch(1)
            val w = InfluxDbWriter(e, HashSet(), stoppedGate)
            val db = Mockito.mock(InfluxDB::class.java)
            val queue = LinkedBlockingQueue<InfluxDbWriter.Sample>()
            val s = Sensor("topic", "source")
            s.verify()
            queue.add(InfluxDbWriter.Sample(Clock.systemUTC().instant().toEpochMilli(), s, "oops"))
            w.flush(db, queue)
            Assertions.assertThat(queue).isEmpty()
        } finally {
            ThreadContext.pop()
        }
    }
}
