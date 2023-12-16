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
    fun get() {
        val e = InfluxDbEndpoint()
        assertEquals("http://localhost:8086", e.url)

        val url = "https://outside:9999"
        e.url = url
        assertEquals(url, e.url)
    }

    @Test
    fun render() {
        val e = InfluxDbEndpoint()
        assertEquals(
            "{class=com.homeclimatecontrol.esphome2influxdb.k.InfluxDbEndpoint,name=localhost:8086,host:port=localhost:8086,url=http://localhost:8086,db=esphome}",
            e.toString())

        e.url = "https://outside:9999"
        assertEquals(
            "{class=com.homeclimatecontrol.esphome2influxdb.k.InfluxDbEndpoint,name=outside:9999,host:port=outside:9999,url=https://outside:9999,db=esphome}",
            e.toString())
    }

    @Test
    fun influxDbEndpoint0() {
        val e = yaml.loadAs(
            javaClass.classLoader.getResourceAsStream("instantiate-influxdb-endpoint-0.yaml"),
            InfluxDbEndpoint::class.java
        )
        logger.info("loaded: {}", e)
        assertEquals("localhost", e.host)
        assertEquals(8086, e.port)

        // Defaults
        assertEquals("localhost:8086", e.name)
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
        assertEquals(8086, e.port)
        assertEquals("eCegh5xe", e.username)
        assertEquals("Boh4ohda", e.password)

        // Defaults
        assertEquals("localhost:8086", e.name)
    }

    @Test
    fun influxDbEndpoint2() {
        val e = yaml.loadAs(
            javaClass.classLoader.getResourceAsStream("instantiate-influxdb-endpoint-2.yaml"),
            InfluxDbEndpoint::class.java
        )
        logger.info("loaded: {}", e)
        assertEquals("localhost", e.host)
        assertEquals(8086, e.port)
        assertEquals("zero", e.name)

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
        assertEquals(9999, e.port)
        assertEquals("localhost:9999", e.name)

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
        assertEquals(1111, e.port)
        assertEquals("remote-host:1111", e.name)
        assertEquals(URL("http://remote-host:1111/").toString(), e.url)

        // Defaults
        assertNull(e.username)
        assertNull(e.password)
    }
}
