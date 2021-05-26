package com.homeclimatecontrol.esphome2influxdb;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class MqttReaderTest {

    @Test
    void testTopicMatch() {

        MqttEndpoint e = new MqttEndpoint();
        Map<String, Device> devices = new LinkedHashMap<>();

        String topicPrefix = "/same/mqtt/topic";
        Sensor s0 = new Sensor();
        Sensor s1 = new Sensor();

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


        CountDownLatch stopGate = new CountDownLatch(1);
        CountDownLatch stoppedGate = new CountDownLatch(1);

        MqttReader r = new MqttReader(e, devices.values(), false, stopGate, stoppedGate);

        Set<InfluxDbWriter> writers = new LinkedHashSet<>();

        String topic0 = key0 + "/state";
        String topic1 = key1 + "/state";

        InfluxDbWriter w = mock(InfluxDbWriter.class);

        ArgumentCaptor<Device> deviceCaptor = ArgumentCaptor.forClass(Device.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);

        doNothing().when(w).consume(isA(Long.class), deviceCaptor.capture(), payloadCaptor.capture());

        writers.add(w);

        String payload0 = "0";
        String payload1 = "1";

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
