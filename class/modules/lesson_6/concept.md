# 🛡️ Lesson 6: Request Validation & Error Handling - Concepts

## 🎯 Learning Objectives

By the end of this lesson, you will understand:
- **Bean Validation** with standard and custom annotations
- **Global error handling** with @ControllerAdvice
- **Custom validation logic** beyond annotations
- **Structured error responses** for APIs
- **Security considerations** in validation

---

## 🔍 Why Validation & Error Handling Matter

### The Reality of Production APIs
```kotlin
// ❌ What happens without proper validation
@PostMapping("/users")
fun createUser(@RequestBody user: CreateUserRequest): User {
    return userService.create(user) // 💥 Crashes on invalid data
}

// ✅ Production-ready validation
@PostMapping("/users")
fun createUser(@Valid @RequestBody user: CreateUserRequest): ResponseEntity<UserResponse> {
    return try {
        val created = userService.validateAndCreate(user)
        ResponseEntity.ok(userResponse(created))
    } catch (ex: ValidationException) {
        // Handled by global exception handler
        throw ex
    }
}
```

### Business Impact
- **Security**: Prevent injection attacks and data corruption
- **User Experience**: Clear, actionable error messages
- **System Stability**: Graceful handling of invalid input
- **Debugging**: Structured errors for faster problem resolution

---

## 📚 Bean Validation Fundamentals

### Standard Validation Annotations

```kotlin
data class CreateUserRequest(
    @field:NotBlank(message = "Username cannot be blank")
    @field:Size(min = 3, max = 20, message = "Username must be 3-20 characters")
    @field:Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscore")
    val username: String,
    
    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email is required")
    val email: String,
    
    @field:Past(message = "Date of birth must be in the past")
    @field:NotNull(message = "Date of birth is required")
    val dateOfBirth: LocalDate,
    
    @field:Valid // Validate nested object
    val address: CreateAddressRequest?
)
```

### Key Annotations Explained

| Annotation | Purpose | Example |
|------------|---------|---------|
| `@NotNull` | Field cannot be null | `@NotNull val id: String` |
| `@NotBlank` | String cannot be null, empty, or whitespace | `@NotBlank val name: String` |
| `@NotEmpty` | Collection/array cannot be null or empty | `@NotEmpty val tags: Set<String>` |
| `@Size` | Validates size constraints | `@Size(min=2, max=50) val name: String` |
| `@Min/@Max` | Numeric range validation | `@Min(0) @Max(120) val age: Int` |
| `@Email` | Email format validation | `@Email val email: String` |
| `@Pattern` | Regular expression validation | `@Pattern(regexp="...") val code: String` |
| `@Valid` | Triggers validation of nested objects | `@Valid val address: Address` |

---

## 🎨 Custom Validation Patterns

### Creating Custom Annotations

```kotlin
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [AgeValidator::class])
annotation class ValidAge(
    val message: String = "Invalid age",
    val min: Int = 0,
    val max: Int = 150,
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class AgeValidator : ConstraintValidator<ValidAge, LocalDate?> {
    private var min: Int = 0
    private var max: Int = 150
    
    override fun initialize(constraintAnnotation: ValidAge) {
        min = constraintAnnotation.min
        max = constraintAnnotation.max
    }
    
    override fun isValid(value: LocalDate?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return true // Let @NotNull handle null checks
        
        val age = Period.between(value, LocalDate.now()).years
        return age in min..max
    }
}
```

### Cross-Field Validation

```kotlin
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [DateRangeValidator::class])
annotation class ValidDateRange(
    val message: String = "End date must be after start date",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class DateRangeValidator : ConstraintValidator<ValidDateRange, Any> {
    override fun isValid(value: Any, context: ConstraintValidatorContext): Boolean {
        // Use reflection to get startDate and endDate fields
        val startDate = getFieldValue(value, "startDate") as? LocalDate
        val endDate = getFieldValue(value, "endDate") as? LocalDate
        
        return when {
            startDate == null || endDate == null -> true
            endDate.isAfter(startDate) -> true
            else -> {
                context.disableDefaultConstraintViolation()
                context.buildConstraintViolationWithTemplate("End date must be after start date")
                    .addPropertyNode("endDate")
                    .addConstraintViolation()
                false
            }
        }
    }
}
```

---

## 🏗️ Global Exception Handling

### Centralized Error Management

