# 🔨 Workshop 1: Building a User Management Foundation

## What We Want to Build

Create a simple but robust user management system demonstrating Kotlin's core features: data classes, null safety, functions, and string templates. You'll build the foundation that will evolve throughout the entire curriculum.

## Expected Result

By the end of this workshop, you'll have:
- ✅ A `User` data class with null-safe properties
- ✅ A `UserService` class with validation and business logic
- ✅ A simple main function that demonstrates all features
- ✅ Proper error handling using Kotlin's null safety

**Sample Output:**
```
=== User Management System ===
✓ Created user: John Doe (john.doe@example.com)
✓ User validation passed
✓ Updated user email successfully
✗ Invalid email format rejected
✓ Display name: John D. (age: 28)
```

## Step-by-Step Code Guide

### Step 1: Create the User Data Class

Navigate to your workshop folder and create the foundation:

**File**: `class/workshop/lesson_1/src/main/kotlin/User.kt`

```kotlin
// TODO: Create a data class called User with the following properties:
// - name: String (non-nullable)
// - email: String (non-nullable) 
// - age: Int with default value 0
// - phone: String? (nullable)
// - isActive: Boolean with default value true

data class User(
    // Add your properties here
)

// TODO: Add a computed property that returns the user's display name
// Format: "FirstName L." (e.g., "John D.")
val User.displayName: String
    get() = TODO("Implement display name logic")
```

### Step 2: Create User Validation Functions

**File**: `class/workshop/lesson_1/src/main/kotlin/UserValidator.kt`

```kotlin
// TODO: Create validation functions that demonstrate Kotlin features

// Email validation using regex and null safety
fun isValidEmail(email: String?): Boolean {
    // TODO: Return false if email is null
    // TODO: Use regex to validate email format
    // Pattern: word characters @ word characters . word characters
    return TODO("Implement email validation")
}

// Name validation demonstrating string operations
fun isValidName(name: String?): Boolean {
    // TODO: Use null safety operators
    // TODO: Check if name is not null, not blank, and at least 2 characters
    return TODO("Implement name validation")
}

// Age validation with range checks
fun isValidAge(age: Int): Boolean {
    // TODO: Age should be between 0 and 150
    return TODO("Implement age validation")
}
```

### Step 3: Create the UserService Class

**File**: `class/workshop/lesson_1/src/main/kotlin/UserService.kt`

```kotlin
class UserService {
    // In-memory storage for users
    private val users = mutableListOf<User>()
    
    // TODO: Create a function that creates and validates a user
    fun createUser(name: String, email: String, age: Int = 0, phone: String? = null): User? {
        // TODO: Validate all inputs using the validation functions
        // TODO: If validation passes, create user and add to list
        // TODO: Return the created user, or null if validation fails
        
        return TODO("Implement user creation with validation")
    }
    
    // TODO: Create a function that updates user email
    fun updateUserEmail(user: User, newEmail: String): User? {
        // TODO: Validate the new email
        // TODO: Use data class copy() function to create updated user
        // TODO: Update the user in the list
        // TODO: Return updated user or null if validation fails
        
        return TODO("Implement email update")
    }
    
    // TODO: Create a function that formats user info
    fun formatUserInfo(user: User): String {
        // TODO: Use string templates to create a formatted string
        // TODO: Include display name, age, and email
        // TODO: Handle nullable phone number gracefully
        // Format: "John D. (age: 28) - john@example.com [Phone: +1234567890]"
        // Or: "John D. (age: 28) - john@example.com [No phone]"
        
        return TODO("Implement user info formatting")
    }
    
    // TODO: Get all active users
    fun getActiveUsers(): List<User> {
        // TODO: Filter users by isActive property
        return TODO("Return only active users")
    }
}
```

### Step 4: Create the Main Application

**File**: `class/workshop/lesson_1/src/main/kotlin/Main.kt`

