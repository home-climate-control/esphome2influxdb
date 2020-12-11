package com.homeclimatecontrol.esphome2influxdb;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * InfluxDB host.
 */
public class InfluxDbEndpoint extends Endpoint {

    /**
     * InfluxDB URL to connect to.
     *
     * Overrides {@code host:port}.
     */
    private String url;

    public String db = "esphome";

    public InfluxDbEndpoint() {

        setPort(8086);
    }

    public String getUrl() {

        if (url == null) {
            return "http://" + host + ":" + getPort();
        }
        return url;
    }

    public void setUrl(String url) {

        try {

            URL target = new URL(url);

            host = target.getHost();
            setPort(target.getPort());

            this.url = target.toString();

        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    protected void render(StringBuilder sb) {

        super.render(sb);

        sb.append(",url=").append(getUrl());
        sb.append(",db=").append(db);
    }
}
