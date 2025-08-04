# 🚀 Lesson 16 Workshop: Logging & Observability

## 🎯 Workshop Objective

Build a comprehensive observability system with structured logging, request correlation, health monitoring, custom metrics, and performance tracking. You'll create a production-ready monitoring solution that provides deep insights into application behavior and performance.

**⏱️ Estimated Time**: 45 minutes

---

## 🏗️ What You'll Build

### **Complete Observability Platform**
- **Structured Logging**: JSON formatted logs with correlation IDs and MDC context
- **Health Monitoring**: Custom health indicators for all system dependencies
- **Business Metrics**: Application-specific metrics with Micrometer integration
- **Performance Tracking**: Request timing, resource monitoring, and alerting
- **Custom Actuator Endpoints**: Business intelligence and system diagnostics
- **Distributed Tracing**: Request correlation across service boundaries

### **Real-World Features**
```kotlin
// Correlated logging across requests
[2024-01-15 10:30:45.123] INFO [req-abc12345] UserService - User login successful: {"userId":123,"duration_ms":245,"ipAddress":"192.168.1.1"}

// Business metrics collection
business.user.login.attempts{result=success} 1.0
business.payment.processing.duration{operation=creditcard} 1.2s

// Custom health checks
GET /actuator/health
{
  "status": "UP",
  "components": {
    "database": {"status": "UP", "details": {"validationQuery": "SELECT 1"}},
    "redis": {"status": "UP", "details": {"response": "PONG"}},
    "paymentService": {"status": "UP", "details": {"responseTime": "125ms"}}
  }
}
```

**🎯 Success Metrics**: 
- Complete request correlation across all operations
- Real-time health monitoring with dependency checks
- Custom business metrics tracking application performance
- Automated alerting for performance and resource thresholds

---

## 📁 Project Structure

```
class/workshop/lesson_16/
├── build.gradle.kts          # ✅ Observability dependencies
├── src/main/
│   ├── kotlin/com/learning/observability/
│   │   ├── ObservabilityApplication.kt
│   │   ├── config/
│   │   │   ├── LoggingConfiguration.kt  # TODO: Configure structured logging
│   │   │   ├── MetricsConfiguration.kt  # TODO: Setup custom metrics
│   │   │   └── TracingConfiguration.kt  # TODO: Distributed tracing setup
│   │   ├── filter/
│   │   │   └── CorrelationIdFilter.kt   # TODO: Request correlation
│   │   ├── health/
│   │   │   ├── DatabaseHealthIndicator.kt    # TODO: Database health check
│   │   │   ├── RedisHealthIndicator.kt       # TODO: Redis health check
│   │   │   └── ExternalServiceHealthIndicator.kt # TODO: External service health
│   │   ├── monitoring/
│   │   │   ├── BusinessMetrics.kt       # TODO: Business-specific metrics
│   │   │   ├── PerformanceMonitoringService.kt # TODO: Performance tracking
│   │   │   └── MetricsCollectionService.kt # TODO: Scheduled metrics collection
│   │   ├── service/
│   │   │   ├── StructuredLogger.kt      # TODO: Smart logging service
│   │   │   ├── UserService.kt           # TODO: Service with observability
│   │   │   └── PaymentService.kt        # TODO: Service with tracing
│   │   ├── controller/
│   │   │   ├── UserController.kt        # TODO: Controller with logging
│   │   │   └── PaymentController.kt     # TODO: Controller with metrics
│   │   ├── actuator/
│   │   │   ├── BusinessMetricsEndpoint.kt # TODO: Custom business metrics
│   │   │   └── ApplicationInfoEndpoint.kt # TODO: Application information
│   │   └── aspect/
│   │       └── LoggingAspect.kt         # TODO: AOP-based logging
│   └── resources/
│       ├── application.yml              # TODO: Observability configuration
│       └── logback-spring.xml           # TODO: Logback configuration
└── src/test/
    └── kotlin/com/learning/observability/
        └── ObservabilityApplicationTests.kt # TODO: Observability tests
```

