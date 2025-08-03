# 📚 Lesson 11: Testing Fundamentals - Comprehensive Testing Strategies

## 🎯 Learning Objectives

By the end of this lesson, you will be able to:
- Master JUnit 5 testing framework with Kotlin best practices
- Implement comprehensive unit testing strategies for business logic
- Create integration tests with Spring Boot Test framework
- Use test slices (@DataJpaTest, @WebMvcTest) for focused testing
- Test repository layers with proper data setup and cleanup
- Mock dependencies effectively with Mockito and Spring Boot
- Test web layers with MockMvc for API endpoint validation
- Implement transaction testing and rollback scenarios
- Use Testcontainers for real database integration testing
- Create test data builders and fixtures for maintainable tests
- Implement performance testing for critical application paths
- Design test-driven development (TDD) workflows

## 🏗️ Testing Strategy Overview

This lesson demonstrates **comprehensive testing patterns** for enterprise Spring Boot applications:

- **Unit Testing**: Fast, isolated tests for business logic and individual components
- **Integration Testing**: End-to-end testing with Spring application context
- **Repository Testing**: Data layer testing with @DataJpaTest and test databases
- **Web Layer Testing**: API testing with MockMvc and @WebMvcTest
- **Transaction Testing**: Testing transaction boundaries and rollback scenarios
- **Performance Testing**: Load testing and benchmarking critical paths
- **Test Data Management**: Builders, fixtures, and data cleanup strategies

## 📊 Testing Pyramid

### Test Types and Strategy

```
           /\
          /  \
         / UI \
        /______\
       /        \
      /Integration\
     /__________\
    /            \
   /   Unit Tests  \
  /________________\
```

**Unit Tests (70%)**:
- Fast execution (< 1ms per test)
- No external dependencies
- Test individual methods and classes
- High test coverage and fast feedback

**Integration Tests (20%)**:
- Test component interactions
- Use Spring test context
- Test with real Spring configurations
- Medium execution time (< 1s per test)

**End-to-End Tests (10%)**:
- Test complete user workflows
- Full application stack testing
- Slower execution but high confidence
- Test critical business scenarios

## 🧪 JUnit 5 Fundamentals

### Test Structure and Annotations

```kotlin
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class UserServiceTest {
    
    companion object {
        @JvmStatic
        @BeforeAll
        fun setUpClass() {
            // Setup shared resources for all tests
        }
        
        @JvmStatic
        @AfterAll
        fun tearDownClass() {
            // Cleanup shared resources
        }
    }
    
    @BeforeEach
    fun setUp() {
        // Setup before each test
    }
    
    @AfterEach
    fun tearDown() {
        // Cleanup after each test
    }
    
    @Test
    @DisplayName("Should create user with valid data")
    fun shouldCreateUserWithValidData() {
        // Given
        val userData = UserData("john", "john@example.com")
        
        // When
        val result = userService.createUser(userData)
        
        // Then
        assertAll(
            { assertNotNull(result.id) },
            { assertEquals("john", result.username) },
            { assertEquals("john@example.com", result.email) },
            { assertTrue(result.isActive) }
        )
    }
    
    @ParameterizedTest
    @ValueSource(strings = ["", " ", "ab", "a".repeat(51)])
    @DisplayName("Should reject invalid usernames")
    fun shouldRejectInvalidUsernames(invalidUsername: String) {
        val userData = UserData(invalidUsername, "test@example.com")
        
        assertThrows<ValidationException> {
            userService.createUser(userData)
        }
    }
    
    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    @DisplayName("Should complete operation within time limit")
    fun shouldCompleteWithinTimeLimit() {
        val result = userService.performExpensiveOperation()
        assertNotNull(result)
    }
}
```

### Advanced JUnit 5 Features

```kotlin
// Nested test classes for organization
@DisplayName("User Service Tests")
class UserServiceTest {
    
    @Nested
    @DisplayName("When creating users")
    inner class WhenCreatingUsers {
        
        @Test
        @DisplayName("Should succeed with valid data")
        fun shouldSucceedWithValidData() {
            // Test implementation
        }
        
        @Test
        @DisplayName("Should fail with invalid email")
        fun shouldFailWithInvalidEmail() {
            // Test implementation
        }
    }
    
    @Nested
    @DisplayName("When updating users")
    inner class WhenUpdatingUsers {
        
        @Test
        @DisplayName("Should update existing user")
        fun shouldUpdateExistingUser() {
            // Test implementation
        }
    }
}

// Dynamic tests
@TestFactory
fun dynamicUserValidationTests(): Collection<DynamicTest> {
    val testCases = listOf(
        TestCase("john", "john@example.com", true),
        TestCase("", "test@example.com", false),
        TestCase("user", "invalid-email", false)
    )
    
    return testCases.map { testCase ->
        DynamicTest.dynamicTest("Testing ${testCase.username}") {
            val isValid = userValidator.isValid(testCase.username, testCase.email)
            assertEquals(testCase.expectedValid, isValid)
        }
    }
}
```

