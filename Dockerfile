FROM openjdk:21-jdk-slim

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 9000

CMD ["java", "-jar", "app.jar"]