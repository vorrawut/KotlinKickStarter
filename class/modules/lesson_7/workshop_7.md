# 🏗️ Lesson 7 Workshop: Service Layer & Clean Architecture

## 🎯 What You'll Build

A **Task Management System** that demonstrates clean architecture principles with:
- **Layered architecture** with proper separation of concerns
- **Service layer** with business logic and transaction management
- **Repository interfaces** for data access abstraction
- **Dependency inversion** with interface-driven design

**Expected Duration**: 45-60 minutes

---

## 🚀 Getting Started

### Prerequisites
- Completed Lesson 6 (Request Validation & Error Handling)
- Understanding of Spring Boot dependency injection
- Familiarity with JPA and databases

### Project Setup
```bash
cd class/workshop/lesson_7
./gradlew build
```

---

## 📋 Workshop Tasks Overview

| Step | Task | Duration | Key Learning |
|------|------|----------|-------------|
| 1 | Application Setup | 5 min | Clean architecture project structure |
| 2 | Domain Models | 10 min | Rich domain entities with business logic |
| 3 | Repository Interfaces | 10 min | Data access abstraction patterns |
| 4 | Service Layer | 15 min | Business logic and transaction management |
| 5 | Controller Layer | 10 min | API integration with service layer |
| 6 | Configuration | 5 min | Dependency injection setup |
| 7 | Testing | 10 min | Testing clean architecture layers |

---

## 🔧 Step 1: Application Setup (5 minutes)

### Task 1.1: Complete the Main Application Class

Open `src/main/kotlin/com/learning/architecture/ArchitectureApplication.kt`

**TODO**: Add the missing annotations and implementation:

```kotlin
@SpringBootApplication
class ArchitectureApplication

fun main(args: Array<String>) {
    runApplication<ArchitectureApplication>(*args)
}
```

**Key Points**:
- `@SpringBootApplication` enables auto-configuration for clean architecture
- JPA and transaction management are automatically configured

### Task 1.2: Verify Database Configuration

Check `src/main/resources/application.yml` - the H2 database is configured for development.

Run the application to ensure it starts:
```bash
./gradlew bootRun
```

You should see H2 console available at http://localhost:8080/h2-console

---

## 🏗️ Step 2: Complete Domain Models (10 minutes)

### Task 2.1: Complete Task Domain Model

Open `src/main/kotlin/com/learning/architecture/model/Task.kt`

**TODO**: Add JPA annotations and implement business logic methods:

```kotlin
@Entity
@Table(name = "tasks")
data class Task(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, length = 100)
    val title: String,
    
    @Column(nullable = false, length = 1000)
    val description: String,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: TaskStatus,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val priority: TaskPriority,
    
    @Embedded
    val assignee: Assignee?,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "due_date")
    val dueDate: LocalDateTime?
) {
    
    fun isOverdue(): Boolean {
        return dueDate?.isBefore(LocalDateTime.now()) == true && 
               status != TaskStatus.COMPLETED
    }
    
    fun canBeAssignedTo(assignee: Assignee): Boolean {
        return status in listOf(TaskStatus.CREATED, TaskStatus.IN_PROGRESS) &&
               assignee.isValid()
    }
    
    fun canTransitionTo(newStatus: TaskStatus): Boolean {
        return when (status) {
            TaskStatus.CREATED -> newStatus in listOf(TaskStatus.IN_PROGRESS, TaskStatus.CANCELLED)
            TaskStatus.IN_PROGRESS -> newStatus in listOf(TaskStatus.IN_REVIEW, TaskStatus.COMPLETED, TaskStatus.CANCELLED)
            TaskStatus.IN_REVIEW -> newStatus in listOf(TaskStatus.IN_PROGRESS, TaskStatus.COMPLETED)
            TaskStatus.COMPLETED -> false
            TaskStatus.CANCELLED -> false
        }
    }
    
    fun isHighPriority(): Boolean {
        return priority in listOf(TaskPriority.HIGH, TaskPriority.CRITICAL)
    }
    
    fun getDaysUntilDue(): Long? {
        return dueDate?.let { 
            ChronoUnit.DAYS.between(LocalDateTime.now(), it) 
        }
    }
    
    fun getEstimatedEffort(): Int {
        return when (priority) {
            TaskPriority.LOW -> 2
            TaskPriority.MEDIUM -> 4
            TaskPriority.HIGH -> 8
            TaskPriority.CRITICAL -> 12
        }
    }
    
    companion object {
        fun createNew(title: String, description: String, priority: TaskPriority): Task {
            return Task(
                title = title,
                description = description,
                status = TaskStatus.CREATED,
                priority = priority,
                assignee = null,
                dueDate = null
            )
        }
        
        fun createWithDueDate(title: String, description: String, priority: TaskPriority, dueDate: LocalDateTime): Task {
            return Task(
                title = title,
                description = description,
                status = TaskStatus.CREATED,
                priority = priority,
                assignee = null,
                dueDate = dueDate
            )
        }
    }
}
```

