# 🎯 Lesson 4: Spring Boot Setup & Dependency Injection

## Objective

Transform your Kotlin applications into production-ready Spring Boot services. Master dependency injection, understand the Spring application context, and learn to structure enterprise applications with proper separation of concerns.

## Key Concepts

### 1. Spring Boot Auto-Configuration

Spring Boot provides intelligent defaults and auto-configuration based on classpath dependencies.

```kotlin
@SpringBootApplication
class PaymentApplication

fun main(args: Array<String>) {
    runApplication<PaymentApplication>(*args)
}

// Auto-configures:
// - Embedded Tomcat server
// - JSON serialization with Jackson
// - Logging with Logback
// - Database connections (if JPA on classpath)
```

### 2. Dependency Injection with Annotations

```kotlin
@Component
class PaymentValidator {
    fun validateAmount(amount: Double): Boolean = amount > 0
}

@Service
class PaymentService(
    private val validator: PaymentValidator,
    private val processor: PaymentProcessor
) {
    fun processPayment(amount: Double): PaymentResult {
        if (!validator.validateAmount(amount)) {
            return PaymentResult.Failed("Invalid amount")
        }
        return processor.process(amount)
    }
}

@Repository
class PaymentRepository {
    private val payments = mutableListOf<Payment>()
    
    fun save(payment: Payment): Payment {
        payments.add(payment)
        return payment
    }
}
```

### 3. Configuration Properties

```kotlin
@ConfigurationProperties("payment")
@ConstructorBinding
data class PaymentConfig(
    val maxAmount: Double,
    val processingFee: Double,
    val retryAttempts: Int,
    val timeoutMs: Long
)

@Configuration
@EnableConfigurationProperties(PaymentConfig::class)
class AppConfig
```

### 4. Profiles and Environment Management

```kotlin
@Component
@Profile("development")
class MockPaymentProcessor : PaymentProcessor {
    override fun process(amount: Double) = PaymentResult.Success("MOCK-123")
}

@Component  
@Profile("production")
class RealPaymentProcessor : PaymentProcessor {
    override fun process(amount: Double) = realProcessingLogic(amount)
}
```

## Best Practices

### ✅ Do:
- **Use constructor injection** - immutable dependencies
- **Prefer interfaces** for service contracts
- **Leverage auto-configuration** instead of manual configuration
- **Use @ConfigurationProperties** for structured configuration
- **Write testable code** with dependency injection

### ❌ Avoid:
- **Field injection** with @Autowired on fields
- **Circular dependencies** between components
- **God classes** that do too much
- **Configuration in code** - use application.properties/yml

## Real-World Example

```kotlin
@Service
class PaymentService(
    private val repository: PaymentRepository,
    private val processor: PaymentProcessor,
    private val auditor: AuditService,
    private val config: PaymentConfig
) {
    @Transactional
    fun processPayment(request: PaymentRequest): PaymentResult {
        auditor.logAttempt(request)
        
        if (request.amount > config.maxAmount) {
            return PaymentResult.Failed("Amount exceeds limit")
        }
        
        val payment = Payment(request.amount, request.method)
        repository.save(payment)
        
        val result = processor.process(request.amount)
        auditor.logResult(result)
        
        return result
    }
}
```

## Next Steps

This prepares you for REST Controllers in Lesson 5 and database integration in Lesson 8.