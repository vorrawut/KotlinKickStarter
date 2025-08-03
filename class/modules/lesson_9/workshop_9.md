# 🛠️ Workshop 9: Advanced CRUD Operations & Transactions

## 🎯 Workshop Objectives

In this hands-on workshop, you will:
1. Build a comprehensive Blog Management System with complex entity relationships
2. Implement advanced CRUD operations beyond basic save/find
3. Master Spring's transaction management with @Transactional
4. Design and implement cascade operations for related entities
5. Build a complete audit trail system for tracking changes
6. Handle complex business operations spanning multiple entities
7. Implement error handling and rollback scenarios
8. Optimize performance for bulk operations
9. Test transactional behavior comprehensively

## 🏗️ Project Structure

```
src/main/kotlin/com/learning/crud/
├── CrudApplication.kt
├── config/
│   ├── DatabaseConfiguration.kt
│   ├── TransactionConfiguration.kt
│   └── AuditingConfiguration.kt
├── model/
│   ├── Author.kt
│   ├── Post.kt
│   ├── Comment.kt
│   ├── Category.kt
│   └── AuditLog.kt
├── repository/
│   ├── AuthorRepository.kt
│   ├── PostRepository.kt
│   ├── CommentRepository.kt
│   ├── CategoryRepository.kt
│   └── AuditLogRepository.kt
├── service/
│   ├── BlogService.kt
│   ├── CommentService.kt
│   ├── AuditService.kt
│   └── BulkOperationService.kt
├── controller/
│   ├── BlogController.kt
│   ├── CommentController.kt
│   └── AdminController.kt
├── dto/
│   ├── PostDTOs.kt
│   ├── CommentDTOs.kt
│   └── AuditDTOs.kt
└── audit/
    ├── AuditableEntity.kt
    └── EntityAuditListener.kt
```

---

## 📋 Step 1: Project Setup and Dependencies

### 1.1 Create build.gradle.kts

```kotlin
// TODO: Add Spring Boot parent and dependencies for:
// - Spring Boot Web
// - Spring Data JPA
// - H2 Database (for development)
// - PostgreSQL (for production)
// - Kotlin Jackson module
// - Validation
// - Transaction management
// - Testing dependencies

plugins {
    // TODO: Add Kotlin and Spring Boot plugins
}

dependencies {
    // TODO: Add all required dependencies
    // Hint: You'll need spring-boot-starter-data-jpa, spring-boot-starter-web
    // Also include spring-boot-starter-validation for Bean Validation
}
```

### 1.2 Create settings.gradle.kts

```kotlin
// TODO: Set the project name
rootProject.name = "lesson-9-crud-transactions"
```

---

## 📋 Step 2: Database Configuration

### 2.1 Create application.yml

```yaml
# TODO: Create configuration for:
# - H2 database (for development/testing)
# - PostgreSQL (for production)
# - JPA settings (ddl-auto, show-sql, etc.)
# - Transaction management settings
# - Logging configuration

spring:
  profiles:
    active: dev
    
  # TODO: Configure H2 for development
  datasource:
    # Add H2 configuration here
    
  # TODO: Configure JPA/Hibernate
  jpa:
    # Add JPA configuration here
    
  # TODO: Configure transaction management
  transaction:
    # Add transaction configuration here

# TODO: Configure logging for SQL and transactions
logging:
  level:
    # Add logging configuration
```

### 2.2 Create TransactionConfiguration.kt

```kotlin
// TODO: Create configuration class for transaction management
// Hints:
// - Use @EnableTransactionManagement
// - Configure PlatformTransactionManager
// - Set up transaction advisor and interceptor

@Configuration
// TODO: Add @EnableTransactionManagement annotation
class TransactionConfiguration {
    
    // TODO: Configure transaction manager
    // TODO: Set up transaction interceptor
    // TODO: Configure transaction advisor
}
```

---

## 📋 Step 3: Create Entity Models

