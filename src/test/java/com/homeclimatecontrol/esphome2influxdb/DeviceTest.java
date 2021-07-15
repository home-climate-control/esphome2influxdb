package com.homeclimatecontrol.esphome2influxdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

class DeviceTest {

    private final Logger logger = LogManager.getLogger();
    private final Yaml yaml = new Yaml();

    @Test
    void sensor0() {

        ThreadContext.push("sensor0");

        try {

            var s = yaml.loadAs(
                    getClass().getClassLoader().getResourceAsStream("instantiate-device-sensor0.yaml"),
                    Sensor.class);

            logger.info("loaded: {}", s);

            assertThat(s).isNotNull();
            assertThat(s.topicPrefix).isEqualTo("/esphome/67db2c");

            s.verify();

            assertThat(s.source).isEqualTo("1b0300a279691428");
            assertThat(s.name).isEqualTo("1b0300a279691428");

        } finally {
            ThreadContext.pop();
        }
    }

    @Test
    void sensor1() {

        ThreadContext.push("sensor1");

        try {

            var s = yaml.loadAs(
                    getClass().getClassLoader().getResourceAsStream("instantiate-device-sensor1.yaml"),
                    Sensor.class);

            logger.info("loaded: {}", s);

            assertThat(s).isNotNull();
            assertThat(s.topicPrefix).isEqualTo("/esphome/67db2c/sensor/1b0300a279691428");

            s.verify();

            assertThat(s.source).isEqualTo("1b0300a279691428");
            assertThat(s.name).isEqualTo("1b0300a279691428");

        } finally {
            ThreadContext.pop();
        }
    }

    @Test
    void sensor2() {

        ThreadContext.push("sensor2");

        try {

            assertThatIllegalArgumentException().isThrownBy(() -> {

                var s = yaml.loadAs(
                        getClass().getClassLoader().getResourceAsStream("instantiate-device-sensor2.yaml"),
                        Sensor.class);

                logger.info("loaded: {}", s);

                assertThat(s).isNotNull();
                assertThat(s.topicPrefix).isEqualTo("/esphome/67db2c");

                s.verify();
                fail("should've failed by now");

            }).withMessage("Short topic provided, must specify the source");

        } finally {
            ThreadContext.pop();
        }
    }

    @Test
    void sensor3() {

        ThreadContext.push("sensor3");

        try {

            assertThatIllegalArgumentException().isThrownBy(() -> {

                var s = yaml.loadAs(
                        getClass().getClassLoader().getResourceAsStream("instantiate-device-sensor3.yaml"),
                        Sensor.class);

                logger.info("loaded: {}", s);

                assertNotNull(s);
                assertEquals("/esphome/67db2c/sensor/1b0300a279691428", s.topicPrefix);

                s.verify();
                fail("should've failed by now");

            }).withMessage("Long topic provided, must not specify the source");

        } finally {
            ThreadContext.pop();
        }
    }

    @Test
    void sensor4() {

        ThreadContext.push("sensor4");

        try {

            var s = yaml.loadAs(
                    getClass().getClassLoader().getResourceAsStream("instantiate-device-sensor4.yaml"),
                    Sensor.class);

            logger.info("loaded: {}", s);

            assertThat(s).isNotNull();
            assertThat(s.topicPrefix).isEqualTo("/esphome/67db2c");

            s.verify();

            assertThat(s.source).isEqualTo("1b0300a279691428");
            assertThat(s.name).isEqualTo("1b0300a279691428");
            assertThat(s.tags).hasSize(2);

            Iterator<Map.Entry<String, String>> i = s.tags.entrySet().iterator();

            Map.Entry<String, String> e1 = i.next();

            assertThat(e1.getKey()).isEqualTo("a");
            assertThat(e1.getValue()).isEqualTo("0");

            Map.Entry<String, String> e2 = i.next();

            assertThat(e2.getKey()).isEqualTo("z");
            assertThat(e2.getValue()).isEqualTo("25");

        } finally {
            ThreadContext.pop();
        }
    }
}
