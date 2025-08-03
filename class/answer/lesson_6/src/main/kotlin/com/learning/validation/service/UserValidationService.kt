/**
 * Lesson 6 Complete Solution: User Validation Service
 */

package com.learning.validation.service

import com.learning.validation.dto.*
import com.learning.validation.exception.*
import com.learning.validation.model.*
import com.learning.validation.validator.ValidationUtils
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Service
class UserValidationService {
    
    private val users = ConcurrentHashMap<String, User>()
    private val usersByUsername = ConcurrentHashMap<String, String>()
    private val usersByEmail = ConcurrentHashMap<String, String>()
    private val idCounter = AtomicLong(1)
    
    init {
        // Initialize with sample data
        createSampleUsers()
    }
    
    fun createUser(request: CreateUserRequest): UserResponse {
        // Business rule validation
        validateCreateUserRequest(request)
        
        // Check for duplicates
        checkForDuplicateUser(request.username, request.email)
        
        // Create user
        val userId = "user_${idCounter.getAndIncrement()}"
        val user = User(
            id = userId,
            username = request.username,
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            dateOfBirth = request.dateOfBirth,
            phoneNumber = request.phoneNumber,
            address = request.address?.let { createAddress(it) },
            status = UserStatus.PENDING,
            roles = request.roles,
            isEmailVerified = false,
            isPhoneVerified = false
        )
        
        // Store user
        users[userId] = user
        usersByUsername[request.username.lowercase()] = userId
        usersByEmail[request.email.lowercase()] = userId
        
        return mapToUserResponse(user)
    }
    
    fun getUserById(id: String): UserResponse {
        val user = users[id] ?: throw ResourceNotFoundException("User", id)
        return mapToUserResponse(user)
    }
    
    fun getUserByUsername(username: String): UserResponse {
        val userId = usersByUsername[username.lowercase()] 
            ?: throw ResourceNotFoundException("User", "username=$username")
        val user = users[userId]!!
        return mapToUserResponse(user)
    }
    
    fun getUserByEmail(email: String): UserResponse {
        val userId = usersByEmail[email.lowercase()] 
            ?: throw ResourceNotFoundException("User", "email=$email")
        val user = users[userId]!!
        return mapToUserResponse(user)
    }
    
    fun updateUser(id: String, request: UpdateUserRequest): UserResponse {
        val existingUser = users[id] ?: throw ResourceNotFoundException("User", id)
        
        validateUpdateUserRequest(request, existingUser)
        
        val updatedUser = existingUser.copy(
            firstName = request.firstName ?: existingUser.firstName,
            lastName = request.lastName ?: existingUser.lastName,
            phoneNumber = request.phoneNumber ?: existingUser.phoneNumber,
            address = request.address?.let { updateAddress(existingUser.address, it) } ?: existingUser.address
        )
        
        users[id] = updatedUser
        return mapToUserResponse(updatedUser)
    }
    
    fun changeUserStatus(id: String, request: ChangeUserStatusRequest): UserResponse {
        val existingUser = users[id] ?: throw ResourceNotFoundException("User", id)
        
        if (!request.isValidStatusTransition(existingUser.status)) {
            throw InvalidStateTransitionException(
                entity = "User",
                currentState = existingUser.status.name,
                targetState = request.status.name
            )
        }
        
        val updatedUser = existingUser.copy(status = request.status)
        users[id] = updatedUser
        
        return mapToUserResponse(updatedUser)
    }
    
    fun deleteUser(id: String): Boolean {
        val user = users[id] ?: throw ResourceNotFoundException("User", id)
        
        // Business rule: Cannot delete admin users
        if (user.hasRole(UserRole.ADMIN) || user.hasRole(UserRole.SUPER_ADMIN)) {
            throw OperationNotAllowedException(
                operation = "DELETE_USER",
                reason = "Cannot delete admin users",
                resourceType = "User",
                resourceId = id
            )
        }
        
        users.remove(id)
        usersByUsername.remove(user.username.lowercase())
        usersByEmail.remove(user.email.lowercase())
        
        return true
    }
    