### 3.1 Create Base Auditable Entity

```kotlin
// TODO: Create base class for auditable entities
// Hints:
// - Use @MappedSuperclass
// - Add @EntityListeners(AuditingEntityListener::class)
// - Include createdAt, updatedAt, createdBy, updatedBy fields
// - Use Spring Data JPA auditing annotations

@MappedSuperclass
// TODO: Add entity listeners annotation
abstract class AuditableEntity {
    
    // TODO: Add @CreatedDate annotation
    lateinit var createdAt: LocalDateTime
    
    // TODO: Add @LastModifiedDate annotation
    lateinit var updatedAt: LocalDateTime
    
    // TODO: Add @CreatedBy annotation
    var createdBy: Long? = null
    
    // TODO: Add @LastModifiedBy annotation
    var updatedBy: Long? = null
}
```

### 3.2 Create Author Entity

```kotlin
// TODO: Create Author entity with following properties:
// - id (Long, auto-generated)
// - username (String, unique)
// - email (String, unique)
// - firstName (String)
// - lastName (String)
// - bio (String, nullable)
// - isActive (Boolean, default true)
// - posts (One-to-Many relationship with Post)

// TODO: Add proper JPA annotations (@Entity, @Table, etc.)
// TODO: Add validation annotations
// TODO: Add cascade operations for posts
// TODO: Extend AuditableEntity

@Entity
@Table(name = "authors")
data class Author(
    // TODO: Implement all properties with proper annotations
) : AuditableEntity() {
    
    // TODO: Add business logic methods
    fun getFullName(): String {
        // TODO: Implement full name logic
    }
    
    fun getPostCount(): Int {
        // TODO: Return number of posts
    }
    
    fun getActivePostCount(): Int {
        // TODO: Return number of active posts
    }
}
```

### 3.3 Create Post Entity

```kotlin
// TODO: Create Post entity with following properties:
// - id (Long, auto-generated)
// - title (String, not null)
// - content (String, not null)
// - summary (String, nullable)
// - status (PostStatus enum)
// - publishedAt (LocalDateTime, nullable)
// - author (Many-to-One relationship with Author)
// - comments (One-to-Many relationship with Comment)
// - categories (Many-to-Many relationship with Category)
// - viewCount (Long, default 0)
// - likeCount (Long, default 0)

// TODO: Add proper JPA annotations
// TODO: Add validation annotations
// TODO: Add cascade operations for comments
// TODO: Add indexes for frequently queried fields
// TODO: Extend AuditableEntity

@Entity
@Table(name = "posts")
data class Post(
    // TODO: Implement all properties with proper annotations
) : AuditableEntity() {
    
    // TODO: Add business logic methods
    fun isPublished(): Boolean {
        // TODO: Check if post is published
    }
    
    fun canBeEditedBy(userId: Long): Boolean {
        // TODO: Check if user can edit this post
    }
    
    fun addComment(comment: Comment) {
        // TODO: Add comment with proper relationship setup
    }
    
    fun removeComment(comment: Comment) {
        // TODO: Remove comment with proper cleanup
    }
    
    fun incrementViewCount() {
        // TODO: Increment view count safely
    }
}

// TODO: Create PostStatus enum
enum class PostStatus {
    // TODO: Add status values (DRAFT, PUBLISHED, ARCHIVED, DELETED)
}
```

### 3.4 Create Comment Entity

