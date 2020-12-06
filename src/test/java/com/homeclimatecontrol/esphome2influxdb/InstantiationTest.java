package com.homeclimatecontrol.esphome2influxdb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

public class InstantiationTest {

    private final Logger logger = LogManager.getLogger();
    private final Yaml yaml = new Yaml();

    @Test
    public void influxDbEndpoint0() {

        InfluxDbEndpoint loaded = yaml.loadAs(
                getClass().getClassLoader().getResourceAsStream("instantiate-influxdb-endpoint-0.yaml"),
                InfluxDbEndpoint.class);

        logger.info("loaded: {}", loaded);
    }
}