---

## 🛠️ Step 1: Configure Dependencies & Logging

### **📝 TODO: Update build.gradle.kts**
```kotlin
dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    
    // Observability
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
    implementation("io.zipkin.reporter2:zipkin-reporter-brave")
    
    // Structured logging
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")
    
    // Kotlin & Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    
    // Database
    runtimeOnly("com.h2database:h2")
    
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}
```

### **📝 TODO: Configure application.yml**
```yaml
spring:
  application:
    name: observability-workshop
  
  datasource:
    url: jdbc:h2:mem:observability_db
    driver-class-name: org.h2.Driver
    username: sa
    password: password
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false  # We'll use custom query logging
  
  data:
    redis:
      host: localhost
      port: 6379
      timeout: 2000ms

# TODO: Configure management endpoints
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,businessmetrics,applicationinfo
      base-path: /actuator
  endpoint:
    health:
      show-details: always
      show-components: always
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        http.server.requests: true
      percentiles:
        http.server.requests: 0.5, 0.9, 0.95, 0.99

# TODO: Configure logging levels
logging:
  level:
    com.learning.observability: DEBUG
    org.springframework.security: INFO
    PERFORMANCE: INFO
    BUSINESS: INFO
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level [%X{correlationId}] %logger{36} - %msg%n"

# TODO: Add custom application configuration
app:
  monitoring:
    enabled: true
    performance:
      slow-query-threshold: 1000
      slow-request-threshold: 2000
    alerts:
      memory-threshold: 80
      cpu-threshold: 75
  external:
    payment-service:
      url: http://localhost:8081
```

### **📝 TODO: Create logback-spring.xml**
```xml
<configuration>
    <springProfile name="!local">
        <!-- Production: JSON structured logs -->
        <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
            <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
                <providers>
                    <timestamp/>
                    <logLevel/>
                    <loggerName/>
                    <mdc/>
                    <arguments/>
                    <message/>
                    <stackTrace/>
                </providers>
            </encoder>
        </appender>
    </springProfile>
    
    <springProfile name="local">
        <!-- Development: Human-readable logs -->
        <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
            <encoder>
                <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level [%X{correlationId}] %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>
    </springProfile>
    
    <!-- TODO: Add file appender with rolling policy -->
    
    <!-- TODO: Add async appender for performance -->
    
    <root level="INFO">
        <appender-ref ref="STDOUT"/>
    </root>
    
    <!-- TODO: Configure specific logger levels -->
</configuration>
```

---

## 🛠️ Step 2: Implement Request Correlation

### **📝 TODO: Create CorrelationIdFilter.kt**
```kotlin
package com.learning.observability.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

@Component
@Order(1)
class CorrelationIdFilter : OncePerRequestFilter() {
    
    companion object {
        const val CORRELATION_ID_HEADER = "X-Correlation-ID"
        const val CORRELATION_ID_MDC_KEY = "correlationId"
        const val USER_ID_MDC_KEY = "userId"
        const val REQUEST_URI_MDC_KEY = "requestUri"
        const val HTTP_METHOD_MDC_KEY = "httpMethod"
    }
    
    private val logger = LoggerFactory.getLogger(CorrelationIdFilter::class.java)
    
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // TODO: Extract or generate correlation ID
        val correlationId = extractCorrelationId(request)
        
        try {
            // TODO: Set up MDC context
            setupMDCContext(request, correlationId)
            
            // TODO: Add correlation ID to response headers
            response.setHeader(CORRELATION_ID_HEADER, correlationId)
            
            logger.debug("Processing request with correlation ID: {}", correlationId)
            
            filterChain.doFilter(request, response)
            
        } finally {
            // TODO: Always clean up MDC
            MDC.clear()
        }
    }
    
    private fun extractCorrelationId(request: HttpServletRequest): String {
        // TODO: Try to get correlation ID from header
        // TODO: Generate new correlation ID if not present
        // HINT: Use UUID.randomUUID() with substring for shorter IDs
        
        return "req-${UUID.randomUUID().toString().substring(0, 8)}"
    }
    
    private fun setupMDCContext(request: HttpServletRequest, correlationId: String) {
        // TODO: Put correlation ID and request info in MDC
        // TODO: Extract user ID from security context if available
        // HINT: MDC.put(key, value)
        
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId)
        // TODO: Add more context
    }
}
```

