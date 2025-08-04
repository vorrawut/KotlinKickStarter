# 🚀 Lesson 14: Scheduled Tasks & Async Processing - Concept Guide

## 🎯 Learning Objectives

By the end of this lesson, you will:
- **Implement scheduled tasks** using @Scheduled annotations and cron expressions
- **Configure async processing** with @Async methods and custom thread pools
- **Monitor task execution** with comprehensive logging and metrics
- **Handle background operations** like email notifications and data cleanup
- **Manage thread pools** for optimal performance and resource usage
- **Test async operations** with proper verification strategies

---

## 🔍 Why Async & Scheduled Processing Matters

### **Real-World Scenarios**
```kotlin
// Synchronous approach - blocks user request
fun sendWelcomeEmail(user: User): ResponseEntity<User> {
    val savedUser = userService.save(user)
    emailService.sendWelcomeEmail(user) // Takes 2-3 seconds!
    return ResponseEntity.ok(savedUser) // User waits 3+ seconds
}

// Asynchronous approach - immediate response
fun registerUser(user: User): ResponseEntity<User> {
    val savedUser = userService.save(user)
    emailService.sendWelcomeEmailAsync(user) // Returns immediately
    return ResponseEntity.ok(savedUser) // User gets instant response
}
```

### **Business Benefits**
- **Improved User Experience**: Instant API responses
- **Better Resource Utilization**: Non-blocking operations
- **Automated Maintenance**: Background cleanup and monitoring
- **Scalable Architecture**: Handle high-load scenarios efficiently

---

## 📅 Scheduled Tasks with @Scheduled

### **Cron Expression Fundamentals**
```kotlin
@Component
class MaintenanceTasks {
    
    // Run every minute
    @Scheduled(cron = "0 * * * * *")
    fun everyMinute() {
        logger.info("Running every minute: ${LocalDateTime.now()}")
    }
    
    // Run every hour at minute 0
    @Scheduled(cron = "0 0 * * * *")
    fun hourlyTask() {
        logger.info("Hourly maintenance: ${LocalDateTime.now()}")
    }
    
    // Run daily at 3 AM
    @Scheduled(cron = "0 0 3 * * *")
    fun dailyCleanup() {
        logger.info("Daily cleanup started: ${LocalDateTime.now()}")
        cleanupOldData()
    }
    
    // Run every weekday at 9 AM
    @Scheduled(cron = "0 0 9 * * MON-FRI")
    fun weekdayReport() {
        logger.info("Generating weekday report: ${LocalDateTime.now()}")
        generateDailyReport()
    }
    
    // Run on the 1st day of every month at midnight
    @Scheduled(cron = "0 0 0 1 * *")
    fun monthlyTask() {
        logger.info("Monthly task execution: ${LocalDateTime.now()}")
        processMonthlyBilling()
    }
}
```

### **Cron Expression Format**
```
┌───────────── second (0-59)
│ ┌───────────── minute (0-59)
│ │ ┌───────────── hour (0-23)
│ │ │ ┌───────────── day of month (1-31)
│ │ │ │ ┌───────────── month (1-12)
│ │ │ │ │ ┌───────────── day of week (0-7, Sunday = 0 or 7)
│ │ │ │ │ │
* * * * * *
```

### **Common Cron Patterns**
| Pattern | Description | Cron Expression |
|---------|-------------|-----------------|
| **Every minute** | Testing/monitoring | `0 * * * * *` |
| **Every 5 minutes** | Cache refresh | `0 */5 * * * *` |
| **Every hour** | Hourly reports | `0 0 * * * *` |
| **Daily at 2 AM** | Backups | `0 0 2 * * *` |
| **Weekly on Sunday** | Weekly reports | `0 0 0 * * SUN` |
| **Monthly on 1st** | Billing | `0 0 0 1 * *` |
| **Weekdays only** | Business tasks | `0 0 9 * * MON-FRI` |

