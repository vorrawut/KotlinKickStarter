/**
 * Lesson 1 Complete Solution: User Validation Functions
 * 
 * This demonstrates:
 * - Null safety operators (?, ?., ?:)
 * - String operations and regex
 * - Range validation
 * - Boolean logic
 */

// Email validation using regex and null safety
fun isValidEmail(email: String?): Boolean {
    // Handle null case
    if (email == null) return false
    
    // Check if email is not blank
    if (email.isBlank()) return false
    
    // Validate email format using regex - accept dots in domain and user parts
    val emailRegex = Regex("[\\w.]+@[\\w.]+\\.[a-zA-Z]{2,}")
    return emailRegex.matches(email)
}

// Name validation demonstrating string operations and null safety
fun isValidName(name: String?): Boolean {
    // Use safe call operator and elvis operator
    return name?.isNotBlank() == true && name.length >= 2
}

// Age validation with range checks  
fun isValidAge(age: Int): Boolean {
    // Age should be between 0 and 150 (inclusive)
    return age in 0..150
}

// Phone validation (optional challenge)
// Accept formats like: "+1234567890", "123-456-7890", "(123) 456-7890"
// Return true for null (since phone is optional)
fun isValidPhone(phone: String?): Boolean {
    // If phone is null, return true (it's optional)
    return phone?.let { phoneNumber ->
        // Remove all non-digit characters and check length
        val digits = phoneNumber.replace(Regex("[^0-9]"), "")
        digits.length in 10..15
    } ?: true
}

// Comprehensive user validation function
fun validateUser(name: String?, email: String?, age: Int, phone: String? = null): Boolean {
    return isValidName(name) && 
           isValidEmail(email) && 
           isValidAge(age) && 
           isValidPhone(phone)
}