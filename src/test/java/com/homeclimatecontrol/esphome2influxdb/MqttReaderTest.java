package com.homeclimatecontrol.esphome2influxdb;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mockito.ArgumentCaptor;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

@EnabledIfEnvironmentVariable(
        named = "TEST_ESPHOME2INFLUXDB",
        matches = "safe",
        disabledReason = "Only execute this test if a suitable MQTT broker and InfluxDB database are available"
)
class MqttReaderTest {

    @Test
    void testTopicMatch() {

        var e = new MqttEndpoint();
        var devices = new LinkedHashMap<String, Device>();

        var topicPrefix = "/same/mqtt/topic";
        var s0 = new Sensor();
        var s1 = new Sensor();

        s0.topicPrefix = topicPrefix;
        s1.topicPrefix = topicPrefix;

        s0.source = "room-0-temperature";
        s1.source = "room-0-temperature-1wire";

        String key0 = topicPrefix + "/" + s0.source;
        String key1 = topicPrefix + "/" + s1.source;

        s0.verify();
        s1.verify();

        devices.put(key0, s0);
        devices.put(key1, s1);


        var stopGate = new CountDownLatch(1);
        var stoppedGate = new CountDownLatch(1);

        var r = new MqttReader(e, devices.values(), false, stopGate, stoppedGate);

        var writers = new LinkedHashSet<InfluxDbWriter>();

        var topic0 = key0 + "/state";
        var topic1 = key1 + "/state";

        var w = mock(InfluxDbWriter.class);

        var deviceCaptor = ArgumentCaptor.forClass(Device.class);
        var payloadCaptor = ArgumentCaptor.forClass(String.class);

        doNothing().when(w).consume(isA(Long.class), deviceCaptor.capture(), payloadCaptor.capture());

        writers.add(w);

        var payload0 = "0";
        var payload1 = "1";

        // Take one

        for (Map.Entry<String, Device> d : devices.entrySet()) {

            // Only the first match is considered, any other way doesn't make sense

            if (r.consume(d, topic0, payload0, writers)) {
                break;
            }
        }

        assertSame(s0, deviceCaptor.getValue());

        // Take two

        for (Map.Entry<String, Device> d : devices.entrySet()) {

            // Only the first match is considered, any other way doesn't make sense

            if (r.consume(d, topic1, payload1, writers)) {
                break;
            }
        }

        // This assertion will fail until https://github.com/home-climate-control/esphome2influxdb/issues/1 is not fixed

        assertSame(s1, deviceCaptor.getValue());
    }
}
