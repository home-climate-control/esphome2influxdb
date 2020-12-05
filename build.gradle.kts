plugins {
    java
    application
}

repositories {
    jcenter()
}

dependencies {
    implementation("org.apache.logging.log4j:log4j-api:2.13.0")
    implementation("org.apache.logging.log4j:log4j-core:2.13.0")
    implementation("org.yaml:snakeyaml:1.26")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
}

application {
    mainClassName = "com.homeclimatecontrol.esphome2influxdb.Gateway"
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform()
}
