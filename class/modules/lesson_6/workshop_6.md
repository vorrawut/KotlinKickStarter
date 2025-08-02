# 🛡️ Lesson 6 Workshop: Request Validation & Error Handling

## 🎯 What You'll Build

A comprehensive **User & Product Management API** with production-grade validation and error handling that demonstrates:
- **Bean Validation** with standard and custom annotations
- **Global error handling** with structured responses
- **Custom business rule validation**
- **Multi-layer validation strategy**

**Expected Duration**: 45-60 minutes

---

## 🚀 Getting Started

### Prerequisites
- Completed Lesson 5 (REST Controllers & DTOs)
- Understanding of Spring Boot basics
- Familiarity with HTTP status codes

### Project Setup
```bash
cd class/workshop/lesson_6
./gradlew build
```

---

## 📋 Workshop Tasks Overview

| Step | Task | Duration | Key Learning |
|------|------|----------|-------------|
| 1 | Application Setup | 5 min | Spring Boot validation configuration |
| 2 | Domain Models | 10 min | Business logic validation |
| 3 | DTOs with Validation | 15 min | Bean validation annotations |
| 4 | Custom Validators | 10 min | Creating custom validation logic |
| 5 | Exception Handling | 10 min | Global error handling patterns |
| 6 | Service Layer | 10 min | Business rule validation |
| 7 | Controllers | 15 min | Endpoint validation integration |
| 8 | Testing | 5 min | Validation testing strategies |

---

## 🔧 Step 1: Application Setup (5 minutes)

### Task 1.1: Complete the Main Application Class

Open `src/main/kotlin/com/learning/validation/ValidationApplication.kt`

**TODO**: Add the missing annotations and implementation:

```kotlin
@SpringBootApplication
class ValidationApplication

fun main(args: Array<String>) {
    runApplication<ValidationApplication>(*args)
}
```

**Key Points**:
- `@SpringBootApplication` enables auto-configuration
- `runApplication` starts the Spring Boot application
- Validation is enabled by default with `spring-boot-starter-validation`

### Task 1.2: Verify Configuration

Run the application to ensure it starts correctly:
```bash
./gradlew bootRun
```

You should see the application start on port 8080.

---

## 🏗️ Step 2: Complete Domain Models (10 minutes)

### Task 2.1: Complete User Domain Model

Open `src/main/kotlin/com/learning/validation/model/User.kt`

**TODO**: Implement the missing computed properties and validation methods:

```kotlin
val age: Int
    get() = Period.between(dateOfBirth, LocalDate.now()).years

val isActive: Boolean
    get() = status == UserStatus.ACTIVE

val fullName: String
    get() = "$firstName $lastName"

fun isEligibleForService(): Boolean {
    return age >= 18 && isVerified() && isActive
}

fun canPerformAction(action: String): Boolean {
    return when (action) {
        "read:profile" -> isActive
        "update:profile" -> isActive
        "delete:account" -> isActive && isVerified()
        "admin:manage" -> hasRole(UserRole.ADMIN) || hasRole(UserRole.SUPER_ADMIN)
        else -> false
    }
}

fun hasRole(role: UserRole): Boolean {
    return roles.contains(role)
}

fun isVerified(): Boolean {
    return isEmailVerified && (phoneNumber == null || isPhoneVerified)
}

fun daysUntilBirthday(): Int {
    val today = LocalDate.now()
    val thisYearBirthday = dateOfBirth.withYear(today.year)
    val nextBirthday = if (thisYearBirthday.isBefore(today) || thisYearBirthday.isEqual(today)) {
        thisYearBirthday.plusYears(1)
    } else {
        thisYearBirthday
    }
    return Period.between(today, nextBirthday).days
}
```

### Task 2.2: Complete Address Validation

In the same file, complete the `Address` data class:

```kotlin
fun isValid(): Boolean {
    return street.isNotBlank() && 
           city.isNotBlank() && 
           state.isNotBlank() && 
           zipCode.isNotBlank() && 
           country.isNotBlank()
}

fun getFormattedAddress(): String {
    return "$street, $city, $state $zipCode, $country"
}
```

### Task 2.3: Complete Product Domain Model

