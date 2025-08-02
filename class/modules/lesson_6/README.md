# 🛡️ Lesson 6: Request Validation & Error Handling

[![Lesson Status](https://img.shields.io/badge/Status-Complete-brightgreen)](#)
[![Difficulty](https://img.shields.io/badge/Difficulty-Intermediate-yellow)](#)
[![Duration](https://img.shields.io/badge/Duration-45--60%20min-blue)](#)

## 🎯 Overview

Master **production-grade validation and error handling** in Spring Boot applications. Learn to build robust APIs that gracefully handle invalid input, provide meaningful error messages, and maintain security best practices.

### What You'll Build
- **User & Product Management API** with comprehensive validation
- **Custom validation annotations** for business rules
- **Global exception handler** with structured error responses
- **Multi-layer validation strategy** for security and data integrity

## 📚 Learning Objectives

By completing this lesson, you will:

- ✅ **Master Bean Validation** with standard and custom annotations
- ✅ **Implement global error handling** with @ControllerAdvice
- ✅ **Create custom validators** for complex business rules
- ✅ **Design structured error responses** for better API usability
- ✅ **Apply security practices** in input validation

## 🏗️ Project Structure

```
lesson_6/
├── workshop/                    # 🔧 Your implementation
│   ├── src/main/kotlin/com/learning/validation/
│   │   ├── ValidationApplication.kt     # Spring Boot app
│   │   ├── model/                      # Domain models
│   │   │   ├── User.kt                # User domain model
│   │   │   └── Product.kt             # Product domain model
│   │   ├── dto/                       # Data Transfer Objects
│   │   │   ├── UserDTOs.kt           # User request/response DTOs
│   │   │   └── ProductDTOs.kt        # Product request/response DTOs
│   │   ├── exception/                 # Custom exceptions
│   │   │   ├── ValidationExceptions.kt
│   │   │   └── GlobalExceptionHandler.kt
│   │   ├── validator/                 # Custom validators
│   │   │   └── CustomValidators.kt    # Custom validation logic
│   │   ├── service/                   # Business logic
│   │   │   ├── UserValidationService.kt
│   │   │   └── ProductValidationService.kt
│   │   ├── controller/                # REST endpoints
│   │   │   ├── UserValidationController.kt
│   │   │   └── ProductValidationController.kt
│   │   └── config/                    # Configuration
│   │       └── ValidationConfiguration.kt
│   └── src/test/                      # Tests
└── answer/                     # ✅ Complete solution
    └── [Same structure as workshop]
```

## 🚀 Quick Start

### Prerequisites
- Completed [Lesson 5](../lesson_5/) (REST Controllers & DTOs)
- Java 21+ installed
- Understanding of HTTP status codes

### Start the Workshop
```bash
cd class/workshop/lesson_6
./gradlew bootRun
```

### Verify Setup
```bash
curl http://localhost:8080/actuator/health
# Should return: {"status":"UP"}
```

## 📋 Workshop Steps

| Step | Task | Key Learning | Duration |
|------|------|-------------|----------|
| 1 | [Application Setup](workshop_6.md#step-1) | Spring Boot validation config | 5 min |
| 2 | [Domain Models](workshop_6.md#step-2) | Business logic validation | 10 min |
| 3 | [DTO Validation](workshop_6.md#step-3) | Bean validation annotations | 15 min |
| 4 | [Custom Validators](workshop_6.md#step-4) | Creating custom validation | 10 min |
| 5 | [Exception Handling](workshop_6.md#step-5) | Global error responses | 10 min |
| 6 | [Service Layer](workshop_6.md#step-6) | Business rule validation | 10 min |
| 7 | [Controllers](workshop_6.md#step-7) | Endpoint validation | 15 min |
| 8 | [Testing](workshop_6.md#step-8) | Validation testing | 5 min |

## 🎯 Key Concepts

### Bean Validation Fundamentals
```kotlin
data class CreateUserRequest(
    @field:NotBlank(message = "Username cannot be blank")
    @field:Size(min = 3, max = 20)
    @field:Pattern(regexp = "^[a-zA-Z0-9_]+$")
    val username: String,
    
    @field:Email(message = "Invalid email format")
    @field:NotBlank
    val email: String,
    
    @field:Past(message = "Date of birth must be in the past")
    @field:NotNull
    val dateOfBirth: LocalDate
)
```

### Global Exception Handling
```kotlin
@ControllerAdvice
class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val fieldErrors = ex.bindingResult.fieldErrors.map { 
            FieldErrorDetail(it.field, it.rejectedValue, it.defaultMessage) 
        }
        
        return ResponseEntity.badRequest().body(
            ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = 400,
                error = "Validation Failed",
                fieldErrors = fieldErrors
            )
        )
    }
}
```

### Custom Validation
```kotlin
@Target(AnnotationTarget.FIELD)
@Constraint(validatedBy = [AgeValidator::class])
annotation class ValidAge(val min: Int = 0, val max: Int = 150)

class AgeValidator : ConstraintValidator<ValidAge, LocalDate?> {
    override fun isValid(value: LocalDate?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return true
        val age = Period.between(value, LocalDate.now()).years
        return age in min..max
    }
}
```

## 🧪 Testing Your Implementation

### Test Valid User Creation
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

### Test Validation Errors
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "",
    "email": "invalid-email",
    "dateOfBirth": "2025-01-01"
  }'
```

Expected response:
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid request parameters",
  "fieldErrors": [
    {
      "field": "username",
      "rejectedValue": "",
      "message": "Username cannot be blank"
    },
    {
      "field": "email",
      "rejectedValue": "invalid-email",
      "message": "Invalid email format"
    }
  ]
}
```

## 📊 Success Criteria

✅ **Application Starts**: Clean startup with no errors  
✅ **Bean Validation**: Standard annotations work correctly  
✅ **Custom Validators**: Age and username validation functional  
✅ **Error Handling**: Structured error responses returned  
✅ **Business Rules**: Age restrictions and duplicates prevented  
✅ **API Testing**: All endpoints respond appropriately  

## 🔗 Related Lessons

- **Previous**: [Lesson 5 - REST Controllers & DTOs](../lesson_5/)
- **Next**: [Lesson 7 - Service Layer & Clean Architecture](../lesson_7/)
- **Concepts**: [Validation Theory](concept.md)
- **Practice**: [Step-by-Step Workshop](workshop_6.md)

## 💡 Production Tips

### Security Best Practices
```kotlin
// Always sanitize input
fun sanitizeInput(input: String): String {
    return input
        .replace(Regex("[<>\"'&]"), "")
        .trim()
        .take(1000)
}

// Check for injection patterns
fun containsSqlInjection(input: String): Boolean {
    val patterns = listOf("SELECT", "INSERT", "DROP", "UNION")
    return patterns.any { input.contains(it, ignoreCase = true) }
}
```

### Performance Considerations
- Use validation groups for different scenarios
- Implement async validation for external services
- Add rate limiting to prevent abuse
- Cache validation results when appropriate

### Monitoring & Observability
- Add correlation IDs to error responses
- Log validation failures for analysis
- Monitor validation error rates
- Track most common validation failures

## 🏆 What You've Learned

### Technical Skills
- **Bean Validation**: Standard and custom annotations
- **Error Handling**: Global exception management
- **Security**: Input validation and sanitization
- **API Design**: Structured error responses

### Professional Practices
- **Defense in Depth**: Multiple validation layers
- **User Experience**: Clear, actionable error messages
- **Monitoring**: Proper logging and error tracking
- **Maintainability**: Clean validation architecture

---

🎉 **Congratulations!** You've mastered production-grade validation and error handling. These patterns form the foundation of secure, robust APIs used in professional applications.

**Ready for the next challenge?** Continue to [Lesson 7: Service Layer & Clean Architecture](../lesson_7/) to learn how to structure maintainable applications with proper separation of concerns.