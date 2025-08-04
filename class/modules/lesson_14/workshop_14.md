# 🚀 Lesson 14 Workshop: Scheduled Tasks & Async Processing

## 🎯 Workshop Objective

Build a comprehensive background processing system with scheduled maintenance tasks, asynchronous email notifications, and performance monitoring. You'll transform blocking operations into efficient async workflows.

**⏱️ Estimated Time**: 35 minutes

---

## 🏗️ What You'll Build

### **Before**: Blocking Operations
```kotlin
// Synchronous approach - blocks user requests
fun registerUser(request: CreateUserRequest): User {
    val user = userRepository.save(user)
    emailService.sendWelcomeEmail(user)     // Blocks for 2-3 seconds
    auditService.logUserRegistration(user)  // Blocks for 500ms
    return user  // User waits 3+ seconds total
}
```

### **After**: Lightning-Fast Async Operations
```kotlin
// Asynchronous approach - immediate response
fun registerUser(request: CreateUserRequest): User {
    val user = userRepository.save(user)
    emailService.sendWelcomeEmailAsync(user)     // Returns immediately
    auditService.logUserRegistrationAsync(user) // Returns immediately
    return user  // User gets instant response (< 100ms)
}
```

### **Plus Automated Background Tasks**
- **Email Notifications**: Welcome emails, password resets, bulk notifications
- **Database Cleanup**: Remove old logs, expired sessions, orphaned data
- **Daily Reports**: User statistics, system health, performance metrics
- **Cache Warming**: Pre-load popular content during off-peak hours

**🎯 Performance Target**: 
- 95%+ faster user-facing operations
- Automated maintenance reducing manual intervention
- Real-time task monitoring and health checks

---

## 📁 Project Structure

```
class/workshop/lesson_14/
├── build.gradle.kts          # ✅ Async dependencies configured
├── src/main/
│   ├── kotlin/com/learning/async/
│   │   ├── AsyncApplication.kt
│   │   ├── config/
│   │   │   ├── AsyncConfig.kt        # TODO: Configure thread pools
│   │   │   └── SchedulingConfig.kt   # TODO: Enable scheduling
│   │   ├── model/
│   │   │   ├── User.kt
│   │   │   ├── EmailNotification.kt
│   │   │   ├── AuditLog.kt
│   │   │   └── DailyStatistics.kt
│   │   ├── repository/
│   │   │   ├── UserRepository.kt
│   │   │   ├── EmailLogRepository.kt
│   │   │   ├── AuditLogRepository.kt
│   │   │   └── DailyStatsRepository.kt
│   │   ├── service/
│   │   │   ├── EmailService.kt       # TODO: Add @Async methods
│   │   │   ├── UserService.kt        # TODO: Integrate async operations
│   │   │   ├── CleanupService.kt     # TODO: Add @Scheduled tasks
│   │   │   └── TaskMonitorService.kt # TODO: Track task metrics
│   │   ├── controller/
│   │   │   ├── UserController.kt
│   │   │   ├── EmailController.kt
│   │   │   └── TaskController.kt
│   │   └── dto/
│   │       └── AsyncDTOs.kt
│   └── resources/
│       └── application.yml           # TODO: Configure thread pools
└── src/test/
    └── kotlin/com/learning/async/
        └── AsyncApplicationTests.kt
```

---

## 🛠️ Step 1: Configure Async & Scheduling

### **📝 TODO: Update build.gradle.kts**
```kotlin
dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    
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
    name: async-workshop
  
  datasource:
    url: jdbc:h2:mem:async_db
    driver-class-name: org.h2.Driver
    username: sa
    password: password
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  
  h2:
    console:
      enabled: true
      path: /h2-console

# TODO: Add async configuration
async:
  thread-pools:
    general:
      core-size: 5
      max-size: 20
      queue-capacity: 100
    fast:
      core-size: 10
      max-size: 50
      queue-capacity: 25
    heavy:
      core-size: 2
      max-size: 8
      queue-capacity: 500
  
# TODO: Add scheduling configuration  
scheduling:
  pool-size: 5
  
logging:
  level:
    com.learning.async: DEBUG
    org.springframework.scheduling: DEBUG
```