### **Fixed Rate vs Fixed Delay**
```kotlin
@Component
class SchedulingExamples {
    
    // Fixed rate - every 5 seconds regardless of execution time
    @Scheduled(fixedRate = 5000)
    fun fixedRateTask() {
        logger.info("Fixed rate task: ${LocalDateTime.now()}")
        // If this takes 7 seconds, next execution starts immediately after
    }
    
    // Fixed delay - 5 seconds AFTER the previous execution completes
    @Scheduled(fixedDelay = 5000)
    fun fixedDelayTask() {
        logger.info("Fixed delay task: ${LocalDateTime.now()}")
        // If this takes 7 seconds, next execution starts 5 seconds after completion
    }
    
    // Initial delay - wait 10 seconds before first execution
    @Scheduled(fixedDelay = 5000, initialDelay = 10000)
    fun delayedStartTask() {
        logger.info("Delayed start task: ${LocalDateTime.now()}")
    }
}
```

---

## ⚡ Asynchronous Processing with @Async

### **Basic Async Configuration**
```kotlin
@Configuration
@EnableAsync
class AsyncConfig {
    
    @Bean("taskExecutor")
    fun taskExecutor(): ThreadPoolTaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 5        // Minimum threads
        executor.maxPoolSize = 20        // Maximum threads
        executor.queueCapacity = 100     // Queue size before creating new threads
        executor.keepAliveSeconds = 60   // Thread timeout
        executor.setThreadNamePrefix("async-")
        executor.setRejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy())
        executor.initialize()
        return executor
    }
}
```

### **Async Method Patterns**
```kotlin
@Service
class EmailService {
    
    private val logger = LoggerFactory.getLogger(EmailService::class.java)
    
    // Fire and forget - no return value
    @Async("taskExecutor")
    fun sendWelcomeEmailAsync(user: User) {
        logger.info("Sending welcome email to ${user.email}")
        Thread.sleep(2000) // Simulate email sending
        logger.info("Welcome email sent to ${user.email}")
    }
    
    // Return CompletableFuture for result tracking
    @Async("taskExecutor")
    fun sendPasswordResetAsync(email: String): CompletableFuture<Boolean> {
        return try {
            logger.info("Sending password reset to $email")
            Thread.sleep(1500) // Simulate processing
            logger.info("Password reset sent to $email")
            CompletableFuture.completedFuture(true)
        } catch (e: Exception) {
            logger.error("Failed to send password reset to $email", e)
            CompletableFuture.completedFuture(false)
        }
    }
    
    // Async with custom exception handling
    @Async("taskExecutor")
    fun sendBulkNotificationAsync(users: List<User>): CompletableFuture<Int> {
        return CompletableFuture.supplyAsync {
            var successCount = 0
            users.forEach { user ->
                try {
                    sendNotification(user)
                    successCount++
                } catch (e: Exception) {
                    logger.error("Failed to send notification to ${user.email}", e)
                }
            }
            logger.info("Bulk notification completed: $successCount/${users.size} sent")
            successCount
        }
    }
}
```

### **Working with CompletableFuture**
```kotlin
@Service
class UserRegistrationService {
    
    @Autowired
    private lateinit var emailService: EmailService
    
    @Autowired
    private lateinit var auditService: AuditService
    
    fun registerUser(userRequest: CreateUserRequest): User {
        // Save user synchronously
        val user = userRepository.save(User(
            username = userRequest.username,
            email = userRequest.email
        ))
        
        // Start async operations
        val emailFuture = emailService.sendWelcomeEmailAsync(user)
        val auditFuture = auditService.logUserRegistrationAsync(user)
        
        // Option 1: Fire and forget (don't wait for completion)
        // Return immediately for best user experience
        
        // Option 2: Wait for critical operations
        try {
            emailFuture.get(5, TimeUnit.SECONDS) // Wait max 5 seconds
            logger.info("Welcome email confirmed for ${user.email}")
        } catch (e: TimeoutException) {
            logger.warn("Welcome email taking longer than expected for ${user.email}")
        }
        
        return user
    }
    
    // Combining multiple async operations
    fun processUserBatch(users: List<User>): BatchResult {
        val futures = users.map { user ->
            CompletableFuture.supplyAsync {
                processUser(user)
            }
        }
        
        // Wait for all to complete
        val results = CompletableFuture.allOf(*futures.toTypedArray())
            .thenApply { futures.map { it.join() } }
            .join()
        
        return BatchResult(
            total = users.size,
            successful = results.count { it.success },
            failed = results.count { !it.success }
        )
    }
}
```

