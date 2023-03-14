package com.homeclimatecontrol.esphome2influxdb.k

import com.homeclimatecontrol.esphome2influxdb.Device
import com.homeclimatecontrol.esphome2influxdb.MqttReader

class MqttEndpoint : Endpoint() {

    /**
     * Topic filter.
     *
     * This filter will be passed to [MqttReader], unlike the filters in
     * [Device.topicPrefix] which will be applied locally.
     */
    var topic = "#"

    init {
        setPort(1883)
    }

     override fun render() : String {
        return super.render().plus(",topic=$topic")
    }
}