---

## 🛠️ Step 2: Create Async Configuration

### **📝 TODO: Implement AsyncConfig.kt**
```kotlin
package com.learning.async.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.ThreadPoolExecutor

@Configuration
@EnableAsync  // TODO: Add this annotation
@ConfigurationProperties(prefix = "async.thread-pools")
class AsyncConfig {
    
    // TODO: Add properties for thread pool configuration
    data class ThreadPoolProperties(
        var coreSize: Int = 5,
        var maxSize: Int = 20,
        var queueCapacity: Int = 100
    )
    
    var general = ThreadPoolProperties()
    var fast = ThreadPoolProperties(10, 50, 25)
    var heavy = ThreadPoolProperties(2, 8, 500)
    
    // TODO: Create general purpose thread pool
    @Bean("generalTaskExecutor")
    fun generalTaskExecutor(): ThreadPoolTaskExecutor {
        // TODO: Configure thread pool with general properties
        // HINT: Set core/max pool size, queue capacity, thread name prefix
        // HINT: Set rejection policy and initialize
    }
    
    // TODO: Create fast task executor for high-priority operations
    @Bean("fastTaskExecutor") 
    fun fastTaskExecutor(): ThreadPoolTaskExecutor {
        // TODO: Configure for low-latency operations
        // HINT: Use fast properties, smaller queue for responsiveness
    }
    
    // TODO: Create heavy task executor for long-running operations
    @Bean("heavyTaskExecutor")
    fun heavyTaskExecutor(): ThreadPoolTaskExecutor {
        // TODO: Configure for CPU-intensive tasks
        // HINT: Use heavy properties, larger queue, CallerRunsPolicy
    }
}
```

### **📝 TODO: Implement SchedulingConfig.kt**
```kotlin
package com.learning.async.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler

@Configuration
@EnableScheduling  // TODO: Add this annotation
@ConfigurationProperties(prefix = "scheduling")
class SchedulingConfig {
    
    var poolSize: Int = 5
    
    // TODO: Create scheduled task executor
    @Bean("scheduledTaskExecutor")
    fun scheduledTaskExecutor(): ThreadPoolTaskScheduler {
        // TODO: Configure scheduler with pool size and thread name prefix
        // HINT: Set poolSize and threadNamePrefix, then initialize
    }
}
```

---

## 🛠️ Step 3: Implement Async Email Service

