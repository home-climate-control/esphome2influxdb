package com.homeclimatecontrol.esphome2influxdb.k

import com.homeclimatecontrol.esphome2influxdb.k.runtime.GitProperties
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.ThreadContext
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.scanner.ScannerException
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.concurrent.CountDownLatch
import kotlin.system.exitProcess


class Gateway {

    companion object {
        /**
         * Run the application.
         *
         * @param args Command line arguments - there is just one, configuration file URL.
         */
        @JvmStatic
        fun main(args: Array<String>) {
            Gateway().run(args)
        }
    }

    private val logger = LogManager.getLogger()

    fun run(args: Array<String>) {
        ThreadContext.push("run")

        try {

            val cf: Configuration

            if (args.isEmpty()) {

                logger.warn("Usage: esphome2influxdb <YAML configuration file path or URL>")
                logger.warn("See https://github.com/home-climate-control/esphome2influxdb/wiki to get started")
                logger.warn("Attempting to start the seed configuration with unauthenticated MQTT and InfluxDB on localhost")
                logger.warn("No persistent configuration will be stored")

                cf = Configuration()
                cf.sources!!.add(MqttEndpoint())
                cf.targets!!.add(InfluxDbEndpoint())
                cf.verify()
            } else {

                // It would be nice to tell them which version is running BEFORE trying to parse the configuration, in case versions are incompatible
                reportGitProperties()

                cf = parseConfiguration(args[0])
            }

            execute(cf)

        } catch (ex: Exception) {
            logger.fatal("Unexpected exception, terminating", ex)
        } finally {
            ThreadContext.pop()
        }
    }

    private fun reportGitProperties() {

        val p = GitProperties.get()

        logger.debug("git.branch={}", p["git.branch"])
        logger.debug("git.commit.id={}", p["git.commit.id"])
        logger.debug("git.commit.id.abbrev={}", p["git.commit.id.abbrev"])
        logger.debug("git.commit.id.describe={}", p["git.commit.id.describe"])
        logger.debug("git.build.version={}", p["git.build.version"])
    }

    private fun parseConfiguration(source: String): Configuration {
        ThreadContext.push("parseConfiguration")

        return try {
            val yaml = Yaml()
            val cf: Configuration =
                yaml.loadAs(
                    getStream(source),
                    Configuration::class.java
                )
                    ?: throw IllegalArgumentException("No usable configuration at $source?")
            cf.verify()
            logger.debug("configuration: {}", cf)

            if (!cf.needToStart()) {
                logger.info("Terminating")
                exitProcess(0)
            }

            cf

        } catch (ex: ScannerException) {
            throw IllegalArgumentException("Malformed YAML while parsing $source", ex)
        } catch (ex: Exception) {
            throw IllegalArgumentException("Unexpected exception while parsing $source", ex)
        } finally {
            ThreadContext.pop()
        }
    }

    /**
     * Execute the given configuration.
     *
     * @param cf Configuration to execute.
     */
    private fun execute(cf: Configuration) {
        ThreadContext.push("execute")

        try {
            val stopGate = CountDownLatch(1)
            val stoppedGate = CountDownLatch(cf.sources!!.size + cf.targets!!.size)
            val readers: MutableSet<MqttReader> = LinkedHashSet()
            val writers: MutableSet<InfluxDbWriter> = LinkedHashSet()
            for (e in cf.sources!!) {
                readers.add(MqttReader(e, cf.getParsedDevices(), cf.autodiscover, stopGate, stoppedGate))
            }
            for (e in cf.targets!!) {
                writers.add(InfluxDbWriter(e, readers, stoppedGate))
            }

            // Preparation is complete, start everything and enjoy the show.

            var roffset = 0
            var woffset = 0

            for (r in readers) {
                Thread(r, "thread-reader" + roffset++).start()
            }
            logger.info("Started {} reader[s]", readers.size)
            for (r in writers) {
                Thread(r, "thread-writer" + woffset++).start()
            }
            logger.info("Started {} writer[s]", writers.size)

            // Ctrl-C or SIGTERM are now the only ways to terminate this process.

            // VT: FIXME: Implement the rest of the lifecycle
            if (true) {
                while (true) {
                    Thread.sleep(60000)
                }
            }
            stopGate.countDown()
            logger.info("Shutting down")
            stoppedGate.await()
            logger.info("All workers shut down, terminating")
            throw IllegalStateException("Not Implemented")
        } catch (ex: InterruptedException) {
            logger.error("Interrupted, terminating", ex)
            Thread.currentThread().interrupt()
        } finally {
            ThreadContext.pop()
        }
    }

    /**
     * Get the stream from the given location.
     *
     * @param source Source location.
     * @return The input stream.
     * @throws IOException if things go sour.
     */
    @Throws(IOException::class)
    private fun getStream(source: String): InputStream {

        return try {
            getStreamAsFile(source)
        } catch (ex: IOException) {
            logger.trace("not a file: {}", source, ex)
            getStreamAsURL(source)
        }
    }

    /**
     * Get the stream from the given file location.
     *
     * @param source Source location as a file name.
     * @return Source stream.
     * @throws IOException if the {@code source} is not a file, or other I/O problem occurred.
     */
    private fun getStreamAsFile(source: String) = FileInputStream(source)

    /**
     * Get the stream from the given URL.
     *
     * @param source Source location as a URL.
     * @return Source stream.
     * @throws IOException if the {@code source} is not a URL, or other I/O problem occurred.
     */
    private fun getStreamAsURL(source: String) = URL(source).openStream()
}
