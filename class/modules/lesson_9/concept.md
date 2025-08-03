# 📚 Lesson 9: CRUD Operations & Transactions

## 🎯 Learning Objectives

By the end of this lesson, you will be able to:
- Implement advanced CRUD operations beyond basic save/find operations
- Master Spring's transaction management with @Transactional
- Design and implement cascade operations for related entities
- Build comprehensive audit trails for data changes
- Handle complex business operations spanning multiple entities
- Implement error handling and rollback scenarios
- Optimize performance for bulk operations
- Test transactional behavior effectively
- Design entity relationships with proper cascade and fetch strategies

## 🏗️ Architecture Overview

This lesson demonstrates **advanced data management patterns** through a comprehensive Blog Management System:

- **Complex Entity Relationships**: Authors, Posts, Comments, Categories with proper mappings
- **Transaction Management**: ACID properties, isolation levels, rollback scenarios
- **Cascade Operations**: Automatic management of related entity lifecycles
- **Audit System**: Complete change tracking and history management
- **Performance Optimization**: Bulk operations, lazy loading, query optimization
- **Error Handling**: Graceful degradation and rollback strategies

## 📊 Entity Relationship Design

### Core Entities

```
Author (1) ←--→ (N) Post (N) ←--→ (M) Category
                     ↓
                 Comment (N)
                     ↓
                 Author (1)
```

### Relationship Patterns

```kotlin
// One-to-Many: Author → Posts
@Entity
class Author {
    @OneToMany(mappedBy = "author", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val posts: List<Post> = emptyList()
}

// Many-to-One: Post → Author
@Entity 
class Post {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    val author: Author
}

// Many-to-Many: Post ↔ Category
@Entity
class Post {
    @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinTable(
        name = "post_categories",
        joinColumns = [JoinColumn(name = "post_id")],
        inverseJoinColumns = [JoinColumn(name = "category_id")]
    )
    val categories: Set<Category> = emptySet()
}

// Self-referencing: Comment → Parent Comment
@Entity
class Comment {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    val parent: Comment?
    
    @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL])
    val replies: List<Comment> = emptyList()
}
```

## 🔄 Transaction Management

### Transaction Fundamentals

**ACID Properties:**
- **Atomicity**: All operations succeed or fail together
- **Consistency**: Data remains in valid state
- **Isolation**: Concurrent transactions don't interfere
- **Durability**: Committed changes are permanent

### Spring Transaction Configuration

```kotlin
@Configuration
@EnableTransactionManagement
class TransactionConfig {
    
    @Bean
    fun transactionManager(entityManagerFactory: EntityManagerFactory): PlatformTransactionManager {
        return JpaTransactionManager(entityManagerFactory)
    }
}
```

### Transaction Annotations

```kotlin
@Service
@Transactional
class BlogService {
    
    // Read-only transaction for queries
    @Transactional(readOnly = true)
    fun findPostById(id: Long): Post? {
        return postRepository.findById(id).orElse(null)
    }
    
    // Default transaction for modifications
    @Transactional
    fun createPost(request: CreatePostRequest): Post {
        // All operations are atomic
        val author = authorRepository.findById(request.authorId)
        val post = Post(title = request.title, content = request.content, author = author)
        return postRepository.save(post)
    }
    
    // Custom transaction settings
    @Transactional(
        isolation = Isolation.READ_COMMITTED,
        timeout = 30,
        rollbackFor = [Exception::class]
    )
    fun publishPost(postId: Long): Post {
        val post = postRepository.findById(postId)
        post.status = PostStatus.PUBLISHED
        post.publishedAt = LocalDateTime.now()
        
        // Audit the action
        auditService.logAction("POST_PUBLISHED", post.id, post.author.id)
        
        return postRepository.save(post)
    }
}
```

### Transaction Propagation

```kotlin
@Service
class BlogService {
    
    // Creates new transaction if none exists
    @Transactional(propagation = Propagation.REQUIRED)
    fun createPostWithComments(request: CreatePostRequest): Post {
        val post = createPost(request)
        
        // This will join the existing transaction
        addInitialComment(post.id, "Welcome to this post!")
        
        return post
    }
    
    // Always creates a new transaction
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun logActivity(action: String, entityId: Long) {
        // This runs in separate transaction
        // Won't rollback if main transaction fails
        activityRepository.save(Activity(action, entityId, LocalDateTime.now()))
    }
    
    // Executes without transaction
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun sendEmailNotification(postId: Long) {
        // External service call - no transaction needed
        emailService.sendNotification(postId)
    }
}
```

