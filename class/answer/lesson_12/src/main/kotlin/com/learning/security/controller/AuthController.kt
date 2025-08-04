/**
 * Lesson 12 Complete Solution: Authentication Controller
 * 
 * Complete REST controller for authentication operations including
 * registration, login, token refresh, logout, and password management
 */

package com.learning.security.controller

import com.learning.security.dto.*
import com.learning.security.service.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
@Validated
class AuthController(
    private val authService: AuthService
) {
    
    private val logger = LoggerFactory.getLogger(AuthController::class.java)
    
    /**
     * Register a new user
     */
    @PostMapping("/register")
    fun register(
        @Valid @RequestBody request: RegisterRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<*> {
        logger.info("Registration request from IP: {} for username: {}", 
            getClientIpAddress(httpRequest), request.username)
        
        return try {
            // Additional validation
            val validationErrors = request.validate()
            if (validationErrors.isNotEmpty()) {
                return ResponseEntity.badRequest().body(
                    ErrorResponse.validationError(
                        validationErrors.associateWith { "Validation failed" }
                    )
                )
            }
            
            val authResponse = authService.register(request)
            
            logger.info("User registered successfully: {} from IP: {}", 
                request.username, getClientIpAddress(httpRequest))
            
            ResponseEntity.status(HttpStatus.CREATED).body(authResponse)
            
        } catch (e: UserAlreadyExistsException) {
            logger.warn("Registration failed - user exists: {} from IP: {}", 
                request.username, getClientIpAddress(httpRequest))
            
            ResponseEntity.badRequest().body(
                ErrorResponse.badRequest("User with this username or email already exists")
            )
        } catch (e: ValidationException) {
            logger.warn("Registration failed - validation: {} from IP: {}", 
                e.message, getClientIpAddress(httpRequest))
            
            ResponseEntity.badRequest().body(
                ErrorResponse.badRequest(e.message ?: "Validation failed")
            )
        } catch (e: Exception) {
            logger.error("Registration failed for user: {} from IP: {}", 
                request.username, getClientIpAddress(httpRequest), e)
            
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.internalError("Registration failed due to server error")
            )
        }
    }
    
    /**
     * Authenticate user and return JWT tokens
     */
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<*> {
        logger.info("Login request from IP: {} for username: {}", 
            getClientIpAddress(httpRequest), request.username)
        
        return try {
            val authResponse = authService.authenticate(request)
            
            logger.info("User logged in successfully: {} from IP: {}", 
                request.username, getClientIpAddress(httpRequest))
            
            ResponseEntity.ok(authResponse)
            
        } catch (e: InvalidCredentialsException) {
            logger.warn("Login failed - invalid credentials for: {} from IP: {}", 
                request.username, getClientIpAddress(httpRequest))
            
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorResponse.unauthorized("Invalid username or password")
            )
        } catch (e: AccountLockedException) {
            logger.warn("Login failed - account locked: {} from IP: {}", 
                request.username, getClientIpAddress(httpRequest))
            
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorResponse.unauthorized("Account is locked due to multiple failed login attempts")
            )
        } catch (e: AccountDisabledException) {
            logger.warn("Login failed - account disabled: {} from IP: {}", 
                request.username, getClientIpAddress(httpRequest))
            
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorResponse.unauthorized("Account is disabled")
            )
        } catch (e: Exception) {
            logger.error("Login failed for user: {} from IP: {}", 
                request.username, getClientIpAddress(httpRequest), e)
            
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.internalError("Authentication failed due to server error")
            )
        }
    }
    
    /**
     * Refresh JWT token using refresh token
     */
    @PostMapping("/refresh")
    fun refreshToken(
        @Valid @RequestBody request: RefreshTokenRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<*> {
        logger.debug("Token refresh request from IP: {}", getClientIpAddress(httpRequest))
        
        return try {
            val authResponse = authService.refreshToken(request)
            
            logger.info("Token refreshed successfully from IP: {}", getClientIpAddress(httpRequest))
            
            ResponseEntity.ok(authResponse)
            
        } catch (e: TokenRefreshException) {
            logger.warn("Token refresh failed from IP: {} - {}", 
                getClientIpAddress(httpRequest), e.message)
            
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorResponse.unauthorized("Token refresh failed: ${e.message}")
            )
        } catch (e: Exception) {
            logger.error("Token refresh error from IP: {}", getClientIpAddress(httpRequest), e)
            
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.internalError("Token refresh failed due to server error")
            )
        }
    }
    
    /**
     * Logout user by revoking refresh token
     */
    @PostMapping("/logout")
    fun logout(
        @Valid @RequestBody request: RefreshTokenRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<*> {
        logger.info("Logout request from IP: {}", getClientIpAddress(httpRequest))
        
        return try {
            val success = authService.logout(request)
            
            if (success) {
                logger.info("User logged out successfully from IP: {}", getClientIpAddress(httpRequest))
                ResponseEntity.ok(SuccessResponse.loggedOut())
            } else {
                logger.warn("Logout failed - invalid token from IP: {}", getClientIpAddress(httpRequest))
                ResponseEntity.badRequest().body(
                    ErrorResponse.badRequest("Invalid refresh token")
                )
            }
            
        } catch (e: Exception) {
            logger.error("Logout error from IP: {}", getClientIpAddress(httpRequest), e)
            
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.internalError("Logout failed due to server error")
            )
        }
    }
    
    /**
     * Logout user from all devices
     */
    @PostMapping("/logout-all")
    fun logoutFromAllDevices(
        authentication: Authentication,
        httpRequest: HttpServletRequest
    ): ResponseEntity<*> {
        logger.info("Logout from all devices request from IP: {} for user: {}", 
            getClientIpAddress(httpRequest), authentication.name)
        
        return try {
            val userDetails = authentication.principal as? com.learning.security.service.CustomUserDetails
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ErrorResponse.unauthorized("Invalid authentication")
                )
            
            val userId = userDetails.getId()
                ?: return ResponseEntity.badRequest().body(
                    ErrorResponse.badRequest("User ID not found")
                )
            
            val success = authService.logoutFromAllDevices(userId)
            
            if (success) {
                logger.info("User logged out from all devices: {} from IP: {}", 
                    authentication.name, getClientIpAddress(httpRequest))
                
                ResponseEntity.ok(SuccessResponse.ok("Logged out from all devices successfully"))
            } else {
                ResponseEntity.badRequest().body(
                    ErrorResponse.badRequest("No active sessions found")
                )
            }
            
        } catch (e: Exception) {
            logger.error("Logout from all devices failed for user: {} from IP: {}", 
                authentication.name, getClientIpAddress(httpRequest), e)
            
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.internalError("Logout from all devices failed")
            )
        }
    }
    
    /**
     * Change user password
     */
    @PostMapping("/change-password")
    fun changePassword(
        @Valid @RequestBody request: ChangePasswordRequest,
        authentication: Authentication,
        httpRequest: HttpServletRequest
    ): ResponseEntity<*> {
        logger.info("Password change request from IP: {} for user: {}", 
            getClientIpAddress(httpRequest), authentication.name)
        
        return try {
            // Additional validation
            val validationErrors = request.validate()
            if (validationErrors.isNotEmpty()) {
                return ResponseEntity.badRequest().body(
                    ErrorResponse.validationError(
                        validationErrors.associateWith { "Validation failed" }
                    )
                )
            }
            
            val userDetails = authentication.principal as? com.learning.security.service.CustomUserDetails
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ErrorResponse.unauthorized("Invalid authentication")
                )
            
            val userId = userDetails.getId()
                ?: return ResponseEntity.badRequest().body(
                    ErrorResponse.badRequest("User ID not found")
                )
            
            val success = authService.changePassword(userId, request)
            
            if (success) {
                logger.info("Password changed successfully for user: {} from IP: {}", 
                    authentication.name, getClientIpAddress(httpRequest))
                
                ResponseEntity.ok(SuccessResponse.passwordChanged())
            } else {
                ResponseEntity.badRequest().body(
                    ErrorResponse.badRequest("Password change failed")
                )
            }
            
        } catch (e: InvalidCredentialsException) {
            logger.warn("Password change failed - invalid current password for user: {} from IP: {}", 
                authentication.name, getClientIpAddress(httpRequest))
            
            ResponseEntity.badRequest().body(
                ErrorResponse.badRequest("Current password is incorrect")
            )
        } catch (e: ValidationException) {
            logger.warn("Password change failed - validation error for user: {} from IP: {}", 
                authentication.name, getClientIpAddress(httpRequest))
            
            ResponseEntity.badRequest().body(
                ErrorResponse.badRequest(e.message ?: "Password validation failed")
            )
        } catch (e: Exception) {
            logger.error("Password change failed for user: {} from IP: {}", 
                authentication.name, getClientIpAddress(httpRequest), e)
            
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.internalError("Password change failed due to server error")
            )
        }
    }
    
    /**
     * Get current user info from token
     */
    @GetMapping("/me")
    fun getCurrentUser(authentication: Authentication): ResponseEntity<*> {
        return try {
            val userDetails = authentication.principal as? com.learning.security.service.CustomUserDetails
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ErrorResponse.unauthorized("Invalid authentication")
                )
            
            val userResponse = UserResponse.from(userDetails.getUser())
            ResponseEntity.ok(userResponse)
            
        } catch (e: Exception) {
            logger.error("Failed to get current user info", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.internalError("Failed to retrieve user information")
            )
        }
    }
    
    /**
     * Health check endpoint for authentication service
     */
    @GetMapping("/health")
    fun health(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(mapOf(
            "status" to "UP",
            "service" to "authentication",
            "timestamp" to System.currentTimeMillis()
        ))
    }
    
    // Helper methods
    
    /**
     * Extract client IP address considering proxy headers
     */
    private fun getClientIpAddress(request: HttpServletRequest): String {
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        if (!xForwardedFor.isNullOrBlank()) {
            return xForwardedFor.split(",")[0].trim()
        }
        
        val xRealIp = request.getHeader("X-Real-IP")
        if (!xRealIp.isNullOrBlank()) {
            return xRealIp.trim()
        }
        
        return request.remoteAddr ?: "unknown"
    }
    
    /**
     * Extract User-Agent header
     */
    private fun getUserAgent(request: HttpServletRequest): String {
        return request.getHeader("User-Agent") ?: "unknown"
    }
}