## 🔧 Unit Testing Strategies

### Testing Business Logic

```kotlin
class OrderServiceTest {
    
    private val orderRepository = mockk<OrderRepository>()
    private val paymentService = mockk<PaymentService>()
    private val inventoryService = mockk<InventoryService>()
    private val orderService = OrderService(orderRepository, paymentService, inventoryService)
    
    @Test
    fun `should create order successfully`() {
        // Given
        val orderRequest = OrderRequest(
            userId = 1L,
            items = listOf(OrderItem(productId = 1L, quantity = 2))
        )
        
        every { inventoryService.checkAvailability(1L, 2) } returns true
        every { paymentService.processPayment(any()) } returns PaymentResult.Success("payment-123")
        every { orderRepository.save(any()) } returns mockOrder()
        
        // When
        val result = orderService.createOrder(orderRequest)
        
        // Then
        assertThat(result).isNotNull
        assertThat(result.status).isEqualTo(OrderStatus.CONFIRMED)
        
        verify { inventoryService.checkAvailability(1L, 2) }
        verify { paymentService.processPayment(any()) }
        verify { orderRepository.save(any()) }
    }
    
    @Test
    fun `should fail when insufficient inventory`() {
        // Given
        val orderRequest = OrderRequest(
            userId = 1L,
            items = listOf(OrderItem(productId = 1L, quantity = 10))
        )
        
        every { inventoryService.checkAvailability(1L, 10) } returns false
        
        // When & Then
        assertThrows<InsufficientInventoryException> {
            orderService.createOrder(orderRequest)
        }
        
        verify { inventoryService.checkAvailability(1L, 10) }
        verify(exactly = 0) { paymentService.processPayment(any()) }
        verify(exactly = 0) { orderRepository.save(any()) }
    }
}
```

### Testing Kotlin-Specific Features

```kotlin
class KotlinSpecificTest {
    
    @Test
    fun `should test data class equality`() {
        val user1 = User(id = 1L, name = "John", email = "john@example.com")
        val user2 = User(id = 1L, name = "John", email = "john@example.com")
        val user3 = user1.copy(name = "Jane")
        
        assertThat(user1).isEqualTo(user2)
        assertThat(user1).isNotEqualTo(user3)
        assertThat(user3.name).isEqualTo("Jane")
    }
    
    @Test
    fun `should test nullable handling`() {
        val service = UserService()
        
        // Test null handling
        assertThat(service.findUser(999L)).isNull()
        
        // Test nullable return
        val result: User? = service.findUser(1L)
        assertThat(result).isNotNull
        assertThat(result!!.name).isNotEmpty()
    }
    
    @Test
    fun `should test extension functions`() {
        val email = "john@example.com"
        
        assertThat(email.isValidEmail()).isTrue()
        assertThat("invalid-email".isValidEmail()).isFalse()
        assertThat("".isValidEmail()).isFalse()
    }
    
    @Test
    fun `should test sealed classes`() {
        val results = listOf(
            Result.Success("data"),
            Result.Error("error message"),
            Result.Loading
        )
        
        results.forEach { result ->
            when (result) {
                is Result.Success -> assertThat(result.data).isNotEmpty()
                is Result.Error -> assertThat(result.message).isNotEmpty()
                is Result.Loading -> assertThat(result).isNotNull
            }
        }
    }
}
```

## 🏢 Spring Boot Integration Testing

### @SpringBootTest Configuration

