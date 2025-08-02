# 🎯 Lesson 17: Dockerizing Your Application

## Objective

Package your Spring Boot application into Docker containers for consistent deployment across all environments. Learn multi-stage builds, container optimization, and Docker Compose for local development.

## Key Concepts

### 1. Multi-Stage Dockerfile

```dockerfile
# Build stage
FROM gradle:8.5-jdk21 AS builder

WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle/ gradle/
COPY src/ src/

RUN gradle build --no-daemon -x test

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Create non-root user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

WORKDIR /app

# Copy application jar
COPY --from=builder /app/build/libs/*.jar app.jar

# Copy additional resources if needed
COPY docker/entrypoint.sh entrypoint.sh
RUN chmod +x entrypoint.sh

# Set ownership to non-root user
RUN chown -R appuser:appgroup /app
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["./entrypoint.sh"]
CMD ["java", "-jar", "app.jar"]
```

### 2. Production-Ready Configuration

```kotlin
# application-docker.yml
server:
  port: 8080
  shutdown: graceful

spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
    
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/payments}
    username: ${DATABASE_USERNAME:payment_user}
    password: ${DATABASE_PASSWORD:payment_pass}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
    properties:
      hibernate:
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
        
  cache:
    type: redis
    redis:
      time-to-live: 600000
      
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
      
logging:
  level:
    com.learning.payment: INFO
    org.springframework.security: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### 3. Docker Compose for Development

```yaml
# docker-compose.yml
version: '3.8'

services:
  payment-app:
    build:
      context: .
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      DATABASE_URL: jdbc:postgresql://postgres:5432/payments
      DATABASE_USERNAME: payment_user
      DATABASE_PASSWORD: payment_pass
      REDIS_URL: redis://redis:6379
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
    networks:
      - payment-network
    restart: unless-stopped
    volumes:
      - app-logs:/app/logs

  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: payments
      POSTGRES_USER: payment_user
      POSTGRES_PASSWORD: payment_pass
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
      - ./docker/init-db:/docker-entrypoint-initdb.d
    networks:
      - payment-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U payment_user -d payments"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    networks:
      - payment-network
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 3s
      retries: 5
    command: redis-server --appendonly yes

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./docker/nginx/nginx.conf:/etc/nginx/nginx.conf
      - ./docker/nginx/ssl:/etc/nginx/ssl
    depends_on:
      - payment-app
    networks:
      - payment-network

volumes:
  postgres-data:
  redis-data:
  app-logs:

networks:
  payment-network:
    driver: bridge
```

### 4. Container Optimization

```dockerfile
# Optimized Dockerfile with security and performance considerations
FROM gradle:8.5-jdk21 AS builder

WORKDIR /app

# Copy dependency files first for better layer caching
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle/ gradle/

# Download dependencies (separate layer for caching)
RUN gradle dependencies --no-daemon

# Copy source code and build
COPY src/ src/
RUN gradle build --no-daemon -x test

# Extract layers for better container performance
FROM eclipse-temurin:21-jre-alpine AS extractor
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# Final runtime image
FROM eclipse-temurin:21-jre-alpine

# Install security updates and necessary tools
RUN apk --no-cache upgrade && \
    apk --no-cache add curl dumb-init && \
    rm -rf /var/cache/apk/*

# Create non-root user
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

WORKDIR /app

# Copy Spring Boot layers for optimal startup
COPY --from=extractor --chown=appuser:appgroup /app/dependencies/ ./
COPY --from=extractor --chown=appuser:appgroup /app/spring-boot-loader/ ./
COPY --from=extractor --chown=appuser:appgroup /app/snapshot-dependencies/ ./
COPY --from=extractor --chown=appuser:appgroup /app/application/ ./

USER appuser

# JVM tuning for container environments
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -XX:+UseStringDeduplication \
               -Djava.security.egd=file:/dev/./urandom"

HEALTHCHECK --interval=30s --timeout=3s --start-period=90s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["dumb-init", "--"]
CMD ["java", "$JAVA_OPTS", "org.springframework.boot.loader.JarLauncher"]
```

### 5. Environment Configuration

```bash
#!/bin/bash
# docker/entrypoint.sh

set -e

# Wait for database to be ready
echo "Waiting for database to be ready..."
while ! nc -z postgres 5432; do
  sleep 1
done
echo "Database is ready!"

# Wait for Redis to be ready
echo "Waiting for Redis to be ready..."
while ! nc -z redis 6379; do
  sleep 1
done
echo "Redis is ready!"

# Run database migrations if needed
if [ "$RUN_MIGRATIONS" = "true" ]; then
  echo "Running database migrations..."
  java -cp /app -Dspring.profiles.active=migration \
       org.springframework.boot.loader.JarLauncher \
       --spring.liquibase.enabled=true
fi

# Start the application
echo "Starting Payment Service..."
exec "$@"
```

## Docker Best Practices

### ✅ Do:
- **Use multi-stage builds** - minimize final image size
- **Run as non-root user** - improve container security
- **Implement health checks** - enable container orchestration
- **Use specific base image tags** - ensure reproducible builds
- **Optimize layer caching** - structure Dockerfile efficiently

### ❌ Avoid:
- **Including secrets in images** - use environment variables or secrets management
- **Running as root** - creates security vulnerabilities
- **Large, monolithic images** - use Alpine or distroless base images
- **Ignoring .dockerignore** - can expose sensitive files

## Production Deployment Commands

```bash
# Build optimized production image
docker build -t payment-service:latest .

# Run with production configuration
docker run -d \
  --name payment-service \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=production \
  -e DATABASE_URL=jdbc:postgresql://prod-db:5432/payments \
  -e DATABASE_USERNAME=prod_user \
  -e DATABASE_PASSWORD_FILE=/run/secrets/db_password \
  --memory=1g \
  --cpus=1.0 \
  --restart=unless-stopped \
  payment-service:latest

# Health check
curl -f http://localhost:8080/actuator/health

# View logs
docker logs payment-service --tail=100 -f

# Execute commands in running container
docker exec -it payment-service /bin/sh
```

This lesson prepares you for modern container-based deployment strategies and cloud-native applications.