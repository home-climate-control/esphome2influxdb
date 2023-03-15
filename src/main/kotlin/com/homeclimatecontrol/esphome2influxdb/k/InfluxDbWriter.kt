package com.homeclimatecontrol.esphome2influxdb.k

import org.apache.logging.log4j.ThreadContext
import org.influxdb.InfluxDB
import org.influxdb.InfluxDBFactory
import org.influxdb.dto.Point
import org.influxdb.dto.Query
import java.math.BigDecimal
import java.time.Clock
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class InfluxDbWriter(endpoint: InfluxDbEndpoint, stoppedGate: CountDownLatch) :
    Worker<InfluxDbEndpoint>(endpoint, stoppedGate) {

    private val clock = Clock.systemUTC()

    private var db: InfluxDB? = null
    private val queue: Queue<Sample> = LinkedBlockingQueue()
    private val QUEUE_MAX = 1024
    constructor(e: InfluxDbEndpoint,
        readers: Set<MqttReader>,
        stoppedGate: CountDownLatch
    ): this(e, stoppedGate) {
        for (r in readers) {
            r.attach(this)
        }
    }

    override fun run() {
        ThreadContext.push("run")

        try {
            logger.info("Started")
            connect()
            db!!.enableBatch()
            db!!.query(Query("CREATE DATABASE " + endpoint.db))
            db!!.setDatabase(endpoint.db)

            // VT: FIXME: Implement the rest of the lifecycle
            while (true) {
                Thread.sleep(60000)
            }
        } catch (ex: InterruptedException) {
            logger.error("Interrupted, terminating", ex)
        } finally {
            stoppedGate.countDown()
            logger.info("Shut down")
            ThreadContext.pop()
        }
    }

    /**
     * Connect to the remote in a non-blocking way.
     */
    private fun connect() {
        ThreadContext.push("connect")
        try {
            val db: InfluxDB
            val start = clock.instant().toEpochMilli()

            // This section will not block synchronized calls
            db =
                if (endpoint.username == null || "" == endpoint.username || endpoint.password == null || "" == endpoint.password) {
                    logger.warn("one of (username, password) is null or missing, connecting unauthenticated - THIS IS A BAD IDEA")
                    logger.warn("see https://docs.influxdata.com/influxdb/v1.8/administration/authentication_and_authorization/")
                    logger.warn("(username, password) = ({}, {})", endpoint.username, endpoint.password)
                    InfluxDBFactory.connect(endpoint.getUrl())
                } else {
                    InfluxDBFactory.connect(endpoint.getUrl(), endpoint.username, endpoint.password)
                }
            val end = clock.instant().toEpochMilli()
            logger.info("connected to {} in {}ms", endpoint.getUrl(), end - start)

            // This section is short and won't delay other synchronized calls much
            synchronized(this) { this.db = db }
        } finally {
            ThreadContext.pop()
        }
    }

    fun consume(timestamp: Long, device: Device, payload: String) {
        ThreadContext.push("consume")
        try {
            logger.debug("payload: {}", payload)
            val s = Sample(timestamp, device, payload)
            if (queue.size < QUEUE_MAX) {

                // The cost of doing this all this time is negligible
                queue.add(s)
            } else {
                logger.error("QUEUE_MAX={} exceeded, skipping sample: {}", QUEUE_MAX, s)
            }

            synchronized(this) {

                // This happens at startup, when the connection is not yet established,
                // but the instance is ready to accept samples
                if (db == null) {
                    logger.warn("no connection yet, {} sample[s] deferred", queue.size)
                    return
                }
            }

            flush(db!!, queue)

        } finally {
            ThreadContext.pop()
        }
    }

    /**
     * Flush the queue content.
     *
     * It is possible for more than one thread to call consume() a the same time,
     * MQTT receiver callbacks are asynchronous, hence synchronized modifier.
     *
     * @param db Writer to write to.
     * @param queue Queue to flush.
     */
    @Synchronized
    fun flush(db: InfluxDB, queue: Queue<Sample>) {
        while (!queue.isEmpty()) {
            try {
                val sample = queue.peek()

                // VT: FIXME: This will only work for a sensor; need to change sample semantics
                // for other device types

                // Known problem
                if ("nan".equals(sample.payload, ignoreCase = true)) {
                    logger.warn("NaN payload, ignored: {}", sample)
                    queue.remove()
                    continue
                }
                var p: Point
                p = try {
                    val b = Point.measurement(sample.device.getType().literal)
                        .time(sample.timestamp, TimeUnit.MILLISECONDS)
                        .tag("source", sample.device.source)
                        .tag("name", sample.device.name)
                        .tag(sample.device.tags)
                        .addField("sample", BigDecimal(sample.payload))
                    b.build()
                } catch (ex: NumberFormatException) {
                    logger.error(
                        "Can't build a point out of a sample, skipped (likely reason is a sensor failure): {}",
                        sample,
                        ex
                    )
                    queue.remove()
                    continue
                }
                db.write(p)
                queue.remove()
            } catch (ex: Exception) {

                // The item we couldn't write is still in the queue
                logger.warn("can't write sample, deferring remaining {} samples for now", queue.size, ex)
                break
            }
        }
        db.flush()
    }

    class Sample(val timestamp: Long, val device: Device, val payload: String) {
        override fun toString(): String {
            return "{@$timestamp: device=$device, payload=$payload}"
        }
    }
}
