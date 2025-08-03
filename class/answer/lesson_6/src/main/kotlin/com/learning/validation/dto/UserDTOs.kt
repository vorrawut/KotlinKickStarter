/**
 * Lesson 6 Complete Solution: User DTOs with Comprehensive Validation
 */

package com.learning.validation.dto

import com.learning.validation.model.UserRole
import com.learning.validation.model.UserStatus
import jakarta.validation.Valid
import jakarta.validation.constraints.*
import java.time.LocalDate
import java.time.Period

data class CreateUserRequest(
    @field:NotBlank(message = "Username cannot be blank")
    @field:Size(min = 3, max = 20, message = "Username must be 3-20 characters")
    @field:Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscore")
    val username: String,
    
    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email is required")
    val email: String,
    
    @field:NotBlank(message = "First name is required")
    @field:Size(min = 1, max = 50, message = "First name must be 1-50 characters")
    val firstName: String,
    
    @field:NotBlank(message = "Last name is required")
    @field:Size(min = 1, max = 50, message = "Last name must be 1-50 characters")
    val lastName: String,
    
    @field:Past(message = "Date of birth must be in the past")
    @field:NotNull(message = "Date of birth is required")
    val dateOfBirth: LocalDate,
    
    @field:Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    val phoneNumber: String?,
    
    @field:Valid
    val address: CreateAddressRequest?,
    
    @field:NotEmpty(message = "At least one role must be specified")
    val roles: Set<UserRole>
) {
    fun isValidForRegistration(): Boolean {
        val age = Period.between(dateOfBirth, LocalDate.now()).years
        return age >= 18 && roles.isNotEmpty() && !roles.contains(UserRole.SUPER_ADMIN)
    }
}

data class UpdateUserRequest(
    @field:Size(min = 1, max = 50, message = "First name must be 1-50 characters")
    val firstName: String?,
    
    @field:Size(min = 1, max = 50, message = "Last name must be 1-50 characters")
    val lastName: String?,
    
    @field:Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    val phoneNumber: String?,
    
    @field:Valid
    val address: UpdateAddressRequest?
) {
    fun hasValidUpdates(): Boolean {
        return firstName != null || lastName != null || phoneNumber != null || address != null
    }
}

data class CreateAddressRequest(
    @field:NotBlank(message = "Street address is required")
    @field:Size(max = 100, message = "Street address too long")
    val street: String,
    
    @field:NotBlank(message = "City is required")
    @field:Size(max = 50, message = "City name too long")
    val city: String,
    
    @field:NotBlank(message = "State is required")
    @field:Size(max = 50, message = "State name too long")
    val state: String,
    
    @field:NotBlank(message = "ZIP code is required")
    @field:Pattern(regexp = "^\\d{5}(-\\d{4})?$", message = "Invalid ZIP code format")
    val zipCode: String,
    
    @field:NotBlank(message = "Country is required")
    @field:Size(min = 2, max = 2, message = "Country must be 2-letter code")
    val country: String
)

data class UpdateAddressRequest(
    @field:Size(max = 100, message = "Street address too long")
    val street: String?,
    
    @field:Size(max = 50, message = "City name too long")
    val city: String?,
    
    @field:Size(max = 50, message = "State name too long")
    val state: String?,
    
    @field:Pattern(regexp = "^\\d{5}(-\\d{4})?$", message = "Invalid ZIP code format")
    val zipCode: String?,
    
    @field:Size(min = 2, max = 2, message = "Country must be 2-letter code")
    val country: String?
)

// Response DTOs (no validation needed, only for output)
data class UserResponse(
    val id: String,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val age: Int,
    val phoneNumber: String?,
    val address: AddressResponse?,
    val status: UserStatus,
    val roles: Set<UserRole>,
    val isEmailVerified: Boolean,
    val isPhoneVerified: Boolean,
    val createdAt: String
)

data class AddressResponse(
    val street: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String,
    val formattedAddress: String
)

data class ChangeUserStatusRequest(
    @field:NotNull(message = "Status is required")
    val status: UserStatus,
    
    @field:Size(max = 500, message = "Reason too long")
    val reason: String?
) {
    fun isValidStatusTransition(currentStatus: UserStatus): Boolean {
        return when (currentStatus) {
            UserStatus.PENDING -> status in setOf(UserStatus.ACTIVE, UserStatus.SUSPENDED)
            UserStatus.ACTIVE -> status in setOf(UserStatus.SUSPENDED, UserStatus.DEACTIVATED)
            UserStatus.SUSPENDED -> status in setOf(UserStatus.ACTIVE, UserStatus.BANNED)
            UserStatus.DEACTIVATED -> status == UserStatus.ACTIVE
            UserStatus.BANNED -> false // Cannot transition from banned
        }
    }
}

data class UserSearchRequest(
    val username: String?,
    val email: String?,
    val status: UserStatus?,
    val roles: Set<UserRole>?,
    
    @field:Min(value = 0, message = "Minimum age cannot be negative")
    val minAge: Int?,
    
    @field:Max(value = 150, message = "Maximum age too high")
    val maxAge: Int?,
    
    val city: String?,
    val state: String?,
    val country: String?,
    
    @field:Min(value = 0, message = "Page cannot be negative")
    val page: Int = 0,
    
    @field:Min(value = 1, message = "Page size must be at least 1")
    @field:Max(value = 100, message = "Page size too large")
    val size: Int = 20,
    
    val sortBy: String = "createdAt",
    val sortDirection: String = "DESC"
) {
    fun isValidSearchCriteria(): Boolean {
        return (minAge == null || minAge >= 0) &&
               (maxAge == null || maxAge <= 150) &&
               (minAge == null || maxAge == null || minAge <= maxAge) &&
               page >= 0 && size in 1..100
    }
}