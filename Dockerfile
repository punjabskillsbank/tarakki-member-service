# =========================================================================
# Stage 1: Build the application JAR
# =========================================================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Build arguments for GitHub Packages authentication
ARG GITHUB_ACTOR
ARG GITHUB_TOKEN

# Expose as env vars so settings.xml can read ${env.GITHUB_ACTOR} / ${env.GITHUB_TOKEN}
ENV GITHUB_ACTOR=${GITHUB_ACTOR}
ENV GITHUB_TOKEN=${GITHUB_TOKEN}

# Copy Maven settings (GitHub Packages credentials) and project files
COPY settings.xml /app/settings.xml
COPY pom.xml /app/
COPY .mvn /app/.mvn
COPY mvnw /app/

# Download dependencies first (cached layer if pom.xml hasn't changed)
RUN mvn dependency:go-offline -s /app/settings.xml -B

# Copy source code
COPY src /app/src

# Build the application JAR (skip tests — they run in CI)
RUN mvn clean package -s /app/settings.xml -DskipTests -B

# =========================================================================
# Stage 2: Lightweight runtime image
# =========================================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy only the built JAR from the builder stage
COPY --from=builder /app/target/tarakki-member-service-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 8081

# Launch the application
ENTRYPOINT ["java", "-jar", "app.jar"]
