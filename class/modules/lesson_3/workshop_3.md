# 🔨 Workshop 3: Payment Processing System with Advanced OOP

## What We Want to Build

Create a sophisticated **Payment Processing System** that demonstrates Kotlin's advanced OOP features: sealed classes for type-safe hierarchies, interfaces with default implementations, delegation patterns, and smart casting. You'll build a system that can handle multiple payment methods with proper error handling and extensibility.

## Expected Result

By the end of this workshop, you'll have:
- ✅ A sealed class hierarchy for different payment methods
- ✅ PaymentResult sealed class for type-safe response handling  
- ✅ PaymentProcessor interface with default implementations
- ✅ Multiple processor implementations using delegation
- ✅ Smart casting for conditional payment logic
- ✅ Exhaustive pattern matching with when expressions

**Sample Output:**
```
=== Payment Processing System ===
💳 Processing Credit Card Payment:
✓ Card Validation: Visa ****1234 - Valid
✓ Payment Processed: $250.00 (Fee: $7.50, Total: $257.50)
✓ Transaction ID: CC-1703123456789
✓ Audit: Payment completed successfully

🏦 Processing Bank Transfer:
✓ Account Validation: Account ****5678 - Sufficient funds
✓ Payment Processed: $1500.00 (Fee: $0.00, Total: $1500.00)  
✓ Transaction ID: BT-1703123456790
✓ Audit: Large transaction flagged for review

💰 Processing Digital Wallet:
✓ Wallet Validation: PayPal - Balance sufficient
✓ Payment Processed: $75.00 (Fee: $2.25, Total: $77.25)
✓ Transaction ID: DW-1703123456791
✓ Result: Success
```

## Step-by-Step Code Guide

### Step 1: Create Payment Method Sealed Classes

**File**: `class/workshop/lesson_3/src/main/kotlin/PaymentMethod.kt`

```kotlin
/**
 * Lesson 3 Workshop: Payment Method Sealed Classes
 * 
 * TODO: Create sealed classes to represent different payment methods
 * This demonstrates:
 * - Sealed class hierarchies for type safety
 * - Data classes within sealed hierarchies
 * - Properties and validation in data classes
 */

// TODO: Create a sealed class for PaymentMethod
sealed class PaymentMethod {
    // TODO: Add common properties that all payment methods should have
    abstract val id: String
    abstract val isActive: Boolean
    
    // TODO: Create data classes for different payment types
    
    // Credit Card payment method
    data class CreditCard(
        override val id: String,
        override val isActive: Boolean = true,
        val cardNumber: String,
        val expiryMonth: Int,
        val expiryYear: Int,
        val cardType: CardType,
        val holderName: String
    ) : PaymentMethod() {
        
        // TODO: Add computed properties
        val lastFourDigits: String
            get() = TODO("Return last 4 digits of card number")
        
        val isExpired: Boolean
            get() = TODO("Check if card is expired based on current date")
        
        val maskedNumber: String
            get() = TODO("Return masked card number like ****1234")
    }
    
    // Bank Account payment method  
    data class BankAccount(
        override val id: String,
        override val isActive: Boolean = true,
        val accountNumber: String,
        val routingNumber: String,
        val accountType: AccountType,
        val bankName: String,
        val balance: Double
    ) : PaymentMethod() {
        
        // TODO: Add computed properties
        val maskedAccountNumber: String
            get() = TODO("Return masked account number")
            
        val hasSufficientFunds: Boolean
            get() = TODO("Check if balance > 0")
    }
    
    // Digital Wallet payment method
    data class DigitalWallet(
        override val id: String,  
        override val isActive: Boolean = true,
        val walletType: WalletType,
        val email: String,
        val balance: Double,
        val currency: String = "USD"
    ) : PaymentMethod() {
        
        // TODO: Add computed properties
        val displayName: String
            get() = TODO("Return formatted display name with wallet type and email")
    }
}

// TODO: Create enums for payment method types
enum class CardType(val displayName: String) {
    // TODO: Add card types
    // VISA("Visa"),
    // MASTERCARD("Mastercard"), 
    // AMEX("American Express"),
    // DISCOVER("Discover")
}

enum class AccountType(val displayName: String) {
    // TODO: Add account types
    // CHECKING("Checking"),
    // SAVINGS("Savings"),
    // BUSINESS("Business")
}

enum class WalletType(val displayName: String) {
    // TODO: Add wallet types  
    // PAYPAL("PayPal"),
    // APPLE_PAY("Apple Pay"),
    // GOOGLE_PAY("Google Pay"),
    // VENMO("Venmo")
}
```

