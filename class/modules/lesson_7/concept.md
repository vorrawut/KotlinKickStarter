# 🏗️ Lesson 7: Service Layer & Clean Architecture - Concepts

## 🎯 Learning Objectives

By the end of this lesson, you will understand:
- **Clean Architecture principles** and layered design
- **Service Layer patterns** for business logic organization
- **Dependency Inversion** and interface-driven development
- **Separation of Concerns** across application layers
- **Transaction management** and data consistency

---

## 🔍 What Is Clean Architecture?

### The Problem with Tight Coupling
```kotlin
// ❌ Tightly coupled, hard to test and maintain
@RestController
class BadUserController {
    fun createUser(@RequestBody request: CreateUserRequest): UserResponse {
        // Direct database access in controller
        val user = User(request.name, request.email)
        entityManager.persist(user)
        
        // Direct email service call
        emailService.sendWelcomeEmail(user.email)
        
        // Business logic mixed with presentation
        if (user.email.endsWith("@company.com")) {
            user.role = "EMPLOYEE"
        }
        
        return UserResponse(user.id, user.name, user.email)
    }
}

// ✅ Clean architecture with proper separation
@RestController
class UserController(
    private val userService: UserService
) {
    @PostMapping("/users")
    fun createUser(@Valid @RequestBody request: CreateUserRequest): ResponseEntity<UserResponse> {
        val response = userService.createUser(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}
```

### Clean Architecture Benefits
- **Testability**: Each layer can be tested independently
- **Maintainability**: Changes in one layer don't affect others
- **Flexibility**: Easy to swap implementations
- **Business Focus**: Core logic separated from technical concerns

---

## 🏛️ Architecture Layers

### The Dependency Rule
**Dependencies point inward.** Outer layers depend on inner layers, never the reverse.

```
┌─────────────────────────────────────┐
│          Presentation Layer         │  ← Controllers, DTOs
│  ┌───────────────────────────────┐  │
│  │       Service Layer           │  │  ← Business Logic
│  │  ┌─────────────────────────┐  │  │
│  │  │    Domain Layer         │  │  │  ← Entities, Value Objects
│  │  │  ┌───────────────────┐  │  │  │
│  │  │  │   Repository      │  │  │  │  ← Data Access Interfaces
│  │  │  │   Interfaces      │  │  │  │
│  │  │  └───────────────────┘  │  │  │
│  │  └─────────────────────────┘  │  │
│  └───────────────────────────────┘  │
└─────────────────────────────────────┘
│         Infrastructure Layer        │  ← Database, External APIs
└─────────────────────────────────────┘
```

### Layer Responsibilities

| Layer | Responsibility | Examples |
|-------|----------------|----------|
| **Presentation** | HTTP handling, validation, formatting | Controllers, DTOs, Error Handlers |
| **Service** | Business logic, orchestration, transactions | Service classes, Business rules |
| **Domain** | Core business entities and rules | Entities, Value Objects, Domain Services |
| **Repository** | Data access abstraction | Repository interfaces |
| **Infrastructure** | External concerns, implementations | Database, Email, File System |

---

## 🎯 Service Layer Patterns

### Service Interfaces and Implementations

```kotlin
// Domain-focused service interface
interface UserService {
    fun createUser(request: CreateUserRequest): UserResponse
    fun getUserById(id: Long): UserResponse
    fun updateUser(id: Long, request: UpdateUserRequest): UserResponse
    fun deleteUser(id: Long)
    fun searchUsers(criteria: UserSearchCriteria): Page<UserResponse>
}

// Service implementation with business logic
@Service
@Transactional
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val userValidationService: UserValidationService,
    private val notificationService: NotificationService
) : UserService {
    
    override fun createUser(request: CreateUserRequest): UserResponse {
        // 1. Validate business rules
        val validationResult = userValidationService.validateCreation(request)
        if (!validationResult.isValid) {
            throw ValidationException(validationResult.errors)
        }
        
        // 2. Create domain entity
        val user = User.create(
            name = request.name,
            email = request.email,
            role = determineUserRole(request.email)
        )
        
        // 3. Save to repository
        val savedUser = userRepository.save(user)
        
        // 4. Trigger side effects
        notificationService.sendWelcomeEmail(savedUser.email)
        
        // 5. Return response
        return UserResponse.from(savedUser)
    }
    
    private fun determineUserRole(email: String): UserRole {
        return when {
            email.endsWith("@company.com") -> UserRole.EMPLOYEE
            email.endsWith("@admin.com") -> UserRole.ADMIN
            else -> UserRole.CUSTOMER
        }
    }
}
```

### Transaction Management

