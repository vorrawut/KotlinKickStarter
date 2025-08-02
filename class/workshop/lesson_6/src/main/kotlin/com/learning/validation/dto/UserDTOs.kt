/**
 * Lesson 6 Workshop: User DTOs with Advanced Validation
 * 
 * TODO: Complete these DTO classes with comprehensive validation
 * This demonstrates:
 * - Bean Validation annotations (@Valid, @NotNull, @Size, etc.)
 * - Custom validation annotations
 * - Cross-field validation
 * - Group validation
 */

package com.learning.validation.dto

import com.learning.validation.model.UserRole
import com.learning.validation.model.UserStatus
import java.time.LocalDate

// TODO: Add validation annotations for user creation
data class CreateUserRequest(
    // TODO: Add @field:NotBlank, @field:Size, @field:Pattern for username validation
    val username: String,
    
    // TODO: Add @field:Email validation
    val email: String,
    
    // TODO: Add @field:NotBlank, @field:Size for names
    val firstName: String,
    val lastName: String,
    
    // TODO: Add @field:Past and age validation for date of birth
    val dateOfBirth: LocalDate,
    
    // TODO: Add optional phone validation with pattern
    val phoneNumber: String?,
    
    // TODO: Add @field:Valid for nested object validation
    val address: CreateAddressRequest?,
    
    // TODO: Add @field:NotEmpty for roles collection
    val roles: Set<UserRole>
) {
    // TODO: Add custom validation method for business rules
    fun isValidForRegistration(): Boolean {
        return TODO("Validate business rules for user registration")
    }
}

// TODO: Add validation annotations for user updates
data class UpdateUserRequest(
    // TODO: Add optional validation (fields can be null for partial updates)
    val firstName: String?,
    val lastName: String?,
    val phoneNumber: String?,
    val address: UpdateAddressRequest?
) {
    // TODO: Add validation to ensure at least one field is provided
    fun hasValidUpdates(): Boolean {
        return TODO("Ensure at least one field is provided for update")
    }
}

// TODO: Add validation for address creation
data class CreateAddressRequest(
    // TODO: Add @field:NotBlank, @field:Size for address fields
    val street: String,
    val city: String,
    val state: String,
    
    // TODO: Add postal code pattern validation
    val zipCode: String,
    
    // TODO: Add country code validation
    val country: String
)

// TODO: Add validation for address updates
data class UpdateAddressRequest(
    val street: String?,
    val city: String?,
    val state: String?,
    val zipCode: String?,
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

// TODO: Add validation for user status changes
data class ChangeUserStatusRequest(
    // TODO: Add @field:NotNull for status
    val status: UserStatus,
    
    // TODO: Add optional reason with size validation
    val reason: String?
) {
    // TODO: Add validation for status transition rules
    fun isValidStatusTransition(currentStatus: UserStatus): Boolean {
        return TODO("Validate if status transition is allowed")
    }
}

// TODO: Add validation for user search/filter requests
data class UserSearchRequest(
    // TODO: Add optional validation for search parameters
    val username: String?,
    val email: String?,
    val status: UserStatus?,
    val roles: Set<UserRole>?,
    val minAge: Int?,
    val maxAge: Int?,
    val city: String?,
    val state: String?,
    val country: String?,
    
    // TODO: Add pagination validation
    val page: Int = 0,
    val size: Int = 20,
    val sortBy: String = "createdAt",
    val sortDirection: String = "DESC"
) {
    // TODO: Add validation for search parameters
    fun isValidSearchCriteria(): Boolean {
        return TODO("Validate search criteria and pagination parameters")
    }
}