# 🔨 Workshop 2: Data Processing Pipeline with Collections

## What We Want to Build

Create a powerful **User Analytics System** that demonstrates Kotlin's collections and functional programming capabilities. You'll build a data processing pipeline that transforms, filters, and analyzes user data using idiomatic Kotlin patterns.

## Expected Result

By the end of this workshop, you'll have:
- ✅ A comprehensive analytics service using functional operations
- ✅ Extension functions for domain-specific operations
- ✅ Advanced collection processing with chaining
- ✅ Real-world data transformation patterns
- ✅ Performance-aware collection operations

**Sample Output:**
```
=== User Analytics Dashboard ===
📊 Processing 1000 users...

🎯 Department Analysis:
Engineering: 45 users, avg age: 32.4, total salary: $4,500,000
Marketing: 23 users, avg age: 29.8, total salary: $1,840,000
Sales: 38 users, avg age: 35.1, total salary: $3,040,000

🏆 Top Performers:
1. Alice Johnson (Engineering) - Score: 98.5
2. Bob Chen (Marketing) - Score: 97.2  
3. Carol Davis (Sales) - Score: 96.8

📈 Activity Insights:
Active Users: 892 (89.2%)
Users with Projects: 634 (63.4%)
Recent Joiners: 156 (15.6%)
```

## Step-by-Step Code Guide

### Step 1: Enhanced User Model with Analytics Data

**File**: `class/workshop/lesson_2/src/main/kotlin/User.kt`

```kotlin
import java.time.LocalDate

// TODO: Extend the User data class to include analytics properties
data class User(
    val id: String,
    val name: String,
    val email: String,
    val age: Int,
    val department: String,
    val salary: Double,
    val isActive: Boolean = true,
    val performanceScore: Double = 0.0,
    val joinDate: LocalDate = LocalDate.now(),
    val projectsCompleted: Int = 0
)

// TODO: Create extension functions for user categorization
// Hint: Use extension properties and functions

// Extension property for experience level
val User.experienceLevel: String
    get() = TODO("Categorize user by years since join date: Junior (0-2), Mid (3-5), Senior (6+)")

// Extension property for performance category  
val User.performanceCategory: String
    get() = TODO("Categorize by score: Excellent (90+), Good (75-90), Average (60-75), Poor (<60)")

// Extension function to check if user is a high performer
fun User.isHighPerformer(): Boolean = TODO("Return true if score >= 85")

// Extension function to calculate years of experience
fun User.yearsOfExperience(): Int = TODO("Calculate years between joinDate and now")

// Extension function for salary bracket
fun User.salaryBracket(): String = TODO("Return: Low (<50k), Medium (50k-100k), High (100k+)")
```

### Step 2: Collection Extension Functions

**File**: `class/workshop/lesson_2/src/main/kotlin/UserCollectionExtensions.kt`

```kotlin
// TODO: Create powerful extension functions for User collections
// These make your code more readable and domain-specific

// Get users by department
fun List<User>.getUsersByDepartment(department: String): List<User> = 
    TODO("Filter users by department (case-insensitive)")

// Get active users
fun List<User>.getActiveUsers(): List<User> = 
    TODO("Filter only active users")

// Get top performers (sorted by performance score)
fun List<User>.getTopPerformers(count: Int = 5): List<User> = 
    TODO("Return top N users sorted by performance score descending")

// Calculate average age
fun List<User>.averageAge(): Double = 
    TODO("Calculate average age, return 0.0 if empty list")

// Calculate total salary
fun List<User>.totalSalary(): Double = 
    TODO("Sum all salaries")

// Group users by department with counts
fun List<User>.departmentCounts(): Map<String, Int> = 
    TODO("Group by department and count users in each")

// Get users joined in last N days
fun List<User>.recentJoiners(days: Int = 30): List<User> = 
    TODO("Filter users who joined within last N days")

// Get users with high project completion
fun List<User>.productiveUsers(minProjects: Int = 5): List<User> = 
    TODO("Filter users with at least minProjects completed")

// Calculate performance distribution
fun List<User>.performanceDistribution(): Map<String, Int> = 
    TODO("Group users by performance category and count each")
```

