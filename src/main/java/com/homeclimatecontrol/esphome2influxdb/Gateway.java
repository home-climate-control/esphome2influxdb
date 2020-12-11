package com.homeclimatecontrol.esphome2influxdb;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.scanner.ScannerException;

public class Gateway {

    private final Logger logger = LogManager.getLogger();

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

            if (args.length == 0) {
                logger.fatal("Usage: esphome2influxdb <YAML configuration file path or URL>");
                logger.fatal("Refusing to work without configuration, see https://github.com/home-climate-control/esphome2influxdb/wiki to get started");
                System.exit(-1);
            }

            Configuration cf = parseConfiguration(args[0]);

            execute(cf);

        } catch (Throwable t) {
            logger.fatal("Unexpected exception, terminating", t);
        } finally {
            ThreadContext.pop();
        }
    }

    private Configuration parseConfiguration(String source) {
        ThreadContext.push("parseConfiguration");

        try {

            Yaml yaml = new Yaml();

            Configuration cf = yaml.loadAs(getStream(source), Configuration.class);

            if (cf == null) {
                throw new IllegalArgumentException("No usable configuration at " + source + "?");
            }

            cf.verify();

            logger.debug("configuration: {}",  cf);

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
        URL sourceURL = new URL(source);
        return sourceURL.openStream();
    }

    /**
     * Execute the given configuration.
     *
     * @param cf Configuration to execute.
     */
    private void execute(Configuration cf) {
        ThreadContext.push("parseConfiguration");

        try {
            throw new IllegalStateException("Not Implemented");
        } finally {
            ThreadContext.pop();
        }
    }
}
