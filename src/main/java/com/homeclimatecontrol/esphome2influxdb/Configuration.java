package com.homeclimatecontrol.esphome2influxdb;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main configuration class.
 */
public class Configuration {

    private final Logger logger = LogManager.getLogger();

    public Set<MqttEndpoint> sources = new LinkedHashSet<>();
    public Set<InfluxDbEndpoint> targets = new LinkedHashSet<>();
    public Set<Device> devices = new LinkedHashSet<>();

    /**
     * Verify the currently loaded configuration.
     *
     * Complain if there are missing parts.
     *
     * @return true if the configuration is sufficient to start at least one source or a target.
     *
     * @throws IllegalArgumentException if the configuration doesn't make sense, or critical parts are missing.
     */
    public boolean verify() {

        if (!haveSources()) {
            logger.warn("No sources specified, assuming configuration test run");
        }

        if (!haveTargets()) {
            logger.warn("No targets specified, assuming configuration test run");
        }

        if (!haveDevices()) {
            logger.warn("No devices specified, assuming configuration test run");
        }

        if (!haveSources() && !haveTargets() && !haveDevices()) {
            logger.error("Empty configuration, nothing to do");
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
}
