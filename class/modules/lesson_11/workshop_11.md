# 🛠️ Workshop 11: Testing Fundamentals - Comprehensive Testing Strategies

## 🎯 Workshop Objectives

In this hands-on workshop, you will:
1. Master JUnit 5 testing framework with Kotlin best practices
2. Implement comprehensive unit testing for business logic and services
3. Create integration tests with Spring Boot Test framework and test slices
4. Use @DataJpaTest for repository layer testing with proper data management
5. Implement web layer testing with MockMvc for API endpoint validation
6. Test transaction behavior, rollback scenarios, and data consistency
7. Use Testcontainers for real database integration testing
8. Create test data builders and fixtures for maintainable test suites
9. Implement performance testing and load testing strategies
10. Apply Test-Driven Development (TDD) principles in practice

## 🏗️ Project Structure

```
src/main/kotlin/com/learning/testing/
├── TestingApplication.kt
├── config/
│   └── TestingConfiguration.kt
├── model/
│   ├── User.kt
│   ├── Post.kt
│   └── Comment.kt
├── repository/
│   ├── UserRepository.kt
│   ├── PostRepository.kt
│   └── CommentRepository.kt
├── service/
│   ├── UserService.kt
│   ├── PostService.kt
│   └── CommentService.kt
├── controller/
│   ├── UserController.kt
│   └── PostController.kt
└── dto/
    ├── UserDTOs.kt
    └── PostDTOs.kt

src/test/kotlin/com/learning/testing/
├── unit/
│   ├── UserServiceTest.kt
│   ├── PostServiceTest.kt
│   └── ValidationTest.kt
├── integration/
│   ├── UserControllerIntegrationTest.kt
│   └── PostControllerIntegrationTest.kt
├── repository/
│   ├── UserRepositoryTest.kt
│   └── PostRepositoryTest.kt
├── web/
│   ├── UserControllerTest.kt
│   └── PostControllerTest.kt
├── performance/
│   └── PerformanceTest.kt
└── fixtures/
    ├── TestDataBuilder.kt
    └── TestDataFixtures.kt
```

---

## 📋 Step 1: Project Setup and Dependencies

### 1.1 Create build.gradle.kts

```kotlin
// TODO: Add Spring Boot parent and comprehensive testing dependencies

plugins {
    // TODO: Add Kotlin and Spring Boot plugins
}

dependencies {
    // TODO: Add Spring Boot starters
    // - spring-boot-starter-web
    // - spring-boot-starter-data-jpa
    // - spring-boot-starter-validation
    // - spring-boot-starter-security (for security testing)
    
    // TODO: Add Kotlin support
    // - jackson-module-kotlin
    // - kotlin-reflect
    
    // TODO: Add database drivers
    // - H2 for testing
    // - PostgreSQL for production
    
    // TODO: Add comprehensive testing dependencies
    // - spring-boot-starter-test (includes JUnit 5, Mockito, AssertJ)
    // - spring-boot-testcontainers
    // - testcontainers-junit-jupiter
    // - testcontainers-postgresql
    // - mockk (Kotlin mocking library)
    // - kotlin-test-junit5
    
    // TODO: Add additional testing utilities
    // - spring-security-test (for security testing)
    // - testcontainers-r2dbc (if using reactive)
}

tasks.withType<Test> {
    // TODO: Configure JUnit 5
    useJUnitPlatform()
    
    // TODO: Configure test execution
    maxParallelForks = Runtime.getRuntime().availableProcessors()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
```

### 1.2 Create settings.gradle.kts

```kotlin
// TODO: Set the project name
rootProject.name = "lesson-11-testing-fundamentals"
```

---

## 📋 Step 2: Application Configuration

### 2.1 Create application.yml

```yaml
# TODO: Create configuration for testing environment
spring:
  profiles:
    active: dev
    
  # TODO: Configure H2 database for development
  datasource:
    # Add H2 configuration
    
  # TODO: Configure JPA/Hibernate
  jpa:
    # Add JPA settings optimized for testing
    
  # TODO: Configure test-specific settings
  
# TODO: Configure logging for test output
logging:
  level:
    # Add appropriate logging levels

---
# TODO: Test profile configuration
spring:
  config:
    activate:
      on-profile: test
      
  # TODO: Configure test database
  datasource:
    # Add test-specific database configuration
    
  jpa:
    # Add test-specific JPA settings
```