### **📝 TODO: Create EmailService.kt**
```kotlin
package com.learning.async.service

import com.learning.async.model.EmailNotification
import com.learning.async.model.User
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import kotlin.random.Random

@Service
class EmailService {
    
    private val logger = LoggerFactory.getLogger(EmailService::class.java)
    
    // TODO: Add @Async annotation for welcome emails
    // HINT: Use "fastTaskExecutor" for user-facing operations
    fun sendWelcomeEmailAsync(user: User): CompletableFuture<EmailResult> {
        return try {
            logger.info("Sending welcome email to ${user.email}")
            
            // TODO: Simulate email sending delay
            // HINT: Use Thread.sleep(1500 + Random.nextInt(1000))
            
            logger.info("Welcome email sent to ${user.email}")
            
            // TODO: Return successful CompletableFuture with EmailResult
            
        } catch (e: Exception) {
            logger.error("Failed to send welcome email to ${user.email}", e)
            // TODO: Return failed CompletableFuture with error
        }
    }
    
    // TODO: Add @Async annotation for password reset emails
    // HINT: Use "generalTaskExecutor" for general operations
    fun sendPasswordResetEmailAsync(email: String, resetToken: String): CompletableFuture<EmailResult> {
        // TODO: Implement password reset email sending
        // TODO: Include resetToken in email (simulate)
        // TODO: Return CompletableFuture<EmailResult>
    }
    
    // TODO: Add @Async annotation for bulk notifications
    // HINT: Use "heavyTaskExecutor" for bulk operations
    fun sendBulkNotificationsAsync(notifications: List<EmailNotification>): CompletableFuture<BulkEmailResult> {
        return CompletableFuture.supplyAsync {
            var successCount = 0
            var failureCount = 0
            val results = mutableListOf<EmailResult>()
            
            // TODO: Process each notification
            notifications.forEach { notification ->
                try {
                    // TODO: Simulate individual email sending
                    // TODO: Track success/failure counts
                    // TODO: Add result to results list
                } catch (e: Exception) {
                    // TODO: Handle individual failures
                }
            }
            
            // TODO: Return BulkEmailResult with statistics
            BulkEmailResult(
                total = notifications.size,
                successful = successCount,
                failed = failureCount,
                results = results,
                duration = 0 // TODO: Calculate actual duration
            )
        }
    }
    
    // Synchronous method for comparison
    fun sendWelcomeEmailSync(user: User): EmailResult {
        logger.info("Sending welcome email synchronously to ${user.email}")
        Thread.sleep(2000) // Simulate delay
        logger.info("Welcome email sent synchronously to ${user.email}")
        
        return EmailResult(
            recipient = user.email,
            subject = "Welcome!",
            success = true,
            sentAt = LocalDateTime.now()
        )
    }
}

// TODO: Define data classes
data class EmailResult(
    val recipient: String,
    val subject: String,
    val success: Boolean,
    val error: String? = null,
    val sentAt: LocalDateTime
)

data class BulkEmailResult(
    val total: Int,
    val successful: Int,
    val failed: Int,
    val results: List<EmailResult>,
    val duration: Long
)
```

---

## 🛠️ Step 4: Create Scheduled Cleanup Service

### **📝 TODO: Implement CleanupService.kt**
```kotlin
package com.learning.async.service

import com.learning.async.repository.AuditLogRepository
import com.learning.async.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CleanupService(
    private val auditLogRepository: AuditLogRepository,
    private val userRepository: UserRepository,
    private val taskMonitorService: TaskMonitorService
) {
    
    private val logger = LoggerFactory.getLogger(CleanupService::class.java)
    
    // TODO: Add @Scheduled annotation for daily cleanup at 2 AM
    // HINT: Use cron = "0 0 2 * * *"
    fun cleanupOldAuditLogs() {
        val taskName = "cleanup-audit-logs"
        taskMonitorService.recordTaskStart(taskName)
        val startTime = System.currentTimeMillis()
        
        try {
            // TODO: Calculate cutoff date (90 days ago)
            val cutoffDate = LocalDateTime.now().minusDays(90)
            
            // TODO: Delete old audit logs
            val deletedCount = auditLogRepository.deleteByCreatedAtBefore(cutoffDate)
            
            val duration = System.currentTimeMillis() - startTime
            taskMonitorService.recordTaskCompletion(taskName, duration, true)
            
            logger.info("Cleanup completed: Deleted $deletedCount old audit logs in ${duration}ms")
            
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            taskMonitorService.recordTaskCompletion(taskName, duration, false)
            logger.error("Failed to cleanup audit logs", e)
        }
    }
    
    // TODO: Add @Scheduled annotation for hourly task
    // HINT: Use fixedRate = 3600000 (1 hour in milliseconds)
    fun generateHourlyStats() {
        val taskName = "generate-hourly-stats"
        taskMonitorService.recordTaskStart(taskName)
        val startTime = System.currentTimeMillis()
        
        try {
            // TODO: Count users registered in the last hour
            val lastHour = LocalDateTime.now().minusHours(1)
            val now = LocalDateTime.now()
            
            val newUsers = userRepository.countByCreatedAtBetween(lastHour, now)
            val totalUsers = userRepository.count()
            
            logger.info("Hourly stats: $newUsers new users (total: $totalUsers)")
            
            val duration = System.currentTimeMillis() - startTime
            taskMonitorService.recordTaskCompletion(taskName, duration, true)
            
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            taskMonitorService.recordTaskCompletion(taskName, duration, false)
            logger.error("Failed to generate hourly stats", e)
        }
    }
    
    // TODO: Add @Scheduled annotation for cache warming every 30 minutes
    // HINT: Use fixedDelay = 1800000, initialDelay = 60000
    fun warmupCache() {
        val taskName = "warmup-cache"
        taskMonitorService.recordTaskStart(taskName)
        val startTime = System.currentTimeMillis()
        
        try {
            logger.info("Starting cache warmup...")
            
            // TODO: Simulate cache warming operations
            // TODO: Pre-load popular user profiles
            // TODO: Pre-load recent posts
            
            Thread.sleep(2000) // Simulate cache warming time
            
            val duration = System.currentTimeMillis() - startTime
            taskMonitorService.recordTaskCompletion(taskName, duration, true)
            
            logger.info("Cache warmup completed in ${duration}ms")
            
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            taskMonitorService.recordTaskCompletion(taskName, duration, false)
            logger.error("Failed to warmup cache", e)
        }
    }
}
```

