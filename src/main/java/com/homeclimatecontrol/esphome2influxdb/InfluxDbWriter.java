package com.homeclimatecontrol.esphome2influxdb;

import org.apache.logging.log4j.ThreadContext;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class InfluxDbWriter extends Worker<InfluxDbEndpoint> {

    private final Clock clock = Clock.systemUTC();

    private InfluxDB db;
    private final Queue<Sample> queue = new LinkedBlockingQueue<>();
    private static final int QUEUE_MAX = 1024;

    public InfluxDbWriter(InfluxDbEndpoint e, Set<MqttReader> readers, CountDownLatch stoppedGate) {
        super(e, stoppedGate);

        for (MqttReader r : readers) {
            r.attach(this);
        }
    }

    @Override
    public void run() {
        ThreadContext.push("run");

        try {

            logger.info("Started");

            connect();

            db.enableBatch();
            db.query(new Query("CREATE DATABASE " + endpoint.db));
            db.setDatabase(endpoint.db);

            // VT: FIXME: Implement the rest of the lifecycle
            while (true) {
                Thread.sleep(60000);
            }

        } catch (InterruptedException ex) {
            logger.error("Interrupted, terminating", ex);
        } finally {
            stoppedGate.countDown();
            logger.info("Shut down");
            ThreadContext.pop();
        }
    }

    /**
     * Connect to the remote in a non-blocking way.
     */
    private void connect() {
        ThreadContext.push("connect");

        try {

            InfluxDB db;
            long start = clock.instant().toEpochMilli();

            // This section will not block synchronized calls

            if (endpoint.username == null || "".equals(endpoint.username) || endpoint.password == null || "".equals(endpoint.password)) {
                logger.warn("one of (username, password) is null or missing, connecting unauthenticated - THIS IS A BAD IDEA");
                logger.warn("see https://docs.influxdata.com/influxdb/v1.8/administration/authentication_and_authorization/");
                logger.warn("(username, password) = ({}, {})",  endpoint.username, endpoint.password);

                db = InfluxDBFactory.connect(endpoint.getUrl());

            } else {
                db = InfluxDBFactory.connect(endpoint.getUrl(), endpoint.username, endpoint.password);
            }

            long end = clock.instant().toEpochMilli();

            logger.info("connected to {} in {}ms", endpoint.getUrl(), end - start);

            // This section is short and won't delay other synchronized calls much

            synchronized (this) {
                this.db = db;
            }
        } finally {
            ThreadContext.pop();
        }
    }

    public void consume(long timestamp, Device device, String payload) {
        ThreadContext.push("consume");

        try {

            logger.trace("payload: {}", payload);

            var s = new Sample(timestamp, device, payload);

            if (queue.size() < QUEUE_MAX) {

                // The cost of doing this all this time is negligible

                queue.add(s);

            } else {
                logger.error("QUEUE_MAX={} exceeded, skipping sample: {}", QUEUE_MAX, s);
            }

            synchronized (this) {

                // This happens at startup, when the connection is not yet established,
                // but the instance is ready to accept samples

                if (db == null) {
                    logger.warn("no connection yet, {} sample[s] deferred", queue.size());
                    return;
                }
            }

            flush(db, queue);

        } finally {
            ThreadContext.pop();
        }
    }

    /**
     * Flush the queue content.
     *
     * It is possible for more than one thread to call consume() a the same time,
     * MQTT receiver callbacks are asynchronous, hence synchronized modifier.
     *
     * @param db Writer to write to.
     * @param queue Queue to flush.
     */
    synchronized void flush(InfluxDB db, Queue<Sample> queue) {

        while (!queue.isEmpty()) {

            try {

                var sample = queue.peek();

                // VT: FIXME: This will only work for a sensor; need to change sample semantics
                // for other device types

                // Known problem

                if ("nan".equalsIgnoreCase(sample.payload)) {
                    logger.debug("NaN payload, ignored: {}", sample);
                    queue.remove();
                    continue;
                }

                Point p;

                try {
                    Point.Builder b = Point.measurement(sample.device.getType().literal)
                        .time(sample.timestamp, TimeUnit.MILLISECONDS)
                        .tag("source", sample.device.source)
                        .tag("name", sample.device.name)
                        .tag(sample.device.tags)
                        .addField("sample", new BigDecimal(sample.payload));

                    p = b.build();

                } catch (NumberFormatException ex) {

                    logger.error("Can't build a point out of a sample, skipped (likely reason is a sensor failure): {}", sample, ex);
                    queue.remove();
                    continue;
                }

                db.write(p);

                queue.remove();

            } catch (Throwable t) {

                // The item we couldn't write is still in the queue

                logger.warn("can't write sample, deferring remaining {} samples for now", queue.size(), t);
                break;
            }
        }

        db.flush();
    }

    static class Sample {

        public final long timestamp;
        public final Device device;
        public final String payload;

        public Sample(long timestamp, Device device, String payload) {
            this.timestamp = timestamp;
            this.device = device;
            this.payload = payload;
        }

        @Override
        public String toString() {

            return "{@" + timestamp + ": device=" + device + ", payload=" + payload + "}";

        }
    }
}