```kotlin
// TODO: Create Comment entity with following properties:
// - id (Long, auto-generated)
// - content (String, not null)
// - author (Many-to-One relationship with Author)
// - post (Many-to-One relationship with Post)
// - parent (Many-to-One self-reference for replies)
// - replies (One-to-Many self-reference for child comments)
// - status (CommentStatus enum)
// - isApproved (Boolean, default false)

// TODO: Add proper JPA annotations
// TODO: Add validation annotations
// TODO: Add cascade operations for replies
// TODO: Extend AuditableEntity

@Entity
@Table(name = "comments")
data class Comment(
    // TODO: Implement all properties with proper annotations
) : AuditableEntity() {
    
    // TODO: Add business logic methods
    fun isReply(): Boolean {
        // TODO: Check if this is a reply to another comment
    }
    
    fun canBeDeletedBy(userId: Long): Boolean {
        // TODO: Check if user can delete this comment
    }
    
    fun addReply(reply: Comment) {
        // TODO: Add reply with proper relationship setup
    }
    
    fun getReplyCount(): Int {
        // TODO: Return number of replies
    }
}

// TODO: Create CommentStatus enum
enum class CommentStatus {
    // TODO: Add status values (PENDING, APPROVED, REJECTED, DELETED)
}
```

### 3.5 Create Category Entity

```kotlin
// TODO: Create Category entity with following properties:
// - id (Long, auto-generated)
// - name (String, unique)
// - description (String, nullable)
// - color (String, nullable, for UI)
// - posts (Many-to-Many relationship with Post)
// - isActive (Boolean, default true)

// TODO: Add proper JPA annotations
// TODO: Add validation annotations
// TODO: Extend AuditableEntity

@Entity
@Table(name = "categories")
data class Category(
    // TODO: Implement all properties with proper annotations
) : AuditableEntity() {
    
    // TODO: Add business logic methods
    fun getPostCount(): Long {
        // TODO: Return number of posts in this category
    }
    
    fun getPublishedPostCount(): Long {
        // TODO: Return number of published posts in this category
    }
}
```

### 3.6 Create AuditLog Entity

```kotlin
// TODO: Create AuditLog entity for tracking all changes
// - id (Long, auto-generated)
// - entityType (String, not null)
// - entityId (String, not null)
// - action (AuditAction enum)
// - userId (Long, not null)
// - timestamp (LocalDateTime, not null)
// - oldValues (String, nullable, JSON)
// - newValues (String, nullable, JSON)
// - ipAddress (String, nullable)
// - userAgent (String, nullable)

@Entity
@Table(name = "audit_logs")
data class AuditLog(
    // TODO: Implement all properties with proper annotations
) {
    
    // TODO: Add utility methods
    fun getChangedFields(): Set<String> {
        // TODO: Parse and return changed field names
    }
    
    fun getOldValue(fieldName: String): Any? {
        // TODO: Parse old values and return specific field
    }
    
    fun getNewValue(fieldName: String): Any? {
        // TODO: Parse new values and return specific field
    }
}

// TODO: Create AuditAction enum
enum class AuditAction {
    // TODO: Add action values (CREATE, UPDATE, DELETE, BULK_UPDATE, etc.)
}
```

---

## 📋 Step 4: Create Repository Interfaces

### 4.1 Create AuthorRepository

```kotlin
// TODO: Create AuthorRepository extending JpaRepository
interface AuthorRepository : JpaRepository<Author, Long> {
    
    // TODO: Add method name queries:
    // - Find author by username
    // - Find author by email
    // - Find active authors
    // - Find authors with posts count greater than specified value
    
    // TODO: Add custom JPQL queries:
    // - Find authors with their post statistics
    // - Find top authors by post count
    // - Find authors by activity period
    
    // TODO: Add bulk operations:
    // - Activate/deactivate multiple authors
    // - Update author statistics
}
```

### 4.2 Create PostRepository

```kotlin
// TODO: Create PostRepository extending JpaRepository
interface PostRepository : JpaRepository<Post, Long> {
    
    // TODO: Add method name queries:
    // - Find posts by status
    // - Find posts by author
    // - Find posts by category
    // - Find posts published after date
    // - Find posts by title containing text
    
    // TODO: Add custom JPQL queries:
    // - Find posts with comment count
    // - Find popular posts (by view count)
    // - Find posts with categories and author
    // - Find posts in date range with statistics
    
    // TODO: Add bulk operations:
    // - Update post status for multiple posts
    // - Increment view counts
    // - Archive old posts
    
    // TODO: Add native SQL queries for complex operations:
    // - Find trending posts with complex criteria
    // - Get post statistics with aggregations
}
```

