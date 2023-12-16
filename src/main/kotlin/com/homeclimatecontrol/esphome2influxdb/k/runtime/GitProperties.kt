package com.homeclimatecontrol.esphome2influxdb.k.runtime

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Properties

class GitProperties {

    companion object {

        private var props: Properties? = null
        private val mutex = Mutex()
        fun get(): Properties {

            runBlocking {
                mutex.withLock {
                    props ?: read()
                }
            }

            return props!!
        }

        private fun read() {

            val p = Properties()
            GitProperties::class.java.classLoader.getResourceAsStream("git.properties").use {
                p.load(it)
                props = p
            }
        }
    }
}
