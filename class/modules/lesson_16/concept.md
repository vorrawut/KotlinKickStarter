# 🚀 Lesson 16: Logging & Observability - Concept Guide

## 🎯 Learning Objectives

By the end of this lesson, you will:
- **Implement structured logging** with Logback and JSON formatting for production systems
- **Use MDC (Mapped Diagnostic Context)** for request correlation and distributed tracing
- **Configure health checks and metrics** with Spring Boot Actuator for system monitoring
- **Create custom metrics and alerts** for business-specific monitoring requirements
- **Set up distributed tracing** for microservices debugging and performance analysis
- **Design log aggregation strategies** for centralized monitoring and analysis

---

## 🔍 Why Observability Matters

### **The Three Pillars of Observability**
```kotlin
// 1. LOGS: What happened and when
logger.info("User {} attempted login with result: {}", userId, loginResult)

// 2. METRICS: How much and how fast
Metrics.counter("login.attempts", "result", "success").increment()
Metrics.timer("login.duration").recordCallable { authenticateUser(credentials) }

// 3. TRACES: How systems interact
@NewSpan("user-authentication")
fun authenticateUser(@SpanTag("userId") userId: String): AuthResult
```

### **Production Benefits**
- **Faster Debugging**: Correlation IDs track requests across services
- **Proactive Monitoring**: Metrics and alerts catch issues before users notice
- **Performance Optimization**: Detailed timing data identifies bottlenecks
- **Compliance & Auditing**: Comprehensive audit trails for security and regulatory requirements

---

## 📊 Structured Logging with Logback

### **JSON Structured Logging Configuration**
```xml
<!-- logback-spring.xml -->
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
    
    <!-- File appender for persistent logs -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/application.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
            <maxFileSize>100MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>3GB</totalSizeCap>
        </rollingPolicy>
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
    
    <!-- Async logging for performance -->
    <appender name="ASYNC" class="ch.qos.logback.classic.AsyncAppender">
        <appender-ref ref="STDOUT"/>
        <queueSize>512</queueSize>
        <discardingThreshold>20</discardingThreshold>
        <includeCallerData>false</includeCallerData>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="ASYNC"/>
        <appender-ref ref="FILE"/>
    </root>
    
    <!-- Custom logger levels -->
    <logger name="com.learning.observability" level="DEBUG"/>
    <logger name="org.springframework.security" level="DEBUG"/>
    <logger name="org.hibernate.SQL" level="DEBUG"/>
    <logger name="org.hibernate.type.descriptor.sql.BasicBinder" level="TRACE"/>
</configuration>
```