### Task 2.2: Complete Enums and Value Objects

Complete the enums and value objects:

```kotlin
enum class TaskStatus(val displayName: String, val isTerminal: Boolean) {
    CREATED("Created", false),
    IN_PROGRESS("In Progress", false),
    IN_REVIEW("In Review", false),
    COMPLETED("Completed", true),
    CANCELLED("Cancelled", true)
}

enum class TaskPriority(val displayName: String, val level: Int) {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3),
    CRITICAL("Critical", 4)
}

@Embeddable
data class Assignee(
    @Column(name = "assignee_user_id")
    val userId: String,
    
    @Column(name = "assignee_name")
    val name: String,
    
    @Column(name = "assignee_email")
    val email: String,
    
    @Column(name = "assignee_department")
    val department: String
) {
    fun isValid(): Boolean {
        return userId.isNotBlank() && 
               name.isNotBlank() && 
               email.contains("@") &&
               department.isNotBlank()
    }
    
    fun canHandlePriority(priority: TaskPriority): Boolean {
        // Simple business rule: all assignees can handle any priority
        return true
    }
}
```

---

## 📊 Step 3: Complete Repository Interfaces (10 minutes)

### Task 3.1: Complete Task Repository

Open `src/main/kotlin/com/learning/architecture/repository/TaskRepository.kt`

**TODO**: Add the `@Repository` annotation and implement key query methods:

```kotlin
@Repository
interface TaskRepository : JpaRepository<Task, Long> {
    
    fun findByStatus(status: TaskStatus): List<Task>
    
    fun findByPriority(priority: TaskPriority): List<Task>
    
    fun findByAssigneeUserId(userId: String): List<Task>
    
    fun findByProjectId(projectId: Long): List<Task>
    
    @Query("SELECT t FROM Task t WHERE t.dueDate < :currentDate AND t.status != 'COMPLETED'")
    fun findOverdueTasks(@Param("currentDate") currentDate: LocalDateTime = LocalDateTime.now()): List<Task>
    
    @Query("SELECT t FROM Task t WHERE t.dueDate BETWEEN :currentDate AND :endDate AND t.status != 'COMPLETED'")
    fun findTasksDueWithinDays(
        @Param("currentDate") currentDate: LocalDateTime = LocalDateTime.now(),
        @Param("endDate") endDate: LocalDateTime
    ): List<Task>
    
    @Query("SELECT t FROM Task t WHERE t.priority IN ('HIGH', 'CRITICAL') AND t.status != 'COMPLETED'")
    fun findHighPriorityTasks(): List<Task>
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = :status")
    fun countByStatus(@Param("status") status: TaskStatus): Long
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignee.userId = :userId AND t.status != 'COMPLETED'")
    fun countActiveTasksByAssignee(@Param("userId") userId: String): Long
}
```

### Task 3.2: Understand Repository Pattern

The repository pattern provides:
- **Abstraction** over data access
- **Testability** with easy mocking
- **Flexibility** to change data storage
- **Query encapsulation** in domain terms

---

## 💼 Step 4: Complete Service Layer (15 minutes)

### Task 4.1: Complete Task Service Implementation

Open `src/main/kotlin/com/learning/architecture/service/TaskServiceImpl.kt`

**TODO**: Add annotations and implement core methods:

