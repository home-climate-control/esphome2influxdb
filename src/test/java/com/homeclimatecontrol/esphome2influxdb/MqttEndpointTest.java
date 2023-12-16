package com.homeclimatecontrol.esphome2influxdb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.yaml.snakeyaml.Yaml;

import java.util.stream.Stream;

class MqttEndpointTest {

    private final Logger logger = LogManager.getLogger();
    private final Yaml yaml = new Yaml();

    @ParameterizedTest
    @MethodSource("getEndpoints")
    void mqttEndpoint(EndpointConfig expected) {

        var e = yaml.loadAs(
                getClass().getClassLoader().getResourceAsStream(expected.source),
                MqttEndpoint.class);

        logger.info("loaded: {}", e);

        assertEquals(expected.host, e.host);
        assertEquals(expected.port, e.getPort());
        assertEquals(expected.name, e.getName());
        assertEquals(expected.username, e.username);
        assertEquals(expected.password, e.password);
    }

    @Test
    void mqttEndpointNegativePort() {

        var e = yaml.loadAs(
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

    private static class EndpointConfig {

        public final String source;
        public final String host;
        public final int port;
        public final String name;
        public final String username;
        public final String password;

        private EndpointConfig(String source, String host, int port, String name, String username, String password) {
            this.source = source;
            this.host = host;
            this.port = port;
            this.name = name;
            this.username = username;
            this.password = password;
        }
    }
    private static Stream<EndpointConfig> getEndpoints() {

        return Stream.of(
                new EndpointConfig("instantiate-mqtt-endpoint-0.yaml", "localhost", 1883, "localhost:1883", null, null),
                new EndpointConfig("instantiate-mqtt-endpoint-1.yaml", "localhost", 1883, "localhost:1883", "eCegh5xe", "Boh4ohda"),
                new EndpointConfig("instantiate-mqtt-endpoint-2.yaml", "localhost", 1883, "localhost:1883", null, null),
                new EndpointConfig("instantiate-mqtt-endpoint-3.yaml", "localhost", 9999, "localhost:9999", null, null)
        );
    }
}
