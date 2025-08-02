/**
 * Lesson 1 Workshop: Main Application
 * 
 * TODO: Complete this main function to demonstrate:
 * - Object creation and method calls
 * - Null safety handling with let, if statements
 * - String output and formatting
 * - Testing various scenarios
 */

fun main() {
    println("=== Kotlin User Management System ===")
    println("Welcome to Lesson 1: Learning Kotlin Fundamentals!\n")
    
    val userService = UserService()
    
    // TODO: Test 1 - Create valid users
    println("📝 Test 1: Creating valid users")
    
    // TODO: Create a user with all fields
    val user1 = userService.createUser(
        name = "John Doe",
        email = "john.doe@example.com",
        age = 28,
        phone = "+1234567890"
    )
    
    // TODO: Handle the nullable result and print success message
    // Hint: user1?.let { println("✓ Created user: ${it.name}") }
    //       ?: println("✗ Failed to create user")
    
    // TODO: Create a user without phone (demonstrating default parameters)
    val user2 = userService.createUser(
        name = "Jane Smith", 
        email = "jane.smith@example.com",
        age = 32
    )
    
    // TODO: Print result for user2
    
    // TODO: Test 2 - Create users with invalid data
    println("\n🔍 Test 2: Testing validation")
    
    // TODO: Try to create user with invalid email
    val invalidEmailUser = userService.createUser(
        name = "Bob Wilson",
        email = "invalid-email-format", 
        age = 25
    )
    
    // TODO: Print result showing validation failed
    // Hint: invalidEmailUser?.let { ... } ?: println("✗ Invalid email rejected correctly")
    
    // TODO: Try to create user with empty name
    val invalidNameUser = userService.createUser(
        name = "",
        email = "test@example.com",
        age = 30
    )
    
    // TODO: Print result for invalid name
    
    // TODO: Try to create user with invalid age
    val invalidAgeUser = userService.createUser(
        name = "Old Person",
        email = "old@example.com", 
        age = 200
    )
    
    // TODO: Print result for invalid age
    
    // TODO: Test 3 - Update user email
    println("\n📧 Test 3: Testing email updates")
    
    // TODO: Update user1's email to a valid new email
    user1?.let { user ->
        val updatedUser = userService.updateUserEmail(user, "john.newemail@example.com")
        // TODO: Print update result
        // updatedUser?.let { println("✓ Email updated successfully") }
        //     ?: println("✗ Email update failed")
    }
    
    // TODO: Try to update to an invalid email
    user1?.let { user ->
        val failedUpdate = userService.updateUserEmail(user, "invalid-email")
        // TODO: Print failed update result
    }
    
    // TODO: Test 4 - Demonstrate user info formatting
    println("\n📋 Test 4: User information display")
    
    // TODO: Show formatted user information
    user1?.let { user ->
        println("User Info: ${userService.formatUserInfo(user)}")
        println("Display Name: ${user.displayName}")
        println("Summary: ${user.summary}")
    }
    
    // TODO: Test 5 - Show active users and statistics
    println("\n📊 Test 5: User statistics")
    
    // TODO: Display active users count
    val activeUsers = userService.getActiveUsers()
    println("Active users count: ${activeUsers.size}")
    
    // TODO: Display user statistics
    println("Statistics: ${userService.getUserStats()}")
    
    // TODO: Test 6 - Find user by email
    println("\n🔎 Test 6: Finding users")
    
    // TODO: Search for an existing user
    val foundUser = userService.findUserByEmail("jane.smith@example.com")
    // TODO: Print search result
    // foundUser?.let { println("✓ Found user: ${it.name}") }
    //     ?: println("✗ User not found")
    
    // TODO: Search for non-existent user
    val notFound = userService.findUserByEmail("notexist@example.com")
    // TODO: Print search result
    
    println("\n🎉 Workshop complete! Check your implementation against the answers.")
    println("📚 Next: Lesson 2 - Collections and Functional Programming")
}