### Step 2: Create Payment Result Sealed Class

**File**: `class/workshop/lesson_3/src/main/kotlin/PaymentResult.kt`

```kotlin
/**
 * Lesson 3 Workshop: Payment Result Sealed Class
 * 
 * TODO: Create sealed classes for type-safe payment results
 * This demonstrates:
 * - Sealed classes for representing outcomes
 * - Different result types with relevant data
 * - Exhaustive when expressions
 */

// TODO: Create sealed class for PaymentResult
sealed class PaymentResult {
    
    // TODO: Success result with transaction details
    data class Success(
        val transactionId: String,
        val amount: Double,
        val fee: Double,
        val total: Double,
        val paymentMethod: PaymentMethod,
        val timestamp: Long = System.currentTimeMillis()
    ) : PaymentResult() {
        
        // TODO: Add computed properties
        fun getFormattedAmount(): String = TODO("Format amount as currency")
        fun getFormattedTimestamp(): String = TODO("Format timestamp as readable date")
    }
    
    // TODO: Failed result with error details
    data class Failed(
        val errorCode: String,
        val errorMessage: String,
        val paymentMethod: PaymentMethod?,
        val amount: Double,
        val timestamp: Long = System.currentTimeMillis()
    ) : PaymentResult() {
        
        // TODO: Add helper methods
        fun isRetryable(): Boolean = TODO("Determine if this error allows retry")
        fun getRetryDelay(): Long = TODO("Get recommended retry delay in milliseconds")
    }
    
    // TODO: Pending result for async operations
    data class Pending(
        val transactionId: String,
        val amount: Double,
        val paymentMethod: PaymentMethod,
        val estimatedCompletionTime: Long,
        val statusCheckUrl: String? = null
    ) : PaymentResult()
    
    // TODO: Cancelled result
    data class Cancelled(
        val reason: String,
        val paymentMethod: PaymentMethod?,
        val amount: Double,
        val timestamp: Long = System.currentTimeMillis()
    ) : PaymentResult()
}

// TODO: Create extension functions for PaymentResult
// Helper function to check if payment was successful
fun PaymentResult.isSuccessful(): Boolean = TODO("Return true only for Success results")

// Helper function to get amount regardless of result type
fun PaymentResult.getAmount(): Double = TODO("Extract amount from any result type")

// Helper function to format result for display
fun PaymentResult.formatSummary(): String = TODO("Return formatted summary string")
```

### Step 3: Create PaymentProcessor Interface

**File**: `class/workshop/lesson_3/src/main/kotlin/PaymentProcessor.kt`