---

## 🛠️ Step 3: Create Structured Logging Service

### **📝 TODO: Implement StructuredLogger.kt**
```kotlin
package com.learning.observability.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class StructuredLogger {
    
    private val logger = LoggerFactory.getLogger(StructuredLogger::class.java)
    private val objectMapper = ObjectMapper()
    
    fun logUserAction(
        action: String,
        userId: Long,
        resource: String? = null,
        result: String = "success",
        duration: Long? = null,
        additionalData: Map<String, Any> = emptyMap()
    ) {
        // TODO: Create structured log data
        val logData = mutableMapOf<String, Any>(
            "action" to action,
            "userId" to userId,
            "result" to result,
            "timestamp" to Instant.now().toString()
        )
        
        // TODO: Add optional fields
        resource?.let { logData["resource"] = it }
        duration?.let { logData["duration_ms"] = it }
        logData.putAll(additionalData)
        
        // TODO: Log with appropriate level based on result
        when (result) {
            "success" -> logger.info("User action completed: {}", objectMapper.writeValueAsString(logData))
            "failure" -> logger.warn("User action failed: {}", objectMapper.writeValueAsString(logData))
            "error" -> logger.error("User action error: {}", objectMapper.writeValueAsString(logData))
        }
    }
    
    fun logBusinessEvent(
        eventType: String,
        entityType: String,
        entityId: String,
        changes: Map<String, Any> = emptyMap(),
        metadata: Map<String, Any> = emptyMap()
    ) {
        // TODO: Create business event log entry
        // TODO: Include correlation ID from MDC
        // TODO: Log as structured JSON
        
        val eventData = mapOf(
            "eventType" to eventType,
            "entityType" to entityType,
            "entityId" to entityId,
            // TODO: Add more fields
        )
        
        logger.info("Business event: {}", objectMapper.writeValueAsString(eventData))
    }
    
    fun logSecurityEvent(
        eventType: String,
        userId: Long?,
        ipAddress: String?,
        severity: SecuritySeverity = SecuritySeverity.INFO,
        details: Map<String, Any> = emptyMap()
    ) {
        // TODO: Create security event log
        // TODO: Log with appropriate level based on severity
        
        val securityData = mutableMapOf<String, Any>(
            "securityEvent" to eventType,
            "severity" to severity,
            // TODO: Add more security context
        )
        
        val message = "Security event: ${objectMapper.writeValueAsString(securityData)}"
        when (severity) {
            SecuritySeverity.CRITICAL -> logger.error(message)
            SecuritySeverity.HIGH -> logger.error(message)
            SecuritySeverity.MEDIUM -> logger.warn(message)
            SecuritySeverity.LOW -> logger.info(message)
            SecuritySeverity.INFO -> logger.info(message)
        }
    }
}

enum class SecuritySeverity {
    CRITICAL, HIGH, MEDIUM, LOW, INFO
}
```

---

## 🛠️ Step 4: Implement Health Indicators

