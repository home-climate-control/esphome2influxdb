package com.homeclimatecontrol.esphome2influxdb.k.adapter.v1.common

import com.homeclimatecontrol.esphome2influxdb.k.config.v1.Endpoint
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
    abstract suspend fun run();
    abstract suspend fun stop();
}
