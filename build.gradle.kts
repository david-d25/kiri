import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

version = "0.0.1"

plugins {
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.spring") version "2.1.10"
    kotlin("plugin.jpa") version "2.1.10"
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

tasks.processResources {
    filesMatching("*") {
        expand(project.properties)
    }
}

dependencies {
    // Telegram
    implementation("io.github.kotlin-telegram-bot.kotlin-telegram-bot:telegram:6.3.0")

    // OpenAI
    implementation(platform("com.aallam.openai:openai-client-bom:4.0.1"))
    implementation("com.aallam.openai:openai-client")

    // Anthropic
    implementation("com.anthropic:anthropic-java:0.7.0")

    runtimeOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.1.10")
    implementation("com.google.code.gson:gson:2.12.1")
    implementation("org.apache.commons:commons-text:1.13.0")
    implementation("jakarta.servlet:jakarta.servlet-api:6.1.0")
    implementation("org.postgresql:postgresql")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("org.slf4j:slf4j-api")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310") // To support ZonedDateTime
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8") // To support Optional
    implementation("com.github.ben-manes.caffeine:caffeine:3.2.0")

    // Flyway
    implementation("org.flywaydb:flyway-database-postgresql:11.3.2")

    // KTor
    runtimeOnly("io.ktor:ktor-client-okhttp:3.1.0")
    implementation("io.ktor:ktor-client-cio-jvm:3.1.0")
    implementation("io.ktor:ktor-client-content-negotiation-jvm:3.1.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:3.1.0")

    // Spring Boot
    implementation("org.springframework.boot:spring-boot")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-tomcat")
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Spring
    implementation("org.springframework:spring-context:6.2.3")
    implementation("org.springframework:spring-web:6.2.3")
    implementation("org.springframework:spring-webmvc:6.2.3")
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") } // For kotlin-telegram-bot
}