### 4.3 Create CommentRepository

```kotlin
// TODO: Create CommentRepository extending JpaRepository
interface CommentRepository : JpaRepository<Comment, Long> {
    
    // TODO: Add method name queries:
    // - Find comments by post
    // - Find comments by author
    // - Find approved comments
    // - Find top-level comments (no parent)
    // - Find replies to specific comment
    
    // TODO: Add custom queries:
    // - Find comment hierarchy for post
    // - Find recent comments with author info
    // - Count comments by status
    
    // TODO: Add bulk operations:
    // - Approve multiple comments
    // - Soft delete comments by post
}
```

### 4.4 Create CategoryRepository

```kotlin
// TODO: Create CategoryRepository extending JpaRepository
interface CategoryRepository : JpaRepository<Category, Long> {
    
    // TODO: Add method name queries:
    // - Find categories by name
    // - Find active categories
    // - Find categories with posts
    
    // TODO: Add custom queries:
    // - Find categories with post counts
    // - Find most popular categories
}
```

### 4.5 Create AuditLogRepository

```kotlin
// TODO: Create AuditLogRepository extending JpaRepository
interface AuditLogRepository : JpaRepository<AuditLog, Long> {
    
    // TODO: Add queries for audit trail:
    // - Find audit logs by entity type and ID
    // - Find audit logs by user
    // - Find audit logs in date range
    // - Find audit logs by action type
    
    // TODO: Add cleanup operations:
    // - Delete old audit logs
    // - Archive audit logs older than specified date
}
```

---

## 📋 Step 5: Create DTOs

### 5.1 Create Post DTOs

```kotlin
// TODO: Create DTOs for Post operations:

// Request DTOs
data class CreatePostRequest(
    // TODO: Add validation annotations and properties
    // title, content, summary, categoryIds, authorId
)

data class UpdatePostRequest(
    // TODO: Add optional properties for partial updates
)

data class PublishPostRequest(
    // TODO: Add properties needed for publishing
    // publishedAt, notifySubscribers
)

// Response DTOs
data class PostResponse(
    // TODO: Add properties from Post entity plus computed fields
    // includeCommentCount, authorName, categoryNames, etc.
)

data class PostSummaryResponse(
    // TODO: Add summary properties for list views
    // id, title, authorName, publishedAt, commentCount, viewCount
)

data class PostDetailsResponse(
    // TODO: Add complete post details including relationships
    // post info, author details, categories, comment summaries
)

// Search and Filter DTOs
data class PostSearchRequest(
    // TODO: Add search criteria
    // status, authorId, categoryIds, dateRange, textSearch, sortBy
)
```

### 5.2 Create Comment DTOs

```kotlin
// TODO: Create DTOs for Comment operations:

data class CreateCommentRequest(
    // TODO: Add properties for comment creation
    // content, postId, parentId (for replies)
)

data class UpdateCommentRequest(
    // TODO: Add properties for comment updates
)

data class CommentResponse(
    // TODO: Add comment properties with author info
    // Include nested replies if applicable
)

data class CommentTreeResponse(
    // TODO: Add hierarchical comment structure
    // comment + nested replies
)
```

### 5.3 Create Audit DTOs

```kotlin
// TODO: Create DTOs for audit operations:

data class AuditLogResponse(
    // TODO: Add audit log properties
    // Include user-friendly formatting
)

data class AuditHistoryRequest(
    // TODO: Add filtering criteria for audit history
    // entityType, entityId, dateRange, userId, actions
)

data class AuditSummaryResponse(
    // TODO: Add audit summary statistics
    // changeCount, userActivity, entityActivity
)
```

---

## 📋 Step 6: Implement Service Layer

### 6.1 Create BlogService

