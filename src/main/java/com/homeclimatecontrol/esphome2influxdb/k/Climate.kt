package com.homeclimatecontrol.esphome2influxdb.k

class Climate : Device() {
    override fun getType(): Type {
        return Type.CLIMATE
    }
}
