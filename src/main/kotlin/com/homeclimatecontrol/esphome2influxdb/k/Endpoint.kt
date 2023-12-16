package com.homeclimatecontrol.esphome2influxdb.k

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

open class Endpoint : Verifiable {
    protected val logger: Logger = LogManager.getLogger()

    var name: String? = null
        get(): String? {
            return if (field != null) {
                field as String
            } else "$host:$port"
        }

    var host: String? = "localhost"
    var username: String? = null
    var password: String? = null

    var port: Int = 0

    override fun verify() {

        val messages: MutableList<String> = ArrayList()

        if (host == null || "" == host) {
            messages.add("host can't be null or empty")
        }

        if (port <= 0) {
            messages.add("port can't be negative ($port provided)")
        }

        if (messages.isNotEmpty()) {
            var message = "Invalid state:"
            for (s in messages) {
                message += """
                
                $s
                """.trimIndent()
            }
            throw IllegalArgumentException(message)
        }
    }

    override fun toString(): String {
        return "{${render()}}"
    }

    protected open fun render() : String {

        val part1 = "class=${javaClass.name},name=$name,host:port=$host:$port"

        if (username == null && password == null) {
            return part1
        }

        return "$part1,username:password=$username:$password"
    }
}