### 2.2 Create TestingConfiguration.kt

```kotlin
// TODO: Create configuration class for testing
@Configuration
class TestingConfiguration {
    
    // TODO: Configure test-specific beans if needed
    // For example: test clock, mock external services
}
```

---

## 📋 Step 3: Entity Models for Testing

### 3.1 Create User Entity

```kotlin
// TODO: Create User entity optimized for testing scenarios
@Entity
@Table(name = "users")
data class User(
    // TODO: Add entity properties
    // id, username, email, firstName, lastName, isActive, createdAt
    
    // TODO: Add relationships for testing
    // posts (One-to-Many)
    
) {
    
    // TODO: Add business logic methods that will be tested
    fun getFullName(): String {
        // TODO: Implement and test this method
    }
    
    fun isEligibleForPosting(): Boolean {
        // TODO: Implement business rule to test
    }
    
    fun getDisplayName(): String {
        // TODO: Implement for testing string manipulation
    }
}
```

### 3.2 Create Post Entity

```kotlin
// TODO: Create Post entity with testable business logic
@Entity
@Table(name = "posts")
data class Post(
    // TODO: Add entity properties
    // id, title, content, status, publishedAt, viewCount, author
    
    // TODO: Add relationships
    // author (Many-to-One)
    // comments (One-to-Many)
    
) {
    
    // TODO: Add business logic methods for testing
    fun isPublished(): Boolean {
        // TODO: Implement status checking logic
    }
    
    fun canBeEditedBy(userId: Long): Boolean {
        // TODO: Implement authorization logic
    }
    
    fun getWordCount(): Int {
        // TODO: Implement text processing logic
    }
    
    fun getReadingTimeMinutes(): Int {
        // TODO: Implement calculation logic
    }
}

// TODO: Create PostStatus enum for testing
enum class PostStatus {
    // TODO: Add status values for different test scenarios
}
```

### 3.3 Create Comment Entity

```kotlin
// TODO: Create Comment entity for relationship testing
@Entity
@Table(name = "comments")
data class Comment(
    // TODO: Add entity properties
    // id, content, author, post, status, createdAt
    
) {
    
    // TODO: Add business logic for testing
    fun isApproved(): Boolean {
        // TODO: Implement status checking
    }
    
    fun canBeDeletedBy(userId: Long): Boolean {
        // TODO: Implement deletion authorization
    }
}

// TODO: Create CommentStatus enum
enum class CommentStatus {
    // TODO: Add status values
}
```

---

## 📋 Step 4: Repository Layer with Test-Friendly Methods

### 4.1 Create UserRepository

```kotlin
// TODO: Create UserRepository with methods to test
interface UserRepository : JpaRepository<User, Long> {
    
    // TODO: Add method name queries to test
    fun findByUsername(username: String): User?
    fun findByEmail(email: String): User?
    fun findByIsActiveTrue(): List<User>
    fun findByUsernameContainingIgnoreCase(username: String): List<User>
    
    // TODO: Add custom JPQL queries to test
    @Query("SELECT u FROM User u WHERE u.createdAt > :date")
    fun findUsersCreatedAfter(@Param("date") date: LocalDateTime): List<User>
    
    @Query("SELECT u FROM User u WHERE SIZE(u.posts) > :count")
    fun findUsersWithMoreThanXPosts(@Param("count") count: Int): List<User>
    
    // TODO: Add update queries to test
    @Modifying
    @Query("UPDATE User u SET u.isActive = :active WHERE u.id = :id")
    fun updateUserStatus(@Param("id") id: Long, @Param("active") active: Boolean): Int
    
    // TODO: Add pagination methods to test
    fun findAllByOrderByUsernameAsc(pageable: Pageable): Page<User>
    
    // TODO: Add native queries to test
    @Query(
        value = "SELECT * FROM users WHERE email LIKE %:domain%",
        nativeQuery = true
    )
    fun findByEmailDomain(@Param("domain") domain: String): List<User>
}
```

### 4.2 Create PostRepository