```kotlin
@ControllerAdvice
class GlobalExceptionHandler {
    
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(
        ex: MethodArgumentNotValidException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        
        val fieldErrors = ex.bindingResult.fieldErrors.map { fieldError ->
            FieldErrorDetail(
                field = fieldError.field,
                rejectedValue = fieldError.rejectedValue,
                message = fieldError.defaultMessage ?: "Invalid value",
                errorCode = fieldError.code
            )
        }
        
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Validation Failed",
            message = "Invalid request parameters",
            errorCode = "VALIDATION_ERROR",
            path = getRequestPath(request),
            correlationId = generateCorrelationId(),
            fieldErrors = fieldErrors
        )
        
        logger.warn("Validation error: {}", errorResponse)
        return ResponseEntity.badRequest().body(errorResponse)
    }
    
    @ExceptionHandler(ValidationException::class)
    fun handleCustomValidation(
        ex: ValidationException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = ex.getHttpStatus().value(),
            error = ex.getHttpStatus().reasonPhrase,
            message = ex.message ?: "Validation error",
            errorCode = ex.errorCode,
            path = getRequestPath(request),
            correlationId = generateCorrelationId(),
            metadata = ex.getErrorMetadata()
        )
        
        logError(ex, request, errorResponse)
        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse)
    }
}
```

### Structured Error Responses

```kotlin
data class ErrorResponse(
    val timestamp: LocalDateTime,
    val status: Int,
    val error: String,
    val message: String,
    val errorCode: String,
    val path: String,
    val correlationId: String,
    val details: String? = null,
    val fieldErrors: List<FieldErrorDetail>? = null,
    val metadata: Map<String, Any?>? = null
)

data class FieldErrorDetail(
    val field: String,
    val rejectedValue: Any?,
    val message: String,
    val errorCode: String? = null
)
```

---

## 🔧 Service-Layer Validation

### Beyond Annotations

```kotlin
@Service
class UserValidationService {
    
    fun validateAndCreateUser(request: CreateUserRequest): User {
        // 1. Bean validation happens automatically with @Valid
        
        // 2. Business rule validation
        validateBusinessRules(request)
        
        // 3. External service validation
        validateWithExternalServices(request)
        
        // 4. Data consistency validation
        validateDataConsistency(request)
        
        return createUser(request)
    }
    
    private fun validateBusinessRules(request: CreateUserRequest) {
        // Check for duplicate username/email
        if (userRepository.existsByUsername(request.username)) {
            throw DuplicateResourceException(
                resourceType = "User",
                field = "username",
                value = request.username
            )
        }
        
        // Age eligibility for service
        val age = Period.between(request.dateOfBirth, LocalDate.now()).years
        if (age < 18) {
            throw BusinessRuleViolationException(
                message = "Users must be at least 18 years old",
                ruleName = "MinimumAge",
                field = "dateOfBirth",
                value = request.dateOfBirth
            )
        }
        
        // Role assignment validation
        if (request.roles.contains(UserRole.ADMIN) && !hasAdminCreationPermission()) {
            throw OperationNotAllowedException(
                operation = "createAdmin",
                reason = "Insufficient permissions to create admin users"
            )
        }
    }
    
    private fun validateWithExternalServices(request: CreateUserRequest): ValidationResult {
        val results = mutableListOf<ValidationResult>()
        
        // Email validation service
        if (emailValidationEnabled) {
            results.add(emailValidationService.validate(request.email))
        }
        
        // Phone validation service
        request.phoneNumber?.let { phone ->
            results.add(phoneValidationService.validate(phone))
        }
        
        // Address validation service
        request.address?.let { address ->
            results.add(addressValidationService.validate(address))
        }
        
        return aggregateValidationResults(results)
    }
}
```

---

## 🔒 Security Considerations

### Input Sanitization

```kotlin
object ValidationUtils {
    
    fun sanitizeInput(input: String): String {
        return input
            .replace(Regex("[<>\"'&]"), "") // Remove potential XSS characters
            .trim()
            .take(1000) // Limit input length
    }
    
    fun containsSqlInjectionPatterns(input: String): Boolean {
        val sqlPatterns = listOf(
            "(?i).*\\b(SELECT|INSERT|UPDATE|DELETE|DROP|CREATE|ALTER)\\b.*",
            "(?i).*\\b(UNION|OR|AND)\\s+\\d+\\s*=\\s*\\d+.*",
            "(?i).*['\"](\\s|\\w)*['\"](\\s)*(=|LIKE|IN).*"
        )
        
        return sqlPatterns.any { pattern ->
            input.matches(Regex(pattern))
        }
    }
    
    fun isValidEmailFormat(email: String): Boolean {
        val emailRegex = Regex(
            "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"
        )
        return email.matches(emailRegex) && email.length <= 254
    }
}
```

