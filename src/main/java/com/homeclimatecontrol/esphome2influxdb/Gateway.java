package com.homeclimatecontrol.esphome2influxdb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.homeclimatecontrol.esphome2influxdb.runtime.GitProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.yaml.snakeyaml.scanner.ScannerException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.concurrent.CountDownLatch;

public class Gateway {

    private final Logger logger = LogManager.getLogger();
    private final ObjectMapper objectMapper;

    private Gateway() {
        objectMapper = new ObjectMapper(new YAMLFactory());
    }

    /**
     * Run the application.
     *
     * @param args Command line arguments - there is just one, configuration file URL.
     */
    public static void main(String[] args) {
        new Gateway().run(args);
    }

    private void run(String[] args) {
        ThreadContext.push("run");

        try {

            Configuration cf;

            if (args.length == 0) {

                logger.warn("Usage: esphome2influxdb <YAML configuration file path or URL>");
                logger.warn("See https://github.com/home-climate-control/esphome2influxdb/wiki to get started");
                logger.warn("Attempting to start the seed configuration with unauthenticated MQTT and InfluxDB on localhost");
                logger.warn("No persistent configuration will be stored");

                cf = new Configuration();

                cf.sources.add(new MqttEndpoint());
                cf.targets.add(new InfluxDbEndpoint());

                cf.verify();

            } else {

                // It would be nice to tell them which version is running BEFORE trying to parse the configuration, in case versions are incompatible

                reportGitProperties();

                cf = parseConfiguration(args[0]);
            }

            execute(cf);

        } catch (Throwable t) {
            logger.fatal("Unexpected exception, terminating", t);
        } finally {
            ThreadContext.pop();
        }
    }

    private void reportGitProperties() throws IOException {

        var p = GitProperties.get();

        logger.debug("git.branch={}", p.get("git.branch"));
        logger.debug("git.commit.id={}", p.get("git.commit.id"));
        logger.debug("git.commit.id.abbrev={}", p.get("git.commit.id.abbrev"));
        logger.debug("git.commit.id.describe={}", p.get("git.commit.id.describe"));
        logger.debug("git.build.version={}", p.get("git.build.version"));
    }
    private Configuration parseConfiguration(String source) {
        ThreadContext.push("parseConfiguration");

        try {

            logger.info("Reading configuration from {}", source);

            Configuration cf = objectMapper.readValue(getStream(source), Configuration.class);

            if (cf == null) {
                throw new IllegalArgumentException("No usable configuration at " + source + "?");
            }

            cf.verify();

            var yaml = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(cf);

            logger.debug("configuration:\n{}",  yaml);

            if (!cf.needToStart()) {
                logger.info("Terminating");
                System.exit(0);
            }

            return cf;

        } catch (ScannerException ex) {
            throw new IllegalArgumentException("Malformed YAML while parsing " + source, ex);
        } catch (Throwable t) {
            throw new IllegalArgumentException("Unexpected exception while parsing " + source,  t);
        } finally {
            ThreadContext.pop();
        }
    }

    /**
     * Get the stream from the given location.
     *
     * @param source Source location.
     * @return The input stream.
     * @throws IOException if things go sour.
     */
    InputStream getStream(String source) throws IOException {

        try {

            return getStreamAsFile(source);

        } catch (IOException ex) {

            logger.trace("not a file: {}", source, ex);

            return getStreamAsURL(source);
        }
    }

    /**
     * Get the stream from the given file location.
     *
     * @param source Source location as a file name.
     * @return Source stream.
     * @throws IOException if the {@code source} is not a file, or other I/O problem occurred.
     */
    private InputStream getStreamAsFile(String source) throws IOException {
        return new FileInputStream(source);
    }

    /**
     * Get the stream from the given URL.
     *
     * @param source Source location as a URL.
     * @return Source stream.
     * @throws IOException if the {@code source} is not a URL, or other I/O problem occurred.
     */
    private InputStream getStreamAsURL(String source) throws IOException {
        return new URL(source).openStream();
    }

    /**
     * Execute the given configuration.
     *
     * @param cf Configuration to execute.
     */
    private void execute(Configuration cf) {
        ThreadContext.push("execute");

        try {

            var stopGate = new CountDownLatch(1);
            var stoppedGate = new CountDownLatch(cf.sources.size() + cf.targets.size());

            var readers = new LinkedHashSet<MqttReader>();
            var writers = new LinkedHashSet<InfluxDbWriter>();

            for (var e : cf.sources) {
                readers.add(new MqttReader(e, cf.getDevices(), cf.autodiscover, stopGate, stoppedGate));
            }

            for (var e : cf.targets) {
                writers.add(new InfluxDbWriter(e, readers, stoppedGate));
            }

            // Preparation is complete, start everything and enjoy the show.

            var roffset = 0;
            var woffset = 0;

            for (var r : readers) {
                new Thread(r, "thread-reader" + roffset++).start();
            }

            logger.info("Started {} reader[s]", readers.size());

            for (var r : writers) {
                new Thread(r, "thread-writer" + woffset++).start();
            }

            logger.info("Started {} writer[s]", writers.size());

            // Ctrl-C or SIGTERM are now the only ways to terminate this process.

            // VT: FIXME: Implement the rest of the lifecycle
            if (true) {
                while (true) {
                    Thread.sleep(60000);
                }
            }

            stopGate.countDown();

            logger.info("Shutting down");

            stoppedGate.await();

            logger.info("All workers shut down, terminating");

            throw new IllegalStateException("Not Implemented");

        } catch (InterruptedException ex) {
            logger.error("Interrupted, terminating", ex);
            Thread.currentThread().interrupt();
        } finally {
            ThreadContext.pop();
        }
    }
}
