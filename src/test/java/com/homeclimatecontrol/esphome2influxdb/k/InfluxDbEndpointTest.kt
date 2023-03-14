package com.homeclimatecontrol.esphome2influxdb.k

import org.apache.logging.log4j.LogManager
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.yaml.snakeyaml.Yaml
import java.net.MalformedURLException
import java.net.URL

class InfluxDbEndpointTest {

    private val logger = LogManager.getLogger()
    private val yaml : Yaml = Yaml()

    @Test
    fun influxDbEndpoint0() {
        val e = yaml.loadAs(
            javaClass.classLoader.getResourceAsStream("instantiate-influxdb-endpoint-0.yaml"),
            InfluxDbEndpoint::class.java
        )
        logger.info("loaded: {}", e)
        assertEquals("localhost", e.host)
        assertEquals(8086, e.getPort())

        // Defaults
        assertEquals("localhost:8086", e.getName())
        assertEquals("esphome", e.db)
        assertNull(e.username)
        assertNull(e.password)
    }

    @Test
    fun influxDbEndpoint1() {
        val e = yaml.loadAs(
            javaClass.classLoader.getResourceAsStream("instantiate-influxdb-endpoint-1.yaml"),
            InfluxDbEndpoint::class.java
        )
        logger.info("loaded: {}", e)
        assertEquals("localhost", e.host)
        assertEquals(8086, e.getPort())
        assertEquals("eCegh5xe", e.username)
        assertEquals("Boh4ohda", e.password)

        // Defaults
        assertEquals("localhost:8086", e.getName())
    }

    @Test
    fun influxDbEndpoint2() {
        val e = yaml.loadAs(
            javaClass.classLoader.getResourceAsStream("instantiate-influxdb-endpoint-2.yaml"),
            InfluxDbEndpoint::class.java
        )
        logger.info("loaded: {}", e)
        assertEquals("localhost", e.host)
        assertEquals(8086, e.getPort())
        assertEquals("zero", e.getName())

        // Defaults
        assertNull(e.username)
        assertNull(e.password)
    }

    @Test
    fun influxDbEndpoint3() {
        val e = yaml.loadAs(
            javaClass.classLoader.getResourceAsStream("instantiate-influxdb-endpoint-3.yaml"),
            InfluxDbEndpoint::class.java
        )
        logger.info("loaded: {}", e)
        assertEquals("localhost", e.host)
        assertEquals(9999, e.getPort())
        assertEquals("localhost:9999", e.getName())

        // Defaults
        assertNull(e.username)
        assertNull(e.password)
    }

    @Test
    @Throws(MalformedURLException::class)
    fun influxDbEndpoint4() {
        val e = yaml.loadAs(
            javaClass.classLoader.getResourceAsStream("instantiate-influxdb-endpoint-4.yaml"),
            InfluxDbEndpoint::class.java
        )
        logger.info("loaded: {}", e)
        assertEquals("remote-host", e.host)
        assertEquals(1111, e.getPort())
        assertEquals("remote-host:1111", e.getName())
        assertEquals(URL("http://remote-host:1111/").toString(), e.getUrl())

        // Defaults
        assertNull(e.username)
        assertNull(e.password)
    }
}
