package com.homeclimatecontrol.esphome2influxdb.k

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.ThreadContext
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.yaml.snakeyaml.Yaml

class DeviceTest {

    private val logger = LogManager.getLogger()
    private val yaml : Yaml = Yaml()

    @Test
    fun sensor0() {
        ThreadContext.push("sensor0")
        try {
            val s = yaml.loadAs(
                javaClass.classLoader.getResourceAsStream("instantiate-device-sensor0.yaml"),
                Sensor::class.java
            )
            logger.info("loaded: {}", s)
            Assertions.assertThat(s).isNotNull
            Assertions.assertThat(s.topicPrefix).isEqualTo("/esphome/67db2c")
            s.verify()
            Assertions.assertThat(s.source).isEqualTo("1b0300a279691428")
            Assertions.assertThat(s.name).isEqualTo("1b0300a279691428")
        } finally {
            ThreadContext.pop()
        }
    }

    @Test
    fun sensor1() {
        ThreadContext.push("sensor1")
        try {
            val s = yaml.loadAs(
                javaClass.classLoader.getResourceAsStream("instantiate-device-sensor1.yaml"),
                Sensor::class.java
            )
            logger.info("loaded: {}", s)
            Assertions.assertThat(s).isNotNull
            Assertions.assertThat(s.topicPrefix).isEqualTo("/esphome/67db2c/sensor/1b0300a279691428")
            s.verify()
            Assertions.assertThat(s.source).isEqualTo("1b0300a279691428")
            Assertions.assertThat(s.name).isEqualTo("1b0300a279691428")
        } finally {
            ThreadContext.pop()
        }
    }

    @Test
    fun sensor2() {
        ThreadContext.push("sensor2")
        try {
            Assertions.assertThatIllegalArgumentException().isThrownBy {
                val s =
                    yaml.loadAs(
                        javaClass.classLoader.getResourceAsStream("instantiate-device-sensor2.yaml"),
                        Sensor::class.java
                    )
                logger.info("loaded: {}", s)
                Assertions.assertThat(s).isNotNull
                Assertions.assertThat(s.topicPrefix).isEqualTo("/esphome/67db2c")
                s.verify()
                org.junit.jupiter.api.Assertions.fail<Any>("should've failed by now")
            }.withMessage("Short topic provided, must specify the source")
        } finally {
            ThreadContext.pop()
        }
    }

    @Test
    fun sensor3() {
        ThreadContext.push("sensor3")
        try {
            Assertions.assertThatIllegalArgumentException().isThrownBy {
                val s =
                    yaml.loadAs(
                        javaClass.classLoader.getResourceAsStream("instantiate-device-sensor3.yaml"),
                        Sensor::class.java
                    )
                logger.info("loaded: {}", s)
                org.junit.jupiter.api.Assertions.assertNotNull(s)
                org.junit.jupiter.api.Assertions.assertEquals("/esphome/67db2c/sensor/1b0300a279691428", s.topicPrefix)
                s.verify()
                org.junit.jupiter.api.Assertions.fail<Any>("should've failed by now")
            }.withMessage("Long topic provided, must not specify the source")
        } finally {
            ThreadContext.pop()
        }
    }

    @Test
    fun sensor4() {
        ThreadContext.push("sensor4")
        try {
            val s = yaml.loadAs(
                javaClass.classLoader.getResourceAsStream("instantiate-device-sensor4.yaml"),
                Sensor::class.java
            )
            logger.info("loaded: {}", s)
            Assertions.assertThat(s).isNotNull
            Assertions.assertThat(s.topicPrefix).isEqualTo("/esphome/67db2c")
            s.verify()
            Assertions.assertThat(s.source).isEqualTo("1b0300a279691428")
            Assertions.assertThat(s.name).isEqualTo("1b0300a279691428")
            Assertions.assertThat(s.tags).hasSize(2)
            val i: Iterator<Map.Entry<String, String>> = s.tags.entries.iterator()
            val (key, value) = i.next()
            Assertions.assertThat(key).isEqualTo("a")
            Assertions.assertThat(value).isEqualTo("0")
            val (key1, value1) = i.next()
            Assertions.assertThat(key1).isEqualTo("z")
            Assertions.assertThat(value1).isEqualTo("25")
        } finally {
            ThreadContext.pop()
        }
    }
}
