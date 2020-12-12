package com.homeclimatecontrol.esphome2influxdb;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.apache.logging.log4j.ThreadContext;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttReader extends Worker<MqttEndpoint> implements MqttCallback {

    private Clock clock = Clock.systemDefaultZone();

    /**
     * The latch indicating the need to stop operation.
     */
    private final CountDownLatch stopGate;

    /**
     * Devices to listen to.
     *
     * The key is the topic, the value is the device descriptor.
     */
    private final Map<String, Device> devices;

    private final Set<InfluxDbWriter> writers = new LinkedHashSet<>();

    /**
     * VT: FIXME: Provide an ability to generate and keep a persistent UUID
     */
    public final String clientId = UUID.randomUUID().toString();

    private final IMqttClient client;

    public MqttReader(MqttEndpoint e, Set<Device> devices, CountDownLatch stopGate, CountDownLatch stoppedGate) {
        super(e, stoppedGate);

        this.devices = parseTopic(devices);
        this.stopGate = stopGate;

        try {
            // Only authenticate if both credentials are present
            if (endpoint.username != null && endpoint.password != null) {
                client = new MqttClient("tcp://" + endpoint.username + ":" + endpoint.password + "@" + endpoint.host + ":" + endpoint.getPort(), clientId);
            } else {
                if (endpoint.username != null) {
                    // Bad idea to have no password
                    logger.warn("Missing MQTT password, connecting unauthenticated. This behavior will not be allowed in future releases.");
                }
                client = new MqttClient("tcp://" + endpoint.host + ":" + endpoint.getPort(), clientId);
            }
        } catch (MqttException ex) {
            throw new IllegalStateException("Failed to create a client for " + endpoint);
        }
    }

    private Map<String, Device> parseTopic(Set<Device> source) {

        Map<String, Device> result = new LinkedHashMap<>();

        for (Device d : source) {
            result.put(
                    d.topicPrefix + "/" + d.getType().literal + "/" + d.source,
                    d);
        }

        return result;
    }

    @Override
    public void run() {
        ThreadContext.push("run");

        try {

            logger.info("Started");

            connect();

            stopGate.await();

            logger.info("Stopped");

        } catch (InterruptedException ex) {
            logger.error("Interrupted, terminating", ex);
            Thread.currentThread().interrupt();
        } catch (MqttException ex) {
            logger.fatal("MQTT problem", ex);
        } finally {
            stoppedGate.countDown();
            logger.info("Shut down");
            ThreadContext.pop();
        }
    }

    private void connect() throws MqttException {

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        options.setUserName(endpoint.username);

        // https://github.com/eclipse/paho.mqtt.java/issues/804
        // https://github.com/home-climate-control/dz/issues/148

        if (endpoint.password != null) {
            options.setPassword(endpoint.password.toCharArray());
        }

        client.setCallback(this);
        client.connect(options);

        client.subscribe(endpoint.topic, 0);
    }

    @Override
    public void connectionLost(Throwable cause) {
        logger.error("lost connection", cause);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        ThreadContext.push("messageArrived");

        try {

            String payload = message.toString();

            logger.debug("topic={}, message={}", topic, payload);

            for (Map.Entry<String, Device> d : devices.entrySet()) {

                // Only the first match is considered, any other way doesn't make sense

                if (consume(d, topic, payload)) {
                    break;
                }
            }

        } finally {
            ThreadContext.pop();
        }
    }

    /**
     * Consume an MQTT message.
     *
     * @param d Device descriptor.
     * @param topic MQTT topic.
     * @param payload MQTT message payload.
     *
     * @return {@code true} if the message was consumed.
     */
    private boolean consume(Map.Entry<String, Device> d, String topic, String payload) {

        if (!topic.startsWith(d.getKey())) {
            return false;
        }

        logger.debug("match: {}", d.getValue().name);

        // Let's generate the timestamp once so that several writers get the same
        long timestamp = clock.instant().toEpochMilli();

        for (InfluxDbWriter w : writers) {
            w.consume(timestamp, d.getValue(), payload);
        }

        return true;
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // VT: NOTE: Nothing to do here, we're not sending anything
    }

    public void attach(InfluxDbWriter writer) {
        writers.add(writer);
    }
}
