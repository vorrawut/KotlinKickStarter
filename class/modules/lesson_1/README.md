# 🎯 Lesson 1: Kotlin 101 - Syntax & Null Safety

**Duration**: 30 minutes  
**Prerequisites**: Basic programming knowledge  
**Difficulty**: Beginner  

## 📋 What You'll Learn

- Kotlin syntax fundamentals and language features
- Null safety - Kotlin's most powerful feature
- Data classes for clean, concise models
- Functions, extension properties, and string templates
- Best practices for writing idiomatic Kotlin code

## 🚀 Quick Start

1. **Workshop** (Start here): [workshop_1.md](workshop_1.md)
2. **Concepts** (Theory): [concept.md](concept.md)
3. **Practice**: Complete the TODOs in `class/workshop/lesson_1/`
4. **Verify**: Compare with solutions in `class/answer/lesson_1/`

## 🎯 Learning Objectives

By the end of this lesson, you will be able to:

- ✅ Write safe, null-aware Kotlin code
- ✅ Create data classes with extension properties
- ✅ Use string templates and Kotlin idioms
- ✅ Validate user input with proper error handling
- ✅ Write basic unit tests in Kotlin

## 🔨 What You'll Build

A **User Management System** that demonstrates:

```kotlin
// Clean, safe data modeling
data class User(
    val name: String,
    val email: String,
    val age: Int = 0,
    val phone: String? = null
)

// Extension properties for computed values
val User.displayName: String
    get() = "${name.split(" ").first()} ${name.split(" ").getOrNull(1)?.first() ?: ""}."

// Null-safe validation
fun isValidEmail(email: String?): Boolean {
    return email?.matches(Regex("\\w+@\\w+\\.\\w+")) ?: false
}
```

## 📚 Key Concepts Covered

### 1. **Null Safety**
- Nullable vs non-nullable types
- Safe call operator (`?.`)
- Elvis operator (`?:`)
- Smart casts

### 2. **Data Classes**
- Automatic `equals()`, `hashCode()`, `toString()`
- `copy()` function for immutable updates
- Destructuring declarations

### 3. **Functions & Properties**
- Extension functions and properties
- Default parameters and named arguments
- Single-expression functions

### 4. **String Templates**
- Variable interpolation with `$`
- Expression interpolation with `${}`

## 🧪 Testing Your Knowledge

The workshop includes comprehensive tests that verify:
- User creation and validation
- Email update functionality
- String formatting and templates
- Collection filtering operations

## 🎯 Success Criteria

Your implementation should:
- ✅ Compile without warnings
- ✅ Pass all unit tests
- ✅ Handle null values safely
- ✅ Use idiomatic Kotlin patterns
- ✅ Demonstrate proper validation

## 🔄 Run & Test

```bash
# Navigate to workshop
cd class/workshop/lesson_1

# Build and run
../../gradlew run

# Run tests
../../gradlew test
```

## 📈 What's Next?

- **Lesson 2**: Collections & Functional Programming
- **Lesson 3**: OOP + Kotlin Features (Sealed Classes)
- **Lesson 4**: Spring Boot Setup & Dependency Injection

## 💡 Pro Tips

1. **Use `val` by default** - Only use `var` when you need reassignment
2. **Embrace null safety** - Let the compiler help you avoid crashes
3. **Leverage data classes** - They eliminate tons of boilerplate
4. **String templates > concatenation** - More readable and efficient

## 🤔 Common Mistakes

- Using `!!` (force unwrap) - Use safe calls instead
- Ignoring null safety warnings
- Writing Java-style code instead of idiomatic Kotlin
- Not using data classes for simple models

## 🎉 Congratulations!

Once you complete this lesson, you'll have a solid foundation in Kotlin fundamentals that will serve you throughout the entire curriculum. The patterns you learn here (null safety, data classes, extension functions) are used extensively in real-world Kotlin development.

**Ready to dive deeper?** Continue to [Lesson 2: Collections & Functional Programming](../lesson_2/)