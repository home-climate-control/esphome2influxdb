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

class ConfigurationTest {

    private final Logger logger = LogManager.getLogger();
    private final Yaml yaml = new Yaml();

    @Test
    void configuration0() {

        ThreadContext.push("configuration0");

        try {

            var c = yaml.loadAs(
                    getClass().getClassLoader().getResourceAsStream("instantiate-configuration-0.yaml"),
                    Configuration.class);

            logger.info("loaded: {}", c);

            assertFalse(c.needToStart());

        } finally {
            ThreadContext.pop();
        }
    }

    @Test
    void configuration1() {

        ThreadContext.push("configuration1");

        try {

            var c = yaml.loadAs(
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
    void complete() {

        ThreadContext.push("complete");

        try {

            var c = yaml.loadAs(
                    getClass().getClassLoader().getResourceAsStream("instantiate-configuration-complete.yaml"),
                    Configuration.class);

            logger.info("loaded: {}", c);

            var targets = c.targets;

            assertEquals(3, targets.size());

            {
                var i = targets.iterator();

                var local = i.next();

                assertEquals("localhost", local.host);
                assertEquals(8086, local.getPort());
                assertEquals("esphome", local.db);

                var remote = i.next();

                assertEquals("remote", remote.host);
                assertEquals(9999, remote.getPort());
                assertEquals("remote-db", remote.db);

                var backup = i.next();

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
    void raw() {

        ThreadContext.push("raw");

        try {

            var c = yaml.load(
                    getClass().getClassLoader().getResourceAsStream("instantiate-configuration-complete.yaml"));

            logger.info("loaded: {}", c);

            // VT: NOTE: This test is just here to validate the YAML in case the other test fails
            assertTrue(true);

        } finally {
            ThreadContext.pop();
        }
    }
}
