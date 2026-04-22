FROM eclipse-temurin:17-jdk-noble

WORKDIR /app

COPY target/xiaochan.jar .

ENTRYPOINT ["java", "-jar", "-Xmx128m", "xiaocan.jar"]
EXPOSE 8080