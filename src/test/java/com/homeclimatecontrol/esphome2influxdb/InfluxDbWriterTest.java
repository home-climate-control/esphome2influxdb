package com.homeclimatecontrol.esphome2influxdb;

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

public class InfluxDbWriterTest {

    @Test
    public void flush0() {
        ThreadContext.push("flush0");
        try {

            InfluxDbEndpoint e = new InfluxDbEndpoint();
            CountDownLatch stoppedGate = new CountDownLatch(1);
            InfluxDbWriter w = new InfluxDbWriter(e, new HashSet<MqttReader>(), stoppedGate);
            InfluxDB db = mock(InfluxDB.class);
            Queue<InfluxDbWriter.Sample> queue = new LinkedBlockingQueue<>();
            Device s = new Sensor("topic", "source");
            s.verify();

            queue.add(new InfluxDbWriter.Sample(Clock.systemUTC().instant().toEpochMilli(), s, "19.84"));

            w.flush(db, queue);

            assertTrue(queue.isEmpty());

        } finally {
            ThreadContext.pop();
        }
    }

    @Test
    public void flush1() {
        ThreadContext.push("flush1");
        try {

            InfluxDbEndpoint e = new InfluxDbEndpoint();
            CountDownLatch stoppedGate = new CountDownLatch(1);
            InfluxDbWriter w = new InfluxDbWriter(e, new HashSet<MqttReader>(), stoppedGate);
            InfluxDB db = mock(InfluxDB.class);
            Queue<InfluxDbWriter.Sample> queue = new LinkedBlockingQueue<>();
            Device s = new Sensor("topic", "source");
            s.verify();

            queue.add(new InfluxDbWriter.Sample(Clock.systemUTC().instant().toEpochMilli(), s, "oops"));

            w.flush(db, queue);

            assertTrue(queue.isEmpty());

        } finally {
            ThreadContext.pop();
        }
    }
}