```kotlin
/**
 * Lesson 3 Workshop: Payment Processor Interface
 * 
 * TODO: Create interface with default implementations
 * This demonstrates:
 * - Interfaces with default method implementations
 * - Abstract properties and methods
 * - Template method pattern
 */

interface PaymentProcessor {
    
    // TODO: Abstract properties that implementations must provide
    val processorName: String
    val supportedMethods: Set<String>
    val processingFeeRate: Double
    
    // TODO: Abstract method for processing payments
    suspend fun processPayment(method: PaymentMethod, amount: Double): PaymentResult
    
    // TODO: Default implementation for calculating fees
    fun calculateFee(amount: Double): Double {
        // TODO: Implement fee calculation based on processingFeeRate
        return TODO("Calculate fee as percentage of amount")
    }
    
    // TODO: Default implementation for calculating total
    fun calculateTotal(amount: Double): Double {
        // TODO: Calculate total including fees
        return TODO("Return amount + calculated fee")
    }
    
    // TODO: Default implementation for validation
    fun validatePaymentMethod(method: PaymentMethod): Boolean {
        // TODO: Perform basic validation
        // Check if method is active, not expired, etc.
        return TODO("Implement basic payment method validation")
    }
    
    // TODO: Default implementation for generating transaction ID
    fun generateTransactionId(method: PaymentMethod): String {
        // TODO: Generate unique transaction ID
        // Use processor name prefix and timestamp
        return TODO("Generate unique transaction ID")
    }
    
    // TODO: Default implementation for logging
    fun logTransaction(result: PaymentResult) {
        // TODO: Log transaction details
        when (result) {
            is PaymentResult.Success -> TODO("Log successful transaction")
            is PaymentResult.Failed -> TODO("Log failed transaction")
            is PaymentResult.Pending -> TODO("Log pending transaction")
            is PaymentResult.Cancelled -> TODO("Log cancelled transaction")
        }
    }
    
    // TODO: Template method combining all steps
    suspend fun executePayment(method: PaymentMethod, amount: Double): PaymentResult {
        // TODO: Implement template method that:
        // 1. Validates payment method
        // 2. Processes payment
        // 3. Logs result
        // 4. Returns result
        
        return TODO("Implement template method for payment execution")
    }
}
```

### Step 4: Create Specific Payment Processors

**File**: `class/workshop/lesson_3/src/main/kotlin/PaymentProcessors.kt`

```kotlin
/**
 * Lesson 3 Workshop: Specific Payment Processors
 * 
 * TODO: Implement specific processors for different payment methods
 * This demonstrates:
 * - Interface implementation
 * - Method overriding
 * - Smart casting in when expressions
 */

// TODO: CreditCardProcessor implementation
class CreditCardProcessor : PaymentProcessor {
    
    override val processorName = "CreditCardProcessor"
    override val supportedMethods = setOf("VISA", "MASTERCARD", "AMEX", "DISCOVER")
    override val processingFeeRate = 0.029 // 2.9%
    
    override suspend fun processPayment(method: PaymentMethod, amount: Double): PaymentResult {
        // TODO: Smart cast and process credit card payment
        return when (method) {
            is PaymentMethod.CreditCard -> {
                // TODO: Credit card specific processing
                // 1. Validate card details
                // 2. Check amount limits
                // 3. Process payment
                // 4. Return appropriate result
                
                TODO("Implement credit card processing logic")
            }
            else -> PaymentResult.Failed(
                "UNSUPPORTED_METHOD",
                "Credit card processor cannot handle ${method::class.simpleName}",
                method,
                amount
            )
        }
    }
    
    // TODO: Override fee calculation for credit cards
    override fun calculateFee(amount: Double): Double {
        // TODO: Credit card specific fee calculation
        // Minimum fee of $0.30, maximum of $50
        return TODO("Implement credit card fee calculation with min/max limits")
    }
}

// TODO: BankTransferProcessor implementation  
class BankTransferProcessor : PaymentProcessor {
    
    override val processorName = "BankTransferProcessor"
    override val supportedMethods = setOf("CHECKING", "SAVINGS", "BUSINESS")
    override val processingFeeRate = 0.0 // No fees for bank transfers
    
    override suspend fun processPayment(method: PaymentMethod, amount: Double): PaymentResult {
        // TODO: Smart cast and process bank transfer
        return when (method) {
            is PaymentMethod.BankAccount -> {
                // TODO: Bank transfer specific processing
                // 1. Validate account details
                // 2. Check account balance
                // 3. Process transfer (may be pending for large amounts)
                // 4. Return appropriate result
                
                TODO("Implement bank transfer processing logic")
            }
            else -> PaymentResult.Failed(
                "UNSUPPORTED_METHOD", 
                "Bank transfer processor cannot handle ${method::class.simpleName}",
                method,
                amount
            )
        }
    }
}

// TODO: DigitalWalletProcessor implementation
class DigitalWalletProcessor : PaymentProcessor {
    
    override val processorName = "DigitalWalletProcessor"
    override val supportedMethods = setOf("PAYPAL", "APPLE_PAY", "GOOGLE_PAY", "VENMO")
    override val processingFeeRate = 0.03 // 3%
    
    override suspend fun processPayment(method: PaymentMethod, amount: Double): PaymentResult {
        // TODO: Smart cast and process digital wallet payment
        return when (method) {
            is PaymentMethod.DigitalWallet -> {
                // TODO: Digital wallet specific processing
                // 1. Validate wallet details
                // 2. Check wallet balance
                // 3. Process payment
                // 4. Return appropriate result
                
                TODO("Implement digital wallet processing logic")
            }
            else -> PaymentResult.Failed(
                "UNSUPPORTED_METHOD",
                "Digital wallet processor cannot handle ${method::class.simpleName}",
                method,
                amount
            )
        }
    }
}
```

