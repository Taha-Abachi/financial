FROM 192.168.0.214:5000/library/gradle:9.2.1-jdk25 AS builder

WORKDIR /app

# Copy dependency files first for better layer caching
COPY build.gradle settings.gradle ./
# Download dependencies (cached unless build.gradle changes)
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY src ./src

# Build application (skip tests for faster builds)
RUN gradle clean build --no-daemon -x test

# Extract the JAR file
RUN find /app/build/libs -name "*.jar" -not -name "*-plain.jar" -exec cp {} /app/app.jar \;

FROM 192.168.0.214:5000/library/eclipse-temurin:25-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copy only the JAR file from builder
COPY --from=builder /app/app.jar app.jar

# Change ownership to non-root user
RUN chown appuser:appuser app.jar

# Switch to non-root user
USER appuser

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=staging"]
