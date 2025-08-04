package com.learning.caching.service

import com.learning.caching.model.User
import com.learning.caching.model.BlogPost
import com.learning.caching.repository.UserRepository
import com.learning.caching.repository.BlogPostRepository
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.*
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val blogPostRepository: BlogPostRepository
) {
    
    private val logger = LoggerFactory.getLogger(UserService::class.java)
    
    // TODO: Add @Cacheable annotation
    // HINT: Use cache name "user-profiles", key = "#userId"
    fun getUserProfile(userId: Long): User? {
        logger.info("Fetching user profile for ID: $userId from database")
        return userRepository.findById(userId).orElse(null)
    }
    
    // TODO: Add @CachePut annotation to update cache when user updated
    // HINT: Use same cache and key as getUserProfile
    fun updateUser(user: User): User {
        logger.info("Updating user: ${user.id}")
        val updated = userRepository.save(user)
        // TODO: Add cache annotation here
        return updated
    }
    
    // TODO: Add @CacheEvict annotation to remove from cache when deleted
    // HINT: Use key = "#userId"
    fun deleteUser(userId: Long) {
        logger.info("Deleting user: $userId")
        userRepository.deleteById(userId)
        // TODO: Add cache annotation here
    }
    
    // TODO: Add @Cacheable for user posts
    // HINT: Use cache "user-posts", key = "#userId + ':' + #page"
    fun getUserPosts(userId: Long, page: Int = 0): List<BlogPost> {
        logger.info("Fetching posts for user $userId, page $page")
        return blogPostRepository.findByAuthorId(userId, PageRequest.of(page, 10)).content
    }
    
    // TODO: Add @CacheEvict to clear user posts when user updates profile
    // HINT: Use allEntries = true for "user-posts" cache
    fun clearUserPostsCache(userId: Long) {
        logger.info("Clearing posts cache for user: $userId")
        // This method just triggers cache eviction
    }
    
    fun createUser(user: User): User {
        logger.info("Creating new user: ${user.username}")
        return userRepository.save(user)
    }
    
    fun findByUsername(username: String): User? {
        logger.info("Finding user by username: $username")
        return userRepository.findByUsername(username)
    }
    
    fun findByEmail(email: String): User? {
        logger.info("Finding user by email: $email")
        return userRepository.findByEmail(email)
    }
}