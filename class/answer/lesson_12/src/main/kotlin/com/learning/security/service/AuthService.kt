/**
 * Lesson 12 Complete Solution: Authentication Service
 * 
 * Complete authentication service handling user registration, login,
 * token refresh, logout, and comprehensive security operations
 */

package com.learning.security.service

import com.learning.security.dto.*
import com.learning.security.model.RefreshToken
import com.learning.security.model.Role
import com.learning.security.model.User
import com.learning.security.repository.RefreshTokenRepository
import com.learning.security.repository.RoleRepository
import com.learning.security.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class AuthService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService,
    private val customUserDetailsService: CustomUserDetailsService
) {
    
    private val logger = LoggerFactory.getLogger(AuthService::class.java)
    
    @Value("\${jwt.refresh.expiration}")
    private var refreshExpiration: Long = 604800000 // 7 days default
    
    /**
     * Register a new user
     */
    fun register(registerRequest: RegisterRequest): AuthResponse {
        logger.info("Registering new user: ${registerRequest.username}")
        
        try {
            // Validate registration data
            validateRegistrationData(registerRequest)
            
            // Check if user already exists
            if (customUserDetailsService.existsByUsernameOrEmail(registerRequest.username, registerRequest.email)) {
                throw UserAlreadyExistsException("User with username '${registerRequest.username}' or email '${registerRequest.email}' already exists")
            }
            
            // Get default role
            val defaultRole = getDefaultRole()
            
            // Create new user
            val user = User(
                username = registerRequest.username.trim(),
                email = registerRequest.email.trim().lowercase(),
                password = passwordEncoder.encode(registerRequest.password),
                firstName = registerRequest.firstName.trim(),
                lastName = registerRequest.lastName.trim(),
                roles = setOf(defaultRole),
                isActive = true,
                isEnabled = true
            )
            
            val savedUser = userRepository.save(user)
            logger.info("User registered successfully: ${savedUser.username} (ID: ${savedUser.id})")
            
            // Generate tokens
            val userDetails = CustomUserDetails(savedUser)
            val jwtToken = jwtService.generateToken(userDetails)
            val refreshToken = createRefreshToken(savedUser)
            
            return AuthResponse(
                token = jwtToken,
                refreshToken = refreshToken.token,
                user = UserResponse.from(savedUser)
            )
            
        } catch (e: UserAlreadyExistsException) {
            logger.warn("Registration failed - user already exists: ${registerRequest.username}")
            throw e
        } catch (e: ValidationException) {
            logger.warn("Registration failed - validation error: ${e.message}")
            throw e
        } catch (e: Exception) {
            logger.error("Registration failed for user: ${registerRequest.username}", e)
            throw AuthenticationServiceException("Registration failed due to internal error")
        }
    }
    
    /**
     * Authenticate user and return JWT tokens
     */
    fun authenticate(loginRequest: LoginRequest): AuthResponse {
        logger.info("Authenticating user: ${loginRequest.username}")
        
        try {
            // Authenticate with Spring Security
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password)
            )
            
            val userDetails = authentication.principal as CustomUserDetails
            val user = userDetails.getUser()
            
            logger.info("User authenticated successfully: ${user.username} (ID: ${user.id})")
            
            // Update last login time and reset failed attempts
            updateSuccessfulLogin(user)
            
            // Revoke existing refresh tokens (optional - for single session per user)
            // refreshTokenRepository.revokeAllByUser(user)
            
            // Generate new tokens
            val jwtToken = jwtService.generateToken(userDetails)
            val refreshToken = createRefreshToken(user, getClientInfo())
            
            return AuthResponse(
                token = jwtToken,
                refreshToken = refreshToken.token,
                user = UserResponse.from(user)
            )
            
        } catch (e: BadCredentialsException) {
            logger.warn("Authentication failed - bad credentials for user: ${loginRequest.username}")
            handleFailedLogin(loginRequest.username)
            throw InvalidCredentialsException("Invalid username or password")
        } catch (e: LockedException) {
            logger.warn("Authentication failed - account locked: ${loginRequest.username}")
            throw AccountLockedException("Account is locked")
        } catch (e: DisabledException) {
            logger.warn("Authentication failed - account disabled: ${loginRequest.username}")
            throw AccountDisabledException("Account is disabled")
        } catch (e: AuthenticationException) {
            logger.warn("Authentication failed for user: ${loginRequest.username}", e)
            handleFailedLogin(loginRequest.username)
            throw InvalidCredentialsException("Authentication failed")
        } catch (e: Exception) {
            logger.error("Authentication error for user: ${loginRequest.username}", e)
            throw AuthenticationServiceException("Authentication failed due to internal error")
        }
    }
    
    /**
     * Refresh JWT token using refresh token
     */
    fun refreshToken(refreshTokenRequest: RefreshTokenRequest): AuthResponse {
        logger.debug("Refreshing token: ${maskToken(refreshTokenRequest.refreshToken)}")
        
        try {
            val refreshToken = validateRefreshToken(refreshTokenRequest.refreshToken)
            val user = refreshToken.user
            
            // Mark refresh token as used
            refreshTokenRepository.save(refreshToken.markAsUsed())
            
            // Generate new JWT token
            val userDetails = CustomUserDetails(user)
            val newJwtToken = jwtService.generateToken(userDetails)
            
            // Optionally create new refresh token (token rotation)
            val newRefreshToken = createRefreshToken(user, getClientInfo())
            
            // Revoke old refresh token
            refreshTokenRepository.save(refreshToken.revoke())
            
            logger.info("Token refreshed successfully for user: ${user.username}")
            
            return AuthResponse(
                token = newJwtToken,
                refreshToken = newRefreshToken.token,
                user = UserResponse.from(user)
            )
            
        } catch (e: TokenRefreshException) {
            logger.warn("Token refresh failed: ${e.message}")
            throw e
        } catch (e: Exception) {
            logger.error("Token refresh error", e)
            throw TokenRefreshException("Token refresh failed due to internal error")
        }
    }
    
    /**
     * Logout user by revoking refresh token
     */
    fun logout(refreshTokenRequest: RefreshTokenRequest): Boolean {
        logger.info("Logging out user with token: ${maskToken(refreshTokenRequest.refreshToken)}")
        
        return try {
            val refreshToken = refreshTokenRepository.findByToken(refreshTokenRequest.refreshToken)
            
            if (refreshToken != null) {
                refreshTokenRepository.save(refreshToken.revoke())
                logger.info("User logged out successfully: ${refreshToken.user.username}")
                true
            } else {
                logger.warn("Logout attempted with invalid refresh token")
                false
            }
            
        } catch (e: Exception) {
            logger.error("Logout error", e)
            false
        }
    }
    
    /**
     * Logout user from all devices by revoking all refresh tokens
     */
    fun logoutFromAllDevices(userId: Long): Boolean {
        logger.info("Logging out user from all devices: $userId")
        
        return try {
            val revokedCount = refreshTokenRepository.revokeAllByUserId(userId)
            logger.info("Revoked $revokedCount refresh tokens for user: $userId")
            revokedCount > 0
        } catch (e: Exception) {
            logger.error("Error logging out user from all devices: $userId", e)
            false
        }
    }
    
    /**
     * Change user password
     */
    fun changePassword(userId: Long, changePasswordRequest: ChangePasswordRequest): Boolean {
        logger.info("Changing password for user: $userId")
        
        try {
            val user = userRepository.findById(userId)
                .orElseThrow { UserNotFoundException("User not found") }
            
            // Verify current password
            if (!passwordEncoder.matches(changePasswordRequest.currentPassword, user.password)) {
                throw InvalidCredentialsException("Current password is incorrect")
            }
            
            // Validate new password
            validatePassword(changePasswordRequest.newPassword)
            
            // Update password
            val updatedUser = user.withUpdatedPassword(passwordEncoder.encode(changePasswordRequest.newPassword))
            userRepository.save(updatedUser)
            
            // Revoke all refresh tokens to force re-authentication
            refreshTokenRepository.revokeAllByUser(user)
            
            logger.info("Password changed successfully for user: ${user.username}")
            return true
            
        } catch (e: Exception) {
            logger.error("Password change failed for user: $userId", e)
            throw e
        }
    }
    
    // Private helper methods
    
    private fun validateRegistrationData(request: RegisterRequest) {
        val errors = mutableListOf<String>()
        
        // Username validation
        if (request.username.isBlank() || request.username.length < 3) {
            errors.add("Username must be at least 3 characters long")
        }
        
        if (!request.username.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            errors.add("Username can only contain letters, numbers, and underscores")
        }
        
        // Email validation
        if (request.email.isBlank() || !request.email.contains("@")) {
            errors.add("Valid email is required")
        }
        
        // Password validation
        validatePassword(request.password)?.let { errors.add(it) }
        
        // Name validation
        if (request.firstName.isBlank()) {
            errors.add("First name is required")
        }
        
        if (request.lastName.isBlank()) {
            errors.add("Last name is required")
        }
        
        if (errors.isNotEmpty()) {
            throw ValidationException("Validation failed: ${errors.joinToString(", ")}")
        }
    }
    
    private fun validatePassword(password: String): String? {
        return when {
            password.length < 8 -> "Password must be at least 8 characters long"
            !password.any { it.isDigit() } -> "Password must contain at least one digit"
            !password.any { it.isUpperCase() } -> "Password must contain at least one uppercase letter"
            !password.any { it.isLowerCase() } -> "Password must contain at least one lowercase letter"
            !password.any { !it.isLetterOrDigit() } -> "Password must contain at least one special character"
            else -> null
        }
    }
    
    private fun getDefaultRole(): Role {
        return roleRepository.findByName(Role.ROLE_USER)
            ?: throw IllegalStateException("Default USER role not found. Please ensure roles are properly initialized.")
    }
    
    private fun createRefreshToken(user: User, clientInfo: String? = null): RefreshToken {
        val refreshToken = RefreshToken.createForUser(user, refreshExpiration, clientInfo)
        return refreshTokenRepository.save(refreshToken)
    }
    
    private fun validateRefreshToken(token: String): RefreshToken {
        val refreshToken = refreshTokenRepository.findByToken(token)
            ?: throw TokenRefreshException("Refresh token not found")
        
        if (refreshToken.isRevoked) {
            throw TokenRefreshException("Refresh token has been revoked")
        }
        
        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken)
            throw TokenRefreshException("Refresh token has expired")
        }
        
        return refreshToken
    }
    
    private fun updateSuccessfulLogin(user: User) {
        try {
            val updatedUser = user.withUpdatedLoginInfo(LocalDateTime.now(), resetFailedAttempts = true)
            userRepository.save(updatedUser)
        } catch (e: Exception) {
            logger.warn("Failed to update successful login info for user: ${user.username}", e)
        }
    }
    
    private fun handleFailedLogin(username: String) {
        try {
            val user = userRepository.findByUsernameOrEmail(username, username)
            if (user != null) {
                val updatedUser = user.withIncrementedFailedAttempts()
                userRepository.save(updatedUser)
                
                // Lock account if too many failed attempts
                if (updatedUser.shouldLockDueToFailedAttempts()) {
                    val lockedUser = updatedUser.withLockedStatus(true)
                    userRepository.save(lockedUser)
                    logger.warn("Account locked due to excessive failed login attempts: $username")
                }
            }
        } catch (e: Exception) {
            logger.warn("Failed to handle failed login for user: $username", e)
        }
    }
    
    private fun getClientInfo(): String? {
        // In a real implementation, you would extract this from the HTTP request
        // For now, return a placeholder
        return "Unknown Client|${System.currentTimeMillis()}"
    }
    
    private fun maskToken(token: String): String {
        return if (token.length > 8) {
            "${token.take(4)}...${token.takeLast(4)}"
        } else {
            "****"
        }
    }
}

// Custom exceptions
class UserAlreadyExistsException(message: String) : RuntimeException(message)
class ValidationException(message: String) : RuntimeException(message)
class InvalidCredentialsException(message: String) : RuntimeException(message)
class AccountLockedException(message: String) : RuntimeException(message)
class AccountDisabledException(message: String) : RuntimeException(message)
class AuthenticationServiceException(message: String) : RuntimeException(message)
class TokenRefreshException(message: String) : RuntimeException(message)
class UserNotFoundException(message: String) : RuntimeException(message)