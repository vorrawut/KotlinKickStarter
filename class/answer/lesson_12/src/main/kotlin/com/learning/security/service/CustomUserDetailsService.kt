/**
 * Lesson 12 Complete Solution: Custom UserDetailsService
 * 
 * Complete implementation of Spring Security UserDetailsService
 * for loading user details during authentication
 */

package com.learning.security.service

import com.learning.security.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.core.userdetails.UserDetails as SpringUserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {
    
    private val logger = LoggerFactory.getLogger(CustomUserDetailsService::class.java)
    
    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): SpringUserDetails {
        logger.debug("Loading user by username: $username")
        
        if (username.isBlank()) {
            logger.warn("Attempted to load user with blank username")
            throw UsernameNotFoundException("Username cannot be blank")
        }
        
        return try {
            // Try to find user by username or email
            val user = userRepository.findByUsernameOrEmail(username, username)
                ?: throw UsernameNotFoundException("User not found with username or email: $username")
            
            logger.debug("User found: ${user.username}, active: ${user.isActive}, roles: ${user.getRoleNames()}")
            
            // Additional security checks
            if (!user.canLogin()) {
                logger.warn("User ${user.username} cannot login - account status check failed")
                throw UsernameNotFoundException("User account is not accessible")
            }
            
            CustomUserDetails(user)
            
        } catch (e: UsernameNotFoundException) {
            logger.warn("User not found: $username")
            throw e
        } catch (e: Exception) {
            logger.error("Error loading user: $username", e)
            throw UsernameNotFoundException("Error loading user: $username", e)
        }
    }
    
    /**
     * Load user by email specifically
     */
    fun loadUserByEmail(email: String): SpringUserDetails {
        logger.debug("Loading user by email: $email")
        
        if (email.isBlank()) {
            throw UsernameNotFoundException("Email cannot be blank")
        }
        
        val user = userRepository.findByEmail(email)
            ?: throw UsernameNotFoundException("User not found with email: $email")
        
        if (!user.canLogin()) {
            throw UsernameNotFoundException("User account is not accessible")
        }
        
        return CustomUserDetails(user)
    }
    
    /**
     * Load user by ID (useful for token-based operations)
     */
    fun loadUserById(userId: Long): SpringUserDetails {
        logger.debug("Loading user by ID: $userId")
        
        val user = userRepository.findById(userId)
            .orElseThrow { UsernameNotFoundException("User not found with ID: $userId") }
        
        if (!user.canLogin()) {
            throw UsernameNotFoundException("User account is not accessible")
        }
        
        return CustomUserDetails(user)
    }
    
    /**
     * Check if username exists
     */
    fun existsByUsername(username: String): Boolean {
        return userRepository.existsByUsername(username)
    }
    
    /**
     * Check if email exists
     */
    fun existsByEmail(email: String): Boolean {
        return userRepository.existsByEmail(email)
    }
    
    /**
     * Check if username or email exists
     */
    fun existsByUsernameOrEmail(username: String, email: String): Boolean {
        return userRepository.existsByUsernameOrEmail(username, email)
    }
}