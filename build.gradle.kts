plugins {
    java
    application
    id("net.ltgt.errorprone") version "1.3.0"
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
    testImplementation("org.assertj:assertj-core:3.20.2")
    errorprone("com.google.errorprone:error_prone_core:2.6.0")
    errorproneJavac("com.google.errorprone:javac:9-dev-r4023-3")
}

application {
    mainClassName = "com.homeclimatecontrol.esphome2influxdb.Gateway"
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform()
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

sonarqube {
    properties {
        property("sonar.projectKey", "home-climate-control_esphome2influxdb")
        property("sonar.organization", "home-climate-control")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}
