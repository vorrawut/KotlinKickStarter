# 🎯 Lesson 3: OOP + Kotlin Features

**Duration**: 30 minutes  
**Prerequisites**: Lessons 1-2  
**Difficulty**: Intermediate  

## 📋 What You'll Learn

- Sealed classes for type-safe hierarchies
- Interfaces with default implementations  
- Class and property delegation patterns
- Smart casting and exhaustive when expressions
- Advanced OOP patterns for domain modeling

## 🚀 Quick Start

1. **Workshop** (Start here): [workshop_3.md](workshop_3.md)
2. **Concepts** (Theory): [concept.md](concept.md)
3. **Practice**: Complete the TODOs in `class/workshop/lesson_3/`
4. **Verify**: Compare with solutions in `class/answer/lesson_3/`

## 🎯 Learning Objectives

- ✅ Create type-safe sealed class hierarchies
- ✅ Use smart casting for conditional logic
- ✅ Implement delegation patterns
- ✅ Write exhaustive when expressions
- ✅ Design robust domain models

## 🔨 What You'll Build

A **Payment Processing System** demonstrating:

```kotlin
sealed class PaymentResult {
    data class Success(val transactionId: String) : PaymentResult()
    data class Failed(val error: String) : PaymentResult()
}

class PaymentService(auditor: Auditable) : Auditable by auditor {
    fun processPayment(method: PaymentMethod): PaymentResult = when (method) {
        is PaymentMethod.CreditCard -> // Smart cast automatically
        is PaymentMethod.BankAccount -> // All cases must be handled
    }
}
```

## 📈 What's Next?

- **Lesson 4**: Spring Boot Setup & Dependency Injection
- **Lesson 5**: REST Controllers & DTOs