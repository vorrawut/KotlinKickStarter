# 🛠️ Workshop 8: Implementing Dual Database Persistence

## 🎯 Workshop Objectives

In this hands-on workshop, you will:
1. Configure Spring Data JPA with H2 (embedded) and PostgreSQL
2. Configure Spring Data MongoDB with embedded MongoDB
3. Create entities and documents for the same domain
4. Implement repository patterns for both databases
5. Build a service layer that uses both databases
6. Create REST controllers with dual persistence
7. Write comprehensive tests for both database technologies
8. Compare performance and use cases

## 🏗️ Project Structure

```
src/main/kotlin/com/learning/persistence/
├── PersistenceApplication.kt
├── config/
│   ├── DatabaseConfiguration.kt
│   ├── JpaConfiguration.kt
│   └── MongoConfiguration.kt
├── model/
│   ├── sql/           # JPA Entities
│   │   ├── Task.kt
│   │   ├── Project.kt
│   │   └── User.kt
│   └── mongo/         # MongoDB Documents
│       ├── TaskDocument.kt
│       ├── ProjectDocument.kt
│       └── UserDocument.kt
├── repository/
│   ├── jpa/           # JPA Repositories
│   │   ├── TaskRepository.kt
│   │   ├── ProjectRepository.kt
│   │   └── UserRepository.kt
│   └── mongo/         # MongoDB Repositories
│       ├── TaskDocumentRepository.kt
│       ├── ProjectDocumentRepository.kt
│       └── UserDocumentRepository.kt
├── service/
│   ├── TaskService.kt
│   ├── ProjectService.kt
│   ├── UserService.kt
│   └── SyncService.kt
├── dto/
│   ├── TaskDTOs.kt
│   ├── ProjectDTOs.kt
│   └── UserDTOs.kt
└── controller/
    ├── TaskController.kt
    ├── ProjectController.kt
    └── DataController.kt
```

---

## 📋 Step 1: Project Setup and Dependencies

### 1.1 Create build.gradle.kts

Create the Gradle build file with all necessary dependencies:

```kotlin
// TODO: Add Spring Boot parent and dependencies for:
// - Spring Boot Web
// - Spring Data JPA  
// - Spring Data MongoDB
// - H2 Database (for testing)
// - PostgreSQL driver
// - Embedded MongoDB (for testing)
// - Kotlin Jackson module
// - Validation
// - Testing dependencies

plugins {
    // TODO: Add Kotlin and Spring Boot plugins
}

dependencies {
    // TODO: Add all required dependencies
    // Hint: You'll need spring-boot-starter-data-jpa, spring-boot-starter-data-mongodb
}
```

### 1.2 Create settings.gradle.kts

```kotlin
// TODO: Set the project name
rootProject.name = "lesson-8-persistence"
```

---

## 📋 Step 2: Database Configuration

### 2.1 Create application.yml

Configure both SQL and MongoDB databases:

```yaml
# TODO: Create configuration for:
# - H2 database (for development/testing)
# - PostgreSQL (for production)
# - MongoDB (local and embedded for testing)
# - JPA settings (ddl-auto, show-sql, etc.)
# - MongoDB settings (auto-index-creation, etc.)

spring:
  profiles:
    active: dev
    
  # TODO: Configure H2 for development
  datasource:
    # Add H2 configuration here
    
  # TODO: Configure JPA/Hibernate
  jpa:
    # Add JPA configuration here
    
  # TODO: Configure MongoDB
  data:
    mongodb:
      # Add MongoDB configuration here

---
# Production profile with PostgreSQL
spring:
  config:
    activate:
      on-profile: prod
      
  # TODO: Configure PostgreSQL
  datasource:
    # Add PostgreSQL configuration
```

### 2.2 Create DatabaseConfiguration.kt

```kotlin
// TODO: Create configuration class that enables both JPA and MongoDB repositories
// Hints:
// - Use @EnableJpaRepositories with basePackages for JPA
// - Use @EnableMongoRepositories with basePackages for MongoDB
// - Configure separate entity manager factories if needed

@Configuration
// TODO: Add @EnableJpaRepositories annotation
// TODO: Add @EnableMongoRepositories annotation
class DatabaseConfiguration {
    
    // TODO: Configure any additional beans needed for database setup
}
```

---

## 📋 Step 3: Create Domain Models

### 3.1 Create SQL Entity - Task.kt