### Step 5: Create Auditing Interface and Implementation

**File**: `class/workshop/lesson_3/src/main/kotlin/Auditing.kt`

```kotlin
/**
 * Lesson 3 Workshop: Auditing System
 * 
 * TODO: Create auditing interface and implementation for delegation
 * This demonstrates:
 * - Interface for cross-cutting concerns
 * - Implementation for delegation
 * - Audit logging patterns
 */

// TODO: Create Auditable interface
interface Auditable {
    fun auditPaymentAttempt(method: PaymentMethod, amount: Double)
    fun auditPaymentResult(result: PaymentResult)
    fun auditSecurityEvent(event: String, details: Map<String, Any>)
}

// TODO: Create PaymentAuditor implementation
class PaymentAuditor : Auditable {
    
    // TODO: Implement audit methods
    override fun auditPaymentAttempt(method: PaymentMethod, amount: Double) {
        // TODO: Log payment attempt with method type and amount
        // Format: "AUDIT: Payment attempt - {method type} - ${amount}"
        TODO("Implement payment attempt auditing")
    }
    
    override fun auditPaymentResult(result: PaymentResult) {
        // TODO: Log payment result with appropriate details
        // Use when expression to handle different result types
        when (result) {
            is PaymentResult.Success -> TODO("Audit successful payment")
            is PaymentResult.Failed -> TODO("Audit failed payment")  
            is PaymentResult.Pending -> TODO("Audit pending payment")
            is PaymentResult.Cancelled -> TODO("Audit cancelled payment")
        }
    }
    
    override fun auditSecurityEvent(event: String, details: Map<String, Any>) {
        // TODO: Log security events like suspicious activity
        // Format audit entry with timestamp and details
        TODO("Implement security event auditing")
    }
    
    // TODO: Add helper methods
    private fun formatAuditEntry(level: String, message: String, details: Any? = null): String {
        // TODO: Format audit entries consistently
        // Include timestamp, level, message, and optional details
        return TODO("Format audit entry with timestamp and details")
    }
}

// TODO: Create ComplianceAuditor for regulatory compliance
class ComplianceAuditor : Auditable {
    
    // TODO: Implement compliance-specific auditing
    override fun auditPaymentAttempt(method: PaymentMethod, amount: Double) {
        // TODO: Check for compliance requirements
        // Flag large transactions, international payments, etc.
        TODO("Implement compliance auditing for payment attempts")
    }
    
    override fun auditPaymentResult(result: PaymentResult) {
        // TODO: Compliance-specific result auditing
        // Track successful transactions for reporting
        TODO("Implement compliance auditing for payment results")
    }
    
    override fun auditSecurityEvent(event: String, details: Map<String, Any>) {
        // TODO: Compliance security event logging
        // Store events for regulatory reporting
        TODO("Implement compliance security auditing")
    }
    
    // TODO: Add compliance-specific methods
    fun generateComplianceReport(): String {
        // TODO: Generate compliance report
        return TODO("Generate compliance report from audit data")
    }
}
```

### Step 6: Create Payment Service with Delegation

**File**: `class/workshop/lesson_3/src/main/kotlin/PaymentService.kt`

