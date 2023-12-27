package com.homeclimatecontrol.esphome2influxdb.k.adapter.v2.`in`

import com.homeclimatecontrol.esphome2influxdb.k.adapter.v2.common.Sample
import kotlinx.coroutines.flow.Flow

interface Reader: AutoCloseable {
    fun read(): Flow<Sample>
}
