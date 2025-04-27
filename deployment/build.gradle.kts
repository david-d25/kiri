import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage

plugins {
    id("com.bmuschko.docker-remote-api") version "9.4.0"
}

val buildBackend by tasks.registering {
    dependsOn(":bootJar")
}
val buildFrontend by tasks.registering {
    dependsOn(":admin-ui:npmBuild")
}

tasks.register<DockerBuildImage>("dockerBuildBackend") {
    dependsOn(buildBackend)
    inputDir.set(rootDir)
    dockerFile.set(file("deployment/Dockerfile.backend"))
    images.set(listOf("kiri-backend:${project.version}"))
}

tasks.register<DockerBuildImage>("dockerBuildFrontend") {
    dependsOn(buildFrontend)
    inputDir.set(rootDir)
    dockerFile.set(file("deployment/Dockerfile.frontend"))
    images.set(listOf("kiri-frontend:${project.version}"))
}
