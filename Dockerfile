FROM eclipse-temurin:17-jdk-noble

WORKDIR /app

COPY target/xiaochan.jar .

ENTRYPOINT ["java", "-jar", "xiaochan.jar"]
EXPOSE 8080