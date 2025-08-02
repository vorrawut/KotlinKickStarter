/**
 * Lesson 1 Workshop: User Data Class
 * 
 * TODO: Complete this data class to demonstrate Kotlin's key features:
 * - Data classes with automatic equals/hashCode/toString
 * - Null safety with nullable and non-nullable types  
 * - Default parameter values
 * - Extension properties
 */

// TODO: Create a data class called User with the following properties:
// - name: String (non-nullable, required)
// - email: String (non-nullable, required) 
// - age: Int with default value 0
// - phone: String? (nullable, optional)
// - isActive: Boolean with default value true

data class User(
    // Add your properties here
    // Hint: data class User(val name: String, ...)
)

// TODO: Add an extension property that returns the user's display name
// Format: "FirstName L." (e.g., "John D.")
// Hint: Use string operations like split(), first(), etc.
val User.displayName: String
    get() = TODO("Extract first name and first letter of last name")
    // Example implementation approach:
    // - Split name by space
    // - Take first part as first name  
    // - Take first character of second part (if exists) as last initial
    // - Return "FirstName L." format

// TODO: Add another extension property for a summary
// Format: "John D. (28 years old)"
val User.summary: String
    get() = TODO("Combine display name with age information")