### **📝 TODO: Create DatabaseHealthIndicator.kt**
```kotlin
package com.learning.observability.health

import org.springframework.boot.actuator.health.Health
import org.springframework.boot.actuator.health.HealthIndicator
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
class DatabaseHealthIndicator(
    private val dataSource: DataSource
) : HealthIndicator {
    
    override fun health(): Health {
        return try {
            // TODO: Test database connection
            // TODO: Execute simple validation query
            // TODO: Check connection validity
            // HINT: Use dataSource.connection and connection.isValid()
            
            dataSource.connection.use { connection ->
                if (connection.isValid(5)) {
                    Health.up()
                        .withDetail("database", "H2")
                        .withDetail("validationQuery", "SELECT 1")
                        .build()
                } else {
                    // TODO: Return down status with error details
                    Health.down()
                        .withDetail("database", "H2")
                        .withDetail("error", "Connection validation failed")
                        .build()
                }
            }
        } catch (e: Exception) {
            // TODO: Handle exceptions and return down status
            Health.down()
                .withDetail("database", "H2")
                .withDetail("error", e.message)
                .withException(e)
                .build()
        }
    }
}
```

### **📝 TODO: Create ExternalServiceHealthIndicator.kt**
```kotlin
package com.learning.observability.health

import org.springframework.boot.actuator.health.Health
import org.springframework.boot.actuator.health.HealthIndicator
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.time.Duration

@Component
class ExternalServiceHealthIndicator : HealthIndicator {
    
    private val restTemplate = RestTemplate()
    
    override fun health(): Health {
        return try {
            // TODO: Check external service health
            // TODO: Measure response time
            // TODO: Handle different response scenarios
            
            val startTime = System.currentTimeMillis()
            
            // Mock external service check (since we don't have a real service)
            Thread.sleep(50) // Simulate network call
            val success = true // Mock success
            
            val responseTime = System.currentTimeMillis() - startTime
            
            if (success) {
                Health.up()
                    .withDetail("paymentService", "Available")
                    .withDetail("responseTime", "${responseTime}ms")
                    .build()
            } else {
                // TODO: Handle service unavailable case
                Health.down()
                    .withDetail("paymentService", "Unavailable")
                    .build()
            }
        } catch (e: Exception) {
            // TODO: Handle connection failures
            Health.down()
                .withDetail("paymentService", "Connection failed")
                .withDetail("error", e.message)
                .withException(e)
                .build()
        }
    }
}
```

---

## 🛠️ Step 5: Create Business Metrics

### **📝 TODO: Implement BusinessMetrics.kt**
```kotlin
package com.learning.observability.monitoring

import io.micrometer.core.instrument.Metrics
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicDouble
import java.util.concurrent.atomic.AtomicLong

@Component
class BusinessMetrics {
    
    // TODO: Define counters for business events
    private val userRegistrationCounter = Metrics.counter("business.user.registrations")
    private val loginAttemptsCounter = Metrics.counter("business.user.login.attempts")
    
    // TODO: Define gauges for current state
    private val activeUsersGauge = Metrics.gauge("business.users.active", AtomicLong(0))
    private val orderValueGauge = Metrics.gauge("business.orders.total_value", AtomicDouble(0.0))
    
    // TODO: Define timers for operation duration
    private val paymentProcessingTimer = Metrics.timer("business.payment.processing.duration")
    private val userRegistrationTimer = Metrics.timer("business.user.registration.duration")
    
    fun recordUserRegistration() {
        // TODO: Increment user registration counter
        userRegistrationCounter.increment()
    }
    
    fun recordLoginAttempt(successful: Boolean) {
        // TODO: Record login attempt with success/failure tag
        // HINT: Use Tags.of("result", "success"/"failure")
        
        if (successful) {
            loginAttemptsCounter.increment()
        } else {
            // TODO: Increment with failure tag
        }
    }
    
    fun updateActiveUsers(count: Long) {
        // TODO: Update active users gauge
        activeUsersGauge?.set(count)
    }
    
    fun <T> timePaymentProcessing(operation: () -> T): T {
        // TODO: Time the operation and return result
        // HINT: Use paymentProcessingTimer.recordCallable()
        
        return paymentProcessingTimer.recordCallable(operation)
    }
    
    fun <T> timeUserRegistration(operation: () -> T): T {
        // TODO: Time user registration operation
        return userRegistrationTimer.recordCallable(operation)
    }
    
    fun recordCustomMetric(name: String, value: Double, tags: Map<String, String> = emptyMap()) {
        // TODO: Record custom metric with tags
        // HINT: Use Metrics.counter() or Metrics.gauge()
        
        val counter = Metrics.counter(name, tags.map { "${it.key}=${it.value}" }.toTypedArray())
        counter.increment(value)
    }
}
```