```kotlin
// TODO: Create service that demonstrates advanced CRUD and transactions
@Service
@Transactional
class BlogService(
    // TODO: Inject all required repositories and services
    private val postRepository: PostRepository,
    private val authorRepository: AuthorRepository,
    private val categoryRepository: CategoryRepository,
    private val auditService: AuditService
) {
    
    // TODO: Implement createPost method with cascading operations
    @Transactional
    fun createPost(request: CreatePostRequest): PostResponse {
        // TODO: 1. Validate request
        // TODO: 2. Load author and categories
        // TODO: 3. Create post with relationships
        // TODO: 4. Save and audit the operation
        // TODO: 5. Return response DTO
    }
    
    // TODO: Implement updatePost method with partial updates
    @Transactional
    fun updatePost(postId: Long, request: UpdatePostRequest): PostResponse {
        // TODO: 1. Load existing post
        // TODO: 2. Apply updates (only non-null fields)
        // TODO: 3. Audit the changes
        // TODO: 4. Save and return response
    }
    
    // TODO: Implement publishPost method with business logic
    @Transactional
    fun publishPost(postId: Long, request: PublishPostRequest): PostResponse {
        // TODO: 1. Load post and validate it can be published
        // TODO: 2. Update status and publishedAt
        // TODO: 3. Send notifications if requested
        // TODO: 4. Audit the publication
        // TODO: 5. Return updated post
    }
    
    // TODO: Implement deletePost method with soft delete
    @Transactional
    fun deletePost(postId: Long): Boolean {
        // TODO: 1. Load post
        // TODO: 2. Perform soft delete (update status)
        // TODO: 3. Handle cascade operations for comments
        // TODO: 4. Audit the deletion
        // TODO: 5. Return success status
    }
    
    // TODO: Implement getPostWithDetails method optimized for performance
    @Transactional(readOnly = true)
    fun getPostWithDetails(postId: Long): PostDetailsResponse {
        // TODO: 1. Load post with optimized fetching
        // TODO: 2. Increment view count (separate transaction?)
        // TODO: 3. Map to detailed response DTO
    }
    
    // TODO: Implement searchPosts method with flexible criteria
    @Transactional(readOnly = true)
    fun searchPosts(request: PostSearchRequest): List<PostSummaryResponse> {
        // TODO: 1. Build dynamic query based on criteria
        // TODO: 2. Apply pagination and sorting
        // TODO: 3. Map to summary response DTOs
    }
    
    // TODO: Implement bulkUpdatePostStatus method for batch operations
    @Transactional(timeout = 300) // 5 minutes for bulk operations
    fun bulkUpdatePostStatus(postIds: List<Long>, newStatus: PostStatus): BulkUpdateResult {
        // TODO: 1. Process in batches to avoid memory issues
        // TODO: 2. Update status for each batch
        // TODO: 3. Handle errors gracefully
        // TODO: 4. Audit bulk operation
        // TODO: 5. Return results summary
    }
    
    // TODO: Add helper methods for mapping and validation
    
    private fun validatePostRequest(request: CreatePostRequest) {
        // TODO: Implement custom validation logic
    }
    
    private fun mapToPostResponse(post: Post): PostResponse {
        // TODO: Map entity to response DTO
    }
}
```

### 6.2 Create CommentService

```kotlin
// TODO: Create service for comment operations
@Service
@Transactional
class CommentService(
    // TODO: Inject required repositories
) {
    
    // TODO: Implement comment CRUD operations
    // - createComment (with parent relationship for replies)
    // - updateComment (with validation)
    // - deleteComment (cascade to replies)
    // - approveComment (for moderation)
    // - getCommentTree (hierarchical structure)
    
    // TODO: Implement bulk operations
    // - bulkApproveComments
    // - bulkDeleteComments
    // - moderateComments
}
```

### 6.3 Create AuditService

