/**
 * Lesson 12 Complete Solution: Authentication DTOs
 * 
 * Complete set of data transfer objects for authentication operations
 * with comprehensive validation and security considerations
 */

package com.learning.security.dto

import com.learning.security.model.User
import jakarta.validation.constraints.*
import java.time.LocalDateTime

// Authentication Request DTOs

/**
 * User registration request
 */
data class RegisterRequest(
    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 50, message = "Username must be 3-50 characters")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9_]+$",
        message = "Username can only contain letters, numbers, and underscores"
    )
    val username: String,
    
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be valid")
    @field:Size(max = 100, message = "Email cannot exceed 100 characters")
    val email: String,
    
    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, max = 128, message = "Password must be 8-128 characters")
    val password: String,
    
    @field:NotBlank(message = "First name is required")
    @field:Size(min = 1, max = 50, message = "First name must be 1-50 characters")
    val firstName: String,
    
    @field:NotBlank(message = "Last name is required")
    @field:Size(min = 1, max = 50, message = "Last name must be 1-50 characters")
    val lastName: String
) {
    
    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        
        // Additional custom validation
        if (!isValidPassword(password)) {
            errors.add("Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
        }
        
        if (username.lowercase() in listOf("admin", "user", "test", "guest")) {
            errors.add("Username '$username' is not allowed")
        }
        
        if (containsPersonalInfo()) {
            errors.add("Password should not contain personal information")
        }
        
        return errors
    }
    
    private fun isValidPassword(pwd: String): Boolean {
        return pwd.any { it.isUpperCase() } &&
                pwd.any { it.isLowerCase() } &&
                pwd.any { it.isDigit() } &&
                pwd.any { !it.isLetterOrDigit() }
    }
    
    private fun containsPersonalInfo(): Boolean {
        val lowerPassword = password.lowercase()
        return listOf(username, firstName, lastName, email.substringBefore("@"))
            .any { info -> 
                info.length >= 3 && lowerPassword.contains(info.lowercase())
            }
    }
    
    fun getSanitizedCopy(): RegisterRequest {
        return copy(
            username = username.trim(),
            email = email.trim().lowercase(),
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            password = "***" // Never log actual password
        )
    }
}

/**
 * User login request
 */
data class LoginRequest(
    @field:NotBlank(message = "Username is required")
    @field:Size(min = 1, max = 100, message = "Username must be 1-100 characters")
    val username: String,
    
    @field:NotBlank(message = "Password is required")
    @field:Size(min = 1, max = 128, message = "Password must be 1-128 characters")
    val password: String,
    
    val rememberMe: Boolean = false
) {
    
    fun getSanitizedCopy(): LoginRequest {
        return copy(password = "***") // Never log actual password
    }
}

/**
 * Refresh token request
 */
data class RefreshTokenRequest(
    @field:NotBlank(message = "Refresh token is required")
    val refreshToken: String
) {
    
    fun getMaskedToken(): String {
        return if (refreshToken.length > 8) {
            "${refreshToken.take(4)}...${refreshToken.takeLast(4)}"
        } else {
            "****"
        }
    }
}

/**
 * Change password request
 */
data class ChangePasswordRequest(
    @field:NotBlank(message = "Current password is required")
    val currentPassword: String,
    
    @field:NotBlank(message = "New password is required")
    @field:Size(min = 8, max = 128, message = "New password must be 8-128 characters")
    val newPassword: String,
    
    @field:NotBlank(message = "Password confirmation is required")
    val confirmPassword: String
) {
    
    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        
        if (newPassword != confirmPassword) {
            errors.add("New password and confirmation do not match")
        }
        
        if (currentPassword == newPassword) {
            errors.add("New password must be different from current password")
        }
        
        if (!isValidPassword(newPassword)) {
            errors.add("New password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
        }
        
        return errors
    }
    
    private fun isValidPassword(pwd: String): Boolean {
        return pwd.any { it.isUpperCase() } &&
                pwd.any { it.isLowerCase() } &&
                pwd.any { it.isDigit() } &&
                pwd.any { !it.isLetterOrDigit() }
    }
    
    fun getSanitizedCopy(): ChangePasswordRequest {
        return copy(
            currentPassword = "***",
            newPassword = "***",
            confirmPassword = "***"
        )
    }
}

// Authentication Response DTOs

/**
 * Authentication response with JWT tokens and user info
 */
data class AuthResponse(
    val token: String,
    val refreshToken: String,
    val user: UserResponse,
    val tokenType: String = "Bearer",
    val expiresIn: Long? = null // Token expiration in seconds
) {
    
    companion object {
        fun create(
            token: String,
            refreshToken: String,
            user: User,
            expiresIn: Long? = null
        ): AuthResponse {
            return AuthResponse(
                token = token,
                refreshToken = refreshToken,
                user = UserResponse.from(user),
                expiresIn = expiresIn
            )
        }
    }
}

