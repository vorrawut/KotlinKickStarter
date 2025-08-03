/**
 * Lesson 11 Complete Solution: User Repository
 * 
 * Complete repository with comprehensive query methods for testing
 */

package com.learning.testing.repository

import com.learning.testing.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface UserRepository : JpaRepository<User, Long> {
    
    // Method name queries for testing
    fun findByUsername(username: String): User?
    
    fun findByEmail(email: String): User?
    
    fun findByIsActiveTrue(): List<User>
    
    fun findByIsActiveFalse(): List<User>
    
    fun findByUsernameContainingIgnoreCase(username: String): List<User>
    
    fun findByFirstNameAndLastName(firstName: String, lastName: String): List<User>
    
    fun findByEmailContaining(emailDomain: String): List<User>
    
    fun existsByUsername(username: String): Boolean
    
    fun existsByEmail(email: String): Boolean
    
    fun countByIsActiveTrue(): Long
    
    // Custom JPQL queries for testing
    @Query("SELECT u FROM User u WHERE u.createdAt > :date")
    fun findUsersCreatedAfter(@Param("date") date: LocalDateTime): List<User>
    
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :start AND :end")
    fun findUsersCreatedBetween(
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime
    ): List<User>
    
    @Query("SELECT u FROM User u WHERE SIZE(u.posts) > :count")
    fun findUsersWithMoreThanXPosts(@Param("count") count: Int): List<User>
    
    @Query("SELECT u FROM User u WHERE SIZE(u.posts) = 0")
    fun findUsersWithNoPosts(): List<User>
    
    @Query("""
        SELECT u FROM User u 
        WHERE u.isActive = true 
        AND SIZE(u.posts) > 0 
        ORDER BY SIZE(u.posts) DESC
    """)
    fun findActiveUsersWithPosts(): List<User>
    
    @Query("""
        SELECT u FROM User u 
        JOIN u.posts p 
        WHERE p.status = 'PUBLISHED' 
        GROUP BY u 
        HAVING COUNT(p) > :minPosts
    """)
    fun findUsersWithPublishedPosts(@Param("minPosts") minPosts: Long): List<User>
    
    // Update queries for testing transaction behavior
    @Modifying
    @Query("UPDATE User u SET u.isActive = :active WHERE u.id = :id")
    fun updateUserStatus(@Param("id") id: Long, @Param("active") active: Boolean): Int
    
    @Modifying
    @Query("UPDATE User u SET u.isActive = false WHERE u.createdAt < :date")
    fun deactivateUsersCreatedBefore(@Param("date") date: LocalDateTime): Int
    
    // Pagination methods for testing
    fun findAllByOrderByUsernameAsc(pageable: Pageable): Page<User>
    
    fun findAllByOrderByCreatedAtDesc(pageable: Pageable): Page<User>
    
    fun findByIsActiveTrueOrderByCreatedAtDesc(pageable: Pageable): Page<User>
    
    // Native queries for testing
    @Query(
        value = "SELECT * FROM users WHERE email LIKE %:domain%",
        nativeQuery = true
    )
    fun findByEmailDomainNative(@Param("domain") domain: String): List<User>
    
    @Query(
        value = """
            SELECT u.*, COUNT(p.id) as post_count 
            FROM users u 
            LEFT JOIN posts p ON u.id = p.author_id 
            GROUP BY u.id 
            ORDER BY post_count DESC
        """,
        nativeQuery = true
    )
    fun findUsersWithPostCounts(): List<Any>
    
    // Complex queries for testing joins and aggregations
    @Query("""
        SELECT u FROM User u 
        LEFT JOIN FETCH u.posts p 
        WHERE u.isActive = true 
        AND (p.status = 'PUBLISHED' OR p IS NULL)
    """)
    fun findActiveUsersWithPublishedPosts(): List<User>
    
    @Query("""
        SELECT COUNT(DISTINCT u.id) FROM User u 
        JOIN u.posts p 
        WHERE p.status = 'PUBLISHED' 
        AND p.publishedAt > :since
    """)
    fun countUsersWithRecentPublishedPosts(@Param("since") since: LocalDateTime): Long
}