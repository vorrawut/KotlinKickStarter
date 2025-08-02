/**
 * Lesson 2 Workshop: Department Statistics Data Class
 * 
 * TODO: Complete this data class and extension functions to demonstrate:
 * - Data classes for complex data structures
 * - Extension functions for formatting
 * - Business logic encapsulation
 * - String formatting and templates
 */

// TODO: Create a data class to hold comprehensive department analytics
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
)

// TODO: Create a companion object with factory function
companion object {
    // Factory function to create DepartmentStats from a list of users
    fun fromUsers(departmentName: String, users: List<User>): DepartmentStats {
        // TODO: Calculate all statistics from the user list
        // Handle empty list case appropriately
        // Hint: Use collection operations like average(), sumOf(), minOf(), etc.
        
        return TODO("Create DepartmentStats by calculating statistics from users list")
        
        // Sample implementation structure:
        // val activeUsers = users.filter { it.isActive }
        // val avgAge = if (users.isEmpty()) 0.0 else users.map { it.age }.average()
        // etc.
    }
}

// TODO: Extension functions for different formatting styles

// Format as summary line for dashboard
fun DepartmentStats.formatSummary(): String = 
    TODO("Format as: 'Engineering: 45 users, avg age: 32.4, total salary: $4,500,000'")
    // Hint: Use string templates and format numbers appropriately
    // Consider using String.format or "%.1f" for decimal formatting

// Format as detailed report
fun DepartmentStats.formatDetailed(): String = 
    TODO("Create multi-line detailed format with all statistics")
    // Include: department name, user counts, salary stats, performance info
    // Example format:
    // """
    // Department: Engineering
    // Total Users: 45 (42 active)
    // Age: Average 32.4 years
    // Salary: $75,000 average ($45,000 - $150,000 range)
    // Performance: 82.5 average score
    // Top Performer: Alice Johnson (95.5 score)
    // """

// Format for comparison (one-line with key metrics)
fun DepartmentStats.formatComparison(): String = 
    TODO("Format for side-by-side comparison: 'Engineering: 45 users, $75K avg, 82.5 score'")

// Format salary information specifically
fun DepartmentStats.formatSalaryInfo(): String = 
    TODO("Format salary details: 'Salary: $75,000 avg ($45,000 - $150,000), total: $3,375,000'")

// Format performance information
fun DepartmentStats.formatPerformanceInfo(): String = 
    TODO("Format performance details with top performer info")

// TODO: Extension properties for derived values

// Calculate the performance rating category for the department
val DepartmentStats.performanceRating: String
    get() = TODO("Return 'Excellent' (85+), 'Good' (75-85), 'Average' (65-75), 'Poor' (<65)")
    // Based on averagePerformanceScore

// Calculate salary competitiveness (compared to a baseline)
fun DepartmentStats.salaryCompetitiveness(marketAverage: Double): String = 
    TODO("Return 'Above Market' (10%+), 'Competitive' (±10%), 'Below Market' (-10%+)")
    // Compare averageSalary to marketAverage

// Calculate activity percentage
val DepartmentStats.activityPercentage: Double
    get() = TODO("Return percentage of active users in department")
    // Hint: (activeUserCount.toDouble() / userCount) * 100

// TODO: Utility functions for data validation and health checks

// Check if department data looks healthy
fun DepartmentStats.isHealthy(): Boolean = 
    TODO("Return true if department metrics are within normal ranges")
    // Consider factors like:
    // - Activity rate > 80%
    // - Average performance > 70
    // - Reasonable salary ranges
    // - Non-zero user count

// Get list of potential issues with this department
fun DepartmentStats.getIssues(): List<String> = 
    TODO("Return list of potential issues detected in the data")
    // Example issues:
    // - "Low activity rate: X%"
    // - "Below average performance: X"
    // - "Large salary gap: $X difference"
    // - "No top performer identified"

// TODO: Comparison functions

// Compare this department with another
fun DepartmentStats.compareWith(other: DepartmentStats): String = 
    TODO("Return comparison summary highlighting key differences")
    // Compare user count, salaries, performance, etc.
    // Return a readable summary of differences