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
    implementation("org.apache.logging.log4j:log4j-api:2.20.0")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
    implementation("org.yaml:snakeyaml:1.33")

    // https://mvnrepository.com/artifact/org.eclipse.paho/org.eclipse.paho.client.mqttv3
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")

    implementation("org.influxdb:influxdb-java:2.23")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("org.mockito:mockito-core:5.2.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    testImplementation("org.assertj:assertj-core:3.24.2")
    errorprone("com.google.errorprone:error_prone_core:2.18.0")
    errorproneJavac("com.google.errorprone:javac:9+181-r4173-1")
}

application {
    mainClass.set("com.homeclimatecontrol.esphome2influxdb.Gateway")
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