---

## 🛠️ Step 6: Create Performance Monitoring

### **📝 TODO: Implement PerformanceMonitoringService.kt**
```kotlin
package com.learning.observability.monitoring

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.lang.management.ManagementFactory

@Service
class PerformanceMonitoringService {
    
    private val performanceLogger = LoggerFactory.getLogger("PERFORMANCE")
    
    companion object {
        const val SLOW_REQUEST_THRESHOLD_MS = 2000L
        const val HIGH_MEMORY_THRESHOLD_PERCENT = 80.0
        const val HIGH_CPU_THRESHOLD_PERCENT = 75.0
    }
    
    @EventListener
    fun handleSlowRequest(event: SlowRequestEvent) {
        // TODO: Log slow request events
        // TODO: Could integrate with alerting system
        
        performanceLogger.warn(
            "Slow request detected: uri={}, method={}, duration={}ms",
            event.uri, event.method, event.duration
        )
    }
    
    @Scheduled(fixedRate = 30000) // Every 30 seconds
    fun monitorSystemResources() {
        // TODO: Monitor memory and CPU usage
        // TODO: Log warnings when thresholds are exceeded
        // TODO: Could trigger alerts
        
        val memoryUsage = getMemoryUsagePercentage()
        val cpuUsage = getCpuUsagePercentage()
        
        performanceLogger.info(
            "System resources: memory={}%, cpu={}%",
            memoryUsage, cpuUsage
        )
        
        // TODO: Check thresholds and send alerts if needed
        if (memoryUsage > HIGH_MEMORY_THRESHOLD_PERCENT) {
            performanceLogger.error("High memory usage: {}%", memoryUsage)
        }
    }
    
    private fun getMemoryUsagePercentage(): Double {
        // TODO: Calculate memory usage percentage
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val usedMemory = totalMemory - freeMemory
        
        return (usedMemory.toDouble() / totalMemory) * 100
    }
    
    private fun getCpuUsagePercentage(): Double {
        // TODO: Get CPU usage if available
        // TODO: Return 0.0 if not available
        
        return try {
            val osBean = ManagementFactory.getOperatingSystemMXBean()
            when (osBean) {
                is com.sun.management.OperatingSystemMXBean -> osBean.processCpuLoad * 100
                else -> 0.0
            }
        } catch (e: Exception) {
            0.0
        }
    }
}

// Performance event classes
data class SlowRequestEvent(
    val uri: String,
    val method: String,
    val duration: Long,
    val statusCode: Int
)
```

---

## 🛠️ Step 7: Create Custom Actuator Endpoints