```kotlin
@Service
@Transactional
class TaskServiceImpl(
    private val taskRepository: TaskRepository,
    private val taskValidationService: TaskValidationService,
    private val taskNotificationService: TaskNotificationService
) : TaskService {
    
    private val logger = LoggerFactory.getLogger(TaskServiceImpl::class.java)
    
    override fun createTask(request: CreateTaskRequest): TaskResponse {
        logger.info("Creating task: {}", request.title)
        
        // 1. Validate business rules
        val validationResult = taskValidationService.validateTaskCreation(request)
        if (!validationResult.isValid) {
            throw ValidationException("Task creation validation failed", validationResult.errors)
        }
        
        // 2. Create domain entity
        val task = if (request.dueDate != null) {
            Task.createWithDueDate(request.title, request.description, request.priority, request.dueDate)
        } else {
            Task.createNew(request.title, request.description, request.priority)
        }
        
        // 3. Save to repository
        val savedTask = taskRepository.save(task)
        
        // 4. Log operation
        logTaskOperation("CREATE", savedTask.id!!, mapOf("title" to savedTask.title))
        
        // 5. Return response
        return mapToTaskResponse(savedTask)
    }
    
    @Transactional(readOnly = true)
    override fun getTaskById(id: Long): TaskResponse {
        val task = findTaskOrThrow(id)
        return mapToTaskResponse(task)
    }
    
    override fun updateTask(id: Long, request: UpdateTaskRequest): TaskResponse {
        logger.info("Updating task: {}", id)
        
        val existingTask = findTaskOrThrow(id)
        val updatedTask = updateTaskFromRequest(existingTask, request)
        
        validateBusinessRules(updatedTask, "UPDATE")
        
        val savedTask = taskRepository.save(updatedTask)
        logTaskOperation("UPDATE", id, mapOf("changes" to request.toString()))
        
        return mapToTaskResponse(savedTask)
    }
    
    override fun deleteTask(id: Long) {
        logger.info("Deleting task: {}", id)
        
        val task = findTaskOrThrow(id)
        
        // Business rule: Can't delete completed tasks
        if (task.status == TaskStatus.COMPLETED) {
            throw BusinessRuleViolationException("Cannot delete completed tasks")
        }
        
        taskRepository.delete(task)
        logTaskOperation("DELETE", id, emptyMap())
    }
    
    @Transactional(readOnly = true)
    override fun getAllTasks(pageable: Pageable): Page<TaskResponse> {
        return taskRepository.findAll(pageable).map { mapToTaskResponse(it) }
    }
    
    override fun assignTask(taskId: Long, request: AssignTaskRequest): TaskResponse {
        val task = findTaskOrThrow(taskId)
        val assignee = Assignee(request.assigneeId, "User Name", "user@example.com", "Development")
        
        if (!task.canBeAssignedTo(assignee)) {
            throw BusinessRuleViolationException("Task cannot be assigned in current state")
        }
        
        val updatedTask = task.copy(
            assignee = assignee,
            dueDate = request.newDueDate ?: task.dueDate,
            status = TaskStatus.IN_PROGRESS
        )
        
        val savedTask = taskRepository.save(updatedTask)
        
        // Trigger notification
        triggerNotifications(savedTask, "ASSIGN")
        
        return mapToTaskResponse(savedTask)
    }
    
    @Transactional(readOnly = true)
    override fun getTaskSummary(): TaskSummaryResponse {
        val allTasks = taskRepository.findAll()
        
        return TaskSummaryResponse(
            totalTasks = allTasks.size,
            tasksByStatus = TaskStatus.values().associateWith { status ->
                allTasks.count { it.status == status }
            },
            tasksByPriority = TaskPriority.values().associateWith { priority ->
                allTasks.count { it.priority == priority }
            },
            overdueTasks = allTasks.count { it.isOverdue() },
            averageCompletionTime = 5.5, // Mock calculation
            upcomingDueTasks = allTasks
                .filter { it.dueDate?.isBefore(LocalDateTime.now().plusDays(7)) == true }
                .map { mapToTaskResponse(it) }
        )
    }
    
    // Helper methods
    
    private fun mapToTaskResponse(task: Task): TaskResponse {
        return TaskResponse(
            id = task.id!!,
            title = task.title,
            description = task.description,
            status = task.status,
            priority = task.priority,
            assignee = task.assignee?.let { 
                AssigneeResponse(it.userId, it.name, it.email, it.department) 
            },
            metadata = null,
            projectId = null,
            createdAt = task.createdAt.toString(),
            updatedAt = task.updatedAt.toString(),
            dueDate = task.dueDate?.toString(),
            isOverdue = task.isOverdue(),
            daysUntilDue = task.getDaysUntilDue(),
            estimatedEffort = task.getEstimatedEffort()
        )
    }
    
    private fun updateTaskFromRequest(task: Task, request: UpdateTaskRequest): Task {
        return task.copy(
            title = request.title ?: task.title,
            description = request.description ?: task.description,
            priority = request.priority ?: task.priority,
            status = request.status ?: task.status,
            dueDate = request.dueDate ?: task.dueDate,
            updatedAt = LocalDateTime.now()
        )
    }
    
    private fun findTaskOrThrow(id: Long): Task {
        return taskRepository.findById(id).orElseThrow {
            ResourceNotFoundException("Task not found with id: $id")
        }
    }
    
    private fun validateBusinessRules(task: Task, operation: String) {
        // Add business rule validation as needed
        logger.debug("Validating business rules for {} operation on task {}", operation, task.id)
    }
    
    private fun logTaskOperation(operation: String, taskId: Long, details: Map<String, Any>) {
        logger.info("Task operation: {} for task: {} with details: {}", operation, taskId, details)
    }
    
    private fun triggerNotifications(task: Task, operation: String, additionalData: Map<String, Any> = emptyMap()) {
        try {
            taskNotificationService.notifyTaskAssignment(task, task.assignee?.userId ?: "")
        } catch (e: Exception) {
            logger.warn("Failed to send notification for task {}: {}", task.id, e.message)
        }
    }
}
```

