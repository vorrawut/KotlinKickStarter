/**
 * Lesson 2 Complete Solution: Collection Extension Functions
 * 
 * This demonstrates:
 * - Extension functions on generic types
 * - Functional programming with collections
 * - Domain-specific operations
 * - Method chaining patterns
 */

// Basic filtering extensions

// Get users by department (case-insensitive)
fun List<User>.getUsersByDepartment(department: String): List<User> = 
    filter { it.department.equals(department, ignoreCase = true) }

// Get only active users
fun List<User>.getActiveUsers(): List<User> = 
    filter { it.isActive }

// Get users with high performance (score >= 85)
fun List<User>.getHighPerformers(): List<User> = 
    filter { it.isHighPerformer() }

// Sorting and ranking extensions

// Get top performers sorted by performance score
fun List<User>.getTopPerformers(count: Int = 5): List<User> = 
    sortedByDescending { it.performanceScore }.take(count)

// Get users sorted by salary (highest first)
fun List<User>.getBySalaryDescending(): List<User> = 
    sortedByDescending { it.salary }

// Get users sorted by experience (most experienced first)
fun List<User>.getByExperience(): List<User> = 
    sortedByDescending { it.yearsOfExperience() }

// Aggregation extensions

// Calculate average age of users
fun List<User>.averageAge(): Double = 
    if (isEmpty()) 0.0 else map { it.age }.average()

// Calculate total salary of all users  
fun List<User>.totalSalary(): Double = 
    sumOf { it.salary }

// Calculate average performance score
fun List<User>.averagePerformanceScore(): Double = 
    if (isEmpty()) 0.0 else map { it.performanceScore }.average()

// Calculate average salary
fun List<User>.averageSalary(): Double = 
    if (isEmpty()) 0.0 else map { it.salary }.average()

// Grouping extensions

// Group users by department with counts
fun List<User>.departmentCounts(): Map<String, Int> = 
    groupBy { it.department }.mapValues { it.value.size }

// Group users by experience level  
fun List<User>.groupByExperienceLevel(): Map<String, List<User>> = 
    groupBy { it.experienceLevel }

// Group users by performance category
fun List<User>.groupByPerformanceCategory(): Map<String, List<User>> = 
    groupBy { it.performanceCategory }

// Group users by salary bracket
fun List<User>.groupBySalaryBracket(): Map<String, List<User>> = 
    groupBy { it.salaryBracket() }

// Time-based filtering extensions

// Get users who joined in the last N days
fun List<User>.recentJoiners(days: Int = 30): List<User> = 
    filter { it.isRecentHire(days) }

// Get users who joined in a specific year
fun List<User>.joinedInYear(year: Int): List<User> = 
    filter { it.joinDate.year == year }

// Project and productivity extensions

// Get users with high project completion (above average)
fun List<User>.productiveUsers(): List<User> {
    val avgProjects = if (isEmpty()) 0.0 else map { it.projectsCompleted }.average()
    return filter { it.projectsCompleted > avgProjects }
}

// Get users with at least N projects completed
fun List<User>.withMinProjects(minProjects: Int): List<User> = 
    filter { it.projectsCompleted >= minProjects }

// Statistical extensions

// Get performance distribution (count by category)
fun List<User>.performanceDistribution(): Map<String, Int> = 
    groupBy { it.performanceCategory }.mapValues { it.value.size }

// Get salary statistics (min, max, average)
fun List<User>.salaryStatistics(): Triple<Double, Double, Double> = 
    if (isEmpty()) Triple(0.0, 0.0, 0.0)
    else Triple(minOf { it.salary }, maxOf { it.salary }, averageSalary())

// Find user with highest performance score
fun List<User>.topPerformer(): User? = 
    maxByOrNull { it.performanceScore }

// Find user with highest salary
fun List<User>.highestPaid(): User? = 
    maxByOrNull { it.salary }

// Complex filtering with multiple criteria
fun List<User>.findMatching(
    department: String? = null,
    minAge: Int? = null,
    maxAge: Int? = null,
    minSalary: Double? = null,
    maxSalary: Double? = null,
    minPerformanceScore: Double? = null,
    isActive: Boolean? = null
): List<User> = 
    filter { user ->
        (department?.let { user.department.equals(it, ignoreCase = true) } ?: true) &&
        (minAge?.let { user.age >= it } ?: true) &&
        (maxAge?.let { user.age <= it } ?: true) &&
        (minSalary?.let { user.salary >= it } ?: true) &&
        (maxSalary?.let { user.salary <= it } ?: true) &&
        (minPerformanceScore?.let { user.performanceScore >= it } ?: true) &&
        (isActive?.let { user.isActive == it } ?: true)
    }

// Transformation extensions

// Extract all unique departments
fun List<User>.getUniqueDepartments(): Set<String> = 
    map { it.department }.toSet()

// Get all user names in a department
fun List<User>.getNamesInDepartment(department: String): List<String> = 
    getUsersByDepartment(department).map { it.name }

// Create email list for active users
fun List<User>.getActiveUserEmails(): List<String> = 
    getActiveUsers().map { it.email }