### **📝 TODO: Implement BusinessMetricsEndpoint.kt**
```kotlin
package com.learning.observability.actuator

import org.springframework.boot.actuator.endpoint.annotation.Endpoint
import org.springframework.boot.actuator.endpoint.annotation.ReadOperation
import org.springframework.stereotype.Component
import java.time.Instant

@Component
@Endpoint(id = "businessmetrics")
class BusinessMetricsEndpoint {
    
    @ReadOperation
    fun businessMetrics(): Map<String, Any> {
        return try {
            mapOf(
                "users" to getUserMetrics(),
                "system" to getSystemMetrics(),
                "performance" to getPerformanceMetrics(),
                "timestamp" to Instant.now().toString()
            )
        } catch (e: Exception) {
            mapOf(
                "error" to "Failed to collect metrics",
                "message" to e.message,
                "timestamp" to Instant.now().toString()
            )
        }
    }
    
    private fun getUserMetrics(): Map<String, Any> {
        // TODO: Collect user-related metrics
        // TODO: This would normally query your user repository
        
        return mapOf(
            "totalUsers" to 150,
            "activeUsers" to 45,
            "recentRegistrations" to 12
        )
    }
    
    private fun getSystemMetrics(): Map<String, Any> {
        // TODO: Collect system resource metrics
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val usedMemory = totalMemory - freeMemory
        
        return mapOf(
            "memoryUsed" to usedMemory,
            "memoryTotal" to totalMemory,
            "memoryUsagePercentage" to (usedMemory.toDouble() / totalMemory * 100),
            "availableProcessors" to runtime.availableProcessors()
        )
    }
    
    private fun getPerformanceMetrics(): Map<String, Any> {
        // TODO: Collect performance-related metrics
        
        return mapOf(
            "averageResponseTime" to "125ms",
            "requestsPerMinute" to 234,
            "errorRate" to 0.02
        )
    }
}
```

---

## 🛠️ Step 8: Add Observability to Services

### **📝 TODO: Create UserService.kt with observability**
```kotlin
package com.learning.observability.service

import com.learning.observability.monitoring.BusinessMetrics
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UserService(
    private val structuredLogger: StructuredLogger,
    private val businessMetrics: BusinessMetrics
) {
    
    private val logger = LoggerFactory.getLogger(UserService::class.java)
    
    fun registerUser(email: String, password: String): UserRegistrationResult {
        // TODO: Add comprehensive logging and metrics
        val startTime = System.currentTimeMillis()
        
        return try {
            logger.info("Starting user registration for email: {}", email)
            
            // TODO: Simulate user registration logic
            Thread.sleep(100) // Simulate processing time
            
            val userId = System.currentTimeMillis() // Mock user ID
            val duration = System.currentTimeMillis() - startTime
            
            // TODO: Log successful registration
            structuredLogger.logUserAction(
                action = "user_registration",
                userId = userId,
                result = "success",
                duration = duration,
                additionalData = mapOf("email" to email)
            )
            
            // TODO: Record business metrics
            businessMetrics.recordUserRegistration()
            
            // TODO: Log business event
            structuredLogger.logBusinessEvent(
                eventType = "USER_REGISTERED",
                entityType = "User",
                entityId = userId.toString(),
                metadata = mapOf("registrationDuration" to duration)
            )
            
            UserRegistrationResult(success = true, userId = userId)
            
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            
            // TODO: Log failed registration
            structuredLogger.logUserAction(
                action = "user_registration",
                userId = 0L,
                result = "error",
                duration = duration,
                additionalData = mapOf("error" to e.message)
            )
            
            throw e
        }
    }
    
    fun authenticateUser(email: String, password: String): AuthenticationResult {
        // TODO: Add authentication with full observability
        val startTime = System.currentTimeMillis()
        
        return try {
            // TODO: Simulate authentication logic
            val success = email.contains("@") && password.length >= 6
            val userId = if (success) 12345L else null
            
            val duration = System.currentTimeMillis() - startTime
            
            // TODO: Record login attempt metric
            businessMetrics.recordLoginAttempt(success)
            
            // TODO: Log authentication result
            structuredLogger.logUserAction(
                action = "user_authentication",
                userId = userId ?: 0L,
                result = if (success) "success" else "failure",
                duration = duration
            )
            
            if (!success) {
                // TODO: Log security event for failed login
                structuredLogger.logSecurityEvent(
                    eventType = "AUTHENTICATION_FAILED",
                    userId = null,
                    ipAddress = "192.168.1.1", // Mock IP
                    severity = SecuritySeverity.MEDIUM
                )
            }
            
            AuthenticationResult(success = success, userId = userId)
            
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            structuredLogger.logUserAction(
                action = "user_authentication",
                userId = 0L,
                result = "error",
                duration = duration,
                additionalData = mapOf("error" to e.message)
            )
            throw e
        }
    }
}

data class UserRegistrationResult(
    val success: Boolean,
    val userId: Long? = null,
    val error: String? = null
)

data class AuthenticationResult(
    val success: Boolean,
    val userId: Long? = null
)
```

