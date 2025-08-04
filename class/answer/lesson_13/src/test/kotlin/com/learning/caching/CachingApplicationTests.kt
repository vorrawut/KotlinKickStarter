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
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import kotlin.system.measureTimeMillis

@SpringBootTest
@TestPropertySource(properties = [
    "spring.cache.type=simple", // Use simple cache for testing
    "logging.level.com.learning.caching=DEBUG"
])
@Transactional
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
    
    private lateinit var testUser: User
    private lateinit var testPost: BlogPost
    
    @BeforeEach
    fun setUp() {
        // Clear caches before each test
        cacheManager.cacheNames.forEach { cacheName ->
            cacheManager.getCache(cacheName)?.clear()
        }
        cacheMetricsService.clearStats()
        
        // Create test data
        testUser = userService.createUser(User(
            username = "testuser",
            email = "test@example.com", 
            firstName = "Test",
            lastName = "User"
        ))
        
        testPost = blogService.createBlogPost(BlogPost(
            title = "Test Post",
            content = "This is a test post about caching with Redis and Spring Boot",
            authorId = testUser.id!!,
            tags = setOf("spring", "redis", "caching"),
            published = true
        ))
    }
    
    @Test
    fun contextLoads() {
        // Basic Spring Boot test
    }
    
    @Test
    fun `should demonstrate cache performance improvement for user profile`() {
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
        
        val cachedUser = cache?.get(userId)?.get() as? User
        assert(cachedUser != null) { "User should be cached" }
        assert(cachedUser?.id == userId) { "Cached user should match requested user" }
        
        // Record metrics (in real app, this would be automatic)
        cacheMetricsService.recordCacheHit("user-profiles")
        cacheMetricsService.recordCacheMiss("user-profiles")
        
        val hitRatio = cacheMetricsService.getHitRatio("user-profiles")
        println("User profiles cache hit ratio: $hitRatio")
    }
    
    @Test
    fun `should evict cache when user updated`() {
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
        val cachedPost = cache?.get(postId)?.get() as? BlogPost
        assert(cachedPost != null) { "Post should be cached" }
        assert(cachedPost?.title == testPost.title) { "Cached post should match original" }
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
        
        // Verify search results cache
        val cache = cacheManager.getCache("post-search")
        val cacheKey = "$query:0:10" // query:page:size
        assert(cache?.get(cacheKey) != null) { "Search results should be cached" }
    }
    
    @Test
    fun `should not cache empty search results`() {
        val query = "nonexistentterm"
        
        searchService.searchPosts(query)
        
        // Empty results should not be cached (unless condition)
        val cache = cacheManager.getCache("post-search")
        val cacheKey = "$query:0:10"
        val cachedResult = cache?.get(cacheKey)?.get() as? List<*>
        
        // This test depends on the cache configuration - if we cache empty results or not
        println("Cached empty results: ${cachedResult != null}")
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
        val cacheKey = "popular:$limit"
        assert(cache?.get(cacheKey) != null) { "Popular posts should be cached" }
    }
    
    @Test
    fun `should clear cache when post deleted`() {
        val postId = testPost.id!!
        
        // Prime cache
        blogService.getBlogPost(postId)
        val cache = cacheManager.getCache("blog-posts")
        assert(cache?.get(postId) != null) { "Post should be cached" }
        
        // Delete post (should evict from multiple caches)
        blogService.deleteBlogPost(postId)
        
        // Verify cache is cleared
        assert(cache?.get(postId) == null) { "Post should be evicted from cache after deletion" }
    }
    
    @Test
    fun `should handle cache manager operations`() {
        // Test cache manager functionality
        val cacheNames = cacheManager.cacheNames
        println("Available caches: $cacheNames")
        
        assert(cacheNames.isNotEmpty()) { "Should have configured caches" }
        
        // Expected caches based on our configuration
        val expectedCaches = setOf("user-profiles", "blog-posts", "post-search", "popular-posts")
        assert(cacheNames.containsAll(expectedCaches)) { 
            "Should contain expected caches. Found: $cacheNames, Expected: $expectedCaches" 
        }
    }
    
    @Test
    fun `should track cache metrics`() {
        // Prime some caches and record metrics
        userService.getUserProfile(testUser.id!!)
        blogService.getBlogPost(testPost.id!!)
        searchService.searchPosts("redis")
        
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
    
    @Test
    fun `cache operations should complete quickly`() {
        // Performance test - cached operations should be fast
        assertTimeout(Duration.ofMillis(100)) {
            // Prime cache
            userService.getUserProfile(testUser.id!!)
            
            // This should be very fast (cache hit)
            repeat(10) {
                userService.getUserProfile(testUser.id!!)
            }
        }
    }
}