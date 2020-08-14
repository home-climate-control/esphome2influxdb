package com.homeclimatecontrol.esphome2influxdb;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class GatewayTest {
    @Test
    void appHasAGreeting() {
        Gateway classUnderTest = new Gateway();
        assertNotNull(classUnderTest.getGreeting(), "app should have a greeting");
    }
}