---

## ⚙️ Thread Pool Configuration

### **Thread Pool Strategies**
```kotlin
@Configuration
@EnableAsync
class ThreadPoolConfig {
    
    // General purpose thread pool
    @Bean("generalTaskExecutor")
    fun generalTaskExecutor(): ThreadPoolTaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 5
        executor.maxPoolSize = 20
        executor.queueCapacity = 100
        executor.setThreadNamePrefix("general-")
        executor.initialize()
        return executor
    }
    
    // High-priority, low-latency tasks
    @Bean("fastTaskExecutor")
    fun fastTaskExecutor(): ThreadPoolTaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 10      // More core threads for responsiveness
        executor.maxPoolSize = 50       // Higher max for burst capacity
        executor.queueCapacity = 25     // Smaller queue for lower latency
        executor.setThreadNamePrefix("fast-")
        executor.initialize()
        return executor
    }
    
    // Heavy, long-running tasks
    @Bean("heavyTaskExecutor")
    fun heavyTaskExecutor(): ThreadPoolTaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 2       // Fewer core threads
        executor.maxPoolSize = 8        // Limited max to prevent resource exhaustion
        executor.queueCapacity = 500    // Large queue for many tasks
        executor.setThreadNamePrefix("heavy-")
        executor.setRejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy())
        executor.initialize()
        return executor
    }
    
    // Scheduled task executor
    @Bean("scheduledTaskExecutor")
    fun scheduledTaskExecutor(): ThreadPoolTaskScheduler {
        val scheduler = ThreadPoolTaskScheduler()
        scheduler.poolSize = 5
        scheduler.setThreadNamePrefix("scheduled-")
        scheduler.initialize()
        return scheduler
    }
}
```

### **Task Executor Selection Strategy**
```kotlin
@Service
class TaskDispatchService {
    
    // Fast tasks - user notifications, cache updates
    @Async("fastTaskExecutor")
    fun sendInstantNotification(message: String) {
        // Quick operations that users are waiting for
    }
    
    // General tasks - email sending, file processing
    @Async("generalTaskExecutor") 
    fun processUserAction(action: UserAction) {
        // Standard background operations
    }
    
    // Heavy tasks - report generation, bulk operations
    @Async("heavyTaskExecutor")
    fun generateLargeReport(criteria: ReportCriteria): CompletableFuture<Report> {
        // CPU/memory intensive operations
        return CompletableFuture.completedFuture(generateReport(criteria))
    }
}
```

---

## 📊 Task Monitoring & Management

### **Task Execution Tracking**
```kotlin
@Component
class TaskMonitorService {
    
    private val taskMetrics = ConcurrentHashMap<String, TaskMetric>()
    private val logger = LoggerFactory.getLogger(TaskMonitorService::class.java)
    
    fun recordTaskStart(taskName: String) {
        val metric = taskMetrics.getOrPut(taskName) { TaskMetric(taskName) }
        metric.incrementStarted()
        logger.debug("Task started: $taskName")
    }
    
    fun recordTaskCompletion(taskName: String, duration: Long, success: Boolean) {
        val metric = taskMetrics[taskName] ?: return
        metric.recordExecution(duration, success)
        
        if (success) {
            logger.info("Task completed: $taskName in ${duration}ms")
        } else {
            logger.warn("Task failed: $taskName after ${duration}ms")
        }
    }
    
    fun getTaskMetrics(): Map<String, TaskMetric> = taskMetrics.toMap()
    
    fun getTaskStatistics(taskName: String): TaskStatistics? {
        val metric = taskMetrics[taskName] ?: return null
        return TaskStatistics(
            taskName = taskName,
            totalExecutions = metric.totalExecutions,
            successfulExecutions = metric.successfulExecutions,
            failedExecutions = metric.failedExecutions,
            averageDuration = metric.averageDuration,
            lastExecution = metric.lastExecution,
            successRate = metric.successRate
        )
    }
}

data class TaskMetric(
    val name: String,
    var totalExecutions: Long = 0,
    var successfulExecutions: Long = 0,
    var failedExecutions: Long = 0,
    var totalDuration: Long = 0,
    var lastExecution: LocalDateTime? = null
) {
    fun incrementStarted() {
        totalExecutions++
    }
    
    fun recordExecution(duration: Long, success: Boolean) {
        totalDuration += duration
        lastExecution = LocalDateTime.now()
        
        if (success) {
            successfulExecutions++
        } else {
            failedExecutions++
        }
    }
    
    val averageDuration: Double
        get() = if (totalExecutions > 0) totalDuration.toDouble() / totalExecutions else 0.0
    
    val successRate: Double  
        get() = if (totalExecutions > 0) successfulExecutions.toDouble() / totalExecutions else 0.0
}
```