Open `src/main/kotlin/com/learning/validation/model/Product.kt`

**TODO**: Implement the business validation methods:

```kotlin
fun isAvailable(): Boolean {
    return status == ProductStatus.ACTIVE && stockQuantity > 0
}

fun canOrderQuantity(quantity: Int): Boolean {
    return isAvailable() && 
           quantity >= minOrderQuantity && 
           (maxOrderQuantity == null || quantity <= maxOrderQuantity!!) &&
           quantity <= stockQuantity
}

fun calculateShippingWeight(): Double {
    return if (isDigital) 0.0 else (weight ?: 0.0) + 0.1 // Add packaging weight
}

fun isValidPriceRange(): Boolean {
    return price > BigDecimal.ZERO && price <= category.maxPrice
}

fun requiresManufacturer(): Boolean {
    return category.requiresManufacturer
}

fun isPhysicalProductValid(): Boolean {
    return if (isDigital) true else weight != null && weight > 0 && dimensions != null
}

fun isDigitalProductValid(): Boolean {
    return if (isDigital) weight == null && dimensions == null else true
}

fun getDiscountedPrice(discountPercent: Double): BigDecimal {
    require(discountPercent in 0.0..100.0) { "Discount percent must be between 0 and 100" }
    val discountMultiplier = BigDecimal((100.0 - discountPercent) / 100.0)
    return price.multiply(discountMultiplier).setScale(2, RoundingMode.HALF_UP)
}
```

---

## 📝 Step 3: Add Bean Validation to DTOs (15 minutes)

### Task 3.1: Complete User DTOs Validation

Open `src/main/kotlin/com/learning/validation/dto/UserDTOs.kt`

**TODO**: Add validation annotations to `CreateUserRequest`:

```kotlin
data class CreateUserRequest(
    @field:NotBlank(message = "Username cannot be blank")
    @field:Size(min = 3, max = 20, message = "Username must be 3-20 characters")
    @field:Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscore")
    val username: String,
    
    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email is required")
    val email: String,
    
    @field:NotBlank(message = "First name is required")
    @field:Size(min = 1, max = 50, message = "First name must be 1-50 characters")
    val firstName: String,
    
    @field:NotBlank(message = "Last name is required")
    @field:Size(min = 1, max = 50, message = "Last name must be 1-50 characters")
    val lastName: String,
    
    @field:Past(message = "Date of birth must be in the past")
    @field:NotNull(message = "Date of birth is required")
    val dateOfBirth: LocalDate,
    
    @field:Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    val phoneNumber: String?,
    
    @field:Valid
    val address: CreateAddressRequest?,
    
    @field:NotEmpty(message = "At least one role must be specified")
    val roles: Set<UserRole>
) {
    fun isValidForRegistration(): Boolean {
        val age = Period.between(dateOfBirth, LocalDate.now()).years
        return age >= 18 && roles.isNotEmpty() && !roles.contains(UserRole.SUPER_ADMIN)
    }
}
```

### Task 3.2: Complete Address DTOs

Add validation to `CreateAddressRequest`:

```kotlin
data class CreateAddressRequest(
    @field:NotBlank(message = "Street address is required")
    @field:Size(max = 100, message = "Street address too long")
    val street: String,
    
    @field:NotBlank(message = "City is required")
    @field:Size(max = 50, message = "City name too long")
    val city: String,
    
    @field:NotBlank(message = "State is required")
    @field:Size(max = 50, message = "State name too long")
    val state: String,
    
    @field:NotBlank(message = "ZIP code is required")
    @field:Pattern(regexp = "^\\d{5}(-\\d{4})?$", message = "Invalid ZIP code format")
    val zipCode: String,
    
    @field:NotBlank(message = "Country is required")
    @field:Size(min = 2, max = 2, message = "Country must be 2-letter code")
    val country: String
)
```

### Task 3.3: Complete Product DTOs

Open `src/main/kotlin/com/learning/validation/dto/ProductDTOs.kt`

Add validation to `CreateProductRequest`:

