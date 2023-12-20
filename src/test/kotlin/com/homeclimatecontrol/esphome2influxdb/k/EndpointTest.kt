package com.homeclimatecontrol.esphome2influxdb.k

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.UUID

class EndpointTest {
    @Test
    fun name() {
        val e = Endpoint()
        assertEquals("localhost:0", e.name)

        val name = UUID.randomUUID().toString()
        e.name = name
        assertEquals(name, e.name)
    }

    @Test
    fun render() {
        val e = Endpoint()
        assertEquals(
            "{class=Endpoint,name=localhost:0,host:port=localhost:0}",
            e.toString(), "localhost:0")

        val name = UUID.randomUUID().toString()
        e.name = name
        assertEquals(
            "{class=Endpoint,name=$name,host:port=localhost:0}",
            e.toString())
    }
}
