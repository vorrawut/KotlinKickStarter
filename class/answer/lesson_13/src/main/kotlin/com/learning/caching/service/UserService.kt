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
    
    @Cacheable("user-profiles", key = "#userId")
    fun getUserProfile(userId: Long): User? {
        logger.info("Fetching user profile for ID: $userId from database")
        return userRepository.findById(userId).orElse(null)
    }
    
    @CachePut("user-profiles", key = "#result.id")
    fun updateUser(user: User): User {
        logger.info("Updating user: ${user.id}")
        val updated = userRepository.save(user)
        logger.debug("Updated user cache for ID: ${updated.id}")
        return updated
    }
    
    @CacheEvict("user-profiles", key = "#userId")
    fun deleteUser(userId: Long) {
        logger.info("Deleting user: $userId")
        userRepository.deleteById(userId)
        logger.debug("Evicted user cache for ID: $userId")
    }
    
    @Cacheable("user-posts", key = "#userId + ':' + #page")
    fun getUserPosts(userId: Long, page: Int = 0): List<BlogPost> {
        logger.info("Fetching posts for user $userId, page $page")
        return blogPostRepository.findByAuthorId(userId, PageRequest.of(page, 10)).content
    }
    
    @CacheEvict("user-posts", allEntries = true)
    fun clearUserPostsCache(userId: Long) {
        logger.info("Clearing posts cache for user: $userId")
        // This method just triggers cache eviction
    }
    
    @Caching(evict = [
        CacheEvict("user-posts", key = "#user.id + ':*'"),
        CacheEvict("post-search", allEntries = true) // Clear search cache as user data changed
    ])
    fun createUser(user: User): User {
        logger.info("Creating new user: ${user.username}")
        val created = userRepository.save(user)
        logger.debug("Created user and cleared related caches")
        return created
    }
    
    @Cacheable("user-profiles", key = "'username:' + #username", unless = "#result == null")
    fun findByUsername(username: String): User? {
        logger.info("Finding user by username: $username")
        return userRepository.findByUsername(username)
    }
    
    @Cacheable("user-profiles", key = "'email:' + #email", unless = "#result == null")
    fun findByEmail(email: String): User? {
        logger.info("Finding user by email: $email")
        return userRepository.findByEmail(email)
    }
    
    // Complex caching with multiple conditions
    @Cacheable(
        value = ["user-search"], 
        key = "#query + ':' + #page",
        condition = "#query.length() >= 3", // Only cache if query is at least 3 characters
        unless = "#result.isEmpty()" // Don't cache empty results
    )
    fun searchUsers(query: String, page: Int = 0): List<User> {
        logger.info("Searching users with query: '$query', page: $page")
        return userRepository.searchUsers(query, PageRequest.of(page, 10)).content
    }
}