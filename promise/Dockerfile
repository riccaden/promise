# Multi-stage build für optimale Image-Größe
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Copy Maven Wrapper und pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Cleanup Maven Wrapper (Windows line endings)
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Download dependencies (Cache-Layer)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build application (skip tests for faster builds)
RUN ./mvnw clean package -DskipTests

# Production Stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install curl for healthchecks
RUN apk add --no-cache curl

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Create non-root user for security
RUN addgroup -g 1001 -S appuser && adduser -u 1001 -S appuser -G appuser
USER appuser

# Expose port (Railway sets PORT env variable)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:${PORT:-8080}/actuator/health || exit 1

# Run application with production profile
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
