package com.learning.caching

import com.learning.caching.service.BlogService
import com.learning.caching.service.UserService
import com.learning.caching.service.SearchService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.CacheManager
import org.springframework.test.context.TestPropertySource
import kotlin.system.measureTimeMillis

@SpringBootTest
@TestPropertySource(properties = [
    "spring.cache.type=simple", // Use simple cache for testing
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
    
    @Test
    fun contextLoads() {
        // Basic Spring Boot test
    }
    
    // TODO: Test cache performance
    @Test
    fun `should demonstrate cache performance improvement`() {
        val userId = 1L
        
        // First call - cache miss
        val firstCallTime = measureTimeMillis {
            userService.getUserProfile(userId)
        }
        
        // Second call - cache hit  
        val secondCallTime = measureTimeMillis {
            userService.getUserProfile(userId)
        }
        
        // TODO: Assert that second call is faster
        // TODO: Verify cache contains the user
        println("First call: ${firstCallTime}ms, Second call: ${secondCallTime}ms")
        
        // In a real test, you would assert:
        // assertThat(secondCallTime).isLessThan(firstCallTime)
        
        // Check if cache exists
        val cache = cacheManager.getCache("user-profiles")
        println("Cache exists: ${cache != null}")
    }
    
    // TODO: Test cache eviction
    @Test
    fun `should evict cache when user updated`() {
        // TODO: Get user to prime cache
        // TODO: Update user
        // TODO: Verify cache was evicted
        // TODO: Verify updated data is returned
        
        println("TODO: Implement cache eviction test")
    }
    
    // TODO: Test search caching
    @Test
    fun `should cache search results`() {
        // TODO: Search for posts
        // TODO: Verify results are cached
        // TODO: Verify second search is faster
        
        val query = "kotlin"
        
        val firstSearchTime = measureTimeMillis {
            searchService.searchPosts(query)
        }
        
        val secondSearchTime = measureTimeMillis {
            searchService.searchPosts(query)
        }
        
        println("First search: ${firstSearchTime}ms, Second search: ${secondSearchTime}ms")
        // TODO: Add proper assertions
    }
    
    @Test
    fun `cache manager should be configured`() {
        val cacheNames = cacheManager.cacheNames
        println("Available caches: $cacheNames")
        
        // TODO: Assert expected caches exist
        // Example:
        // assertThat(cacheNames).contains("user-profiles", "blog-posts", "post-search")
    }
}