```kotlin
// TODO: Create service for audit trail management
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
class AuditService(
    // TODO: Inject audit repository and object mapper
) {
    
    // TODO: Implement audit methods
    fun <T> auditCreate(entity: T, userId: Long) {
        // TODO: Create audit log for entity creation
    }
    
    fun <T> auditUpdate(oldEntity: T, newEntity: T, userId: Long) {
        // TODO: Create audit log for entity update with differences
    }
    
    fun <T> auditDelete(entity: T, userId: Long) {
        // TODO: Create audit log for entity deletion
    }
    
    fun auditBulkOperation(entityType: String, entityIds: List<Long>, action: AuditAction, userId: Long) {
        // TODO: Create audit log for bulk operations
    }
    
    // TODO: Implement audit history retrieval
    @Transactional(readOnly = true)
    fun getAuditHistory(entityType: String, entityId: Long): List<AuditLogResponse> {
        // TODO: Retrieve and format audit history
    }
    
    // TODO: Implement audit cleanup
    @Transactional
    fun cleanupOldAuditLogs(olderThan: LocalDateTime): Int {
        // TODO: Delete or archive old audit logs
    }
}
```

### 6.4 Create BulkOperationService

```kotlin
// TODO: Create service for bulk operations
@Service
class BulkOperationService(
    // TODO: Inject required repositories and services
) {
    
    // TODO: Implement bulk operations with proper transaction management
    @Transactional(timeout = 600) // 10 minutes for large operations
    fun bulkCreatePosts(requests: List<CreatePostRequest>): BulkCreateResult {
        // TODO: Process posts in batches
        // TODO: Handle errors gracefully
        // TODO: Return detailed results
    }
    
    @Transactional
    fun bulkArchiveOldPosts(olderThan: LocalDateTime): BulkUpdateResult {
        // TODO: Find and archive old posts
        // TODO: Update related entities
        // TODO: Audit the operation
    }
    
    // TODO: Add more bulk operations as needed
}
```

---

## 📋 Step 7: Create REST Controllers

### 7.1 Create BlogController

```kotlin
// TODO: Create REST controller for blog operations
@RestController
@RequestMapping("/api/posts")
class BlogController(
    private val blogService: BlogService
) {
    
    // TODO: Implement CRUD endpoints:
    // POST /api/posts - Create post
    // GET /api/posts/{id} - Get post details
    // PUT /api/posts/{id} - Update post
    // DELETE /api/posts/{id} - Delete post
    // GET /api/posts - Search/list posts with pagination
    
    // TODO: Implement special operations:
    // POST /api/posts/{id}/publish - Publish post
    // POST /api/posts/{id}/archive - Archive post
    // GET /api/posts/{id}/history - Get audit history
    
    // TODO: Implement bulk operations:
    // PUT /api/posts/bulk/status - Bulk status update
    // DELETE /api/posts/bulk - Bulk delete
    
    // TODO: Add proper validation, error handling, and HTTP status codes
    
    // TODO: Example implementation for create:
    @PostMapping
    fun createPost(@Valid @RequestBody request: CreatePostRequest): ResponseEntity<PostResponse> {
        // TODO: Call service and return created post with 201 status
    }
}
```

### 7.2 Create CommentController

```kotlin
// TODO: Create REST controller for comment operations
@RestController
@RequestMapping("/api/comments")
class CommentController(
    private val commentService: CommentService
) {
    
    // TODO: Implement comment endpoints:
    // POST /api/comments - Create comment
    // PUT /api/comments/{id} - Update comment
    // DELETE /api/comments/{id} - Delete comment
    // GET /api/posts/{postId}/comments - Get comments for post
    // GET /api/comments/{id}/replies - Get replies to comment
    
    // TODO: Implement moderation endpoints:
    // POST /api/comments/{id}/approve - Approve comment
    // POST /api/comments/{id}/reject - Reject comment
    // POST /api/comments/bulk/moderate - Bulk moderation
}
```

### 7.3 Create AdminController