---

## 🛠️ Step 5: Create Task Monitor Service

### **📝 TODO: Implement TaskMonitorService.kt**
```kotlin
package com.learning.async.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Service
class TaskMonitorService {
    
    private val logger = LoggerFactory.getLogger(TaskMonitorService::class.java)
    private val taskMetrics = ConcurrentHashMap<String, TaskMetric>()
    
    // TODO: Implement task tracking methods
    fun recordTaskStart(taskName: String) {
        // TODO: Get or create TaskMetric for taskName
        // TODO: Increment started count
        // TODO: Log task start
    }
    
    fun recordTaskCompletion(taskName: String, duration: Long, success: Boolean) {
        // TODO: Get TaskMetric for taskName
        // TODO: Record execution result
        // TODO: Log completion with appropriate level (info/warn)
    }
    
    fun getTaskMetrics(): Map<String, TaskMetric> = taskMetrics.toMap()
    
    fun getTaskStatistics(taskName: String): TaskStatistics? {
        val metric = taskMetrics[taskName] ?: return null
        
        // TODO: Calculate and return TaskStatistics
        return TaskStatistics(
            taskName = taskName,
            totalExecutions = metric.totalExecutions.get(),
            successfulExecutions = metric.successfulExecutions.get(),
            failedExecutions = metric.failedExecutions.get(),
            averageDuration = metric.averageDuration,
            lastExecution = metric.lastExecution,
            successRate = metric.successRate
        )
    }
    
    fun getAllTaskStatistics(): List<TaskStatistics> {
        // TODO: Return statistics for all tracked tasks
        return taskMetrics.keys.mapNotNull { getTaskStatistics(it) }
    }
}

// TODO: Define TaskMetric data class
data class TaskMetric(
    val name: String,
    val totalExecutions: AtomicLong = AtomicLong(0),
    val successfulExecutions: AtomicLong = AtomicLong(0), 
    val failedExecutions: AtomicLong = AtomicLong(0),
    val totalDuration: AtomicLong = AtomicLong(0),
    var lastExecution: LocalDateTime? = null
) {
    // TODO: Add methods for incrementing counters and recording execution
    fun incrementStarted() {
        // TODO: Increment total executions
    }
    
    fun recordExecution(duration: Long, success: Boolean) {
        // TODO: Update duration and last execution time
        // TODO: Increment success or failure count
    }
    
    val averageDuration: Double
        get() = if (totalExecutions.get() > 0) {
            totalDuration.get().toDouble() / totalExecutions.get()
        } else 0.0
    
    val successRate: Double
        get() = if (totalExecutions.get() > 0) {
            successfulExecutions.get().toDouble() / totalExecutions.get()
        } else 0.0
}

data class TaskStatistics(
    val taskName: String,
    val totalExecutions: Long,
    val successfulExecutions: Long,
    val failedExecutions: Long,
    val averageDuration: Double,
    val lastExecution: LocalDateTime?,
    val successRate: Double
)
```

---

## 🛠️ Step 6: Integrate Async Operations in UserService