### Rate Limiting and DoS Protection

```kotlin
@Component
class ValidationRateLimiter {
    
    private val requestCounts = ConcurrentHashMap<String, AtomicInteger>()
    private val lastResetTime = ConcurrentHashMap<String, Long>()
    
    fun isAllowed(clientId: String, maxRequests: Int, windowMs: Long): Boolean {
        val now = System.currentTimeMillis()
        val windowStart = lastResetTime.computeIfAbsent(clientId) { now }
        
        if (now - windowStart > windowMs) {
            // Reset window
            requestCounts[clientId] = AtomicInteger(0)
            lastResetTime[clientId] = now
        }
        
        val currentCount = requestCounts.computeIfAbsent(clientId) { AtomicInteger(0) }
        return currentCount.incrementAndGet() <= maxRequests
    }
}
```

---

## 📊 Validation Patterns & Best Practices

### 1. Validation Groups

```kotlin
interface BasicValidation
interface ExtendedValidation

data class CreateUserRequest(
    @field:NotBlank(groups = [BasicValidation::class])
    val username: String,
    
    @field:Size(min = 8, groups = [ExtendedValidation::class])
    val password: String
)

// Use different validation groups in different scenarios
@PostMapping("/users/quick")
fun createUserQuick(@Validated(BasicValidation::class) @RequestBody request: CreateUserRequest) {
    // Only basic validation
}

@PostMapping("/users/full")
fun createUserFull(@Validated(ExtendedValidation::class) @RequestBody request: CreateUserRequest) {
    // Extended validation
}
```

### 2. Conditional Validation

```kotlin
data class ProductRequest(
    val isDigital: Boolean,
    
    @field:NotNull
    @field:Positive
    val weight: Double?, // Required only for physical products
    
    @field:Valid
    val dimensions: ProductDimensions? // Required only for physical products
) {
    @AssertTrue(message = "Physical products must have weight and dimensions")
    fun isValidPhysicalProduct(): Boolean {
        return if (!isDigital) {
            weight != null && weight > 0 && dimensions != null
        } else true
    }
    
    @AssertTrue(message = "Digital products cannot have physical properties")
    fun isValidDigitalProduct(): Boolean {
        return if (isDigital) {
            weight == null && dimensions == null
        } else true
    }
}
```

### 3. Async Validation

```kotlin
@Service
class AsyncValidationService {
    
    @Async
    fun validateEmailAsync(email: String): CompletableFuture<ValidationResult> {
        return CompletableFuture.supplyAsync {
            // Call external email validation service
            emailValidationService.validate(email)
        }.orTimeout(5, TimeUnit.SECONDS)
    }
    
    fun validateUserWithExternalServices(request: CreateUserRequest): ValidationResult {
        val emailValidation = validateEmailAsync(request.email)
        val phoneValidation = request.phoneNumber?.let { 
            validatePhoneAsync(it) 
        }
        
        // Wait for all validations to complete
        val results = listOfNotNull(
            emailValidation.get(),
            phoneValidation?.get()
        )
        
        return aggregateResults(results)
    }
}
```

---

## 🎯 Key Takeaways

### What You've Learned

1. **Bean Validation**: Standard annotations for common validation scenarios
2. **Custom Validators**: Creating domain-specific validation logic
3. **Global Error Handling**: Centralized, consistent error responses
4. **Service Validation**: Complex business rule validation beyond annotations
5. **Security**: Input sanitization and injection prevention
6. **Performance**: Async validation and rate limiting

### Production-Ready Patterns

- ✅ **Layered Validation**: Annotations + service logic + external validation
- ✅ **Structured Errors**: Consistent, parseable error responses
- ✅ **Security First**: Always sanitize and validate user input
- ✅ **Performance Aware**: Rate limiting and async validation
- ✅ **Monitoring**: Correlation IDs and proper logging

### Next Steps

In the next lesson, we'll explore **Service Layer & Clean Architecture**, building on this validation foundation to create well-structured, maintainable applications.

---

*Remember: Validation is your first line of defense against bad data, security vulnerabilities, and system instability. Invest in robust validation early!*