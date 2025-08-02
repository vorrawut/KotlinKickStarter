/**
 * Lesson 6 Workshop: User Domain Model with Validation
 * 
 * TODO: Complete this domain model class
 * This demonstrates:
 * - Domain model validation vs DTO validation
 * - Business rule validation
 * - Complex validation scenarios
 */

package com.learning.validation.model

import java.time.LocalDate
import java.time.LocalDateTime

data class User(
    val id: String,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: LocalDate,
    val phoneNumber: String?,
    val address: Address?,
    val status: UserStatus,
    val roles: Set<UserRole>,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastLoginAt: LocalDateTime? = null,
    val isEmailVerified: Boolean = false,
    val isPhoneVerified: Boolean = false
) {
    
    // TODO: Add computed properties with validation logic
    val age: Int
        get() = TODO("Calculate age from date of birth")
    
    val isActive: Boolean
        get() = TODO("Check if user status is ACTIVE")
    
    val fullName: String
        get() = TODO("Combine first and last name")
    
    // TODO: Add business rule validation methods
    fun isEligibleForService(): Boolean {
        return TODO("Check if user meets service eligibility criteria (age, verification, etc.)")
    }
    
    fun canPerformAction(action: String): Boolean {
        return TODO("Check if user can perform specific action based on roles and status")
    }
    
    fun hasRole(role: UserRole): Boolean {
        return TODO("Check if user has specific role")
    }
    
    fun isVerified(): Boolean {
        return TODO("Check if user is fully verified (email AND phone if phone provided)")
    }
    
    fun daysUntilBirthday(): Int {
        return TODO("Calculate days until next birthday")
    }
}

data class Address(
    val street: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String
) {
    // TODO: Add address validation methods
    fun isValid(): Boolean {
        return TODO("Validate address completeness and format")
    }
    
    fun getFormattedAddress(): String {
        return TODO("Return formatted address string")
    }
}

enum class UserStatus(val displayName: String) {
    PENDING("Pending Activation"),
    ACTIVE("Active"),
    SUSPENDED("Suspended"),
    DEACTIVATED("Deactivated"),
    BANNED("Banned")
}

enum class UserRole(val displayName: String, val permissions: Set<String>) {
    USER("Regular User", setOf("read:profile", "update:profile")),
    MODERATOR("Moderator", setOf("read:profile", "update:profile", "moderate:content")),
    ADMIN("Administrator", setOf("read:all", "update:all", "delete:all", "manage:users")),
    SUPER_ADMIN("Super Administrator", setOf("*"))
}