```kotlin
// TODO: Create JPA entity for Task with following properties:
// - id (Long, auto-generated)
// - title (String, not null)
// - description (String)
// - status (TaskStatus enum)
// - priority (TaskPriority enum)  
// - assigneeId (String, nullable)
// - projectId (Long, nullable)
// - createdAt (LocalDateTime)
// - updatedAt (LocalDateTime)
// - dueDate (LocalDateTime, nullable)

// TODO: Add proper JPA annotations (@Entity, @Table, @Id, etc.)
// TODO: Add indexes for frequently queried fields
// TODO: Add validation annotations

@Entity
@Table(name = "tasks")
data class Task(
    // TODO: Implement all properties with proper annotations
)

// TODO: Create TaskStatus enum
enum class TaskStatus {
    // TODO: Add status values (CREATED, IN_PROGRESS, COMPLETED, CANCELLED)
}

// TODO: Create TaskPriority enum  
enum class TaskPriority {
    // TODO: Add priority values (LOW, MEDIUM, HIGH, CRITICAL)
}
```

### 3.2 Create MongoDB Document - TaskDocument.kt

```kotlin
// TODO: Create MongoDB document for Task with following properties:
// - id (String)
// - title (String, indexed)
// - description (String, text indexed for search)
// - status (String)
// - priority (Int)
// - assignee (embedded AssigneeInfo object)
// - projectInfo (embedded ProjectInfo object)
// - metadata (embedded TaskMetadata object)
// - tags (List<String>, indexed)
// - createdAt (LocalDateTime)
// - updatedAt (LocalDateTime)

// TODO: Add proper MongoDB annotations (@Document, @Id, @Indexed, etc.)
// TODO: Add compound indexes for efficient queries
// TODO: Add text indexes for search functionality

@Document(collection = "tasks")
data class TaskDocument(
    // TODO: Implement all properties with proper annotations
)

// TODO: Create embedded objects for document structure
data class AssigneeInfo(
    // TODO: Add assignee properties
)

data class ProjectInfo(
    // TODO: Add project properties  
)

data class TaskMetadata(
    // TODO: Add metadata properties
)
```

### 3.3 Create Additional Entities

Follow the same pattern to create:
- **User** (SQL) and **UserDocument** (MongoDB)
- **Project** (SQL) and **ProjectDocument** (MongoDB)

```kotlin
// TODO: Create User entity with JPA annotations
@Entity
@Table(name = "users")
data class User(
    // TODO: Add user properties (id, username, email, name, etc.)
)

// TODO: Create UserDocument with MongoDB annotations
@Document(collection = "users")
data class UserDocument(
    // TODO: Add user document properties with flexible schema
)

// TODO: Create Project entity
@Entity
@Table(name = "projects")
data class Project(
    // TODO: Add project properties with relationships to tasks
)

// TODO: Create ProjectDocument
@Document(collection = "projects")
data class ProjectDocument(
    // TODO: Add project document properties with embedded task info
)
```

---

## 📋 Step 4: Create Repository Interfaces

### 4.1 Create JPA Repositories

```kotlin
// TODO: Create TaskRepository extending JpaRepository
interface TaskRepository : JpaRepository<Task, Long> {
    
    // TODO: Add method name queries:
    // - Find tasks by status
    // - Find tasks by assignee ID
    // - Find tasks by project ID and status
    // - Find overdue tasks (due date before now)
    
    // TODO: Add custom JPQL queries:
    // - Find tasks in date range
    // - Find tasks by priority greater than specified value
    // - Count tasks by status for a project
    
    // TODO: Add native SQL queries if needed for complex operations
}

// TODO: Create UserRepository
interface UserRepository : JpaRepository<User, Long> {
    // TODO: Add queries for finding users by username, email, etc.
}

// TODO: Create ProjectRepository  
interface ProjectRepository : JpaRepository<Project, Long> {
    // TODO: Add queries for finding projects by owner, status, etc.
}
```

### 4.2 Create MongoDB Repositories

```kotlin
// TODO: Create TaskDocumentRepository extending MongoRepository
interface TaskDocumentRepository : MongoRepository<TaskDocument, String> {
    
    // TODO: Add method name queries:
    // - Find tasks by status
    // - Find tasks by tags containing specific values
    // - Find tasks by assignee user ID
    
    // TODO: Add JSON-based queries using @Query:
    // - Find tasks with priority greater than specified value
    // - Find tasks created in date range
    // - Find tasks with specific metadata properties
    
    // TODO: Add aggregation pipelines using @Aggregation:
    // - Group tasks by status and count
    // - Calculate average completion time by priority
    // - Find most active assignees
}

// TODO: Create UserDocumentRepository
interface UserDocumentRepository : MongoRepository<UserDocument, String> {
    // TODO: Add MongoDB-specific queries for flexible user data
}

// TODO: Create ProjectDocumentRepository
interface ProjectDocumentRepository : MongoRepository<ProjectDocument, String> {
    // TODO: Add MongoDB-specific queries for project analytics
}
```

---

## 📋 Step 5: Create DTOs

### 5.1 Create Task DTOs

