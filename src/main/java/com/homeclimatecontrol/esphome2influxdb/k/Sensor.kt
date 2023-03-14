package com.homeclimatecontrol.esphome2influxdb.k

class Sensor : Device {
    constructor()
    constructor(topicPrefix: String?, source: String?) {
        this.topicPrefix = topicPrefix
        this.source = source
    }

    override fun getType(): Type {
        return Type.SENSOR
    }
}
