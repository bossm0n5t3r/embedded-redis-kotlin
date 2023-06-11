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
    implementation("com.google.guava:guava:32.0.1-jre")
    implementation("commons-io:commons-io:2.13.0")

    testImplementation(kotlin("test"))
    testImplementation("redis.clients:jedis:5.0.0-beta1")
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
