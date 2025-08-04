# 🚀 Lesson 17 Workshop: Dockerizing Your Application

## 🎯 Workshop Objective

Build a complete containerization strategy for Spring Boot applications with multi-stage Docker builds, development environments using Docker Compose, secure production configurations, and automated deployment patterns. You'll create production-ready containers optimized for performance, security, and scalability.

**⏱️ Estimated Time**: 50 minutes

---

## 🏗️ What You'll Build

### **Complete Containerization Solution**
- **Optimized Dockerfiles**: Multi-stage builds with security and performance optimization
- **Development Environment**: Docker Compose setup with databases, caching, and monitoring
- **Environment Management**: Secure configuration for development, staging, and production
- **Container Security**: Non-root users, minimal images, vulnerability scanning
- **Production Deployment**: Health checks, orchestration-ready configurations
- **Monitoring Integration**: Prometheus, Grafana, and logging for containerized apps

### **Real-World Features**
```dockerfile
# Multi-stage optimized build
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
RUN gradle dependencies --no-daemon
COPY src/ src/
RUN gradle build --no-daemon -x test

FROM openjdk:17-jre-slim AS production
RUN groupadd -r appuser && useradd -r -g appuser appuser
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
USER appuser
HEALTHCHECK CMD curl -f http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**🎯 Success Metrics**: 
- Docker images under 200MB with security hardening
- Complete development environment with one command
- Production-ready containers with health checks and monitoring
- Automated builds with vulnerability scanning

---

## 📁 Project Structure

```
class/workshop/lesson_17/
├── Dockerfile                    # TODO: Multi-stage Docker build
├── Dockerfile.dev               # TODO: Development-specific configuration
├── .dockerignore               # TODO: Optimize build context
├── docker-compose.yml          # TODO: Complete development environment
├── docker-compose.override.yml # TODO: Development overrides
├── docker-compose.prod.yml     # TODO: Production configuration
├── docker/
│   ├── nginx/
│   │   └── nginx.conf          # TODO: Load balancer configuration
│   ├── postgres/
│   │   └── init/               # TODO: Database initialization scripts
│   ├── prometheus/
│   │   └── prometheus.yml      # TODO: Monitoring configuration
│   └── grafana/
│       ├── dashboards/         # TODO: Application dashboards
│       └── datasources/        # TODO: Data source configuration
├── k8s/
│   ├── deployment.yml          # TODO: Kubernetes deployment
│   ├── service.yml             # TODO: Kubernetes service
│   └── configmap.yml           # TODO: Configuration management
├── .env.example                # TODO: Environment variable template
├── build.gradle.kts            # ✅ Existing Spring Boot project
└── src/main/
    ├── kotlin/com/learning/docker/
    │   ├── DockerApplication.kt
    │   ├── controller/
    │   │   └── HealthController.kt    # TODO: Custom health endpoints
    │   ├── service/
    │   │   └── ApplicationService.kt  # TODO: Business logic service
    │   └── config/
    │       └── DockerConfiguration.kt # TODO: Container-specific configuration
    └── resources/
        ├── application.yml             # TODO: Base configuration
        ├── application-docker.yml      # TODO: Container configuration
        └── application-production.yml  # TODO: Production settings
```

---

## 🛠️ Step 1: Create Optimized Dockerfile

### **📝 TODO: Create basic Dockerfile**
```dockerfile
# Dockerfile
# Multi-stage build for Spring Boot application

# Stage 1: Build stage with full JDK and Gradle
FROM gradle:8.5-jdk17 AS builder

# Set working directory
WORKDIR /app

# TODO: Copy build configuration files first (for layer caching)
# HINT: Copy build.gradle.kts, settings.gradle.kts, gradle.properties
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle/ gradle/

# TODO: Download dependencies (this creates a cached layer)
# HINT: Run 'gradle dependencies --no-daemon'
RUN gradle dependencies --no-daemon

# TODO: Copy source code
# HINT: Copy src/ directory
COPY src/ src/

# TODO: Build the application
# HINT: Run 'gradle build --no-daemon -x test'
RUN gradle build --no-daemon -x test