```kotlin
// TODO: Create PostRepository with complex queries to test
interface PostRepository : JpaRepository<Post, Long> {
    
    // TODO: Add method name queries for testing
    fun findByStatus(status: PostStatus): List<Post>
    fun findByAuthor(author: User): List<Post>
    fun findByTitleContainingIgnoreCase(title: String): List<Post>
    
    // TODO: Add relationship queries to test
    @Query("SELECT p FROM Post p JOIN FETCH p.author WHERE p.status = :status")
    fun findByStatusWithAuthor(@Param("status") status: PostStatus): List<Post>
    
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.comments WHERE p.id = :id")
    fun findByIdWithComments(@Param("id") id: Long): Post?
    
    // TODO: Add aggregation queries to test
    @Query("SELECT COUNT(p) FROM Post p WHERE p.author.id = :authorId AND p.status = :status")
    fun countByAuthorAndStatus(@Param("authorId") authorId: Long, @Param("status") status: PostStatus): Long
    
    // TODO: Add complex queries to test
    @Query("""
        SELECT p FROM Post p 
        WHERE p.status = :status 
        AND p.viewCount > :minViews 
        AND p.publishedAt > :since
    """)
    fun findPopularPostsSince(
        @Param("status") status: PostStatus,
        @Param("minViews") minViews: Long,
        @Param("since") since: LocalDateTime
    ): List<Post>
}
```

### 4.3 Create CommentRepository

```kotlin
// TODO: Create CommentRepository for relationship testing
interface CommentRepository : JpaRepository<Comment, Long> {
    
    // TODO: Add queries to test relationships
    fun findByPost(post: Post): List<Comment>
    fun findByAuthor(author: User): List<Comment>
    fun findByStatus(status: CommentStatus): List<Comment>
    
    // TODO: Add complex relationship queries
    @Query("SELECT c FROM Comment c WHERE c.post.author.id = :authorId")
    fun findCommentsOnAuthorPosts(@Param("authorId") authorId: Long): List<Comment>
}
```

---

## 📋 Step 5: Service Layer with Business Logic

### 5.1 Create UserService

```kotlin
// TODO: Create UserService with comprehensive business logic to test
@Service
@Transactional
class UserService(
    private val userRepository: UserRepository
) {
    
    // TODO: Implement methods with business logic to test
    fun createUser(userData: CreateUserRequest): User {
        // TODO: 1. Validate user data
        validateUserData(userData)
        
        // TODO: 2. Check for duplicates
        checkForDuplicateUser(userData.username, userData.email)
        
        // TODO: 3. Create user entity
        val user = User(
            username = userData.username,
            email = userData.email,
            firstName = userData.firstName,
            lastName = userData.lastName
        )
        
        // TODO: 4. Save user
        val savedUser = userRepository.save(user)
        
        // TODO: 5. Send welcome email (external dependency to mock)
        // emailService.sendWelcomeEmail(savedUser)
        
        return savedUser
    }
    
    @Transactional(readOnly = true)
    fun findByUsername(username: String): User? {
        // TODO: Implement with validation
        if (username.isBlank()) {
            throw IllegalArgumentException("Username cannot be blank")
        }
        return userRepository.findByUsername(username)
    }
    
    fun updateUserStatus(userId: Long, isActive: Boolean): User {
        // TODO: Implement with business rules
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException("User not found: $userId") }
        
        // TODO: Business rule: Cannot deactivate user with active posts
        if (!isActive && hasActivePosts(user)) {
            throw IllegalStateException("Cannot deactivate user with active posts")
        }
        
        userRepository.updateUserStatus(userId, isActive)
        return userRepository.findById(userId).orElseThrow()
    }
    
    @Transactional(readOnly = true)
    fun getUserStatistics(userId: Long): UserStatistics {
        // TODO: Implement statistics calculation
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException("User not found: $userId") }
        
        // TODO: Calculate various statistics
        TODO("Implement getUserStatistics method")
    }
    
    // TODO: Add helper methods to test
    private fun validateUserData(userData: CreateUserRequest) {
        // TODO: Implement validation logic
        if (userData.username.length < 3) {
            throw ValidationException("Username must be at least 3 characters")
        }
        if (!isValidEmail(userData.email)) {
            throw ValidationException("Invalid email format")
        }
    }
    
    private fun checkForDuplicateUser(username: String, email: String) {
        // TODO: Check for existing users
        if (userRepository.findByUsername(username) != null) {
            throw DuplicateUserException("Username already exists: $username")
        }
        if (userRepository.findByEmail(email) != null) {
            throw DuplicateUserException("Email already exists: $email")
        }
    }
    
    private fun hasActivePosts(user: User): Boolean {
        // TODO: Check if user has active posts
        TODO("Implement hasActivePosts method")
    }
    
    private fun isValidEmail(email: String): Boolean {
        // TODO: Implement email validation
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return emailRegex.matches(email)
    }
}

// TODO: Create data classes for service layer
data class CreateUserRequest(
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String
)

data class UserStatistics(
    val totalPosts: Int,
    val publishedPosts: Int,
    val totalViews: Long,
    val joinDate: LocalDateTime
)

// TODO: Create custom exceptions for testing
class UserNotFoundException(message: String) : RuntimeException(message)
class DuplicateUserException(message: String) : RuntimeException(message)
class ValidationException(message: String) : RuntimeException(message)
```

