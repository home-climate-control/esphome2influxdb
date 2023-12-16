esphome2influxdb: Docs
==

## Quickstart

1. `apt install mosquitto` or your favorite MQTT broker
1. Configure [ESPHome MQTT Client Component](https://esphome.io/components/mqtt.html) on your ESPHome devices
1. [Install InfluxDB](https://docs.influxdata.com/influxdb/v1.8/introduction/install/)
1. Build and run `esphome2influxdb` ([Gradle](./build/gradle.md) or [Docker](./build/docker.md))
1. [Configure your tags](./tagging.md)
1. PROFIT!!!

## Current Limitations

* Currently works only with [ESPHome Sensor](https://esphome.io/components/sensor/index.html) components, more to come.
* The project's just been open sourced, documentation is coming, keep checking this wiki for updates.

## Further Down the Rabbit Hole

[Home Climate Control Project](https://github.com/home-climate-control/dz)
