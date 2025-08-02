/**
 * Lesson 2 Complete Solution: Enhanced User Model with Analytics
 * 
 * This demonstrates:
 * - Data classes with complex properties
 * - Extension functions and properties
 * - Date handling with LocalDate
 * - Business logic through extensions
 */

import java.time.LocalDate
import java.time.Period

// Enhanced User data class with analytics properties
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

// Extension property for experience level based on join date
val User.experienceLevel: String
    get() {
        val years = yearsOfExperience()
        return when {
            years <= 2 -> "Junior"
            years <= 5 -> "Mid"
            else -> "Senior"
        }
    }

// Extension property for performance category based on score
val User.performanceCategory: String
    get() = when {
        performanceScore >= 90 -> "Excellent"
        performanceScore >= 75 -> "Good"
        performanceScore >= 60 -> "Average"
        else -> "Poor"
    }

// Extension function to check if user is a high performer
fun User.isHighPerformer(): Boolean = performanceScore >= 85

// Extension function to calculate years of experience
fun User.yearsOfExperience(): Int = Period.between(joinDate, LocalDate.now()).years

// Extension function for salary bracket categorization
fun User.salaryBracket(): String = when {
    salary < 50000 -> "Low"
    salary < 100000 -> "Medium"
    else -> "High"
}

// Extension function to check if user is recently hired
fun User.isRecentHire(days: Int = 90): Boolean = 
    joinDate.isAfter(LocalDate.now().minusDays(days.toLong()))

// Extension function to get formatted summary
fun User.getFormattedSummary(): String {
    val performanceStatus = if (isHighPerformer()) "High performer" else "Standard performer"
    return "$name ($department) - $experienceLevel level, $performanceStatus, $${salary.toInt()}"
}