```kotlin
data class CreateProductRequest(
    @field:NotBlank(message = "Product name is required")
    @field:Size(min = 3, max = 100, message = "Product name must be 3-100 characters")
    val name: String,
    
    @field:NotBlank(message = "Description is required")
    @field:Size(min = 10, max = 1000, message = "Description must be 10-1000 characters")
    val description: String,
    
    @field:NotNull(message = "Price is required")
    @field:Positive(message = "Price must be positive")
    @field:DecimalMax(value = "999999.99", message = "Price too high")
    val price: BigDecimal,
    
    @field:NotNull(message = "Category is required")
    val category: ProductCategory,
    
    @field:Size(max = 10, message = "Maximum 10 tags allowed")
    val tags: Set<String>,
    
    @field:Valid
    val dimensions: CreateProductDimensionsRequest?,
    
    @field:Positive(message = "Weight must be positive")
    val weight: Double?,
    
    val isDigital: Boolean,
    
    @field:Min(value = 0, message = "Stock quantity cannot be negative")
    val stockQuantity: Int,
    
    @field:Min(value = 1, message = "Minimum order quantity must be at least 1")
    val minOrderQuantity: Int,
    
    @field:Min(value = 1, message = "Maximum order quantity must be at least 1")
    val maxOrderQuantity: Int?,
    
    val manufacturerId: String?
) {
    fun isValidProduct(): Boolean {
        return price <= category.maxPrice &&
               (maxOrderQuantity == null || maxOrderQuantity >= minOrderQuantity) &&
               (!category.requiresManufacturer || !manufacturerId.isNullOrBlank())
    }
    
    fun isValidForProductType(): Boolean {
        return if (isDigital) {
            weight == null && dimensions == null
        } else {
            weight != null && weight > 0
        }
    }
}
```

---

## 🎨 Step 4: Create Custom Validators (10 minutes)

### Task 4.1: Complete Age Validator

Open `src/main/kotlin/com/learning/validation/validator/CustomValidators.kt`

**TODO**: Implement the `AgeValidator`:

```kotlin
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

### Task 4.2: Complete Username Validator

Implement the `UsernameValidator`:

```kotlin
class UsernameValidator : ConstraintValidator<ValidUsername, String?> {
    
    private var allowSpecialChars: Boolean = false
    private var minLength: Int = 3
    private var maxLength: Int = 20
    
    override fun initialize(constraintAnnotation: ValidUsername) {
        allowSpecialChars = constraintAnnotation.allowSpecialChars
        minLength = constraintAnnotation.minLength
        maxLength = constraintAnnotation.maxLength
    }
    
    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return true
        
        return value.length in minLength..maxLength &&
               hasValidCharacters(value, allowSpecialChars) &&
               !containsProhibitedWords(value)
    }
    
    private fun containsProhibitedWords(username: String): Boolean {
        val prohibitedWords = listOf("admin", "root", "system", "test", "null", "undefined")
        return prohibitedWords.any { username.lowercase().contains(it) }
    }
    
    private fun hasValidCharacters(username: String, allowSpecialChars: Boolean): Boolean {
        val pattern = if (allowSpecialChars) {
            "^[a-zA-Z0-9_.-]+$"
        } else {
            "^[a-zA-Z0-9_]+$"
        }
        return username.matches(Regex(pattern))
    }
}
```

### Task 4.3: Complete Validation Utils

Implement utility methods:

```kotlin
object ValidationUtils {
    
    fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")
        return email.matches(emailRegex) && email.length <= 254
    }
    
    fun isValidPostalCode(postalCode: String, countryCode: String): Boolean {
        return when (countryCode.uppercase()) {
            "US" -> postalCode.matches(Regex("^\\d{5}(-\\d{4})?$"))
            "CA" -> postalCode.matches(Regex("^[A-Za-z]\\d[A-Za-z] \\d[A-Za-z]\\d$"))
            "GB" -> postalCode.matches(Regex("^[A-Z]{1,2}\\d[A-Z\\d]? \\d[A-Z]{2}$"))
            else -> postalCode.isNotBlank()
        }
    }
    
    fun containsSqlInjectionPatterns(input: String): Boolean {
        val sqlPatterns = listOf(
            "(?i).*\\b(SELECT|INSERT|UPDATE|DELETE|DROP|CREATE|ALTER)\\b.*",
            "(?i).*\\b(UNION|OR|AND)\\s+\\d+\\s*=\\s*\\d+.*",
            "(?i).*['\"](\\s|\\w)*['\"](\\s)*(=|LIKE|IN).*"
        )
        return sqlPatterns.any { input.contains(Regex(it)) }
    }
    
    fun sanitizeInput(input: String): String {
        return input
            .replace(Regex("[<>\"'&]"), "")
            .trim()
            .take(1000)
    }
}
```

---

## 🚨 Step 5: Implement Global Exception Handling (10 minutes)

### Task 5.1: Complete Global Exception Handler

Open `src/main/kotlin/com/learning/validation/exception/GlobalExceptionHandler.kt`

**TODO**: Add the missing annotations and implement exception handlers:

```kotlin
@ControllerAdvice
class GlobalExceptionHandler {
    
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    
    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(
        ex: ValidationException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        
        val errorResponse = buildErrorResponse(
            status = ex.getHttpStatus(),
            errorCode = ex.errorCode,
            message = ex.message ?: "Validation error",
            details = null,
            fieldErrors = null,
            metadata = ex.getErrorMetadata(),
            request = request
        )
        
        logError(ex, request, errorResponse)
        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse)
    }
    
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        
        val fieldErrors = extractFieldErrors(ex)
        
        val errorResponse = buildErrorResponse(
            status = HttpStatus.BAD_REQUEST,
            errorCode = "VALIDATION_ERROR",
            message = "Invalid request parameters",
            details = "Please check the field errors for details",
            fieldErrors = fieldErrors,
            metadata = null,
            request = request
        )
        
        logError(ex, request, errorResponse)
        return ResponseEntity.badRequest().body(errorResponse)
    }
    
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        
        val errorResponse = buildErrorResponse(
            status = HttpStatus.BAD_REQUEST,
            errorCode = "ILLEGAL_ARGUMENT",
            message = ex.message ?: "Invalid argument",
            request = request
        )
        
        logError(ex, request, errorResponse)
        return ResponseEntity.badRequest().body(errorResponse)
    }
    
    @ExceptionHandler(Exception::class)
    fun handleGeneralException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        
        val errorResponse = buildErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            errorCode = "INTERNAL_ERROR",
            message = "An unexpected error occurred",
            details = if (isDevelopmentMode()) ex.message else null,
            request = request
        )
        
        logger.error("Unexpected error: ", ex)
        return ResponseEntity.internalServerError().body(errorResponse)
    }
    
    private fun buildErrorResponse(
        status: HttpStatus,
        errorCode: String,
        message: String,
        details: String? = null,
        fieldErrors: List<FieldErrorDetail>? = null,
        metadata: Map<String, Any?>? = null,
        request: WebRequest
    ): ErrorResponse {
        return ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = status.value(),
            error = status.reasonPhrase,
            message = message,
            errorCode = errorCode,
            path = getRequestPath(request),
            correlationId = generateCorrelationId(),
            details = details,
            fieldErrors = fieldErrors,
            metadata = metadata
        )
    }
    
    private fun extractFieldErrors(ex: MethodArgumentNotValidException): List<FieldErrorDetail> {
        return ex.bindingResult.fieldErrors.map { fieldError ->
            FieldErrorDetail(
                field = fieldError.field,
                rejectedValue = fieldError.rejectedValue,
                message = fieldError.defaultMessage ?: "Invalid value",
                errorCode = fieldError.code
            )
        }
    }
    
    private fun getRequestPath(request: WebRequest): String {
        return request.getDescription(false).removePrefix("uri=")
    }
    
    private fun generateCorrelationId(): String {
        return UUID.randomUUID().toString()
    }
    
    private fun logError(ex: Exception, request: WebRequest, errorResponse: ErrorResponse) {
        when (errorResponse.status) {
            in 400..499 -> logger.warn("Client error: {} - {}", errorResponse.errorCode, ex.message)
            in 500..599 -> logger.error("Server error: {} - {}", errorResponse.errorCode, ex.message, ex)
        }
    }
    
    private fun isDevelopmentMode(): Boolean {
        // Check if running in development mode
        return System.getProperty("spring.profiles.active")?.contains("dev") == true
    }
}
```

---

## 🔧 Step 6: Implement Service Layer Validation (10 minutes)

### Task 6.1: Complete User Validation Service

Open `src/main/kotlin/com/learning/validation/service/UserValidationService.kt`

**TODO**: Add the `@Service` annotation and implement key methods:

```kotlin
@Service
class UserValidationService {
    
