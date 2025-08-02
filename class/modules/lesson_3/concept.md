# 🎯 Lesson 3: OOP + Kotlin Features

## Objective

Master Kotlin's advanced object-oriented features that enable safe, expressive, and maintainable code. Learn sealed classes for type-safe hierarchies, interfaces with default implementations, delegation patterns, and smart casting for elegant conditional logic.

## Key Concepts

### 1. Sealed Classes - Type-Safe Hierarchies

Sealed classes represent restricted inheritance hierarchies where all subclasses are known at compile time.

```kotlin
sealed class PaymentResult {
    data class Success(val transactionId: String, val amount: Double) : PaymentResult()
    data class Failed(val error: String, val errorCode: Int) : PaymentResult()
    data class Pending(val estimatedTime: Long) : PaymentResult()
}

// Exhaustive when expressions - compiler ensures all cases handled
fun handlePayment(result: PaymentResult): String = when (result) {
    is PaymentResult.Success -> "Payment successful: ${result.transactionId}"
    is PaymentResult.Failed -> "Payment failed: ${result.error}"
    is PaymentResult.Pending -> "Payment pending for ${result.estimatedTime}ms"
    // No else clause needed - all cases covered!
}
```

**Why sealed classes?**
- **Type safety**: All possible subtypes known at compile time
- **Exhaustive matching**: Compiler ensures all cases handled in `when`
- **Evolution safety**: Adding new subtypes causes compilation errors where they need handling

### 2. Interfaces with Default Implementations

Kotlin interfaces can contain default implementations, making them more powerful than Java interfaces.

```kotlin
interface PaymentProcessor {
    val processingFee: Double get() = 0.0
    
    fun processPayment(amount: Double): PaymentResult
    
    // Default implementation with business logic
    fun calculateTotal(amount: Double): Double {
        return amount + (amount * processingFee)
    }
    
    // Default logging behavior
    fun logTransaction(result: PaymentResult) {
        println("Transaction processed: $result")
    }
}

class CreditCardProcessor : PaymentProcessor {
    override val processingFee = 0.03 // 3% fee
    
    override fun processPayment(amount: Double): PaymentResult {
        // Credit card specific logic
        return if (amount <= 10000) {
            PaymentResult.Success("CC-${System.currentTimeMillis()}", amount)
        } else {
            PaymentResult.Failed("Amount exceeds limit", 1001)
        }
    }
    // calculateTotal() and logTransaction() inherited with default behavior
}
```

### 3. Class Delegation - "Composition over Inheritance"

Kotlin provides built-in support for the delegation pattern.

```kotlin
interface Auditable {
    fun audit(action: String)
}

class DatabaseAuditor : Auditable {
    override fun audit(action: String) {
        println("DB Audit: $action at ${System.currentTimeMillis()}")
    }
}

class PaymentService(
    private val processor: PaymentProcessor,
    auditor: Auditable
) : Auditable by auditor { // Delegate all Auditable calls to auditor
    
    fun makePayment(amount: Double): PaymentResult {
        audit("Payment attempt: $amount") // Calls auditor.audit()
        
        val result = processor.processPayment(amount)
        
        audit("Payment result: $result") // Calls auditor.audit()
        
        return result
    }
}
```

### 4. Property Delegation

Kotlin provides built-in property delegates and allows custom ones.

```kotlin
class PaymentAccount {
    // Lazy initialization - computed only when first accessed
    val paymentHistory: List<Payment> by lazy {
        println("Loading payment history...")
        loadPaymentHistoryFromDatabase()
    }
    
    // Observable property - trigger actions on changes
    var balance: Double by Delegates.observable(0.0) { prop, old, new ->
        println("Balance changed from $old to $new")
        if (new < 0) {
            sendLowBalanceAlert()
        }
    }
    
    // Delegated to map - useful for dynamic properties
    private val properties = mutableMapOf<String, Any>()
    var accountType: String by properties
    var isActive: Boolean by properties
}
```

### 5. Smart Casts - Type-Safe Conditional Logic

Kotlin automatically casts types after type checks, eliminating explicit casting.

```kotlin
fun processPaymentMethod(method: Any): String {
    return when (method) {
        is CreditCard -> {
            // method is automatically cast to CreditCard here
            "Processing credit card ending in ${method.lastFourDigits}"
        }
        is BankAccount -> {
            // method is automatically cast to BankAccount here
            if (method.balance >= 100) {
                "Processing bank transfer from ${method.accountNumber}"
            } else {
                "Insufficient funds in account ${method.accountNumber}"
            }
        }
        is DigitalWallet -> {
            // method is automatically cast to DigitalWallet here
            "Processing ${method.walletType} payment"
        }
        else -> "Unsupported payment method"
    }
}

// Smart casts work with nullable types too
fun processNullablePayment(payment: Payment?) {
    if (payment != null) {
        // payment is automatically cast to non-null here
        println("Processing payment of ${payment.amount}")
        
        if (payment.status == PaymentStatus.PENDING) {
            // Can access payment properties safely
            payment.process()
        }
    }
}
```

