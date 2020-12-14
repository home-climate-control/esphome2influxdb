# esphome2influxdb
## ESPHome to InfluxDB Gateway

Execute `./gradlew installDist` to get an executable shell script,  
and run `./build/install/esphome2influxdb/bin/esphome2influxdb ${your-config-file}.yaml`.

Or, build the Docker image and run it like this (it will expect its configuration in `esphome2influxdb.yaml`):  
```
docker run --name=esphome2influxdb \
  -v ${your-config-directory}:/app/conf \
  -v ${your-logs-directory}:/app/logs esphome2influxdb
```