    fun validateAndCreateUser(request: CreateUserRequest): User {
        // 1. Check for duplicates
        validateNoDuplicates(request)
        
        // 2. Apply business rules
        validateBusinessRules(request)
        
        // 3. Create and store user
        val user = createUserFromRequest(request)
        users[user.id] = user
        usernames.add(user.username)
        emails.add(user.email)
        
        logUserOperation("CREATE", user.id, mapOf("username" to user.username))
        return user
    }
    
    fun validateUsernameAvailability(username: String): Boolean {
        return !usernames.contains(username) && 
               username.length >= 3 && 
               !ValidationUtils.containsSqlInjectionPatterns(username)
    }
    
    fun validateEmailAvailability(email: String): Boolean {
        return !emails.contains(email) && ValidationUtils.isValidEmail(email)
    }
    
    private fun validateNoDuplicates(request: CreateUserRequest) {
        if (usernames.contains(request.username)) {
            throw DuplicateResourceException(
                resourceType = "User",
                field = "username",
                value = request.username
            )
        }
        
        if (emails.contains(request.email)) {
            throw DuplicateResourceException(
                resourceType = "User",
                field = "email",
                value = request.email
            )
        }
    }
    
    private fun validateBusinessRules(request: CreateUserRequest) {
        // Age validation
        val age = Period.between(request.dateOfBirth, LocalDate.now()).years
        if (age < 18) {
            throw BusinessRuleViolationException(
                message = "Users must be at least 18 years old",
                ruleName = "MinimumAge",
                field = "dateOfBirth",
                value = request.dateOfBirth
            )
        }
        
        // Role validation
        if (request.roles.contains(UserRole.SUPER_ADMIN)) {
            throw OperationNotAllowedException(
                operation = "createSuperAdmin",
                reason = "Super admin users cannot be created through this endpoint"
            )
        }
    }
    
    private fun createUserFromRequest(request: CreateUserRequest): User {
        return User(
            id = generateUserId(),
            username = request.username,
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            dateOfBirth = request.dateOfBirth,
            phoneNumber = request.phoneNumber,
            address = request.address?.let { 
                Address(it.street, it.city, it.state, it.zipCode, it.country)
            },
            status = UserStatus.PENDING,
            roles = request.roles,
            createdAt = LocalDateTime.now()
        )
    }
    
    private fun generateUserId(): String = "user_${System.currentTimeMillis()}"
    