### **Smart Logging Service Implementation**
```kotlin
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
        val logData = mutableMapOf<String, Any>(
            "action" to action,
            "userId" to userId,
            "result" to result,
            "timestamp" to Instant.now().toString()
        )
        
        resource?.let { logData["resource"] = it }
        duration?.let { logData["duration_ms"] = it }
        logData.putAll(additionalData)
        
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
        val eventData = mapOf(
            "eventType" to eventType,
            "entityType" to entityType,
            "entityId" to entityId,
            "changes" to changes,
            "metadata" to metadata,
            "timestamp" to Instant.now().toString(),
            "correlationId" to MDC.get("correlationId")
        )
        
        logger.info("Business event: {}", objectMapper.writeValueAsString(eventData))
    }
    
    fun logPerformanceMetric(
        operation: String,
        duration: Long,
        success: Boolean,
        additionalMetrics: Map<String, Any> = emptyMap()
    ) {
        val performanceData = mutableMapOf<String, Any>(
            "operation" to operation,
            "duration_ms" to duration,
            "success" to success,
            "timestamp" to Instant.now().toString()
        )
        performanceData.putAll(additionalMetrics)
        
        val level = when {
            !success -> "ERROR"
            duration > 5000 -> "WARN"  // Slow operation
            duration > 1000 -> "INFO"
            else -> "DEBUG"
        }
        
        val message = "Performance metric: ${objectMapper.writeValueAsString(performanceData)}"
        when (level) {
            "ERROR" -> logger.error(message)
            "WARN" -> logger.warn(message)
            "INFO" -> logger.info(message)
            "DEBUG" -> logger.debug(message)
        }
    }
    
    fun logSecurityEvent(
        eventType: String,
        userId: Long?,
        ipAddress: String?,
        userAgent: String?,
        severity: SecuritySeverity = SecuritySeverity.INFO,
        details: Map<String, Any> = emptyMap()
    ) {
        val securityData = mutableMapOf<String, Any>(
            "securityEvent" to eventType,
            "severity" to severity,
            "timestamp" to Instant.now().toString(),
            "correlationId" to MDC.get("correlationId")
        )
        
        userId?.let { securityData["userId"] = it }
        ipAddress?.let { securityData["ipAddress"] = it }
        userAgent?.let { securityData["userAgent"] = it }
        securityData.putAll(details)
        
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

## 🔗 Request Correlation with MDC

### **Correlation ID Filter**
```kotlin
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
        val correlationId = extractCorrelationId(request)
        
        try {
            // Set up MDC context
            setupMDCContext(request, correlationId)
            
            // Add correlation ID to response headers
            response.setHeader(CORRELATION_ID_HEADER, correlationId)
            
            logger.debug("Processing request with correlation ID: {}", correlationId)
            
            filterChain.doFilter(request, response)
            
        } finally {
            // Always clean up MDC
            MDC.clear()
        }
    }
    
    private fun extractCorrelationId(request: HttpServletRequest): String {
        // Try to get correlation ID from header
        val headerCorrelationId = request.getHeader(CORRELATION_ID_HEADER)
        
        return if (!headerCorrelationId.isNullOrBlank()) {
            headerCorrelationId
        } else {
            // Generate new correlation ID
            "req-${UUID.randomUUID().toString().substring(0, 8)}"
        }
    }
    
    private fun setupMDCContext(request: HttpServletRequest, correlationId: String) {
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId)
        MDC.put(REQUEST_URI_MDC_KEY, request.requestURI)
        MDC.put(HTTP_METHOD_MDC_KEY, request.method)
        
        // Add user ID if available from security context
        try {
            val authentication = SecurityContextHolder.getContext().authentication
            if (authentication != null && authentication.isAuthenticated && authentication.principal != "anonymousUser") {
                val userDetails = authentication.principal as? UserDetails
                userDetails?.let { user ->
                    // Assuming UserDetails has a method to get user ID
                    MDC.put(USER_ID_MDC_KEY, extractUserId(user).toString())
                }
            }
        } catch (e: Exception) {
            logger.debug("Could not extract user ID for MDC", e)
        }
    }
    
    private fun extractUserId(userDetails: UserDetails): Long {
        // Extract user ID from your UserDetails implementation
        return when (userDetails) {
            is CustomUserDetails -> userDetails.userId
            else -> 0L
        }
    }
}

// Enhanced logging aspect that uses MDC
@Aspect
@Component
class LoggingAspect {
    
    private val logger = LoggerFactory.getLogger(LoggingAspect::class.java)
    
    @Around("@annotation(Loggable)")
    fun logExecutionTime(joinPoint: ProceedingJoinPoint): Any? {
        val startTime = System.currentTimeMillis()
        val methodName = "${joinPoint.signature.declaringTypeName}.${joinPoint.signature.name}"
        
        logger.info("Executing method: {} with args: {}", methodName, joinPoint.args.contentToString())
        
        return try {
            val result = joinPoint.proceed()
            val duration = System.currentTimeMillis() - startTime
            
            logger.info("Method {} completed successfully in {}ms", methodName, duration)
            
            // Log performance metric
            logPerformanceMetric(methodName, duration, true)
            
            result
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            
            logger.error("Method {} failed after {}ms with error: {}", methodName, duration, e.message, e)
            
            // Log performance metric for failure
            logPerformanceMetric(methodName, duration, false, mapOf("error" to e.message))
            
            throw e
        }
    }
    
