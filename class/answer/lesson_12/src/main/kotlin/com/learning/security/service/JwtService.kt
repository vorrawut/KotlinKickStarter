/**
 * Lesson 12 Complete Solution: JWT Service
 * 
 * Complete JWT service for token generation, validation, and management
 * with comprehensive security features and claims handling
 */

package com.learning.security.service

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.io.Decoders
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails as SpringUserDetails
import org.springframework.security.core.GrantedAuthority
import org.springframework.stereotype.Service
import com.learning.security.model.User
import java.security.Key
import java.util.*
import javax.crypto.spec.SecretKeySpec

@Service
class JwtService {
    
    private val logger = LoggerFactory.getLogger(JwtService::class.java)
    
    @Value("\${jwt.secret}")
    private lateinit var secret: String
    
    @Value("\${jwt.expiration}")
    private var expiration: Long = 86400000 // 24 hours default
    
    private fun getJwtParser(): JwtParser {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
    }
    
    companion object {
        private const val CLAIM_USER_ID = "userId"
        private const val CLAIM_EMAIL = "email"
        private const val CLAIM_ROLES = "roles"
        private const val CLAIM_FULL_NAME = "fullName"
        private const val CLAIM_IS_ADMIN = "isAdmin"
    }
    
    /**
     * Generate JWT token for authenticated user
     */
    fun generateToken(userDetails: SpringUserDetails): String {
        val claims = buildClaims(userDetails)
        return createToken(claims, userDetails.username)
    }
    
    /**
     * Generate JWT token with custom expiration
     */
    fun generateToken(userDetails: SpringUserDetails, customExpirationMs: Long): String {
        val claims = buildClaims(userDetails)
        return createToken(claims, userDetails.username, customExpirationMs)
    }
    
    /**
     * Validate JWT token against user details
     */
    fun validateToken(token: String, userDetails: SpringUserDetails): Boolean {
        return try {
            val username = extractUsername(token)
            val isUsernameValid = username == userDetails.username
            val isTokenNotExpired = !isTokenExpired(token)
            val isTokenStructureValid = validateTokenStructure(token)
            
            logger.debug("Token validation - Username: $isUsernameValid, NotExpired: $isTokenNotExpired, Structure: $isTokenStructureValid")
            
            isUsernameValid && isTokenNotExpired && isTokenStructureValid
        } catch (e: Exception) {
            logger.warn("Token validation failed", e)
            false
        }
    }
    
    /**
     * Extract username from JWT token
     */
    fun extractUsername(token: String): String {
        return extractClaim(token, Claims::getSubject)
    }
    
    /**
     * Extract expiration date from JWT token
     */
    fun extractExpiration(token: String): Date {
        return extractClaim(token, Claims::getExpiration)
    }
    
    /**
     * Extract user ID from JWT token
     */
    fun extractUserId(token: String): Long? {
        return try {
            val userIdClaim = extractClaim(token) { claims -> claims[CLAIM_USER_ID] }
            when (userIdClaim) {
                is Number -> userIdClaim.toLong()
                is String -> userIdClaim.toLongOrNull()
                else -> null
            }
        } catch (e: Exception) {
            logger.warn("Failed to extract user ID from token", e)
            null
        }
    }
    
    /**
     * Extract email from JWT token
     */
    fun extractEmail(token: String): String? {
        return try {
            extractClaim(token) { claims -> claims[CLAIM_EMAIL] as? String }
        } catch (e: Exception) {
            logger.warn("Failed to extract email from token", e)
            null
        }
    }
    
    /**
     * Extract roles from JWT token
     */
    @Suppress("UNCHECKED_CAST")
    fun extractRoles(token: String): List<String> {
        return try {
            val rolesClaim = extractClaim(token) { claims -> claims[CLAIM_ROLES] }
            when (rolesClaim) {
                is List<*> -> rolesClaim.filterIsInstance<String>()
                else -> emptyList()
            }
        } catch (e: Exception) {
            logger.warn("Failed to extract roles from token", e)
            emptyList()
        }
    }
    