### **📝 TODO: Update UserService.kt**
```kotlin
package com.learning.async.service

import com.learning.async.model.User
import com.learning.async.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

@Service
class UserService(
    private val userRepository: UserRepository,
    private val emailService: EmailService,
    private val auditService: AuditService
) {
    
    private val logger = LoggerFactory.getLogger(UserService::class.java)
    
    // TODO: Implement async user registration
    fun registerUserAsync(user: User): User {
        logger.info("Registering user: ${user.email}")
        
        // Save user synchronously (database operation)
        val savedUser = userRepository.save(user)
        
        // TODO: Start async operations
        // TODO: Send welcome email asynchronously
        // TODO: Log registration asynchronously
        // TODO: Don't wait for completion - return immediately
        
        logger.info("User registration initiated for: ${user.email}")
        return savedUser
    }
    
    // Synchronous version for comparison
    fun registerUserSync(user: User): User {
        logger.info("Registering user synchronously: ${user.email}")
        val startTime = System.currentTimeMillis()
        
        // Save user
        val savedUser = userRepository.save(user)
        
        // Send email synchronously (blocks)
        emailService.sendWelcomeEmailSync(savedUser)
        
        // Log audit synchronously (blocks)
        auditService.logUserRegistrationSync(savedUser)
        
        val duration = System.currentTimeMillis() - startTime
        logger.info("User registration completed synchronously in ${duration}ms")
        return savedUser
    }
    
    // TODO: Implement bulk user processing
    fun processBulkUsers(users: List<User>): CompletableFuture<BulkProcessResult> {
        return CompletableFuture.supplyAsync {
            val startTime = System.currentTimeMillis()
            var successCount = 0
            var failureCount = 0
            
            users.forEach { user ->
                try {
                    // TODO: Process each user asynchronously
                    // TODO: Save user and trigger async operations
                    registerUserAsync(user)
                    successCount++
                } catch (e: Exception) {
                    logger.error("Failed to process user ${user.email}", e)
                    failureCount++
                }
            }
            
            val duration = System.currentTimeMillis() - startTime
            BulkProcessResult(
                total = users.size,
                successful = successCount,
                failed = failureCount,
                duration = duration
            )
        }
    }
}

data class BulkProcessResult(
    val total: Int,
    val successful: Int,
    val failed: Int,
    val duration: Long
)
```

---

## 🛠️ Step 7: Create Management Controllers

### **📝 TODO: Create TaskController.kt**
```kotlin
package com.learning.async.controller

import com.learning.async.service.TaskMonitorService
import com.learning.async.service.CleanupService
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tasks")
class TaskController(
    private val taskMonitorService: TaskMonitorService,
    private val cleanupService: CleanupService,
    private val generalTaskExecutor: ThreadPoolTaskExecutor,
    private val fastTaskExecutor: ThreadPoolTaskExecutor,
    private val heavyTaskExecutor: ThreadPoolTaskExecutor
) {
    
    // TODO: Endpoint to get all task statistics
    @GetMapping("/stats")
    fun getTaskStatistics(): Map<String, Any> {
        // TODO: Return task statistics and thread pool status
        return mapOf(
            "tasks" to taskMonitorService.getAllTaskStatistics(),
            "threadPools" to getThreadPoolStatus()
        )
    }
    
    // TODO: Endpoint to get specific task statistics
    @GetMapping("/stats/{taskName}")
    fun getTaskStatistics(@PathVariable taskName: String): Any {
        // TODO: Return statistics for specific task or 404 if not found
    }
    
    // TODO: Endpoint to trigger manual cleanup
    @PostMapping("/cleanup/audit-logs")
    fun triggerAuditLogCleanup(): Map<String, String> {
        // TODO: Manually trigger audit log cleanup
        // TODO: Return success message
    }
    
    // TODO: Endpoint to trigger cache warmup
    @PostMapping("/warmup-cache")
    fun triggerCacheWarmup(): Map<String, String> {
        // TODO: Manually trigger cache warmup
        // TODO: Return success message
    }
    
    // TODO: Helper method to get thread pool status
    private fun getThreadPoolStatus(): Map<String, Any> {
        return mapOf(
            "general" to getExecutorStatus(generalTaskExecutor),
            "fast" to getExecutorStatus(fastTaskExecutor),
            "heavy" to getExecutorStatus(heavyTaskExecutor)
        )
    }
    
    private fun getExecutorStatus(executor: ThreadPoolTaskExecutor): Map<String, Any> {
        return mapOf(
            "poolSize" to executor.poolSize,
            "activeCount" to executor.activeCount,
            "queueSize" to executor.queueSize,
            "completedTaskCount" to executor.threadPoolExecutor.completedTaskCount
        )
    }
}
```