```kotlin
// TODO: Create controller for administrative operations
@RestController
@RequestMapping("/api/admin")
class AdminController(
    // TODO: Inject required services
) {
    
    // TODO: Implement admin endpoints:
    // GET /api/admin/audit/{entityType}/{entityId} - Get audit history
    // GET /api/admin/stats - Get system statistics
    // POST /api/admin/cleanup/audit - Cleanup old audit logs
    // POST /api/admin/bulk/archive - Archive old content
    
    // TODO: Add proper authorization (admin only)
}
```

---

## 📋 Step 8: Configuration Classes

### 8.1 Create AuditingConfiguration

```kotlin
// TODO: Create configuration for JPA auditing
@Configuration
// TODO: Add @EnableJpaAuditing annotation
class AuditingConfiguration {
    
    // TODO: Configure AuditorAware bean for tracking current user
    @Bean
    fun auditorProvider(): AuditorAware<Long> {
        // TODO: Return current user ID from security context
        // For workshop, can return fixed user ID
    }
    
    // TODO: Configure date/time provider if needed
}
```

### 8.2 Create DatabaseConfiguration

```kotlin
// TODO: Create additional database configuration if needed
@Configuration
class DatabaseConfiguration {
    
    // TODO: Configure connection pools
    // TODO: Configure query timeout settings
    // TODO: Configure batch processing settings
}
```

---

## 📋 Step 9: Create Comprehensive Tests

### 9.1 Create Entity Tests

```kotlin
// TODO: Create tests for entity relationships and business logic
@DataJpaTest
class EntityRelationshipTest {
    
    @Autowired
    private lateinit var testEntityManager: TestEntityManager
    
    // TODO: Test entity relationships
    @Test
    fun `should maintain author-post relationship`() {
        // TODO: Create author and post, verify relationship
    }
    
    @Test
    fun `should cascade delete comments when post is deleted`() {
        // TODO: Test cascade operations
    }
    
    @Test
    fun `should handle many-to-many category relationships`() {
        // TODO: Test many-to-many operations
    }
}
```

### 9.2 Create Service Tests

```kotlin
// TODO: Create tests for service layer business logic
@SpringBootTest
@Transactional
class BlogServiceTest {
    
    @Autowired
    private lateinit var blogService: BlogService
    
    // TODO: Test CRUD operations
    @Test
    fun `should create post with categories`() {
        // TODO: Test post creation with relationships
    }
    
    @Test
    fun `should handle concurrent post updates`() {
        // TODO: Test optimistic locking
    }
    
    @Test
    fun `should rollback transaction on validation failure`() {
        // TODO: Test transaction rollback scenarios
    }
}
```

### 9.3 Create Transaction Tests

```kotlin
// TODO: Create tests specifically for transaction behavior
@SpringBootTest
class TransactionBehaviorTest {
    
    // TODO: Test transaction propagation
    // TODO: Test rollback scenarios
    // TODO: Test bulk operations
    // TODO: Test timeout behavior
    // TODO: Test isolation levels
}
```

### 9.4 Create Performance Tests

```kotlin
// TODO: Create tests for performance optimization
@SpringBootTest
class PerformanceTest {
    
    // TODO: Test N+1 query prevention
    // TODO: Test bulk operation performance
    // TODO: Test query optimization
    // TODO: Test memory usage in large operations
}
```

---

## 📋 Step 10: Error Handling and Validation

### 10.1 Create Custom Exceptions

```kotlin
// TODO: Create exception hierarchy for blog domain
sealed class BlogException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class PostNotFoundException(postId: Long) : BlogException("Post not found: $postId")
class AuthorNotFoundException(authorId: Long) : BlogException("Author not found: $authorId")
class DuplicatePostException(title: String) : BlogException("Post with title already exists: $title")
class PostValidationException(message: String) : BlogException(message)
class CommentNotAllowedException(message: String) : BlogException(message)
class BulkOperationException(message: String, val failures: List<String>) : BlogException(message)
```

