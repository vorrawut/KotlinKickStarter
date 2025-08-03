/**
 * Lesson 12 Complete Solution: RefreshToken Entity
 * 
 * Complete RefreshToken entity for implementing secure token refresh mechanism
 * with expiration tracking and revocation support
 */

package com.learning.security.model

import jakarta.persistence.*
import jakarta.validation.constraints.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(
    name = "refresh_tokens",
    indexes = [
        Index(name = "idx_refresh_tokens_token", columnList = "token"),
        Index(name = "idx_refresh_tokens_user", columnList = "user_id"),
        Index(name = "idx_refresh_tokens_expiry", columnList = "expiry_date"),
        Index(name = "idx_refresh_tokens_revoked", columnList = "is_revoked")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_refresh_token", columnNames = ["token"])
    ]
)
data class RefreshToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(unique = true, nullable = false, length = 255)
    @NotBlank(message = "Token is required")
    val token: String,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    
    @Column(name = "expiry_date", nullable = false)
    @NotNull(message = "Expiry date is required")
    val expiryDate: LocalDateTime,
    
    @Column(name = "is_revoked", nullable = false)
    val isRevoked: Boolean = false,
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "revoked_at")
    val revokedAt: LocalDateTime? = null,
    
    @Column(name = "last_used_at")
    val lastUsedAt: LocalDateTime? = null,
    
    @Column(name = "client_info", length = 500)
    val clientInfo: String? = null // User agent, IP address, etc.
    
) {
    
    companion object {
        // Generate a secure random token
        fun generateToken(): String {
            return UUID.randomUUID().toString()
        }
        
        // Create a new refresh token for a user
        fun createForUser(user: User, expirationInMs: Long, clientInfo: String? = null): RefreshToken {
            return RefreshToken(
                token = generateToken(),
                user = user,
                expiryDate = LocalDateTime.now().plusNanos(expirationInMs * 1_000_000),
                clientInfo = clientInfo
            )
        }
    }
    
    // Token validation methods
    fun isExpired(): Boolean {
        return LocalDateTime.now().isAfter(expiryDate)
    }
    
    fun isValid(): Boolean {
        return !isRevoked && !isExpired()
    }
    
    fun isActive(): Boolean {
        return isValid()
    }
    
    // Token lifecycle methods
    fun revoke(): RefreshToken {
        return copy(
            isRevoked = true,
            revokedAt = LocalDateTime.now()
        )
    }
    
    fun markAsUsed(): RefreshToken {
        return copy(
            lastUsedAt = LocalDateTime.now()
        )
    }
    
    // Security and audit methods
    fun getTimeUntilExpiry(): Long {
        if (isExpired()) return 0
        return java.time.Duration.between(LocalDateTime.now(), expiryDate).toMillis()
    }
    
    fun getDaysUntilExpiry(): Long {
        if (isExpired()) return 0
        return java.time.Duration.between(LocalDateTime.now(), expiryDate).toDays()
    }
    
    fun getAgeInHours(): Long {
        return java.time.Duration.between(createdAt, LocalDateTime.now()).toHours()
    }
    
    fun getUsageCount(): Int {
        // In a real implementation, you might track usage in a separate table
        return if (lastUsedAt != null) 1 else 0
    }
    
    fun isRecentlyCreated(hoursThreshold: Int = 1): Boolean {
        val threshold = LocalDateTime.now().minusHours(hoursThreshold.toLong())
        return createdAt.isAfter(threshold)
    }
    
    fun isNearingExpiry(hoursThreshold: Int = 24): Boolean {
        val threshold = LocalDateTime.now().plusHours(hoursThreshold.toLong())
        return expiryDate.isBefore(threshold)
    }
    
    fun shouldBeRotated(hoursThreshold: Int = 168): Boolean { // 7 days
        return getAgeInHours() >= hoursThreshold
    }
    
    // Display and logging methods
    fun getTokenSummary(): String {
        return "RefreshToken(id=$id, user=${user.username}, " +
                "created=${createdAt.toLocalDate()}, " +
                "expires=${expiryDate.toLocalDate()}, " +
                "valid=${isValid()})"
    }
    
    fun getMaskedToken(): String {
        return if (token.length > 8) {
            "${token.take(4)}...${token.takeLast(4)}"
        } else {
            "****"
        }
    }
    
    // Client information helpers
    fun hasClientInfo(): Boolean = !clientInfo.isNullOrBlank()
    
    fun getClientSummary(): String {
        return clientInfo?.let { info ->
            // Extract meaningful information from client info
            val parts = info.split("|")
            when {
                parts.size >= 2 -> "IP: ${parts[0]}, Client: ${parts[1].take(50)}"
                else -> info.take(100)
            }
        } ?: "Unknown client"
    }
}