---

## 🛠️ Step 9: Create Controllers with Observability

### **📝 TODO: Implement UserController.kt**
```kotlin
package com.learning.observability.controller

import com.learning.observability.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {
    
    private val logger = LoggerFactory.getLogger(UserController::class.java)
    
    @PostMapping("/register")
    fun registerUser(
        @RequestBody request: UserRegistrationRequest
    ): ResponseEntity<*> {
        
        return try {
            logger.info("Received user registration request for email: {}", request.email)
            
            // TODO: Call service with observability
            val result = userService.registerUser(request.email, request.password)
            
            if (result.success) {
                ResponseEntity.ok(mapOf(
                    "success" to true,
                    "userId" to result.userId,
                    "message" to "User registered successfully"
                ))
            } else {
                ResponseEntity.badRequest().body(mapOf(
                    "success" to false,
                    "error" to result.error
                ))
            }
            
        } catch (e: Exception) {
            logger.error("User registration failed", e)
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "error" to "Registration failed: ${e.message}"
            ))
        }
    }
    
    @PostMapping("/login")
    fun loginUser(
        @RequestBody request: LoginRequest
    ): ResponseEntity<*> {
        
        return try {
            logger.info("Received login request for email: {}", request.email)
            
            // TODO: Call service with observability
            val result = userService.authenticateUser(request.email, request.password)
            
            if (result.success) {
                ResponseEntity.ok(mapOf(
                    "success" to true,
                    "userId" to result.userId,
                    "message" to "Login successful"
                ))
            } else {
                ResponseEntity.badRequest().body(mapOf(
                    "success" to false,
                    "error" to "Invalid credentials"
                ))
            }
            
        } catch (e: Exception) {
            logger.error("User login failed", e)
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "error" to "Login failed: ${e.message}"
            ))
        }
    }
}

data class UserRegistrationRequest(
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)
```

---

## 🛠️ Step 10: Test Your Observability System

### **📝 TODO: Create comprehensive tests**
```kotlin
package com.learning.observability

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@TestPropertySource(properties = [
    "spring.datasource.url=jdbc:h2:mem:test_observability",
    "logging.level.com.learning.observability=DEBUG"
])
class ObservabilityApplicationTests {
    
    @Test
    fun contextLoads() {
        // Basic Spring Boot test
    }
    
    // TODO: Test correlation ID handling
    @Test
    fun `should generate correlation ID for requests`() {
        // TODO: Test CorrelationIdFilter
        // TODO: Verify MDC context is set up correctly
        // TODO: Check correlation ID is added to response headers
    }
    
    // TODO: Test structured logging
    @Test
    fun `should log structured data correctly`() {
        // TODO: Test StructuredLogger service
        // TODO: Verify JSON format
        // TODO: Check all required fields are present
    }
    
    // TODO: Test health indicators
    @Test
    fun `should report correct health status`() {
        // TODO: Test custom health indicators
        // TODO: Verify database health check
        // TODO: Check external service health indicator
    }
    
    // TODO: Test business metrics
    @Test
    fun `should record business metrics correctly`() {
        // TODO: Test BusinessMetrics component
        // TODO: Verify counters are incremented
        // TODO: Check timers record durations
    }
}
```

---

## 🚀 Step 11: Run and Test Your Observability System

### **1. Start the Application**
```bash
cd class/workshop/lesson_16
./gradlew bootRun
```

### **2. Test Health Endpoints**
```bash
# Check overall application health
curl http://localhost:8080/actuator/health

# Get detailed health information
curl http://localhost:8080/actuator/health | jq

# Check custom business metrics
curl http://localhost:8080/actuator/businessmetrics

# View Prometheus metrics
curl http://localhost:8080/actuator/prometheus
```