### 10.2 Create Global Exception Handler

```kotlin
// TODO: Create controller advice for exception handling
@ControllerAdvice
class GlobalExceptionHandler {
    
    // TODO: Handle domain exceptions
    @ExceptionHandler(PostNotFoundException::class)
    fun handlePostNotFound(ex: PostNotFoundException): ResponseEntity<ErrorResponse> {
        // TODO: Return 404 with error details
    }
    
    @ExceptionHandler(PostValidationException::class)
    fun handleValidation(ex: PostValidationException): ResponseEntity<ErrorResponse> {
        // TODO: Return 400 with validation details
    }
    
    // TODO: Handle transaction exceptions
    @ExceptionHandler(TransactionSystemException::class)
    fun handleTransactionError(ex: TransactionSystemException): ResponseEntity<ErrorResponse> {
        // TODO: Return 500 with transaction error details
    }
    
    // TODO: Handle bulk operation exceptions
    @ExceptionHandler(BulkOperationException::class)
    fun handleBulkOperation(ex: BulkOperationException): ResponseEntity<ErrorResponse> {
        // TODO: Return detailed bulk operation results
    }
}
```

---

## 📋 Step 11: Sample Data and Demo

### 11.1 Create Data Initialization

```kotlin
// TODO: Create component to initialize sample data
@Component
class DataInitializer : ApplicationRunner {
    
    override fun run(args: ApplicationArguments) {
        // TODO: Create sample authors
        // TODO: Create sample categories
        // TODO: Create sample posts with relationships
        // TODO: Create sample comments
        // TODO: Demonstrate cascade operations
    }
}
```

### 11.2 Create Demo Endpoints

```kotlin
// TODO: Create controller with demo endpoints
@RestController
@RequestMapping("/api/demo")
class DemoController {
    
    // TODO: Add endpoints that demonstrate:
    // - Transaction rollback scenarios
    // - Cascade operations
    // - Bulk operations
    // - Audit trail examples
    // - Performance optimizations
}
```

---

## 🎯 Expected Results

After completing this workshop, you should have:

1. **Complex Entity Model**: Blog system with proper relationships and cascade operations
2. **Advanced CRUD Operations**: Beyond basic save/find with business logic
3. **Transaction Management**: Proper use of @Transactional with different settings
4. **Audit System**: Complete change tracking for all entities
5. **Bulk Operations**: Efficient processing of large datasets
6. **Error Handling**: Comprehensive exception handling and rollback scenarios
7. **Performance Optimization**: Optimized queries and lazy loading strategies
8. **Comprehensive Tests**: Testing both successful operations and failure scenarios

## 🚀 Testing Your Implementation

Run these commands to verify your implementation:

```bash
# Build the project
./gradlew build

# Run tests (including transaction tests)
./gradlew test

# Start the application
./gradlew bootRun

# Test the APIs
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{"title":"My First Post","content":"This is content","authorId":1,"categoryIds":[1,2]}'

# Test bulk operations
curl -X PUT http://localhost:8080/api/posts/bulk/status \
  -H "Content-Type: application/json" \
  -d '{"postIds":[1,2,3],"status":"PUBLISHED"}'

# Check audit history
curl http://localhost:8080/api/admin/audit/Post/1
```

## 💡 Key Learning Points

- **Transaction Boundaries**: Understanding when and how to define transaction boundaries
- **Cascade Operations**: Managing entity lifecycle automatically through relationships
- **Performance Optimization**: Writing efficient queries and avoiding common pitfalls
- **Audit Trails**: Implementing comprehensive change tracking for compliance
- **Error Handling**: Designing robust error handling and rollback strategies
- **Bulk Operations**: Processing large datasets efficiently
- **Testing Strategies**: Testing both successful operations and failure scenarios

Good luck with your implementation! This workshop will give you solid experience with advanced data management patterns used in production applications.