    private fun logPerformanceMetric(method: String, duration: Long, success: Boolean, additionalData: Map<String, Any?> = emptyMap()) {
        val performanceData = mutableMapOf<String, Any?>(
            "method" to method,
            "duration_ms" to duration,
            "success" to success,
            "correlationId" to MDC.get("correlationId"),
            "userId" to MDC.get("userId")
        )
        performanceData.putAll(additionalData)
        
        logger.info("Performance: {}", performanceData)
    }
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Loggable
```

---

## 📊 Health Checks & Metrics with Actuator

### **Custom Health Indicators**
```kotlin
@Component
class DatabaseHealthIndicator(
    private val dataSource: DataSource
) : HealthIndicator {
    
    override fun health(): Health {
        return try {
            dataSource.connection.use { connection ->
                if (connection.isValid(5)) {
                    Health.up()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("validationQuery", "SELECT 1")
                        .build()
                } else {
                    Health.down()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("error", "Connection validation failed")
                        .build()
                }
            }
        } catch (e: Exception) {
            Health.down()
                .withDetail("database", "PostgreSQL")
                .withDetail("error", e.message)
                .withException(e)
                .build()
        }
    }
}

@Component
class RedisHealthIndicator(
    private val redisTemplate: RedisTemplate<String, String>
) : HealthIndicator {
    
    override fun health(): Health {
        return try {
            val ping = redisTemplate.connectionFactory?.connection?.ping()
            
            if (ping == "PONG") {
                Health.up()
                    .withDetail("redis", "Available")
                    .withDetail("response", ping)
                    .build()
            } else {
                Health.down()
                    .withDetail("redis", "Unavailable")
                    .withDetail("response", ping ?: "No response")
                    .build()
            }
        } catch (e: Exception) {
            Health.down()
                .withDetail("redis", "Connection failed")
                .withDetail("error", e.message)
                .withException(e)
                .build()
        }
    }
}

@Component
class ExternalServiceHealthIndicator(
    private val webClient: WebClient
) : HealthIndicator {
    
    @Value("\${app.external.payment-service.url}")
    private lateinit var paymentServiceUrl: String
    
    override fun health(): Health {
        return try {
            val startTime = System.currentTimeMillis()
            
            val response = webClient.get()
                .uri("$paymentServiceUrl/health")
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(5))
                .block()
            
            val responseTime = System.currentTimeMillis() - startTime
            
            if (response?.statusCode?.is2xxSuccessful == true) {
                Health.up()
                    .withDetail("paymentService", "Available")
                    .withDetail("responseTime", "${responseTime}ms")
                    .withDetail("statusCode", response.statusCode.value())
                    .build()
            } else {
                Health.down()
                    .withDetail("paymentService", "Unavailable")
                    .withDetail("statusCode", response?.statusCode?.value() ?: "No response")
                    .build()
            }
        } catch (e: Exception) {
            Health.down()
                .withDetail("paymentService", "Connection failed")
                .withDetail("error", e.message)
                .withException(e)
                .build()
        }
    }
}
```

### **Custom Metrics and Monitoring**
```kotlin
@Component
class BusinessMetrics {
    
    private val userRegistrationCounter = Metrics.counter("business.user.registrations")
    private val loginAttemptsCounter = Metrics.counter("business.user.login.attempts")
    private val loginSuccessCounter = Metrics.counter("business.user.login.success")
    private val orderCreatedCounter = Metrics.counter("business.orders.created")
    private val orderValueGauge = Metrics.gauge("business.orders.total_value", AtomicDouble(0.0))
    private val activeUsersGauge = Metrics.gauge("business.users.active", AtomicLong(0))
    
    private val paymentProcessingTimer = Metrics.timer("business.payment.processing.duration")
    private val databaseQueryTimer = Metrics.timer("business.database.query.duration")
    
    fun recordUserRegistration() {
        userRegistrationCounter.increment()
    }
    
    fun recordLoginAttempt(successful: Boolean) {
        loginAttemptsCounter.increment(Tags.of("result", if (successful) "success" else "failure"))
        if (successful) {
            loginSuccessCounter.increment()
        }
    }
    
    fun recordOrderCreated(orderValue: Double) {
        orderCreatedCounter.increment()
        orderValueGauge?.set(orderValue)
    }
    
    fun updateActiveUsers(count: Long) {
        activeUsersGauge?.set(count)
    }
    
    fun <T> timePaymentProcessing(operation: () -> T): T {
        return paymentProcessingTimer.recordCallable(operation)
    }
    
    fun <T> timeDatabaseQuery(queryType: String, operation: () -> T): T {
        return Timer.Sample.start(Metrics.globalRegistry).let { sample ->
            try {
                val result = operation()
                sample.stop(Timer.builder("business.database.query.duration")
                    .tag("query_type", queryType)
                    .tag("result", "success")
                    .register(Metrics.globalRegistry))
                result
            } catch (e: Exception) {
                sample.stop(Timer.builder("business.database.query.duration")
                    .tag("query_type", queryType)
                    .tag("result", "error")
                    .register(Metrics.globalRegistry))
                throw e
            }
        }
    }
}