## 🔗 Cascade Operations

### Cascade Types

```kotlin
@Entity
class Post {
    // DELETE post → DELETE all comments
    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], orphanRemoval = true)
    val comments: List<Comment> = emptyList()
    
    // PERSIST/MERGE categories when saving post
    @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    val categories: Set<Category> = emptySet()
    
    // REMOVE post → REMOVE all tags (but not tag entities)
    @OneToMany(mappedBy = "post", cascade = [CascadeType.REMOVE])
    val tags: List<PostTag> = emptyList()
}
```

### Cascade Best Practices

```kotlin
@Service
@Transactional
class PostService {
    
    // Demonstrates cascade operations
    fun createPostWithContent(request: CreatePostRequest): Post {
        val post = Post(
            title = request.title,
            content = request.content,
            author = authorRepository.findById(request.authorId)
        )
        
        // Add categories (will be persisted due to CASCADE.PERSIST)
        val categories = categoryRepository.findByIdsIn(request.categoryIds)
        post.categories.addAll(categories)
        
        // Add initial comment (will be persisted due to CASCADE.ALL)
        val initialComment = Comment(
            content = "Post created",
            author = post.author,
            post = post
        )
        post.comments.add(initialComment)
        
        // Single save operation persists everything
        return postRepository.save(post)
    }
    
    // Demonstrates orphan removal
    fun removeComments(postId: Long, commentIds: List<Long>) {
        val post = postRepository.findById(postId)
        
        // Remove comments from collection
        // orphanRemoval = true will delete them from database
        post.comments.removeIf { it.id in commentIds }
        
        postRepository.save(post)
    }
}
```

## 📝 Advanced CRUD Patterns

### Repository Pattern Extensions

```kotlin
interface PostRepository : JpaRepository<Post, Long> {
    
    // Batch operations
    @Modifying
    @Query("UPDATE Post p SET p.status = :status WHERE p.id IN :ids")
    fun updateStatusBatch(@Param("ids") ids: List<Long>, @Param("status") status: PostStatus): Int
    
    // Bulk delete with conditions
    @Modifying
    @Query("DELETE FROM Post p WHERE p.status = :status AND p.createdAt < :before")
    fun deleteOldPosts(@Param("status") status: PostStatus, @Param("before") before: LocalDateTime): Int
    
    // Custom save with audit
    @Modifying
    @Query("""
        INSERT INTO post_audit (post_id, action, changed_by, changed_at) 
        VALUES (:postId, :action, :userId, :timestamp)
    """, nativeQuery = true)
    fun auditPostChange(
        @Param("postId") postId: Long,
        @Param("action") action: String,
        @Param("userId") userId: Long,
        @Param("timestamp") timestamp: LocalDateTime
    )
}
```

### Service Layer Patterns

```kotlin
@Service
@Transactional
class AdvancedPostService {
    
    // Upsert pattern - insert or update
    fun upsertPost(request: UpsertPostRequest): Post {
        val existingPost = request.id?.let { postRepository.findById(it).orElse(null) }
        
        return if (existingPost != null) {
            // Update existing
            existingPost.copy(
                title = request.title,
                content = request.content,
                updatedAt = LocalDateTime.now()
            ).also {
                auditService.logUpdate("POST_UPDATED", it.id, getCurrentUserId())
                postRepository.save(it)
            }
        } else {
            // Create new
            Post(
                title = request.title,
                content = request.content,
                author = getCurrentUser()
            ).also {
                auditService.logCreate("POST_CREATED", it.id, getCurrentUserId())
                postRepository.save(it)
            }
        }
    }
    
    // Soft delete pattern
    fun softDeletePost(postId: Long): Post {
        val post = postRepository.findById(postId)
            .orElseThrow { PostNotFoundException("Post not found: $postId") }
        
        val deletedPost = post.copy(
            deletedAt = LocalDateTime.now(),
            deletedBy = getCurrentUserId()
        )
        
        // Cascade soft delete to comments
        commentService.softDeleteByPostId(postId)
        
        auditService.logDelete("POST_DELETED", postId, getCurrentUserId())
        
        return postRepository.save(deletedPost)
    }
    
    // Bulk operations with transaction
    @Transactional(timeout = 300) // 5 minutes for large operations
    fun bulkUpdatePostStatus(postIds: List<Long>, newStatus: PostStatus): BulkUpdateResult {
        val batchSize = 100
        var successCount = 0
        val errors = mutableListOf<String>()
        
        postIds.chunked(batchSize).forEach { batch ->
            try {
                val updatedCount = postRepository.updateStatusBatch(batch, newStatus)
                successCount += updatedCount
                
                // Audit each batch
                auditService.logBulkUpdate("BULK_STATUS_UPDATE", batch, getCurrentUserId())
                
            } catch (e: Exception) {
                errors.add("Batch failed: ${e.message}")
                logger.error("Bulk update failed for batch: $batch", e)
            }
        }
        
        return BulkUpdateResult(
            totalProcessed = postIds.size,
            successCount = successCount,
            errorCount = postIds.size - successCount,
            errors = errors
        )
    }
}
```

