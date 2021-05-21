[![Build Status](https://travis-ci.com/home-climate-control/esphome2influxdb.svg)](https://travis-ci.com/home-climate-control/esphome2influxdb)
[![Build Status](https://github.com/home-climate-control/esphome2influxdb/actions/workflows/gradle.yml/badge.svg)](https://github.com/home-climate-control/esphome2influxdb/actions/workflows/gradle.yml)
[![Build Status](https://github.com/home-climate-control/esphome2influxdb/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/home-climate-control/esphome2influxdb/actions/workflows/codeql-analysis.yml)
# ESPHome to InfluxDB Gateway

## Why?

To get the [tagging](https://github.com/home-climate-control/esphome2influxdb/wiki/Tagging) right, in a lightweight package.

## How?

Execute `./gradlew installDist` to get an executable shell script,  
and run `./build/install/esphome2influxdb/bin/esphome2influxdb ${your-config-file}.yaml`.

Or, [build the Docker image](https://github.com/home-climate-control/esphome2influxdb/wiki/Build-with-Docker)
and run it like this (it will expect its configuration in `esphome2influxdb.yaml`):  
```
docker run --name=esphome2influxdb \
  -v ${your-config-directory}:/app/conf \
  -v ${your-logs-directory}:/app/logs esphome2influxdb
```

For further details, see [esphome2influxdb Wiki](https://github.com/home-climate-control/esphome2influxdb/wiki).