# TODO: Extract JAR layers for better caching (Spring Boot 2.3+)
# HINT: Use jarmode=layertools to extract layers
WORKDIR /app/build/libs
RUN java -Djarmode=layertools -jar *.jar extract

# Stage 2: Production runtime with minimal JRE
FROM openjdk:17-jre-slim AS production

# TODO: Install essential packages and security updates
# HINT: Install curl for health checks, update packages for security
RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y --no-install-recommends \
    curl \
    ca-certificates && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# TODO: Create non-root user for security
# HINT: Create appuser with specific UID/GID
RUN groupadd -r -g 1001 appuser && \
    useradd -r -u 1001 -g appuser -d /app -s /sbin/nologin appuser

# Set working directory
WORKDIR /app

# TODO: Copy application layers from builder stage
# HINT: Copy dependencies, spring-boot-loader, snapshot-dependencies, application
COPY --from=builder --chown=appuser:appuser /app/build/libs/dependencies/ ./
COPY --from=builder --chown=appuser:appuser /app/build/libs/spring-boot-loader/ ./
COPY --from=builder --chown=appuser:appuser /app/build/libs/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appuser /app/build/libs/application/ ./

# TODO: Switch to non-root user
USER appuser

# TODO: Configure health check
# HINT: Use curl to check /actuator/health endpoint
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose application port
EXPOSE 8080

# TODO: Set JVM optimization for containers
ENV JVM_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

# TODO: Start application using JarLauncher
# HINT: Use org.springframework.boot.loader.JarLauncher
ENTRYPOINT ["sh", "-c", "java $JVM_OPTS org.springframework.boot.loader.JarLauncher"]
```

### **📝 TODO: Create .dockerignore**
```dockerignore
# .dockerignore
# Exclude unnecessary files from Docker build context

# Build outputs
build/
target/
*.jar
*.war

# TODO: Add development files to ignore
# HINT: IDE files, logs, temporary files
.gradle/
.idea/
*.iml
*.ipr
*.iws
.vscode/
.DS_Store

# TODO: Add runtime files
# HINT: Logs, uploads, cache directories
logs/
uploads/
*.log
*.tmp

# TODO: Add version control and CI files
# HINT: Git, GitHub Actions, documentation
.git/
.github/
*.md
docker-compose*.yml

# TODO: Add environment and secret files
# HINT: Environment files, secrets, keys
.env*
*.key
*.pem
secrets/

# Node modules and frontend builds (if any)
node_modules/
npm-debug.log*
```

---

## 🛠️ Step 2: Configure Application for Containers

### **📝 TODO: Create application-docker.yml**
```yaml
# src/main/resources/application-docker.yml
spring:
  profiles:
    active: docker
  
  # TODO: Configure database connection for containers
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://postgres:5432/dockerdb}
    username: ${DATABASE_USERNAME:dockeruser}
    password: ${DATABASE_PASSWORD:dockerpass}
    hikari:
      maximum-pool-size: ${DB_POOL_SIZE:20}
      minimum-idle: ${DB_POOL_MIN_IDLE:5}
      connection-timeout: ${DB_CONNECTION_TIMEOUT:30000}
      leak-detection-threshold: ${DB_LEAK_DETECTION:60000}
  
  # TODO: Configure Redis for container networking
  data:
    redis:
      host: ${REDIS_HOST:redis}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      timeout: ${REDIS_TIMEOUT:2000ms}
      lettuce:
        pool:
          max-active: ${REDIS_POOL_MAX:20}
          max-idle: ${REDIS_POOL_IDLE:10}
  
  # TODO: Configure JPA for containers
  jpa:
    hibernate:
      ddl-auto: ${JPA_DDL_AUTO:validate}
    show-sql: ${JPA_SHOW_SQL:false}
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