    fun searchUsers(request: UserSearchRequest): List<UserResponse> {
        if (!request.isValidSearchCriteria()) {
            throw InvalidFieldValueException(
                field = "searchCriteria",
                value = request,
                expectedFormat = "Valid age range and pagination parameters"
            )
        }
        
        return users.values
            .filter { user -> matchesSearchCriteria(user, request) }
            .sortedWith(createComparator(request.sortBy, request.sortDirection))
            .drop(request.page * request.size)
            .take(request.size)
            .map { mapToUserResponse(it) }
    }
    
    fun validateUserOperationPermission(userId: String, operation: String): Boolean {
        val user = users[userId] ?: return false
        return user.canPerformAction(operation)
    }
    
    fun getUsersWithStatus(status: UserStatus): List<UserResponse> {
        return users.values
            .filter { it.status == status }
            .map { mapToUserResponse(it) }
    }
    
    private fun validateCreateUserRequest(request: CreateUserRequest) {
        // Age validation
        val age = Period.between(request.dateOfBirth, LocalDate.now()).years
        if (age < 18) {
            throw BusinessRuleViolationException(
                message = "User must be at least 18 years old",
                ruleName = "MINIMUM_AGE",
                field = "dateOfBirth",
                value = request.dateOfBirth
            )
        }
        
        // Role validation
        if (request.roles.contains(UserRole.SUPER_ADMIN)) {
            throw BusinessRuleViolationException(
                message = "Cannot create user with SUPER_ADMIN role through this endpoint",
                ruleName = "RESTRICTED_ROLE",
                field = "roles",
                value = request.roles
            )
        }
        
        // Email format validation
        if (!ValidationUtils.isValidEmail(request.email)) {
            throw InvalidFieldValueException(
                field = "email",
                value = request.email,
                expectedFormat = "valid email address"
            )
        }
        
        // Phone number validation if provided
        request.phoneNumber?.let { phoneNumber ->
            if (ValidationUtils.containsSqlInjectionPatterns(phoneNumber)) {
                throw BusinessRuleViolationException(
                    message = "Phone number contains invalid characters",
                    ruleName = "SECURITY_VALIDATION",
                    field = "phoneNumber",
                    value = phoneNumber
                )
            }
        }
    }
    
    private fun validateUpdateUserRequest(request: UpdateUserRequest, existingUser: User) {
        if (!request.hasValidUpdates()) {
            throw IllegalArgumentException("At least one field must be provided for update")
        }
        
        // Validate phone number if provided
        request.phoneNumber?.let { phoneNumber ->
            if (ValidationUtils.containsSqlInjectionPatterns(phoneNumber)) {
                throw BusinessRuleViolationException(
                    message = "Phone number contains invalid characters",
                    ruleName = "SECURITY_VALIDATION",
                    field = "phoneNumber",
                    value = phoneNumber
                )
            }
        }
    }
    
    private fun checkForDuplicateUser(username: String, email: String) {
        if (usersByUsername.containsKey(username.lowercase())) {
            throw DuplicateResourceException("User", "username", username)
        }
        
        if (usersByEmail.containsKey(email.lowercase())) {
            throw DuplicateResourceException("User", "email", email)
        }
    }
    
    private fun createAddress(request: CreateAddressRequest): Address {
        return Address(
            street = ValidationUtils.sanitizeInput(request.street),
            city = ValidationUtils.sanitizeInput(request.city),
            state = ValidationUtils.sanitizeInput(request.state),
            zipCode = request.zipCode,
            country = request.country.uppercase()
        )
    }
    
