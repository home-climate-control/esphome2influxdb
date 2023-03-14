package com.homeclimatecontrol.esphome2influxdb.k

class Switch : Device() {
    override fun getType(): Type {
        return Type.SWITCH
    }
}