# TODO: Add application-specific configuration
app:
  name: ${APP_NAME:Docker Workshop Application}
  version: ${APP_VERSION:1.0.0}
  
  # File handling in containers
  file:
    upload-dir: ${FILE_UPLOAD_DIR:/app/uploads}
    max-size: ${FILE_MAX_SIZE:10485760}
  
  # Security configuration
  security:
    jwt:
      secret: ${JWT_SECRET:docker-workshop-secret-change-in-production}
      expiration: ${JWT_EXPIRATION:86400000}

# TODO: Configure management endpoints for containers
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,env
  endpoint:
    health:
      show-details: always
      show-components: always
  metrics:
    export:
      prometheus:
        enabled: ${PROMETHEUS_ENABLED:true}
  health:
    livenessstate:
      enabled: true
    readinessstate:
      enabled: true

# TODO: Configure logging for containers
logging:
  level:
    com.learning.docker: ${LOG_LEVEL_APP:INFO}
    org.springframework.security: ${LOG_LEVEL_SECURITY:WARN}
  pattern:
    console: "%d{ISO8601} [%thread] %-5level [%X{correlationId}] %logger{36} - %msg%n"

# TODO: Configure server for container environment
server:
  port: ${SERVER_PORT:8080}
  shutdown: graceful
  servlet:
    context-path: ${SERVER_CONTEXT_PATH:}
```

### **📝 TODO: Create HealthController.kt**
```kotlin
package com.learning.docker.controller

import org.springframework.boot.actuator.health.Health
import org.springframework.boot.actuator.health.HealthIndicator
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.time.LocalDateTime

@RestController
class HealthController : HealthIndicator {
    
    @GetMapping("/api/health")
    fun health(): Map<String, Any> {
        return mapOf(
            "status" to "UP",
            "timestamp" to Instant.now().toString(),
            "application" to "Docker Workshop",
            "version" to "1.0.0"
        )
    }
    
    @GetMapping("/api/info")
    fun info(): Map<String, Any> {
        return mapOf(
            "app" to mapOf(
                "name" to "Docker Workshop Application",
                "version" to "1.0.0",
                "environment" to System.getenv("SPRING_PROFILES_ACTIVE") ?: "default"
            ),
            "build" to mapOf(
                "time" to LocalDateTime.now().toString(),
                "java" to System.getProperty("java.version")
            ),
            "container" to mapOf(
                "hostname" to System.getenv("HOSTNAME") ?: "unknown",
                "user" to System.getProperty("user.name")
            )
        )
    }
    
    // TODO: Implement HealthIndicator interface
    override fun health(): Health {
        // TODO: Add custom health checks
        // TODO: Check database connectivity
        // TODO: Check external service availability
        
        return try {
            // Simple health check implementation
            val memoryUsage = getMemoryUsage()
            
            Health.up()
                .withDetail("memory", memoryUsage)
                .withDetail("status", "Application is running")
                .withDetail("timestamp", Instant.now().toString())
                .build()
        } catch (e: Exception) {
            Health.down()
                .withDetail("error", e.message)
                .withException(e)
                .build()
        }
    }
    
    private fun getMemoryUsage(): Map<String, Any> {
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val usedMemory = totalMemory - freeMemory
        
        return mapOf(
            "total" to totalMemory,
            "used" to usedMemory,
            "free" to freeMemory,
            "usage_percentage" to (usedMemory.toDouble() / totalMemory * 100).toInt()
        )
    }
}
```

---

## 🛠️ Step 3: Create Docker Compose Environment

### **📝 TODO: Create docker-compose.yml**
```yaml
# docker-compose.yml
version: '3.8'

