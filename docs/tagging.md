esphome2influxdb: Tagging
==

# The General Idea

Everyone wants their own tags and doesn't care about what others do. Here, have it your way - tags are free form.

# Examples

They're all going to look pretty similar and boring - the only difference is tags.

## Temperature Sensors

### Climate Control Zone
```
  - type: sensor
    topicPrefix: /esphome/7AC96F
    source: htu21df-bedroom-master-temperature
    tags:
      location: Master Bedroom
      zone: Master Bedroom
      building: house
      protocol: I2C
      hardware: HTU21D-F
      type: indoor
      medium: air
      measurement: temperature
      measurementUnit: °C
```
### HVAC Unit
You heed four sensors to keep an eye on a HVAC unit: air temperature on return and supply, and refrigerant temperature on pressure and suction lines.
```
  - type: sensor
    topicPrefix: /esphome/E475FA
    source: hvac-north-air-return
    tags:
      location: hvac-north
      building: workshop
      protocol: 1-Wire
      hardware: DS18B20
      type: unit
      medium: air
      measurement: temperature
      measurementUnit: °C

  - type: sensor
    topicPrefix: /esphome/E475FA
    source: hvac-north-air-supply
    tags:
      location: hvac-north
      building: workshop
      protocol: 1-Wire
      hardware: DS18B20
      type: unit
      medium: air
      measurement: temperature
      measurementUnit: °C

  - type: sensor
    topicPrefix: /esphome/E475FA
    source: hvac-north-refrigerant-suction
    tags:
      location: hvac-north
      building: workshop
      protocol: 1-Wire
      hardware: DS18B20
      type: unit
      medium: refrigerant
      measurement: temperature
      measurementUnit: °C

  - type: sensor
    topicPrefix: /esphome/E475FA
    source: hvac-north-refrigerant-pressure
    tags:
      location: hvac-north
      building: workshop
      protocol: 1-Wire
      hardware: DS18B20
      type: unit
      medium: refrigerant
      measurement: temperature
      measurementUnit: °C

```

### Water Heater
```
  - type: sensor
    topicPrefix: /esphome/45FB71
    source: water-heater-out
    tags:
      location: Water Heater
      zone: Water Heater
      building: house
      protocol: 1-Wire
      hardware: DS18B20
      type: unit
      medium: water
      measurement: temperature
      measurementUnit: °C
```
## Other Sensors
### Pressure Sensor
```
  - type: sensor
    topicPrefix: /esphome/794221
    source: bme280-bathroom-master-pressure
    tags:
      location: Master Bathroom
      building: house
      protocol: I2C
      hardware: BME280
      type: indoor
      medium: air
      measurement: pressure
      measurementUnit: hPa
```

### Humidity Sensor
```
  - type: sensor
    topicPrefix: /esphome/794221
    source: bme280-bathroom-master-humidity
    tags:
      location: Master Bathroom
      building: house
      protocol: I2C
      hardware: BME280
      type: indoor
      medium: air
      measurement: relative humidity
      measurementUnit: "%"
```
### VOC Sensor
```
  - type: sensor
    topicPrefix: /esphome/5459EA
    source: bme680-0-gas-resistance
    tags:
      location: Kitchen
      building: house
      protocol: I2C
      hardware: BME680
      type: indoor
      medium: air
      measurement: gas resistance
      measurementUnit: Ω
```

### CO2 sensor
```
  - type: sensor
    topicPrefix: /esphome/26EFE2
    source: scd40-0-co2
    tags:
      location: Workshop
      zone: Back Wall
      building: workshop
      protocol: I2C
      hardware: SCD40
      type: indoor
      medium: air
      measurement: "carbon dioxide"
      measurementUnit: ppm
      busDeviceCount: 1
      macOUI: 40F520
```

---
[^^^ Index](./index.md)
