/**
 * Lesson 2 Workshop: Collection Extension Functions
 * 
 * TODO: Create powerful extension functions for User collections
 * These demonstrate:
 * - Extension functions on generic types
 * - Functional programming with collections
 * - Domain-specific operations
 * - Method chaining patterns
 */

// TODO: Basic filtering extensions

// Get users by department (case-insensitive)
fun List<User>.getUsersByDepartment(department: String): List<User> = 
    TODO("Filter users by department, ignoring case")
    // Hint: filter { it.department.equals(department, ignoreCase = true) }

// Get only active users
fun List<User>.getActiveUsers(): List<User> = 
    TODO("Filter only users where isActive is true")

// Get users with high performance (score >= 85)
fun List<User>.getHighPerformers(): List<User> = 
    TODO("Filter users with performanceScore >= 85")

// TODO: Sorting and ranking extensions

// Get top performers sorted by performance score
fun List<User>.getTopPerformers(count: Int = 5): List<User> = 
    TODO("Return top N users sorted by performance score (highest first)")
    // Hint: sortedByDescending { it.performanceScore }.take(count)

// Get users sorted by salary (highest first)
fun List<User>.getBySalaryDescending(): List<User> = 
    TODO("Sort users by salary in descending order")

// Get users sorted by experience (most experienced first)
fun List<User>.getByExperience(): List<User> = 
    TODO("Sort users by years of experience (use extension function)")

// TODO: Aggregation extensions

// Calculate average age of users
fun List<User>.averageAge(): Double = 
    TODO("Calculate average age, return 0.0 if list is empty")
    // Hint: if (isEmpty()) 0.0 else map { it.age }.average()

// Calculate total salary of all users  
fun List<User>.totalSalary(): Double = 
    TODO("Sum all user salaries")
    // Hint: sumOf { it.salary }

// Calculate average performance score
fun List<User>.averagePerformanceScore(): Double = 
    TODO("Calculate average performance score, return 0.0 if empty")

// Calculate average salary
fun List<User>.averageSalary(): Double = 
    TODO("Calculate average salary, return 0.0 if empty")

// TODO: Grouping extensions

// Group users by department with counts
fun List<User>.departmentCounts(): Map<String, Int> = 
    TODO("Group by department and return map of department to user count")
    // Hint: groupBy { it.department }.mapValues { it.value.size }

// Group users by experience level  
fun List<User>.groupByExperienceLevel(): Map<String, List<User>> = 
    TODO("Group users by their experience level (Junior/Mid/Senior)")

// Group users by performance category
fun List<User>.groupByPerformanceCategory(): Map<String, List<User>> = 
    TODO("Group users by performance category (Excellent/Good/Average/Poor)")

// Group users by salary bracket
fun List<User>.groupBySalaryBracket(): Map<String, List<User>> = 
    TODO("Group users by salary bracket (Low/Medium/High)")

// TODO: Time-based filtering extensions

// Get users who joined in the last N days
fun List<User>.recentJoiners(days: Int = 30): List<User> = 
    TODO("Filter users who joined within the last N days")
    // Hint: filter { it.isRecentHire(days) }

// Get users who joined in a specific year
fun List<User>.joinedInYear(year: Int): List<User> = 
    TODO("Filter users who joined in the specified year")
    // Hint: filter { it.joinDate.year == year }

// TODO: Project and productivity extensions

// Get users with high project completion (above average)
fun List<User>.productiveUsers(): List<User> = 
    TODO("Filter users with above-average project completion")
    // Hint: Calculate average projects first, then filter

// Get users with at least N projects completed
fun List<User>.withMinProjects(minProjects: Int): List<User> = 
    TODO("Filter users with at least minProjects completed")

// TODO: Statistical extensions

// Get performance distribution (count by category)
fun List<User>.performanceDistribution(): Map<String, Int> = 
    TODO("Return count of users in each performance category")
    // Hint: groupBy { it.performanceCategory }.mapValues { it.value.size }

// Get salary statistics (min, max, average)
fun List<User>.salaryStatistics(): Triple<Double, Double, Double> = 
    TODO("Return Triple of (min salary, max salary, average salary)")
    // Handle empty list case by returning Triple(0.0, 0.0, 0.0)

// Find user with highest performance score
fun List<User>.topPerformer(): User? = 
    TODO("Return user with highest performance score, null if empty")
    // Hint: maxByOrNull { it.performanceScore }

// Find user with highest salary
fun List<User>.highestPaid(): User? = 
    TODO("Return user with highest salary, null if empty")

// TODO: Complex filtering with multiple criteria

// Find users matching multiple criteria
fun List<User>.findMatching(
    department: String? = null,
    minAge: Int? = null,
    maxAge: Int? = null,
    minSalary: Double? = null,
    maxSalary: Double? = null,
    minPerformanceScore: Double? = null,
    isActive: Boolean? = null
): List<User> = 
    TODO("Chain multiple filters based on non-null parameters")
    // Hint: Start with 'this' and chain filter operations for each non-null parameter
    // Example: filter { department?.let { dept -> it.department.equals(dept, ignoreCase = true) } ?: true }

// TODO: Transformation extensions

// Extract all unique departments
fun List<User>.getUniqueDepartments(): Set<String> = 
    TODO("Return set of all unique department names")
    // Hint: map { it.department }.toSet()

// Get all user names in a department
fun List<User>.getNamesInDepartment(department: String): List<String> = 
    TODO("Return list of user names in the specified department")

// Create email list for active users
fun List<User>.getActiveUserEmails(): List<String> = 
    TODO("Return list of email addresses for active users only")