### 5.2 Create PostService

```kotlin
// TODO: Create PostService with complex business logic
@Service
@Transactional
class PostService(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) {
    
    // TODO: Implement methods with transaction testing scenarios
    fun createPost(postData: CreatePostRequest): Post {
        // TODO: 1. Validate post data
        validatePostData(postData)
        
        // TODO: 2. Load author
        val author = userRepository.findById(postData.authorId)
            .orElseThrow { UserNotFoundException("Author not found: ${postData.authorId}") }
        
        // TODO: 3. Check author permissions
        if (!author.isEligibleForPosting()) {
            throw IllegalStateException("Author is not eligible for posting")
        }
        
        // TODO: 4. Create post
        val post = Post(
            title = postData.title,
            content = postData.content,
            author = author,
            status = PostStatus.DRAFT
        )
        
        // TODO: 5. Save post
        return postRepository.save(post)
    }
    
    fun publishPost(postId: Long): Post {
        // TODO: Implement with transaction testing
        val post = postRepository.findById(postId)
            .orElseThrow { PostNotFoundException("Post not found: $postId") }
        
        // TODO: Validate post can be published
        if (!post.canBePublished()) {
            throw IllegalStateException("Post cannot be published in current state")
        }
        
        // TODO: Update post status
        val publishedPost = post.copy(
            status = PostStatus.PUBLISHED,
            publishedAt = LocalDateTime.now()
        )
        
        return postRepository.save(publishedPost)
    }
    
    @Transactional(readOnly = true)
    fun getPostWithComments(postId: Long): PostWithCommentsResponse {
        // TODO: Test optimized loading
        val post = postRepository.findByIdWithComments(postId)
            ?: throw PostNotFoundException("Post not found: $postId")
        
        // TODO: Map to response DTO
        TODO("Implement getPostWithComments method")
    }
    
    fun deletePost(postId: Long, userId: Long): Boolean {
        // TODO: Implement with authorization testing
        val post = postRepository.findById(postId)
            .orElseThrow { PostNotFoundException("Post not found: $postId") }
        
        if (!post.canBeEditedBy(userId)) {
            throw IllegalStateException("User not authorized to delete this post")
        }
        
        postRepository.deleteById(postId)
        return true
    }
    
    // TODO: Add batch operations for testing
    @Transactional(timeout = 60)
    fun bulkPublishPosts(postIds: List<Long>): BulkOperationResult {
        // TODO: Implement bulk operation with error handling
        var successCount = 0
        val errors = mutableListOf<String>()
        
        postIds.forEach { postId ->
            try {
                publishPost(postId)
                successCount++
            } catch (e: Exception) {
                errors.add("Failed to publish post $postId: ${e.message}")
            }
        }
        
        return BulkOperationResult(
            totalProcessed = postIds.size,
            successCount = successCount,
            errors = errors
        )
    }
    
    // TODO: Add helper methods for testing
    private fun validatePostData(postData: CreatePostRequest) {
        if (postData.title.isBlank()) {
            throw ValidationException("Title cannot be blank")
        }
        if (postData.content.length < 10) {
            throw ValidationException("Content must be at least 10 characters")
        }
    }
}

// TODO: Create DTOs and result classes
data class CreatePostRequest(
    val title: String,
    val content: String,
    val authorId: Long
)

data class PostWithCommentsResponse(
    val post: Post,
    val comments: List<Comment>
)

data class BulkOperationResult(
    val totalProcessed: Int,
    val successCount: Int,
    val errors: List<String>
)

class PostNotFoundException(message: String) : RuntimeException(message)
```

---

## 📋 Step 6: REST Controllers for Web Testing

### 6.1 Create UserController