```kotlin
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
    ]
)
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
    fun `should create user via REST API`() {
        // Given
        val createRequest = CreateUserRequest(
            username = "testuser",
            email = "test@example.com",
            firstName = "Test",
            lastName = "User"
        )
        
        // When
        val response = testRestTemplate.postForEntity(
            "/api/users",
            createRequest,
            UserResponse::class.java
        )
        
        // Then
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body).isNotNull
        assertThat(response.body!!.username).isEqualTo("testuser")
        
        // Verify in database
        val savedUser = userRepository.findByUsername("testuser")
        assertThat(savedUser).isNotNull
    }
    
    @Test
    @Rollback
    fun `should handle validation errors`() {
        // Given
        val invalidRequest = CreateUserRequest(
            username = "",  // Invalid
            email = "invalid-email",  // Invalid
            firstName = "Test",
            lastName = "User"
        )
        
        // When
        val response = testRestTemplate.postForEntity(
            "/api/users",
            invalidRequest,
            ErrorResponse::class.java
        )
        
        // Then
        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body!!.errors).hasSizeGreaterThan(0)
    }
}
```

### Custom Test Configuration

```kotlin
@TestConfiguration
class TestConfig {
    
    @Bean
    @Primary
    fun mockEmailService(): EmailService {
        return mockk<EmailService>(relaxed = true)
    }
    
    @Bean
    @Primary
    fun testClock(): Clock {
        return Clock.fixed(
            Instant.parse("2023-01-01T00:00:00Z"),
            ZoneOffset.UTC
        )
    }
}

@SpringBootTest
@Import(TestConfig::class)
class ServiceWithMocksTest {
    
    @Autowired
    private lateinit var userService: UserService
    
    @Autowired
    private lateinit var emailService: EmailService
    
    @Test
    fun `should use mocked email service`() {
        // Given
        val userData = UserData("john", "john@example.com")
        
        // When
        userService.createUser(userData)
        
        // Then
        verify { emailService.sendWelcomeEmail(any()) }
    }
}
```

## 🗄️ Repository Testing with @DataJpaTest

### Repository Layer Testing

```kotlin
@DataJpaTest
class UserRepositoryTest {
    
    @Autowired
    private lateinit var testEntityManager: TestEntityManager
    
    @Autowired
    private lateinit var userRepository: UserRepository
    
    @Test
    fun `should find user by username`() {
        // Given
        val user = User(
            username = "testuser",
            email = "test@example.com",
            firstName = "Test",
            lastName = "User"
        )
        testEntityManager.persistAndFlush(user)
        
        // When
        val found = userRepository.findByUsername("testuser")
        
        // Then
        assertThat(found).isNotNull
        assertThat(found!!.email).isEqualTo("test@example.com")
    }
    
    @Test
    fun `should find users by email domain`() {
        // Given
        val users = listOf(
            User(username = "user1", email = "user1@company.com", firstName = "User1", lastName = "Last1"),
            User(username = "user2", email = "user2@company.com", firstName = "User2", lastName = "Last2"),
            User(username = "user3", email = "user3@other.com", firstName = "User3", lastName = "Last3")
        )
        users.forEach { testEntityManager.persistAndFlush(it) }
        
        // When
        val companyUsers = userRepository.findByEmailContaining("@company.com")
        
        // Then
        assertThat(companyUsers).hasSize(2)
        assertThat(companyUsers.map { it.username }).containsExactlyInAnyOrder("user1", "user2")
    }
    
    @Test
    fun `should update user status`() {
        // Given
        val user = User(
            username = "testuser",
            email = "test@example.com",
            firstName = "Test",
            lastName = "User",
            isActive = true
        )
        val savedUser = testEntityManager.persistAndFlush(user)
        
        // When
        userRepository.updateUserStatus(savedUser.id!!, false)
        testEntityManager.clear() // Clear persistence context
        
        // Then
        val updatedUser = testEntityManager.find(User::class.java, savedUser.id)
        assertThat(updatedUser.isActive).isFalse()
    }
    
    @Test
    fun `should test custom query with pagination`() {
        // Given
        val users = (1..10).map { i ->
            User(
                username = "user$i",
                email = "user$i@example.com",
                firstName = "User$i",
                lastName = "Last$i"
            )
        }
        users.forEach { testEntityManager.persistAndFlush(it) }
        
        // When
        val pageRequest = PageRequest.of(0, 5, Sort.by("username"))
        val page = userRepository.findAllActiveUsers(pageRequest)
        
        // Then
        assertThat(page.content).hasSize(5)
        assertThat(page.totalElements).isEqualTo(10)
        assertThat(page.totalPages).isEqualTo(2)
    }
}
```

### Testing JPA Relationships

