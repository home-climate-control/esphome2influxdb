package com.homeclimatecontrol.esphome2influxdb.k

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

open class Endpoint : Verifiable {
    protected val logger: Logger = LogManager.getLogger()

    private var name: String? = null
    var host: String? = "localhost"
    private var port = 0
    var username: String? = null
    var password: String? = null

    fun getPort(): Int {
        return port
    }

    fun setPort(port: Int) {
        this.port = port
    }

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

    fun getName(): String {
        return if (name != null) {
            name as String
        } else "$host:$port"
    }

    fun setName(name: String) {
        this.name = name
    }

    override fun toString(): String {
        return "{".plus(render()).plus("}")
    }

    protected open fun render() : String {

        val part1 = "class=".plus(javaClass.name)
            .plus(",name=").plus(getName())
            .plus(",host:port=$host:$port")

        if (username == null && password == null) {
            return part1;
        }

        return part1.plus(",username:password=$username:$password")
    }
}