```kotlin
// TODO: Create UserController with comprehensive endpoints for testing
@RestController
@RequestMapping("/api/users")
@Validated
class UserController(
    private val userService: UserService
) {
    
    // TODO: Implement endpoints with various validation scenarios
    @PostMapping
    fun createUser(@Valid @RequestBody request: CreateUserRequest): ResponseEntity<UserResponse> {
        // TODO: Call service and return created user
        val user = userService.createUser(request)
        val response = UserResponse.from(user)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
    
    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long): ResponseEntity<UserResponse> {
        // TODO: Get user and return response
        val user = userService.findById(id)
            ?: return ResponseEntity.notFound().build()
        
        return ResponseEntity.ok(UserResponse.from(user))
    }
    
    @GetMapping
    fun getUsers(
        @RequestParam(required = false) username: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<UserResponse>> {
        // TODO: Implement with pagination testing
        TODO("Implement getUsers method")
    }
    
    @PutMapping("/{id}/status")
    fun updateUserStatus(
        @PathVariable id: Long,
        @RequestBody request: UpdateStatusRequest
    ): ResponseEntity<UserResponse> {
        // TODO: Update status and return user
        val user = userService.updateUserStatus(id, request.isActive)
        return ResponseEntity.ok(UserResponse.from(user))
    }
    
    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long): ResponseEntity<Void> {
        // TODO: Delete user
        userService.deleteUser(id)
        return ResponseEntity.noContent().build()
    }
    
    @GetMapping("/{id}/statistics")
    fun getUserStatistics(@PathVariable id: Long): ResponseEntity<UserStatistics> {
        // TODO: Get user statistics
        val statistics = userService.getUserStatistics(id)
        return ResponseEntity.ok(statistics)
    }
}

// TODO: Create response DTOs
data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val isActive: Boolean,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(user: User): UserResponse {
            return UserResponse(
                id = user.id!!,
                username = user.username,
                email = user.email,
                firstName = user.firstName,
                lastName = user.lastName,
                isActive = user.isActive,
                createdAt = user.createdAt
            )
        }
    }
}

data class UpdateStatusRequest(
    val isActive: Boolean
)
```

### 6.2 Create PostController

```kotlin
// TODO: Create PostController for web layer testing
@RestController
@RequestMapping("/api/posts")
class PostController(
    private val postService: PostService
) {
    
    // TODO: Implement endpoints for testing various scenarios
    @PostMapping
    fun createPost(@Valid @RequestBody request: CreatePostRequest): ResponseEntity<PostResponse> {
        // TODO: Create post and return response
        TODO("Implement createPost endpoint")
    }
    
    @GetMapping("/{id}")
    fun getPost(@PathVariable id: Long): ResponseEntity<PostResponse> {
        // TODO: Get post and return response
        TODO("Implement getPost endpoint")
    }
    
    @PutMapping("/{id}/publish")
    fun publishPost(@PathVariable id: Long): ResponseEntity<PostResponse> {
        // TODO: Publish post and return response
        TODO("Implement publishPost endpoint")
    }
    
    @DeleteMapping("/{id}")
    fun deletePost(
        @PathVariable id: Long,
        @RequestParam userId: Long
    ): ResponseEntity<Void> {
        // TODO: Delete post with authorization
        TODO("Implement deletePost endpoint")
    }
    
    @PostMapping("/bulk/publish")
    fun bulkPublishPosts(@RequestBody request: BulkPublishRequest): ResponseEntity<BulkOperationResult> {
        // TODO: Bulk publish posts
        TODO("Implement bulkPublishPosts endpoint")
    }
}

// TODO: Create DTOs
data class PostResponse(
    val id: Long,
    val title: String,
    val content: String,
    val status: PostStatus,
    val author: UserResponse,
    val createdAt: LocalDateTime,
    val publishedAt: LocalDateTime?
)

data class BulkPublishRequest(
    val postIds: List<Long>
)
```

---

## 📋 Step 7: Comprehensive Test Implementation

### 7.1 Create Unit Tests