services:
  # Main application
  app:
    build:
      context: .
      dockerfile: Dockerfile
      target: production
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      # TODO: Configure database connection
      - DATABASE_URL=jdbc:postgresql://postgres:5432/dockerdb
      - DATABASE_USERNAME=dockeruser
      - DATABASE_PASSWORD=dockerpass
      # TODO: Configure Redis connection
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      # TODO: Add application configuration
      - APP_NAME=Docker Workshop Application
      - LOG_LEVEL_APP=INFO
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_started
    volumes:
      - ./logs:/app/logs
      - ./uploads:/app/uploads
    networks:
      - app-network
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

  # TODO: PostgreSQL database service
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: dockerdb
      POSTGRES_USER: dockeruser
      POSTGRES_PASSWORD: dockerpass
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./docker/postgres/init:/docker-entrypoint-initdb.d
    networks:
      - app-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U dockeruser -d dockerdb"]
      interval: 10s
      timeout: 5s
      retries: 5
    restart: unless-stopped

  # TODO: Redis cache service
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - app-network
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 3s
      retries: 5
    restart: unless-stopped

  # TODO: Prometheus monitoring
  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./docker/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--web.console.libraries=/etc/prometheus/console_libraries'
      - '--web.console.templates=/etc/prometheus/consoles'
    networks:
      - app-network
    restart: unless-stopped

  # TODO: Grafana visualization
  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - grafana_data:/var/lib/grafana
    depends_on:
      - prometheus
    networks:
      - app-network
    restart: unless-stopped

# TODO: Define persistent volumes
volumes:
  postgres_data:
    driver: local
  redis_data:
    driver: local
  prometheus_data:
    driver: local
  grafana_data:
    driver: local

# TODO: Define custom network
networks:
  app-network:
    driver: bridge
```

### **📝 TODO: Create docker-compose.override.yml (Development)**
```yaml
# docker-compose.override.yml
# Development-specific overrides
version: '3.8'

services:
  app:
    build:
      context: .
      dockerfile: Dockerfile.dev  # Development-specific Dockerfile
    volumes:
      # TODO: Mount source code for live reloading
      - .:/app
      - /app/build    # Exclude build directory
      - /app/.gradle  # Exclude gradle cache
    environment:
      # TODO: Enable development features
      - SPRING_DEVTOOLS_RESTART_ENABLED=true
      - SPRING_DEVTOOLS_LIVERELOAD_ENABLED=true
      - LOG_LEVEL_APP=DEBUG
      - JPA_SHOW_SQL=true
    ports:
      # TODO: Add debug port
      - "5005:5005"
    command: ["./gradlew", "bootRun", "-Dspring.profiles.active=docker"]

  # TODO: Add development database with test data
  postgres:
    environment:
      POSTGRES_DB: dockerdb_dev
    volumes:
      - ./docker/postgres/dev-data:/docker-entrypoint-initdb.d
```

---

## 🛠️ Step 4: Create Development Dockerfile

### **📝 TODO: Create Dockerfile.dev**
```dockerfile
# Dockerfile.dev
# Development-specific Dockerfile with hot reloading

FROM gradle:8.5-jdk17

# Install development tools
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    curl \
    netcat-traditional \
    vim && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# TODO: Copy gradle configuration
# HINT: Copy build files for dependency caching
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle/ gradle/

# TODO: Download dependencies
# HINT: Pre-download dependencies for faster startup
RUN gradle dependencies --no-daemon

# TODO: Copy source code (will be overridden by volume in development)
COPY . .

# Create non-root user for development
RUN groupadd -r devuser && \
    useradd -r -g devuser -d /app -s /bin/bash devuser && \
    chown -R devuser:devuser /app

USER devuser

# Expose application and debug ports
EXPOSE 8080 5005

# TODO: Set development JVM options
ENV JAVA_TOOL_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
ENV GRADLE_OPTS="-Dorg.gradle.daemon=false"

# TODO: Default development command
# HINT: Use gradlew bootRun with development profile
CMD ["./gradlew", "bootRun", "-Dspring.profiles.active=docker", "--continuous"]
```

---

## 🛠️ Step 5: Create Configuration Files

### **📝 TODO: Create prometheus.yml**
```yaml
# docker/prometheus/prometheus.yml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  # TODO: Add alerting rules if needed

