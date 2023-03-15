package com.homeclimatecontrol.esphome2influxdb.k

import org.apache.logging.log4j.LogManager
import org.yaml.snakeyaml.Yaml
import java.io.StringReader
import java.io.StringWriter

class Configuration : Verifiable {

    private val logger = LogManager.getLogger()

    var autodiscover: Boolean = true

    // The following are only declared nullable so that SnakeYAML doesn't choke

    var sources: MutableSet<MqttEndpoint>? = LinkedHashSet()
    var targets: MutableSet<InfluxDbEndpoint>? = LinkedHashSet()
    var devices: Set<Any>? = LinkedHashSet()

    val parsed: MutableSet<Device> = LinkedHashSet()

    fun needToStart(): Boolean {

        if (!haveSources()) {
            logger.warn("No sources specified, assuming configuration test run")
        }

        if (!haveTargets()) {
            logger.warn("No targets specified, assuming configuration test run")
        }

        if (!haveDevices() && !autodiscover) {
            logger.warn("No devices specified, autodiscovery disabled, assuming configuration test run")
        }

        if (!haveSources() && !haveTargets() && !haveDevices()) {
            logger.error("Empty configuration, nothing to do")
            return false
        }

        if (!haveSources() && !haveTargets() && haveDevices()) {
            logger.error("Just the device configuration found, not starting anything")
            return false
        }

        return true
    }

    private fun haveSources() : Boolean {
        return sources!!.isNotEmpty()
    }
    private fun haveTargets() : Boolean {
        return targets!!.isNotEmpty()
    }
    private fun haveDevices() : Boolean {
        return devices!!.isNotEmpty()
    }
    override fun verify() {

        // It is simpler to provide empty collections than to if-then-else them everywhere

        fillNulls()

        for (v in sources!!) {
            v.verify()
        }

        for (v in targets!!) {
            v.verify()
        }

        parseDevices()

        for (v in parsed) {
            v.verify()
        }
    }

    private fun fillNulls() {

        if (sources == null) {
            sources = LinkedHashSet()
        }

        if (targets == null) {
            targets = LinkedHashSet()
        }
    }

    private fun parseDevices() {

        // No YAML parsers support dynamically typed generic collections without contortions, so
        // let's dump entries and pull them back by type

        val yaml = Yaml()

        for (o in devices!!) {

            logger.trace("{}: {}", o.javaClass.name, o)
            val m = o as MutableMap<String, String>
            val type = m["type"] ?: throw IllegalArgumentException("'type' is missing in $m")

            val t = try {
                Device.Type.valueOf(type.uppercase())
            } catch (ex: IllegalArgumentException) {
                throw IllegalArgumentException("unknown type '$type in $m", ex)
            }

            logger.trace("type: {}", t)
            m.remove("type")
            val sw = StringWriter()
            yaml.dump(m, sw)
            val dump = sw.toString()
            logger.trace("YAML:\n{}", dump)
            val sr = StringReader(dump)
            val d = yaml.loadAs(sr, t.cls) as Device
            logger.trace("parsed: {}", d)
            parsed.add(d)
        }
    }

    /**
     * Get the list of devices parsed out of [devices].
     *
     * This will only return a valid result after [parseDevices] is called.
     */
    fun getParsedDevices() : Set<Device> {
        return parsed
    }

    override fun toString() : String {
        return "{sources=$sources,targets=$targets,devices/raw=$devices,devices/parsed=$parsed}"
    }
}
