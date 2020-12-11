package com.homeclimatecontrol.esphome2influxdb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

public class ConfigurationTest {

    private final Logger logger = LogManager.getLogger();
    private final Yaml yaml = new Yaml();

    @Test
    public void configuration0() {

        ThreadContext.push("configuration0");

        try {

            Configuration c = yaml.loadAs(
                    getClass().getClassLoader().getResourceAsStream("instantiate-configuration-0.yaml"),
                    Configuration.class);

            logger.info("loaded: {}", c);

            assertFalse(c.needToStart());

        } finally {
            ThreadContext.pop();
        }
    }

    @Test
    public void configuration1() {

        ThreadContext.push("configuration1");

        try {

            Configuration c = yaml.loadAs(
                    getClass().getClassLoader().getResourceAsStream("instantiate-configuration-1.yaml"),
                    Configuration.class);

            logger.info("loaded: {}", c);

            assertEquals(2, c.sources.size());

            Iterator<MqttEndpoint> is = c.sources.iterator();

            assertEquals(8888, is.next().getPort());
            assertEquals(9999, is.next().getPort());

        } finally {
            ThreadContext.pop();
        }
    }

    @Test
    public void complete() {

        ThreadContext.push("complete");

        try {

            Configuration c = yaml.loadAs(
                    getClass().getClassLoader().getResourceAsStream("instantiate-configuration-complete.yaml"),
                    Configuration.class);

            logger.info("loaded: {}", c);

            Set<InfluxDbEndpoint> targets = c.targets;

            assertEquals(3, targets.size());

            {
                Iterator<InfluxDbEndpoint> i = targets.iterator();

                InfluxDbEndpoint local = i.next();

                assertEquals("localhost", local.host);
                assertEquals(8086, local.getPort());
                assertEquals("esphome", local.db);

                InfluxDbEndpoint remote = i.next();

                assertEquals("remote", remote.host);
                assertEquals(9999, remote.getPort());
                assertEquals("remote-db", remote.db);

                InfluxDbEndpoint backup = i.next();

                assertEquals("backup", backup.host);
                assertEquals(1111, backup.getPort());
                assertEquals("backup-db", backup.db);
            }

            c.verify();

        } finally {
            ThreadContext.pop();
        }
    }

    @Test
    public void raw() {

        ThreadContext.push("raw");

        try {

            Object c = yaml.load(
                    getClass().getClassLoader().getResourceAsStream("instantiate-configuration-complete.yaml"));

            logger.info("loaded: {}", c);

            // VT: NOTE: This test is just here to validate the YAML in case the other test fails
            assertTrue(true);

        } finally {
            ThreadContext.pop();
        }
    }
}
