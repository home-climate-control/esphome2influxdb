package com.homeclimatecontrol.esphome2influxdb.k

interface Verifiable {
    /**
     * Verify the state.
     *
     * @throws IllegalArgumentException if the state doesn't make sense, or critical parts are missing.
     */
    fun verify()
}