---

## 🛠️ Step 8: Performance Testing

### **📝 TODO: Update AsyncApplicationTests.kt**
```kotlin
package com.learning.async

import com.learning.async.model.User
import com.learning.async.service.UserService
import com.learning.async.service.EmailService
import com.learning.async.service.TaskMonitorService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

@SpringBootTest
@TestPropertySource(properties = [
    "spring.datasource.url=jdbc:h2:mem:test_async",
    "logging.level.com.learning.async=DEBUG"
])
class AsyncApplicationTests {
    
    @Autowired
    private lateinit var userService: UserService
    
    @Autowired
    private lateinit var emailService: EmailService
    
    @Autowired
    private lateinit var taskMonitorService: TaskMonitorService
    
    @Test
    fun contextLoads() {
        // Basic Spring Boot test
    }
    
    // TODO: Test async performance improvement
    @Test
    fun `should demonstrate async performance improvement`() {
        val user = User(
            email = "test@example.com",
            firstName = "Test",
            lastName = "User"
        )
        
        // Test synchronous registration
        val syncTime = measureTimeMillis {
            userService.registerUserSync(user.copy(email = "sync@example.com"))
        }
        
        // Test asynchronous registration  
        val asyncTime = measureTimeMillis {
            userService.registerUserAsync(user.copy(email = "async@example.com"))
        }
        
        println("Sync registration: ${syncTime}ms")
        println("Async registration: ${asyncTime}ms")
        println("Performance improvement: ${((syncTime - asyncTime).toDouble() / syncTime * 100).toInt()}%")
        
        // TODO: Assert that async is significantly faster
        // TODO: Verify user was saved correctly
    }
    
    // TODO: Test async email sending
    @Test
    fun `should send email asynchronously`() {
        val user = User(
            email = "email-test@example.com",
            firstName = "Email",
            lastName = "Test"
        )
        
        // TODO: Send email and verify future is returned immediately
        val future = emailService.sendWelcomeEmailAsync(user)
        
        // TODO: Wait for completion and verify result
        val result = future.get(5, TimeUnit.SECONDS)
        
        // TODO: Assert email was sent successfully
    }
    
    // TODO: Test bulk processing
    @Test
    fun `should process users in bulk efficiently`() {
        val users = (1..10).map { i ->
            User(
                email = "bulk$i@example.com",
                firstName = "Bulk",
                lastName = "User$i"
            )
        }
        
        // TODO: Process bulk users and measure time
        val startTime = System.currentTimeMillis()
        val future = userService.processBulkUsers(users)
        val result = future.get(30, TimeUnit.SECONDS)
        val totalTime = System.currentTimeMillis() - startTime
        
        // TODO: Verify all users were processed
        // TODO: Assert reasonable processing time
        println("Bulk processing: ${result.successful}/${result.total} users in ${totalTime}ms")
    }
    
    // TODO: Test task monitoring
    @Test
    fun `should track task execution metrics`() {
        // TODO: Execute some tasks to generate metrics
        val user = User(email = "metrics@example.com", firstName = "Metrics", lastName = "Test")
        userService.registerUserAsync(user)
        
        // Wait a bit for async operations to complete
        Thread.sleep(2000)
        
        // TODO: Verify task statistics are being recorded
        val allStats = taskMonitorService.getAllTaskStatistics()
        println("Task statistics recorded for ${allStats.size} tasks")
        
        // TODO: Assert that statistics are being tracked
    }
}
```

