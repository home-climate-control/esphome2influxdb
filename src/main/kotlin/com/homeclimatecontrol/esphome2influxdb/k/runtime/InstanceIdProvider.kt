package com.homeclimatecontrol.esphome2influxdb.k.runtime

import com.homeclimatecontrol.esphome2influxdb.k.Constants
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID

class InstanceIdProvider private constructor() {

    companion object {

        private val instance = InstanceIdProvider()
        private val source = File(System.getProperty("user.home"), "${Constants.PROPS_DIR_NAME}/system.id")

        fun getId(): UUID = instance.getId()
    }

    private val logger = LogManager.getLogger()
    private lateinit var id: UUID

    private fun getId(): UUID {

        synchronized(this::class.java) {
            if (!this::id.isInitialized) {
                id = readId()
            }

            return id
        }
    }

    private fun readId(): UUID {
        try {
            File(source.toString()).useLines {
                return UUID.fromString(it.iterator().next()).also {
                        id -> logger.info("persistent ID: $id (retrieved)")
                }
            }
        } catch (ex: FileNotFoundException) {

            // In case there was something insidious
            logger.debug("readId failed", ex)

            // For public consumption
            logger.info("no persistent ID found at $source, generating a new one")

            return generateId()
        }
    }

    private fun generateId(): UUID {
        return UUID.randomUUID()
            .also {
                Files.createDirectories(Paths.get("${System.getProperty("user.home")}${File.separator}${Constants.PROPS_DIR_NAME}"))
                File(source.toString()).printWriter().use { out -> out.println(it) }
                logger.info("persistent ID: $it (generated)")
            }
    }
}
