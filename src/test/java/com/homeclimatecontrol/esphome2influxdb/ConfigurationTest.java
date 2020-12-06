package com.homeclimatecontrol.esphome2influxdb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class ConfigurationTest {

    private final Logger logger = LogManager.getLogger();
    private final Yaml yaml = getYaml();

    private Yaml getYaml() {

        Constructor c = new Constructor(Configuration.class);
        TypeDescription td = new TypeDescription(Configuration.class);

        td.addPropertyParameters("devices", Device.class);
//        td.addPropertyParameters("sensor", Sensor.class);
//        td.addPropertyParameters("switch", Switch.class);

        c.addTypeDescription(td);

        return new Yaml(c);
    }

    @Test
    public void sources0() {

        ThreadContext.push("sources0");

        try {

            Configuration c = yaml.loadAs(
                    getClass().getClassLoader().getResourceAsStream("instantiate-configuration-0.yaml"),
                    Configuration.class);

            logger.info("loaded: {}", c);

            assertFalse(c.needToStart());

        } finally {
            ThreadContext.pop();
        }
    }

    @Test
    public void sources1() {

        ThreadContext.push("sources1");

        try {

            Configuration c = yaml.loadAs(
                    getClass().getClassLoader().getResourceAsStream("instantiate-configuration-1.yaml"),
                    Configuration.class);

            logger.info("loaded: {}", c);

            assertEquals(2, c.sources.size());

            Iterator<MqttEndpoint> is = c.sources.iterator();

            assertEquals(8888, is.next().getPort());
            assertEquals(9999, is.next().getPort());

        } finally {
            ThreadContext.pop();
        }
    }

    @Test
    public void complete() {

        ThreadContext.push("complete");

        try {

            CF c = yaml.loadAs(
                    getClass().getClassLoader().getResourceAsStream("instantiate-configuration-complete.yaml"),
                    CF.class);

            logger.info("loaded: {}", c);

        } finally {
            ThreadContext.pop();
        }
    }

    public static class CF {
        public Set<MqttEndpoint> sources = new LinkedHashSet<>();
        public Set<InfluxDbEndpoint> targets = new LinkedHashSet<>();
        public Set<Object> devices = new LinkedHashSet<>();
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
}
