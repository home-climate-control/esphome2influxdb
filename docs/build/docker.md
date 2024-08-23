esphome2influxdb: Build for (not with) Docker
==

## Pull

Run `docker pull climategadgets/esphome2influxdb` and then proceed to [configuration section](#configure-and-run) below - don't forget to adjust the image name to `climategadgets/esphome2influxdb`.

Just in case, the image is hosted at https://hub.docker.com/r/climategadgets/esphome2influxdb.

## Build

Simply run `./gradlew jibDockerBuild`, this will build the image into the local Docker container (might need `sudo` if Docker is only configured to run as `root`).
See [Jib Gradle Plugin](https://github.com/GoogleContainerTools/jib/tree/master/jib-gradle-plugin) docs for details.
Mind the image name, it will be different for different release trains.

### Build on Raspberry Pi

The above will work seamlessly only on `amd64` architectures. If you need to build the image on Raspberry Pi, apply the following diff:

```
diff --git a/build.gradle.kts b/build.gradle.kts
index e38dbe8..1eaebe3 100644
--- a/build.gradle.kts
+++ b/build.gradle.kts
@@ -74,6 +74,16 @@ sonarqube {

 jib {

+    from {
+        platforms {
+            platform {
+                architecture = "arm"
+                os = "linux"
+            }
+        }
+    }
+
     to {
         image ="climategadgets/esphome2influxdb"
     }
```

> NOTE: Ideally, this should be seamless, but it is not quite trivial and there's no demand. If it bothers you, please [submit a ticket](https://github.com/home-climate-control/esphome2influxdb/issues).

## Configure and Run

By default, the `esphome2influxdb` Docker image will be created with configuration found in `src/main/resources/esphome2influxdb.yaml` (specifically, source host of `mqtt-esphome` and target host of `influxdb-esphome`).

To make it do something meaningful, either create DNS records for these hosts and pass them to Docker daemon when you create the container, and/or provide your own configuration and map Docker volumes
(this is the preferred option since you will have to provide your tags at some point anyway, see [Tagging](../tagging.md) for details).

### Option 1 (default configuration, use this to make sure it works)

```
docker run \
    --name esphome2influxdb-defaultconf \
    --rm -it \
    -e TZ=${your-time-zone} \
    --dns ${your-dns-server-host} \
    --dns-search ${your-search-domain} \
    esphome2influxdb

```

With this configuration, the logs will be only stored in the container - execute `docker exec -it esphome2influxdb-defaultconf /bin/bash` to connect to it and see what's going on.

### Option 2 (custom configuration, logs exposed - this is what you'll have to do in the long run)

```
docker run \
    --name esphome2influxdb \
    --restart=unless-stopped \
    -e TZ=${your-time-zone} \
    --dns ${your-dns-server-host} \
    --dns-search ${your-search-domain} \
    -v ${your-esphome2influxdb-config-directory}:/app/conf \
    -v ${your-esphome2influxdb-log-directory}:/app/logs \
    esphome2influxdb
```

Config directory must have a `esphome2influxdb.yaml` file containing the configuration (see [Minimal Configuration](../minimal-configuration.md) for details).

---
[^^^ Index](../index.md)
