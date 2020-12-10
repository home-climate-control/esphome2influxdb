package com.homeclimatecontrol.esphome2influxdb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

public class MqttEndpointTest {

    private final Logger logger = LogManager.getLogger();
    private final Yaml yaml = new Yaml();

    @Test
    public void mqttEndpoint0() {

        MqttEndpoint e = yaml.loadAs(
                getClass().getClassLoader().getResourceAsStream("instantiate-mqtt-endpoint-0.yaml"),
                MqttEndpoint.class);

        logger.info("loaded: {}", e);

        assertEquals("localhost", e.host);
        assertEquals(1883, e.getPort());

        // Defaults

        assertEquals("localhost:1883", e.getName());
        assertNull(e.username);
        assertNull(e.password);
    }

    @Test
    public void mqttEndpoint1() {

        MqttEndpoint e = yaml.loadAs(
                getClass().getClassLoader().getResourceAsStream("instantiate-mqtt-endpoint-1.yaml"),
                MqttEndpoint.class);

        logger.info("loaded: {}", e);

        assertEquals("localhost", e.host);
        assertEquals(1883, e.getPort());
        assertEquals("eCegh5xe", e.username);
        assertEquals("Boh4ohda", e.password);

        // Defaults

        assertEquals("localhost:1883", e.getName());
    }

    @Test
    public void mqttEndpoint2() {

        MqttEndpoint e = yaml.loadAs(
                getClass().getClassLoader().getResourceAsStream("instantiate-mqtt-endpoint-2.yaml"),
                MqttEndpoint.class);

        logger.info("loaded: {}", e);

        assertEquals("localhost", e.host);
        assertEquals(1883, e.getPort());

        // Defaults

        assertEquals("localhost:1883", e.getName());
        assertNull(e.username);
        assertNull(e.password);
    }

    @Test
    public void mqttEndpoint3() {

        MqttEndpoint e = yaml.loadAs(
                getClass().getClassLoader().getResourceAsStream("instantiate-mqtt-endpoint-3.yaml"),
                MqttEndpoint.class);

        logger.info("loaded: {}", e);

        assertEquals("localhost", e.host);
        assertEquals(9999, e.getPort());
        assertEquals("localhost:9999", e.getName());

        // Defaults

        assertNull(e.username);
        assertNull(e.password);
    }

    @Test
    public void mqttEndpointNegativePort() {

        MqttEndpoint e = yaml.loadAs(
                getClass().getClassLoader().getResourceAsStream("instantiate-endpoint-negative-port.yaml"),
                MqttEndpoint.class);

        logger.info("loaded: {}", e);

        assertEquals("localhost", e.host);

        // We're still good at this point
        assertEquals(-100, e.getPort());

        // But the verification mustn't pass
        try {

            e.verify();

        } catch (IllegalArgumentException ex) {
            assertEquals("Invalid state:\nport can't be negative (-100 provided)", ex.getMessage());
        }
    }
}