```kotlin
// TODO: Create comprehensive unit tests for UserService
class UserServiceTest {
    
    // TODO: Set up mocks and test subject
    private val userRepository = mockk<UserRepository>()
    private val userService = UserService(userRepository)
    
    @BeforeEach
    fun setUp() {
        // TODO: Reset mocks before each test
        clearAllMocks()
    }
    
    @Nested
    @DisplayName("When creating users")
    inner class WhenCreatingUsers {
        
        @Test
        @DisplayName("Should create user with valid data")
        fun shouldCreateUserWithValidData() {
            // TODO: Given
            val request = CreateUserRequest(
                username = "testuser",
                email = "test@example.com",
                firstName = "Test",
                lastName = "User"
            )
            
            val expectedUser = User(
                id = 1L,
                username = "testuser",
                email = "test@example.com",
                firstName = "Test",
                lastName = "User"
            )
            
            // TODO: Mock repository calls
            every { userRepository.findByUsername("testuser") } returns null
            every { userRepository.findByEmail("test@example.com") } returns null
            every { userRepository.save(any()) } returns expectedUser
            
            // TODO: When
            val result = userService.createUser(request)
            
            // TODO: Then
            assertThat(result).isNotNull
            assertThat(result.username).isEqualTo("testuser")
            assertThat(result.email).isEqualTo("test@example.com")
            
            // TODO: Verify interactions
            verify { userRepository.findByUsername("testuser") }
            verify { userRepository.findByEmail("test@example.com") }
            verify { userRepository.save(any()) }
        }
        
        @Test
        @DisplayName("Should throw exception for duplicate username")
        fun shouldThrowExceptionForDuplicateUsername() {
            // TODO: Test duplicate username scenario
            TODO("Implement duplicate username test")
        }
        
        @ParameterizedTest
        @ValueSource(strings = ["", " ", "ab"])
        @DisplayName("Should reject invalid usernames")
        fun shouldRejectInvalidUsernames(invalidUsername: String) {
            // TODO: Test validation
            val request = CreateUserRequest(
                username = invalidUsername,
                email = "test@example.com",
                firstName = "Test",
                lastName = "User"
            )
            
            assertThrows<ValidationException> {
                userService.createUser(request)
            }
        }
    }
    
    @Nested
    @DisplayName("When updating user status")
    inner class WhenUpdatingUserStatus {
        
        @Test
        @DisplayName("Should update status for valid user")
        fun shouldUpdateStatusForValidUser() {
            // TODO: Implement status update test
            TODO("Implement status update test")
        }
        
        @Test
        @DisplayName("Should throw exception for user with active posts")
        fun shouldThrowExceptionForUserWithActivePosts() {
            // TODO: Test business rule validation
            TODO("Implement business rule test")
        }
    }
}
```

### 7.2 Create Repository Tests

```kotlin
// TODO: Create comprehensive repository tests
@DataJpaTest
class UserRepositoryTest {
    
    @Autowired
    private lateinit var testEntityManager: TestEntityManager
    
    @Autowired
    private lateinit var userRepository: UserRepository
    
    @BeforeEach
    fun setUp() {
        // TODO: Set up test data
    }
    
    @Test
    @DisplayName("Should find user by username")
    fun shouldFindUserByUsername() {
        // TODO: Given
        val user = User(
            username = "testuser",
            email = "test@example.com",
            firstName = "Test",
            lastName = "User"
        )
        testEntityManager.persistAndFlush(user)
        
        // TODO: When
        val found = userRepository.findByUsername("testuser")
        
        // TODO: Then
        assertThat(found).isNotNull
        assertThat(found!!.email).isEqualTo("test@example.com")
    }
    
    @Test
    @DisplayName("Should find users created after date")
    fun shouldFindUsersCreatedAfterDate() {
        // TODO: Test custom JPQL query
        TODO("Implement date query test")
    }
    
    @Test
    @DisplayName("Should update user status")
    fun shouldUpdateUserStatus() {
        // TODO: Test update query
        TODO("Implement update query test")
    }
    
    @Test
    @DisplayName("Should find users with pagination")
    fun shouldFindUsersWithPagination() {
        // TODO: Test pagination
        TODO("Implement pagination test")
    }
}
```

### 7.3 Create Web Layer Tests

```kotlin
// TODO: Create MockMvc tests for UserController
@WebMvcTest(UserController::class)
class UserControllerTest {
    
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @MockBean
    private lateinit var userService: UserService
    
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    
    @Test
    @DisplayName("Should create user successfully")
    fun shouldCreateUserSuccessfully() {
        // TODO: Given
        val request = CreateUserRequest(
            username = "testuser",
            email = "test@example.com",
            firstName = "Test",
            lastName = "User"
        )
        
        val createdUser = User(
            id = 1L,
            username = "testuser",
            email = "test@example.com",
            firstName = "Test",
            lastName = "User"
        )
        
        every { userService.createUser(any()) } returns createdUser
        
        // TODO: When & Then
        mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.username").value("testuser"))
            .andExpected(jsonPath("$.email").value("test@example.com"))
        
        verify { userService.createUser(any()) }
    }
    
    @Test
    @DisplayName("Should handle validation errors")
    fun shouldHandleValidationErrors() {
        // TODO: Test validation error handling
        TODO("Implement validation error test")
    }
    
    @Test
    @DisplayName("Should handle service exceptions")
    fun shouldHandleServiceExceptions() {
        // TODO: Test exception handling
        TODO("Implement exception handling test")
    }
}
```

