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
    private var url: String? = null

    var db = "esphome"

    init {
        setPort(8086)
    }

    fun getUrl(): String? {
        return if (url == null) {
            "http://" + host + ":" + getPort()
        } else url
    }

    fun setUrl(url: String?) {
        try {
            val target = URL(url)
            host = target.host
            setPort(target.port)
            this.url = target.toString()
        } catch (ex: MalformedURLException) {
            throw IllegalArgumentException(ex)
        }
    }

    override fun render() : String {
        return super.render().plus(",url=").plus(getUrl()).plus(",db=$db")
    }
}
