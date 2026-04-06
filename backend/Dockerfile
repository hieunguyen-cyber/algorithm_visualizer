# Multi-stage Docker build for Algorithm Visualizer backend

# Stage 1: Build with Maven
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src
COPY .mvn ./.mvn
COPY mvnw ./mvnw

# Build the application
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime with lightweight JRE
FROM eclipse-temurin:17-jre-jammy

# Set working directory
WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /build/target/algorithm-visualizer-*.jar app.jar

# Expose port (informational - actual port comes from PORT env variable)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:${PORT:-8080}/health || exit 1

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]