### Task 4.2: Create Exception Classes

Create custom exceptions in `src/main/kotlin/com/learning/architecture/exception/`:

```kotlin
// BusinessRuleViolationException.kt
class BusinessRuleViolationException(message: String) : RuntimeException(message)

// ResourceNotFoundException.kt  
class ResourceNotFoundException(message: String) : RuntimeException(message)

// ValidationException.kt
class ValidationException(
    message: String, 
    val errors: List<ValidationError>
) : RuntimeException(message)
```

---

## 🌐 Step 5: Complete Controller Layer (10 minutes)

### Task 5.1: Complete Task Controller

Open `src/main/kotlin/com/learning/architecture/controller/TaskController.kt`

**TODO**: Add annotations and implement endpoints:

```kotlin
@RestController
@RequestMapping("/api/tasks")
class TaskController(
    private val taskService: TaskService
) {
    
    private val logger = LoggerFactory.getLogger(TaskController::class.java)
    
    @PostMapping
    fun createTask(
        @Valid @RequestBody request: CreateTaskRequest
    ): ResponseEntity<TaskResponse> {
        logRequest("CREATE_TASK", request)
        val startTime = System.currentTimeMillis()
        
        val response = taskService.createTask(request)
        
        logResponse("CREATE_TASK", response, System.currentTimeMillis() - startTime)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
    
    @GetMapping("/{id}")
    fun getTask(
        @PathVariable id: Long
    ): ResponseEntity<TaskResponse> {
        val response = taskService.getTaskById(id)
        return ResponseEntity.ok(response)
    }
    
    @PutMapping("/{id}")
    fun updateTask(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateTaskRequest
    ): ResponseEntity<TaskResponse> {
        val response = taskService.updateTask(id, request)
        return ResponseEntity.ok(response)
    }
    
    @DeleteMapping("/{id}")
    fun deleteTask(
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        taskService.deleteTask(id)
        return ResponseEntity.noContent().build()
    }
    
    @GetMapping
    fun getAllTasks(
        pageable: Pageable
    ): ResponseEntity<Page<TaskResponse>> {
        val response = taskService.getAllTasks(pageable)
        return ResponseEntity.ok(response)
    }
    
    @PostMapping("/{id}/assign")
    fun assignTask(
        @PathVariable id: Long,
        @Valid @RequestBody request: AssignTaskRequest
    ): ResponseEntity<TaskResponse> {
        val response = taskService.assignTask(id, request)
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/summary")
    fun getTaskSummary(): ResponseEntity<TaskSummaryResponse> {
        val response = taskService.getTaskSummary()
        return ResponseEntity.ok(response)
    }
    
    private fun logRequest(operation: String, request: Any?) {
        logger.info("Incoming request: {} with payload: {}", operation, request.toString())
    }
    
    private fun logResponse(operation: String, response: Any, duration: Long) {
        logger.info("Outgoing response: {} completed in {}ms", operation, duration)
    }
}
```

---

## ⚙️ Step 6: Complete Configuration (5 minutes)

### Task 6.1: Complete Architecture Configuration

Open `src/main/kotlin/com/learning/architecture/config/ArchitectureConfiguration.kt`

**TODO**: Add configuration annotations:

```kotlin
@Configuration
@EnableTransactionManagement
class ArchitectureConfiguration {
    
    @Bean
    @ConfigurationProperties(prefix = "architecture")
    fun architectureProperties(): ArchitectureProperties {
        return ArchitectureProperties()
    }
    
    @Bean
    @Profile("dev")
    fun developmentConfiguration(): DevelopmentConfig {
        return DevelopmentConfig()
    }
    
    @Bean
    @Profile("prod")
    fun productionConfiguration(): ProductionConfig {
        return ProductionConfig()
    }
}
```

