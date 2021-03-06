package com.homeclimatecontrol.esphome2influxdb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

class InfluxDbEndpointTest {

    private final Logger logger = LogManager.getLogger();
    private final Yaml yaml = new Yaml();

    @Test
    void influxDbEndpoint0() {

        var e = yaml.loadAs(
                getClass().getClassLoader().getResourceAsStream("instantiate-influxdb-endpoint-0.yaml"),
                InfluxDbEndpoint.class);

        logger.info("loaded: {}", e);

        assertEquals("localhost", e.host);
        assertEquals(8086, e.getPort());

        // Defaults

        assertEquals("localhost:8086", e.getName());
        assertEquals("esphome", e.db);
        assertNull(e.username);
        assertNull(e.password);
    }

    @Test
    void influxDbEndpoint1() {

        var e = yaml.loadAs(
                getClass().getClassLoader().getResourceAsStream("instantiate-influxdb-endpoint-1.yaml"),
                InfluxDbEndpoint.class);

        logger.info("loaded: {}", e);

        assertEquals("localhost", e.host);
        assertEquals(8086, e.getPort());
        assertEquals("eCegh5xe", e.username);
        assertEquals("Boh4ohda", e.password);

        // Defaults

        assertEquals("localhost:8086", e.getName());
    }

    @Test
    void influxDbEndpoint2() {

        var e = yaml.loadAs(
                getClass().getClassLoader().getResourceAsStream("instantiate-influxdb-endpoint-2.yaml"),
                InfluxDbEndpoint.class);

        logger.info("loaded: {}", e);

        assertEquals("localhost", e.host);
        assertEquals(8086, e.getPort());
        assertEquals("zero", e.getName());

        // Defaults

        assertNull(e.username);
        assertNull(e.password);
    }

    @Test
    void influxDbEndpoint3() {

        var e = yaml.loadAs(
                getClass().getClassLoader().getResourceAsStream("instantiate-influxdb-endpoint-3.yaml"),
                InfluxDbEndpoint.class);

        logger.info("loaded: {}", e);

        assertEquals("localhost", e.host);
        assertEquals(9999, e.getPort());
        assertEquals("localhost:9999", e.getName());

        // Defaults

        assertNull(e.username);
        assertNull(e.password);
    }

    @Test
    void influxDbEndpoint4() throws MalformedURLException {

        var e = yaml.loadAs(
                getClass().getClassLoader().getResourceAsStream("instantiate-influxdb-endpoint-4.yaml"),
                InfluxDbEndpoint.class);

        logger.info("loaded: {}", e);

        assertEquals("remote-host", e.host);
        assertEquals(1111, e.getPort());
        assertEquals("remote-host:1111", e.getName());
        assertEquals(new URL("http://remote-host:1111/").toString(), e.getUrl());

        // Defaults

        assertNull(e.username);
        assertNull(e.password);
    }
}
