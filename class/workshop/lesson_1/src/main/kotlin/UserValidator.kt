/**
 * Lesson 1 Workshop: User Validation Functions
 * 
 * TODO: Complete these validation functions to demonstrate:
 * - Null safety operators (?, ?., ?:)
 * - String operations and regex
 * - Range validation
 * - Boolean logic
 */

// TODO: Email validation using regex and null safety
fun isValidEmail(email: String?): Boolean {
    // TODO: Step 1 - Handle null case
    // If email is null, return false
    // Hint: Use ?: operator or explicit null check
    
    // TODO: Step 2 - Check if email is not blank
    // Hint: Use isBlank() function
    
    // TODO: Step 3 - Validate email format using regex
    // Pattern should match: one or more word chars, @, one or more word chars, ., one or more word chars
    // Hint: Use Regex class and matches() function
    // Example: Regex("\\w+@\\w+\\.\\w+").matches(email)
    
    return TODO("Implement email validation with null safety")
}

// TODO: Name validation demonstrating string operations and null safety
fun isValidName(name: String?): Boolean {
    // TODO: Use safe call operator and elvis operator
    // Check if name is not null, not blank, and at least 2 characters long
    // Hint: name?.isNotBlank() == true && name.length >= 2
    // Or: name?.takeIf { it.isNotBlank() && it.length >= 2 } != null
    
    return TODO("Implement name validation")
}

// TODO: Age validation with range checks  
fun isValidAge(age: Int): Boolean {
    // TODO: Age should be between 0 and 150 (inclusive)
    // Hint: Use 'in' operator or range comparisons
    
    return TODO("Implement age validation")
}

// TODO: Phone validation (optional challenge)
// Accept formats like: "+1234567890", "123-456-7890", "(123) 456-7890"
// Return true for null (since phone is optional)
fun isValidPhone(phone: String?): Boolean {
    // TODO: If phone is null, return true (it's optional)
    // TODO: If phone is not null, validate format
    // Hint: phone?.let { /* validation logic */ } ?: true
    
    return TODO("Implement phone validation")
}

// TODO: Comprehensive user validation function
fun validateUser(name: String?, email: String?, age: Int, phone: String? = null): Boolean {
    // TODO: Use all the validation functions above
    // All validations must pass for user to be valid
    // Hint: Use && operator to combine all validations
    
    return TODO("Combine all validations")
}