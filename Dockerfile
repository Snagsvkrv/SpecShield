# ----------------------------------------
# Stage 1: Build the application using Gradle + JDK 21
# ----------------------------------------
FROM gradle:8.7-jdk21 AS builder

WORKDIR /workspace

# Copy Gradle files first (better caching)
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon || return 0

# Copy source code
COPY src ./src

# Build the jar
RUN ./gradlew clean build -x test --no-daemon

# ----------------------------------------
# Stage 2: Create lightweight runtime image (JDK 21)
# ----------------------------------------
FROM amazoncorretto:21

WORKDIR /app

# Copy the final jar from builder stage
COPY --from=builder /workspace/build/libs/*.jar app.jar

EXPOSE 9000

ENTRYPOINT ["java", "-jar", "app.jar"]
