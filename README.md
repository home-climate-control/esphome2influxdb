[![Build Status](https://travis-ci.com/home-climate-control/esphome2influxdb.svg)](https://travis-ci.com/home-climate-control/esphome2influxdb)
[![Build Status](https://github.com/home-climate-control/esphome2influxdb/actions/workflows/gradle.yml/badge.svg)](https://github.com/home-climate-control/esphome2influxdb/actions/workflows/gradle.yml)
[![Build Status](https://github.com/home-climate-control/esphome2influxdb/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/home-climate-control/esphome2influxdb/actions/workflows/codeql-analysis.yml)
# ESPHome to InfluxDB Gateway

## Why?

To get the [tagging](https://github.com/home-climate-control/esphome2influxdb/wiki/Tagging) right, in a lightweight package.

## How?

Execute `./gradlew installDist` to get an executable shell script,  
and run `./build/install/esphome2influxdb/bin/esphome2influxdb ${your-config-file}.yaml`.

Or, [build and run the Docker image](https://github.com/home-climate-control/esphome2influxdb/wiki/Build-with-Docker).

For further details, see [esphome2influxdb Wiki](https://github.com/home-climate-control/esphome2influxdb/wiki).
