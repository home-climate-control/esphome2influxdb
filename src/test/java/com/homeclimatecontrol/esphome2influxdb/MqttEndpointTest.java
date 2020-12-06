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
        assertEquals(1883, e.port);

        // Defaults

        assertNull(e.name);
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
        assertEquals(1883, e.port);
        assertEquals("eCegh5xe", e.username);
        assertEquals("Boh4ohda", e.password);

        // Defaults

        assertNull(e.name);
    }

    @Test
    public void mqttEndpoint2() {

        MqttEndpoint e = yaml.loadAs(
                getClass().getClassLoader().getResourceAsStream("instantiate-mqtt-endpoint-2.yaml"),
                MqttEndpoint.class);

        logger.info("loaded: {}", e);

        assertEquals("localhost", e.host);
        assertEquals(1883, e.port);

        // Defaults

        assertNull(e.name);
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
        assertEquals(9999, e.port);

        // Defaults

        assertNull(e.name);
        assertNull(e.username);
        assertNull(e.password);
    }
}