## 📊 Audit System Implementation

### Audit Entity Design

```kotlin
@Entity
@Table(name = "audit_logs")
data class AuditLog(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false)
    val entityType: String,
    
    @Column(nullable = false)
    val entityId: String,
    
    @Enumerated(EnumType.STRING)
    val action: AuditAction,
    
    @Column(nullable = false)
    val userId: Long,
    
    @Column(nullable = false)
    val timestamp: LocalDateTime = LocalDateTime.now(),
    
    @Column(columnDefinition = "TEXT")
    val oldValues: String?,
    
    @Column(columnDefinition = "TEXT")
    val newValues: String?,
    
    val ipAddress: String?,
    val userAgent: String?
)

enum class AuditAction {
    CREATE, UPDATE, DELETE, BULK_UPDATE, BULK_DELETE, STATUS_CHANGE
}
```

### Automatic Auditing with JPA Listeners

```kotlin
@EntityListeners(AuditingEntityListener::class)
@MappedSuperclass
abstract class AuditableEntity {
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime
    
    @LastModifiedDate
    @Column(nullable = false)
    lateinit var updatedAt: LocalDateTime
    
    @CreatedBy
    @Column(updatable = false)
    var createdBy: Long? = null
    
    @LastModifiedBy
    var updatedBy: Long? = null
}

@Configuration
@EnableJpaAuditing
class AuditingConfig {
    
    @Bean
    fun auditorProvider(): AuditorAware<Long> {
        return AuditorAware {
            // Get current user from security context
            SecurityContextHolder.getContext()
                .authentication
                ?.name
                ?.toLongOrNull()
                ?.let { Optional.of(it) }
                ?: Optional.empty()
        }
    }
}
```

### Custom Audit Service

```kotlin
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
class AuditService {
    
    @Autowired
    private lateinit var auditRepository: AuditLogRepository
    
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    
    fun <T> auditChange(
        entity: T,
        action: AuditAction,
        oldValue: T? = null,
        userId: Long = getCurrentUserId()
    ) {
        val entityType = entity!!::class.simpleName!!
        val entityId = getEntityId(entity)
        
        val auditLog = AuditLog(
            entityType = entityType,
            entityId = entityId,
            action = action,
            userId = userId,
            oldValues = oldValue?.let { objectMapper.writeValueAsString(it) },
            newValues = objectMapper.writeValueAsString(entity),
            ipAddress = getCurrentIpAddress(),
            userAgent = getCurrentUserAgent()
        )
        
        auditRepository.save(auditLog)
    }
    
    fun auditBulkOperation(
        entityType: String,
        entityIds: List<String>,
        action: AuditAction,
        userId: Long = getCurrentUserId()
    ) {
        val auditLog = AuditLog(
            entityType = entityType,
            entityId = "BULK:${entityIds.joinToString(",")}",
            action = action,
            userId = userId,
            oldValues = null,
            newValues = "Bulk operation on ${entityIds.size} entities"
        )
        
        auditRepository.save(auditLog)
    }
    
    @Transactional(readOnly = true)
    fun getAuditHistory(entityType: String, entityId: String): List<AuditLog> {
        return auditRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId)
    }
}
```

## ⚡ Performance Optimization

### Lazy Loading Strategies

```kotlin
@Entity
class Post {
    // Lazy loading for large collections
    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    val comments: List<Comment> = emptyList()
    
    // Eager loading for frequently accessed data
    @ManyToOne(fetch = FetchType.EAGER)
    val author: Author
}

// Explicit fetching in service layer
@Service
class PostService {
    
    @Transactional(readOnly = true)
    fun getPostWithComments(postId: Long): PostWithCommentsDTO {
        // Use JOIN FETCH to avoid N+1 problem
        val post = postRepository.findByIdWithComments(postId)
            ?: throw PostNotFoundException("Post not found: $postId")
        
        return PostWithCommentsDTO.from(post)
    }
}

interface PostRepository : JpaRepository<Post, Long> {
    
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.comments WHERE p.id = :id")
    fun findByIdWithComments(@Param("id") id: Long): Post?
    
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.author LEFT JOIN FETCH p.categories WHERE p.id = :id")
    fun findByIdWithAuthorAndCategories(@Param("id") id: Long): Post?
}
```

