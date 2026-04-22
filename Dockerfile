FROM eclipse-temurin:17-jdk-noble

WORKDIR /app

COPY target/xiaocan.jar .

ENTRYPOINT ["java", "-jar", "-Xmx128m", "xiaocan.jar"]
EXPOSE 8080