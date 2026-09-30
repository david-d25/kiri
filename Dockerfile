# The backend, as a runnable image.
#
# The jar is built outside, by `./gradlew :build` — in CI that is the same step
# that runs the tests, so an image never exists for a commit that failed them.
# Everything else in the repository is kept out of the context by .dockerignore.
#
# The config is not baked in: compose mounts it at /app/config/application.yml,
# which `spring.config.import` picks up relative to the working directory.

FROM eclipse-temurin:21-jre-noble
WORKDIR /app
COPY build/libs/kiri.jar ./app.jar
# Never root. On the server compose overrides this with the deploy user's uid.
USER 1000:1000
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
