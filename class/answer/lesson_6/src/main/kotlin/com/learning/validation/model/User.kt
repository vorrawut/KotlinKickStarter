/**
 * Lesson 6 Complete Solution: User Domain Model with Business Logic
 */

package com.learning.validation.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period

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
    
    val age: Int
        get() = Period.between(dateOfBirth, LocalDate.now()).years
    
    val isActive: Boolean
        get() = status == UserStatus.ACTIVE
    
    val fullName: String
        get() = "$firstName $lastName"
    
    fun isEligibleForService(): Boolean {
        return age >= 18 && isVerified() && isActive
    }
    
    fun canPerformAction(action: String): Boolean {
        return when (action) {
            "read:profile" -> isActive
            "update:profile" -> isActive && isVerified()
            "delete:account" -> isActive && isVerified() && !hasRole(UserRole.ADMIN)
            "admin:manage" -> hasRole(UserRole.ADMIN) || hasRole(UserRole.SUPER_ADMIN)
            "moderate:content" -> hasRole(UserRole.MODERATOR) || hasRole(UserRole.ADMIN) || hasRole(UserRole.SUPER_ADMIN)
            else -> false
        }
    }
    
    fun hasRole(role: UserRole): Boolean {
        return roles.contains(role)
    }
    
    fun isVerified(): Boolean {
        return isEmailVerified && (phoneNumber == null || isPhoneVerified)
    }
    
    fun daysUntilBirthday(): Int {
        val today = LocalDate.now()
        val thisYearBirthday = dateOfBirth.withYear(today.year)
        val nextBirthday = if (thisYearBirthday.isBefore(today) || thisYearBirthday.isEqual(today)) {
            thisYearBirthday.plusYears(1)
        } else {
            thisYearBirthday
        }
        return Period.between(today, nextBirthday).days
    }
}

data class Address(
    val street: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String
) {
    fun isValid(): Boolean {
        return street.isNotBlank() && 
               city.isNotBlank() && 
               state.isNotBlank() && 
               zipCode.isNotBlank() && 
               country.isNotBlank()
    }
    
    fun getFormattedAddress(): String {
        return "$street, $city, $state $zipCode, $country"
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