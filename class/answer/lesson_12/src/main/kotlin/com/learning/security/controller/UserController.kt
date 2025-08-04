/**
 * Lesson 12 Complete Solution: User Management Controller
 * 
 * Complete REST controller for user management operations with
 * role-based access control and comprehensive security features
 */

package com.learning.security.controller

import com.learning.security.dto.*
import com.learning.security.service.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
@Validated
class UserController(
    private val userService: UserService
) {
    
    private val logger = LoggerFactory.getLogger(UserController::class.java)
    
    /**
     * Get current authenticated user information
     */
    @GetMapping("/me")
    fun getCurrentUser(authentication: Authentication): ResponseEntity<*> {
        logger.debug("Getting current user info for: {}", authentication.name)
        
        return try {
            val userDetails = authentication.principal as? CustomUserDetails
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ErrorResponse.unauthorized("Invalid authentication")
                )
            
            val userResponse = UserResponse.from(userDetails.getUser())
            ResponseEntity.ok(userResponse)
            
        } catch (e: Exception) {
            logger.error("Failed to get current user info for: {}", authentication.name, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.internalError("Failed to retrieve user information")
            )
        }
    }
    
    /**
     * Update current user profile
     */
    @PutMapping("/me")
    fun updateCurrentUser(
        @Valid @RequestBody request: UpdateUserRequest,
        authentication: Authentication,
        httpRequest: HttpServletRequest
    ): ResponseEntity<*> {
        logger.info("Updating user profile for: {} from IP: {}", 
            authentication.name, getClientIpAddress(httpRequest))
        
        return try {
            val userDetails = authentication.principal as? CustomUserDetails
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ErrorResponse.unauthorized("Invalid authentication")
                )
            
            val userId = userDetails.getId()
                ?: return ResponseEntity.badRequest().body(
                    ErrorResponse.badRequest("User ID not found")
                )
            
            if (!request.hasUpdates()) {
                return ResponseEntity.badRequest().body(
                    ErrorResponse.badRequest("No updates provided")
                )
            }
            
            val updatedUser = userService.updateUser(userId, request)
            val userResponse = UserResponse.from(updatedUser)
            
            logger.info("User profile updated successfully for: {} from IP: {}", 
                authentication.name, getClientIpAddress(httpRequest))
            
            ResponseEntity.ok(userResponse)
            
        } catch (e: UserNotFoundException) {
            logger.warn("User not found for profile update: {}", authentication.name)
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.badRequest("User not found")
            )
        } catch (e: ValidationException) {
            logger.warn("Validation failed for user profile update: {} - {}", 
                authentication.name, e.message)
            ResponseEntity.badRequest().body(
                ErrorResponse.badRequest(e.message ?: "Validation failed")
            )
        } catch (e: Exception) {
            logger.error("Failed to update user profile for: {} from IP: {}", 
                authentication.name, getClientIpAddress(httpRequest), e)
            
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.internalError("Profile update failed")
            )
        }
    }
    
    /**
     * Get all users (Admin only) with pagination
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllUsers(
        @PageableDefault(size = 20, sort = ["createdAt"]) pageable: Pageable,
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) role: String?,
        @RequestParam(required = false) active: Boolean?,
        authentication: Authentication
    ): ResponseEntity<*> {
        logger.info("Admin {} requesting user list from IP: {}", 
            authentication.name, getCurrentRequestIp())
        
        return try {
            val usersPage = userService.getAllUsers(pageable, search, role, active)
            
            val response = mapOf(
                "users" to usersPage.content.map { UserResponse.from(it) },
                "pagination" to mapOf(
                    "page" to usersPage.number,
                    "size" to usersPage.size,
                    "totalElements" to usersPage.totalElements,
                    "totalPages" to usersPage.totalPages,
                    "hasNext" to usersPage.hasNext(),
                    "hasPrevious" to usersPage.hasPrevious()
                )
            )
            
            ResponseEntity.ok(response)
            
        } catch (e: Exception) {
            logger.error("Failed to get users list for admin: {}", authentication.name, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.internalError("Failed to retrieve users")
            )
        }
    }
    
    /**
     * Get user by ID (Admin or own profile)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurityService.isOwnProfile(authentication, #id)")
    fun getUserById(
        @PathVariable id: Long,
        authentication: Authentication
    ): ResponseEntity<*> {
        logger.debug("Getting user by ID: {} requested by: {}", id, authentication.name)
        
        return try {
            val user = userService.getUserById(id)
            val userResponse = UserResponse.from(user)
            ResponseEntity.ok(userResponse)
            
        } catch (e: UserNotFoundException) {
            logger.warn("User not found: {} requested by: {}", id, authentication.name)
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.badRequest("User not found")
            )
        } catch (e: Exception) {
            logger.error("Failed to get user by ID: {} for: {}", id, authentication.name, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.internalError("Failed to retrieve user")
            )
        }
    }
    
    /**
     * Update user by ID (Admin or own profile for limited fields)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurityService.canUpdateUser(authentication, #id)")
    fun updateUser(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateUserRequest,
        authentication: Authentication,
        httpRequest: HttpServletRequest
    ): ResponseEntity<*> {
        logger.info("Updating user: {} by: {} from IP: {}", 
            id, authentication.name, getClientIpAddress(httpRequest))
        
        return try {
            if (!request.hasUpdates()) {
                return ResponseEntity.badRequest().body(
                    ErrorResponse.badRequest("No updates provided")
                )
            }
            
            val updatedUser = userService.updateUser(id, request)
            val userResponse = UserResponse.from(updatedUser)
            
            logger.info("User updated successfully: {} by: {} from IP: {}", 
                id, authentication.name, getClientIpAddress(httpRequest))
            
            ResponseEntity.ok(userResponse)
            
        } catch (e: UserNotFoundException) {
            logger.warn("User not found for update: {} by: {}", id, authentication.name)
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.badRequest("User not found")
            )
        } catch (e: ValidationException) {
            logger.warn("Validation failed for user update: {} - {}", id, e.message)
            ResponseEntity.badRequest().body(
                ErrorResponse.badRequest(e.message ?: "Validation failed")
            )
        } catch (e: Exception) {
            logger.error("Failed to update user: {} by: {} from IP: {}", 
                id, authentication.name, getClientIpAddress(httpRequest), e)
            
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.internalError("User update failed")
            )
        }
    }
    
    /**
     * Delete user by ID (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteUser(
        @PathVariable id: Long,
        authentication: Authentication,
        httpRequest: HttpServletRequest
    ): ResponseEntity<*> {
        logger.warn("Deleting user: {} by admin: {} from IP: {}", 
            id, authentication.name, getClientIpAddress(httpRequest))
        
        return try {
            // Prevent admin from deleting themselves
            val currentUserDetails = authentication.principal as? CustomUserDetails
            if (currentUserDetails?.getId() == id) {
                return ResponseEntity.badRequest().body(
                    ErrorResponse.badRequest("Cannot delete your own account")
                )
            }
            
            val success = userService.deleteUser(id)
            
            if (success) {
                logger.warn("User deleted successfully: {} by admin: {} from IP: {}", 
                    id, authentication.name, getClientIpAddress(httpRequest))
                
                ResponseEntity.ok(SuccessResponse.ok("User deleted successfully"))
            } else {
                ResponseEntity.badRequest().body(
                    ErrorResponse.badRequest("User deletion failed")
                )
            }
            
        } catch (e: UserNotFoundException) {
            logger.warn("User not found for deletion: {} by admin: {}", id, authentication.name)
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.badRequest("User not found")
            )
        } catch (e: Exception) {
            logger.error("Failed to delete user: {} by admin: {} from IP: {}", 
                id, authentication.name, getClientIpAddress(httpRequest), e)
            
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.internalError("User deletion failed")
            )
        }
    }
    
    /**
     * Lock user account (Admin only)
     */
    @PostMapping("/{id}/lock")
    @PreAuthorize("hasRole('ADMIN')")
    fun lockUser(
        @PathVariable id: Long,
        authentication: Authentication,
        httpRequest: HttpServletRequest
    ): ResponseEntity<*> {
        logger.warn("Locking user account: {} by admin: {} from IP: {}", 
            id, authentication.name, getClientIpAddress(httpRequest))
        
        return try {
            // Prevent admin from locking themselves
            val currentUserDetails = authentication.principal as? CustomUserDetails
            if (currentUserDetails?.getId() == id) {
                return ResponseEntity.badRequest().body(
                    ErrorResponse.badRequest("Cannot lock your own account")
                )
            }
            
            val success = userService.lockUser(id)
            
            if (success) {
                logger.warn("User account locked: {} by admin: {} from IP: {}", 
                    id, authentication.name, getClientIpAddress(httpRequest))
                
                ResponseEntity.ok(SuccessResponse.accountLocked())
            } else {
                ResponseEntity.badRequest().body(
                    ErrorResponse.badRequest("Failed to lock user account")
                )
            }
            
        } catch (e: UserNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.badRequest("User not found")
            )
        } catch (e: Exception) {
            logger.error("Failed to lock user: {} by admin: {}", id, authentication.name, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.internalError("Account lock operation failed")
            )
        }
    }
    
    /**
     * Unlock user account (Admin only)
     */
    @PostMapping("/{id}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    fun unlockUser(
        @PathVariable id: Long,
        authentication: Authentication,
        httpRequest: HttpServletRequest
    ): ResponseEntity<*> {
        logger.info("Unlocking user account: {} by admin: {} from IP: {}", 
            id, authentication.name, getClientIpAddress(httpRequest))
        
        return try {
            val success = userService.unlockUser(id)
            
            if (success) {
                logger.info("User account unlocked: {} by admin: {} from IP: {}", 
                    id, authentication.name, getClientIpAddress(httpRequest))
                
                ResponseEntity.ok(SuccessResponse.accountUnlocked())
            } else {
                ResponseEntity.badRequest().body(
                    ErrorResponse.badRequest("Failed to unlock user account")
                )
            }
            
        } catch (e: UserNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.badRequest("User not found")
            )
        } catch (e: Exception) {
            logger.error("Failed to unlock user: {} by admin: {}", id, authentication.name, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.internalError("Account unlock operation failed")
            )
        }
    }
    
    /**
     * Get user statistics (Admin only)
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    fun getUserStatistics(authentication: Authentication): ResponseEntity<*> {
        logger.info("Getting user statistics requested by admin: {}", authentication.name)
        
        return try {
            val stats = userService.getUserStatistics()
            ResponseEntity.ok(stats)
            
        } catch (e: Exception) {
            logger.error("Failed to get user statistics for admin: {}", authentication.name, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.internalError("Failed to retrieve user statistics")
            )
        }
    }
    
    // Helper methods
    
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
    
    private fun getCurrentRequestIp(): String {
        // In a real implementation, you would inject HttpServletRequest
        // For now, return a placeholder
        return "unknown"
    }
}