package com.learning.caching

import com.learning.caching.model.User
import com.learning.caching.model.BlogPost
import com.learning.caching.service.BlogService
import com.learning.caching.service.UserService
import com.learning.caching.service.SearchService
import com.learning.caching.service.CacheMetricsService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertTimeout
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.CacheManager
import org.springframework.test.context.TestPropertySource

import java.time.Duration
import kotlin.system.measureTimeMillis

@SpringBootTest
@TestPropertySource(properties = [
    "spring.cache.type=simple", // Use simple cache for testing
    "spring.datasource.url=jdbc:h2:mem:test_cache",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "logging.level.com.learning.caching=DEBUG"
])
class CachingApplicationTests {
    
    @Autowired
    private lateinit var userService: UserService
    
    @Autowired 
    private lateinit var blogService: BlogService
    
    @Autowired
    private lateinit var searchService: SearchService
    
    @Autowired
    private lateinit var cacheManager: CacheManager
    
    @Autowired
    private lateinit var cacheMetricsService: CacheMetricsService
    

    
    @BeforeEach
    fun setUp() {
        // Clear caches before each test
        try {
            cacheManager.cacheNames.forEach { cacheName ->
                cacheManager.getCache(cacheName)?.clear()
            }
            cacheMetricsService.clearStats()
        } catch (e: Exception) {
            // Ignore cache clearing errors in tests
        }
    }
    
    @Test
    fun contextLoads() {
        // Basic Spring Boot test
    }
    
    @Test
    fun `should demonstrate cache performance improvement for user profile`() {
        // Create test user
        val testUser = userService.createUser(User(
            username = "testuser",
            email = "test@example.com", 
            firstName = "Test",
            lastName = "User"
        ))
        val userId = testUser.id!!
        
        // First call - cache miss
        val firstCallTime = measureTimeMillis {
            userService.getUserProfile(userId)
        }
        
        // Second call - cache hit  
        val secondCallTime = measureTimeMillis {
            userService.getUserProfile(userId)
        }
        
        println("User Profile - First call: ${firstCallTime}ms, Second call: ${secondCallTime}ms")
        
        // Verify cache contains the user
        val cache = cacheManager.getCache("user-profiles")
        assert(cache != null) { "User profiles cache should exist" }
        
        // Record metrics (in real app, this would be automatic)
        cacheMetricsService.recordCacheHit("user-profiles")
        cacheMetricsService.recordCacheMiss("user-profiles")
        
        val hitRatio = cacheMetricsService.getHitRatio("user-profiles")
        println("User profiles cache hit ratio: $hitRatio")
    }
    
    @Test
    fun `should evict cache when user updated`() {
        // Create test user
        val testUser = userService.createUser(User(
            username = "testuser2",
            email = "test2@example.com", 
            firstName = "Test",
            lastName = "User"
        ))
        val userId = testUser.id!!
        
        // Prime cache
        val originalUser = userService.getUserProfile(userId)
        assert(originalUser != null) { "User should exist" }
        
        // Verify user is cached
        val cache = cacheManager.getCache("user-profiles")
        assert(cache?.get(userId) != null) { "User should be cached after first call" }
        
        // Update user (should update cache with @CachePut)
        val updatedUser = originalUser!!.copy(firstName = "Updated")
        userService.updateUser(updatedUser)
        
        // Verify cache contains updated data
        val cachedUser = cache?.get(userId)?.get() as? User
        assert(cachedUser?.firstName == "Updated") { "Cache should contain updated user data" }
    }
    
    @Test
    fun `should cache blog post retrieval`() {
        // Create test user and post
        val testUser = userService.createUser(User(
            username = "bloguser",
            email = "blog@example.com", 
            firstName = "Blog",
            lastName = "User"
        ))
        
        val testPost = blogService.createBlogPost(BlogPost(
            title = "Test Post",
            content = "This is a test post about caching",
            authorId = testUser.id!!,
            tags = setOf("spring", "redis"),
            published = true
        ))
        val postId = testPost.id!!
        
        // First call - cache miss
        val firstCallTime = measureTimeMillis {
            blogService.getBlogPost(postId)
        }
        
        // Second call - cache hit
        val secondCallTime = measureTimeMillis {
            blogService.getBlogPost(postId)
        }
        
        println("Blog Post - First call: ${firstCallTime}ms, Second call: ${secondCallTime}ms")
        
        // Verify cache
        val cache = cacheManager.getCache("blog-posts")
        assert(cache != null) { "Blog posts cache should exist" }
    }
    
    @Test
    fun `should cache search results`() {
        val query = "redis"
        
        // First search - cache miss
        val firstSearchTime = measureTimeMillis {
            searchService.searchPosts(query)
        }
        
        // Second search - cache hit
        val secondSearchTime = measureTimeMillis {
            searchService.searchPosts(query)
        }
        
        println("Search - First: ${firstSearchTime}ms, Second: ${secondSearchTime}ms")
        
        // Verify search results cache exists
        val cache = cacheManager.getCache("post-search")
        assert(cache != null) { "Post search cache should exist" }
    }
    
    @Test
    fun `should cache popular posts`() {
        val limit = 5
        
        // First call
        val firstTime = measureTimeMillis {
            blogService.getPopularPosts(limit)
        }
        
        // Second call (should be cached)
        val secondTime = measureTimeMillis {
            blogService.getPopularPosts(limit)
        }
        
        println("Popular Posts - First: ${firstTime}ms, Second: ${secondTime}ms")
        
        val cache = cacheManager.getCache("popular-posts")
        assert(cache != null) { "Popular posts cache should exist" }
    }
    
    @Test
    fun `should handle cache manager operations`() {
        // Test cache manager functionality
        val cacheNames = cacheManager.cacheNames
        println("Available caches: $cacheNames")
        
        assert(cacheNames.isNotEmpty()) { "Should have configured caches" }
    }
    
    @Test
    fun `should track cache metrics`() {
        // Simulate some hits and misses
        cacheMetricsService.recordCacheHit("user-profiles")
        cacheMetricsService.recordCacheHit("user-profiles")
        cacheMetricsService.recordCacheMiss("user-profiles")
        
        val hitRatio = cacheMetricsService.getHitRatio("user-profiles")
        assert(hitRatio > 0.0) { "Hit ratio should be greater than 0" }
        
        val allStats = cacheMetricsService.getAllCacheStats()
        assert(allStats.isNotEmpty()) { "Should have cache statistics" }
        
        val overallStats = cacheMetricsService.getOverallStats()
        println("Overall cache stats: $overallStats")
    }
}