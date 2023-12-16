[![Build Status](https://github.com/home-climate-control/esphome2influxdb/actions/workflows/gradle.yml/badge.svg)](https://github.com/home-climate-control/esphome2influxdb/actions/workflows/gradle.yml)
[![Build Status](https://github.com/home-climate-control/esphome2influxdb/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/home-climate-control/esphome2influxdb/actions/workflows/codeql-analysis.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=home-climate-control_esphome2influxdb&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=home-climate-control_esphome2influxdb)
# ESPHome to InfluxDB Gateway

## Why?

To get the [tagging](./docs/tagging.md) right, in a lightweight package.

## How?

Execute `./gradlew installDist` to get an executable shell script,  
and run `./build/install/esphome2influxdb/bin/esphome2influxdb ${your-config-file}.yaml`.

Or, [build the Docker image](./docs/build/docker.md).

For further details, see detailed documentation at [./docs](./docs/index.md).
