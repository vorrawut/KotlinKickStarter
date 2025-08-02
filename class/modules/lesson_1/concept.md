# 🎯 Lesson 1: Kotlin 101 - Syntax & Null Safety

## Objective

Master Kotlin's fundamental syntax, understand null safety, and learn to write idiomatic Kotlin code that's both safe and expressive. This lesson establishes the foundation for all future backend development with Kotlin.

## Key Concepts

### 1. Variables and Immutability

Kotlin encourages immutable data by default:

```kotlin
val immutable = "Cannot be changed"      // Read-only (like final in Java)
var mutable = "Can be reassigned"        // Mutable

// Type inference - Kotlin figures out the type
val name = "John"                        // String inferred
val age = 25                            // Int inferred

// Explicit typing when needed
val email: String = "john@example.com"
```

**Why this matters**: Immutable data reduces bugs, makes code more predictable, and enables better performance optimizations.

### 2. Null Safety - Kotlin's Superpower

Kotlin eliminates the billion-dollar mistake of null pointer exceptions:

```kotlin
// Non-nullable types (default)
var name: String = "John"
// name = null  // Compilation error!

// Nullable types (explicit)
var email: String? = null               // Can be null
email = "john@example.com"              // Or have a value

// Safe call operator (?.)
val length = email?.length              // Returns null if email is null

// Elvis operator (?:)
val displayName = name ?: "Unknown User"

// Safe casting
val user = something as? User           // Returns null if cast fails
```

**Real-world impact**: No more `NullPointerException` crashes in production!

### 3. Functions - First-Class Citizens

```kotlin
// Basic function
fun greet(name: String): String {
    return "Hello, $name!"
}

// Single expression function
fun greet(name: String) = "Hello, $name!"

// Function with default parameters
fun createUser(name: String, email: String = "unknown@example.com") = 
    User(name, email)

// Named parameters
val user = createUser(email = "john@doe.com", name = "John")
```

### 4. Data Classes - Boilerplate-Free Models

```kotlin
data class User(
    val name: String,
    val email: String,
    val age: Int = 0
)

// Automatically generated:
// - equals() and hashCode()
// - toString()
// - copy() function
// - componentN() functions for destructuring
```

**Benefits**: 
- 90% less boilerplate compared to Java
- Automatic implementations of common methods
- Built-in copying with modifications

### 5. String Templates

```kotlin
val name = "John"
val age = 25

// Simple interpolation
val message = "User $name is $age years old"

// Expression interpolation
val status = "User ${name.uppercase()} has ${if (age >= 18) "adult" else "minor"} status"
```

## Best Practices

### ✅ Do:
- **Use `val` by default**, only use `var` when you need to reassign
- **Embrace null safety** - use nullable types explicitly when needed
- **Use data classes** for models and DTOs
- **Leverage type inference** - let Kotlin figure out types when obvious
- **Use string templates** instead of concatenation

### ❌ Avoid:
- **Force unwrapping with `!!`** - only use when absolutely certain value is not null
- **Using `var` unnecessarily** - immutability is your friend
- **Ignoring null safety** - don't cast everything to non-null without thought
- **Java-style getters/setters** - Kotlin properties handle this automatically

## Kotlin vs Java Comparison

| Concept | Java | Kotlin |
|---------|------|--------|
| **Null Safety** | `@Nullable` annotations | Built-in nullable types |
| **Data Classes** | Lots of boilerplate | `data class` keyword |
| **Properties** | Getters/setters | Automatic properties |
| **String Interpolation** | Concatenation | `$variable` syntax |
| **Immutability** | `final` keyword | `val` by default |

## Common Gotchas & Solutions

### 1. **Platform Types from Java**
```kotlin
// When calling Java code, be explicit about nullability
val javaString: String = javaMethod()     // Assume non-null
val javaString: String? = javaMethod()    // Can be null
```

### 2. **Smart Casts**
```kotlin
fun processUser(obj: Any) {
    if (obj is User) {
        // obj is automatically cast to User here
        println(obj.name)  // No manual casting needed!
    }
}
```

### 3. **When vs If**
```kotlin
// Use 'when' for multiple conditions (like switch, but better)
when (user.status) {
    "active" -> enableFeatures()
    "inactive" -> showReactivationPrompt()
    "banned" -> denyAccess()
    else -> handleUnknownStatus()
}
```

## Modern Kotlin Idioms

### 1. **Scope Functions**
```kotlin
val user = User("John", "john@example.com").apply {
    // 'this' refers to the user object
    validate()
    assignRole("admin")
}

// let for nullable handling
user?.let { 
    // Only executes if user is not null
    sendWelcomeEmail(it)
}
```

### 2. **Collection Operations**
```kotlin
val activeUsers = users
    .filter { it.isActive }
    .map { it.name.uppercase() }
    .sortedBy { it }
```

## Production Tips

- **Use meaningful variable names** - type inference means you don't need to encode types in names
- **Leverage data classes** for all your DTOs and models
- **Make illegal states unrepresentable** - use nullable types and sealed classes appropriately
- **Write functions that return meaningful types** - avoid returning `Any` or generic types
- **Use extension functions** to add functionality to existing classes cleanly

## Next Steps

After mastering these fundamentals, you'll move on to:
- Collections and functional programming (Lesson 2)
- Advanced OOP features and sealed classes (Lesson 3)
- Spring Boot integration patterns (Lesson 4)

**Remember**: Kotlin is designed to be **pragmatic, safe, and interoperable**. These fundamentals will make all future lessons much easier to understand and implement!