/**
 * Lesson 1 Complete Solution: Unit Tests
 * 
 * This demonstrates:
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
        // Test that valid user data creates a user successfully
        val user = userService.createUser("John Doe", "john@example.com", 25)
        
        // Assert that user is not null
        assertNotNull(user)
        
        // Assert user properties are correct
        assertEquals("John Doe", user?.name)
        assertEquals("john@example.com", user?.email)
        assertEquals(25, user?.age)
        assertTrue(user?.isActive == true)
    }
    
    @Test
    @DisplayName("Should reject user with invalid email")
    fun `should reject invalid email`() {
        // Test that invalid email returns null
        val user = userService.createUser("John Doe", "invalid-email", 25)
        
        // Assert that user is null
        assertNull(user)
    }
    
    @Test
    @DisplayName("Should reject user with empty name")
    fun `should reject empty name`() {
        // Test that empty name returns null
        val user = userService.createUser("", "john@example.com", 25)
        
        // Assert that user is null
        assertNull(user)
    }
    
    @Test
    @DisplayName("Should reject user with invalid age")
    fun `should reject invalid age`() {
        // Test that invalid age returns null
        val user = userService.createUser("John Doe", "john@example.com", 200)
        
        // Assert that user is null
        assertNull(user)
    }
    
    @Test
    @DisplayName("Should update user email successfully")
    fun `should update user email`() {
        // Create a user first
        val user = userService.createUser("John Doe", "john@example.com", 25)
        assertNotNull(user)
        
        // Update the email
        val updatedUser = user?.let { userService.updateUserEmail(it, "newemail@example.com") }
        
        // Assert that update was successful
        assertNotNull(updatedUser)
        
        // Assert that email was changed
        assertEquals("newemail@example.com", updatedUser?.email)
        
        // Assert other properties remain the same
        assertEquals("John Doe", updatedUser?.name)
        assertEquals(25, updatedUser?.age)
    }
    
    @Test
    @DisplayName("Should reject invalid email update")
    fun `should reject invalid email update`() {
        // Create a user first
        val user = userService.createUser("John Doe", "john@example.com", 25)
        assertNotNull(user)
        
        // Try to update with invalid email
        val updatedUser = user?.let { userService.updateUserEmail(it, "invalid-email") }
        
        // Assert that update failed
        assertNull(updatedUser)
    }
    
    @Test
    @DisplayName("Should format user info correctly")
    fun `should format user info correctly`() {
        // Create a test user
        val user = User("John Doe", "john@example.com", 25, "+1234567890", true)
        
        // Format user info
        val info = userService.formatUserInfo(user)
        
        // Assert the formatted string contains expected information
        assertTrue(info.contains("John D."))
        assertTrue(info.contains("25"))
        assertTrue(info.contains("john@example.com"))
        assertTrue(info.contains("+1234567890"))
    }
    
    @Test
    @DisplayName("Should handle user without phone correctly")
    fun `should format user info without phone`() {
        // Create a test user without phone
        val user = User("Jane Smith", "jane@example.com", 30, null, true)
        
        // Format user info
        val info = userService.formatUserInfo(user)
        
        // Assert the formatted string contains expected information
        assertTrue(info.contains("Jane S."))
        assertTrue(info.contains("30"))
        assertTrue(info.contains("jane@example.com"))
        assertTrue(info.contains("No phone"))
    }
    
    @Test
    @DisplayName("Should test display name extension property")
    fun `should create correct display name`() {
        // Create a user and test the displayName extension
        val user = User("John Doe", "john@example.com")
        
        // Assert display name format
        assertEquals("John D.", user.displayName)
        
        // Test with single name
        val singleNameUser = User("Madonna", "madonna@example.com")
        assertEquals("Madonna", singleNameUser.displayName)
    }
    
    @Test
    @DisplayName("Should test summary extension property")
    fun `should create correct summary`() {
        // Create a user and test the summary extension
        val user = User("John Doe", "john@example.com", 28)
        
        // Assert summary format
        assertEquals("John D. (28 years old)", user.summary)
    }
    
    @Test
    @DisplayName("Should filter active users")
    fun `should return only active users`() {
        // Create both active and inactive users
        val activeUser = userService.createUser("Active User", "active@example.com", 25)
        assertNotNull(activeUser)
        
        // Create an inactive user by modifying the users list directly
        // (In real scenarios, you'd have a method to deactivate users)
        val inactiveUser = User("Inactive User", "inactive@example.com", 30, null, false)
        // Since we can't directly add inactive users through createUser (it always creates active users),
        // we'll test the filter functionality with what we have
        
        // Get active users
        val activeUsers = userService.getActiveUsers()
        
        // Assert all returned users are active
        assertTrue(activeUsers.all { it.isActive })
        assertTrue(activeUsers.isNotEmpty())
    }
    
    @Test
    @DisplayName("Should find user by email case-insensitively")
    fun `should find user by email ignore case`() {
        // Create a user
        val created = userService.createUser("John Doe", "John@Example.com", 25)
        assertNotNull(created)
        
        // Search with different case
        val foundUser = userService.findUserByEmail("john@example.com")
        
        // Assert user was found
        assertNotNull(foundUser)
        assertEquals("John Doe", foundUser?.name)
        
        // Test with uppercase
        val foundUppercase = userService.findUserByEmail("JOHN@EXAMPLE.COM")
        assertNotNull(foundUppercase)
        assertEquals("John Doe", foundUppercase?.name)
    }
    
    @Test
    @DisplayName("Should return null for non-existent user")
    fun `should return null for non existent user`() {
        // Search for non-existent user
        val notFound = userService.findUserByEmail("notexist@example.com")
        
        // Assert user was not found
        assertNull(notFound)
    }
    
    @Test
    @DisplayName("Should generate correct user statistics")
    fun `should generate correct user statistics`() {
        // Create users with different properties
        userService.createUser("User1", "user1@example.com", 25, "+1234567890")
        userService.createUser("User2", "user2@example.com", 30, null)
        userService.createUser("User3", "user3@example.com", 35, "+0987654321")
        
        // Get statistics
        val stats = userService.getUserStats()
        
        // Verify statistics format and content
        assertTrue(stats.contains("Total: 3"))
        assertTrue(stats.contains("Active: 3"))
        assertTrue(stats.contains("With Phone: 2"))
    }
    
    @Test
    @DisplayName("Should validate email correctly")
    fun `should validate email formats`() {
        // Test valid emails
        assertTrue(isValidEmail("test@example.com"))
        assertTrue(isValidEmail("user123@domain.org"))
        
        // Test invalid emails
        assertFalse(isValidEmail(null))
        assertFalse(isValidEmail(""))
        assertFalse(isValidEmail("invalid-email"))
        assertFalse(isValidEmail("@example.com"))
        assertFalse(isValidEmail("test@"))
        assertFalse(isValidEmail("test.example.com"))
    }
    
    @Test
    @DisplayName("Should validate names correctly")
    fun `should validate names`() {
        // Test valid names
        assertTrue(isValidName("John"))
        assertTrue(isValidName("John Doe"))
        assertTrue(isValidName("A B"))
        
        // Test invalid names
        assertFalse(isValidName(null))
        assertFalse(isValidName(""))
        assertFalse(isValidName(" "))
        assertFalse(isValidName("J"))
    }
    
    @Test
    @DisplayName("Should validate ages correctly")
    fun `should validate ages`() {
        // Test valid ages
        assertTrue(isValidAge(0))
        assertTrue(isValidAge(25))
        assertTrue(isValidAge(150))
        
        // Test invalid ages
        assertFalse(isValidAge(-1))
        assertFalse(isValidAge(151))
        assertFalse(isValidAge(200))
    }
}