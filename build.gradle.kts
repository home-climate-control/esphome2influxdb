plugins {
    java
    application
}

repositories {
    jcenter()
}

dependencies {
    implementation("com.google.guava:guava:28.2-jre")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
}

application {
    mainClassName = "com.homeclimatecontrol.esphome2influxdb.App"
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform()
}