```kotlin
@DataJpaTest
class PostRepositoryTest {
    
    @Autowired
    private lateinit var testEntityManager: TestEntityManager
    
    @Autowired
    private lateinit var postRepository: PostRepository
    
    @Test
    fun `should load posts with comments using fetch join`() {
        // Given
        val author = createTestAuthor()
        val post = createTestPost(author)
        val comments = createTestComments(post, 3)
        
        // Persist all entities
        testEntityManager.persistAndFlush(author)
        testEntityManager.persistAndFlush(post)
        comments.forEach { testEntityManager.persistAndFlush(it) }
        testEntityManager.clear()
        
        // When
        val postsWithComments = postRepository.findAllWithComments()
        
        // Then
        assertThat(postsWithComments).hasSize(1)
        val loadedPost = postsWithComments[0]
        assertThat(loadedPost.comments).hasSize(3)
        
        // Verify no additional queries are executed
        // (This would be verified with query count monitoring in real tests)
    }
    
    @Test
    fun `should cascade delete comments when post is deleted`() {
        // Given
        val author = createTestAuthor()
        val post = createTestPost(author)
        val comments = createTestComments(post, 2)
        
        testEntityManager.persistAndFlush(author)
        val savedPost = testEntityManager.persistAndFlush(post)
        comments.forEach { testEntityManager.persistAndFlush(it) }
        
        // When
        postRepository.deleteById(savedPost.id!!)
        testEntityManager.flush()
        
        // Then
        val remainingComments = testEntityManager.entityManager
            .createQuery("SELECT c FROM Comment c WHERE c.post.id = :postId", Comment::class.java)
            .setParameter("postId", savedPost.id)
            .resultList
        
        assertThat(remainingComments).isEmpty()
    }
}
```

## 🌐 Web Layer Testing with @WebMvcTest

### Controller Testing

```kotlin
@WebMvcTest(UserController::class)
class UserControllerTest {
    
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @MockBean
    private lateinit var userService: UserService
    
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    
    @Test
    fun `should create user successfully`() {
        // Given
        val createRequest = CreateUserRequest(
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
        
        // When & Then
        mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.username").value("testuser"))
            .andExpect(jsonPath("$.email").value("test@example.com"))
        
        verify { userService.createUser(any()) }
    }
    
    @Test
    fun `should return validation errors for invalid request`() {
        // Given
        val invalidRequest = CreateUserRequest(
            username = "",  // Invalid
            email = "invalid-email",  // Invalid
            firstName = "Test",
            lastName = "User"
        )
        
        // When & Then
        mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.errors").isArray)
            .andExpect(jsonPath("$.errors", hasSize<Any>(greaterThan(0))))
    }
    
    @Test
    fun `should handle service exceptions`() {
        // Given
        val createRequest = CreateUserRequest(
            username = "existinguser",
            email = "existing@example.com",
            firstName = "Test",
            lastName = "User"
        )
        
        every { userService.createUser(any()) } throws DuplicateUserException("Username already exists")
        
        // When & Then
        mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.error").value("DUPLICATE_USER"))
            .andExpect(jsonPath("$.message").value("Username already exists"))
    }
    
    @Test
    fun `should get user by id`() {
        // Given
        val userId = 1L
        val user = User(
            id = userId,
            username = "testuser",
            email = "test@example.com",
            firstName = "Test",
            lastName = "User"
        )
        
        every { userService.findById(userId) } returns user
        
        // When & Then
        mockMvc.perform(get("/api/users/{id}", userId))
            .andExpect(status().isOk)
            .andExpected(jsonPath("$.id").value(userId))
            .andExpect(jsonPath("$.username").value("testuser"))
    }
    
    @Test
    fun `should return 404 for non-existent user`() {
        // Given
        val userId = 999L
        every { userService.findById(userId) } throws UserNotFoundException("User not found")
        
        // When & Then
        mockMvc.perform(get("/api/users/{id}", userId))
            .andExpect(status().isNotFound)
    }
}
```

### Testing Security