```kotlin
/**
 * Lesson 3 Workshop: Payment Service with Delegation
 * 
 * TODO: Create service that uses delegation and coordinates processors
 * This demonstrates:
 * - Class delegation with 'by' keyword
 * - Processor selection logic
 * - Error handling and validation
 */

// TODO: Create PaymentService that delegates auditing
class PaymentService(
    private val processors: Map<String, PaymentProcessor>,
    auditor: Auditable
) : Auditable by auditor { // TODO: Delegate all Auditable methods to auditor
    
    // TODO: Constructor with default processors
    constructor(auditor: Auditable = PaymentAuditor()) : this(
        processors = mapOf(
            // TODO: Initialize default processors
            // "CREDIT_CARD" to CreditCardProcessor(),
            // "BANK_TRANSFER" to BankTransferProcessor(), 
            // "DIGITAL_WALLET" to DigitalWalletProcessor()
        ),
        auditor = auditor
    )
    
    // TODO: Main payment processing method
    suspend fun processPayment(method: PaymentMethod, amount: Double): PaymentResult {
        // TODO: Implement payment processing workflow:
        // 1. Audit payment attempt
        // 2. Validate inputs
        // 3. Select appropriate processor
        // 4. Process payment
        // 5. Audit result
        // 6. Return result
        
        try {
            // TODO: Audit the attempt (uses delegated method)
            auditPaymentAttempt(method, amount)
            
            // TODO: Validate inputs
            val validationResult = validatePaymentRequest(method, amount)
            if (validationResult != null) {
                return validationResult // Return validation error
            }
            
            // TODO: Select processor
            val processor = selectProcessor(method)
                ?: return PaymentResult.Failed(
                    "NO_PROCESSOR",
                    "No processor available for ${method::class.simpleName}",
                    method,
                    amount
                )
            
            // TODO: Process payment
            val result = processor.executePayment(method, amount)
            
            // TODO: Audit result (uses delegated method)
            auditPaymentResult(result)
            
            return result
            
        } catch (e: Exception) {
            // TODO: Handle exceptions and create error result
            val errorResult = PaymentResult.Failed(
                "PROCESSING_ERROR",
                "Payment processing failed: ${e.message}",
                method,
                amount
            )
            
            auditPaymentResult(errorResult)
            return errorResult
        }
    }
    
    // TODO: Processor selection logic
    private fun selectProcessor(method: PaymentMethod): PaymentProcessor? {
        // TODO: Use when expression to select processor based on payment method type
        return when (method) {
            is PaymentMethod.CreditCard -> TODO("Return credit card processor")
            is PaymentMethod.BankAccount -> TODO("Return bank transfer processor")
            is PaymentMethod.DigitalWallet -> TODO("Return digital wallet processor")
        }
    }
    
    // TODO: Input validation
    private fun validatePaymentRequest(method: PaymentMethod, amount: Double): PaymentResult? {
        // TODO: Validate payment request and return error result if invalid
        // Check for:
        // - Negative or zero amount
        // - Inactive payment method
        // - Method-specific validations
        
        return TODO("Implement payment request validation")
    }
    
    // TODO: Batch payment processing
    suspend fun processBatchPayments(payments: List<Pair<PaymentMethod, Double>>): List<PaymentResult> {
        // TODO: Process multiple payments and return results
        // Use map to process each payment
        return TODO("Implement batch payment processing")
    }
    
    // TODO: Payment method management
    fun addProcessor(type: String, processor: PaymentProcessor) {
        // TODO: Add new processor type
        // Note: This would require making processors mutable
        TODO("Implement processor addition")
    }
    
    // TODO: Get supported payment methods
    fun getSupportedPaymentMethods(): Set<String> {
        // TODO: Return all supported payment method types
        return TODO("Return supported payment method types from all processors")
    }
    
    // TODO: Get processor statistics
    fun getProcessorStats(): Map<String, Any> {
        // TODO: Return statistics about processors
        return TODO("Return processor statistics")
    }
}
```

### Step 7: Create Main Application with Smart Casting Demo

**File**: `class/workshop/lesson_3/src/main/kotlin/Main.kt`

