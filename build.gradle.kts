import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version libs.versions.kotlin
    application
    jacoco
    alias(libs.plugins.errorprone)
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.git.properties)
    alias(libs.plugins.gradle.versions)
    alias(libs.plugins.jib)
}

repositories {
    mavenLocal()
    mavenCentral()
}

group = "com.homeclimatecontrol.esphome2influxdb"
version = "2.0.1-SNAPSHOT"

dependencies {

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.log4j.api)
    implementation(libs.log4j.core)
    implementation(libs.snakeyaml)

    // https://github.com/davidepianca98/KMQTT
    implementation(libs.kmqtt.common.jvm)
    implementation(libs.kmqtt.client.jvm)

    implementation(libs.influxdb)

    runtimeOnly(libs.kotlinx.coroutines.core.jvm)

    testImplementation(libs.junit5.api)
    testImplementation(libs.junit5.params)
    testImplementation(libs.mockito)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.assertj.core)

    testRuntimeOnly(libs.junit5.engine)

    errorprone(libs.errorprone)
}

application {
    mainClass.set("com.homeclimatecontrol.esphome2influxdb.k.Gateway")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
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

jib {

    to {
        image ="climategadgets/esphome2influxdb-k"
    }

    container {
        args = listOf("conf/esphome2influxdb.yaml")
        extraDirectories {

            paths {
                path {
                    setFrom("src/main/resources")
                    into = "${jib.container.appRoot}/app/conf"
                    includes.add("esphome2influxdb.yaml")
                }
            }
        }
        workingDirectory = "${jib.container.appRoot}/app/"
    }
}
