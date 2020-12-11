package com.homeclimatecontrol.esphome2influxdb;

/**
 * MQTT broker endpoint.
 */
public class MqttEndpoint extends Endpoint {

    /**
     * Topic filter.
     *
     * This filter will be passed to {@link MqttReader}, unlike the filters in
     * {@link Device#topicPrefix} which will be applied locally.
     */
    public String topic = "#";

    public MqttEndpoint() {
        setPort(1883);
    }

    @Override
    protected void render(StringBuilder sb) {

        super.render(sb);

        sb.append(",topic=").append(topic);
    }
}
