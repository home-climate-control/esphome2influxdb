package com.homeclimatecontrol.esphome2influxdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
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

            assertThat(c.needToStart()).isFalse();

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

            assertThat(c.sources).hasSize(2);

            Iterator<MqttEndpoint> is = c.sources.iterator();

            assertThat(is.next().getPort()).isEqualTo(8888);
            assertThat(is.next().getPort()).isEqualTo(9999);

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

            assertThat(targets).hasSize(3);

            {
                var i = targets.iterator();

                var local = i.next();

                assertThat(local.host).isEqualTo("localhost");
                assertThat(local.getPort()).isEqualTo(8086);
                assertThat(local.db).isEqualTo("esphome");

                var remote = i.next();

                assertThat(remote.host).isEqualTo("remote");
                assertThat(remote.getPort()).isEqualTo(9999);
                assertThat(remote.db).isEqualTo("remote-db");

                var backup = i.next();

                assertThat(backup.host).isEqualTo("backup");
                assertThat(backup.getPort()).isEqualTo(1111);
                assertThat(backup.db).isEqualTo("backup-db");
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

            assertThatCode(() -> {

            var c = yaml.load(
                    getClass().getClassLoader().getResourceAsStream("instantiate-configuration-complete.yaml"));

            logger.info("loaded: {}", c);
            }).doesNotThrowAnyException();

        } finally {
            ThreadContext.pop();
        }
    }
}