### 6. When Expressions - Pattern Matching

`when` is more powerful than switch statements and works with any type.

```kotlin
fun getPaymentIcon(payment: PaymentMethod): String = when (payment) {
    is CreditCard -> when (payment.type) {
        CardType.VISA -> "💳"
        CardType.MASTERCARD -> "💳"
        CardType.AMEX -> "💎"
    }
    is BankAccount -> "🏦"
    is DigitalWallet -> when (payment.walletType) {
        "PayPal" -> "🅿️"
        "Apple Pay" -> "🍎"
        "Google Pay" -> "🔍"
        else -> "💰"
    }
}

// When with conditions
fun getTransactionFee(amount: Double, paymentType: PaymentType): Double = when {
    amount < 10 -> 0.50
    amount < 100 -> amount * 0.02
    paymentType == PaymentType.INTERNATIONAL -> amount * 0.05
    else -> amount * 0.03
}
```

## Best Practices

### ✅ Do:
- **Use sealed classes** for known, finite hierarchies (payment types, API responses, states)
- **Leverage smart casts** - let the compiler eliminate explicit casting
- **Prefer delegation** over inheritance when modeling "has-a" relationships
- **Use data classes** for sealed class subtypes when you need value semantics
- **Make `when` expressions exhaustive** - avoid `else` when all cases are covered

### ❌ Avoid:
- **Overusing inheritance** - favor composition and delegation
- **Ignoring exhaustive `when`** - compiler warnings indicate missing cases
- **Manual type casting** when smart casts would work
- **Large sealed hierarchies** - consider breaking down into smaller, focused hierarchies

## Real-World Applications

### API Response Modeling
```kotlin
sealed class ApiResponse<out T> {
    data class Success<T>(val data: T) : ApiResponse<T>()
    data class Error(val message: String, val code: Int) : ApiResponse<Nothing>()
    object Loading : ApiResponse<Nothing>()
}

fun handleUserResponse(response: ApiResponse<User>) = when (response) {
    is ApiResponse.Success -> displayUser(response.data)
    is ApiResponse.Error -> showError(response.message)
    ApiResponse.Loading -> showLoadingSpinner()
}
```

### State Machines
```kotlin
sealed class OrderState {
    object Draft : OrderState()
    object Submitted : OrderState()
    data class Processing(val estimatedCompletion: Long) : OrderState()
    data class Shipped(val trackingNumber: String) : OrderState()
    data class Delivered(val deliveryTime: Long) : OrderState()
    data class Cancelled(val reason: String) : OrderState()
}
```

### Domain Modeling
```kotlin
sealed class DiscountType {
    data class Percentage(val percent: Double) : DiscountType()
    data class FixedAmount(val amount: Double) : DiscountType()
    object BuyOneGetOne : DiscountType()
}

fun applyDiscount(price: Double, discount: DiscountType): Double = when (discount) {
    is DiscountType.Percentage -> price * (1 - discount.percent / 100)
    is DiscountType.FixedAmount -> (price - discount.amount).coerceAtLeast(0.0)
    DiscountType.BuyOneGetOne -> price / 2
}
```

## Advanced Patterns

### Sealed Interfaces
```kotlin
sealed interface Result<out T, out E>

data class Success<T>(val value: T) : Result<T, Nothing>
data class Failure<E>(val error: E) : Result<Nothing, E>

// Can implement multiple sealed interfaces
data class Loading(val progress: Float) : Result<Nothing, Nothing>
```

### Delegation with Custom Logic
```kotlin
class ValidatedProperty<T>(
    private val validator: (T) -> Boolean,
    private var value: T
) : ReadWriteProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value
    
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        if (validator(value)) {
            this.value = value
        } else {
            throw IllegalArgumentException("Invalid value: $value")
        }
    }
}

class PaymentRequest {
    var amount: Double by ValidatedProperty({ it > 0 }, 0.0)
    var email: String by ValidatedProperty({ it.contains("@") }, "")
}
```

## Performance Considerations

- **Sealed classes** have no runtime overhead compared to regular inheritance
- **Smart casts** eliminate runtime type checking overhead
- **Property delegation** may have slight overhead but provides significant maintainability benefits
- **When expressions** compile to efficient jump tables when possible

## Next Steps

This lesson prepares you for:
- Spring Boot dependency injection patterns (Lesson 4)
- REST API design with proper error handling (Lesson 5)
- Complex domain modeling in larger applications (Lessons 6+)

The OOP features you learn here are fundamental to writing clean, maintainable Kotlin code and are used extensively in real-world applications and frameworks like Spring Boot.