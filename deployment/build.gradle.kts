plugins {
    id("base")
}

tasks.register<Copy>("prepareDeployment") {
    dependsOn(":admin-ui:build", ":build")

    from(project(":admin-ui").projectDir) {
        include("package*.json")
        include("next.config.js")
        include("tsconfig.json")
        include("next-env.d.ts")
        include("components/**")
        include("fonts/**")
        include("hooks/**")
        include("icons/**")
        include("lib/**")
        include("pages/**")
        include("services/**")
        include("styles/**")
        include("util/**")
        into("frontend")
    }

    from(rootProject.file("build/libs")) {
        include("${rootProject.name}-${project.version}.jar")
        rename { "app.jar" }
        into("backend")
    }

    from("src/overlay") {
        include("**/*")
        into(".")
    }

    from("src/scripts") {
        include("**/*")
        into("scripts")
    }

    into(layout.buildDirectory.dir("staging"))
}

tasks.register<Zip>("packageDeployment") {
    dependsOn("prepareDeployment")

    from(layout.buildDirectory.dir("staging"))
    archiveFileName.set("docker-compose-${project.version}.zip")
    destinationDirectory.set(file(layout.buildDirectory.dir("distributions")))
}

tasks.named("build") {
    dependsOn("packageDeployment")
}