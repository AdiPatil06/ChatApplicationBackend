import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // Use Kotlin 2.4 to match the Gradle/Kotlin toolchain available (Gradle 9.x bundles Kotlin 2.4)
    kotlin("jvm") version "2.4.0"
    kotlin("plugin.serialization") version "2.4.0"
    application
}

group = "com.example"
version = "0.1.0"

repositories {
    mavenCentral()
}

val ktor_version = "2.3.4"
val logback_version = "1.4.7"

dependencies {
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-server-websockets:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("ch.qos.logback:logback-classic:$logback_version")

    testImplementation(kotlin("test"))
}

application {
    mainClass.set("com.example.ApplicationKt")
}


kotlin {
    // ensure the JVM toolchain used for compilation is Java 17
    jvmToolchain(17)
}