### Step 3: Department Statistics Data Class

**File**: `class/workshop/lesson_2/src/main/kotlin/DepartmentStats.kt`

```kotlin
// TODO: Create a data class to hold department analytics
data class DepartmentStats(
    val departmentName: String,
    val userCount: Int,
    val averageAge: Double,
    val totalSalary: Double,
    val averageSalary: Double,
    val topPerformer: User?,
    val averagePerformanceScore: Double
)

// TODO: Add extension functions for formatting
fun DepartmentStats.formatSummary(): String = 
    TODO("Format as: 'Engineering: 45 users, avg age: 32.4, total salary: $4,500,000'")

fun DepartmentStats.formatDetailed(): String = 
    TODO("Include all stats in a readable format")
```

### Step 4: User Analytics Service

**File**: `class/workshop/lesson_2/src/main/kotlin/UserAnalyticsService.kt`

```kotlin
class UserAnalyticsService {
    
    // TODO: Generate comprehensive department analysis
    fun analyzeDepartments(users: List<User>): Map<String, DepartmentStats> {
        // TODO: Group users by department
        // TODO: For each department, calculate:
        //   - User count
        //   - Average age  
        //   - Total salary
        //   - Average salary
        //   - Top performer (highest score)
        //   - Average performance score
        // TODO: Return map of department name to DepartmentStats
        
        return TODO("Implement department analysis using groupBy and map transformations")
    }
    
    // TODO: Find top performers across all departments
    fun getTopPerformers(users: List<User>, count: Int = 10): List<User> {
        // TODO: Sort by performance score descending
        // TODO: Take top N users
        // TODO: Return list of top performers
        
        return TODO("Implement top performers analysis")
    }
    
    // TODO: Generate activity insights
    fun generateActivityInsights(users: List<User>): ActivityInsights {
        // TODO: Calculate various activity metrics:
        //   - Total user count
        //   - Active user count and percentage
        //   - Users with projects (projectsCompleted > 0)
        //   - Recent joiners (joined in last 30 days)
        //   - Average projects per user
        
        return TODO("Implement activity insights calculation")
    }
    
    // TODO: Find users matching multiple criteria
    fun findUsersMatching(
        users: List<User>,
        department: String? = null,
        minAge: Int? = null,
        maxAge: Int? = null,
        minSalary: Double? = null,
        isActive: Boolean? = null,
        minPerformanceScore: Double? = null
    ): List<User> {
        // TODO: Chain multiple filter operations
        // TODO: Apply each filter only if the parameter is not null
        // TODO: Demonstrate functional programming with chained operations
        
        return TODO("Implement multi-criteria filtering")
    }
    
    // TODO: Calculate salary statistics by department
    fun getSalaryStatsByDepartment(users: List<User>): Map<String, SalaryStats> {
        // TODO: Group by department
        // TODO: For each department calculate min, max, average, median salary
        // TODO: Use collection operations like minOf, maxOf, average
        
        return TODO("Implement salary statistics calculation")
    }
    
    // TODO: Generate performance trends
    fun getPerformanceTrends(users: List<User>): Map<String, Double> {
        // TODO: Group users by experience level (Junior, Mid, Senior)
        // TODO: Calculate average performance score for each level
        // TODO: Return trends showing how performance varies by experience
        
        return TODO("Implement performance trend analysis")
    }
}

// TODO: Supporting data classes
data class ActivityInsights(
    val totalUsers: Int,
    val activeUsers: Int,
    val activePercentage: Double,
    val usersWithProjects: Int,
    val projectsPercentage: Double,
    val recentJoiners: Int,
    val recentJoinersPercentage: Double,
    val averageProjectsPerUser: Double
)

data class SalaryStats(
    val department: String,
    val minSalary: Double,
    val maxSalary: Double,
    val averageSalary: Double,
    val medianSalary: Double,
    val userCount: Int
)
```