### **3. Test Request Correlation**
```bash
# Send request with correlation ID
curl -H "X-Correlation-ID: test-12345" \
     -H "Content-Type: application/json" \
     -d '{"email":"test@example.com","password":"password123"}' \
     http://localhost:8080/api/users/register

# Check logs for correlation ID: test-12345
```

### **4. Generate Metrics and Logs**
```bash
# Register multiple users to generate metrics
for i in {1..5}; do
  curl -H "Content-Type: application/json" \
       -d "{\"email\":\"user$i@example.com\",\"password\":\"password123\"}" \
       http://localhost:8080/api/users/register
  sleep 1
done

# Test login attempts
curl -H "Content-Type: application/json" \
     -d '{"email":"user1@example.com","password":"password123"}' \
     http://localhost:8080/api/users/login

# Test failed login
curl -H "Content-Type: application/json" \
     -d '{"email":"user1@example.com","password":"wrong"}' \
     http://localhost:8080/api/users/login
```

### **5. Check Application Information**
```bash
# Get application info
curl http://localhost:8080/actuator/applicationinfo

# Check metrics
curl http://localhost:8080/actuator/metrics/business.user.registrations
curl http://localhost:8080/actuator/metrics/business.user.login.attempts
```

---

## 🎯 Expected Results

### **Structured Log Output**
```json
{
  "timestamp": "2024-01-15T10:30:45.123Z",
  "level": "INFO",
  "logger": "c.l.o.service.UserService",
  "mdc": {
    "correlationId": "req-abc12345",
    "requestUri": "/api/users/register",
    "httpMethod": "POST"
  },
  "message": "User action completed: {\"action\":\"user_registration\",\"userId\":1642249845123,\"result\":\"success\",\"duration_ms\":156,\"email\":\"test@example.com\"}"
}
```

### **Health Check Response**
```json
{
  "status": "UP",
  "components": {
    "database": {
      "status": "UP",
      "details": {
        "database": "H2",
        "validationQuery": "SELECT 1"
      }
    },
    "externalService": {
      "status": "UP",
      "details": {
        "paymentService": "Available",
        "responseTime": "67ms"
      }
    }
  }
}
```

### **Business Metrics Output**
```json
{
  "users": {
    "totalUsers": 150,
    "activeUsers": 45,
    "recentRegistrations": 12
  },
  "system": {
    "memoryUsagePercentage": 45.2,
    "availableProcessors": 8
  },
  "performance": {
    "averageResponseTime": "125ms",
    "requestsPerMinute": 234,
    "errorRate": 0.02
  }
}
```

---

## 🏆 Challenge Extensions

### **🔥 Bonus Challenge 1: ELK Stack Integration**
Set up Elasticsearch, Logstash, and Kibana for log aggregation and visualization.

### **🔥 Bonus Challenge 2: Alerting System**
Implement Slack or email notifications for critical alerts and performance thresholds.

### **🔥 Bonus Challenge 3: Custom Dashboards**
Create Grafana dashboards for business metrics and system monitoring.

### **🔥 Bonus Challenge 4: Distributed Tracing**
Implement Zipkin or Jaeger for end-to-end request tracing.

### **🔥 Bonus Challenge 5: Log Analysis**
Add log parsing and analysis features for security and performance insights.

---

## 🎓 Learning Outcomes

Upon completion, you'll have:

✅ **Implemented structured logging** with JSON format and correlation IDs for production debugging  
✅ **Created comprehensive health checks** for all system dependencies and external services  
✅ **Built custom business metrics** tracking application-specific performance indicators  
✅ **Added performance monitoring** with automated alerting for resource thresholds  
✅ **Set up request correlation** enabling end-to-end request tracking across services  
✅ **Designed observability patterns** supporting production monitoring and troubleshooting

**🚀 Next Lesson**: Dockerizing Your Application - containerizing applications for consistent deployment!