### Task 6.2: Create Mock Service Implementations

Create simple implementations for validation and notification services:

```kotlin
// TaskValidationServiceImpl.kt
@Service
class TaskValidationServiceImpl : TaskValidationService {
    override fun validateTaskCreation(request: CreateTaskRequest): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        if (request.title.length < 3) {
            errors.add(ValidationError("title", "Title must be at least 3 characters", "TITLE_TOO_SHORT"))
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }
    
    // Implement other methods with simple validation logic
}

// TaskNotificationServiceImpl.kt
@Service  
class TaskNotificationServiceImpl : TaskNotificationService {
    private val logger = LoggerFactory.getLogger(TaskNotificationServiceImpl::class.java)
    
    override fun notifyTaskAssignment(task: Task, assigneeId: String) {
        logger.info("Notification: Task '{}' assigned to user '{}'", task.title, assigneeId)
    }
    
    // Implement other notification methods with logging
}
```

---

## 🧪 Step 7: Test Your Implementation (10 minutes)

### Task 7.1: Complete Application Tests

Open `src/test/kotlin/com/learning/architecture/ArchitectureApplicationTests.kt`

**TODO**: Implement basic tests:

```kotlin
@SpringBootTest
@ActiveProfiles("test")
class ArchitectureApplicationTests {

    @Test
    fun contextLoads() {
        // Test passes if Spring context loads successfully
    }
    
    @Autowired
    private lateinit var taskService: TaskService
    
    @Test
    fun serviceLayerIntegrationTest() {
        // Test service layer integration
        val request = CreateTaskRequest(
            title = "Test Task",
            description = "Test Description", 
            priority = TaskPriority.MEDIUM,
            dueDate = null,
            assignee = null,
            metadata = null
        )
        
        val response = taskService.createTask(request)
        
        assertThat(response.title).isEqualTo("Test Task")
        assertThat(response.status).isEqualTo(TaskStatus.CREATED)
    }
}
```

### Task 7.2: Run Tests

```bash
./gradlew test
```

### Task 7.3: Test the API

Start the application:
```bash
./gradlew bootRun
```

**Create a task**:
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Implement clean architecture",
    "description": "Build a task management system with clean architecture",
    "priority": "HIGH"
  }'
```

**Get task summary**:
```bash
curl http://localhost:8080/api/tasks/summary
```

---

## 🎯 Verification & Testing

### Success Criteria

✅ **Application Starts**: Clean startup with no errors  
✅ **Service Layer**: Business logic properly encapsulated  
✅ **Repository Pattern**: Data access abstracted through interfaces  
✅ **Transaction Management**: Operations are properly transactional  
✅ **Dependency Injection**: All dependencies properly injected  
✅ **API Integration**: Controllers properly delegate to services  

### Test Scenarios

1. **Task Creation**: Should create task with validation
2. **Task Assignment**: Should assign task and send notification
3. **Business Rules**: Should enforce task status transitions
4. **Error Handling**: Should handle validation and business rule violations
5. **Transaction Rollback**: Should rollback on exceptions

---

## 🏆 What You've Accomplished

### Technical Skills Gained

- ✅ **Clean Architecture**: Proper layer separation and dependency inversion
- ✅ **Service Layer**: Business logic encapsulation and transaction management
- ✅ **Repository Pattern**: Data access abstraction and testability
- ✅ **Domain Modeling**: Rich domain entities with business behavior
- ✅ **Configuration**: Dependency injection and profile-based configuration

### Architecture Patterns Learned

- 🏗️ **Layered Architecture**: Clear separation between presentation, service, and data layers
- 🔄 **Dependency Inversion**: Services depend on interfaces, not implementations
- 💼 **Service Layer**: Centralized business logic and transaction boundaries
- 📊 **Repository Pattern**: Data access abstraction and query encapsulation
- 🎯 **Domain-Driven Design**: Rich domain models with business behavior

---

## 🚀 Next Steps

Ready for **Lesson 8: Persistence with Spring Data JPA**? You'll build on this clean architecture foundation to add sophisticated data access patterns, relationships, and query optimization.

**Optional Enhancements**:
- Add more complex business rules and validation
- Implement audit logging with JPA auditing
- Add caching with Spring Cache abstraction
- Create additional service compositions

---

*Excellent work! You've built a clean, maintainable architecture that follows industry best practices. These patterns will serve as the foundation for all your future Spring Boot applications.*