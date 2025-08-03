/**
 * Lesson 6 Complete Solution: Custom Validation Annotations and Validators
 */

package com.learning.validation.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import java.time.LocalDate
import java.time.Period
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [AgeValidator::class])
annotation class ValidAge(
    val message: String = "Invalid age",
    val min: Int = 0,
    val max: Int = 150,
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class AgeValidator : ConstraintValidator<ValidAge, LocalDate?> {
    
    private var min: Int = 0
    private var max: Int = 150
    
    override fun initialize(constraintAnnotation: ValidAge) {
        min = constraintAnnotation.min
        max = constraintAnnotation.max
    }
    
    override fun isValid(value: LocalDate?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return true // Let @NotNull handle null checks
        
        val age = Period.between(value, LocalDate.now()).years
        return age in min..max
    }
}

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [UsernameValidator::class])
annotation class ValidUsername(
    val message: String = "Invalid username format",
    val allowSpecialChars: Boolean = false,
    val minLength: Int = 3,
    val maxLength: Int = 20,
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class UsernameValidator : ConstraintValidator<ValidUsername, String?> {
    
    private var allowSpecialChars: Boolean = false
    private var minLength: Int = 3
    private var maxLength: Int = 20
    
    override fun initialize(constraintAnnotation: ValidUsername) {
        allowSpecialChars = constraintAnnotation.allowSpecialChars
        minLength = constraintAnnotation.minLength
        maxLength = constraintAnnotation.maxLength
    }
    
    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return true
        
        return value.length in minLength..maxLength &&
               hasValidCharacters(value, allowSpecialChars) &&
               !containsProhibitedWords(value)
    }
    
    private fun containsProhibitedWords(username: String): Boolean {
        val prohibitedWords = listOf("admin", "root", "system", "test", "null", "undefined", "password")
        return prohibitedWords.any { username.lowercase().contains(it) }
    }
    
    private fun hasValidCharacters(username: String, allowSpecialChars: Boolean): Boolean {
        val pattern = if (allowSpecialChars) {
            "^[a-zA-Z0-9_.-]+$"
        } else {
            "^[a-zA-Z0-9_]+$"
        }
        return username.matches(Regex(pattern))
    }
}

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [PhoneNumberValidator::class])
annotation class ValidPhoneNumber(
    val message: String = "Invalid phone number format",
    val countryCode: String = "",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class PhoneNumberValidator : ConstraintValidator<ValidPhoneNumber, String?> {
    
    private var countryCode: String = ""
    
    override fun initialize(constraintAnnotation: ValidPhoneNumber) {
        countryCode = constraintAnnotation.countryCode
    }
    
    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return true
        
        return isValidFormat(value, countryCode)
    }
    
    private fun isValidFormat(phoneNumber: String, countryCode: String): Boolean {
        return when (countryCode.uppercase()) {
            "US" -> phoneNumber.matches(Regex("^\\+?1?[2-9]\\d{2}[2-9]\\d{2}\\d{4}$"))
            "UK" -> phoneNumber.matches(Regex("^\\+?44[1-9]\\d{8,9}$"))
            "" -> phoneNumber.matches(Regex("^\\+?[1-9]\\d{1,14}$")) // International format
            else -> phoneNumber.matches(Regex("^\\+?[1-9]\\d{1,14}$"))
        }
    }
}

object ValidationUtils {
    
    fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")
        return email.matches(emailRegex) && email.length <= 254
    }
    
    fun isValidPostalCode(postalCode: String, countryCode: String): Boolean {
        return when (countryCode.uppercase()) {
            "US" -> postalCode.matches(Regex("^\\d{5}(-\\d{4})?$"))
            "CA" -> postalCode.matches(Regex("^[A-Za-z]\\d[A-Za-z] \\d[A-Za-z]\\d$"))
            "GB" -> postalCode.matches(Regex("^[A-Z]{1,2}\\d[A-Z\\d]? \\d[A-Z]{2}$"))
            "DE" -> postalCode.matches(Regex("^\\d{5}$"))
            else -> postalCode.isNotBlank()
        }
    }
    
    fun containsSqlInjectionPatterns(input: String): Boolean {
        val sqlPatterns = listOf(
            "(?i).*\\b(SELECT|INSERT|UPDATE|DELETE|DROP|CREATE|ALTER)\\b.*",
            "(?i).*\\b(UNION|OR|AND)\\s+\\d+\\s*=\\s*\\d+.*",
            "(?i).*['\"](\\s|\\w)*['\"](\\s)*(=|LIKE|IN).*",
            "(?i).*(--|#|/\\*|\\*/).*"
        )
        return sqlPatterns.any { input.contains(Regex(it)) }
    }
    
    fun sanitizeInput(input: String): String {
        return input
            .replace(Regex("[<>\"'&]"), "")
            .replace(Regex("(--|#|/\\*|\\*/)"), "")
            .trim()
            .take(1000)
    }
    
    fun isValidDateRange(startDate: Any?, endDate: Any?): Boolean {
        return try {
            when {
                startDate is LocalDate && endDate is LocalDate -> startDate.isBefore(endDate)
                else -> true
            }
        } catch (e: Exception) {
            false
        }
    }
    
    fun isWithinBusinessHours(dateTime: Any?): Boolean {
        // Simple business hours check: Monday-Friday, 9 AM - 5 PM
        return try {
            if (dateTime is java.time.LocalDateTime) {
                val dayOfWeek = dateTime.dayOfWeek
                val hour = dateTime.hour
                dayOfWeek.value in 1..5 && hour in 9..17
            } else {
                true
            }
        } catch (e: Exception) {
            false
        }
    }
}