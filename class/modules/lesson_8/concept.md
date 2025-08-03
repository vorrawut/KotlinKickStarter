# 📚 Lesson 8: Persistence with Spring Data JPA & MongoDB

## 🎯 Learning Objectives

By the end of this lesson, you will be able to:
- Configure and use Spring Data JPA with SQL databases (PostgreSQL, H2)
- Configure and use Spring Data MongoDB for NoSQL persistence
- Design entities for both relational and document databases
- Implement repository patterns for both SQL and MongoDB
- Choose the right database technology for different use cases
- Handle data modeling differences between SQL and NoSQL
- Implement transactions and data consistency strategies
- Test applications with both embedded and real databases

## 🏗️ Architecture Overview

This lesson demonstrates a **dual-database architecture** where:
- **SQL Database (PostgreSQL/H2)**: Handles structured data with relationships
- **MongoDB**: Handles flexible, document-based data
- **Unified Service Layer**: Abstracts persistence implementation details
- **Repository Pattern**: Provides consistent data access interfaces

## 🗄️ Database Technologies Comparison

### SQL Databases (PostgreSQL, H2)
**Best for:**
- Structured data with clear relationships
- ACID transactions and consistency requirements
- Complex queries with joins
- Financial data, user accounts, orders

**Characteristics:**
- Schema-first design
- Strong consistency
- SQL query language
- Foreign key relationships
- Normalized data structure

### MongoDB (NoSQL Document Database)
**Best for:**
- Flexible, evolving schemas
- Hierarchical/nested data
- Rapid prototyping
- Content management, logs, analytics

**Characteristics:**
- Schema-less (flexible schema)
- Horizontal scaling
- Document-based storage (JSON/BSON)
- Eventual consistency options
- Denormalized data structure

## 🔧 Spring Data Technologies

### Spring Data JPA
```kotlin
// Entity mapping
@Entity
@Table(name = "users")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, unique = true)
    val username: String,
    
    @OneToMany(mappedBy = "author", cascade = [CascadeType.ALL])
    val posts: List<Post> = emptyList()
)

// Repository interface
interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): User?
    
    @Query("SELECT u FROM User u WHERE u.createdAt > :date")
    fun findRecentUsers(@Param("date") date: LocalDateTime): List<User>
}
```

### Spring Data MongoDB
```kotlin
// Document mapping
@Document(collection = "user_profiles")
data class UserProfile(
    @Id
    val id: String? = null,
    
    @Indexed(unique = true)
    val userId: String,
    
    val preferences: Map<String, Any>,
    val metadata: UserMetadata,
    val tags: List<String> = emptyList()
)

// Repository interface
interface UserProfileRepository : MongoRepository<UserProfile, String> {
    fun findByUserId(userId: String): UserProfile?
    
    @Query("{ 'tags': { \$in: ?0 } }")
    fun findByTagsIn(tags: List<String>): List<UserProfile>
}
```

## 🏛️ Data Modeling Strategies

### 1. SQL Modeling (Normalized)
```kotlin
// Separate entities with relationships
@Entity
data class Author(
    @Id val id: Long,
    val name: String,
    val email: String
)

@Entity
data class Book(
    @Id val id: Long,
    val title: String,
    val isbn: String,
    
    @ManyToOne
    @JoinColumn(name = "author_id")
    val author: Author
)
```

### 2. MongoDB Modeling (Denormalized)
```kotlin
// Embedded documents
@Document
data class BookDocument(
    @Id val id: String,
    val title: String,
    val isbn: String,
    
    // Embedded author information
    val author: AuthorInfo,
    val reviews: List<Review> = emptyList()
)

data class AuthorInfo(
    val authorId: String,
    val name: String,
    val email: String
)
```

## 📋 Configuration Essentials

### SQL Database Configuration
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/lesson8_db
    username: ${DB_USER:lesson8}
    password: ${DB_PASSWORD:password}
    driver-class-name: org.postgresql.Driver
    
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
```

### MongoDB Configuration
```yaml
# application.yml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/lesson8_mongo
      auto-index-creation: true
      
  mongodb:
    embedded:
      version: 4.4.0  # For testing