```kotlin
fun main() {
    println("=== User Management System ===")
    
    val userService = UserService()
    
    // TODO: Test user creation with valid data
    val user1 = userService.createUser(
        name = "John Doe",
        email = "john.doe@example.com", 
        age = 28,
        phone = "+1234567890"
    )
    
    // TODO: Handle the nullable result and print success/failure
    // Use user1?.let { } or if statement
    
    // TODO: Test user creation with invalid data
    val invalidUser = userService.createUser(
        name = "", // Invalid name
        email = "invalid-email", // Invalid email
        age = 200 // Invalid age
    )
    
    // TODO: Print result showing validation failed
    
    // TODO: Test email update functionality
    user1?.let { user ->
        val updatedUser = userService.updateUserEmail(user, "john.new@example.com")
        // TODO: Print update result
    }
    
    // TODO: Test invalid email update
    user1?.let { user ->
        val failedUpdate = userService.updateUserEmail(user, "invalid-email")
        // TODO: Print failed update result
    }
    
    // TODO: Demonstrate user info formatting
    user1?.let { user ->
        println(userService.formatUserInfo(user))
    }
    
    // TODO: Show active users list
    val activeUsers = userService.getActiveUsers()
    println("Active users: ${activeUsers.size}")
}
```

### Step 5: Test Your Implementation

Create a simple test to verify everything works:

**File**: `class/workshop/lesson_1/src/test/kotlin/UserTest.kt`

```kotlin
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class UserTest {
    
    @Test
    fun `should create user with valid data`() {
        // TODO: Test that valid user data creates a user successfully
        val service = UserService()
        val user = service.createUser("John Doe", "john@example.com", 25)
        
        // TODO: Assert that user is not null
        // TODO: Assert user properties are correct
    }
    
    @Test
    fun `should reject invalid email`() {
        // TODO: Test that invalid email returns null
        val service = UserService()
        val user = service.createUser("John Doe", "invalid-email", 25)
        
        // TODO: Assert that user is null
    }
    
    @Test
    fun `should format user info correctly`() {
        // TODO: Test the user info formatting
        val service = UserService()
        val user = User("John Doe", "john@example.com", 25, "+1234567890")
        val info = service.formatUserInfo(user)
        
        // TODO: Assert the formatted string contains expected information
    }
}
```

## How to Run

1. **Navigate to the workshop directory:**
   ```bash
   cd class/workshop/lesson_1
   ```

2. **Build the project:**
   ```bash
   ../../gradlew build
   ```

3. **Run the main application:**
   ```bash
   ../../gradlew run
   ```

4. **Run the tests:**
   ```bash
   ../../gradlew test
   ```

## Success Criteria

Your implementation should:
- ✅ **Compile without errors** - Kotlin's type system catches issues early
- ✅ **Handle null safety properly** - No `!!` operators, proper nullable handling
- ✅ **Demonstrate data classes** - Use `copy()`, automatic `toString()`, etc.
- ✅ **Use string templates** - No string concatenation
- ✅ **Validate input data** - Reject invalid emails, names, and ages
- ✅ **Print meaningful output** - Show validation results and user information

## Key Learning Points

After completing this workshop, you should understand:

1. **Data Classes**: How they eliminate boilerplate and provide useful functionality
2. **Null Safety**: How `?`, `?.`, and `?:` operators prevent runtime crashes
3. **Function Defaults**: Making APIs more flexible and user-friendly
4. **String Templates**: Clean, readable string formatting
5. **Type Inference**: Letting Kotlin figure out types when obvious
6. **Extension Properties**: Adding functionality to existing classes

## Troubleshooting

**Common Issues:**

1. **"Cannot resolve reference"** - Make sure you've implemented all TODO items
2. **"Type mismatch"** - Check that you're handling nullable types correctly
3. **"Unresolved reference 'TODO'"** - Replace all `TODO(...)` calls with actual implementations

**Compilation Errors:**
- Remember that `val` properties cannot be reassigned
- Nullable types need safe navigation (`?.`) or explicit null checks
- Data class properties need to be declared in the primary constructor

## Next Steps

Once you've completed this workshop:
1. Compare your solution with the complete answer in `class/answer/lesson_1/`
2. Experiment with the code - try adding new properties or validation rules
3. Move on to Lesson 2 to learn about collections and functional programming

**Congratulations!** You've just built your first Kotlin application with proper null safety, data classes, and idiomatic Kotlin code. This foundation will serve you well throughout the rest of the curriculum!