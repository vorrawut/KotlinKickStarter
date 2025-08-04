/**
 * Lesson 12 Complete Solution: User Service
 * 
 * Complete user management service with CRUD operations,
 * security features, and administrative functions
 */

package com.learning.security.service

import com.learning.security.dto.UpdateUserRequest
import com.learning.security.model.User
import com.learning.security.repository.RefreshTokenRepository
import com.learning.security.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository
) {
    
    private val logger = LoggerFactory.getLogger(UserService::class.java)
    
    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    fun getUserById(id: Long): User {
        return userRepository.findById(id)
            .orElseThrow { UserNotFoundException("User not found with ID: $id") }
    }
    
    /**
     * Get user by username
     */
    @Transactional(readOnly = true)
    fun getUserByUsername(username: String): User? {
        return userRepository.findByUsername(username)
    }
    
    /**
     * Get user by email
     */
    @Transactional(readOnly = true)
    fun getUserByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }
    
    /**
     * Get all users with pagination and filtering
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllUsers(
        pageable: Pageable,
        search: String? = null,
        role: String? = null,
        active: Boolean? = null
    ): Page<User> {
        return when {
            !search.isNullOrBlank() -> {
                if (active == true) {
                    userRepository.searchActiveUsers(search, pageable)
                } else {
                    userRepository.searchUsers(search, pageable)
                }
            }
            role != null -> {
                userRepository.findByRoleNameOrderByCreatedAtDesc(role, pageable)
            }
            active == true -> {
                userRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable)
            }
            else -> {
                userRepository.findAll(pageable)
            }
        }
    }
    
    /**
     * Update user profile
     */
    fun updateUser(id: Long, updateRequest: UpdateUserRequest): User {
        logger.info("Updating user profile: {}", id)
        
        val user = getUserById(id)
        
        // Validate email uniqueness if email is being changed
        updateRequest.email?.let { newEmail ->
            if (newEmail != user.email && userRepository.existsByEmail(newEmail)) {
                throw ValidationException("Email already exists: $newEmail")
            }
        }
        
        val updatedUser = updateRequest.applyTo(user)
        val savedUser = userRepository.save(updatedUser)
        
        logger.info("User profile updated successfully: {}", id)
        return savedUser
    }
    
    /**
     * Delete user (Admin only)
     */
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteUser(id: Long): Boolean {
        logger.warn("Deleting user: {}", id)
        
        return try {
            val user = getUserById(id)
            
            // Revoke all refresh tokens
            refreshTokenRepository.revokeAllByUser(user)
            
            // Delete user
            userRepository.delete(user)
            
            logger.warn("User deleted successfully: {}", id)
            true
        } catch (e: Exception) {
            logger.error("Failed to delete user: {}", id, e)
            false
        }
    }
    
    /**
     * Lock user account (Admin only)
     */
    @PreAuthorize("hasRole('ADMIN')")
    fun lockUser(id: Long): Boolean {
        logger.warn("Locking user account: {}", id)
        
        return try {
            val user = getUserById(id)
            val lockedUser = user.withLockedStatus(true)
            userRepository.save(lockedUser)
            
            // Revoke all refresh tokens to force logout
            refreshTokenRepository.revokeAllByUser(user)
            
            logger.warn("User account locked successfully: {}", id)
            true
        } catch (e: Exception) {
            logger.error("Failed to lock user: {}", id, e)
            false
        }
    }
    
    /**
     * Unlock user account (Admin only)
     */
    @PreAuthorize("hasRole('ADMIN')")
    fun unlockUser(id: Long): Boolean {
        logger.info("Unlocking user account: {}", id)
        
        return try {
            val user = getUserById(id)
            val unlockedUser = user.copy(
                isLocked = false,
                failedLoginAttempts = 0,
                updatedAt = LocalDateTime.now()
            )
            userRepository.save(unlockedUser)
            
            logger.info("User account unlocked successfully: {}", id)
            true
        } catch (e: Exception) {
            logger.error("Failed to unlock user: {}", id, e)
            false
        }
    }
    
    /**
     * Activate/Deactivate user account (Admin only)
     */
    @PreAuthorize("hasRole('ADMIN')")
    fun setUserActiveStatus(id: Long, active: Boolean): Boolean {
        logger.info("Setting user active status: {} to {}", id, active)
        
        return try {
            val user = getUserById(id)
            val updatedUser = user.withActiveStatus(active)
            userRepository.save(updatedUser)
            
            if (!active) {
                // Revoke all refresh tokens if deactivating
                refreshTokenRepository.revokeAllByUser(user)
            }
            
            logger.info("User active status updated successfully: {} to {}", id, active)
            true
        } catch (e: Exception) {
            logger.error("Failed to set user active status: {} to {}", id, active, e)
            false
        }
    }
    
    /**
     * Get user statistics (Admin only)
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    fun getUserStatistics(): Map<String, Any> {
        logger.info("Generating user statistics")
        
        val totalUsers = userRepository.count()
        val activeUsers = userRepository.countActiveUsers()
        val lockedUsers = userRepository.countLockedUsers()
        val newUsersThisMonth = userRepository.countUsersCreatedAfter(
            LocalDateTime.now().minusMonths(1)
        )
        val activeUsersThisWeek = userRepository.countActiveUsersSince(
            LocalDateTime.now().minusWeeks(1)
        )
        
        return mapOf(
            "totalUsers" to totalUsers,
            "activeUsers" to activeUsers,
            "lockedUsers" to lockedUsers,
            "inactiveUsers" to (totalUsers - activeUsers),
            "newUsersThisMonth" to newUsersThisMonth,
            "activeUsersThisWeek" to activeUsersThisWeek,
            "userGrowthRate" to calculateGrowthRate(),
            "averageUsersPerDay" to calculateAverageUsersPerDay(),
            "roleDistribution" to getUserRoleDistribution(),
            "lastUpdated" to LocalDateTime.now()
        )
    }
    
    /**
     * Search users by various criteria
     */
    @Transactional(readOnly = true)
    fun searchUsers(
        searchTerm: String,
        activeOnly: Boolean = true,
        pageable: Pageable
    ): Page<User> {
        return if (activeOnly) {
            userRepository.searchActiveUsers(searchTerm, pageable)
        } else {
            userRepository.searchUsers(searchTerm, pageable)
        }
    }
    
    /**
     * Get users by role
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    fun getUsersByRole(roleName: String): List<User> {
        return userRepository.findByRoleName(roleName)
    }
    
    /**
     * Get recently created users
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    fun getRecentUsers(days: Int = 7): List<User> {
        val cutoffDate = LocalDateTime.now().minusDays(days.toLong())
        return userRepository.findUsersCreatedAfter(cutoffDate)
    }
    
    /**
     * Get inactive users
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    fun getInactiveUsers(days: Int = 30): List<User> {
        val cutoffDate = LocalDateTime.now().minusDays(days.toLong())
        return userRepository.findInactiveUsersSince(cutoffDate)
    }
    
    /**
     * Bulk lock users with excessive failed attempts
     */
    @PreAuthorize("hasRole('ADMIN')")
    fun lockUsersWithExcessiveFailedAttempts(maxAttempts: Int = 5): Int {
        logger.warn("Bulk locking users with excessive failed attempts (max: {})", maxAttempts)
        
        return try {
            val lockedCount = userRepository.lockUsersWithExcessiveFailedAttempts(maxAttempts)
            logger.warn("Locked {} users with excessive failed attempts", lockedCount)
            lockedCount
        } catch (e: Exception) {
            logger.error("Failed to bulk lock users with excessive failed attempts", e)
            0
        }
    }
    
    /**
     * Cleanup inactive users
     */
    @PreAuthorize("hasRole('ADMIN')")
    fun deactivateInactiveUsers(days: Int = 365): Int {
        logger.info("Deactivating users inactive for {} days", days)
        
        return try {
            val cutoffDate = LocalDateTime.now().minusDays(days.toLong())
            val deactivatedCount = userRepository.deactivateInactiveUsers(cutoffDate)
            logger.info("Deactivated {} inactive users", deactivatedCount)
            deactivatedCount
        } catch (e: Exception) {
            logger.error("Failed to deactivate inactive users", e)
            0
        }
    }
    
    // Private helper methods
    
    private fun calculateGrowthRate(): Double {
        val currentMonth = userRepository.countUsersCreatedAfter(LocalDateTime.now().minusMonths(1))
        val previousMonth = userRepository.countUsersCreatedBetween(
            LocalDateTime.now().minusMonths(2),
            LocalDateTime.now().minusMonths(1)
        )
        
        return if (previousMonth > 0) {
            ((currentMonth.toDouble() - previousMonth.toDouble()) / previousMonth.toDouble()) * 100
        } else {
            if (currentMonth > 0) 100.0 else 0.0
        }
    }
    
    private fun calculateAverageUsersPerDay(): Double {
        val thirtyDaysAgo = LocalDateTime.now().minusDays(30)
        val usersInLast30Days = userRepository.countUsersCreatedAfter(thirtyDaysAgo)
        return usersInLast30Days.toDouble() / 30.0
    }
    
    private fun getUserRoleDistribution(): Map<String, Long> {
        // This would typically use a custom query to get role counts
        // For now, return a simplified version
        return mapOf(
            "USER" to userRepository.countByRoleName("USER"),
            "ADMIN" to userRepository.countByRoleName("ADMIN"),
            "MODERATOR" to userRepository.countByRoleName("MODERATOR")
        )
    }
}

