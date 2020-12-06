package com.homeclimatecontrol.esphome2influxdb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

public class ConfigurationTest {

    private final Logger logger = LogManager.getLogger();
    private final Yaml yaml = new Yaml();

    @Test
    public void sources0() {

        ThreadContext.push("sources0");

        try {

            Configuration c = yaml.loadAs(
                    getClass().getClassLoader().getResourceAsStream("instantiate-configuration-0.yaml"),
                    Configuration.class);

            logger.info("loaded: {}", c);

            assertFalse(c.verify());

        } finally {
            ThreadContext.pop();
        }
    }

    @Test
    public void sources1() {

        ThreadContext.push("sources1");

        try {

            Configuration c = yaml.loadAs(
                    getClass().getClassLoader().getResourceAsStream("instantiate-configuration-1.yaml"),
                    Configuration.class);

            logger.info("loaded: {}", c);

            assertEquals(2, c.sources.size());

            Iterator<MqttEndpoint> is = c.sources.iterator();

            assertEquals(8888, is.next().port);
            assertEquals(9999, is.next().port);

        } finally {
            ThreadContext.pop();
        }
    }
}