```kotlin
@Service
@Transactional
class OrderService(
    private val orderRepository: OrderRepository,
    private val inventoryService: InventoryService,
    private val paymentService: PaymentService
) {
    
    // Declarative transaction management
    @Transactional
    fun createOrder(request: CreateOrderRequest): OrderResponse {
        // All operations within this method run in a single transaction
        val order = Order.create(request.customerId, request.items)
        
        // Reserve inventory
        inventoryService.reserveItems(order.items)
        
        // Process payment
        val payment = paymentService.processPayment(order.total, request.paymentMethod)
        order.confirmPayment(payment.id)
        
        // Save order
        val savedOrder = orderRepository.save(order)
        
        return OrderResponse.from(savedOrder)
    }
    
    // Read-only transaction for performance
    @Transactional(readOnly = true)
    fun getOrderById(id: Long): OrderResponse {
        val order = orderRepository.findById(id)
            ?: throw OrderNotFoundException("Order not found: $id")
        return OrderResponse.from(order)
    }
}
```

---

## 🔄 Dependency Inversion Principle

### Interface-Driven Design

```kotlin
// ❌ Service depends on concrete implementation
@Service
class BadOrderService {
    private val mysqlOrderRepository = MySQLOrderRepository() // Tight coupling
    private val smtpEmailService = SMTPEmailService() // Hard to test
    
    fun createOrder(order: Order) {
        mysqlOrderRepository.save(order)
        smtpEmailService.sendConfirmation(order.customerEmail)
    }
}

// ✅ Service depends on abstractions
@Service
class OrderService(
    private val orderRepository: OrderRepository, // Interface
    private val emailService: EmailService        // Interface
) {
    fun createOrder(order: Order) {
        orderRepository.save(order)
        emailService.sendConfirmation(order.customerEmail)
    }
}

// Repository interface in domain layer
interface OrderRepository {
    fun save(order: Order): Order
    fun findById(id: Long): Order?
    fun findByCustomerId(customerId: Long): List<Order>
}

// Implementation in infrastructure layer
@Repository
class JpaOrderRepository(
    private val entityManager: EntityManager
) : OrderRepository {
    override fun save(order: Order): Order {
        return entityManager.merge(order)
    }
    
    override fun findById(id: Long): Order? {
        return entityManager.find(Order::class.java, id)
    }
}
```

### Benefits of Dependency Inversion
- **Testability**: Easy to mock dependencies
- **Flexibility**: Swap implementations without changing business logic
- **Independence**: Core logic doesn't depend on external concerns

---

## 📊 Data Transfer Objects (DTOs)

### API Boundary Separation

```kotlin
// Request DTOs with validation
data class CreateUserRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(min = 2, max = 50)
    val name: String,
    
    @field:Email(message = "Invalid email format")
    @field:NotBlank
    val email: String,
    
    @field:Valid
    val address: CreateAddressRequest?
)

// Response DTOs for API formatting
data class UserResponse(
    val id: Long,
    val name: String,
    val email: String,
    val role: String,
    val createdAt: String,
    val isActive: Boolean
) {
    companion object {
        fun from(user: User): UserResponse {
            return UserResponse(
                id = user.id!!,
                name = user.name,
                email = user.email,
                role = user.role.name,
                createdAt = user.createdAt.toString(),
                isActive = user.isActive
            )
        }
    }
}

// Domain entity (internal representation)
@Entity
data class User(
    @Id @GeneratedValue
    val id: Long? = null,
    
    @Column(nullable = false)
    val name: String,
    
    @Column(unique = true, nullable = false)
    val email: String,
    
    @Enumerated(EnumType.STRING)
    val role: UserRole,
    
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val isActive: Boolean = true
) {
    companion object {
        fun create(name: String, email: String, role: UserRole): User {
            return User(
                name = name,
                email = email,
                role = role
            )
        }
    }
}
```

### DTO Mapping Strategies

```kotlin
// 1. Manual mapping (full control)
fun mapToUserResponse(user: User): UserResponse {
    return UserResponse(
        id = user.id!!,
        name = user.name,
        email = user.email,
        role = user.role.displayName,
        createdAt = user.createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        isActive = user.isActive
    )
}

// 2. Extension functions for clean mapping
fun User.toResponse(): UserResponse {
    return UserResponse(
        id = this.id!!,
        name = this.name,
        email = this.email,
        role = this.role.displayName,
        createdAt = this.createdAt.toString(),
        isActive = this.isActive
    )
}

// 3. Companion object factory methods
data class UserResponse(/*...*/) {
    companion object {
        fun from(user: User): UserResponse = user.toResponse()
        
        fun fromList(users: List<User>): List<UserResponse> {
            return users.map { it.toResponse() }
        }
    }
}
```

---

## 🧪 Testing Clean Architecture

### Testing Each Layer Independently