/**
 * Security service for method-level security expressions
 */
@Service("userSecurityService")
class UserSecurityService {
    
    private val logger = LoggerFactory.getLogger(UserSecurityService::class.java)
    
    /**
     * Check if the authenticated user can access the specified user profile
     */
    fun canAccessUser(authentication: org.springframework.security.core.Authentication, userId: Long): Boolean {
        val currentUser = authentication.principal as? CustomUserDetails
            ?: return false
        
        // Admins can access any user
        if (currentUser.hasRole("ADMIN")) {
            return true
        }
        
        // Users can access their own profile
        return currentUser.getId() == userId
    }
    
    /**
     * Check if the authenticated user can update the specified user
     */
    fun canUpdateUser(authentication: org.springframework.security.core.Authentication, userId: Long): Boolean {
        return canAccessUser(authentication, userId)
    }
    
    /**
     * Check if the authenticated user is accessing their own profile
     */
    fun isOwnProfile(authentication: org.springframework.security.core.Authentication, userId: Long): Boolean {
        val currentUser = authentication.principal as? CustomUserDetails
            ?: return false
        
        return currentUser.getId() == userId
    }
    
    /**
     * Check if the authenticated user has higher privileges than the target user
     */
    fun hasHigherPrivileges(
        authentication: org.springframework.security.core.Authentication,
        targetUserId: Long
    ): Boolean {
        val currentUser = authentication.principal as? CustomUserDetails
            ?: return false
        
        // Only admins have higher privileges
        return currentUser.hasRole("ADMIN")
    }
}