package com.homeclimatecontrol.esphome2influxdb.k.adapter.v2.common

import com.homeclimatecontrol.esphome2influxdb.k.config.v1.Endpoint
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.time.Clock

open class Worker<T: Endpoint>(
    val endpoint: T
) {
    protected val logger: Logger = LogManager.getLogger()
    protected val clock: Clock = Clock.systemUTC()
}
