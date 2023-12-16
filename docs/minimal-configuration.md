esphome2influxdb: Minimal Configuration
==

## Before you start

Keep in mind: this configuration will just start capturing data, you need to configure [tagging](./tagging.md) to make it meaningful.

## Running as a script

This will get you going:
```
sources:
  - host: localhost

targets:
  - host: localhost
```
[[download source](https://github.com/home-climate-control/esphome2influxdb/blob/master/src/main/resources/esphome2influxdb-localhost.yaml)]

## Running in Docker

This is a bit more complicated - `localhost` refers to the container itself (which has nothing other than `esp2influxdb` running), you need to [configure container networking](https://docs.docker.com/config/containers/container-networking/) to make hostnames resolve (look for `--dns` and related options). Or, just provide the IP.
```
sources:
  - host: ${mqtt-broker-ip-or-hostname}

targets:
  - host: ${influxdb-ip-or-hostname}
```
[download source](https://github.com/home-climate-control/esphome2influxdb/blob/master/src/main/resources/esphome2influxdb.yaml)

Read more: [Build with Docker](./build/docker.md)

---
[^^^ Index](./index.md)
