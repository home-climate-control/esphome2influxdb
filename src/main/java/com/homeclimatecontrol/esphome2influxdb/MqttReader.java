package com.homeclimatecontrol.esphome2influxdb;

import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.apache.logging.log4j.ThreadContext;

public class MqttReader extends Worker<MqttEndpoint> {

    /**
     * The latch indicating the need to stop operation.
     */
    private final CountDownLatch stopGate;

    /**
     * Devices to listen to.
     */
    private final Set<Device> devices;

    public MqttReader(MqttEndpoint e, Set<Device> devices, CountDownLatch stopGate, CountDownLatch stoppedGate) {
        super(e, stoppedGate);

        this.devices = devices;
        this.stopGate = stopGate;
    }

    @Override
    public void run() {
        ThreadContext.push("run");

        try {

            logger.info("Started");

            stopGate.await();

            logger.info("Stopped");

        } catch (InterruptedException ex) {
            logger.error("Interrupted, terminating", ex);
            Thread.currentThread().interrupt();
        } finally {
            stoppedGate.countDown();
            logger.info("Shut down");
            ThreadContext.pop();
        }
    }
}
