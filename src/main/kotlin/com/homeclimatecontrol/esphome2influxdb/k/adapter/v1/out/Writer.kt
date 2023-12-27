package com.homeclimatecontrol.esphome2influxdb.k.adapter.v1.out

import com.homeclimatecontrol.esphome2influxdb.k.adapter.v1.common.Worker
import com.homeclimatecontrol.esphome2influxdb.k.config.v1.Device
import com.homeclimatecontrol.esphome2influxdb.k.config.v1.Endpoint
import java.util.concurrent.CountDownLatch

abstract class Writer<T: Endpoint>(
    endpoint: T,
    stoppedGate: CountDownLatch
): Worker<T>(endpoint, stoppedGate) {
    abstract fun consume(timestamp: Long, device: Device, payload: String)
}
