# Workshop 4: Spring Boot Setup & Dependency Injection

## 🎯 Learning Objectives

By the end of this workshop, you will:
- Set up a complete Spring Boot application from scratch
- Master dependency injection and inversion of control
- Create type-safe configuration with `@ConfigurationProperties`
- Use `@Component`, `@Service`, `@Configuration` annotations effectively
- Implement profile-based configuration for different environments
- Build testable Spring applications with proper architecture

## 📋 Prerequisites

- Completed Lessons 1-3 (Kotlin fundamentals, collections, OOP)
- Understanding of interfaces and dependency injection concepts
- Basic knowledge of build tools (Gradle)

## 🏗️ Project Structure

```
lesson_4/
├── src/main/kotlin/com/learning/payment/
│   ├── PaymentApplication.kt           # Spring Boot main class
│   ├── config/
│   │   ├── PaymentConfiguration.kt     # Bean configuration
│   │   └── PaymentConfigurationProperties.kt  # Type-safe config
│   ├── model/                          # Domain models (from Lesson 3)
│   ├── processor/                      # Payment processors as Spring components
│   ├── audit/                          # Auditing components
│   ├── service/                        # Business logic services
│   └── controller/                     # REST API controllers
├── src/main/resources/
│   └── application.yml                 # Application configuration
└── src/test/kotlin/
    └── PaymentServiceTest.kt           # Integration tests
```

## 🚀 Workshop Steps

### Step 1: Complete the Spring Boot Application Class

Open `PaymentApplication.kt` and:

1. **Add the `@SpringBootApplication` annotation**
   ```kotlin
   @SpringBootApplication
   class PaymentApplication
   ```

2. **Implement the main function**
   ```kotlin
   fun main(args: Array<String>) {
       runApplication<PaymentApplication>(*args)
   }
   ```

**💡 Key Learning:** `@SpringBootApplication` combines three annotations:
- `@Configuration`: Enables configuration classes
- `@EnableAutoConfiguration`: Enables Spring Boot's auto-configuration
- `@ComponentScan`: Enables component scanning in the package

### Step 2: Create Type-Safe Configuration Properties

Open `PaymentConfigurationProperties.kt` and:

1. **Add the configuration annotations**
   ```kotlin
   @ConfigurationProperties("payment")
   @ConstructorBinding
   data class PaymentConfigurationProperties(...)
   ```

2. **Complete the data class properties**
   ```kotlin
   data class ProcessorSettings(
       val enabled: Boolean = true,
       val feeRate: Double = 0.0,
       val maxAmount: Double = 10000.0
   )
   
   data class AuditConfig(
       val enabled: Boolean = true,
       val complianceMode: Boolean = false
   )
   
   data class RetryConfig(
       val maxAttempts: Int = 3,
       val delayMs: Long = 1000L
   )
   ```

**💡 Key Learning:** Type-safe configuration prevents runtime errors and provides IDE autocompletion.

### Step 3: Configure Spring Beans

Open `PaymentConfiguration.kt` and:

1. **Add the configuration annotations**
   ```kotlin
   @Configuration
   @EnableConfigurationProperties(PaymentConfigurationProperties::class)
   class PaymentConfiguration(...)
   ```

2. **Create processor beans**
   ```kotlin
   @Bean
   fun creditCardProcessor(): PaymentProcessor {
       return CreditCardProcessor(paymentConfig)
   }
   
   @Bean
   fun bankTransferProcessor(): PaymentProcessor {
       return BankTransferProcessor(paymentConfig)
   }
   
   @Bean  
   fun digitalWalletProcessor(): PaymentProcessor {
       return DigitalWalletProcessor(paymentConfig)
   }
   ```

3. **Create auditor beans with profiles**
   ```kotlin
   @Bean
   @Primary
   @Profile("!production")
   fun paymentAuditor(): Auditable {
       return PaymentAuditor()
   }
   
   @Bean
   @Profile("production")
   fun complianceAuditor(): Auditable {
       return ComplianceAuditor()
   }
   ```

4. **Create processor map bean**
   ```kotlin
   @Bean
   fun paymentProcessors(
       creditCardProcessor: PaymentProcessor,
       bankTransferProcessor: PaymentProcessor,
       digitalWalletProcessor: PaymentProcessor
   ): Map<String, PaymentProcessor> {
       return mapOf(
           "CREDIT_CARD" to creditCardProcessor,
           "BANK_TRANSFER" to bankTransferProcessor,
           "DIGITAL_WALLET" to digitalWalletProcessor
       )
   }
   ```

**💡 Key Learning:** Spring will automatically inject dependencies based on types and names.

### Step 4: Complete the Payment Processors

In each processor file (`CreditCardProcessor.kt`, etc.):

1. **Add the `@Component` annotation**
   ```kotlin
   @Component
   class CreditCardProcessor(...)
   ```

2. **Use configuration properties**
   ```kotlin
   override val processingFeeRate: Double
       get() = config.processors.creditCard.feeRate
   
   override val maxAmount: Double
       get() = config.processors.creditCard.maxAmount
   ```

3. **Check if processor is enabled**
   ```kotlin
   if (!config.processors.creditCard.enabled) {
       return PaymentResult.Failed(...)
   }
   ```

**💡 Key Learning:** Spring components are automatically discovered and managed by the container.

### Step 5: Implement the Auditing Components

Open `PaymentAuditor.kt` and:

1. **Add the `@Component` annotation**
   ```kotlin
   @Component
   class PaymentAuditor : Auditable
   ```

