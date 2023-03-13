package com.homeclimatecontrol.esphome2influxdb;

import org.apache.logging.log4j.ThreadContext;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.time.Clock;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;

public class MqttReader extends Worker<MqttEndpoint> implements MqttCallback {

    private final Clock clock = Clock.systemUTC();

    private final boolean autodiscover;

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

    /**
     * Create an instance.
     *
     * @param e Endpoint configuration to connect to.
     * @param devices Set of previously configured devices to render the feed for.
     * @param autodiscover {@code true} if newly discovered devices get their own feed automatically.
     * @param stopGate Semaphore to listen to to initiate shutdown.
     * @param stoppedGate Semaphore to count down when the shutdown is complete.
     */
    public MqttReader(MqttEndpoint e, Collection<Device> devices, boolean autodiscover, CountDownLatch stopGate, CountDownLatch stoppedGate) {
        super(e, stoppedGate);

        this.devices = parseTopic(devices);
        this.autodiscover = autodiscover;
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

    private Map<String, Device> parseTopic(Collection<Device> source) {

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

        var options = new MqttConnectOptions();
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
        logger.error("Lost connection", cause);
        logger.info("Attempting to reconnect");
        try {
            // VT: NOTE: This may not be enough, let's see how reliable this is
            connect();
        } catch (MqttException ex) {
            logger.fatal("Reconnect failed, giving up", ex);
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        ThreadContext.push("messageArrived");

        try {

            var payload = message.toString();

            logger.debug("topic={}, message={}", topic, payload);

            if (!consume(topic, payload)) {
                autodiscover(topic, payload);
            }


        } finally {
            ThreadContext.pop();
        }
    }

    /**
     * Consume an MQTT message.
     *
     * @param topic MQTT topic.
     * @param payload MQTT message payload.
     *
     * @return {@code true} if the message was consumed.
     */
    private boolean consume(String topic, String payload) {

        for (Map.Entry<String, Device> d : devices.entrySet()) {

            // Only the first match is considered, any other way doesn't make sense

            if (consume(d, topic, payload, writers)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Consume an MQTT message if the device matches.
     *
     * @param d Device descriptor.
     * @param topic MQTT topic.
     * @param payload MQTT message payload.
     * @param writers InfluxDB writers to pass the message to.
     *
     * @return {@code true} if the message was consumed.
     */
    boolean consume(Map.Entry<String, Device> d, String topic, String payload, Set<InfluxDbWriter> writers) {

        // Save ourselves extra memory allocation
        if (!topic.startsWith(d.getKey())) {
            return false;
        }

        // Dodge https://github.com/home-climate-control/esphome2influxdb/issues/1
        // The price is one memory allocation per matching substring per message
        if (!topic.equals(d.getKey() + "/state")) {
            // Close, but no cigar
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

    static Pattern patternClimate = Pattern.compile("(.*)/climate/(.*)/mode/state");
    static Pattern patternSensor = Pattern.compile("(.*)/sensor/(.*)/state");
    static Pattern patternSwitch = Pattern.compile("(.*)/switch/(.*)/state");

    Set<String> knownTopics = new LinkedHashSet<>();
    Map<String, String> autodiscovered = new TreeMap<>();
    Set<Device> autodiscoveredDevices = new LinkedHashSet<>();

    /**
     * Autodiscover devices not specified in the configuration.
     *
     * Note: autodiscovery will be performed always, but newly discovered devices will
     * only get their feed created if {@link #autodiscover} is {@code true}.
     *
     * @param topic MQTT topic.
     * @param payload MQTT message payload (VT: FIXME: unused now,
     * but will be used later when autodiscovered devices will be activated immediately)
     */
    private void autodiscover(String topic, String payload) {
        ThreadContext.push("autodiscover");
        try {

            if (knownTopics.contains(topic)) {
                // No sense mulling it over again
                return;
            }

            knownTopics.add(topic);

            logger.debug("candidate: {}", topic);

            // VT: FIXME: Just the sensor for now

            var m = patternSensor.matcher(topic);

            if (m.matches()) {

                String topicPrefix = m.group(1);
                String name = m.group(2);

                if (!autodiscovered.containsKey(name)) {

                    logger.info("Found sensor {} at {}", name, topicPrefix);
                    autodiscovered.put(name, topicPrefix);

                    if (autodiscover) {

                        Sensor s = new Sensor(topicPrefix, name);

                        s.verify();

                        devices.put(topicPrefix + "/sensor/" + name, s);

                        renderSensorConfiguration(
                                "Starting the feed. You will still have to provide configuration for extra tags, snippet",
                                name,
                                topicPrefix);

                    } else {

                        renderSensorConfiguration(
                                "Autodiscovery is disabled, not creating a feed. Add this snippet to the configuration to create it",
                                name,
                                topicPrefix);
                    }
                }
            }

        } finally {
            ThreadContext.pop();
        }
    }

    /**
     * Render a YAML configuration snippet for the given source and topic prefix into the log.
     *
     * @param message Log message to provide.
     * @param source Sensor source.
     * @param topicPrefix Sensor topic prefix.
     */
    private void renderSensorConfiguration(String message, String source, String topicPrefix) {

        // It's simpler to just dump a string literal into the log then to fiddle with YAML here.

        logger.info("{}:\n"
                + "  - type: sensor\n"
                + "    topicPrefix: {}\n"
                + "    source: {}\n"
                + "    tags: {} # put your tags here",
                message, topicPrefix, source);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // VT: NOTE: Nothing to do here, we're not sending anything
    }

    public void attach(InfluxDbWriter writer) {
        writers.add(writer);
    }
}
