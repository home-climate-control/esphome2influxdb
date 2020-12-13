plugins {
    java
    application
    id("net.ltgt.errorprone") version "1.3.0"
    jacoco
}

repositories {
    jcenter()
}

dependencies {
    implementation("org.apache.logging.log4j:log4j-api:2.13.0")
    implementation("org.apache.logging.log4j:log4j-core:2.13.0")
    implementation("org.yaml:snakeyaml:1.27")

    // https://mvnrepository.com/artifact/org.eclipse.paho/org.eclipse.paho.client.mqttv3
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.2")

    implementation("org.influxdb:influxdb-java:2.21")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testImplementation("org.mockito:mockito-core:3.6.28")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
    errorprone("com.google.errorprone:error_prone_core:2.4.0")
}

application {
    mainClassName = "com.homeclimatecontrol.esphome2influxdb.Gateway"
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform()
}