### 7.4 Create Integration Tests

```kotlin
// TODO: Create full integration tests
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class UserControllerIntegrationTest {
    
    @Autowired
    private lateinit var testRestTemplate: TestRestTemplate
    
    @Autowired
    private lateinit var userRepository: UserRepository
    
    @LocalServerPort
    private var port: Int = 0
    
    @Test
    @DisplayName("Should create user via REST API")
    fun shouldCreateUserViaRestApi() {
        // TODO: Test full stack integration
        TODO("Implement integration test")
    }
    
    @Test
    @DisplayName("Should handle database constraints")
    fun shouldHandleDatabaseConstraints() {
        // TODO: Test database constraint violations
        TODO("Implement constraint test")
    }
}
```

### 7.5 Create Transaction Tests

```kotlin
// TODO: Create transaction behavior tests
@SpringBootTest
@Transactional
class TransactionBehaviorTest {
    
    @Autowired
    private lateinit var userService: UserService
    
    @Autowired
    private lateinit var postService: PostService
    
    @Test
    @Rollback(false)
    @DisplayName("Should commit transaction on success")
    fun shouldCommitTransactionOnSuccess() {
        // TODO: Test successful transaction commit
        TODO("Implement transaction commit test")
    }
    
    @Test
    @DisplayName("Should rollback transaction on exception")
    fun shouldRollbackTransactionOnException() {
        // TODO: Test transaction rollback
        TODO("Implement transaction rollback test")
    }
    
    @Test
    @DisplayName("Should test transaction propagation")
    fun shouldTestTransactionPropagation() {
        // TODO: Test transaction propagation
        TODO("Implement propagation test")
    }
}
```

### 7.6 Create Testcontainers Tests

```kotlin
// TODO: Create Testcontainers integration tests
@SpringBootTest
@Testcontainers
class DatabaseIntegrationTest {
    
    companion object {
        @Container
        @JvmStatic
        val postgresContainer = PostgreSQLContainer<Nothing>("postgres:15").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }
        
        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgresContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgresContainer::getUsername)
            registry.add("spring.datasource.password", postgresContainer::getPassword)
        }
    }
    
    @Autowired
    private lateinit var userRepository: UserRepository
    
    @Test
    @DisplayName("Should work with real PostgreSQL database")
    fun shouldWorkWithRealPostgreSQLDatabase() {
        // TODO: Test with real database
        TODO("Implement real database test")
    }
}
```

### 7.7 Create Performance Tests

```kotlin
// TODO: Create performance tests
@SpringBootTest
class PerformanceTest {
    
    @Autowired
    private lateinit var userService: UserService
    
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @DisplayName("Should handle bulk operations within time limit")
    fun shouldHandleBulkOperationsWithinTimeLimit() {
        // TODO: Test performance requirements
        TODO("Implement performance test")
    }
    
    @RepeatedTest(10)
    @DisplayName("Should consistently perform user lookup")
    fun shouldConsistentlyPerformUserLookup() {
        // TODO: Test consistency
        TODO("Implement consistency test")
    }
}
```

---

## 📋 Step 8: Test Data Builders and Fixtures

### 8.1 Create Test Data Builders

