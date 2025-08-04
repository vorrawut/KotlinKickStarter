/**
 * Lesson 12 Complete Solution: User Repository
 * 
 * Complete repository with comprehensive query methods for user management,
 * authentication, and security operations
 */

package com.learning.security.repository

import com.learning.security.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Repository
interface UserRepository : JpaRepository<User, Long> {
    
    // Authentication queries
    fun findByUsername(username: String): User?
    
    fun findByEmail(email: String): User?
    
    fun findByUsernameOrEmail(username: String, email: String): User?
    
    // Existence checks for registration validation
    fun existsByUsername(username: String): Boolean
    
    fun existsByEmail(email: String): Boolean
    
    fun existsByUsernameOrEmail(username: String, email: String): Boolean
    
    // Active user queries
    fun findByIsActiveTrue(): List<User>
    
    fun findByIsActiveTrueAndIsEnabledTrue(): List<User>
    
    fun findByIsActiveTrueAndIsLockedFalse(): List<User>
    
    fun countByIsActiveTrue(): Long
    
    fun countByIsActiveTrueAndIsEnabledTrue(): Long
    
    // Role-based queries
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    fun findByRoleName(@Param("roleName") roleName: String): List<User>
    
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name IN :roleNames")
    fun findByRoleNames(@Param("roleNames") roleNames: List<String>): List<User>
    
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
    fun countByRoleName(@Param("roleName") roleName: String): Long
    
    // Comprehensive active user query
    @Query("""
        SELECT u FROM User u 
        WHERE u.isActive = true 
        AND u.isLocked = false 
        AND u.isEnabled = true 
        AND u.isCredentialsExpired = false
    """)
    fun findAllActiveAndEligibleUsers(): List<User>
    
    // Security and audit queries
    @Query("SELECT u FROM User u WHERE u.failedLoginAttempts >= :maxAttempts")
    fun findUsersWithExcessiveFailedAttempts(@Param("maxAttempts") maxAttempts: Int): List<User>
    
    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :cutoffDate")
    fun findInactiveUsersSince(@Param("cutoffDate") cutoffDate: LocalDateTime): List<User>
    
    @Query("SELECT u FROM User u WHERE u.createdAt > :date")
    fun findUsersCreatedAfter(@Param("date") date: LocalDateTime): List<User>
    
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :start AND :end")
    fun findUsersCreatedBetween(
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime
    ): List<User>
    
    // Search queries
    @Query("""
        SELECT u FROM User u 
        WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
    """)
    fun searchUsers(@Param("search") searchTerm: String): List<User>
    
    @Query("""
        SELECT u FROM User u 
        WHERE u.isActive = true
        AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')))
    """)
    fun searchActiveUsers(@Param("search") searchTerm: String): List<User>
    
    // Pagination queries
    fun findByIsActiveTrueOrderByCreatedAtDesc(pageable: Pageable): Page<User>
    
    fun findByIsActiveTrueOrderByUsernameAsc(pageable: Pageable): Page<User>
    
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName ORDER BY u.createdAt DESC")
    fun findByRoleNameOrderByCreatedAtDesc(
        @Param("roleName") roleName: String,
        pageable: Pageable
    ): Page<User>
    
    // Update operations
    @Modifying
    @Query("UPDATE User u SET u.isActive = :active WHERE u.id = :id")
    fun updateUserActiveStatus(@Param("id") id: Long, @Param("active") active: Boolean): Int
    
    @Modifying
    @Query("UPDATE User u SET u.isLocked = :locked WHERE u.id = :id")
    fun updateUserLockedStatus(@Param("id") id: Long, @Param("locked") locked: Boolean): Int
    
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = :attempts WHERE u.id = :id")
    fun updateFailedLoginAttempts(@Param("id") id: Long, @Param("attempts") attempts: Int): Int
    
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime WHERE u.id = :id")
    fun updateLastLoginTime(@Param("id") id: Long, @Param("loginTime") loginTime: LocalDateTime): Int
    
    @Modifying
    @Query("""
        UPDATE User u SET 
        u.lastLoginAt = :loginTime, 
        u.failedLoginAttempts = 0 
        WHERE u.id = :id
    """)
    fun updateSuccessfulLogin(@Param("id") id: Long, @Param("loginTime") loginTime: LocalDateTime): Int
    
    @Modifying
    @Query("UPDATE User u SET u.password = :password WHERE u.id = :id")
    fun updateUserPassword(@Param("id") id: Long, @Param("password") password: String): Int
    
    // Bulk operations
    @Modifying
    @Query("UPDATE User u SET u.isLocked = true WHERE u.failedLoginAttempts >= :maxAttempts")
    fun lockUsersWithExcessiveFailedAttempts(@Param("maxAttempts") maxAttempts: Int): Int
    
    @Modifying
    @Query("UPDATE User u SET u.isActive = false WHERE u.lastLoginAt < :cutoffDate")
    fun deactivateInactiveUsers(@Param("cutoffDate") cutoffDate: LocalDateTime): Int
    
    // Statistics queries
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    fun countActiveUsers(): Long
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.isLocked = true")
    fun countLockedUsers(): Long
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt > :date")
    fun countUsersCreatedAfter(@Param("date") date: LocalDateTime): Long
    
    @Query("""
        SELECT COUNT(u) FROM User u 
        WHERE u.lastLoginAt IS NOT NULL 
        AND u.lastLoginAt > :date
    """)
    fun countActiveUsersSince(@Param("date") date: LocalDateTime): Long
    
    // Complex queries with joins
    @Query("""
        SELECT u FROM User u 
        LEFT JOIN FETCH u.roles 
        WHERE u.username = :username
    """)
    fun findByUsernameWithRoles(@Param("username") username: String): User?
    
    @Query("""
        SELECT u FROM User u 
        LEFT JOIN FETCH u.roles 
        WHERE u.email = :email
    """)
    fun findByEmailWithRoles(@Param("email") email: String): User?
    
    @Query("""
        SELECT DISTINCT u FROM User u 
        LEFT JOIN FETCH u.roles r 
        WHERE u.isActive = true 
        ORDER BY u.createdAt DESC
    """)
    fun findAllActiveUsersWithRoles(): List<User>
    
    // Additional methods for pagination support
    @Query("""
        SELECT u FROM User u 
        WHERE u.isActive = true
        AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
        ORDER BY u.createdAt DESC
    """)
    fun searchActiveUsers(@Param("searchTerm") searchTerm: String, pageable: Pageable): Page<User>
    
    @Query("""
        SELECT u FROM User u 
        WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        ORDER BY u.createdAt DESC
    """)
    fun searchUsers(@Param("searchTerm") searchTerm: String, pageable: Pageable): Page<User>
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :start AND :end")
    fun countUsersCreatedBetween(
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime
    ): Long
}