    private fun updateAddress(existingAddress: Address?, request: UpdateAddressRequest): Address? {
        if (existingAddress == null && request.hasNoUpdates()) return null
        
        val base = existingAddress ?: Address("", "", "", "", "")
        
        return Address(
            street = request.street?.let { ValidationUtils.sanitizeInput(it) } ?: base.street,
            city = request.city?.let { ValidationUtils.sanitizeInput(it) } ?: base.city,
            state = request.state?.let { ValidationUtils.sanitizeInput(it) } ?: base.state,
            zipCode = request.zipCode ?: base.zipCode,
            country = request.country?.uppercase() ?: base.country
        )
    }
    
    private fun UpdateAddressRequest.hasNoUpdates(): Boolean {
        return street == null && city == null && state == null && zipCode == null && country == null
    }
    
    private fun matchesSearchCriteria(user: User, request: UserSearchRequest): Boolean {
        return (request.username == null || user.username.contains(request.username, ignoreCase = true)) &&
               (request.email == null || user.email.contains(request.email, ignoreCase = true)) &&
               (request.status == null || user.status == request.status) &&
               (request.roles == null || request.roles.any { user.hasRole(it) }) &&
               (request.minAge == null || user.age >= request.minAge) &&
               (request.maxAge == null || user.age <= request.maxAge) &&
               (request.city == null || user.address?.city?.contains(request.city, ignoreCase = true) == true) &&
               (request.state == null || user.address?.state?.contains(request.state, ignoreCase = true) == true) &&
               (request.country == null || user.address?.country?.equals(request.country, ignoreCase = true) == true)
    }
    
    private fun createComparator(sortBy: String, sortDirection: String): Comparator<User> {
        val baseComparator = when (sortBy) {
            "username" -> compareBy<User> { it.username }
            "email" -> compareBy { it.email }
            "firstName" -> compareBy { it.firstName }
            "lastName" -> compareBy { it.lastName }
            "age" -> compareBy { it.age }
            "status" -> compareBy { it.status.name }
            else -> compareBy { it.createdAt }
        }
        
        return if (sortDirection.uppercase() == "DESC") {
            baseComparator.reversed()
        } else {
            baseComparator
        }
    }
    
    private fun mapToUserResponse(user: User): UserResponse {
        return UserResponse(
            id = user.id,
            username = user.username,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            fullName = user.fullName,
            age = user.age,
            phoneNumber = user.phoneNumber,
            address = user.address?.let { 
                AddressResponse(it.street, it.city, it.state, it.zipCode, it.country, it.getFormattedAddress()) 
            },
            status = user.status,
            roles = user.roles,
            isEmailVerified = user.isEmailVerified,
            isPhoneVerified = user.isPhoneVerified,
            createdAt = user.createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )
    }
    
    private fun createSampleUsers() {
        val sampleUsers = listOf(
            User(
                id = "user_1001",
                username = "johndoe",
                email = "john.doe@example.com",
                firstName = "John",
                lastName = "Doe",
                dateOfBirth = LocalDate.of(1990, 5, 15),
                phoneNumber = "+1234567890",
                address = Address("123 Main St", "Springfield", "IL", "62701", "US"),
                status = UserStatus.ACTIVE,
                roles = setOf(UserRole.USER),
                isEmailVerified = true,
                isPhoneVerified = true
            ),
            User(
                id = "user_1002",
                username = "janedoe",
                email = "jane.doe@example.com",
                firstName = "Jane",
                lastName = "Doe",
                dateOfBirth = LocalDate.of(1985, 8, 22),
                phoneNumber = "+1234567891",
                address = Address("456 Oak Ave", "Springfield", "IL", "62702", "US"),
                status = UserStatus.ACTIVE,
                roles = setOf(UserRole.USER, UserRole.MODERATOR),
                isEmailVerified = true,
                isPhoneVerified = true
            )
        )
        
        sampleUsers.forEach { user ->
            users[user.id] = user
            usersByUsername[user.username.lowercase()] = user.id
            usersByEmail[user.email.lowercase()] = user.id
            idCounter.set(maxOf(idCounter.get(), user.id.removePrefix("user_").toLongOrNull() ?: 0L) + 1)
        }
    }
}