```

### Dual Database Configuration Class
```kotlin
@Configuration
@EnableJpaRepositories(
    basePackages = ["com.learning.persistence.repository.jpa"],
    entityManagerFactoryRef = "sqlEntityManagerFactory",
    transactionManagerRef = "sqlTransactionManager"
)
@EnableMongoRepositories(
    basePackages = ["com.learning.persistence.repository.mongo"]
)
class DatabaseConfiguration {
    
    @Primary
    @Bean
    fun sqlEntityManagerFactory(): LocalContainerEntityManagerFactoryBean {
        // SQL configuration
    }
    
    @Bean
    fun mongoTemplate(): MongoTemplate {
        // MongoDB configuration
    }
}
```

## 🔍 Query Strategies

### SQL Queries (JPQL/Criteria API)
```kotlin
interface TaskRepository : JpaRepository<Task, Long> {
    // Method name queries
    fun findByStatusAndPriorityOrderByCreatedAtDesc(
        status: TaskStatus, 
        priority: TaskPriority
    ): List<Task>
    
    // Custom JPQL
    @Query("""
        SELECT t FROM Task t 
        WHERE t.assignee.userId = :userId 
        AND t.dueDate BETWEEN :start AND :end
    """)
    fun findTasksInDateRange(
        @Param("userId") userId: String,
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime
    ): List<Task>
    
    // Native SQL for complex queries
    @Query(
        value = "SELECT * FROM tasks WHERE status = ?1 AND priority_level > ?2",
        nativeQuery = true
    )
    fun findHighPriorityTasks(status: String, minPriority: Int): List<Task>
}
```

### MongoDB Queries (Criteria API/Query Methods)
```kotlin
interface TaskDocumentRepository : MongoRepository<TaskDocument, String> {
    // Method name queries
    fun findByStatusAndTagsContaining(status: String, tag: String): List<TaskDocument>
    
    // JSON-based queries
    @Query("{ 'assignee.department': ?0, 'priority': { \$gte: ?1 } }")
    fun findByDepartmentAndMinPriority(department: String, minPriority: Int): List<TaskDocument>
    
    // Aggregation pipeline
    @Aggregation(pipeline = [
        "{ '\$match': { 'status': 'ACTIVE' } }",
        "{ '\$group': { '_id': '\$assignee.department', 'count': { '\$sum': 1 } } }",
        "{ '\$sort': { 'count': -1 } }"
    ])
    fun getTaskCountByDepartment(): List<DepartmentStats>
}
```

## 🔄 Transaction Management

### SQL Transactions
```kotlin
@Service
@Transactional
class TaskService {
    
    @Transactional(readOnly = true)
    fun findTask(id: Long): Task? {
        return taskRepository.findById(id).orElse(null)
    }
    
    @Transactional(rollbackFor = [Exception::class])
    fun createTaskWithAudit(request: CreateTaskRequest): Task {
        val task = taskRepository.save(task)
        auditRepository.save(AuditLog("TASK_CREATED", task.id))
        return task
    }
    
    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun updateTaskStatus(id: Long, status: TaskStatus): Task {
        // Complex status transition logic
    }
}
```

### MongoDB Transactions (4.0+)
```kotlin
@Service
class TaskDocumentService {
    
    @Autowired
    private lateinit var mongoTemplate: MongoTemplate
    
    @Transactional
    fun createTaskWithAudit(request: CreateTaskRequest): TaskDocument {
        val session = mongoTemplate.mongoDatabaseFactory.session
        
        return session.withTransaction {
            val task = mongoTemplate.save(task)
            val audit = AuditDocument("TASK_CREATED", task.id)
            mongoTemplate.save(audit)
            task
        }
    }
}
```

## 🧪 Testing Strategies

### SQL Testing with H2
```kotlin
@DataJpaTest
class TaskRepositoryTest {
    
    @Autowired
    private lateinit var taskRepository: TaskRepository
    
    @Autowired
    private lateinit var testEntityManager: TestEntityManager
    
