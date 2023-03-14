package com.homeclimatecontrol.esphome2influxdb.k

import org.apache.logging.log4j.LogManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.yaml.snakeyaml.Yaml

class MqttEndpointTest {

    private val logger = LogManager.getLogger()
    private val yaml : Yaml = Yaml()

    @Test
    fun mqttEndpoint0() {
        val e = yaml.loadAs(
            javaClass.classLoader.getResourceAsStream("instantiate-mqtt-endpoint-0.yaml"),
            MqttEndpoint::class.java
        )
        logger.info("loaded: {}", e)
        assertEquals("localhost", e.host)
        assertEquals(1883, e.getPort())

        // Defaults
        assertEquals("localhost:1883", e.getName())
        assertNull(e.username)
        assertNull(e.password)
    }

    @Test
    fun mqttEndpoint1() {
        val e = yaml.loadAs(
            javaClass.classLoader.getResourceAsStream("instantiate-mqtt-endpoint-1.yaml"),
            MqttEndpoint::class.java
        )
        logger.info("loaded: {}", e)
        assertEquals("localhost", e.host)
        assertEquals(1883, e.getPort())
        assertEquals("eCegh5xe", e.username)
        assertEquals("Boh4ohda", e.password)

        // Defaults
        assertEquals("localhost:1883", e.getName())
    }

    @Test
    fun mqttEndpoint2() {
        val e = yaml.loadAs(
            javaClass.classLoader.getResourceAsStream("instantiate-mqtt-endpoint-2.yaml"),
            MqttEndpoint::class.java
        )
        logger.info("loaded: {}", e)
        assertEquals("localhost", e.host)
        assertEquals(1883, e.getPort())

        // Defaults
        assertEquals("localhost:1883", e.getName())
        assertNull(e.username)
        assertNull(e.password)
    }

    @Test
    fun mqttEndpoint3() {
        val e = yaml.loadAs(
            javaClass.classLoader.getResourceAsStream("instantiate-mqtt-endpoint-3.yaml"),
            MqttEndpoint::class.java
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
    fun mqttEndpointNegativePort() {
        val e = yaml.loadAs(
            javaClass.classLoader.getResourceAsStream("instantiate-endpoint-negative-port.yaml"),
            MqttEndpoint::class.java
        )
        logger.info("loaded: {}", e)
        assertEquals("localhost", e.host)

        // We're still good at this point
        assertEquals(-100, e.getPort())

        // But the verification mustn't pass
        try {
            e.verify()
        } catch (ex: IllegalArgumentException) {
            assertEquals("Invalid state:\nport can't be negative (-100 provided)", ex.message)
        }
    }
}