```kotlin
// TODO: Create DTOs for Task operations:

// Request DTOs
data class CreateTaskRequest(
    // TODO: Add validation annotations and properties
)

data class UpdateTaskRequest(
    // TODO: Add optional properties for partial updates
)

// Response DTOs
data class TaskResponse(
    // TODO: Add properties that combine data from both SQL and MongoDB
)

data class TaskSummaryResponse(
    // TODO: Add summary properties for dashboard views
)

// Search DTOs
data class TaskSearchRequest(
    // TODO: Add search criteria that work with both databases
)
```

---

## 📋 Step 6: Implement Service Layer

### 6.1 Create TaskService

```kotlin
// TODO: Create service that uses both SQL and MongoDB repositories
@Service
@Transactional
class TaskService(
    // TODO: Inject both JPA and MongoDB repositories
    private val taskRepository: TaskRepository,
    private val taskDocumentRepository: TaskDocumentRepository,
    // TODO: Add other required dependencies
) {
    
    // TODO: Implement createTask method that:
    // 1. Saves core task data to SQL database
    // 2. Saves flexible metadata to MongoDB
    // 3. Handles transaction management
    fun createTask(request: CreateTaskRequest): TaskResponse {
        // TODO: Implement dual database save operation
    }
    
    // TODO: Implement updateTask method
    fun updateTask(id: Long, request: UpdateTaskRequest): TaskResponse {
        // TODO: Update both SQL entity and MongoDB document
    }
    
    // TODO: Implement findTask method that combines data from both databases
    fun findTask(id: Long): TaskResponse? {
        // TODO: Fetch from SQL and merge with MongoDB data
    }
    
    // TODO: Implement searchTasks using MongoDB for flexible search
    fun searchTasks(request: TaskSearchRequest): List<TaskResponse> {
        // TODO: Use MongoDB for complex search, then fetch SQL data
    }
    
    // TODO: Implement deleteTask
    fun deleteTask(id: Long): Boolean {
        // TODO: Delete from both databases with proper error handling
    }
    
    // TODO: Add methods that demonstrate database-specific advantages:
    // - getTaskAnalytics() using MongoDB aggregation
    // - getTasksWithRelationships() using SQL joins
    // - bulkUpdateTasks() using appropriate database features
}
```

### 6.2 Create SyncService

```kotlin
// TODO: Create service to synchronize data between SQL and MongoDB
@Service
class SyncService(
    // TODO: Inject repositories and mapping utilities
) {
    
    // TODO: Implement syncSqlToMongo method
    fun syncSqlToMongo(): SyncResult {
        // TODO: Read from SQL and update MongoDB documents
    }
    
    // TODO: Implement syncMongoToSql method  
    fun syncMongoToSql(): SyncResult {
        // TODO: Read from MongoDB and update SQL entities
    }
    
    // TODO: Implement validateDataConsistency method
    fun validateDataConsistency(): ConsistencyReport {
        // TODO: Compare data between databases and report differences
    }
}
```

---

## 📋 Step 7: Create REST Controllers

### 7.1 Create TaskController

```kotlin
// TODO: Create REST controller for Task operations
@RestController
@RequestMapping("/api/tasks")
class TaskController(
    private val taskService: TaskService
) {
    
    // TODO: Implement CRUD endpoints:
    // POST /api/tasks - Create task
    // GET /api/tasks/{id} - Get task by ID
    // PUT /api/tasks/{id} - Update task
    // DELETE /api/tasks/{id} - Delete task
    // GET /api/tasks - Get all tasks with pagination
    
    // TODO: Implement search endpoints:
    // GET /api/tasks/search - Search tasks with flexible criteria
    // GET /api/tasks/analytics - Get task analytics from MongoDB
    
    // TODO: Add proper validation, error handling, and HTTP status codes
}
```

### 7.2 Create DataController

```kotlin
// TODO: Create controller for database management operations
@RestController  
@RequestMapping("/api/data")
class DataController(
    private val syncService: SyncService
) {
    
    // TODO: Implement data management endpoints:
    // POST /api/data/sync/sql-to-mongo - Sync SQL to MongoDB
    // POST /api/data/sync/mongo-to-sql - Sync MongoDB to SQL
    // GET /api/data/consistency - Check data consistency
    // GET /api/data/stats - Get database statistics
}
```

---

## 📋 Step 8: Write Comprehensive Tests

### 8.1 Create SQL Repository Tests