```kotlin
// Unit testing service layer
@ExtendWith(MockitoExtension::class)
class UserServiceTest {
    
    @Mock
    private lateinit var userRepository: UserRepository
    
    @Mock
    private lateinit var notificationService: NotificationService
    
    @InjectMocks
    private lateinit var userService: UserServiceImpl
    
    @Test
    fun `should create user successfully`() {
        // Given
        val request = CreateUserRequest("John Doe", "john@example.com")
        val user = User.create(request.name, request.email, UserRole.CUSTOMER)
        val savedUser = user.copy(id = 1L)
        
        whenever(userRepository.save(any())).thenReturn(savedUser)
        
        // When
        val response = userService.createUser(request)
        
        // Then
        assertThat(response.id).isEqualTo(1L)
        assertThat(response.name).isEqualTo("John Doe")
        verify(notificationService).sendWelcomeEmail("john@example.com")
    }
}

// Integration testing with test slices
@DataJpaTest
class UserRepositoryTest {
    
    @Autowired
    private lateinit var userRepository: UserRepository
    
    @Test
    fun `should save and find user`() {
        // Given
        val user = User.create("John Doe", "john@example.com", UserRole.CUSTOMER)
        
        // When
        val saved = userRepository.save(user)
        val found = userRepository.findById(saved.id!!)
        
        // Then
        assertThat(found).isNotNull
        assertThat(found!!.email).isEqualTo("john@example.com")
    }
}

// Controller testing with MockMvc
@WebMvcTest(UserController::class)
class UserControllerTest {
    
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @MockBean
    private lateinit var userService: UserService
    
    @Test
    fun `should create user via API`() {
        // Given
        val request = CreateUserRequest("John Doe", "john@example.com")
        val response = UserResponse(1L, "John Doe", "john@example.com", "CUSTOMER", "2024-01-01T10:00:00", true)
        
        whenever(userService.createUser(any())).thenReturn(response)
        
        // When & Then
        mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isCreated)
        .andExpected(jsonPath("$.name").value("John Doe"))
    }
}
```

---

## 🔧 Configuration and Dependency Injection

### Spring Configuration Patterns

```kotlin
@Configuration
@EnableTransactionManagement
class ServiceLayerConfiguration {
    
    @Bean
    fun userService(
        userRepository: UserRepository,
        validationService: UserValidationService,
        notificationService: NotificationService
    ): UserService {
        return UserServiceImpl(userRepository, validationService, notificationService)
    }
    
    @Bean
    @Profile("dev")
    fun mockNotificationService(): NotificationService {
        return MockNotificationService()
    }
    
    @Bean
    @Profile("prod")
    fun emailNotificationService(): NotificationService {
        return EmailNotificationService()
    }
}

// Configuration properties for service layer
@ConfigurationProperties(prefix = "app.service")
data class ServiceProperties(
    val batchSize: Int = 100,
    val timeoutSeconds: Int = 30,
    val enableAsyncProcessing: Boolean = false
)
```

### Conditional Bean Configuration

```kotlin
@Service
@ConditionalOnProperty(name = "app.features.advanced-search", havingValue = "true")
class AdvancedUserSearchService : UserSearchService {
    // Implementation when advanced search is enabled
}

@Service
@ConditionalOnProperty(name = "app.features.advanced-search", havingValue = "false", matchIfMissing = true)
class BasicUserSearchService : UserSearchService {
    // Default implementation
}
```

---

## 🎯 Key Takeaways

### Clean Architecture Principles

1. **Dependency Inversion**: High-level modules should not depend on low-level modules
2. **Single Responsibility**: Each layer has one reason to change
3. **Interface Segregation**: Depend on abstractions, not concretions
4. **Separation of Concerns**: Business logic separated from technical concerns

### Service Layer Best Practices

- ✅ **Business Logic Belongs Here**: Not in controllers or repositories
- ✅ **Transaction Boundaries**: Service methods define transaction scope
- ✅ **Interface First**: Design interfaces before implementations
- ✅ **Stateless Services**: Services should not maintain state between calls
- ✅ **Error Handling**: Translate technical exceptions to business exceptions

### Testing Strategy

- **Unit Tests**: Test service logic with mocked dependencies
- **Integration Tests**: Test service with real repositories
- **Contract Tests**: Test interface compliance
- **End-to-End Tests**: Test complete workflows

### Common Anti-Patterns to Avoid

- ❌ **Anemic Domain Model**: All logic in services, entities just data holders
- ❌ **God Services**: Services that do too many things
- ❌ **Repository in Controller**: Bypassing the service layer
- ❌ **Leaky Abstractions**: Domain concepts leaking into presentation layer

---

## 🚀 Next Steps

In the next lesson, we'll explore **Persistence with Spring Data JPA**, building on this clean architecture foundation to add robust data access patterns.

Remember: **Clean architecture is about making the right things easy and the wrong things hard.** When changing requirements feels difficult, it's often a sign that your architecture needs improvement.

---

*Clean architecture isn't about perfect separation—it's about making your code easy to understand, test, and change over time.*