/**
 * Lesson 1 Workshop: User Service Class
 * 
 * TODO: Complete this service class to demonstrate:
 * - Class creation and methods
 * - Working with collections (mutableListOf)
 * - Data class copy() function
 * - String templates and formatting
 * - Null safety in return types
 */

class UserService {
    // In-memory storage for users
    private val users = mutableListOf<User>()
    
    // TODO: Create a function that creates and validates a user
    fun createUser(name: String, email: String, age: Int = 0, phone: String? = null): User? {
        // TODO: Step 1 - Validate all inputs using validation functions
        // Use the validateUser function from UserValidator.kt
        // Hint: if (!validateUser(name, email, age, phone)) return null
        
        // TODO: Step 2 - Create user if validation passes
        // Create a User object with the provided parameters
        
        // TODO: Step 3 - Add user to the users list
        // Hint: users.add(user)
        
        // TODO: Step 4 - Return the created user
        
        return TODO("Implement user creation with validation")
    }
    
    // TODO: Create a function that updates user email
    fun updateUserEmail(user: User, newEmail: String): User? {
        // TODO: Step 1 - Validate the new email
        // Use isValidEmail function
        
        // TODO: Step 2 - Use data class copy() function to create updated user
        // Hint: val updatedUser = user.copy(email = newEmail)
        
        // TODO: Step 3 - Find and update the user in the list
        // Find the index of the original user and replace it
        // Hint: users.indexOf(user), then users[index] = updatedUser
        
        // TODO: Step 4 - Return updated user or null if validation fails
        
        return TODO("Implement email update")
    }
    
    // TODO: Create a function that formats user info using string templates
    fun formatUserInfo(user: User): String {
        // TODO: Use string templates to create formatted output
        // Format: "John D. (age: 28) - john@example.com [Phone: +1234567890]"
        // If no phone: "John D. (age: 28) - john@example.com [No phone]"
        // 
        // Hints:
        // - Use user.displayName extension property
        // - Use string templates: "text $variable more text"
        // - Handle nullable phone: user.phone?.let { "Phone: $it" } ?: "No phone"
        
        return TODO("Implement user info formatting with string templates")
    }
    
    // TODO: Get all active users using collection filtering
    fun getActiveUsers(): List<User> {
        // TODO: Filter users by isActive property
        // Hint: users.filter { it.isActive }
        
        return TODO("Return only active users")
    }
    
    // TODO: Find user by email (case-insensitive)
    fun findUserByEmail(email: String): User? {
        // TODO: Search through users list for matching email
        // Make the search case-insensitive
        // Hint: users.find { it.email.equals(email, ignoreCase = true) }
        
        return TODO("Implement user search by email")
    }
    
    // TODO: Get user count statistics
    fun getUserStats(): String {
        // TODO: Calculate statistics and return formatted string
        // Include: total users, active users, users with phone numbers
        // Use string templates and collection operations
        // Hint: 
        // - users.size for total
        // - users.count { it.isActive } for active
        // - users.count { it.phone != null } for users with phone
        
        return TODO("Implement user statistics")
    }
}