### **Task Health Monitoring**
```kotlin
@Component
class TaskHealthIndicator : HealthIndicator {
    
    @Autowired
    private lateinit var taskMonitorService: TaskMonitorService
    
    @Autowired  
    private lateinit var taskExecutor: ThreadPoolTaskExecutor
    
    override fun health(): Health {
        val builder = Health.Builder()
        
        try {
            // Check thread pool health
            val poolSize = taskExecutor.poolSize
            val activeCount = taskExecutor.activeCount
            val queueSize = taskExecutor.queueSize
            
            builder.withDetail("threadPool", mapOf(
                "poolSize" to poolSize,
                "activeThreads" to activeCount,
                "queuedTasks" to queueSize,
                "utilization" to "${(activeCount.toDouble() / poolSize * 100).toInt()}%"
            ))
            
            // Check recent task failures
            val taskMetrics = taskMonitorService.getTaskMetrics()
            val recentFailures = taskMetrics.values.count { metric ->
                metric.lastExecution?.isAfter(LocalDateTime.now().minusMinutes(15)) == true &&
                metric.successRate < 0.8 // Less than 80% success rate
            }
            
            when {
                recentFailures == 0 && activeCount < poolSize -> 
                    builder.up().withDetail("status", "All tasks running normally")
                
                recentFailures > 0 -> 
                    builder.down().withDetail("issue", "$recentFailures tasks with low success rate")
                    
                activeCount >= poolSize -> 
                    builder.down().withDetail("issue", "Thread pool at maximum capacity")
                    
                else -> 
                    builder.up().withDetail("status", "Operating normally")
            }
            
        } catch (e: Exception) {
            builder.down().withException(e)
        }
        
        return builder.build()
    }
}
```

---

## 📧 Real-World Example: Email Notification System

