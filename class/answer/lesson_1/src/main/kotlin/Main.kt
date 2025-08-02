/**
 * Lesson 1 Complete Solution: Main Application
 * 
 * This demonstrates:
 * - Object creation and method calls
 * - Null safety handling with let, if statements
 * - String output and formatting
 * - Testing various scenarios
 */

fun main() {
    println("=== Kotlin User Management System ===")
    println("Welcome to Lesson 1: Learning Kotlin Fundamentals!\n")
    
    val userService = UserService()
    
    // Test 1 - Create valid users
    println("📝 Test 1: Creating valid users")
    
    // Create a user with all fields
    val user1 = userService.createUser(
        name = "John Doe",
        email = "john.doe@example.com",
        age = 28,
        phone = "+1234567890"
    )
    
    // Handle the nullable result and print success message
    user1?.let { println("✓ Created user: ${it.name}") }
        ?: println("✗ Failed to create user")
    
    // Create a user without phone (demonstrating default parameters)
    val user2 = userService.createUser(
        name = "Jane Smith", 
        email = "jane.smith@example.com",
        age = 32
    )
    
    // Print result for user2
    user2?.let { println("✓ Created user: ${it.name}") }
        ?: println("✗ Failed to create user")
    
    // Test 2 - Create users with invalid data
    println("\n🔍 Test 2: Testing validation")
    
    // Try to create user with invalid email
    val invalidEmailUser = userService.createUser(
        name = "Bob Wilson",
        email = "invalid-email-format", 
        age = 25
    )
    
    // Print result showing validation failed
    invalidEmailUser?.let { println("✗ Should not have created user with invalid email") } 
        ?: println("✓ Invalid email rejected correctly")
    
    // Try to create user with empty name
    val invalidNameUser = userService.createUser(
        name = "",
        email = "test@example.com",
        age = 30
    )
    
    // Print result for invalid name
    invalidNameUser?.let { println("✗ Should not have created user with empty name") }
        ?: println("✓ Empty name rejected correctly")
    
    // Try to create user with invalid age
    val invalidAgeUser = userService.createUser(
        name = "Old Person",
        email = "old@example.com", 
        age = 200
    )
    
    // Print result for invalid age
    invalidAgeUser?.let { println("✗ Should not have created user with invalid age") }
        ?: println("✓ Invalid age rejected correctly")
    
    // Test 3 - Update user email
    println("\n📧 Test 3: Testing email updates")
    
    // Update user1's email to a valid new email
    user1?.let { user ->
        val updatedUser = userService.updateUserEmail(user, "john.newemail@example.com")
        updatedUser?.let { println("✓ Email updated successfully to ${it.email}") }
            ?: println("✗ Email update failed")
    }
    
    // Try to update to an invalid email
    user1?.let { user ->
        val failedUpdate = userService.updateUserEmail(user, "invalid-email")
        failedUpdate?.let { println("✗ Should not have updated to invalid email") }
            ?: println("✓ Invalid email update rejected correctly")
    }
    
    // Test 4 - Demonstrate user info formatting
    println("\n📋 Test 4: User information display")
    
    // Show formatted user information
    user1?.let { user ->
        println("User Info: ${userService.formatUserInfo(user)}")
        println("Display Name: ${user.displayName}")
        println("Summary: ${user.summary}")
    }
    
    user2?.let { user ->
        println("User Info: ${userService.formatUserInfo(user)}")
        println("Display Name: ${user.displayName}")
        println("Summary: ${user.summary}")
    }
    
    // Test 5 - Show active users and statistics
    println("\n📊 Test 5: User statistics")
    
    // Display active users count
    val activeUsers = userService.getActiveUsers()
    println("Active users count: ${activeUsers.size}")
    
    // Display user statistics
    println("Statistics: ${userService.getUserStats()}")
    
    // Test 6 - Find user by email
    println("\n🔎 Test 6: Finding users")
    
    // Search for an existing user
    val foundUser = userService.findUserByEmail("jane.smith@example.com")
    foundUser?.let { println("✓ Found user: ${it.name}") }
        ?: println("✗ User not found")
    
    // Search for non-existent user
    val notFound = userService.findUserByEmail("notexist@example.com")
    notFound?.let { println("✗ Should not have found non-existent user") }
        ?: println("✓ Correctly returned null for non-existent user")
    
    // Test case-insensitive search
    val caseInsensitiveFound = userService.findUserByEmail("JANE.SMITH@EXAMPLE.COM")
    caseInsensitiveFound?.let { println("✓ Case-insensitive search works: ${it.name}") }
        ?: println("✗ Case-insensitive search failed")
    
    println("\n🎉 Workshop complete! All Kotlin fundamentals demonstrated.")
    println("📚 Next: Lesson 2 - Collections and Functional Programming")
    
    // Bonus: Demonstrate some additional Kotlin features
    println("\n🌟 Bonus: Additional Kotlin features")
    
    // Destructuring declaration with data class
    user1?.let { (name, email, age, phone, isActive) ->
        println("Destructured: name=$name, email=$email, age=$age")
    }
    
    // Using 'when' expression
    val ageCategory = user1?.age?.let { age ->
        when {
            age < 18 -> "Minor"
            age < 65 -> "Adult"
            else -> "Senior"
        }
    } ?: "Unknown"
    println("Age category: $ageCategory")
}