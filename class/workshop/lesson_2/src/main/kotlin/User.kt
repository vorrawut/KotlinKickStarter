/**
 * Lesson 2 Workshop: Enhanced User Model with Analytics
 * 
 * TODO: Complete this enhanced user model to demonstrate:
 * - Data classes with more complex properties
 * - Extension functions and properties
 * - Date handling with LocalDate
 * - Business logic through extensions
 */

import java.time.LocalDate
import java.time.Period

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

// TODO: Create extension properties for user categorization

// Extension property for experience level based on join date
val User.experienceLevel: String
    get() = TODO("Categorize user by years since join date: 'Junior' (0-2), 'Mid' (3-5), 'Senior' (6+)")
    // Hint: Use yearsOfExperience() function and when expression

// Extension property for performance category based on score
val User.performanceCategory: String
    get() = TODO("Categorize by score: 'Excellent' (90+), 'Good' (75-90), 'Average' (60-75), 'Poor' (<60)")
    // Hint: Use when expression with ranges

// TODO: Create extension functions for business logic

// Extension function to check if user is a high performer
fun User.isHighPerformer(): Boolean = TODO("Return true if performanceScore >= 85")

// Extension function to calculate years of experience
fun User.yearsOfExperience(): Int = TODO("Calculate years between joinDate and LocalDate.now()")
    // Hint: Use Period.between(joinDate, LocalDate.now()).years

// Extension function for salary bracket categorization
fun User.salaryBracket(): String = TODO("Return: 'Low' (<50k), 'Medium' (50k-100k), 'High' (100k+)")
    // Hint: Use when expression with ranges

// Extension function to check if user is recently hired
fun User.isRecentHire(days: Int = 90): Boolean = TODO("Return true if joined within last 'days' days")
    // Hint: Compare joinDate with LocalDate.now().minusDays(days)

// Extension function to get formatted summary
fun User.getFormattedSummary(): String = TODO("Return formatted string with key user info")
    // Format: "John Doe (Engineering) - Senior level, High performer, $95,000"
    // Include: name, department, experience level, performance status, salary