package com.homeclimatecontrol.esphome2influxdb;

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

            assertNotNull(s);
            assertEquals("/esphome/67db2c", s.topicPrefix);

            s.verify();

            assertEquals("1b0300a279691428", s.source);
            assertEquals("1b0300a279691428", s.name);

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

            assertNotNull(s);
            assertEquals("/esphome/67db2c/sensor/1b0300a279691428", s.topicPrefix);

            s.verify();

            assertEquals("1b0300a279691428", s.source);
            assertEquals("1b0300a279691428", s.name);

        } finally {
            ThreadContext.pop();
        }
    }

    @Test
    void sensor2() {

        ThreadContext.push("sensor2");

        try {

            var s = yaml.loadAs(
                    getClass().getClassLoader().getResourceAsStream("instantiate-device-sensor2.yaml"),
                    Sensor.class);

            logger.info("loaded: {}", s);

            assertNotNull(s);
            assertEquals("/esphome/67db2c", s.topicPrefix);

            s.verify();
            fail("should've failed by now");

        } catch (IllegalArgumentException ex) {

            assertEquals("Short topic provided, must specify the source", ex.getMessage());
            logger.info("passed");

        } finally {
            ThreadContext.pop();
        }
    }

    @Test
    void sensor3() {

        ThreadContext.push("sensor3");

        try {

            var s = yaml.loadAs(
                    getClass().getClassLoader().getResourceAsStream("instantiate-device-sensor3.yaml"),
                    Sensor.class);

            logger.info("loaded: {}", s);

            assertNotNull(s);
            assertEquals("/esphome/67db2c/sensor/1b0300a279691428", s.topicPrefix);

            s.verify();
            fail("should've failed by now");

        } catch (IllegalArgumentException ex) {

            assertEquals("Long topic provided, must not specify the source", ex.getMessage());
            logger.info("passed");

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

            assertNotNull(s);
            assertEquals("/esphome/67db2c", s.topicPrefix);

            s.verify();

            assertEquals("1b0300a279691428", s.source);
            assertEquals("1b0300a279691428", s.name);
            assertEquals(2, s.tags.size());

            Iterator<Map.Entry<String, String>> i = s.tags.entrySet().iterator();

            Map.Entry<String, String> e1 = i.next();

            assertEquals("a", e1.getKey());
            assertEquals("0", e1.getValue());

            Map.Entry<String, String> e2 = i.next();

            assertEquals("z", e2.getKey());
            assertEquals("25", e2.getValue());

        } finally {
            ThreadContext.pop();
        }
    }
}