### **Email Service Implementation**
```kotlin
@Service
class EmailNotificationService {
    
    private val logger = LoggerFactory.getLogger(EmailNotificationService::class.java)
    
    @Autowired
    private lateinit var taskMonitorService: TaskMonitorService
    
    @Async("generalTaskExecutor")
    fun sendWelcomeEmail(user: User): CompletableFuture<EmailResult> {
        val taskName = "send-welcome-email"
        taskMonitorService.recordTaskStart(taskName)
        val startTime = System.currentTimeMillis()
        
        return try {
            logger.info("Sending welcome email to ${user.email}")
            
            // Simulate email service call
            Thread.sleep(1500 + Random.nextInt(1000)) // 1.5-2.5 seconds
            
            val duration = System.currentTimeMillis() - startTime
            taskMonitorService.recordTaskCompletion(taskName, duration, true)
            
            CompletableFuture.completedFuture(EmailResult(
                recipient = user.email,
                subject = "Welcome to our platform!",
                success = true,
                sentAt = LocalDateTime.now()
            ))
            
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            taskMonitorService.recordTaskCompletion(taskName, duration, false)
            logger.error("Failed to send welcome email to ${user.email}", e)
            
            CompletableFuture.completedFuture(EmailResult(
                recipient = user.email,
                subject = "Welcome to our platform!",
                success = false,
                error = e.message,
                sentAt = LocalDateTime.now()
            ))
        }
    }
    
    @Async("generalTaskExecutor")
    fun sendPasswordResetEmail(email: String, resetToken: String): CompletableFuture<EmailResult> {
        val taskName = "send-password-reset"
        taskMonitorService.recordTaskStart(taskName)
        val startTime = System.currentTimeMillis()
        
        return try {
            logger.info("Sending password reset email to $email")
            
            // Build email content
            val emailContent = buildPasswordResetEmail(resetToken)
            
            // Send email (simulate external service)
            Thread.sleep(1200 + Random.nextInt(800))
            
            val duration = System.currentTimeMillis() - startTime
            taskMonitorService.recordTaskCompletion(taskName, duration, true)
            
            CompletableFuture.completedFuture(EmailResult(
                recipient = email,
                subject = "Password Reset Request",
                success = true,
                sentAt = LocalDateTime.now()
            ))
            
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            taskMonitorService.recordTaskCompletion(taskName, duration, false)
            logger.error("Failed to send password reset email to $email", e)
            
            CompletableFuture.completedFuture(EmailResult(
                recipient = email,
                subject = "Password Reset Request", 
                success = false,
                error = e.message,
                sentAt = LocalDateTime.now()
            ))
        }
    }
    
    // Batch email processing
    @Async("heavyTaskExecutor")
    fun sendBulkNotifications(notifications: List<EmailNotification>): CompletableFuture<BulkEmailResult> {
        val taskName = "send-bulk-notifications"
        taskMonitorService.recordTaskStart(taskName)
        val startTime = System.currentTimeMillis()
        
        return CompletableFuture.supplyAsync {
            var successCount = 0
            var failureCount = 0
            val results = mutableListOf<EmailResult>()
            
            notifications.forEach { notification ->
                try {
                    // Simulate individual email sending
                    Thread.sleep(500 + Random.nextInt(300))
                    
                    results.add(EmailResult(
                        recipient = notification.recipient,
                        subject = notification.subject,
                        success = true,
                        sentAt = LocalDateTime.now()
                    ))
                    successCount++
                    
                } catch (e: Exception) {
                    results.add(EmailResult(
                        recipient = notification.recipient,
                        subject = notification.subject,
                        success = false,
                        error = e.message,
                        sentAt = LocalDateTime.now()
                    ))
                    failureCount++
                }
            }
            
            val duration = System.currentTimeMillis() - startTime
            val success = failureCount == 0
            taskMonitorService.recordTaskCompletion(taskName, duration, success)
            
            BulkEmailResult(
                total = notifications.size,
                successful = successCount,
                failed = failureCount,
                results = results,
                duration = duration
            )
        }
    }
}

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

## 🧹 Scheduled Cleanup Tasks

### **Database Cleanup Service**
```kotlin
@Component
class DatabaseCleanupService {
    
    private val logger = LoggerFactory.getLogger(DatabaseCleanupService::class.java)
    
    @Autowired
    private lateinit var auditLogRepository: AuditLogRepository
    
    @Autowired
    private lateinit var sessionRepository: SessionRepository
    
    @Autowired
    private lateinit var taskMonitorService: TaskMonitorService
    