```kotlin
/**
 * Lesson 3 Workshop: Main Application
 * 
 * TODO: Create main application demonstrating all OOP features
 * This demonstrates:
 * - Smart casting in action
 * - Exhaustive when expressions  
 * - Sealed class pattern matching
 * - Interface delegation usage
 */

suspend fun main() {
    println("=== Payment Processing System ===")
    println("🚀 Welcome to Lesson 3: OOP + Kotlin Features!\n")
    
    val paymentService = PaymentService()
    
    // TODO: Create sample payment methods
    val creditCard = TODO("Create sample credit card")
    val bankAccount = TODO("Create sample bank account") 
    val digitalWallet = TODO("Create sample digital wallet")
    
    val paymentMethods = listOf(creditCard, bankAccount, digitalWallet)
    val amounts = listOf(250.0, 1500.0, 75.0)
    
    // TODO: Process each payment method
    paymentMethods.zip(amounts).forEachIndexed { index, (method, amount) ->
        println("${index + 1}. ${getPaymentMethodIcon(method)} Processing ${getPaymentMethodName(method)}:")
        
        // TODO: Process payment and handle result
        val result = paymentService.processPayment(method, amount)
        
        // TODO: Display result using when expression
        displayPaymentResult(result)
        
        println()
    }
    
    // TODO: Demonstrate smart casting
    println("🔍 Smart Casting Demonstrations:")
    demonstrateSmartCasting(paymentMethods)
    
    // TODO: Demonstrate when expressions
    println("\n🎯 When Expression Demonstrations:")
    demonstrateWhenExpressions(paymentMethods)
    
    // TODO: Demonstrate sealed class exhaustiveness
    println("\n✅ Exhaustive When Expressions:")
    demonstrateExhaustiveWhen(paymentMethods)
    
    println("\n🎉 Workshop complete! You've mastered Kotlin OOP features!")
    println("📚 Next: Lesson 4 - Spring Boot Setup & Dependency Injection")
}

// TODO: Smart casting demonstration
fun demonstrateSmartCasting(methods: List<PaymentMethod>) {
    methods.forEach { method ->
        // TODO: Use smart casting to access type-specific properties
        when (method) {
            is PaymentMethod.CreditCard -> {
                // TODO: Access credit card specific properties
                println("Credit Card: ${method.maskedNumber} expires ${method.expiryMonth}/${method.expiryYear}")
                if (method.isExpired) {
                    println("⚠️ Card is expired!")
                }
            }
            is PaymentMethod.BankAccount -> {
                // TODO: Access bank account specific properties  
                println("Bank Account: ${method.maskedAccountNumber} at ${method.bankName}")
                println("Available balance: $${method.balance}")
            }
            is PaymentMethod.DigitalWallet -> {
                // TODO: Access digital wallet specific properties
                println("Digital Wallet: ${method.displayName}")
                println("Balance: $${method.balance} ${method.currency}")
            }
        }
    }
}

// TODO: When expression demonstrations
fun demonstrateWhenExpressions(methods: List<PaymentMethod>) {
    methods.forEach { method ->
        // TODO: Demonstrate different when expression patterns
        
        // When with is checks
        val description = when (method) {
            is PaymentMethod.CreditCard -> TODO("Return credit card description")
            is PaymentMethod.BankAccount -> TODO("Return bank account description")
            is PaymentMethod.DigitalWallet -> TODO("Return digital wallet description")
        }
        
        println("Description: $description")
        
        // TODO: When with conditions
        val riskLevel = when {
            TODO("Implement risk level logic based on method and amount")
        }
        
        println("Risk Level: $riskLevel")
    }
}

// TODO: Exhaustive when demonstration
fun demonstrateExhaustiveWhen(methods: List<PaymentMethod>) {
    methods.forEach { method ->
        // TODO: Show exhaustive when that compiler enforces
        val processingTime = when (method) {
            is PaymentMethod.CreditCard -> "Instant"
            is PaymentMethod.BankAccount -> "1-3 business days"
            is PaymentMethod.DigitalWallet -> "Instant"
            // Note: No else clause needed - compiler ensures all cases covered
        }
        
        println("${getPaymentMethodName(method)} processing time: $processingTime")
    }
}

// TODO: Helper functions using smart casting
fun getPaymentMethodName(method: PaymentMethod): String = when (method) {
    is PaymentMethod.CreditCard -> TODO("Return credit card name with type")
    is PaymentMethod.BankAccount -> TODO("Return bank account name")
    is PaymentMethod.DigitalWallet -> TODO("Return wallet name with type")
}

fun getPaymentMethodIcon(method: PaymentMethod): String = when (method) {
    is PaymentMethod.CreditCard -> TODO("Return appropriate credit card icon")
    is PaymentMethod.BankAccount -> "🏦"
    is PaymentMethod.DigitalWallet -> TODO("Return wallet-specific icon")
}

// TODO: Result display function
fun displayPaymentResult(result: PaymentResult) {
    // TODO: Use when expression to display different result types
    when (result) {
        is PaymentResult.Success -> {
            // TODO: Display success information
            println("✅ Payment Successful!")
            println("   Transaction ID: ${result.transactionId}")
            println("   Amount: ${result.getFormattedAmount()}")
            println("   Fee: $${result.fee}")
            println("   Total: $${result.total}")
        }
        is PaymentResult.Failed -> {
            // TODO: Display failure information
            println("❌ Payment Failed!")
            println("   Error: ${result.errorMessage}")
            println("   Code: ${result.errorCode}")
            if (result.isRetryable()) {
                println("   💡 Retry recommended in ${result.getRetryDelay()}ms")
            }
        }
        is PaymentResult.Pending -> {
            // TODO: Display pending information
            println("⏳ Payment Pending")
            println("   Transaction ID: ${result.transactionId}")
            println("   Estimated completion: ${result.estimatedCompletionTime}ms")
        }
        is PaymentResult.Cancelled -> {
            // TODO: Display cancellation information
            println("🚫 Payment Cancelled")
            println("   Reason: ${result.reason}")
        }
    }
}
```

