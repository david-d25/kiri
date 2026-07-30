FROM eclipse-temurin:21-jre-noble
WORKDIR /app
COPY app.jar ./app.jar
USER 1000:1000
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
