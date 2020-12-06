package com.homeclimatecontrol.esphome2influxdb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

public class InfluxDbEndpointTest {

    private final Logger logger = LogManager.getLogger();
    private final Yaml yaml = new Yaml();

    @Test
    public void influxDbEndpoint0() {

        InfluxDbEndpoint e = yaml.loadAs(
                getClass().getClassLoader().getResourceAsStream("instantiate-influxdb-endpoint-0.yaml"),
                InfluxDbEndpoint.class);

        logger.info("loaded: {}", e);

        assertEquals("localhost", e.host);
        assertEquals(8086, e.port);

        // Defaults

        assertNull(e.name);
        assertNull(e.username);
        assertNull(e.password);
    }

    @Test
    public void influxDbEndpoint1() {

        InfluxDbEndpoint e = yaml.loadAs(
                getClass().getClassLoader().getResourceAsStream("instantiate-influxdb-endpoint-1.yaml"),
                InfluxDbEndpoint.class);

        logger.info("loaded: {}", e);

        assertEquals("localhost", e.host);
        assertEquals(8086, e.port);
        assertEquals("eCegh5xe", e.username);
        assertEquals("Boh4ohda", e.password);

        // Defaults

        assertNull(e.name);
    }

    @Test
    public void influxDbEndpoint2() {

        InfluxDbEndpoint e = yaml.loadAs(
                getClass().getClassLoader().getResourceAsStream("instantiate-influxdb-endpoint-2.yaml"),
                InfluxDbEndpoint.class);

        logger.info("loaded: {}", e);

        assertEquals("localhost", e.host);
        assertEquals(8086, e.port);

        // Defaults

        assertNull(e.name);
        assertNull(e.username);
        assertNull(e.password);
    }

    @Test
    public void influxDbEndpoint3() {

        InfluxDbEndpoint e = yaml.loadAs(
                getClass().getClassLoader().getResourceAsStream("instantiate-influxdb-endpoint-3.yaml"),
                InfluxDbEndpoint.class);

        logger.info("loaded: {}", e);

        assertEquals("localhost", e.host);
        assertEquals(9999, e.port);

        // Defaults

        assertNull(e.name);
        assertNull(e.username);
        assertNull(e.password);
    }
}