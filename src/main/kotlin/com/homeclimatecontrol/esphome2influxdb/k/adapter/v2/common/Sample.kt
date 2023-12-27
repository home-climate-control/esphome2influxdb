package com.homeclimatecontrol.esphome2influxdb.k.adapter.v2.common

import com.homeclimatecontrol.esphome2influxdb.k.config.v1.Device
import java.time.Instant

data class Sample(
    val timestamp: Instant,
    val device: Device,
    val payload: String
)