// Metrics collection service
@Service
class MetricsCollectionService(
    private val userRepository: UserRepository,
    private val orderRepository: OrderRepository,
    private val businessMetrics: BusinessMetrics
) {
    
    @Scheduled(fixedRate = 60000) // Every minute
    fun collectActiveUserMetrics() {
        try {
            val activeUsers = businessMetrics.timeDatabaseQuery("count_active_users") {
                userRepository.countActiveUsers()
            }
            businessMetrics.updateActiveUsers(activeUsers)
        } catch (e: Exception) {
            logger.error("Failed to collect active user metrics", e)
        }
    }
    
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    fun collectOrderMetrics() {
        try {
            val recentOrders = businessMetrics.timeDatabaseQuery("recent_orders") {
                orderRepository.findOrdersCreatedInLastHour()
            }
            
            val totalValue = recentOrders.sumOf { it.totalAmount }
            businessMetrics.recordOrderCreated(totalValue)
            
        } catch (e: Exception) {
            logger.error("Failed to collect order metrics", e)
        }
    }
    
    companion object {
        private val logger = LoggerFactory.getLogger(MetricsCollectionService::class.java)
    }
}
```

### **Custom Actuator Endpoints**
```kotlin
@Component
@Endpoint(id = "businessmetrics")
class BusinessMetricsEndpoint(
    private val userRepository: UserRepository,
    private val orderRepository: OrderRepository
) {
    
    @ReadOperation
    fun businessMetrics(): Map<String, Any> {
        return try {
            mapOf(
                "users" to getUserMetrics(),
                "orders" to getOrderMetrics(),
                "system" to getSystemMetrics(),
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
    
    @ReadOperation
    fun userMetrics(): Map<String, Any> {
        return getUserMetrics()
    }
    
    @ReadOperation 
    fun orderMetrics(): Map<String, Any> {
        return getOrderMetrics()
    }
    
    private fun getUserMetrics(): Map<String, Any> {
        val totalUsers = userRepository.count()
        val activeUsers = userRepository.countActiveUsers()
        val recentRegistrations = userRepository.countUsersRegisteredInLastWeek()
        
        return mapOf(
            "totalUsers" to totalUsers,
            "activeUsers" to activeUsers,
            "activePercentage" to if (totalUsers > 0) (activeUsers.toDouble() / totalUsers * 100) else 0.0,
            "recentRegistrations" to recentRegistrations
        )
    }
    
    private fun getOrderMetrics(): Map<String, Any> {
        val todayStart = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val todayOrders = orderRepository.countOrdersCreatedAfter(todayStart)
        val todayRevenue = orderRepository.sumRevenueForDate(todayStart)
        
        return mapOf(
            "todayOrders" to todayOrders,
            "todayRevenue" to todayRevenue,
            "averageOrderValue" to if (todayOrders > 0) todayRevenue / todayOrders else 0.0
        )
    }
    
    private fun getSystemMetrics(): Map<String, Any> {
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
}

@Component
@Endpoint(id = "applicationinfo")
class ApplicationInfoEndpoint {
    
    @Value("\${spring.application.name}")
    private lateinit var applicationName: String
    
    @Value("\${app.version:unknown}")
    private lateinit var applicationVersion: String
    
    @ReadOperation
    fun applicationInfo(): Map<String, Any> {
        return mapOf(
            "application" to mapOf(
                "name" to applicationName,
                "version" to applicationVersion,
                "buildTime" to getBuildTime(),
                "gitCommit" to getGitCommit()
            ),
            "environment" to mapOf(
                "activeProfiles" to getActiveProfiles(),
                "javaVersion" to System.getProperty("java.version"),
                "springBootVersion" to getSpringBootVersion()
            ),
            "uptime" to getUptime(),
            "timestamp" to Instant.now().toString()
        )
    }
    
    private fun getBuildTime(): String {
        return try {
            val properties = Properties()
            properties.load(javaClass.getResourceAsStream("/build-info.properties"))
            properties.getProperty("build.time", "unknown")
        } catch (e: Exception) {
            "unknown"
        }
    }
    
    private fun getGitCommit(): String {
        return try {
            val properties = Properties()
            properties.load(javaClass.getResourceAsStream("/git.properties"))
            properties.getProperty("git.commit.id.abbrev", "unknown")
        } catch (e: Exception) {
            "unknown"
        }
    }
    
    private fun getActiveProfiles(): List<String> {
        return try {
            val context = ApplicationContextProvider.getApplicationContext()
            context.environment.activeProfiles.toList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun getSpringBootVersion(): String {
        return SpringBootVersion.getVersion() ?: "unknown"
    }
    
    private fun getUptime(): String {
        val runtimeMXBean = ManagementFactory.getRuntimeMXBean()
        val uptimeMillis = runtimeMXBean.uptime
        val duration = Duration.ofMillis(uptimeMillis)
        
        return "${duration.toDays()}d ${duration.toHoursPart()}h ${duration.toMinutesPart()}m ${duration.toSecondsPart()}s"
    }
}
```

---

## 🔍 Distributed Tracing Setup

### **Micrometer Tracing Configuration**
```kotlin
@Configuration
@EnableAutoConfiguration
class TracingConfiguration {
    
    @Bean
    fun spanCustomizer(): SpanCustomizer {
        return SpanCustomizer { span ->
            // Add default tags to all spans
            span.tag("service.name", "observability-service")
            span.tag("service.version", getServiceVersion())
            
            // Add MDC context to spans
            val correlationId = MDC.get("correlationId")
            if (!correlationId.isNullOrBlank()) {
                span.tag("correlation.id", correlationId)
            }
            
            val userId = MDC.get("userId")
            if (!userId.isNullOrBlank()) {
                span.tag("user.id", userId)
            }
        }
    }
    
    @Bean
    fun customTraceFilter(): TraceFilter {
        return object : TraceFilter {
            override fun shouldTrace(span: SpanBuilder): Boolean {
                // Skip tracing for health check endpoints
                val operationName = span.toString()
                return !operationName.contains("/actuator/health")
            }
        }
    }
    
    private fun getServiceVersion(): String {
        return javaClass.`package`.implementationVersion ?: "unknown"
    }
}

// Enhanced service with tracing
@Service
class TracedUserService(
    private val userRepository: UserRepository,
    private val structuredLogger: StructuredLogger
) {
    
    @NewSpan("user-service.create-user")
    fun createUser(@SpanTag("userEmail") email: String, password: String): User {
        return Span.current().let { span ->
            try {
                span.setTag("operation", "create_user")
                span.setTag("email", email)
                
                structuredLogger.logUserAction(
                    action = "user_creation_started",
                    userId = 0L,
                    additionalData = mapOf("email" to email)
                )
                
                val user = User(email = email, password = hashPassword(password))
                val savedUser = userRepository.save(user)
                
                span.setTag("userId", savedUser.id.toString())
                span.setTag("success", "true")
                
                structuredLogger.logUserAction(
                    action = "user_creation_completed",
                    userId = savedUser.id!!,
                    result = "success"
                )
                
                savedUser
                
            } catch (e: Exception) {
                span.setTag("success", "false")
                span.setTag("error", e.message ?: "unknown")
                
                structuredLogger.logUserAction(
                    action = "user_creation_failed",
                    userId = 0L,
                    result = "error",
                    additionalData = mapOf("error" to e.message)
                )
                
                throw e
            }
        }
    }
    
    @NewSpan("user-service.authenticate")
    fun authenticateUser(
        @SpanTag("email") email: String, 
        password: String
    ): AuthenticationResult {
        return Span.current().let { span ->
            val startTime = System.currentTimeMillis()
            
            try {
                span.setTag("operation", "authenticate_user")
                
                val user = userRepository.findByEmail(email)
                    ?: throw AuthenticationException("User not found")
                
                span.setTag("userId", user.id.toString())
                
                if (!verifyPassword(password, user.password)) {
                    span.setTag("success", "false")
                    span.setTag("reason", "invalid_password")
                    
                    structuredLogger.logSecurityEvent(
                        eventType = "authentication_failed",
                        userId = user.id,
                        ipAddress = getCurrentIpAddress(),
                        userAgent = getCurrentUserAgent(),
                        severity = SecuritySeverity.MEDIUM,
                        details = mapOf("reason" to "invalid_password")
                    )
                    
                    throw AuthenticationException("Invalid credentials")
                }
                
                span.setTag("success", "true")
                val duration = System.currentTimeMillis() - startTime
                
                structuredLogger.logUserAction(
                    action = "authentication_success",
                    userId = user.id!!,
                    result = "success",
                    duration = duration
                )
                
                AuthenticationResult(success = true, user = user)
                
            } catch (e: Exception) {
                span.setTag("success", "false")
                span.setTag("error", e.message ?: "unknown")
                
                val duration = System.currentTimeMillis() - startTime
                
                structuredLogger.logUserAction(
                    action = "authentication_failed",
                    userId = 0L,
                    result = "error",
                    duration = duration,
                    additionalData = mapOf("error" to e.message)
                )
                
                throw e
            }
        }
    }
    
    private fun getCurrentIpAddress(): String {
        return try {
            val request = (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes).request
            request.getHeader("X-Forwarded-For") ?: request.remoteAddr
        } catch (e: Exception) {
            "unknown"
        }
    }
    
    private fun getCurrentUserAgent(): String {
        return try {
            val request = (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes).request
            request.getHeader("User-Agent") ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }
}
```

---

## 📈 Performance Monitoring & Alerting

### **Performance Monitoring Service**
```kotlin
@Service
class PerformanceMonitoringService {
    
    private val performanceLogger = LoggerFactory.getLogger("PERFORMANCE")
    
    // Performance thresholds
    companion object {
        const val SLOW_QUERY_THRESHOLD_MS = 1000L
        const val SLOW_REQUEST_THRESHOLD_MS = 2000L
        const val HIGH_MEMORY_THRESHOLD_PERCENT = 80.0
        const val HIGH_CPU_THRESHOLD_PERCENT = 75.0
    }
    
    @EventListener
    fun handleSlowQuery(event: SlowQueryEvent) {
        performanceLogger.warn(
            "Slow query detected: query={}, duration={}ms, threshold={}ms",
            event.query, event.duration, SLOW_QUERY_THRESHOLD_MS
        )
        
        // Could send alert to monitoring system
        sendSlowQueryAlert(event)
    }
    
    @EventListener
    fun handleSlowRequest(event: SlowRequestEvent) {
        performanceLogger.warn(
            "Slow request detected: uri={}, method={}, duration={}ms, threshold={}ms",
            event.uri, event.method, event.duration, SLOW_REQUEST_THRESHOLD_MS
        )
        
        sendSlowRequestAlert(event)
    }
    
    @Scheduled(fixedRate = 30000) // Every 30 seconds
    fun monitorSystemResources() {
        val memoryUsage = getMemoryUsagePercentage()
        val cpuUsage = getCpuUsagePercentage()
        
        if (memoryUsage > HIGH_MEMORY_THRESHOLD_PERCENT) {
            performanceLogger.error(
                "High memory usage detected: {}% (threshold: {}%)",
                memoryUsage, HIGH_MEMORY_THRESHOLD_PERCENT
            )
            sendHighMemoryAlert(memoryUsage)
        }
        
        if (cpuUsage > HIGH_CPU_THRESHOLD_PERCENT) {
            performanceLogger.error(
                "High CPU usage detected: {}% (threshold: {}%)",
                cpuUsage, HIGH_CPU_THRESHOLD_PERCENT
            )
            sendHighCpuAlert(cpuUsage)
        }
        
        // Log current resource usage
        performanceLogger.info(
            "System resources: memory={}%, cpu={}%",
            memoryUsage, cpuUsage
        )
    }
    
    private fun getMemoryUsagePercentage(): Double {
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val usedMemory = totalMemory - freeMemory
        
        return (usedMemory.toDouble() / totalMemory) * 100
    }
    
    private fun getCpuUsagePercentage(): Double {
        val osBean = ManagementFactory.getOperatingSystemMXBean()
        return when (osBean) {
            is com.sun.management.OperatingSystemMXBean -> osBean.processCpuLoad * 100
            else -> 0.0
        }
    }
    
    private fun sendSlowQueryAlert(event: SlowQueryEvent) {
        // Integration with alerting system (Slack, PagerDuty, etc.)
        performanceLogger.info("Sending slow query alert for: {}", event.query)
    }
    
    private fun sendSlowRequestAlert(event: SlowRequestEvent) {
        performanceLogger.info("Sending slow request alert for: {} {}", event.method, event.uri)
    }
    
    private fun sendHighMemoryAlert(memoryUsage: Double) {
        performanceLogger.info("Sending high memory usage alert: {}%", memoryUsage)
    }
    
    private fun sendHighCpuAlert(cpuUsage: Double) {
        performanceLogger.info("Sending high CPU usage alert: {}%", cpuUsage)
    }
}

// Performance events
data class SlowQueryEvent(
    val query: String,
    val duration: Long,
    val parameters: List<Any> = emptyList()
)

data class SlowRequestEvent(
    val uri: String,
    val method: String,
    val duration: Long,
    val statusCode: Int,
    val userAgent: String? = null
)

// Request timing interceptor
@Component
class RequestTimingInterceptor : HandlerInterceptor {
    
    private val logger = LoggerFactory.getLogger(RequestTimingInterceptor::class.java)
    private val applicationEventPublisher: ApplicationEventPublisher
    
    constructor(applicationEventPublisher: ApplicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher
    }
    
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        request.setAttribute("startTime", System.currentTimeMillis())
        return true
    }
    
    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        val startTime = request.getAttribute("startTime") as? Long ?: return
        val duration = System.currentTimeMillis() - startTime
        
        logger.info(
            "Request completed: {} {} - {}ms - status: {}",
            request.method, request.requestURI, duration, response.status
        )
        
        // Check for slow requests
        if (duration > PerformanceMonitoringService.SLOW_REQUEST_THRESHOLD_MS) {
            applicationEventPublisher.publishEvent(
                SlowRequestEvent(
                    uri = request.requestURI,
                    method = request.method,
                    duration = duration,
                    statusCode = response.status,
                    userAgent = request.getHeader("User-Agent")
                )
            )
        }
    }
}
```

---

## 🎯 Best Practices

### **Do's**
- ✅ **Use structured logging** with consistent JSON format
- ✅ **Include correlation IDs** in all log entries
- ✅ **Set up meaningful health checks** for all dependencies
- ✅ **Create business-specific metrics** beyond technical metrics
- ✅ **Implement distributed tracing** for service interactions
- ✅ **Monitor performance proactively** with alerting
- ✅ **Log security events** with appropriate severity levels

### **Don'ts**
- ❌ **Don't log sensitive data** like passwords or tokens
- ❌ **Don't create too many custom metrics** - focus on what matters
- ❌ **Don't ignore log levels** - use appropriate levels for different events
- ❌ **Don't skip error context** - always include correlation IDs in errors
- ❌ **Don't forget log retention** - manage disk space with rotation
- ❌ **Don't block threads** with synchronous logging in high-traffic areas
- ❌ **Don't expose internal details** in public health check endpoints

### **Production Considerations**
- 🔒 **Security**: Sanitize logs, secure endpoints, audit access
- 📊 **Performance**: Use async logging, sampling for traces
- 💾 **Storage**: Configure log rotation, retention policies
- 🚨 **Alerting**: Set up intelligent alerts with proper thresholds
- 📈 **Scaling**: Consider log aggregation tools like ELK stack

---

## 🚀 Configuration Best Practices

### **Application Configuration**
```yaml
spring:
  application:
    name: observability-service
  
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,businessmetrics,applicationinfo
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
      show-components: always
    metrics:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        http.server.requests: true
      percentiles:
        http.server.requests: 0.5, 0.9, 0.95, 0.99
  health:
    circuitbreakers:
      enabled: true
    diskspace:
      enabled: true
    db:
      enabled: true

logging:
  level:
    com.learning.observability: DEBUG
    org.springframework.security: INFO
    org.springframework.web: INFO
    PERFORMANCE: INFO
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level [%X{correlationId}] %logger{36} - %msg%n"

app:
  monitoring:
    enabled: true
    performance:
      slow-query-threshold: 1000
      slow-request-threshold: 2000
    alerts:
      memory-threshold: 80
      cpu-threshold: 75
```

---

## 🎓 Summary

Logging & Observability provides:

- **📊 Structured Logging**: JSON formatted logs with correlation IDs
- **🔗 Request Correlation**: MDC context for distributed tracing
- **💓 Health Monitoring**: Custom health checks and business metrics
- **📈 Performance Tracking**: Timing, resource usage, and alerting
- **🔍 Distributed Tracing**: End-to-end request tracking across services
- **🚨 Proactive Monitoring**: Automated alerts and performance thresholds

**Key Takeaways**:
1. **Observability First**: Design logging and monitoring from the start
2. **Correlation Context**: Always include correlation IDs for debugging
3. **Business Metrics**: Monitor what matters to your business, not just technical metrics
4. **Proactive Alerts**: Set up intelligent alerting before problems occur
5. **Performance Monitoring**: Track and optimize critical system operations

Next lesson: **Dockerizing Your Application** for consistent deployment across environments!