/**
 * User information response
 */
data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val roles: Set<String>,
    val isActive: Boolean,
    val isLocked: Boolean,
    val createdAt: LocalDateTime,
    val lastLoginAt: LocalDateTime?
) {
    
    companion object {
        fun from(user: User): UserResponse {
            return UserResponse(
                id = user.id ?: 0,
                username = user.username,
                email = user.email,
                firstName = user.firstName,
                lastName = user.lastName,
                fullName = user.getFullName(),
                roles = user.getRoleNames(),
                isActive = user.isActive,
                isLocked = user.isLocked,
                createdAt = user.createdAt,
                lastLoginAt = user.lastLoginAt
            )
        }
    }
    
    fun isAdmin(): Boolean = roles.contains("ADMIN")
    
    fun hasRole(roleName: String): Boolean = roles.contains(roleName)
    
    fun hasAnyRole(vararg roleNames: String): Boolean = roleNames.any { hasRole(it) }
}

/**
 * User profile update request
 */
data class UpdateUserRequest(
    @field:Size(min = 1, max = 50, message = "First name must be 1-50 characters")
    val firstName: String?,
    
    @field:Size(min = 1, max = 50, message = "Last name must be 1-50 characters")
    val lastName: String?,
    
    @field:Email(message = "Email must be valid")
    @field:Size(max = 100, message = "Email cannot exceed 100 characters")
    val email: String?
) {
    
    fun hasUpdates(): Boolean {
        return firstName != null || lastName != null || email != null
    }
    
    fun applyTo(user: User): User {
        return user.copy(
            firstName = firstName?.trim() ?: user.firstName,
            lastName = lastName?.trim() ?: user.lastName,
            email = email?.trim()?.lowercase() ?: user.email,
            updatedAt = LocalDateTime.now()
        )
    }
}

// Error Response DTOs

/**
 * Standard error response
 */
data class ErrorResponse(
    val error: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val path: String? = null,
    val details: Map<String, Any>? = null
) {
    
    companion object {
        fun unauthorized(message: String = "Access denied"): ErrorResponse {
            return ErrorResponse(
                error = "UNAUTHORIZED",
                message = message
            )
        }
        
        fun forbidden(message: String = "Forbidden"): ErrorResponse {
            return ErrorResponse(
                error = "FORBIDDEN",
                message = message
            )
        }
        
        fun badRequest(message: String, details: Map<String, Any>? = null): ErrorResponse {
            return ErrorResponse(
                error = "BAD_REQUEST",
                message = message,
                details = details
            )
        }
        
        fun validationError(errors: Map<String, String>): ErrorResponse {
            return ErrorResponse(
                error = "VALIDATION_ERROR",
                message = "Request validation failed",
                details = errors
            )
        }
        
        fun internalError(message: String = "Internal server error"): ErrorResponse {
            return ErrorResponse(
                error = "INTERNAL_ERROR",
                message = message
            )
        }
    }
}

/**
 * Validation error details
 */
data class ValidationErrorResponse(
    val error: String = "VALIDATION_ERROR",
    val message: String = "Request validation failed",
    val timestamp: Long = System.currentTimeMillis(),
    val path: String? = null,
    val fieldErrors: Map<String, String> = emptyMap(),
    val globalErrors: List<String> = emptyList()
)

// Success Response DTOs

/**
 * Simple success response
 */
data class SuccessResponse(
    val success: Boolean = true,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val data: Any? = null
) {
    
    companion object {
        fun ok(message: String, data: Any? = null): SuccessResponse {
            return SuccessResponse(message = message, data = data)
        }
        
        fun passwordChanged(): SuccessResponse {
            return ok("Password changed successfully")
        }
        
        fun loggedOut(): SuccessResponse {
            return ok("Logged out successfully")
        }
        
        fun accountLocked(): SuccessResponse {
            return ok("Account has been locked")
        }
        
        fun accountUnlocked(): SuccessResponse {
            return ok("Account has been unlocked")
        }
    }
}

// Security Audit DTOs

/**
 * Login attempt information
 */
data class LoginAttemptInfo(
    val username: String,
    val success: Boolean,
    val timestamp: LocalDateTime,
    val ipAddress: String?,
    val userAgent: String?,
    val failureReason: String? = null
)

/**
 * Token usage information
 */
data class TokenUsageInfo(
    val tokenId: String,
    val userId: Long,
    val username: String,
    val action: String, // CREATED, USED, REFRESHED, REVOKED
    val timestamp: LocalDateTime,
    val ipAddress: String?,
    val userAgent: String?
)