    @Test
    fun `should find tasks by status`() {
        // Given
        val task = Task(title = "Test", status = TaskStatus.ACTIVE)
        testEntityManager.persistAndFlush(task)
        
        // When
        val found = taskRepository.findByStatus(TaskStatus.ACTIVE)
        
        // Then
        assertThat(found).hasSize(1)
        assertThat(found[0].title).isEqualTo("Test")
    }
}
```

### MongoDB Testing with Embedded MongoDB
```kotlin
@DataMongoTest
class TaskDocumentRepositoryTest {
    
    @Autowired
    private lateinit var taskDocumentRepository: TaskDocumentRepository
    
    @Test
    fun `should find tasks by tags`() {
        // Given
        val task = TaskDocument(
            title = "Test",
            tags = listOf("urgent", "backend")
        )
        taskDocumentRepository.save(task)
        
        // When
        val found = taskDocumentRepository.findByTagsContaining("urgent")
        
        // Then
        assertThat(found).hasSize(1)
        assertThat(found[0].title).isEqualTo("Test")
    }
}
```

## 🎨 Design Patterns and Best Practices

### 1. Repository Pattern Implementation
```kotlin
// Abstract repository interface
interface BaseRepository<T, ID> {
    fun save(entity: T): T
    fun findById(id: ID): T?
    fun findAll(): List<T>
    fun delete(entity: T)
}

// SQL implementation
@Repository
class JpaTaskRepository(
    private val jpaRepository: TaskJpaRepository
) : BaseRepository<Task, Long> {
    override fun save(entity: Task): Task = jpaRepository.save(entity)
    // ... other methods
}

// MongoDB implementation
@Repository  
class MongoTaskRepository(
    private val mongoRepository: TaskDocumentRepository
) : BaseRepository<TaskDocument, String> {
    override fun save(entity: TaskDocument): TaskDocument = mongoRepository.save(entity)
    // ... other methods
}
```

### 2. Data Mapping Strategies
```kotlin
// Entity to Document mapping
@Component
class TaskMapper {
    
    fun toDocument(entity: Task): TaskDocument {
        return TaskDocument(
            id = entity.id?.toString(),
            title = entity.title,
            description = entity.description,
            status = entity.status.name,
            assignee = entity.assignee?.let {
                AssigneeInfo(it.userId, it.name, it.email, it.department)
            },
            metadata = TaskMetadata(
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
                tags = extractTags(entity)
            )
        )
    }
    
    fun toEntity(document: TaskDocument): Task {
        return Task(
            id = document.id?.toLongOrNull(),
            title = document.title,
            description = document.description,
            status = TaskStatus.valueOf(document.status),
            assignee = document.assignee?.let {
                Assignee(it.userId, it.name, it.email, it.department)
            },
            createdAt = document.metadata.createdAt,
            updatedAt = document.metadata.updatedAt
        )
    }
}
```

### 3. Hybrid Data Access Pattern
```kotlin
@Service
class HybridTaskService(
    private val sqlTaskRepository: JpaTaskRepository,
    private val mongoTaskRepository: MongoTaskRepository,
    private val taskMapper: TaskMapper
) {
    
    // Store structured data in SQL
    @Transactional
    fun createTask(request: CreateTaskRequest): Task {
        val task = sqlTaskRepository.save(task)
        
        // Store flexible metadata in MongoDB
        val document = taskMapper.toDocument(task)
        mongoTaskRepository.save(document)
        
        return task
    }
    
    // Query SQL for structured queries
    fun findTasksByStatus(status: TaskStatus): List<Task> {
        return sqlTaskRepository.findByStatus(status)
    }
    
    // Query MongoDB for flexible searches
    fun searchTasksByTags(tags: List<String>): List<TaskDocument> {
        return mongoTaskRepository.findByTagsIn(tags)
    }
}
```

## 🚀 Performance Optimization

### SQL Optimization
```kotlin
// Entity with optimized mappings
@Entity
@Table(name = "tasks", indexes = [
    Index(name = "idx_task_status", columnList = "status"),
    Index(name = "idx_task_assignee", columnList = "assignee_user_id"),
    Index(name = "idx_task_due_date", columnList = "due_date")
])
data class Task(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    // Lazy loading for large collections
    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY)
    val comments: List<Comment> = emptyList(),
    
    // Batch loading optimization
    @BatchSize(size = 10)
    @OneToMany(mappedBy = "task")
    val attachments: List<Attachment> = emptyList()
)

