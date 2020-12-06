package com.homeclimatecontrol.esphome2influxdb;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main configuration class.
 */
public class Configuration implements Verifiable {

    private final Logger logger = LogManager.getLogger();

    public Set<MqttEndpoint> sources = new LinkedHashSet<>();
    public Set<InfluxDbEndpoint> targets = new LinkedHashSet<>();
    public Set<Object> devices = new LinkedHashSet<>();

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

        try {

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

            if (!haveSources() && !haveTargets() && haveDevices()) {
                logger.error("Just the device configuration found, not starting anything");
                return false;
            }

            return true;

        } finally {

            // No matter what happens next, need to make sure the configuration is sane
            verify();
        }
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

        // VT: FIXME: A bit later; need to figure out how to load them right first

//        if (devices != null) {
//            for (Verifiable v : devices) {
//                v.verify();
//            }
//        }
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append("{");

        sb.append("sources=").append(sources).append(",");
        sb.append("targets=").append(targets).append(",");
        sb.append("devices=").append(devices);

        sb.append("}");

        return sb.toString();
    }
}
