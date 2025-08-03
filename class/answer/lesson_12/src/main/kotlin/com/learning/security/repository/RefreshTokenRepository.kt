/**
 * Lesson 12 Complete Solution: RefreshToken Repository
 * 
 * Complete repository for refresh token management, cleanup, and security operations
 */

package com.learning.security.repository

import com.learning.security.model.RefreshToken
import com.learning.security.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    
    // Basic token queries
    fun findByToken(token: String): RefreshToken?
    
    fun existsByToken(token: String): Boolean
    
    // User-specific token queries
    fun findByUser(user: User): List<RefreshToken>
    
    fun findByUserOrderByCreatedAtDesc(user: User): List<RefreshToken>
    
    fun findByUserAndIsRevokedFalse(user: User): List<RefreshToken>
    
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId")
    fun findByUserId(@Param("userId") userId: Long): List<RefreshToken>
    
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.username = :username")
    fun findByUsername(@Param("username") username: String): List<RefreshToken>
    
    // Active and valid token queries
    @Query("""
        SELECT rt FROM RefreshToken rt 
        WHERE rt.isRevoked = false 
        AND rt.expiryDate > :now
    """)
    fun findAllActiveTokens(@Param("now") now: LocalDateTime = LocalDateTime.now()): List<RefreshToken>
    
    @Query("""
        SELECT rt FROM RefreshToken rt 
        WHERE rt.user = :user 
        AND rt.isRevoked = false 
        AND rt.expiryDate > :now
    """)
    fun findActiveTokensByUser(
        @Param("user") user: User,
        @Param("now") now: LocalDateTime = LocalDateTime.now()
    ): List<RefreshToken>
    
    @Query("""
        SELECT rt FROM RefreshToken rt 
        WHERE rt.user.id = :userId 
        AND rt.isRevoked = false 
        AND rt.expiryDate > :now
    """)
    fun findActiveTokensByUserId(
        @Param("userId") userId: Long,
        @Param("now") now: LocalDateTime = LocalDateTime.now()
    ): List<RefreshToken>
    
    // Expired and cleanup queries
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiryDate < :now")
    fun findExpiredTokens(@Param("now") now: LocalDateTime = LocalDateTime.now()): List<RefreshToken>
    
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.isRevoked = true")
    fun findRevokedTokens(): List<RefreshToken>
    
    @Query("""
        SELECT rt FROM RefreshToken rt 
        WHERE rt.expiryDate < :cutoffDate 
        OR rt.isRevoked = true
    """)
    fun findTokensToCleanup(@Param("cutoffDate") cutoffDate: LocalDateTime): List<RefreshToken>
    
    // Count and statistics queries
    fun countByUser(user: User): Long
    
    fun countByUserAndIsRevokedFalse(user: User): Long
    
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.expiryDate < :now")
    fun countExpiredTokens(@Param("now") now: LocalDateTime = LocalDateTime.now()): Long
    
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.isRevoked = true")
    fun countRevokedTokens(): Long
    
    @Query("""
        SELECT COUNT(rt) FROM RefreshToken rt 
        WHERE rt.isRevoked = false 
        AND rt.expiryDate > :now
    """)
    fun countActiveTokens(@Param("now") now: LocalDateTime = LocalDateTime.now()): Long
    
    // Modifying operations
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.user = :user")
    fun revokeAllByUser(@Param("user") user: User): Int
    
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.user.id = :userId")
    fun revokeAllByUserId(@Param("userId") userId: Long): Int
    
    @Modifying
    @Transactional
    @Query("""
        UPDATE RefreshToken rt SET 
        rt.isRevoked = true, 
        rt.revokedAt = :revokedAt 
        WHERE rt.token = :token
    """)
    fun revokeToken(
        @Param("token") token: String,
        @Param("revokedAt") revokedAt: LocalDateTime = LocalDateTime.now()
    ): Int
    
    @Modifying
    @Transactional
    @Query("""
        UPDATE RefreshToken rt SET 
        rt.lastUsedAt = :usedAt 
        WHERE rt.token = :token
    """)
    fun updateLastUsedTime(
        @Param("token") token: String,
        @Param("usedAt") usedAt: LocalDateTime = LocalDateTime.now()
    ): Int
    
    // Cleanup operations
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :cutoffDate")
    fun deleteExpiredTokens(@Param("cutoffDate") cutoffDate: LocalDateTime): Int
    
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.isRevoked = true AND rt.revokedAt < :cutoffDate")
    fun deleteOldRevokedTokens(@Param("cutoffDate") cutoffDate: LocalDateTime): Int
    
    @Modifying
    @Transactional
    fun deleteByUser(user: User): Int
    
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId")
    fun deleteAllByUserId(@Param("userId") userId: Long): Int
    
    // Security and audit queries
    @Query("""
        SELECT rt FROM RefreshToken rt 
        WHERE rt.createdAt BETWEEN :start AND :end
    """)
    fun findTokensCreatedBetween(
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime
    ): List<RefreshToken>
    
    @Query("""
        SELECT rt FROM RefreshToken rt 
        WHERE rt.user = :user 
        AND rt.createdAt > :since
    """)
    fun findRecentTokensByUser(
        @Param("user") user: User,
        @Param("since") since: LocalDateTime
    ): List<RefreshToken>
    
    @Query("""
        SELECT rt.user.username, COUNT(rt) 
        FROM RefreshToken rt 
        WHERE rt.createdAt > :since 
        GROUP BY rt.user.username 
        ORDER BY COUNT(rt) DESC
    """)
    fun findMostActiveUsersSince(@Param("since") since: LocalDateTime): List<Array<Any>>
    
    // Advanced security queries
    @Query("""
        SELECT rt FROM RefreshToken rt 
        WHERE rt.clientInfo LIKE %:clientPattern% 
        AND rt.isRevoked = false
    """)
    fun findActiveTokensByClientPattern(@Param("clientPattern") clientPattern: String): List<RefreshToken>
    
    @Query("""
        SELECT COUNT(rt) FROM RefreshToken rt 
        WHERE rt.user = :user 
        AND rt.createdAt > :since 
        AND rt.isRevoked = false
    """)
    fun countActiveTokensForUserSince(
        @Param("user") user: User,
        @Param("since") since: LocalDateTime
    ): Long
}