// Repository with optimized queries
interface TaskRepository : JpaRepository<Task, Long> {
    
    @Query("SELECT t FROM Task t JOIN FETCH t.assignee WHERE t.status = :status")
    fun findByStatusWithAssignee(@Param("status") status: TaskStatus): List<Task>
    
    // Pagination for large results
    fun findByStatus(status: TaskStatus, pageable: Pageable): Page<Task>
}
```

### MongoDB Optimization
```kotlin
// Document with indexes
@Document(collection = "tasks")
@CompoundIndex(def = "{'status': 1, 'priority': -1}")
@CompoundIndex(def = "{'assignee.userId': 1, 'dueDate': 1}")
data class TaskDocument(
    @Id val id: String? = null,
    
    @Indexed val status: String,
    @Indexed val priority: Int,
    
    @TextIndexed val title: String,
    @TextIndexed val description: String
)

// Repository with aggregation pipelines
interface TaskDocumentRepository : MongoRepository<TaskDocument, String> {
    
    @Aggregation(pipeline = [
        "{ '\$match': { 'status': ?0 } }",
        "{ '\$lookup': { 'from': 'users', 'localField': 'assignee.userId', 'foreignField': '_id', 'as': 'assigneeDetails' } }",
        "{ '\$limit': ?1 }"
    ])
    fun findTasksWithAssigneeDetails(status: String, limit: Int): List<TaskWithAssignee>
}
```

## 🔒 Security Considerations

### SQL Security
```kotlin
// Always use parameterized queries
@Query("SELECT t FROM Task t WHERE t.assignee.userId = :userId")
fun findByAssigneeUserId(@Param("userId") userId: String): List<Task>

// Avoid string concatenation
// BAD: @Query("SELECT t FROM Task t WHERE t.title = '" + title + "'")
// GOOD: Use @Param or method parameters
```

### MongoDB Security
```kotlin
// Use type-safe queries
fun findByStatus(status: TaskStatus): List<TaskDocument> {
    val query = Query(Criteria.where("status").`is`(status.name))
    return mongoTemplate.find(query, TaskDocument::class.java)
}

// Validate input for JSON queries
@Query("{ 'priority': { '\$gte': ?0 } }")
fun findByMinPriority(@Min(1) @Max(5) priority: Int): List<TaskDocument>
```

## 📊 When to Use Which Database?

### Use SQL Database When:
- ✅ Strong consistency requirements
- ✅ Complex relationships between entities
- ✅ ACID transactions are critical
- ✅ Mature, well-defined schema
- ✅ Complex analytical queries with joins
- ✅ Regulatory compliance requirements

### Use MongoDB When:
- ✅ Flexible, evolving schema
- ✅ Hierarchical or nested data structures
- ✅ Rapid prototyping and development
- ✅ Horizontal scaling requirements
- ✅ Content management systems
- ✅ Real-time analytics and logging

### Hybrid Approach When:
- ✅ Core business data in SQL + flexible metadata in MongoDB
- ✅ Transactional data in SQL + search/analytics in MongoDB
- ✅ Different teams/services have different data needs
- ✅ Migration strategy from SQL to NoSQL (or vice versa)

## 🎯 Key Takeaways

1. **Choose the Right Tool**: SQL for structured, relational data; MongoDB for flexible, document-based data
2. **Repository Pattern**: Provides consistent data access regardless of underlying technology
3. **Configuration Matters**: Proper database configuration is crucial for performance and reliability
4. **Testing Strategy**: Use embedded databases for integration tests
5. **Transaction Management**: Understand transaction capabilities and limitations of each database
6. **Performance Optimization**: Indexes, query optimization, and proper data modeling are essential
7. **Security First**: Always use parameterized queries and validate inputs
8. **Hybrid Approaches**: Modern applications often benefit from using multiple database technologies

This lesson provides the foundation for building robust, scalable data persistence layers that can adapt to changing business requirements while maintaining performance and reliability.