/**
 * Lesson 2 Complete Solution: Department Statistics Data Class
 * 
 * This demonstrates:
 * - Data classes for complex data structures
 * - Companion objects with factory functions
 * - Extension functions for formatting
 * - Business logic encapsulation
 */

// Comprehensive department analytics data class
data class DepartmentStats(
    val departmentName: String,
    val userCount: Int,
    val averageAge: Double,
    val totalSalary: Double,
    val averageSalary: Double,
    val topPerformer: User?,
    val averagePerformanceScore: Double,
    val minSalary: Double,
    val maxSalary: Double,
    val activeUserCount: Int
) {
    companion object {
        // Factory function to create DepartmentStats from a list of users
        fun fromUsers(departmentName: String, users: List<User>): DepartmentStats {
            if (users.isEmpty()) {
                return DepartmentStats(
                    departmentName = departmentName,
                    userCount = 0,
                    averageAge = 0.0,
                    totalSalary = 0.0,
                    averageSalary = 0.0,
                    topPerformer = null,
                    averagePerformanceScore = 0.0,
                    minSalary = 0.0,
                    maxSalary = 0.0,
                    activeUserCount = 0
                )
            }
            
            val activeUsers = users.filter { it.isActive }
            val totalSalary = users.sumOf { it.salary }
            
            return DepartmentStats(
                departmentName = departmentName,
                userCount = users.size,
                averageAge = users.map { it.age }.average(),
                totalSalary = totalSalary,
                averageSalary = totalSalary / users.size,
                topPerformer = users.maxByOrNull { it.performanceScore },
                averagePerformanceScore = users.map { it.performanceScore }.average(),
                minSalary = users.minOf { it.salary },
                maxSalary = users.maxOf { it.salary },
                activeUserCount = activeUsers.size
            )
        }
    }
}

// Extension functions for different formatting styles

// Format as summary line for dashboard
fun DepartmentStats.formatSummary(): String = 
    "$departmentName: $userCount users, avg age: ${"%.1f".format(averageAge)}, total salary: $${"%,.0f".format(totalSalary)}"

// Format as detailed report
fun DepartmentStats.formatDetailed(): String = """
    Department: $departmentName
    Total Users: $userCount ($activeUserCount active)
    Age: Average ${"%.1f".format(averageAge)} years
    Salary: $${"%,.0f".format(averageSalary)} average ($${"%,.0f".format(minSalary)} - $${"%,.0f".format(maxSalary)} range)
    Performance: ${"%.1f".format(averagePerformanceScore)} average score
    Top Performer: ${topPerformer?.name ?: "None"} ${topPerformer?.let { "(${"%.1f".format(it.performanceScore)} score)" } ?: ""}
""".trimIndent()

// Format for comparison (one-line with key metrics)
fun DepartmentStats.formatComparison(): String = 
    "$departmentName: $userCount users, $${"%,.0f".format(averageSalary.toInt())}K avg, ${"%.1f".format(averagePerformanceScore)} score"

// Format salary information specifically
fun DepartmentStats.formatSalaryInfo(): String = 
    "Salary: $${"%,.0f".format(averageSalary)} avg ($${"%,.0f".format(minSalary)} - $${"%,.0f".format(maxSalary)}), total: $${"%,.0f".format(totalSalary)}"

// Format performance information
fun DepartmentStats.formatPerformanceInfo(): String {
    val topPerformerInfo = topPerformer?.let { " (Top: ${it.name} - ${"%.1f".format(it.performanceScore)})" } ?: ""
    return "Performance: ${"%.1f".format(averagePerformanceScore)} average$topPerformerInfo"
}

// Extension properties for derived values

// Calculate the performance rating category for the department
val DepartmentStats.performanceRating: String
    get() = when {
        averagePerformanceScore >= 85 -> "Excellent"
        averagePerformanceScore >= 75 -> "Good"
        averagePerformanceScore >= 65 -> "Average"
        else -> "Poor"
    }

// Calculate salary competitiveness (compared to a baseline)
fun DepartmentStats.salaryCompetitiveness(marketAverage: Double): String {
    val difference = (averageSalary - marketAverage) / marketAverage
    return when {
        difference > 0.1 -> "Above Market"
        difference < -0.1 -> "Below Market"
        else -> "Competitive"
    }
}

// Calculate activity percentage
val DepartmentStats.activityPercentage: Double
    get() = if (userCount == 0) 0.0 else (activeUserCount.toDouble() / userCount) * 100

// Utility functions for data validation and health checks

// Check if department data looks healthy
fun DepartmentStats.isHealthy(): Boolean = 
    activityPercentage > 80 && 
    averagePerformanceScore > 70 && 
    userCount > 0 &&
    (maxSalary - minSalary) / averageSalary < 2.0 // Salary range not too extreme

// Get list of potential issues with this department
fun DepartmentStats.getIssues(): List<String> = buildList {
    if (activityPercentage < 80) {
        add("Low activity rate: ${"%.1f".format(activityPercentage)}%")
    }
    if (averagePerformanceScore < 70) {
        add("Below average performance: ${"%.1f".format(averagePerformanceScore)}")
    }
    if ((maxSalary - minSalary) > averageSalary * 1.5) {
        add("Large salary gap: $${"%,.0f".format(maxSalary - minSalary)} difference")
    }
    if (topPerformer == null) {
        add("No top performer identified")
    }
    if (userCount < 5) {
        add("Very small team size: $userCount users")
    }
}

// Comparison functions

// Compare this department with another
fun DepartmentStats.compareWith(other: DepartmentStats): String {
    val sizeDiff = userCount - other.userCount
    val salaryDiff = averageSalary - other.averageSalary
    val performanceDiff = averagePerformanceScore - other.averagePerformanceScore
    
    return buildString {
        append("$departmentName vs ${other.departmentName}:\n")
        append("Size: ${if (sizeDiff > 0) "+" else ""}$sizeDiff users\n")
        append("Salary: ${if (salaryDiff > 0) "+" else ""}$${"%,.0f".format(salaryDiff)} average\n")
        append("Performance: ${if (performanceDiff > 0) "+" else ""}${"%.1f".format(performanceDiff)} points")
    }
}