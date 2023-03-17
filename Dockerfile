FROM gradle:7.6.1-jdk17 as builder

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle --no-daemon installDist

FROM openjdk:17-jdk-slim
COPY --from=builder /home/gradle/src/build/install/esphome2influxdb /app
COPY src/main/resources/esphome2influxdb.yaml /app/conf/esphome2influxdb.yaml
RUN mkdir /app/logs

VOLUME /app/conf /app/logs

WORKDIR /app
CMD /app/bin/esphome2influxdb conf/esphome2influxdb.yaml
