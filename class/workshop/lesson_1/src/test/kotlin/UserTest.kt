/**
 * Lesson 1 Workshop: Unit Tests
 * 
 * TODO: Complete these tests to demonstrate:
 * - JUnit 5 testing with Kotlin
 * - Assertion methods
 * - Testing null safety
 * - Testing validation logic
 */

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.BeforeEach

class UserTest {
    
    private lateinit var userService: UserService
    
    @BeforeEach
    fun setup() {
        userService = UserService()
    }
    
    @Test
    @DisplayName("Should create user with valid data")
    fun `should create user with valid data`() {
        // TODO: Test that valid user data creates a user successfully
        val user = userService.createUser("John Doe", "john@example.com", 25)
        
        // TODO: Assert that user is not null
        // Hint: assertNotNull(user)
        
        // TODO: Assert user properties are correct
        // Hint: assertEquals("John Doe", user?.name)
        //       assertEquals("john@example.com", user?.email)
        //       assertEquals(25, user?.age)
        
        TODO("Complete the user creation test")
    }
    
    @Test
    @DisplayName("Should reject user with invalid email")
    fun `should reject invalid email`() {
        // TODO: Test that invalid email returns null
        val user = userService.createUser("John Doe", "invalid-email", 25)
        
        // TODO: Assert that user is null
        // Hint: assertNull(user)
        
        TODO("Complete the invalid email test")
    }
    
    @Test
    @DisplayName("Should reject user with empty name")
    fun `should reject empty name`() {
        // TODO: Test that empty name returns null
        val user = userService.createUser("", "john@example.com", 25)
        
        // TODO: Assert that user is null
        
        TODO("Complete the empty name test")
    }
    
    @Test
    @DisplayName("Should reject user with invalid age")
    fun `should reject invalid age`() {
        // TODO: Test that invalid age returns null
        val user = userService.createUser("John Doe", "john@example.com", 200)
        
        // TODO: Assert that user is null
        
        TODO("Complete the invalid age test")
    }
    
    @Test
    @DisplayName("Should update user email successfully")
    fun `should update user email`() {
        // TODO: Create a user first
        val user = userService.createUser("John Doe", "john@example.com", 25)
        
        // TODO: Update the email
        val updatedUser = user?.let { userService.updateUserEmail(it, "newemail@example.com") }
        
        // TODO: Assert that update was successful
        // TODO: Assert that email was changed
        // Hint: assertEquals("newemail@example.com", updatedUser?.email)
        
        TODO("Complete the email update test")
    }
    
    @Test
    @DisplayName("Should format user info correctly")
    fun `should format user info correctly`() {
        // TODO: Create a test user
        val user = User("John Doe", "john@example.com", 25, "+1234567890", true)
        
        // TODO: Format user info
        val info = userService.formatUserInfo(user)
        
        // TODO: Assert the formatted string contains expected information
        // Hint: assertTrue(info.contains("John D."))
        //       assertTrue(info.contains("25"))
        //       assertTrue(info.contains("john@example.com"))
        //       assertTrue(info.contains("+1234567890"))
        
        TODO("Complete the formatting test")
    }
    
    @Test
    @DisplayName("Should test display name extension property")
    fun `should create correct display name`() {
        // TODO: Create a user and test the displayName extension
        val user = User("John Doe", "john@example.com")
        
        // TODO: Assert display name format
        // Hint: assertEquals("John D.", user.displayName)
        
        TODO("Complete the display name test")
    }
    
    @Test
    @DisplayName("Should filter active users")
    fun `should return only active users`() {
        // TODO: Create both active and inactive users
        userService.createUser("Active User", "active@example.com", 25)
        // Note: You'll need to figure out how to create inactive users
        // or modify the User class to support setting isActive to false
        
        // TODO: Get active users
        val activeUsers = userService.getActiveUsers()
        
        // TODO: Assert all returned users are active
        // Hint: assertTrue(activeUsers.all { it.isActive })
        
        TODO("Complete the active users test")
    }
    
    @Test
    @DisplayName("Should find user by email case-insensitively")
    fun `should find user by email ignore case`() {
        // TODO: Create a user
        userService.createUser("John Doe", "John@Example.com", 25)
        
        // TODO: Search with different case
        val foundUser = userService.findUserByEmail("john@example.com")
        
        // TODO: Assert user was found
        // Hint: assertNotNull(foundUser)
        //       assertEquals("John Doe", foundUser?.name)
        
        TODO("Complete the case-insensitive search test")
    }
}