### Batch Processing

```kotlin
@Service
class BatchProcessingService {
    
    @Transactional
    fun processPosts(postIds: List<Long>, batchSize: Int = 100) {
        postIds.chunked(batchSize).forEach { batch ->
            // Process each batch in separate transaction
            processBatch(batch)
        }
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private fun processBatch(postIds: List<Long>) {
        val posts = postRepository.findAllById(postIds)
        
        posts.forEach { post ->
            // Apply business logic
            post.processContent()
        }
        
        // Batch save
        postRepository.saveAll(posts)
        
        // Clear persistence context to avoid memory issues
        entityManager.flush()
        entityManager.clear()
    }
}
```

### Query Optimization

```kotlin
interface PostRepository : JpaRepository<Post, Long> {
    
    // Efficient pagination with sorting
    @Query("""
        SELECT p FROM Post p 
        LEFT JOIN FETCH p.author 
        WHERE p.status = :status 
        ORDER BY p.createdAt DESC
    """)
    fun findPublishedPostsWithAuthor(
        @Param("status") status: PostStatus,
        pageable: Pageable
    ): Page<Post>
    
    // Projection for summary data
    @Query("""
        SELECT new com.learning.crud.dto.PostSummaryDTO(
            p.id, p.title, p.author.name, p.createdAt, SIZE(p.comments)
        ) 
        FROM Post p 
        LEFT JOIN p.author 
        LEFT JOIN p.comments 
        WHERE p.status = :status
        GROUP BY p.id, p.title, p.author.name, p.createdAt
    """)
    fun findPostSummaries(@Param("status") status: PostStatus): List<PostSummaryDTO>
    
    // Native query for complex operations
    @Query(
        value = """
            SELECT p.*, 
                   COUNT(c.id) as comment_count,
                   AVG(c.rating) as avg_rating
            FROM posts p 
            LEFT JOIN comments c ON p.id = c.post_id 
            WHERE p.created_at > :since
            GROUP BY p.id
            HAVING comment_count > :minComments
            ORDER BY avg_rating DESC
        """,
        nativeQuery = true
    )
    fun findPopularPostsWithStats(
        @Param("since") since: LocalDateTime,
        @Param("minComments") minComments: Int
    ): List<PostWithStats>
}
```

## 🔒 Error Handling & Rollback

### Exception Hierarchy

```kotlin
sealed class BlogException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class PostNotFoundException(message: String) : BlogException(message)
class AuthorNotFoundException(message: String) : BlogException(message)
class DuplicatePostException(message: String) : BlogException(message)
class PostValidationException(message: String) : BlogException(message)
class TransactionTimeoutException(message: String) : BlogException(message)
```

### Rollback Scenarios

```kotlin
@Service
@Transactional
class BlogTransactionService {
    
    // Rollback on specific exceptions
    @Transactional(rollbackFor = [BlogException::class, ValidationException::class])
    fun createPostWithValidation(request: CreatePostRequest): Post {
        // Validate request
        validatePostRequest(request)
        
        val post = createPost(request)
        
        // This will cause rollback if validation fails
        validatePostContent(post.content)
        
        return post
    }
    
    // No rollback on specific exceptions
    @Transactional(noRollbackFor = [EmailSendException::class])
    fun publishPostWithNotification(postId: Long): Post {
        val post = publishPost(postId)
        
        try {
            // Email failure won't rollback the post publication
            emailService.sendPublicationNotification(post)
        } catch (e: EmailSendException) {
            logger.warn("Failed to send email for post ${post.id}", e)
            // Continue with transaction
        }
        
        return post
    }
    
    // Manual rollback
    @Transactional
    fun processPostWithManualRollback(postId: Long): ProcessResult {
        val post = postRepository.findById(postId)
        
        return try {
            val result = externalService.processPost(post)
            
            if (result.hasErrors()) {
                // Force rollback even though no exception was thrown
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()
                ProcessResult.failure("External service reported errors")
            } else {
                post.status = PostStatus.PROCESSED
                postRepository.save(post)
                ProcessResult.success()
            }
        } catch (e: ExternalServiceException) {
            logger.error("External service failed for post $postId", e)
            ProcessResult.failure("External service unavailable")
        }
    }
}
```

### Global Transaction Error Handling