```kotlin
// TODO: Create test data builders for maintainable test data
class UserTestDataBuilder {
    
    private var id: Long? = null
    private var username: String = "testuser"
    private var email: String = "test@example.com"
    private var firstName: String = "Test"
    private var lastName: String = "User"
    private var isActive: Boolean = true
    private var createdAt: LocalDateTime = LocalDateTime.now()
    
    // TODO: Add fluent builder methods
    fun withId(id: Long) = apply { this.id = id }
    fun withUsername(username: String) = apply { this.username = username }
    fun withEmail(email: String) = apply { this.email = email }
    fun withName(firstName: String, lastName: String) = apply {
        this.firstName = firstName
        this.lastName = lastName
    }
    fun inactive() = apply { this.isActive = false }
    fun createdAt(createdAt: LocalDateTime) = apply { this.createdAt = createdAt }
    
    // TODO: Build method
    fun build() = User(
        id = id,
        username = username,
        email = email,
        firstName = firstName,
        lastName = lastName,
        isActive = isActive,
        createdAt = createdAt
    )
    
    companion object {
        fun aUser() = UserTestDataBuilder()
        fun anActiveUser() = UserTestDataBuilder().apply { isActive = true }
        fun anInactiveUser() = UserTestDataBuilder().inactive()
    }
}

// TODO: Create PostTestDataBuilder
class PostTestDataBuilder {
    // TODO: Implement post builder similar to user builder
}
```

### 8.2 Create Test Fixtures

```kotlin
// TODO: Create test fixtures component
@Component
class TestDataFixtures {
    
    fun createTestUser(
        username: String = "testuser",
        email: String = "test@example.com"
    ): User {
        // TODO: Create test user with defaults
        TODO("Implement createTestUser")
    }
    
    fun createTestPost(
        author: User,
        title: String = "Test Post"
    ): Post {
        // TODO: Create test post
        TODO("Implement createTestPost")
    }
    
    fun createUserWithPosts(
        username: String = "testuser",
        postCount: Int = 3
    ): User {
        // TODO: Create user with posts
        TODO("Implement createUserWithPosts")
    }
}
```

---

## 📋 Step 9: Test Configuration and Utilities

### 9.1 Create Test Configuration

```kotlin
// TODO: Create test-specific configuration
@TestConfiguration
class TestConfig {
    
    @Bean
    @Primary
    fun testClock(): Clock {
        // TODO: Provide fixed clock for testing
        return Clock.fixed(
            Instant.parse("2023-01-01T00:00:00Z"),
            ZoneOffset.UTC
        )
    }
    
    @Bean
    @Primary
    fun mockEmailService(): EmailService {
        // TODO: Provide mock email service
        return mockk<EmailService>(relaxed = true)
    }
}
```

### 9.2 Create Test Utilities

```kotlin
// TODO: Create test utility functions
object TestUtils {
    
    fun createValidUser(): User {
        // TODO: Create valid user for testing
        TODO("Implement createValidUser")
    }
    
    fun assertUserEquals(expected: User, actual: User) {
        // TODO: Custom assertion for user equality
        TODO("Implement assertUserEquals")
    }
    
    fun <T> captureArgument(mockCall: () -> T): T {
        // TODO: Utility for capturing mock arguments
        TODO("Implement captureArgument")
    }
}
```

---

## 🎯 Expected Results

After completing this workshop, you should have:

1. **Comprehensive Test Suite**: Unit, integration, repository, and web tests
2. **Test Organization**: Well-structured test packages and naming conventions
3. **Mock Strategy**: Effective use of mocks and test doubles
4. **Data Management**: Test data builders and fixtures for maintainable tests
5. **Transaction Testing**: Verification of transaction behavior and rollback
6. **Performance Testing**: Load testing and benchmark verification
7. **Real Database Testing**: Testcontainers integration for realistic testing
8. **TDD Practice**: Test-driven development examples and patterns

## 🚀 Testing Your Implementation

Run these commands to verify your testing implementation:

```bash
# Run all tests
./gradlew test

# Run specific test categories
./gradlew test --tests "*Unit*"
./gradlew test --tests "*Integration*"
./gradlew test --tests "*Repository*"

# Run tests with coverage
./gradlew test jacocoTestReport

# Run performance tests
./gradlew test --tests "*Performance*"

# Run with different profiles
./gradlew test -Dspring.profiles.active=test
```

## 💡 Key Learning Points

- **Test Pyramid**: Balance unit, integration, and E2E tests appropriately
- **Test Independence**: Each test should be independent and repeatable
- **Fast Feedback**: Optimize test execution time for rapid development cycles
- **Realistic Testing**: Use Testcontainers for integration testing with real databases
- **Mock Strategy**: Mock external dependencies while testing real business logic
- **Data Management**: Use builders and fixtures for maintainable test data
- **Transaction Testing**: Verify transactional behavior and error scenarios
- **TDD Practice**: Write tests first to drive better design and coverage

Good luck building a comprehensive test suite that ensures your Spring Boot application is reliable and maintainable!