    private fun logUserOperation(operation: String, userId: String, details: Map<String, Any>) {
        logger.info("User operation: {} for user: {} with details: {}", operation, userId, details)
    }
}
```

---

## 🌐 Step 7: Complete Controllers (15 minutes)

### Task 7.1: Complete User Controller

Open `src/main/kotlin/com/learning/validation/controller/UserValidationController.kt`

**TODO**: Add annotations and implement endpoints:

```kotlin
@RestController
@RequestMapping("/api/users")
class UserValidationController(
    private val userValidationService: UserValidationService
) {
    
    @PostMapping
    fun createUser(
        @Valid @RequestBody request: CreateUserRequest
    ): ResponseEntity<UserResponse> {
        logRequest("CREATE_USER", request)
        val startTime = System.currentTimeMillis()
        
        val user = userValidationService.validateAndCreateUser(request)
        val response = mapToUserResponse(user)
        
        logResponse("CREATE_USER", response, System.currentTimeMillis() - startTime)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
    
    @GetMapping("/validate/username")
    fun checkUsernameAvailability(
        @RequestParam @Size(min = 3, max = 20) username: String
    ): ResponseEntity<Map<String, Any>> {
        val isAvailable = userValidationService.validateUsernameAvailability(username)
        
        return ResponseEntity.ok(mapOf(
            "username" to username,
            "available" to isAvailable,
            "message" to if (isAvailable) "Username is available" else "Username is not available"
        ))
    }
    
    @GetMapping("/validate/email")
    fun checkEmailAvailability(
        @RequestParam @Email email: String
    ): ResponseEntity<Map<String, Any>> {
        val isAvailable = userValidationService.validateEmailAvailability(email)
        
        return ResponseEntity.ok(mapOf(
            "email" to email,
            "available" to isAvailable,
            "message" to if (isAvailable) "Email is available" else "Email is not available"
        ))
    }
    
    private fun mapToUserResponse(user: User): UserResponse {
        return UserResponse(
            id = user.id,
            username = user.username,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            fullName = user.fullName,
            age = user.age,
            phoneNumber = user.phoneNumber,
            address = user.address?.let { 
                AddressResponse(it.street, it.city, it.state, it.zipCode, it.country, it.getFormattedAddress())
            },
            status = user.status,
            roles = user.roles,
            isEmailVerified = user.isEmailVerified,
            isPhoneVerified = user.isPhoneVerified,
            createdAt = user.createdAt.toString()
        )
    }
    
    private fun logRequest(operation: String, request: Any) {
        logger.info("Incoming request: {} with payload: {}", operation, request.toString())
    }
    
    private fun logResponse(operation: String, response: Any, duration: Long) {
        logger.info("Outgoing response: {} completed in {}ms", operation, duration)
    }
}
```

---

## 🧪 Step 8: Test Your Implementation (5 minutes)

### Task 8.1: Run the Application

```bash
./gradlew bootRun
```

### Task 8.2: Test User Creation

**Valid Request**:
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "dateOfBirth": "1990-01-01",
    "roles": ["USER"]
  }'
```

**Expected Response**: `201 Created` with user details

**Invalid Request** (missing required fields):
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "",
    "email": "invalid-email"
  }'
```

**Expected Response**: `400 Bad Request` with validation errors

### Task 8.3: Test Username Availability

```bash
curl "http://localhost:8080/api/users/validate/username?username=john_doe"
```

**Expected Response**: Availability check result

---

## 🎯 Verification & Testing

### Success Criteria

✅ **Application Starts**: No compilation errors, application starts on port 8080  
✅ **Validation Works**: Invalid requests return 400 with structured error messages  
✅ **Business Rules**: Age restrictions and duplicate checks work correctly  
✅ **Custom Validators**: Username and email validation work as expected  
✅ **Error Handling**: Global exception handler provides consistent error responses  

### Test Scenarios

1. **Valid User Creation**: Should return 201 with user details
2. **Invalid Email**: Should return 400 with email validation error
3. **Underage User**: Should return 400 with age business rule error
4. **Duplicate Username**: Should return 409 with conflict error
5. **Username Availability**: Should return correct availability status

---

## 🏆 What You've Accomplished

### Technical Skills Gained

- ✅ **Bean Validation**: Standard annotation usage and configuration
- ✅ **Custom Validators**: Creating domain-specific validation logic
- ✅ **Global Error Handling**: Centralized, consistent error responses
- ✅ **Service Validation**: Business rule validation beyond annotations
- ✅ **API Testing**: Validation testing with curl commands

### Production Patterns Learned

- 🛡️ **Defense in Depth**: Multiple validation layers for security
- 📊 **Structured Errors**: Consistent, parseable error responses
- 🔍 **Observability**: Proper logging and correlation IDs
- ⚡ **Performance**: Efficient validation patterns
- 🧪 **Testability**: Validation logic that's easy to test

---

## 🚀 Next Steps

Ready for **Lesson 7: Service Layer & Clean Architecture**? You'll build on this validation foundation to create well-structured, maintainable applications with proper separation of concerns.

**Optional Enhancements**:
- Add more custom validators for complex business rules
- Implement async validation for external service calls
- Add rate limiting to prevent abuse
- Create validation groups for different scenarios

---

*Great job! You've built a robust validation system that's ready for production use. The patterns you've learned here will serve as the foundation for building secure, reliable APIs.*