---

## 🚀 Step 9: Test Your Implementation

### **1. Run the Application**
```bash
cd class/workshop/lesson_14
./gradlew bootRun
```

### **2. Test Async Performance**

**Register users and compare performance:**
```bash
# Async registration (should be fast)
time curl -X POST http://localhost:8080/api/users/register-async \
  -H "Content-Type: application/json" \
  -d '{"email":"async@example.com","firstName":"Async","lastName":"User"}'

# Sync registration (should be slow)  
time curl -X POST http://localhost:8080/api/users/register-sync \
  -H "Content-Type: application/json" \
  -d '{"email":"sync@example.com","firstName":"Sync","lastName":"User"}'
```

### **3. Monitor Task Execution**
```bash
# Check task statistics
curl http://localhost:8080/api/tasks/stats

# Check thread pool status
curl http://localhost:8080/api/tasks/stats | jq '.threadPools'

# Trigger manual cleanup
curl -X POST http://localhost:8080/api/tasks/cleanup/audit-logs

# Trigger cache warmup
curl -X POST http://localhost:8080/api/tasks/warmup-cache
```

### **4. Test Bulk Operations**
```bash
# Process multiple users
curl -X POST http://localhost:8080/api/users/bulk \
  -H "Content-Type: application/json" \
  -d '[
    {"email":"bulk1@example.com","firstName":"Bulk","lastName":"User1"},
    {"email":"bulk2@example.com","firstName":"Bulk","lastName":"User2"},
    {"email":"bulk3@example.com","firstName":"Bulk","lastName":"User3"}
  ]'
```

---

## 🎯 Expected Results

### **Performance Improvements**
- **Async Registration**: ~50-100ms response time
- **Sync Registration**: ~3000-4000ms response time  
- **Performance Gain**: 95%+ faster user-facing operations

### **Scheduled Task Logs**
```
2024-01-15 02:00:00.123 INFO  --- CleanupService: Cleanup completed: Deleted 150 old audit logs in 245ms
2024-01-15 03:00:00.456 INFO  --- CleanupService: Hourly stats: 23 new users (total: 1,247)
2024-01-15 03:30:00.789 INFO  --- CleanupService: Cache warmup completed in 2,156ms
```

### **Task Statistics Response**
```json
{
  "tasks": [
    {
      "taskName": "send-welcome-email",
      "totalExecutions": 45,
      "successfulExecutions": 44,
      "failedExecutions": 1,
      "averageDuration": 1523.4,
      "successRate": 0.98
    }
  ],
  "threadPools": {
    "general": {
      "poolSize": 5,
      "activeCount": 2,
      "queueSize": 3,
      "completedTaskCount": 127
    }
  }
}
```

---

## 🏆 Challenge Extensions

### **🔥 Bonus Challenge 1: Email Queue Management**
Implement an email queue system that processes emails in batches and handles failures with retry logic.

### **🔥 Bonus Challenge 2: Dynamic Thread Pool Scaling**
Create a system that automatically adjusts thread pool sizes based on current load and queue depth.

### **🔥 Bonus Challenge 3: Task Scheduling UI**
Build a web interface to view scheduled tasks, trigger manual execution, and modify cron expressions.

### **🔥 Bonus Challenge 4: Circuit Breaker Integration**
Add circuit breaker pattern to email service to handle external service failures gracefully.

---

## 🎓 Learning Outcomes

Upon completion, you'll have:

✅ **Implemented async processing** with @Async annotations and custom thread pools  
✅ **Created scheduled tasks** with @Scheduled annotations and cron expressions  
✅ **Built task monitoring** with comprehensive execution metrics and health checks  
✅ **Achieved 95%+ performance improvement** in user-facing operations  
✅ **Automated maintenance tasks** with background cleanup and reporting  
✅ **Tested async operations** with proper verification and timeout handling

**🚀 Next Lesson**: File Handling & Uploads - managing file operations and cloud storage!