scrape_configs:
  # TODO: Configure Spring Boot application scraping
  - job_name: 'spring-boot-app'
    static_configs:
      - targets: ['app:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 10s
    
  # TODO: Configure Prometheus self-monitoring
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  # TODO: Add other services monitoring
  - job_name: 'postgres'
    static_configs:
      - targets: ['postgres:5432']
    
  - job_name: 'redis'
    static_configs:
      - targets: ['redis:6379']
```

### **📝 TODO: Create environment template**
```bash
# .env.example
# Copy to .env and update values for your environment

# Application Configuration
APP_NAME=Docker Workshop Application
APP_VERSION=1.0.0
SPRING_PROFILES_ACTIVE=docker

# Database Configuration
DATABASE_URL=jdbc:postgresql://postgres:5432/dockerdb
DATABASE_USERNAME=dockeruser
DATABASE_PASSWORD=dockerpass
DB_POOL_SIZE=20

# Redis Configuration
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=

# Security Configuration
JWT_SECRET=your-super-secure-jwt-secret-key-here
JWT_EXPIRATION=86400000

# Logging Configuration
LOG_LEVEL_APP=INFO
LOG_LEVEL_SECURITY=WARN

# File Upload Configuration
FILE_UPLOAD_DIR=/app/uploads
FILE_MAX_SIZE=10485760

# Monitoring Configuration
PROMETHEUS_ENABLED=true

# TODO: Add production-specific variables
# EXTERNAL_API_KEY=your-api-key
# EXTERNAL_SERVICE_URL=https://api.example.com
```

---

## 🛠️ Step 6: Create Production Configuration

### **📝 TODO: Create docker-compose.prod.yml**
```yaml
# docker-compose.prod.yml
# Production-specific configuration
version: '3.8'

services:
  app:
    image: myregistry.com/docker-workshop:${VERSION:-latest}
    deploy:
      replicas: 3
      update_config:
        parallelism: 1
        delay: 30s
        failure_action: rollback
        order: start-first
      restart_policy:
        condition: on-failure
        delay: 10s
        max_attempts: 3
      resources:
        limits:
          cpus: '1.0'
          memory: 1G
        reservations:
          cpus: '0.5'
          memory: 512M
    environment:
      # TODO: Production environment variables
      - SPRING_PROFILES_ACTIVE=production
      - JVM_OPTS=-Xmx768m -XX:+UseG1GC -XX:+UseContainerSupport
      - LOG_LEVEL_APP=WARN
      - JPA_SHOW_SQL=false
    secrets:
      - db_password
      - jwt_secret
    networks:
      - app-network
      - traefik-network

  postgres:
    image: postgres:15
    deploy:
      replicas: 1
      placement:
        constraints:
          - node.role == manager
    environment:
      POSTGRES_DB: proddb
      POSTGRES_USER: produser
      POSTGRES_PASSWORD_FILE: /run/secrets/db_password
    secrets:
      - db_password
    volumes:
      - postgres_prod_data:/var/lib/postgresql/data
    networks:
      - app-network

# TODO: Production volumes with different drivers
volumes:
  postgres_prod_data:
    driver: local
    driver_opts:
      type: none
      o: bind
      device: /opt/app/data/postgres

# TODO: Production networks
networks:
  app-network:
    driver: overlay
    attachable: true
  traefik-network:
    external: true

# TODO: Production secrets
secrets:
  db_password:
    external: true
  jwt_secret:
    external: true
```

---

## 🛠️ Step 7: Create Kubernetes Configuration

### **📝 TODO: Create k8s/deployment.yml**
```yaml
# k8s/deployment.yml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: docker-workshop-app
  labels:
    app: docker-workshop
    version: v1
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 1
      maxSurge: 1
  selector:
    matchLabels:
      app: docker-workshop
  template:
    metadata:
      labels:
        app: docker-workshop
        version: v1
    spec:
      # TODO: Security context
      securityContext:
        runAsNonRoot: true
        runAsUser: 1001
        fsGroup: 1001
      containers:
      - name: app
        image: myregistry.com/docker-workshop:v1.0.0
        imagePullPolicy: Always
        ports:
        - containerPort: 8080
          name: http
        # TODO: Environment configuration
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes"
        - name: DATABASE_URL
          valueFrom:
            configMapKeyRef:
              name: app-config
              key: database.url
        - name: DATABASE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: database.password
        # TODO: Resource limits
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        # TODO: Health checks
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
          timeoutSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
        # TODO: Security context
        securityContext:
          allowPrivilegeEscalation: false
          readOnlyRootFilesystem: true
          capabilities:
            drop:
            - ALL
        # TODO: Volume mounts
        volumeMounts:
        - name: tmp-volume
          mountPath: /tmp
        - name: logs-volume
          mountPath: /app/logs
      volumes:
      - name: tmp-volume
        emptyDir: {}
      - name: logs-volume
        emptyDir: {}
```

---

## 🛠️ Step 8: Build and Test Your Container

### **1. Build Docker Image**
```bash
cd class/workshop/lesson_17

# TODO: Build development image
docker build -f Dockerfile.dev -t docker-workshop:dev .

# TODO: Build production image
docker build -t docker-workshop:latest .

# TODO: Check image size
docker images docker-workshop
```

### **2. Test Container Locally**
```bash
# TODO: Run container locally
docker run -d \
  --name docker-workshop-test \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  docker-workshop:latest

# TODO: Check container health
docker ps
docker logs docker-workshop-test

# TODO: Test application endpoints
curl http://localhost:8080/api/health
curl http://localhost:8080/actuator/health

# TODO: Clean up
docker stop docker-workshop-test
docker rm docker-workshop-test
```

### **3. Start Complete Environment**
```bash
# TODO: Start development environment
docker-compose up -d

# TODO: Check all services
docker-compose ps

# TODO: View logs
docker-compose logs -f app

# TODO: Test application with database
curl http://localhost:8080/api/health
curl http://localhost:8080/actuator/health
```

### **4. Test Production Configuration**
```bash
# TODO: Start production environment
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d

# TODO: Scale application
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d --scale app=3

# TODO: Test load balancing
for i in {1..10}; do
  curl http://localhost:8080/api/info | jq '.container.hostname'
  sleep 1
done
```

---

## 🛠️ Step 9: Optimize and Secure Container

### **📝 TODO: Create security scanning script**
```bash
#!/bin/bash
# scripts/security-scan.sh

echo "🔍 Scanning Docker image for vulnerabilities..."

# TODO: Install Trivy if not present
if ! command -v trivy &> /dev/null; then
    echo "Installing Trivy..."
    curl -sfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/install.sh | sh -s -- -b /usr/local/bin
fi

# TODO: Scan image for vulnerabilities
echo "Scanning docker-workshop:latest..."
trivy image docker-workshop:latest

# TODO: Scan for misconfigurations
echo "Scanning Dockerfile for best practices..."
trivy config .

# TODO: Generate report
trivy image --format json --output security-report.json docker-workshop:latest

echo "✅ Security scan complete. Check security-report.json for details."
```

### **📝 TODO: Create optimization script**
```bash
#!/bin/bash
# scripts/optimize.sh

echo "⚡ Optimizing Docker setup..."

# TODO: Check image size
echo "Current image sizes:"
docker images docker-workshop

# TODO: Clean up unused images and containers
echo "Cleaning up Docker..."
docker system prune -f

# TODO: Optimize image layers
echo "Analyzing image layers..."
docker history docker-workshop:latest

# TODO: Check for security issues
echo "Checking for security issues..."
docker run --rm -i hadolint/hadolint < Dockerfile

echo "✅ Optimization complete!"
```

---

## 🛠️ Step 10: Create Deployment Scripts

### **📝 TODO: Create deployment script**
```bash
#!/bin/bash
# scripts/deploy.sh

set -e

VERSION=${1:-latest}
ENVIRONMENT=${2:-development}

echo "🚀 Deploying docker-workshop:$VERSION to $ENVIRONMENT..."

# TODO: Build image with version tag
echo "Building image..."
docker build -t docker-workshop:$VERSION .

# TODO: Tag for registry
if [ "$ENVIRONMENT" != "development" ]; then
    docker tag docker-workshop:$VERSION myregistry.com/docker-workshop:$VERSION
    
    # TODO: Push to registry
    echo "Pushing to registry..."
    docker push myregistry.com/docker-workshop:$VERSION
fi

# TODO: Deploy based on environment
case $ENVIRONMENT in
    "development")
        echo "Deploying to development..."
        docker-compose up -d
        ;;
    "staging")
        echo "Deploying to staging..."
        docker-compose -f docker-compose.yml -f docker-compose.staging.yml up -d
        ;;
    "production")
        echo "Deploying to production..."
        docker stack deploy -c docker-compose.yml -c docker-compose.prod.yml app-stack
        ;;
    "kubernetes")
        echo "Deploying to Kubernetes..."
        kubectl apply -f k8s/
        kubectl rollout status deployment/docker-workshop-app
        ;;
    *)
        echo "Unknown environment: $ENVIRONMENT"
        exit 1
        ;;