```kotlin
@ControllerAdvice
class TransactionExceptionHandler {
    
    @ExceptionHandler(TransactionSystemException::class)
    fun handleTransactionException(ex: TransactionSystemException): ResponseEntity<ErrorResponse> {
        logger.error("Transaction system error", ex)
        
        val response = ErrorResponse(
            error = "TRANSACTION_ERROR",
            message = "Transaction could not be completed",
            timestamp = LocalDateTime.now()
        )
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }
    
    @ExceptionHandler(OptimisticLockingFailureException::class)
    fun handleOptimisticLock(ex: OptimisticLockingFailureException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            error = "CONCURRENT_MODIFICATION",
            message = "Resource was modified by another user. Please refresh and try again.",
            timestamp = LocalDateTime.now()
        )
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response)
    }
}
```

## 🧪 Testing Transactional Behavior

### Transaction Testing Strategies

```kotlin
@SpringBootTest
@Transactional
class BlogServiceTransactionTest {
    
    @Autowired
    private lateinit var blogService: BlogService
    
    @Autowired
    private lateinit var testEntityManager: TestEntityManager
    
    @Test
    @Rollback(false) // Don't rollback this test
    fun `should commit transaction on successful operation`() {
        val request = CreatePostRequest("Test Title", "Content", authorId = 1L)
        
        val post = blogService.createPost(request)
        
        // Flush to database
        testEntityManager.flush()
        
        // Verify in database
        val savedPost = testEntityManager.find(Post::class.java, post.id)
        assertThat(savedPost).isNotNull
        assertThat(savedPost.title).isEqualTo("Test Title")
    }
    
    @Test
    fun `should rollback transaction on exception`() {
        val request = CreatePostRequest("", "Content", authorId = 999L) // Invalid
        
        assertThrows<ValidationException> {
            blogService.createPost(request)
        }
        
        // Verify nothing was saved
        val posts = testEntityManager.entityManager
            .createQuery("SELECT p FROM Post p WHERE p.title = ''", Post::class.java)
            .resultList
        
        assertThat(posts).isEmpty()
    }
    
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun `should test transaction propagation`() {
        // This test runs without transaction
        val postId = runInTransaction { 
            blogService.createPost(CreatePostRequest("Test", "Content", 1L)).id 
        }
        
        // Verify data persisted across transaction boundary
        runInTransaction {
            val post = blogService.findById(postId!!)
            assertThat(post).isNotNull
        }
    }
    
    private fun <T> runInTransaction(operation: () -> T): T {
        return TransactionTemplate(transactionManager).execute { operation() }!!
    }
}
```

### Performance Testing

```kotlin
@SpringBootTest
class BlogServicePerformanceTest {
    
    @Autowired
    private lateinit var blogService: BlogService
    
    @Test
    fun `should handle bulk operations efficiently`() {
        val startTime = System.currentTimeMillis()
        
        // Create 1000 posts
        val posts = (1..1000).map { i ->
            CreatePostRequest("Post $i", "Content $i", authorId = 1L)
        }
        
        val createdPosts = blogService.bulkCreatePosts(posts)
        
        val duration = System.currentTimeMillis() - startTime
        
        assertThat(createdPosts).hasSize(1000)
        assertThat(duration).isLessThan(10000) // Less than 10 seconds
    }
    
    @Test
    fun `should avoid N+1 queries when loading posts with comments`() {
        // Create test data
        val post = createPostWithComments(10)
        
        // Clear persistence context
        entityManager.clear()
        
        // Monitor query count
        val queryCount = QueryCountHolder.getGrandTotal()
        
        val loadedPost = blogService.getPostWithComments(post.id!!)
        
        val queriesExecuted = QueryCountHolder.getGrandTotal() - queryCount
        
        // Should execute only 1 query with JOIN FETCH
        assertThat(queriesExecuted).isEqualTo(1)
        assertThat(loadedPost.comments).hasSize(10)
    }
}
```

## 🎯 Key Takeaways

1. **Transaction Management**: Use @Transactional appropriately with correct propagation and isolation levels
2. **Cascade Operations**: Design entity relationships with proper cascade strategies
3. **Performance**: Optimize queries, use batch operations, and avoid N+1 problems
4. **Audit Trails**: Implement comprehensive change tracking for compliance and debugging
5. **Error Handling**: Design proper rollback strategies and exception handling
6. **Testing**: Test both successful operations and failure scenarios
7. **Bulk Operations**: Use efficient patterns for processing large datasets
8. **Data Integrity**: Maintain consistency across related entities

This lesson provides the foundation for building robust, scalable data layers that can handle complex business requirements while maintaining data integrity and performance.