### Step 5: Data Generator for Testing

**File**: `class/workshop/lesson_2/src/main/kotlin/UserDataGenerator.kt`

```kotlin
import java.time.LocalDate
import kotlin.random.Random

object UserDataGenerator {
    
    private val departments = listOf("Engineering", "Marketing", "Sales", "HR", "Finance")
    private val firstNames = listOf("Alice", "Bob", "Carol", "David", "Emma", "Frank", "Grace", "Henry", "Ivy", "Jack")
    private val lastNames = listOf("Johnson", "Chen", "Davis", "Wilson", "Brown", "Taylor", "Anderson", "Thomas", "Jackson", "White")
    
    // TODO: Generate realistic test data
    fun generateUsers(count: Int): List<User> {
        // TODO: Use ranges and random to create realistic data:
        //   - Random names from the lists above
        //   - Ages between 22-65
        //   - Random departments
        //   - Salaries between 40k-200k (vary by department)
        //   - Performance scores between 50-100
        //   - Join dates in last 5 years
        //   - Project counts 0-20
        //   - 90% active users
        
        return TODO("Generate $count realistic users with varied data")
    }
    
    // TODO: Generate users for specific testing scenarios
    fun generateTestScenarios(): List<User> {
        // TODO: Create specific users for testing edge cases:
        //   - High performers in each department
        //   - New joiners vs veterans
        //   - Different salary ranges
        //   - Mix of active/inactive users
        
        return TODO("Generate users for specific test scenarios")
    }
}
```

### Step 6: Main Application with Interactive Demo

**File**: `class/workshop/lesson_2/src/main/kotlin/Main.kt`

```kotlin
fun main() {
    println("=== User Analytics Dashboard ===")
    println("🚀 Welcome to Lesson 2: Collections & Functional Programming!\n")
    
    val analyticsService = UserAnalyticsService()
    
    // TODO: Generate sample data
    println("📊 Generating sample data...")
    val users = UserDataGenerator.generateUsers(1000)
    println("✓ Generated ${users.size} users\n")
    
    // TODO: Demonstrate department analysis
    println("🎯 Department Analysis:")
    val departmentStats = analyticsService.analyzeDepartments(users)
    // TODO: Print department stats in formatted way
    // Hint: departmentStats.values.forEach { println(it.formatSummary()) }
    
    // TODO: Show top performers
    println("\n🏆 Top Performers:")
    val topPerformers = analyticsService.getTopPerformers(users, 5)
    // TODO: Print top performers with ranking
    // Hint: topPerformers.forEachIndexed { index, user -> 
    //         println("${index + 1}. ${user.name} (${user.department}) - Score: ${user.performanceScore}")
    //       }
    
    // TODO: Display activity insights
    println("\n📈 Activity Insights:")
    val insights = analyticsService.generateActivityInsights(users)
    // TODO: Print activity insights in readable format
    
    // TODO: Demonstrate advanced filtering
    println("\n🔍 Advanced Search Examples:")
    
    // TODO: Find high-performing engineers
    val topEngineers = analyticsService.findUsersMatching(
        users = users,
        department = "Engineering",
        minPerformanceScore = 90.0,
        isActive = true
    )
    println("Top Engineers (90+ score): ${topEngineers.size}")
    
    // TODO: Find high-salary recent joiners
    val highSalaryNewbies = users
        .recentJoiners(90) // Last 90 days
        .filter { it.salary > 80000 }
    println("High-salary recent joiners: ${highSalaryNewbies.size}")
    
    // TODO: Show collection chaining examples
    println("\n⛓️ Collection Chaining Examples:")
    
    // TODO: Complex data transformation pipeline
    val seniorHighPerformers = users
        .filter { it.isActive }
        .filter { it.yearsOfExperience() >= 5 }
        .filter { it.isHighPerformer() }
        .sortedByDescending { it.performanceScore }
        .take(10)
    
    println("Senior high performers: ${seniorHighPerformers.size}")
    
    // TODO: Demonstrate grouping and aggregation
    println("\n📊 Grouping & Aggregation:")
    
    // TODO: Performance by experience level
    val performanceByLevel = users
        .groupBy { it.experienceLevel }
        .mapValues { (_, levelUsers) -> levelUsers.map { it.performanceScore }.average() }
    
    // TODO: Print performance trends
    // performanceByLevel.forEach { (level, avgScore) ->
    //     println("$level: Average score %.1f".format(avgScore))
    // }
    
    // TODO: Show salary statistics
    println("\n💰 Salary Analysis:")
    val salaryStats = analyticsService.getSalaryStatsByDepartment(users)
    // TODO: Print salary statistics for each department
    
    println("\n🎉 Workshop complete! You've mastered Kotlin collections!")
    println("📚 Next: Lesson 3 - OOP + Kotlin Features")
    
    // TODO: Bonus: Demonstrate some advanced collection operations
    println("\n🌟 Bonus: Advanced Operations")
    
    // TODO: Find the department with highest average performance
    val bestDepartment = departmentStats.maxByOrNull { it.value.averagePerformanceScore }
    // TODO: Print best performing department
    
    // TODO: Create a summary of all insights
    val summary = """
        |📋 Analytics Summary:
        |   Total Users: ${users.size}
        |   Departments: ${departmentStats.size}
        |   Top Performer: ${topPerformers.firstOrNull()?.name ?: "N/A"}
        |   Best Department: ${bestDepartment?.key ?: "N/A"}
        |   Active Rate: ${"%.1f".format(insights.activePercentage)}%
    """.trimMargin()
    
    println(summary)
}
```