## How to Run

1. **Navigate to the workshop directory:**
   ```bash
   cd class/workshop/lesson_3
   ```

2. **Build the project:**
   ```bash
   ./gradlew build
   ```

3. **Run the payment system:**
   ```bash
   ./gradlew run
   ```

4. **Run the tests:**
   ```bash
   ./gradlew test
   ```

## Success Criteria

Your implementation should:
- ✅ **Use sealed classes effectively** - PaymentMethod and PaymentResult hierarchies
- ✅ **Implement interfaces with defaults** - PaymentProcessor with template methods
- ✅ **Demonstrate delegation** - PaymentService delegates to Auditable
- ✅ **Show smart casting** - Access type-specific properties safely
- ✅ **Use exhaustive when** - Handle all sealed class cases without else
- ✅ **Handle errors gracefully** - Proper validation and error reporting

## Key Learning Points

After completing this workshop, you should understand:

1. **Sealed Classes**: Type-safe hierarchies with exhaustive pattern matching
2. **Interface Default Methods**: Implementing template methods and common behavior
3. **Class Delegation**: "Composition over inheritance" with `by` keyword
4. **Smart Casting**: Automatic type casting after type checks
5. **When Expressions**: Powerful pattern matching beyond simple switch statements
6. **Property Delegation**: Custom property behavior with delegates

## Advanced Challenges

Once you complete the basic workshop:

1. **Add more payment methods** - Gift cards, cryptocurrency, bank drafts
2. **Implement retry logic** - Automatic retry with exponential backoff
3. **Add fraud detection** - Suspicious transaction detection
4. **Create payment workflows** - Multi-step payment processes
5. **Add transaction history** - Store and query payment history

## Common Patterns Demonstrated

### Type-Safe Error Handling
```kotlin
sealed class PaymentResult {
    data class Success(...) : PaymentResult()
    data class Failed(...) : PaymentResult()
}
```

### Template Method Pattern
```kotlin
interface PaymentProcessor {
    suspend fun executePayment(...): PaymentResult {
        validate()
        val result = process()
        log(result)
        return result
    }
}
```

### Delegation Pattern
```kotlin
class PaymentService(auditor: Auditable) : Auditable by auditor
```

## Next Steps

- Compare your solution with `class/answer/lesson_3/`
- Experiment with adding new payment methods
- Try creating your own sealed class hierarchies
- Move on to Lesson 4: Spring Boot Setup & Dependency Injection

**Congratulations!** You've mastered Kotlin's advanced OOP features and built a sophisticated, type-safe payment system. These patterns are fundamental to building robust, maintainable Kotlin applications!