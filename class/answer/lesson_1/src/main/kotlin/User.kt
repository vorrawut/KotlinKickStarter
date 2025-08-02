/**
 * Lesson 1 Complete Solution: User Data Class
 * 
 * This demonstrates Kotlin's key features:
 * - Data classes with automatic equals/hashCode/toString
 * - Null safety with nullable and non-nullable types  
 * - Default parameter values
 * - Extension properties
 */

data class User(
    val name: String,
    val email: String,
    val age: Int = 0,
    val phone: String? = null,
    val isActive: Boolean = true
)

// Extension property that returns the user's display name
// Format: "FirstName L." (e.g., "John D.")
val User.displayName: String
    get() {
        val parts = name.split(" ")
        val firstName = parts.first()
        val lastInitial = if (parts.size > 1) "${parts[1].first()}." else ""
        return "$firstName $lastInitial".trim()
    }

// Extension property for a user summary
// Format: "John D. (28 years old)"
val User.summary: String
    get() = "$displayName ($age years old)"