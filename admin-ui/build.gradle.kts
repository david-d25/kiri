import com.github.gradle.node.npm.task.NpmTask

plugins {
    id("com.github.node-gradle.node") version "3.5.1"
}

repositories {
    mavenCentral()
}

node {
    version.set("18.16.0")
    npmVersion.set("9.5.1")
    download.set(true)
}

tasks.register<NpmTask>("npmBuild") {
    dependsOn("npmInstall")
    args.set(listOf("run", "build"))
}