    // Clean up old audit logs daily at 2 AM
    @Scheduled(cron = "0 0 2 * * *")
    fun cleanupOldAuditLogs() {
        val taskName = "cleanup-audit-logs"
        taskMonitorService.recordTaskStart(taskName)
        val startTime = System.currentTimeMillis()
        
        try {
            val cutoffDate = LocalDateTime.now().minusDays(90) // Keep 90 days
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
    
    // Clean up expired sessions every hour
    @Scheduled(fixedRate = 3600000) // 1 hour
    fun cleanupExpiredSessions() {
        val taskName = "cleanup-expired-sessions"
        taskMonitorService.recordTaskStart(taskName)
        val startTime = System.currentTimeMillis()
        
        try {
            val now = LocalDateTime.now()
            val deletedCount = sessionRepository.deleteByExpiresAtBefore(now)
            
            val duration = System.currentTimeMillis() - startTime
            taskMonitorService.recordTaskCompletion(taskName, duration, true)
            
            logger.info("Session cleanup: Deleted $deletedCount expired sessions in ${duration}ms")
            
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            taskMonitorService.recordTaskCompletion(taskName, duration, false)
            logger.error("Failed to cleanup expired sessions", e)
        }
    }
    
    // Generate daily statistics report
    @Scheduled(cron = "0 30 1 * * *") // 1:30 AM daily
    fun generateDailyStats() {
        val taskName = "generate-daily-stats"
        taskMonitorService.recordTaskStart(taskName)
        val startTime = System.currentTimeMillis()
        
        try {
            val yesterday = LocalDate.now().minusDays(1)
            
            val userRegistrations = userRepository.countByCreatedAtBetween(
                yesterday.atStartOfDay(),
                yesterday.atTime(23, 59, 59)
            )
            
            val postsCreated = blogPostRepository.countByCreatedAtBetween(
                yesterday.atStartOfDay(),
                yesterday.atTime(23, 59, 59)
            )
            
            val loginAttempts = auditLogRepository.countByActionAndCreatedAtBetween(
                "LOGIN",
                yesterday.atStartOfDay(),
                yesterday.atTime(23, 59, 59)
            )
            
            // Save daily statistics
            val stats = DailyStatistics(
                date = yesterday,
                userRegistrations = userRegistrations,
                postsCreated = postsCreated,
                loginAttempts = loginAttempts,
                generatedAt = LocalDateTime.now()
            )
            
            dailyStatsRepository.save(stats)
            
            val duration = System.currentTimeMillis() - startTime
            taskMonitorService.recordTaskCompletion(taskName, duration, true)
            
            logger.info("Daily stats generated for $yesterday: " +
                       "$userRegistrations users, $postsCreated posts, $loginAttempts logins")
            
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            taskMonitorService.recordTaskCompletion(taskName, duration, false)
            logger.error("Failed to generate daily statistics", e)
        }
    }
}
```

---

## 🧪 Testing Async Operations

### **Async Testing Strategies**
```kotlin
@SpringBootTest
class AsyncServiceTest {
    
    @Autowired
    private lateinit var emailService: EmailNotificationService
    
    @Autowired
    private lateinit var taskMonitorService: TaskMonitorService
    
    @Test
    fun `should send welcome email asynchronously`() {
        // Given
        val user = User(id = 1, email = "test@example.com", firstName = "Test")
        
        // When
        val future = emailService.sendWelcomeEmail(user)
        
        // Then
        assertThat(future).isNotNull()
        
        // Wait for completion with timeout
        val result = future.get(5, TimeUnit.SECONDS)
        
        assertThat(result.success).isTrue()
        assertThat(result.recipient).isEqualTo("test@example.com")
        assertThat(result.sentAt).isNotNull()
    }
    
    @Test
    fun `should handle email sending failure gracefully`() {
        // Given
        val user = User(id = 1, email = "invalid-email", firstName = "Test")
        
        // When
        val future = emailService.sendWelcomeEmail(user)
        
        // Then - should complete even if email fails
        val result = future.get(5, TimeUnit.SECONDS)
        
        assertThat(result.success).isFalse()
        assertThat(result.error).isNotNull()
    }
    
    @Test
    fun `should process bulk emails efficiently`() {
        // Given
        val notifications = (1..10).map { i ->
            EmailNotification(
                recipient = "user$i@example.com",
                subject = "Test Notification",
                content = "Test content"
            )
        }
        
        // When
        val startTime = System.currentTimeMillis()
        val future = emailService.sendBulkNotifications(notifications)
        val result = future.get(30, TimeUnit.SECONDS)
        val totalTime = System.currentTimeMillis() - startTime
        
        // Then
        assertThat(result.total).isEqualTo(10)
        assertThat(result.successful + result.failed).isEqualTo(10)
        assertThat(totalTime).isLessThan(15000) // Should be faster than sequential processing
        
        logger.info("Bulk email processing: ${result.successful}/${result.total} successful in ${totalTime}ms")
    }
}

@SpringBootTest
class ScheduledTaskTest {
    
    @Autowired
    private lateinit var databaseCleanupService: DatabaseCleanupService
    
    @Test
    fun `should cleanup old audit logs`() {
        // Given - create some old audit logs
        val oldDate = LocalDateTime.now().minusDays(100)
        auditLogRepository.save(AuditLog(action = "TEST", createdAt = oldDate))
        
        val recentDate = LocalDateTime.now().minusDays(30)
        auditLogRepository.save(AuditLog(action = "TEST", createdAt = recentDate))
        
        // When
        databaseCleanupService.cleanupOldAuditLogs()
        
        // Then
        val remainingLogs = auditLogRepository.findAll()
        assertThat(remainingLogs).hasSize(1) // Only recent log should remain
        assertThat(remainingLogs[0].createdAt).isAfter(LocalDateTime.now().minusDays(90))
    }
}
```

---

## 🎯 Best Practices

### **Do's**
- ✅ **Use appropriate thread pools** for different types of tasks
- ✅ **Monitor task execution** with comprehensive logging and metrics
- ✅ **Handle failures gracefully** with proper exception handling
- ✅ **Set timeouts** for async operations to prevent hanging
- ✅ **Use CompletableFuture** for operations that need result tracking
- ✅ **Configure proper cron expressions** for scheduled tasks

### **Don'ts**
- ❌ **Don't block async methods** with synchronous operations
- ❌ **Don't ignore task failures** - always log and monitor
- ❌ **Don't use unlimited thread pools** - set reasonable limits
- ❌ **Don't run heavy tasks on the main thread pool**
- ❌ **Don't forget to test async operations** with proper timeouts
- ❌ **Don't schedule too many frequent tasks** - consider resource impact

### **Performance Tips**
- 🚀 **Tune thread pool sizes** based on your application's needs
- 🚀 **Use fixed delay for dependent tasks** to avoid overlapping
- 🚀 **Batch operations** when possible for better efficiency
- 🚀 **Monitor queue sizes** to detect bottlenecks
- 🚀 **Use appropriate task executors** for different workload types

---

## 🚀 Real-World Integration Patterns

### **Event-Driven Architecture**
```kotlin
@Component
class UserEventHandler {
    
    @EventListener
    @Async("fastTaskExecutor")
    fun handleUserRegistration(event: UserRegisteredEvent) {
        // Quick async processing of user registration
        emailService.sendWelcomeEmailAsync(event.user)
        userAnalyticsService.trackRegistrationAsync(event.user)
    }
    
    @EventListener
    @Async("generalTaskExecutor")
    fun handlePostPublished(event: PostPublishedEvent) {
        // Background processing for new posts
        searchIndexService.indexPostAsync(event.post)
        notificationService.notifyFollowersAsync(event.post)
    }
}
```

### **Circuit Breaker Pattern**
```kotlin
@Service
class ResilientEmailService {
    
    private val circuitBreaker = CircuitBreaker.ofDefaults("email-service")
    
    @Async("generalTaskExecutor")
    fun sendEmailWithCircuitBreaker(email: Email): CompletableFuture<EmailResult> {
        return CompletableFuture.supplyAsync {
            circuitBreaker.executeSupplier {
                sendEmailInternal(email)
            }
        }
    }
}
```

---

## 🎓 Summary

Scheduled Tasks & Async Processing provides:

- **🚀 Performance**: Non-blocking operations for better user experience
- **⏰ Automation**: Scheduled maintenance and cleanup tasks
- **📊 Scalability**: Efficient resource utilization with thread pools
- **🔧 Monitoring**: Comprehensive task tracking and health checks
- **🛡️ Resilience**: Proper error handling and failure recovery

**Key Takeaways**:
1. **Strategic Async Usage**: Use async for I/O operations and background tasks
2. **Proper Thread Pools**: Configure different pools for different workloads
3. **Cron Expressions**: Schedule tasks at optimal times for minimal impact
4. **Task Monitoring**: Track execution metrics for performance insights
5. **Error Handling**: Always handle failures gracefully with proper logging

Next lesson: **File Handling & Uploads** for managing file operations and storage!