package com.homeclimatecontrol.esphome2influxdb;

import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.apache.logging.log4j.ThreadContext;

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
}