esac

echo "✅ Deployment complete!"

# TODO: Run health checks
echo "Running health checks..."
sleep 30
curl -f http://localhost:8080/actuator/health || {
    echo "❌ Health check failed!"
    exit 1
}

echo "✅ Application is healthy!"
```

---

## 🚀 Step 11: Test Complete Workflow

### **1. Development Workflow**
```bash
# Start development environment
docker-compose up -d

# Make code changes and test hot reload
# Check logs: docker-compose logs -f app

# Run tests in container
docker-compose exec app ./gradlew test

# Clean up
docker-compose down
```

### **2. Production Deployment**
```bash
# Build and deploy to production
./scripts/deploy.sh v1.0.0 production

# Check deployment status
docker stack ps app-stack

# Monitor logs
docker service logs -f app-stack_app

# Scale service
docker service scale app-stack_app=5
```

### **3. Kubernetes Deployment**
```bash
# Deploy to Kubernetes
kubectl apply -f k8s/

# Check deployment
kubectl get deployments
kubectl get pods
kubectl get services

# Test application
kubectl port-forward service/docker-workshop-service 8080:80
curl http://localhost:8080/api/health
```

---

## 🎯 Expected Results

### **Container Metrics**
- **Image Size**: Production image under 200MB
- **Build Time**: Multi-stage build under 3 minutes
- **Startup Time**: Application ready in under 60 seconds
- **Memory Usage**: JVM optimized for container limits

### **Security Assessment**
- **Non-root User**: All containers run as unprivileged users
- **Vulnerability Scan**: Zero high-severity vulnerabilities
- **Minimal Attack Surface**: Distroless or minimal base images
- **Secret Management**: No secrets in images or environment variables

### **Monitoring Integration**
```bash
# Check Prometheus metrics
curl http://localhost:9090/api/v1/query?query=up

