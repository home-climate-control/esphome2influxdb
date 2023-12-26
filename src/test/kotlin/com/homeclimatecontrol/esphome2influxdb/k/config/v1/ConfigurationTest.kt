package com.homeclimatecontrol.esphome2influxdb.k.config.v1

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.ThreadContext
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test
import org.yaml.snakeyaml.Yaml

class ConfigurationTest {

    private val logger = LogManager.getLogger()
    private val yaml : Yaml = Yaml()

    @Test
    fun configuration0() {
        ThreadContext.push("configuration0")
        try {
            val c = yaml.loadAs(
                javaClass.classLoader.getResourceAsStream("instantiate-configuration-0.yaml"),
                Configuration::class.java
            )
            logger.info("loaded: {}", c)
            c.verify()
            assertThat(c.needToStart()).isFalse
        } finally {
            ThreadContext.pop()
        }
    }

    @Test
    fun configuration1() {
        ThreadContext.push("configuration1")
        try {
            val c = yaml.loadAs(
                javaClass.classLoader.getResourceAsStream("instantiate-configuration-1.yaml"),
                Configuration::class.java
            )
            logger.info("loaded: {}", c)
            assertThat(c!!.sources).hasSize(2)
            val `is` = c.sources!!.iterator()
            assertThat(`is`.next().port).isEqualTo(8888)
            assertThat(`is`.next().port).isEqualTo(9999)
        } finally {
            ThreadContext.pop()
        }
    }

    @Test
    fun complete() {
        ThreadContext.push("complete")
        try {
            val c = yaml.loadAs(
                javaClass.classLoader.getResourceAsStream("instantiate-configuration-complete.yaml"),
                Configuration::class.java
            )
            logger.info("loaded: {}", c)
            val targets = c!!.targets
            assertThat(targets).hasSize(3)
            run {
                val i = targets!!.iterator()
                val local = i.next()
                assertThat(local.host).isEqualTo("localhost")
                assertThat(local.port).isEqualTo(8086)
                assertThat(local.db).isEqualTo("esphome")
                val remote = i.next()
                assertThat(remote.host).isEqualTo("remote")
                assertThat(remote.port).isEqualTo(9999)
                assertThat(remote.db).isEqualTo("remote-db")
                val backup = i.next()
                assertThat(backup.host).isEqualTo("backup")
                assertThat(backup.port).isEqualTo(1111)
                assertThat(backup.db).isEqualTo("backup-db")
            }
            c.verify()
        } finally {
            ThreadContext.pop()
        }
    }

    @Test
    fun raw() {
        ThreadContext.push("raw")
        try {
            assertThatCode {
                val c = yaml.load<Any?>(
                    javaClass.classLoader.getResourceAsStream("instantiate-configuration-complete.yaml")
                )
                logger.info("loaded: {}", c)
            }.doesNotThrowAnyException()
        } finally {
            ThreadContext.pop()
        }
    }
}