    /**
     * Check if user is admin from JWT token
     */
    fun isAdmin(token: String): Boolean {
        return try {
            extractClaim(token) { claims -> claims[CLAIM_IS_ADMIN] as? Boolean } ?: false
        } catch (e: Exception) {
            logger.warn("Failed to check admin status from token", e)
            false
        }
    }
    
    /**
     * Extract specific claim from JWT token
     */
    fun <T> extractClaim(token: String, claimsResolver: (Claims) -> T): T {
        val claims = extractAllClaims(token)
        return claimsResolver(claims)
    }
    
    /**
     * Check if JWT token is expired
     */
    fun isTokenExpired(token: String): Boolean {
        return try {
            extractExpiration(token).before(Date())
        } catch (e: Exception) {
            logger.warn("Failed to check token expiration", e)
            true
        }
    }
    
    /**
     * Get remaining time until token expiration (in milliseconds)
     */
    fun getTimeUntilExpiration(token: String): Long {
        return try {
            val expiration = extractExpiration(token)
            maxOf(0, expiration.time - System.currentTimeMillis())
        } catch (e: Exception) {
            logger.warn("Failed to get time until expiration", e)
            0
        }
    }
    
    /**
     * Refresh JWT token by creating new one with extended expiration
     */
    fun refreshToken(token: String): String? {
        return try {
            val claims = extractAllClaims(token)
            val subject = claims.subject
            
            if (subject.isNullOrBlank()) {
                logger.warn("Cannot refresh token: missing subject")
                return null
            }
            
            // Create new token with refreshed expiration but same claims
            val refreshedClaims = HashMap<String, Any>(claims)
            createToken(refreshedClaims, subject)
        } catch (e: Exception) {
            logger.warn("Failed to refresh token", e)
            null
        }
    }
    
    /**
     * Validate token structure and signature without checking expiration
     */
    fun validateTokenStructure(token: String): Boolean {
        return try {
            getJwtParser().parseClaimsJws(token)
            true
        } catch (e: ExpiredJwtException) {
            // Token is expired but structure is valid
            true
        } catch (e: Exception) {
            logger.warn("Token structure validation failed", e)
            false
        }
    }
    
    /**
     * Extract token type (if present in header)
     */
    fun extractTokenType(token: String): String? {
        return try {
            val header = getJwtParser().parseClaimsJws(token).header
            header["typ"] as? String
        } catch (e: Exception) {
            logger.warn("Failed to extract token type", e)
            null
        }
    }
    
    // Private helper methods
    
    private fun createToken(claims: Map<String, Any>, subject: String): String {
        return createToken(claims, subject, expiration)
    }
    
    private fun createToken(claims: Map<String, Any>, subject: String, expirationMs: Long): String {
        val now = Date()
        val expirationDate = Date(now.time + expirationMs)
        
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(now)
            .setExpiration(expirationDate)
            .setIssuer("kotlinkickstarter-security")
            .setAudience("kotlinkickstarter-app")
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact()
    }
    
    private fun buildClaims(userDetails: SpringUserDetails): Map<String, Any> {
        val claims = HashMap<String, Any>()
        
        // Add standard claims
        if (userDetails is CustomUserDetails) {
            userDetails.getId()?.let { claims[CLAIM_USER_ID] = it }
            claims[CLAIM_EMAIL] = userDetails.getEmail()
            claims[CLAIM_FULL_NAME] = userDetails.getUser().getFullName()
            claims[CLAIM_IS_ADMIN] = userDetails.getUser().isAdmin()
        }
        
        // Add roles
        val roles = userDetails.authorities.map { it.authority }
        claims[CLAIM_ROLES] = roles
        
        return claims
    }
    
    private fun extractAllClaims(token: String): Claims {
        return try {
            getJwtParser().parseClaimsJws(token).body
        } catch (e: ExpiredJwtException) {
            // Still return claims even if expired for refresh scenarios
            e.claims
        }
    }
    
    private fun getSigningKey(): Key {
        val keyBytes = secret.toByteArray()
        return Keys.hmacShaKeyFor(keyBytes)
    }
}

