# 🚀 Lesson 17: Dockerizing Your Application - Concept Guide

## 🎯 Learning Objectives

By the end of this lesson, you will:
- **Create optimized Dockerfiles** with multi-stage builds for Spring Boot applications
- **Configure Docker Compose** for local development environments with databases and services
- **Manage environment variables** and secrets securely across different deployment stages
- **Optimize container performance** with proper resource allocation and layer caching
- **Implement container security** with non-root users, minimal base images, and vulnerability scanning
- **Design production deployment** strategies with health checks, logging, and monitoring integration

---

## 🔍 Why Containerization Matters

### **The Container Revolution**
```dockerfile
# Before: "It works on my machine" 
Developer Machine: JDK 17, Gradle 8, PostgreSQL 14, Redis 6
Staging Server: JDK 11, Maven 3, MySQL 8, Redis 7
Production: JDK 8, Ant 1.10, Oracle 19c, Memcached

# After: "It works everywhere"
FROM openjdk:17-jre-slim
COPY app.jar /app/
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
# Same container runs identically everywhere
```

### **Business Benefits**
- **Consistent Environments**: Eliminate "works on my machine" issues
- **Faster Deployments**: Immutable artifacts with rollback capabilities
- **Resource Efficiency**: Optimal resource utilization and scaling
- **Development Velocity**: Simplified local development and testing
- **Infrastructure as Code**: Version-controlled infrastructure configuration

---

## 🐳 Docker Fundamentals for Spring Boot

### **Basic Dockerfile Structure**
```dockerfile
# Simple Spring Boot Dockerfile
FROM openjdk:17-jre-slim

# Create application directory
WORKDIR /app

# Copy JAR file
COPY build/libs/myapp.jar app.jar

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### **Multi-Stage Build Optimization**
```dockerfile
# Multi-stage build for optimal size and security
FROM gradle:8.5-jdk17 AS builder

# Set working directory
WORKDIR /app

# Copy build files first (for layer caching)
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle/ gradle/

# Download dependencies (cached layer)
RUN gradle dependencies --no-daemon

# Copy source code
COPY src/ src/

# Build application
RUN gradle build --no-daemon -x test

# Production stage
FROM openjdk:17-jre-slim AS production

# Install security updates and required packages
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    curl \
    netcat-traditional && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r appuser && \
    useradd -r -g appuser -d /app -s /sbin/nologin -c "App User" appuser

# Set working directory
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# JVM tuning for containers
ENV JVM_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

# Run application
ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar app.jar"]
```

### **Layer Optimization Strategies**
```dockerfile
# Optimized layering for better caching
FROM openjdk:17-jre-slim

# 1. System dependencies (rarely change)
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 2. Application dependencies (change occasionally)
COPY build/libs/lib/ /app/lib/

# 3. Application configuration (change sometimes)
COPY src/main/resources/application.yml /app/config/

# 4. Application code (change frequently)
COPY build/libs/myapp.jar /app/app.jar

# Each COPY creates a new layer - optimize order by change frequency
```

---

## 🏗️ Docker Compose for Development

### **Complete Development Environment**
```yaml
# docker-compose.yml
version: '3.8'