## How to Run

1. **Navigate to the workshop directory:**
   ```bash
   cd class/workshop/lesson_2
   ```

2. **Build the project:**
   ```bash
   ./gradlew build
   ```

3. **Run the analytics dashboard:**
   ```bash
   ./gradlew run
   ```

4. **Run the tests:**
   ```bash
   ./gradlew test
   ```

## Success Criteria

Your implementation should:
- ✅ **Use functional operations** - map, filter, groupBy, sortedBy
- ✅ **Chain operations fluently** - readable transformation pipelines
- ✅ **Create extension functions** - domain-specific collection operations
- ✅ **Handle edge cases** - empty collections, null values
- ✅ **Demonstrate performance** - efficient collection processing
- ✅ **Generate realistic insights** - meaningful analytics output

## Key Learning Points

After completing this workshop, you should understand:

1. **Collection Types**: Lists, Sets, Maps and when to use each
2. **Functional Operations**: map, filter, groupBy, reduce, fold
3. **Extension Functions**: Adding domain-specific functionality
4. **Method Chaining**: Fluent, readable data transformation
5. **Performance Considerations**: When to use sequences for large data
6. **Real-World Patterns**: Data processing pipelines in applications

## Troubleshooting

**Common Issues:**

1. **"Type mismatch" errors** - Check your collection operation return types
2. **"Division by zero"** - Handle empty collections in average calculations
3. **Performance issues** - Use sequences for large data processing
4. **Null pointer exceptions** - Use safe operations with nullable data

**Collection Operation Tips:**
- Use `mapNotNull` when transformation might return null
- Use `firstOrNull()` instead of `first()` for safety
- Use `sumOf { }` instead of `map { }.sum()` for efficiency
- Use `associateBy` when creating maps from collections

## Extension Challenges

Once you complete the basic workshop:

1. **Add more analytics** - Department comparison, trend analysis
2. **Implement caching** - Cache expensive calculations
3. **Add data validation** - Ensure data quality in pipeline
4. **Create reports** - Generate formatted reports
5. **Add benchmarking** - Compare different approaches' performance

## Next Steps

- Compare your solution with `class/answer/lesson_2/`
- Experiment with different collection operations
- Try processing larger datasets to see performance characteristics
- Move on to Lesson 3: OOP + Kotlin Features

**Congratulations!** You've mastered Kotlin's powerful collection operations and functional programming features. These skills are essential for backend development and data processing!