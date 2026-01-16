# Multi-stage Dockerfile for CMCI CR Service

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy pom files first (for better caching)
COPY pom.xml .
COPY cr-domain/pom.xml cr-domain/
COPY cr-application/pom.xml cr-application/
COPY cr-infrastructure/pom.xml cr-infrastructure/
COPY cr-api/pom.xml cr-api/
COPY cr-bootstrap/pom.xml cr-bootstrap/

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY cr-domain/src cr-domain/src
COPY cr-application/src cr-application/src
COPY cr-infrastructure/src cr-infrastructure/src
COPY cr-api/src cr-api/src
COPY cr-bootstrap/src cr-bootstrap/src

# Build application
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy jar from builder
COPY --from=builder /app/cr-bootstrap/target/*.jar app.jar

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/api/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-jar", \
    "app.jar"]