# View Grafana dashboards
open http://localhost:3000  # admin/admin

# Check application metrics
curl http://localhost:8080/actuator/prometheus | grep http_server_requests
```

---

## 🏆 Challenge Extensions

### **🔥 Bonus Challenge 1: Multi-Architecture Builds**
Create builds for both AMD64 and ARM64 architectures using Docker BuildKit.

### **🔥 Bonus Challenge 2: Zero-Downtime Deployment**
Implement blue-green deployment strategy with health checks and rollback.

### **🔥 Bonus Challenge 3: Advanced Security**
Implement image signing, runtime security monitoring, and compliance scanning.

### **🔥 Bonus Challenge 4: Performance Optimization**
Optimize JVM settings, implement application profiling, and performance testing.

### **🔥 Bonus Challenge 5: GitOps Integration**
Set up automated deployment pipeline with ArgoCD or Flux for Kubernetes.

---

## 🎓 Learning Outcomes

Upon completion, you'll have:

✅ **Created optimized Dockerfiles** with multi-stage builds reducing image size by 60%  
✅ **Built complete development environment** with Docker Compose supporting databases and monitoring  
✅ **Implemented container security** with non-root users, minimal images, and vulnerability scanning  
✅ **Configured production deployment** with health checks, resource limits, and orchestration support  
✅ **Set up monitoring integration** with Prometheus, Grafana, and application metrics  
✅ **Designed deployment automation** supporting multiple environments and platforms

**🚀 Next Lesson**: CI/CD Pipeline Setup - automating the complete build, test, and deployment workflow!