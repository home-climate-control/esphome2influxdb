package com.homeclimatecontrol.esphome2influxdb;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.ThreadContext;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;

public class InfluxDbWriter extends Worker<InfluxDbEndpoint> {

    public InfluxDbWriter(InfluxDbEndpoint e, Set<MqttReader> readers, CountDownLatch stoppedGate) {
        super(e, stoppedGate);
    }

    @Override
    public void run() {
        ThreadContext.push("run");

        try {

            logger.info("Started");

//        } catch (InterruptedException ex) {
//            logger.error("Interrupted, terminating", ex);
        } finally {
            stoppedGate.countDown();
            logger.info("Shut down");
            ThreadContext.pop();
        }
    }

    public void consume(long timestamp, Device device, String payload) {
        ThreadContext.push("run");

        try {
            logger.info("payload: {}", payload);

            Builder b = Point.measurement(device.getType().literal)
                    .time(timestamp, TimeUnit.MILLISECONDS)
                    .tag(device.tags)
                    .addField("sample", payload);

        } finally {
            ThreadContext.pop();
        }
    }
}
