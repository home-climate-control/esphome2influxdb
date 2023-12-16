package com.homeclimatecontrol.esphome2influxdb.k

import java.net.MalformedURLException
import java.net.URL

/**
 * InfluxDB host.
 */
class InfluxDbEndpoint() : Endpoint() {

    /**
     * InfluxDB URL to connect to.
     *
     * Overrides `host:port`.
     */
    var url: String? = null
        get() = field ?: ("http://" + host + ":" + port)
        set(url) {
            try {
                val target = URL(url)
                host = target.host
                port = target.port
                field = target.toString()
            } catch (ex: MalformedURLException) {
                throw IllegalArgumentException(ex)
            }
        }

    var db = "esphome"

    init {
        port = 8086
    }

    override fun render() : String {
        return "${super.render()},url=$url,db=$db"
    }
}
