import org.gradle.kotlin.dsl.support.CompiledKotlinBuildScript
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.8.10"
    application
    id("net.ltgt.errorprone") version "3.0.1"
    jacoco
    id("org.sonarqube") version "3.2.0"
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("org.apache.logging.log4j:log4j-api:2.14.0")
    implementation("org.apache.logging.log4j:log4j-core:2.17.1")
    implementation("org.yaml:snakeyaml:2.0")

    // https://mvnrepository.com/artifact/org.eclipse.paho/org.eclipse.paho.client.mqttv3
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.2")

    implementation("org.influxdb:influxdb-java:2.21")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testImplementation("org.mockito:mockito-core:3.6.28")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    errorprone("com.google.errorprone:error_prone_core:2.18.0")
}

application {
    mainClass.set("com.homeclimatecontrol.esphome2influxdb.k.Gateway")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "17"
    }
}

sonarqube {
    properties {
        property("sonar.projectKey", "home-climate-control_esphome2influxdb")
        property("sonar.organization", "home-climate-control")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}
