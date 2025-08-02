/**
 * Lesson 1 Complete Solution: User Service Class
 * 
 * This demonstrates:
 * - Class creation and methods
 * - Working with collections (mutableListOf)
 * - Data class copy() function
 * - String templates and formatting
 * - Null safety in return types
 */

class UserService {
    // In-memory storage for users
    private val users = mutableListOf<User>()
    
    // Create a function that creates and validates a user
    fun createUser(name: String, email: String, age: Int = 0, phone: String? = null): User? {
        // Validate all inputs using validation functions
        if (!validateUser(name, email, age, phone)) {
            return null
        }
        
        // Create user if validation passes
        val user = User(name, email, age, phone)
        
        // Add user to the users list
        users.add(user)
        
        // Return the created user
        return user
    }
    
    // Create a function that updates user email
    fun updateUserEmail(user: User, newEmail: String): User? {
        // Validate the new email
        if (!isValidEmail(newEmail)) {
            return null
        }
        
        // Use data class copy() function to create updated user
        val updatedUser = user.copy(email = newEmail)
        
        // Find and update the user in the list
        val index = users.indexOf(user)
        if (index >= 0) {
            users[index] = updatedUser
        }
        
        // Return updated user
        return updatedUser
    }
    
    // Create a function that formats user info using string templates
    fun formatUserInfo(user: User): String {
        val phoneInfo = user.phone?.let { "Phone: $it" } ?: "No phone"
        return "${user.displayName} (age: ${user.age}) - ${user.email} [$phoneInfo]"
    }
    
    // Get all active users using collection filtering
    fun getActiveUsers(): List<User> {
        return users.filter { it.isActive }
    }
    
    // Find user by email (case-insensitive)
    fun findUserByEmail(email: String): User? {
        return users.find { it.email.equals(email, ignoreCase = true) }
    }
    
    // Get user count statistics
    fun getUserStats(): String {
        val totalUsers = users.size
        val activeUsers = users.count { it.isActive }
        val usersWithPhone = users.count { it.phone != null }
        
        return "Total: $totalUsers, Active: $activeUsers, With Phone: $usersWithPhone"
    }
}