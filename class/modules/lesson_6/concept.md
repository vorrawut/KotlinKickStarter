# 🎯 Lesson 6: Request Validation & Error Handling

## Objective

Build robust APIs that gracefully handle invalid inputs and provide meaningful error responses. Master Bean Validation, custom validators, and global exception handling for production-grade applications.

## Key Concepts

### 1. Bean Validation with Annotations

```kotlin
data class CreatePaymentRequest(
    @field:NotNull(message = "Amount is required")
    @field:DecimalMin(value = "0.01", message = "Amount must be positive")
    @field:DecimalMax(value = "10000.00", message = "Amount exceeds maximum limit")
    val amount: BigDecimal,
    
    @field:NotBlank(message = "Currency is required")
    @field:Pattern(regexp = "USD|EUR|GBP", message = "Unsupported currency")
    val currency: String,
    
    @field:Valid
    val paymentMethod: PaymentMethodDto,
    
    @field:Email(message = "Invalid email format")
    val customerEmail: String
)

@RestController
class PaymentController(private val paymentService: PaymentService) {
    
    @PostMapping("/payments")
    fun createPayment(@Valid @RequestBody request: CreatePaymentRequest): ResponseEntity<PaymentResponse> {
        // Validation happens automatically before method execution
        return ResponseEntity.ok(paymentService.processPayment(request))
    }
}
```

### 2. Custom Validators

```kotlin
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [CreditCardValidator::class])
annotation class ValidCreditCard(
    val message: String = "Invalid credit card number",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class CreditCardValidator : ConstraintValidator<ValidCreditCard, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value.isNullOrBlank()) return false
        
        // Luhn algorithm validation
        return value.replace("\\s".toRegex(), "")
            .reversed()
            .mapIndexed { index, char ->
                val digit = char.digitToInt()
                if (index % 2 == 1) {
                    val doubled = digit * 2
                    if (doubled > 9) doubled - 9 else doubled
                } else digit
            }
            .sum() % 10 == 0
    }
}
```

### 3. Global Exception Handling

```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.map { error ->
            FieldError(
                field = error.field,
                message = error.defaultMessage ?: "Invalid value",
                rejectedValue = error.rejectedValue
            )
        }
        
        return ResponseEntity.badRequest().body(
            ErrorResponse(
                code = "VALIDATION_ERROR",
                message = "Request validation failed",
                errors = errors
            )
        )
    }
    
    @ExceptionHandler(PaymentProcessingException::class)
    fun handlePaymentErrors(ex: PaymentProcessingException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
            ErrorResponse(
                code = ex.errorCode,
                message = ex.message ?: "Payment processing failed"
            )
        )
    }
    
    @ExceptionHandler(Exception::class)
    fun handleGenericErrors(ex: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ErrorResponse(
                code = "INTERNAL_ERROR",
                message = "An unexpected error occurred"
            )
        )
    }
}

data class ErrorResponse(
    val code: String,
    val message: String,
    val errors: List<FieldError> = emptyList(),
    val timestamp: Instant = Instant.now()
)

data class FieldError(
    val field: String,
    val message: String,
    val rejectedValue: Any?
)
```

## Best Practices

### ✅ Do:
- **Validate at API boundaries** - use @Valid on controller parameters
- **Provide clear error messages** - help developers understand what went wrong
- **Use appropriate HTTP status codes** - 400 for validation, 422 for business logic
- **Log errors appropriately** - debug info for 4xx, full stack traces for 5xx
- **Sanitize error responses** - don't expose sensitive internal details

### ❌ Avoid:
- **Exposing stack traces** to clients in production
- **Generic error messages** that don't help users
- **Validating in multiple layers** unnecessarily
- **Ignoring validation failures** - always handle them properly

## Real-World Implementation

```kotlin
@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val fraudDetectionService: FraudDetectionService
) {
    
    @Transactional
    fun processPayment(request: CreatePaymentRequest): PaymentResponse {
        // Business validation
        if (fraudDetectionService.isSuspicious(request)) {
            throw PaymentProcessingException("FRAUD_DETECTED", "Transaction flagged for review")
        }
        
        // Domain validation
        val payment = Payment.create(request)
        if (!payment.isValid()) {
            throw PaymentProcessingException("INVALID_PAYMENT", "Payment validation failed")
        }
        
        return try {
            val savedPayment = paymentRepository.save(payment)
            savedPayment.toResponse()
        } catch (e: DataIntegrityViolationException) {
            throw PaymentProcessingException("DUPLICATE_PAYMENT", "Payment already exists")
        }
    }
}
```

This lesson prepares you for building reliable, user-friendly APIs that handle edge cases gracefully.