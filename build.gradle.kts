plugins {
    kotlin("jvm") version "1.8.22"
    application
    id("org.jlleitschuh.gradle.ktlint") version "11.4.0"
}

group = "me.bossm0n5t3r"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("redis.clients:jedis:5.0.0-beta1")
    testImplementation("io.mockk:mockk:1.13.5")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
}

ktlint {
    version.set("0.49.1")
}
