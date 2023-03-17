FROM gradle:7.6.1-jdk11 as builder

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle --no-daemon installDist

FROM openjdk:11-jre-slim
COPY --from=builder /home/gradle/src/build/install/esphome2influxdb /app
COPY src/main/resources/esphome2influxdb-docker.yaml /app/conf/esphome2influxdb.yaml
RUN mkdir /app/logs

VOLUME /app/conf /app/logs

WORKDIR /app
CMD /app/bin/esphome2influxdb conf/esphome2influxdb.yaml
