package com.homeclimatecontrol.esphome2influxdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.time.Clock;
import java.util.HashSet;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.ThreadContext;
import org.influxdb.InfluxDB;
import org.junit.jupiter.api.Test;

class InfluxDbWriterTest {

    @Test
    void flush0() {
        ThreadContext.push("flush0");
        try {

            var e = new InfluxDbEndpoint();
            var stoppedGate = new CountDownLatch(1);
            var w = new InfluxDbWriter(e, new HashSet<MqttReader>(), stoppedGate);
            var db = mock(InfluxDB.class);
            var queue = new LinkedBlockingQueue<InfluxDbWriter.Sample>();
            var s = new Sensor("topic", "source");
            s.verify();

            queue.add(new InfluxDbWriter.Sample(Clock.systemUTC().instant().toEpochMilli(), s, "19.84"));

            w.flush(db, queue);

            assertThat(queue).isEmpty();

        } finally {
            ThreadContext.pop();
        }
    }

    @Test
    void flush1() {
        ThreadContext.push("flush1");
        try {

            var e = new InfluxDbEndpoint();
            var stoppedGate = new CountDownLatch(1);
            var w = new InfluxDbWriter(e, new HashSet<MqttReader>(), stoppedGate);
            var db = mock(InfluxDB.class);
            var queue = new LinkedBlockingQueue<InfluxDbWriter.Sample>();
            var s = new Sensor("topic", "source");
            s.verify();

            queue.add(new InfluxDbWriter.Sample(Clock.systemUTC().instant().toEpochMilli(), s, "oops"));

            w.flush(db, queue);

            assertThat(queue).isEmpty();

        } finally {
            ThreadContext.pop();
        }
    }
}
