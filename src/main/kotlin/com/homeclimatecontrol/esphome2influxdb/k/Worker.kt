package com.homeclimatecontrol.esphome2influxdb.k

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.CountDownLatch

/**
 * @param endpoint Endpoint to connect to.
 * @param stoppedGate Gate to `countDown()` when the worker shutdown is complete.
 */
abstract class Worker<T : Endpoint>(
    protected val endpoint: T,
    protected val stoppedGate: CountDownLatch
) {
    protected val logger: Logger = LogManager.getLogger()
    public abstract suspend fun run();
}
