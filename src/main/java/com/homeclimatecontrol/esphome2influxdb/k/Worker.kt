package com.homeclimatecontrol.esphome2influxdb.k

import org.apache.logging.log4j.LogManager
import java.util.concurrent.CountDownLatch

abstract class Worker<T : Endpoint> protected constructor(

    /**
     * Endpoint to connect to.
     */
    protected val endpoint: T,

    /**
     * Gate to `countDown()` when the worker shutdown is complete.
     */
    protected val stoppedGate: CountDownLatch
) :
    Runnable {
    protected val logger = LogManager.getLogger()

}
