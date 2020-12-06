package com.homeclimatecontrol.esphome2influxdb;

public interface Verifiable {

    /**
     * Verify the state.
     *
     * @throws IllegalArgumentException if the state doesn't make sense, or critical parts are missing.
     */
    void verify();
}
