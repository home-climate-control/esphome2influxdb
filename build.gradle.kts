plugins {
    java
    application
    id("net.ltgt.errorprone") version "3.0.1"
    jacoco
    id("org.sonarqube") version "4.0.0.2929"
    id("com.google.cloud.tools.jib") version "3.3.1"
    id("com.gorylenko.gradle-git-properties") version "2.4.1"
}

repositories {
    mavenLocal()
    mavenCentral()
}

group = "com.homeclimatecontrol.esphome2influxdb"
version = "1.0.0"

dependencies {
    implementation("org.apache.logging.log4j:log4j-api:2.20.0")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
    implementation("org.yaml:snakeyaml:1.33")

    // https://mvnrepository.com/artifact/org.eclipse.paho/org.eclipse.paho.client.mqttv3
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")

    implementation("org.influxdb:influxdb-java:2.23")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.3")
    testImplementation("org.mockito:mockito-core:5.2.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    testImplementation("org.assertj:assertj-core:3.24.2")
    errorprone("com.google.errorprone:error_prone_core:2.18.0")
}

application {
    mainClass.set("com.homeclimatecontrol.esphome2influxdb.Gateway")
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform()
}

jacoco {
    toolVersion = "0.8.10"
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

sonarqube {
    properties {
        property("sonar.projectKey", "home-climate-control_esphome2influxdb")
        property("sonar.organization", "home-climate-control")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

jib {

    to {
        image ="climategadgets/esphome2influxdb"
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