```kotlin
// TODO: Create JPA repository tests using @DataJpaTest
@DataJpaTest
class TaskRepositoryTest {
    
    @Autowired
    private lateinit var taskRepository: TaskRepository
    
    @Autowired
    private lateinit var testEntityManager: TestEntityManager
    
    // TODO: Test basic CRUD operations
    @Test
    fun `should save and find task`() {
        // TODO: Implement test
    }
    
    // TODO: Test custom queries
    @Test
    fun `should find tasks by status`() {
        // TODO: Implement test
    }
    
    // TODO: Test relationship mappings
    @Test
    fun `should handle task-project relationship`() {
        // TODO: Implement test
    }
}
```

### 8.2 Create MongoDB Repository Tests

```kotlin
// TODO: Create MongoDB repository tests using @DataMongoTest
@DataMongoTest
class TaskDocumentRepositoryTest {
    
    @Autowired
    private lateinit var taskDocumentRepository: TaskDocumentRepository
    
    // TODO: Test document operations
    @Test
    fun `should save and find task document`() {
        // TODO: Implement test
    }
    
    // TODO: Test MongoDB-specific queries
    @Test
    fun `should find tasks by tags`() {
        // TODO: Implement test
    }
    
    // TODO: Test aggregation pipelines
    @Test
    fun `should aggregate task statistics`() {
        // TODO: Implement test
    }
}
```

### 8.3 Create Service Integration Tests

```kotlin
// TODO: Create service tests using @SpringBootTest
@SpringBootTest
@Transactional
class TaskServiceIntegrationTest {
    
    @Autowired
    private lateinit var taskService: TaskService
    
    // TODO: Test dual database operations
    @Test
    fun `should create task in both databases`() {
        // TODO: Implement test that verifies both SQL and MongoDB persistence
    }
    
    // TODO: Test data consistency
    @Test
    fun `should maintain consistency between databases`() {
        // TODO: Implement test
    }
}
```

---

## 📋 Step 9: Performance and Monitoring

### 9.1 Add Database Monitoring

```kotlin
// TODO: Create configuration for database monitoring
@Component
class DatabaseHealthIndicator : HealthIndicator {
    
    // TODO: Implement health checks for both databases
    override fun health(): Health {
        // TODO: Check SQL database connectivity
        // TODO: Check MongoDB connectivity
        // TODO: Return combined health status
    }
}
```

### 9.2 Add Performance Metrics

```kotlin
// TODO: Create service for performance monitoring
@Service
class PerformanceMonitoringService {
    
    // TODO: Add methods to measure:
    // - Query execution times for SQL vs MongoDB
    // - Data consistency metrics
    // - Storage usage comparison
    // - Throughput metrics
}
```

---

## 📋 Step 10: Create Sample Data and Demo

### 10.1 Create Data Initialization

```kotlin
// TODO: Create component to initialize sample data
@Component
class DataInitializer : ApplicationRunner {
    
    override fun run(args: ApplicationArguments) {
        // TODO: Create sample users, projects, and tasks
        // TODO: Populate both SQL and MongoDB with test data
        // TODO: Demonstrate different data modeling approaches
    }
}
```

### 10.2 Create Demo Endpoints

```kotlin
// TODO: Create controller with demo endpoints
@RestController
@RequestMapping("/api/demo")
class DemoController {
    
    // TODO: Add endpoints that demonstrate:
    // - SQL strengths (complex joins, transactions)
    // - MongoDB strengths (flexible schema, aggregation)
    // - Hybrid approaches (structured + unstructured data)
}
```

---

## 🎯 Expected Results

After completing this workshop, you should have:

1. **Working Dual Database Setup**: Both H2/PostgreSQL and MongoDB configured and working
2. **Entity and Document Models**: Proper data models for both SQL and NoSQL approaches
3. **Repository Implementations**: Spring Data repositories for both databases
4. **Service Layer**: Business logic that leverages both databases appropriately
5. **REST API**: Complete CRUD operations with validation and error handling
6. **Comprehensive Tests**: Unit and integration tests for both database technologies
7. **Performance Monitoring**: Tools to compare and monitor both databases
8. **Sample Data**: Realistic test data demonstrating different use cases

## 🚀 Testing Your Implementation

Run these commands to verify your implementation:

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Start the application
./gradlew bootRun

# Test the APIs
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Test Task","description":"Testing dual persistence"}'

# Check database health
curl http://localhost:8080/actuator/health

# View database statistics
curl http://localhost:8080/api/data/stats
```

## 💡 Key Learning Points

- **Database Selection**: Understanding when to use SQL vs NoSQL
- **Data Modeling**: Different approaches for relational vs document databases
- **Spring Data**: Using both JPA and MongoDB repositories
- **Transaction Management**: Handling transactions across different database types
- **Performance Optimization**: Optimizing queries for each database type
- **Testing Strategies**: Different testing approaches for different databases
- **Hybrid Architecture**: Combining multiple database technologies effectively

Good luck with your implementation! Remember to leverage the strengths of each database technology appropriately.