services:
  # Main application
  app:
    build: 
      context: .
      dockerfile: Dockerfile
      target: development  # Use development stage
    ports:
      - "8080:8080"
      - "5005:5005"  # Debug port
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/appdb
      - SPRING_DATASOURCE_USERNAME=appuser
      - SPRING_DATASOURCE_PASSWORD=apppass
      - SPRING_DATA_REDIS_HOST=redis
      - SPRING_DATA_REDIS_PORT=6379
      - JAVA_TOOL_OPTIONS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
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

  # PostgreSQL Database
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: appdb
      POSTGRES_USER: appuser
      POSTGRES_PASSWORD: apppass
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./docker/postgres/init:/docker-entrypoint-initdb.d
    networks:
      - app-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U appuser -d appdb"]
      interval: 10s
      timeout: 5s
      retries: 5
    restart: unless-stopped

  # Redis Cache
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
      - ./docker/redis/redis.conf:/usr/local/etc/redis/redis.conf
    command: redis-server /usr/local/etc/redis/redis.conf
    networks:
      - app-network
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 3s
      retries: 5
    restart: unless-stopped

  # Nginx Load Balancer (for multi-instance scenarios)
  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./docker/nginx/nginx.conf:/etc/nginx/nginx.conf
      - ./docker/nginx/ssl:/etc/nginx/ssl
    depends_on:
      - app
    networks:
      - app-network
    restart: unless-stopped

  # Monitoring with Prometheus
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

  # Grafana for visualization
  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - grafana_data:/var/lib/grafana
      - ./docker/grafana/dashboards:/etc/grafana/provisioning/dashboards
      - ./docker/grafana/datasources:/etc/grafana/provisioning/datasources
    depends_on:
      - prometheus
    networks:
      - app-network
    restart: unless-stopped

# Persistent volumes
volumes:
  postgres_data:
    driver: local
  redis_data:
    driver: local
  prometheus_data:
    driver: local
  grafana_data:
    driver: local

# Custom network for service communication
networks:
  app-network:
    driver: bridge
    ipam:
      config:
        - subnet: 172.20.0.0/16
```

### **Environment-Specific Overrides**
```yaml
# docker-compose.override.yml (local development)
version: '3.8'

services:
  app:
    build:
      target: development
    volumes:
      - .:/app
      - /app/build
      - /app/.gradle
    environment:
      - SPRING_DEVTOOLS_RESTART_ENABLED=true
      - SPRING_DEVTOOLS_LIVERELOAD_ENABLED=true
    command: ["./gradlew", "bootRun"]

# docker-compose.prod.yml (production)
version: '3.8'

services:
  app:
    build:
      target: production
    deploy:
      replicas: 3
      resources:
        limits:
          cpus: '1.0'
          memory: 1G
        reservations:
          cpus: '0.5'
          memory: 512M
    environment:
      - SPRING_PROFILES_ACTIVE=production
      - JVM_OPTS=-Xmx768m -XX:+UseG1GC
```

---

## 🔒 Environment Configuration & Secrets

### **Secure Environment Management**
```yaml
# .env file (never commit to version control)
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=production_db
DB_USERNAME=prod_user
DB_PASSWORD=super_secure_password

# Redis Configuration
REDIS_HOST=redis-cluster.example.com
REDIS_PASSWORD=redis_secure_password

# JWT Configuration
JWT_SECRET=your-super-secure-jwt-secret-key-here
JWT_EXPIRATION=86400000

# External Services
PAYMENT_SERVICE_URL=https://api.payment.example.com
PAYMENT_API_KEY=your-payment-api-key

# Monitoring
DATADOG_API_KEY=your-datadog-api-key
NEWRELIC_LICENSE_KEY=your-newrelic-license
```

### **Spring Boot Configuration for Containers**
```yaml
# application-docker.yml
spring:
  profiles:
    active: docker
  
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/appdb}
    username: ${DATABASE_USERNAME:appuser}
    password: ${DATABASE_PASSWORD:apppass}
    hikari:
      maximum-pool-size: ${DB_POOL_SIZE:20}
      minimum-idle: ${DB_POOL_MIN_IDLE:5}
      connection-timeout: ${DB_CONNECTION_TIMEOUT:30000}
  
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      timeout: ${REDIS_TIMEOUT:2000ms}
  
  jpa:
    hibernate:
      ddl-auto: ${JPA_DDL_AUTO:validate}
    show-sql: ${JPA_SHOW_SQL:false}
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true

