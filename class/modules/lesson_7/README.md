# 🏗️ Lesson 7: Service Layer & Clean Architecture

[![Lesson Status](https://img.shields.io/badge/Status-Complete-brightgreen)](#)
[![Difficulty](https://img.shields.io/badge/Difficulty-Intermediate-yellow)](#)
[![Duration](https://img.shields.io/badge/Duration-45--60%20min-blue)](#)

## 🎯 Overview

Master **clean architecture principles** and **service layer patterns** to build maintainable, testable, and scalable Spring Boot applications. Learn to properly separate concerns across application layers and implement dependency inversion for flexible design.

### What You'll Build
- **Task Management System** with clean layered architecture
- **Service layer** with business logic and transaction management
- **Repository interfaces** for data access abstraction
- **Dependency inversion** with interface-driven design

## 📚 Learning Objectives

By completing this lesson, you will:

- ✅ **Master clean architecture principles** and layer separation
- ✅ **Implement service layer patterns** for business logic organization
- ✅ **Apply dependency inversion** with interface-driven development
- ✅ **Design transaction boundaries** and manage data consistency
- ✅ **Create testable architectures** with proper abstraction

## 🏗️ Project Structure

```
lesson_7/
├── workshop/                    # 🔧 Your implementation
│   ├── src/main/kotlin/com/learning/architecture/
│   │   ├── ArchitectureApplication.kt  # Spring Boot app
│   │   ├── model/                     # Domain entities
│   │   │   ├── Task.kt               # Rich domain model
│   │   │   └── Project.kt            # Aggregate root
│   │   ├── dto/                      # Data Transfer Objects
│   │   │   ├── TaskDTOs.kt          # API request/response DTOs
│   │   │   └── ProjectDTOs.kt       # Project-related DTOs
│   │   ├── repository/               # Data access interfaces
│   │   │   ├── TaskRepository.kt     # Task repository interface
│   │   │   └── ProjectRepository.kt  # Project repository interface
│   │   ├── service/                  # Business logic layer
│   │   │   ├── TaskService.kt        # Service interface
│   │   │   ├── TaskServiceImpl.kt    # Service implementation
│   │   │   └── ProjectService.kt     # Project service
│   │   ├── controller/               # REST API layer
│   │   │   ├── TaskController.kt     # Task endpoints
│   │   │   └── ProjectController.kt  # Project endpoints
│   │   ├── config/                   # Configuration
│   │   │   └── ArchitectureConfiguration.kt
│   │   └── exception/               # Custom exceptions
│   └── src/test/                    # Test suite
└── answer/                     # ✅ Complete solution
    └── [Same structure as workshop]
```

## 🚀 Quick Start

### Prerequisites
- Completed [Lesson 6](../lesson_6/) (Request Validation & Error Handling)
- Understanding of Spring Boot dependency injection
- Familiarity with JPA and databases

### Start the Workshop
```bash
cd class/workshop/lesson_7
./gradlew bootRun
```

### Verify Setup
```bash
curl http://localhost:8080/actuator/health
# Should return: {"status":"UP"}

# Check H2 console
open http://localhost:8080/h2-console
```

## 📋 Workshop Steps

| Step | Task | Key Learning | Duration |
|------|------|-------------|----------|
| 1 | [Application Setup](workshop_7.md#step-1) | Clean architecture project structure | 5 min |
| 2 | [Domain Models](workshop_7.md#step-2) | Rich domain entities with business logic | 10 min |
| 3 | [Repository Interfaces](workshop_7.md#step-3) | Data access abstraction patterns | 10 min |
| 4 | [Service Layer](workshop_7.md#step-4) | Business logic and transaction management | 15 min |
| 5 | [Controller Layer](workshop_7.md#step-5) | API integration with service layer | 10 min |
| 6 | [Configuration](workshop_7.md#step-6) | Dependency injection setup | 5 min |
| 7 | [Testing](workshop_7.md#step-7) | Testing clean architecture layers | 10 min |

## 🎯 Key Concepts

### Clean Architecture Layers
```
┌─────────────────────────────────────┐
│          Controllers (API)          │  ← HTTP handling, validation
│  ┌───────────────────────────────┐  │
│  │         Services              │  │  ← Business logic, transactions
│  │  ┌─────────────────────────┐  │  │
│  │  │       Domain            │  │  │  ← Entities, business rules
│  │  │  ┌───────────────────┐  │  │  │
│  │  │  │   Repository      │  │  │  │  ← Data access interfaces
│  │  │  │   Interfaces      │  │  │  │
│  │  │  └───────────────────┘  │  │  │
│  │  └─────────────────────────┘  │  │
│  └───────────────────────────────┘  │
└─────────────────────────────────────┘
│         Infrastructure             │  ← Database, external APIs
└─────────────────────────────────────┘
```

### Service Layer Pattern
```kotlin
@Service
@Transactional
class TaskServiceImpl(
    private val taskRepository: TaskRepository,
    private val validationService: TaskValidationService,
    private val notificationService: NotificationService
) : TaskService {
    
    override fun createTask(request: CreateTaskRequest): TaskResponse {
        // 1. Validate business rules
        val validationResult = validationService.validateCreation(request)
        if (!validationResult.isValid) {
            throw ValidationException(validationResult.errors)
        }
        
        // 2. Create domain entity
        val task = Task.createNew(request.title, request.description, request.priority)
        
        // 3. Save to repository
        val savedTask = taskRepository.save(task)
        
        // 4. Trigger side effects
        notificationService.notifyTaskCreated(savedTask)
        
        // 5. Return response
        return TaskResponse.from(savedTask)
    }
}
```

### Repository Interface
```kotlin
@Repository
interface TaskRepository : JpaRepository<Task, Long> {
    fun findByStatus(status: TaskStatus): List<Task>
    fun findByAssigneeUserId(userId: String): List<Task>
    
    @Query("SELECT t FROM Task t WHERE t.dueDate < :date AND t.status != 'COMPLETED'")
    fun findOverdueTasks(@Param("date") date: LocalDateTime): List<Task>
}
```

### Dependency Inversion
```kotlin
// High-level service depends on abstraction
class TaskService(
    private val taskRepository: TaskRepository  // Interface, not implementation
)

// Low-level implementation
@Repository
class JpaTaskRepository : TaskRepository {
    // Implementation details
}
```

## 🧪 Testing Your Implementation

### Test Clean Architecture Separation
```bash
# Create a task
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Implement clean architecture",
    "description": "Build maintainable service layer",
    "priority": "HIGH"
  }'
```

### Test Business Logic
```bash
# Assign task
curl -X POST http://localhost:8080/api/tasks/1/assign \
  -H "Content-Type: application/json" \
  -d '{
    "assigneeId": "user123",
    "newDueDate": "2024-12-31T23:59:59"
  }'
```

### Test Analytics
```bash
# Get task summary
curl http://localhost:8080/api/tasks/summary
```

Expected response structure:
```json
{
  "totalTasks": 5,
  "tasksByStatus": {
    "CREATED": 2,
    "IN_PROGRESS": 2,
    "COMPLETED": 1
  },
  "tasksByPriority": {
    "HIGH": 3,
    "MEDIUM": 2
  },
  "overdueTasks": 1,
  "averageCompletionTime": 5.5
}
```

## 📊 Success Criteria

✅ **Clean Layer Separation**: Business logic in services, not controllers  
✅ **Interface Abstraction**: Services depend on repository interfaces  
✅ **Transaction Management**: Proper transaction boundaries  
✅ **Domain Logic**: Rich domain models with business behavior  
✅ **Testability**: Each layer can be tested independently  
✅ **Error Handling**: Proper exception propagation and handling  

## 🔗 Related Lessons

- **Previous**: [Lesson 6 - Request Validation & Error Handling](../lesson_6/)
- **Next**: [Lesson 8 - Persistence with Spring Data JPA](../lesson_8/)
- **Concepts**: [Clean Architecture Theory](concept.md)
- **Practice**: [Step-by-Step Workshop](workshop_7.md)

## 💡 Architecture Best Practices

### Service Layer Guidelines
```kotlin
// ✅ Good: Single responsibility, clear interface
interface UserService {
    fun createUser(request: CreateUserRequest): UserResponse
    fun getUserById(id: Long): UserResponse
}

// ❌ Bad: Too many responsibilities
interface UserService {
    fun createUser(request: CreateUserRequest): UserResponse
    fun sendEmail(to: String, subject: String): Boolean
    fun generateReport(): ByteArray
    fun validateCreditCard(number: String): Boolean
}
```

### Dependency Direction
```kotlin
// ✅ Good: Service depends on repository interface
class UserService(
    private val userRepository: UserRepository // Interface in domain layer
)

// ❌ Bad: Service depends on implementation details
class UserService(
    private val jdbcTemplate: JdbcTemplate // Infrastructure concern
)
```

### Transaction Boundaries
```kotlin
// ✅ Good: Transaction at service method level
@Service
@Transactional
class OrderService {
    
    @Transactional  // Single transaction for entire operation
    fun createOrder(request: CreateOrderRequest): OrderResponse {
        val order = orderRepository.save(createOrderFromRequest(request))
        inventoryService.reserveItems(order.items)
        paymentService.processPayment(order.total)
        return OrderResponse.from(order)
    }
}
```

## 🏆 What You've Learned

### Technical Skills
- **Clean Architecture**: Proper layer separation and dependency management
- **Service Layer**: Business logic organization and transaction management
- **Repository Pattern**: Data access abstraction and query encapsulation
- **Domain Modeling**: Rich entities with business behavior

### Professional Practices
- **Separation of Concerns**: Each layer has clear responsibilities
- **Testability**: Architecture supports comprehensive testing
- **Maintainability**: Changes are localized and predictable
- **Flexibility**: Easy to modify and extend functionality

## 🌟 Advanced Topics

### Service Composition
```kotlin
@Service
class OrderProcessingService(
    private val orderService: OrderService,
    private val inventoryService: InventoryService,
    private val paymentService: PaymentService,
    private val notificationService: NotificationService
) {
    @Transactional
    fun processOrder(request: CreateOrderRequest): OrderResponse {
        // Orchestrate multiple services
        val order = orderService.createOrder(request)
        inventoryService.allocateInventory(order.items)
        paymentService.chargeCustomer(order.customer, order.total)
        notificationService.sendOrderConfirmation(order)
        return order
    }
}
```

### Event-Driven Architecture
```kotlin
@Service
class UserService(
    private val userRepository: UserRepository,
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    fun createUser(request: CreateUserRequest): UserResponse {
        val user = userRepository.save(User.from(request))
        
        // Publish domain event
        applicationEventPublisher.publishEvent(UserCreatedEvent(user.id, user.email))
        
        return UserResponse.from(user)
    }
}
```

---

🎉 **Congratulations!** You've mastered clean architecture and service layer patterns. These foundational skills will help you build maintainable, testable applications that can scale with your business needs.

**Ready for the next challenge?** Continue to [Lesson 8: Persistence with Spring Data JPA](../lesson_8/) to learn advanced data access patterns and database optimization techniques.