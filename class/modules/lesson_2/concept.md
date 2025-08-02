# 🎯 Lesson 2: Collections & Functional Programming

## Objective

Master Kotlin's powerful collection operations and functional programming features. Learn to transform, filter, and process data using idiomatic Kotlin patterns that are essential for backend development.

## Key Concepts

### 1. Collection Types

```kotlin
// Immutable collections (default, preferred)
val numbers = listOf(1, 2, 3, 4, 5)
val users = setOf(user1, user2, user3)  
val config = mapOf("debug" to true, "port" to 8080)

// Mutable collections (when needed)
val mutableList = mutableListOf<String>()
val mutableMap = mutableMapOf<String, Int>()
```

### 2. Functional Operations

```kotlin
// Transform data with map
val upperCaseNames = users.map { it.name.uppercase() }

// Filter collections
val activeUsers = users.filter { it.isActive }
val adults = users.filter { it.age >= 18 }

// Find specific items
val admin = users.find { it.role == "admin" }
val firstAdult = users.firstOrNull { it.age >= 18 }

// Aggregate data
val totalAge = users.sumOf { it.age }
val averageAge = users.map { it.age }.average()

// Group and organize
val usersByAge = users.groupBy { it.age }
val usersByDepartment = users.associateBy { it.department }
```

### 3. Chaining Operations

```kotlin
val result = users
    .filter { it.isActive }
    .map { it.name.uppercase() }
    .sorted()
    .take(10)
```

### 4. Extension Functions

```kotlin
// Add functionality to existing types
fun List<User>.getActiveUsers() = filter { it.isActive }
fun List<User>.averageAge() = map { it.age }.average()

// Use them naturally
val activeUsers = userList.getActiveUsers()
val avgAge = userList.averageAge()
```

## Best Practices

### ✅ Do:
- **Use immutable collections by default** - safer and more predictable
- **Chain operations fluently** - readable and expressive
- **Leverage extension functions** - add domain-specific operations
- **Use appropriate collection types** - List for ordered, Set for unique, Map for key-value

### ❌ Avoid:
- **Nested loops when functional operations work** - less readable and error-prone
- **Mutating collections unnecessarily** - harder to reason about
- **Complex operations in single chain** - break into multiple steps for clarity

## Real-World Applications

### Data Processing Pipeline
```kotlin
class UserAnalyticsService {
    fun generateUserReport(users: List<User>): UserReport {
        val activeUsers = users.filter { it.isActive }
        
        val usersByDepartment = activeUsers
            .groupBy { it.department }
            .mapValues { (_, deptUsers) ->
                DepartmentStats(
                    count = deptUsers.size,
                    averageAge = deptUsers.map { it.age }.average(),
                    totalSalary = deptUsers.sumOf { it.salary }
                )
            }
        
        return UserReport(
            totalActive = activeUsers.size,
            departmentStats = usersByDepartment,
            topPerformers = activeUsers
                .sortedByDescending { it.performanceScore }
                .take(5)
        )
    }
}
```

## Next Steps

This lesson prepares you for:
- Advanced OOP features in Lesson 3
- Spring Boot collections handling in Lessons 4-5
- Database query result processing in Lessons 8-10