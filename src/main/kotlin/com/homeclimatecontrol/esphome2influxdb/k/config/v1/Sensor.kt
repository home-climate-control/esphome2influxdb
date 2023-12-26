package com.homeclimatecontrol.esphome2influxdb.k.config.v1

class Sensor(
    topicPrefix: String? = null,
    source: String? = null
) : Device(topicPrefix, source) {
    override fun getType() = Type.SENSOR
}
