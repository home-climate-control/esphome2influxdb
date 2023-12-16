package com.homeclimatecontrol.esphome2influxdb.k

/**
 * MQTT [Endpoint].
 *
 * @param topic Topic filter. This filter will be passed to [MqttReader], unlike the filters in [Device.topicPrefix] which will be applied locally.
 */
class MqttEndpoint(
    var topic: String = "#"
) : Endpoint() {

    init {
        setPort(1883)
    }

    override fun render() = super.render().plus(",topic=$topic")
}
