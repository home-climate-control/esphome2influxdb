package com.homeclimatecontrol.esphome2influxdb.k.config.v1

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.ThreadContext
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.yaml.snakeyaml.Yaml
import java.util.UUID

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
            assertThat(s).isNotNull
            assertThat(s.topicPrefix).isEqualTo("/esphome/67db2c")
            s.verify()
            assertThat(s.source).isEqualTo("1b0300a279691428")
            assertThat(s.name).isEqualTo("1b0300a279691428")
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
            assertThat(s).isNotNull
            assertThat(s.topicPrefix).isEqualTo("/esphome/67db2c/sensor/1b0300a279691428")
            s.verify()
            assertThat(s.source).isEqualTo("1b0300a279691428")
            assertThat(s.name).isEqualTo("1b0300a279691428")
        } finally {
            ThreadContext.pop()
        }
    }

    @Test
    fun sensor2() {
        ThreadContext.push("sensor2")
        try {
            assertThatIllegalArgumentException().isThrownBy {
                val s =
                    yaml.loadAs(
                        javaClass.classLoader.getResourceAsStream("instantiate-device-sensor2.yaml"),
                        Sensor::class.java
                    )
                logger.info("loaded: {}", s)
                assertThat(s).isNotNull
                assertThat(s.topicPrefix).isEqualTo("/esphome/67db2c")
                s.verify()
                fail<Any>("should've failed by now")
            }.withMessage("Short topic provided, must specify the source")
        } finally {
            ThreadContext.pop()
        }
    }

    @Test
    fun sensor3() {
        ThreadContext.push("sensor3")
        try {
            assertThatIllegalArgumentException().isThrownBy {
                val s =
                    yaml.loadAs(
                        javaClass.classLoader.getResourceAsStream("instantiate-device-sensor3.yaml"),
                        Sensor::class.java
                    )
                logger.info("loaded: {}", s)
                assertNotNull(s)
                assertEquals("/esphome/67db2c/sensor/1b0300a279691428", s.topicPrefix)
                s.verify()
                fail<Any>("should've failed by now")
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
            assertThat(s).isNotNull
            assertThat(s.topicPrefix).isEqualTo("/esphome/67db2c")
            s.verify()
            assertThat(s.source).isEqualTo("1b0300a279691428")
            assertThat(s.name).isEqualTo("1b0300a279691428")
            assertThat(s.tags).hasSize(2)
            val i: Iterator<Map.Entry<String, String>> = s.tags.entries.iterator()
            val (key, value) = i.next()
            assertThat(key).isEqualTo("a")
            assertThat(value).isEqualTo("0")
            val (key1, value1) = i.next()
            assertThat(key1).isEqualTo("z")
            assertThat(value1).isEqualTo("25")
        } finally {
            ThreadContext.pop()
        }
    }

    @Test
    fun render() {
        val td = TestDevice()
        assertEquals(
            "{class=TestDevice,topic=null,source=null,name=null,type=climate,tags={}",
            td.toString(), "localhost:0"
        )

        val name = UUID.randomUUID().toString()
        td.name = name
        assertEquals(
            "{class=TestDevice,topic=null,source=null,name=$name,type=climate,tags={}",
            td.toString()
        )
    }

    private class TestDevice(topicPrefix: String? = null, source: String? = null) : Device(topicPrefix, source) {
        override fun getType() = Type.CLIMATE
    }
}
