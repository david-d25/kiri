import com.github.gradle.node.npm.task.NpmTask

plugins {
    id("base")
    id("com.github.node-gradle.node") version "3.5.1"
}

repositories {
    mavenCentral()
}

node {
    version.set("20.19.4")
    npmVersion.set("9.8.1")
    download.set(true)
}

tasks.replace("build", NpmTask::class)
tasks.named<NpmTask>("build") {
    dependsOn("npmInstall")
    args.set(listOf("run", "build"))
}

tasks.clean {
    delete += listOf(
        "build",
        ".next"
    )
}