2. **Use Spring's logging**
   ```kotlin
   override fun auditPaymentAttempt(method: PaymentMethod, amount: Double) {
       val methodType = when (method) {
           is PaymentMethod.CreditCard -> "Credit Card (${method.cardType.displayName})"
           is PaymentMethod.BankAccount -> "Bank Account (${method.accountType.displayName})" 
           is PaymentMethod.DigitalWallet -> "Digital Wallet (${method.walletType.displayName})"
       }
       
       logger.info("Payment attempt: method={}, amount={}, methodId={}", 
                   methodType, amount, method.id)
   }
   ```

For `ComplianceAuditor.kt`:

1. **Add profile-specific annotation**
   ```kotlin
   @Component
   @Profile("production")
   class ComplianceAuditor : Auditable
   ```

**💡 Key Learning:** Profiles allow different configurations for different environments.

### Step 6: Create the Payment Service

Open `PaymentService.kt` and:

1. **Add the `@Service` annotation**
   ```kotlin
   @Service
   class PaymentService(...)
   ```

2. **Implement dependency injection**
   ```kotlin
   suspend fun processPayment(method: PaymentMethod, amount: Double): PaymentResult {
       auditor.auditPaymentAttempt(method, amount)
       // ... rest of implementation
   }
   ```

3. **Use the injected processors**
   ```kotlin
   private fun selectProcessor(method: PaymentMethod): PaymentProcessor? {
       return when (method) {
           is PaymentMethod.CreditCard -> processors["CREDIT_CARD"]
           is PaymentMethod.BankAccount -> processors["BANK_TRANSFER"]
           is PaymentMethod.DigitalWallet -> processors["DIGITAL_WALLET"]
       }
   }
   ```

**💡 Key Learning:** `@Service` is a specialization of `@Component` for business logic.

### Step 7: Build a REST Controller

Open `PaymentController.kt` and:

1. **Add REST annotations**
   ```kotlin
   @RestController
   @RequestMapping("/api/payments")
   class PaymentController(...)
   ```

2. **Create endpoints**
   ```kotlin
   @GetMapping("/processors")
   fun getSupportedMethods(): Map<String, Any> {
       return mapOf(
           "supportedMethods" to paymentService.getSupportedPaymentMethods(),
           "processorStats" to paymentService.getProcessorStats()
       )
   }
   
   @PostMapping("/process")
   suspend fun processPayment(@RequestBody request: PaymentRequest): PaymentResponse {
       // Implementation
   }
   ```

3. **Define request/response DTOs**
   ```kotlin
   data class PaymentRequest(
       val methodType: String,
       val methodData: Map<String, Any>,
       val amount: Double
   )
   
   data class PaymentResponse(
       val success: Boolean,
       val transactionId: String?,
       val amount: Double,
       val fee: Double?,
       val total: Double?,
       val errorMessage: String?
   )
   ```

### Step 8: Write Integration Tests

Open `PaymentServiceTest.kt` and:

1. **Add test annotations**
   ```kotlin
   @SpringBootTest
   class PaymentServiceTest {
       @Autowired
       lateinit var paymentService: PaymentService
   }
   ```

2. **Test dependency injection**
   ```kotlin
   @Test
   fun `should inject dependencies correctly`() {
       assertNotNull(paymentService)
   }
   ```

**💡 Key Learning:** `@SpringBootTest` loads the full application context for integration testing.

### Step 9: Test Your Application

1. **Build the application**
   ```bash
   ./gradlew build
   ```

2. **Run the application**
   ```bash
   ./gradlew bootRun
   ```

3. **Test the endpoints**
   ```bash
   curl http://localhost:8080/api/payments/health
   curl http://localhost:8080/api/payments/processors
   ```

## 🎯 Expected Outcomes

After completing this workshop, you should have:

1. **Working Spring Boot Application**
   - Auto-configuration working properly
   - All beans properly wired
   - Application starts without errors

2. **Configuration Management**
   - Type-safe configuration properties
   - Profile-based configuration (dev vs production)
   - Environment-specific auditing

3. **Dependency Injection**
   - Components automatically discovered
   - Dependencies injected by type and name
   - Proper separation of concerns

4. **REST API**
   - Working endpoints for payment processing
   - Proper request/response handling
   - Integration with business logic

## 🔍 Key Concepts Demonstrated

### Dependency Injection
- **Constructor Injection**: Preferred method for required dependencies
- **Type-based Wiring**: Spring matches beans by type
- **Qualifier**: Use `@Primary` and `@Profile` for bean selection

### Configuration
- **`@ConfigurationProperties`**: Type-safe configuration binding
- **Profiles**: Environment-specific configuration
- **Bean Definition**: Manual bean creation with `@Bean`

### Component Scanning
- **`@Component`**: General-purpose components
- **`@Service`**: Business logic components  
- **`@Repository`**: Data access components
- **`@Controller`**: Web layer components

### Testing
- **`@SpringBootTest`**: Full application context loading
- **`@MockBean`**: Mocking Spring beans in tests
- **Integration Testing**: Testing complete workflows

## 🚀 Next Steps

Ready for **Lesson 5: REST Controllers & DTOs**? You'll learn:
- Advanced REST endpoint design
- Request/response validation
- Error handling strategies
- API documentation with OpenAPI

## 💡 Pro Tips

1. **Use Constructor Injection**: More testable and immutable
2. **Leverage Profiles**: Different configurations for different environments  
3. **Keep Configuration External**: Use `application.yml` for all settings
4. **Write Integration Tests**: Test the full Spring context
5. **Use Type-Safe Configuration**: Prevent runtime configuration errors

---

**🎉 Congratulations!** You've mastered Spring Boot setup and dependency injection. Your payment system is now a professional Spring Boot application ready for production deployment!