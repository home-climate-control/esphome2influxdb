package com.homeclimatecontrol.esphome2influxdb;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

/**
 * Main configuration class.
 */
public class Configuration implements Verifiable {

    private final Logger logger = LogManager.getLogger();

    public boolean autodiscover = true;
    public Set<MqttEndpoint> sources = new LinkedHashSet<>();
    public Set<InfluxDbEndpoint> targets = new LinkedHashSet<>();
    public Set<Object> devices = new LinkedHashSet<>();

    private Set<Device> parsed = new LinkedHashSet<>();

    /**
     * Verify the currently loaded configuration.
     *
     * Complain if there are missing parts.
     *
     * @return true if the configuration is sufficient to start at least one source or a target.
     *
     * @throws IllegalArgumentException if the configuration doesn't make sense, or critical parts are missing.
     */
    public boolean needToStart() {

        if (!haveSources()) {
            logger.warn("No sources specified, assuming configuration test run");
        }

        if (!haveTargets()) {
            logger.warn("No targets specified, assuming configuration test run");
        }

        if (!haveDevices() && !autodiscover) {
            logger.warn("No devices specified, autodiscovery disabled, assuming configuration test run");
        }

        if (!haveSources() && !haveTargets() && !haveDevices()) {
            logger.error("Empty configuration, nothing to do");
            return false;
        }

        if (!haveSources() && !haveTargets() && haveDevices()) {
            logger.error("Just the device configuration found, not starting anything");
            return false;
        }

        return true;
    }

    private boolean haveSources() {
        return sources != null && !sources.isEmpty();
    }

    private boolean haveTargets() {
        return targets != null && !targets.isEmpty();
    }

    private boolean haveDevices() {
        return devices != null && !devices.isEmpty();
    }

    @Override
    public void verify() {

        if (sources != null) {
            for (Verifiable v : sources) {
                v.verify();
            }
        }

        if (targets != null) {
            for (Verifiable v : targets) {
                v.verify();
            }
        }

        parseDevices();

        for (Verifiable v : parsed) {
            v.verify();
        }
    }

    private void parseDevices() {

        // No YAML parsers support dynamically typed generic collections without contortions, so
        // let's dump entries and pull them back by type

        Yaml yaml = new Yaml();

        for (Object o : devices) {

            logger.trace("{}: {}", o.getClass().getName(), o);

            @SuppressWarnings({ "unchecked" })
            Map<String, String> m = (Map<String, String>) o;
            String type = m.get("type");

            if (type == null) {
                throw new IllegalArgumentException("'type' is missing in " + m);
            }

            Device.Type t;

            try {

                t = Device.Type.valueOf(type.toUpperCase());

            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("unknown type '" + type + " in " + m, ex);
            }

            logger.trace("type: {}", t);

            m.remove("type");

            StringWriter sw = new StringWriter();

            yaml.dump(m, sw);

            String dump = sw.toString();
            logger.trace("YAML:\n{}", dump);

            StringReader sr = new StringReader(dump);
            @SuppressWarnings("unchecked")
            Device d = (Device) yaml.loadAs(sr, t.cls);

            logger.trace("parsed: {}",  d);

            parsed.add(d);
        }

    }

    public Set<Device> getDevices() {

        return parsed;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append("{");

        sb.append("sources=").append(sources);
        sb.append(",targets=").append(targets);
        sb.append(",devices/raw=").append(devices);
        sb.append(",devices/parsed=").append(getDevices());

        sb.append("}");

        return sb.toString();
    }
}