# Application-specific configuration
app:
  jwt:
    secret: ${JWT_SECRET:default-secret-change-in-production}
    expiration: ${JWT_EXPIRATION:86400000}
  
  file:
    upload-dir: ${FILE_UPLOAD_DIR:/app/uploads}
    max-size: ${FILE_MAX_SIZE:10485760}
  
  external:
    payment-service:
      url: ${PAYMENT_SERVICE_URL:http://localhost:8081}
      api-key: ${PAYMENT_API_KEY:test-key}
      timeout: ${PAYMENT_TIMEOUT:30s}

# Actuator configuration
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: ${ACTUATOR_HEALTH_DETAILS:when-authorized}
  metrics:
    export:
      prometheus:
        enabled: ${PROMETHEUS_ENABLED:true}

# Logging configuration for containers
logging:
  level:
    com.learning: ${LOG_LEVEL_APP:INFO}
    org.springframework.security: ${LOG_LEVEL_SECURITY:WARN}
    org.hibernate.SQL: ${LOG_LEVEL_SQL:WARN}
  pattern:
    console: "%d{ISO8601} [%thread] %-5level [%X{correlationId}] %logger{36} - %msg%n"

# Server configuration
server:
  port: ${SERVER_PORT:8080}
  servlet:
    context-path: ${SERVER_CONTEXT_PATH:}
  compression:
    enabled: true
  http2:
    enabled: true
```

### **Secrets Management with Docker Swarm**
```yaml
# docker-compose.secrets.yml
version: '3.8'

services:
  app:
    image: myapp:latest
    environment:
      - DATABASE_PASSWORD_FILE=/run/secrets/db_password
      - JWT_SECRET_FILE=/run/secrets/jwt_secret
    secrets:
      - db_password
      - jwt_secret
    deploy:
      replicas: 3

secrets:
  db_password:
    external: true
  jwt_secret:
    external: true
```

```bash
# Create secrets in Docker Swarm
echo "super_secure_db_password" | docker secret create db_password -
echo "super_secure_jwt_secret" | docker secret create jwt_secret -
```

---

## ⚡ Container Optimization Techniques

### **JVM Optimization for Containers**
```dockerfile
# Optimized JVM settings for containers
FROM openjdk:17-jre-slim

# JVM tuning environment variables
ENV JVM_OPTS="\
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UseG1GC \
    -XX:G1HeapRegionSize=16m \
    -XX:+UseStringDeduplication \
    -XX:+OptimizeStringConcat \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.backgroundpreinitializer.ignore=true"

# Spring Boot specific optimizations
ENV SPRING_OPTS="\
    --spring.jmx.enabled=false \
    --spring.output.ansi.enabled=never"

# Application startup
ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar app.jar $SPRING_OPTS"]
```

### **Layer Caching Strategy**
```dockerfile
# Optimized build with dependency caching
FROM gradle:8.5-jdk17 AS deps

WORKDIR /app

# Copy only dependency-related files
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle/ gradle/

# Download and cache dependencies
RUN gradle dependencies --no-daemon

# Build stage
FROM deps AS builder

# Copy source code
COPY src/ src/

# Build application (dependencies already cached)
RUN gradle build --no-daemon -x test

# Extract layers for better caching
RUN java -Djarmode=layertools -jar build/libs/*.jar extract

# Final production image
FROM openjdk:17-jre-slim

WORKDIR /app

# Copy each layer separately for optimal caching
COPY --from=builder app/dependencies/ ./
COPY --from=builder app/spring-boot-loader/ ./
COPY --from=builder app/snapshot-dependencies/ ./
COPY --from=builder app/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
```

### **Image Size Optimization**
```dockerfile
# Multi-stage build with minimal final image
FROM gradle:8.5-jdk17-alpine AS builder

WORKDIR /app
COPY . .
RUN gradle build --no-daemon -x test

# Use distroless image for production
FROM gcr.io/distroless/java17-debian11

WORKDIR /app

# Create non-root user (distroless includes nonroot user)
USER nonroot:nonroot

# Copy only the JAR file
COPY --from=builder /app/build/libs/*.jar app.jar

# Distroless images don't have shell, so use exec form
ENTRYPOINT ["java", "-jar", "app.jar"]

# Final image size: ~180MB vs 400MB+ with full JDK
```

---

## 🛡️ Container Security Best Practices

### **Security-Hardened Dockerfile**
```dockerfile
# Security-focused Dockerfile
FROM openjdk:17-jre-slim AS base

# Update system packages and install security updates
RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y --no-install-recommends \
    curl \
    ca-certificates && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# Create application user with specific UID/GID
RUN groupadd -r -g 1001 appuser && \
    useradd -r -u 1001 -g appuser -d /app -s /sbin/nologin appuser

# Set secure permissions on application directory
WORKDIR /app
RUN chmod 755 /app

FROM base AS production

# Copy application
COPY --chown=appuser:appuser build/libs/*.jar app.jar

# Remove unnecessary packages (if any were added)
RUN apt-get autoremove -y && \
    apt-get autoclean

# Switch to non-root user
USER appuser

# Security labels
LABEL security.scan.enabled="true"
LABEL security.non-root="true"
LABEL security.minimal="true"

# Health check with security consideration
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f -s http://localhost:8080/actuator/health/readiness || exit 1

# Expose only necessary port
EXPOSE 8080

# Secure JVM settings
ENV JVM_SECURITY_OPTS="\
    -Djava.security.egd=file:/dev/./urandom \
    -Dnetworkaddress.cache.ttl=60 \
    -Djava.net.preferIPv4Stack=true"

ENTRYPOINT ["sh", "-c", "java $JVM_OPTS $JVM_SECURITY_OPTS -jar app.jar"]
```

### **Container Scanning and Security**
```yaml
# .github/workflows/security-scan.yml
name: Container Security Scan

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  security-scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Build Docker image
        run: docker build -t myapp:latest .
      
      - name: Run Trivy vulnerability scanner
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: 'myapp:latest'
          format: 'sarif'
          output: 'trivy-results.sarif'
      
      - name: Upload Trivy scan results
        uses: github/codeql-action/upload-sarif@v2
        with:
          sarif_file: 'trivy-results.sarif'
      
      - name: Docker Scout scan
        uses: docker/scout-action@v1
        with:
          command: cves
          image: myapp:latest
```

---

## 🚀 Production Deployment Patterns

### **Health Checks and Monitoring**
```dockerfile
# Production-ready health checks
FROM openjdk:17-jre-slim

# Install health check dependencies
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl jq && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

COPY build/libs/*.jar app.jar

# Comprehensive health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health/readiness | \
        jq -e '.status == "UP"' || exit 1

# Graceful shutdown support
STOPSIGNAL SIGTERM

# Application startup with proper signal handling
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### **Docker Swarm Production Deployment**
```yaml
# docker-stack.yml
version: '3.8'

services:
  app:
    image: myregistry.com/myapp:${VERSION:-latest}
    deploy:
      replicas: 3
      update_config:
        parallelism: 1
        delay: 30s
        failure_action: rollback
        order: start-first
      rollback_config:
        parallelism: 1
        delay: 30s
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
      labels:
        - "traefik.enable=true"
        - "traefik.http.routers.app.rule=Host(`api.example.com`)"
        - "traefik.http.services.app.loadbalancer.server.port=8080"
    environment:
      - SPRING_PROFILES_ACTIVE=production
      - DATABASE_URL=postgresql://postgres:5432/proddb
    secrets:
      - db_password
      - jwt_secret
    networks:
      - app-network
      - traefik-network
    volumes:
      - app-logs:/app/logs
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

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
      - postgres_data:/var/lib/postgresql/data
    networks:
      - app-network

volumes:
  postgres_data:
    driver: local
  app-logs:
    driver: local

networks:
  app-network:
    driver: overlay
    attachable: true
  traefik-network:
    external: true

secrets:
  db_password:
    external: true
  jwt_secret:
    external: true
```

### **Kubernetes Deployment**
```yaml
# k8s-deployment.yml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: myapp
  labels:
    app: myapp
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 1
      maxSurge: 1
  selector:
    matchLabels:
      app: myapp
  template:
    metadata:
      labels:
        app: myapp
    spec:
      securityContext:
        runAsNonRoot: true
        runAsUser: 1001
        fsGroup: 1001
      containers:
      - name: myapp
        image: myregistry.com/myapp:v1.0.0
        imagePullPolicy: Always
        ports:
        - containerPort: 8080
          name: http
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes"
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: database-url
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
          timeoutSeconds: 10
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        securityContext:
          allowPrivilegeEscalation: false
          readOnlyRootFilesystem: true
          capabilities:
            drop:
            - ALL
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
---
apiVersion: v1
kind: Service
metadata:
  name: myapp-service
spec:
  selector:
    app: myapp
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: ClusterIP
```

---

## 🎯 Best Practices

### **Do's**
- ✅ **Use multi-stage builds** to minimize image size
- ✅ **Run as non-root user** for security
- ✅ **Implement proper health checks** for orchestration
- ✅ **Use .dockerignore** to exclude unnecessary files
- ✅ **Pin base image versions** for reproducible builds
- ✅ **Configure JVM for containers** with appropriate memory settings
- ✅ **Use distroless or minimal base images** for production

### **Don'ts**
- ❌ **Don't include secrets in images** - use environment variables or secret management
- ❌ **Don't run as root** - always use non-privileged users
- ❌ **Don't use latest tags** in production - use specific versions
- ❌ **Don't include build tools** in production images
- ❌ **Don't ignore security scanning** - regularly scan for vulnerabilities
- ❌ **Don't forget resource limits** - always set CPU and memory constraints
- ❌ **Don't hardcode environment-specific values** in images

### **Performance Considerations**
- 🚀 **Layer Caching**: Order Dockerfile commands by change frequency
- 🚀 **Image Size**: Use multi-stage builds and minimal base images
- 🚀 **JVM Tuning**: Configure heap and GC for container environments
- 🚀 **Network Optimization**: Use overlay networks for service communication
- 🚀 **Volume Strategy**: Use appropriate volume types for different data needs

---

## 🚀 Configuration Best Practices

### **Environment Variable Management**
```bash
# Development environment
export SPRING_PROFILES_ACTIVE=development
export DATABASE_URL=jdbc:postgresql://localhost:5432/devdb
export LOG_LEVEL_APP=DEBUG

# Staging environment
export SPRING_PROFILES_ACTIVE=staging
export DATABASE_URL=jdbc:postgresql://staging-db:5432/stagingdb
export LOG_LEVEL_APP=INFO

# Production environment
export SPRING_PROFILES_ACTIVE=production
export DATABASE_URL=jdbc:postgresql://prod-db:5432/proddb
export LOG_LEVEL_APP=WARN
```

### **Docker Compose for Different Environments**
```bash
# Development
docker-compose up

# Testing
docker-compose -f docker-compose.yml -f docker-compose.test.yml up

# Production
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

---

## 🎓 Summary

Dockerizing Your Application provides:

- **🐳 Container Fundamentals**: Dockerfile creation with multi-stage builds
- **🏗️ Development Environment**: Docker Compose for local development
- **🔒 Security Implementation**: Non-root users, minimal images, vulnerability scanning
- **⚡ Performance Optimization**: JVM tuning, layer caching, image size reduction
- **🚀 Production Deployment**: Health checks, orchestration, scaling strategies
- **🛡️ Best Practices**: Security, monitoring, and operational excellence

**Key Takeaways**:
1. **Consistency**: Containers ensure identical environments across all stages
2. **Security**: Always use non-root users and scan for vulnerabilities
3. **Optimization**: Multi-stage builds and proper layering reduce image size
4. **Monitoring**: Health checks and observability are essential for production
5. **Automation**: Use orchestration tools for scaling and deployment management

Next lesson: **CI/CD Pipeline Setup** for automated testing, building, and deployment!