```kotlin
@WebMvcTest(SecureController::class)
@WithMockUser(username = "testuser", roles = ["USER"])
class SecureControllerTest {
    
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @MockBean
    private lateinit var secureService: SecureService
    
    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should allow admin access`() {
        every { secureService.getAdminData() } returns AdminData("secret")
        
        mockMvc.perform(get("/api/admin/data"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").value("secret"))
    }
    
    @Test
    @WithMockUser(roles = ["USER"])
    fun `should deny user access to admin endpoint`() {
        mockMvc.perform(get("/api/admin/data"))
            .andExpect(status().isForbidden)
    }
    
    @Test
    @WithAnonymousUser
    fun `should require authentication`() {
        mockMvc.perform(get("/api/secure/data"))
            .andExpect(status().isUnauthorized)
    }
}
```

## 💾 Transaction Testing

### Testing Transaction Boundaries

```kotlin
@SpringBootTest
@Transactional
class TransactionBehaviorTest {
    
    @Autowired
    private lateinit var userService: UserService
    
    @Autowired
    private lateinit var userRepository: UserRepository
    
    @Autowired
    private lateinit var testEntityManager: TestEntityManager
    
    @Test
    @Rollback(false)
    fun `should commit transaction on success`() {
        // Given
        val userData = UserData("testuser", "test@example.com")
        
        // When
        val createdUser = userService.createUser(userData)
        
        // Then
        testEntityManager.flush()
        val savedUser = userRepository.findById(createdUser.id!!)
        assertThat(savedUser).isPresent
    }
    
    @Test
    fun `should rollback transaction on exception`() {
        // Given
        val userData = UserData("", "invalid-email") // Invalid data
        
        // When & Then
        assertThrows<ValidationException> {
            userService.createUser(userData)
        }
        
        // Verify no user was saved
        val userCount = userRepository.count()
        assertThat(userCount).isEqualTo(0)
    }
    
    @Test
    fun `should test transaction propagation`() {
        // Given
        val parentData = UserData("parent", "parent@example.com")
        val childData = UserData("child", "child@example.com")
        
        // When
        userService.createUserWithChild(parentData, childData)
        
        // Then
        assertThat(userRepository.count()).isEqualTo(2)
    }
    
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun `should test without transaction`() {
        // This test runs without a transaction
        val userData = UserData("notransaction", "test@example.com")
        
        // Direct repository call without service transaction
        val user = User(
            username = userData.username,
            email = userData.email,
            firstName = "Test",
            lastName = "User"
        )
        userRepository.save(user)
        
        // Verify immediate persistence
        val found = userRepository.findByUsername("notransaction")
        assertThat(found).isNotNull
    }
}
```

## 🐳 Testcontainers Integration

### Real Database Testing

```kotlin
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
    }
    
    @DynamicPropertySource
    companion object {
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
    fun `should work with real PostgreSQL database`() {
        // Given
        val user = User(
            username = "realdbtest",
            email = "realdb@example.com",
            firstName = "Real",
            lastName = "Database"
        )
        
        // When
        val savedUser = userRepository.save(user)
        
        // Then
        assertThat(savedUser.id).isNotNull
        
        val foundUser = userRepository.findByUsername("realdbtest")
        assertThat(foundUser).isNotNull
        assertThat(foundUser!!.email).isEqualTo("realdb@example.com")
    }
}
```

## 🏗️ Test Data Builders and Fixtures

### Test Data Builder Pattern

```kotlin
class UserTestDataBuilder {
    private var id: Long? = null
    private var username: String = "testuser"
    private var email: String = "test@example.com"
    private var firstName: String = "Test"
    private var lastName: String = "User"
    private var isActive: Boolean = true
    private var createdAt: LocalDateTime = LocalDateTime.now()
    
    fun withId(id: Long) = apply { this.id = id }
    fun withUsername(username: String) = apply { this.username = username }
    fun withEmail(email: String) = apply { this.email = email }
    fun withName(firstName: String, lastName: String) = apply {
        this.firstName = firstName
        this.lastName = lastName
    }
    fun inactive() = apply { this.isActive = false }
    fun createdAt(createdAt: LocalDateTime) = apply { this.createdAt = createdAt }
    
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

// Usage in tests
class UserServiceTest {
    
    @Test
    fun `should handle user creation`() {
        // Given
        val user = UserTestDataBuilder.aUser()
            .withUsername("john")
            .withEmail("john@example.com")
            .withName("John", "Doe")
            .build()
        
        // Test implementation
    }
    
    @Test
    fun `should handle inactive users`() {
        // Given
        val inactiveUser = UserTestDataBuilder.anInactiveUser()
            .withUsername("inactive")
            .build()
        
        // Test implementation
    }
}
```

### Test Fixtures

```kotlin
@Component
class TestDataFixtures {
    
