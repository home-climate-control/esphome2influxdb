package com.homeclimatecontrol.esphome2influxdb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

public class DeviceTest {

    private final Logger logger = LogManager.getLogger();
    private final Yaml yaml = new Yaml();

    @Test
    public void sensor0() {

        ThreadContext.push("sensor0");

        try {

            Sensor s = yaml.loadAs(
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
    public void sensor1() {

        ThreadContext.push("sensor1");

        try {

            Sensor s = yaml.loadAs(
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
}
