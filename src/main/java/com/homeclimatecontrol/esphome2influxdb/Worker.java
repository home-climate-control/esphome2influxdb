package com.homeclimatecontrol.esphome2influxdb;

import java.util.concurrent.CountDownLatch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Worker<T extends Endpoint> implements Runnable {

    protected final Logger logger = LogManager.getLogger();

    /**
     * Endpoint to connect to.
     */
    protected final T endpoint;

    /**
     * Gate to {@code countDown()} when the worker shutdown is complete.
     */
    protected final CountDownLatch stoppedGate;

    protected Worker(T endpoint, CountDownLatch stoppedGate) {
        this.endpoint = endpoint;
        this.stoppedGate = stoppedGate;
    }
}