    fun createTestUser(
        username: String = "testuser",
        email: String = "test@example.com"
    ): User {
        return User(
            username = username,
            email = email,
            firstName = "Test",
            lastName = "User"
        )
    }
    
    fun createTestPost(
        author: User,
        title: String = "Test Post",
        status: PostStatus = PostStatus.PUBLISHED
    ): Post {
        return Post(
            title = title,
            content = "Test content for post",
            author = author,
            status = status,
            publishedAt = if (status == PostStatus.PUBLISHED) LocalDateTime.now() else null
        )
    }
    
    fun createTestComment(
        post: Post,
        author: User,
        content: String = "Test comment"
    ): Comment {
        return Comment(
            content = content,
            author = author,
            post = post,
            status = CommentStatus.APPROVED
        )
    }
    
    fun createUserWithPosts(
        username: String = "testuser",
        postCount: Int = 3
    ): User {
        val user = createTestUser(username)
        val posts = (1..postCount).map { i ->
            createTestPost(user, "Post $i")
        }
        return user.copy(posts = posts)
    }
}
```

## ⚡ Performance Testing

### Load Testing with JUnit

```kotlin
@SpringBootTest
class PerformanceTest {
    
    @Autowired
    private lateinit var userService: UserService
    
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    fun `should handle bulk user creation within time limit`() {
        val startTime = System.nanoTime()
        
        // Create 1000 users
        val users = (1..1000).map { i ->
            UserData("user$i", "user$i@example.com")
        }
        
        val createdUsers = userService.createUsers(users)
        
        val duration = Duration.ofNanos(System.nanoTime() - startTime)
        
        assertThat(createdUsers).hasSize(1000)
        assertThat(duration).isLessThan(Duration.ofSeconds(3))
        
        println("Created 1000 users in ${duration.toMillis()}ms")
    }
    
    @RepeatedTest(10)
    fun `should consistently perform user lookup`() {
        // Given
        val user = createTestUser()
        userService.createUser(user)
        
        // When
        val startTime = System.nanoTime()
        val foundUser = userService.findByUsername(user.username)
        val duration = Duration.ofNanos(System.nanoTime() - startTime)
        
        // Then
        assertThat(foundUser).isNotNull
        assertThat(duration).isLessThan(Duration.ofMillis(100))
    }
}
```

## 🧪 Test-Driven Development (TDD)

### TDD Workflow Example

```kotlin
// Step 1: Write failing test
class UserValidatorTest {
    
    private val userValidator = UserValidator()
    
    @Test
    fun `should validate email format`() {
        // Red: This test will fail initially
        assertThat(userValidator.isValidEmail("test@example.com")).isTrue()
        assertThat(userValidator.isValidEmail("invalid-email")).isFalse()
    }
}

// Step 2: Write minimal implementation to make test pass
class UserValidator {
    fun isValidEmail(email: String): Boolean {
        // Green: Simple implementation to make test pass
        return email.contains("@")
    }
}

// Step 3: Refactor with additional test cases
class UserValidatorTest {
    
    @ParameterizedTest
    @ValueSource(strings = [
        "test@example.com",
        "user.name@domain.org",
        "user+tag@example.co.uk"
    ])
    fun `should accept valid emails`(email: String) {
        assertThat(userValidator.isValidEmail(email)).isTrue()
    }
    
    @ParameterizedTest
    @ValueSource(strings = [
        "invalid-email",
        "@example.com",
        "test@",
        "test..test@example.com"
    ])
    fun `should reject invalid emails`(email: String) {
        assertThat(userValidator.isValidEmail(email)).isFalse()
    }
}

// Step 4: Refactor implementation
class UserValidator {
    private val emailRegex = Regex(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    )
    
    fun isValidEmail(email: String): Boolean {
        return emailRegex.matches(email)
    }
}
```

## 🎯 Key Takeaways

1. **Test Pyramid**: Focus on unit tests (70%), integration tests (20%), E2E tests (10%)
2. **Test Independence**: Each test should be independent and repeatable
3. **Fast Feedback**: Unit tests should execute quickly (< 1ms per test)
4. **Real Databases**: Use Testcontainers for integration testing with real databases
5. **Test Data**: Use builders and fixtures for maintainable test data
6. **Mocking**: Mock external dependencies appropriately
7. **Coverage**: Aim for high test coverage but focus on business-critical paths
8. **TDD**: Write tests first for better design and coverage

This comprehensive testing approach ensures your Spring Boot applications are reliable, maintainable, and production-ready.