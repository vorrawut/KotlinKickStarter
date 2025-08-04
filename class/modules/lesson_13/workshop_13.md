# 🚀 Lesson 13 Workshop: Caching with Redis

## 🎯 Workshop Objective

Transform a blog platform into a high-performance system by implementing strategic Redis caching. You'll cache user profiles, blog posts, and search results to achieve 90%+ performance improvements.

**⏱️ Estimated Time**: 30 minutes

---

## 🏗️ What You'll Build

### **Before**: Slow Database Queries
```kotlin
// Every request hits the database
fun getBlogPost(id: Long): BlogPost = blogRepository.findById(id) // 250ms
fun getUserProfile(id: Long): User = userRepository.findById(id) // 150ms
fun searchPosts(query: String): List<BlogPost> = blogRepository.search(query) // 500ms
```

### **After**: Lightning-Fast Cache Responses
```kotlin
@Cacheable("blog-posts", key = "#id")
fun getBlogPost(id: Long): BlogPost = blogRepository.findById(id) // 2ms after first call

@Cacheable("user-profiles", key = "#id") 
fun getUserProfile(id: Long): User = userRepository.findById(id) // 2ms after first call

@Cacheable("post-search", key = "#query + ':' + #page")
fun searchPosts(query: String, page: Int): List<BlogPost> = // 2ms after first call
```

**🎯 Performance Target**: 
- 90%+ faster response times for cached data
- 50%+ reduction in database queries
- Improved user experience with instant loading

---

## 📁 Project Structure

```
class/workshop/lesson_13/
├── build.gradle.kts          # ✅ Redis dependencies configured
├── src/main/
│   ├── kotlin/com/learning/caching/
│   │   ├── CachingApplication.kt
│   │   ├── config/
│   │   │   └── RedisConfig.kt        # TODO: Configure Redis & cache manager
│   │   ├── model/
│   │   │   ├── User.kt
│   │   │   ├── BlogPost.kt
│   │   │   └── Comment.kt
│   │   ├── repository/
│   │   │   ├── UserRepository.kt
│   │   │   ├── BlogPostRepository.kt
│   │   │   └── CommentRepository.kt
│   │   ├── service/
│   │   │   ├── UserService.kt        # TODO: Add @Cacheable annotations
│   │   │   ├── BlogService.kt        # TODO: Add cache operations
│   │   │   └── SearchService.kt      # TODO: Cache search results
│   │   ├── controller/
│   │   │   ├── UserController.kt
│   │   │   ├── BlogController.kt
│   │   │   └── SearchController.kt
│   │   └── dto/
│   │       └── CacheDTOs.kt
│   └── resources/
│       └── application.yml           # TODO: Redis configuration
└── src/test/
    └── kotlin/com/learning/caching/
        └── CachingApplicationTests.kt
```

---

## 🛠️ Step 1: Configure Redis Dependencies

### **Verify build.gradle.kts**
```kotlin
dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    
    // 🔥 Cache Dependencies - ADD THESE
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.apache.commons:commons-pool2") // Connection pooling
    
    // Kotlin & Jackson support
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    
    // Database
    runtimeOnly("com.h2database:h2")
    
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("it.ozimov:embedded-redis:0.7.3") // Embedded Redis for tests
}
```

---

## 🛠️ Step 2: Configure Redis Connection

### **📝 TODO: Update application.yml**

Add Redis configuration to `src/main/resources/application.yml`:

```yaml
spring:
  # Existing configuration...
  datasource:
    url: jdbc:h2:mem:caching_db
    driver-class-name: org.h2.Driver
    username: sa
    password: password
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  
  # 🔥 ADD REDIS CONFIGURATION
  redis:
    host: localhost
    port: 6379
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 1
        max-wait: 2000ms
  
  # 🔥 ADD CACHE CONFIGURATION
  cache:
    type: redis
    redis:
      time-to-live: 300000  # 5 minutes default
      cache-null-values: false
      key-prefix: "kickstarter:"
      use-key-prefix: true

# 🔥 ADD CUSTOM CACHE TTL SETTINGS
cache:
  ttl:
    user-profiles: 3600000    # 1 hour
    blog-posts: 1800000       # 30 minutes
    post-search: 600000       # 10 minutes
    user-posts: 900000        # 15 minutes
    popular-posts: 7200000    # 2 hours

logging:
  level:
    com.learning.caching: DEBUG
    org.springframework.cache: DEBUG
```

---

## 🛠️ Step 3: Create Redis Configuration

### **📝 TODO: Implement RedisConfig.kt**

Complete the Redis configuration in `src/main/kotlin/com/learning/caching/config/RedisConfig.kt`:

```kotlin
package com.learning.caching.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
@EnableCaching  // 🔥 TODO: Add this annotation
@ConfigurationProperties(prefix = "cache.ttl")
class RedisConfig {
    
    // 🔥 TODO: Add TTL properties
    var userProfiles: Long = 3600000    // 1 hour
    var blogPosts: Long = 1800000       // 30 minutes
    var postSearch: Long = 600000       // 10 minutes
    var userPosts: Long = 900000        // 15 minutes
    var popularPosts: Long = 7200000    // 2 hours
    
    // 🔥 TODO: Create Redis connection factory
    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory {
        // TODO: Return LettuceConnectionFactory with proper configuration
        // HINT: Use RedisStandaloneConfiguration for localhost:6379
    }
    
    // 🔥 TODO: Configure RedisTemplate for manual operations
    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        // TODO: Create RedisTemplate with JSON serialization
        // HINT: Use StringRedisSerializer for keys, GenericJackson2JsonRedisSerializer for values
    }
    
    // 🔥 TODO: Configure cache manager with different TTLs
    @Bean
    fun cacheManager(connectionFactory: RedisConnectionFactory): CacheManager {
        // TODO: Create RedisCacheManager with custom TTL for each cache
        // HINT: Use RedisCacheConfiguration.defaultCacheConfig() as base
        // HINT: Create map of cache names to configurations with specific TTLs
    }
}
```

**🎯 Implementation Hints:**
- Use `RedisStandaloneConfiguration` for single Redis instance
- Configure JSON serialization for human-readable cache values
- Set different TTLs for different types of data
- Disable caching of null values for better performance

---

## 🛠️ Step 4: Add Cache Annotations to Services

### **📝 TODO: Implement UserService Caching**

Update `src/main/kotlin/com/learning/caching/service/UserService.kt`:

```kotlin
package com.learning.caching.service

import com.learning.caching.model.User
import com.learning.caching.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.*
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {
    
    private val logger = LoggerFactory.getLogger(UserService::class.java)
    
    // 🔥 TODO: Add @Cacheable annotation
    // HINT: Use cache name "user-profiles", key = "#userId"
    fun getUserProfile(userId: Long): User? {
        logger.info("Fetching user profile for ID: $userId from database")
        return userRepository.findById(userId).orElse(null)
    }
    
    // 🔥 TODO: Add @CachePut annotation to update cache when user updated
    // HINT: Use same cache and key as getUserProfile
    fun updateUser(user: User): User {
        logger.info("Updating user: ${user.id}")
        val updated = userRepository.save(user)
        // TODO: Add cache annotation here
        return updated
    }
    
    // 🔥 TODO: Add @CacheEvict annotation to remove from cache when deleted
    // HINT: Use key = "#userId"
    fun deleteUser(userId: Long) {
        logger.info("Deleting user: $userId")
        userRepository.deleteById(userId)
        // TODO: Add cache annotation here
    }
    
    // 🔥 TODO: Add @Cacheable for user posts
    // HINT: Use cache "user-posts", key = "#userId + ':' + #page"
    fun getUserPosts(userId: Long, page: Int = 0): List<BlogPost> {
        logger.info("Fetching posts for user $userId, page $page")
        return blogPostRepository.findByAuthorId(userId, PageRequest.of(page, 10))
    }
    
    // 🔥 TODO: Add @CacheEvict to clear user posts when user updates profile
    // HINT: Use allEntries = true for "user-posts" cache
    fun clearUserPostsCache(userId: Long) {
        logger.info("Clearing posts cache for user: $userId")
        // This method just triggers cache eviction
    }
}
```

### **📝 TODO: Implement BlogService Caching**

Update `src/main/kotlin/com/learning/caching/service/BlogService.kt`:

```kotlin
package com.learning.caching.service

import com.learning.caching.model.BlogPost
import com.learning.caching.repository.BlogPostRepository
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.*
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class BlogService(
    private val blogPostRepository: BlogPostRepository
) {
    
    private val logger = LoggerFactory.getLogger(BlogService::class.java)
    
    // 🔥 TODO: Add @Cacheable annotation
    // HINT: Cache name "blog-posts", key = "#postId"
    fun getBlogPost(postId: Long): BlogPost? {
        logger.info("Fetching blog post ID: $postId from database")
        return blogPostRepository.findById(postId).orElse(null)
    }
    
    // 🔥 TODO: Add @CachePut to update cache
    // HINT: Use key = "#result.id" to use the returned post's ID
    fun updateBlogPost(post: BlogPost): BlogPost {
        logger.info("Updating blog post: ${post.id}")
        // TODO: Add cache annotation here
        return blogPostRepository.save(post)
    }
    
    // 🔥 TODO: Add @Caching with multiple cache evictions
    // HINT: Evict from "blog-posts" and clear "popular-posts"
    fun deleteBlogPost(postId: Long) {
        logger.info("Deleting blog post: $postId")
        blogPostRepository.deleteById(postId)
        // TODO: Add cache annotations here
    }
    
    // 🔥 TODO: Add @Cacheable for popular posts
    // HINT: Cache name "popular-posts", key = "'popular:' + #limit"
    fun getPopularPosts(limit: Int = 10): List<BlogPost> {
        logger.info("Fetching $limit popular posts from database")
        return blogPostRepository.findTopByOrderByViewCountDesc(PageRequest.of(0, limit))
    }
    
    // 🔥 TODO: Add @CacheEvict to refresh popular posts
    // HINT: Use allEntries = true for "popular-posts"
    fun refreshPopularPosts() {
        logger.info("Refreshing popular posts cache")
        // This method triggers cache refresh
    }
}
```

### **📝 TODO: Implement SearchService Caching**

Update `src/main/kotlin/com/learning/caching/service/SearchService.kt`:

```kotlin
package com.learning.caching.service

import com.learning.caching.model.BlogPost
import com.learning.caching.repository.BlogPostRepository
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.CacheEvict
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class SearchService(
    private val blogPostRepository: BlogPostRepository
) {
    
    private val logger = LoggerFactory.getLogger(SearchService::class.java)
    
    // 🔥 TODO: Add @Cacheable annotation
    // HINT: Cache name "post-search", complex key with query, page, and size
    // HINT: Use condition to only cache non-empty queries
    fun searchPosts(query: String, page: Int = 0, size: Int = 10): List<BlogPost> {
        logger.info("Searching posts with query: '$query', page: $page, size: $size")
        // TODO: Add cache annotation with conditions
        return if (query.isBlank()) {
            emptyList()
        } else {
            blogPostRepository.findByTitleContainingOrContentContaining(
                query, query, PageRequest.of(page, size)
            )
        }
    }
    
    // 🔥 TODO: Add @CacheEvict to clear search cache when new posts added
    // HINT: Clear all entries in "post-search" cache
    fun clearSearchCache() {
        logger.info("Clearing search cache")
        // TODO: Add cache annotation here
    }
    
    // 🔥 TODO: Add @Cacheable for tag-based search
    // HINT: Cache name "tag-search", key = "#tag + ':' + #page"
    fun searchByTag(tag: String, page: Int = 0): List<BlogPost> {
        logger.info("Searching posts by tag: '$tag', page: $page")
        // TODO: Add cache annotation here
        return blogPostRepository.findByTagsContaining(tag, PageRequest.of(page, 10))
    }
}
```

---

## 🛠️ Step 5: Implement Cache Monitoring

### **📝 TODO: Create CacheMetricsService**

Create `src/main/kotlin/com/learning/caching/service/CacheMetricsService.kt`:

```kotlin
package com.learning.caching.service

import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class CacheMetricsService(
    private val cacheManager: CacheManager
) {
    
    private val logger = LoggerFactory.getLogger(CacheMetricsService::class.java)
    private val hitCounts = ConcurrentHashMap<String, Long>()
    private val missCounts = ConcurrentHashMap<String, Long>()
    
    // 🔥 TODO: Implement cache statistics
    fun recordCacheHit(cacheName: String) {
        // TODO: Increment hit count for cache
        // TODO: Log cache hit
    }
    
    fun recordCacheMiss(cacheName: String) {
        // TODO: Increment miss count for cache
        // TODO: Log cache miss
    }
    
    // 🔥 TODO: Calculate hit ratio
    fun getHitRatio(cacheName: String): Double {
        // TODO: Calculate hits / (hits + misses)
        // TODO: Handle division by zero
        return 0.0
    }
    
    // 🔥 TODO: Get all cache statistics
    fun getAllCacheStats(): Map<String, Map<String, Any>> {
        // TODO: Return comprehensive cache statistics
        // TODO: Include hit counts, miss counts, hit ratios
        return emptyMap()
    }
    
    // 🔥 TODO: Clear cache statistics
    fun clearStats() {
        // TODO: Reset all counters
    }
}
```

---

## 🛠️ Step 6: Add Cache Management Endpoints

### **📝 TODO: Create CacheController**

Create `src/main/kotlin/com/learning/caching/controller/CacheController.kt`:

```kotlin
package com.learning.caching.controller

import com.learning.caching.service.CacheMetricsService
import org.springframework.cache.CacheManager
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/cache")
class CacheController(
    private val cacheManager: CacheManager,
    private val cacheMetricsService: CacheMetricsService
) {
    
    // 🔥 TODO: Endpoint to get all cache statistics
    @GetMapping("/stats")
    fun getCacheStats(): Map<String, Any> {
        // TODO: Return cache statistics and hit ratios
        return emptyMap()
    }
    
    // 🔥 TODO: Endpoint to clear specific cache
    @DeleteMapping("/{cacheName}")
    fun clearCache(@PathVariable cacheName: String): Map<String, String> {
        // TODO: Clear specified cache
        // TODO: Return success/failure message
        return emptyMap()
    }
    
    // 🔥 TODO: Endpoint to clear all caches
    @DeleteMapping("/all")
    fun clearAllCaches(): Map<String, String> {
        // TODO: Clear all caches
        // TODO: Clear metrics
        // TODO: Return success message
        return emptyMap()
    }
    
    // 🔥 TODO: Endpoint to get cache contents (for debugging)
    @GetMapping("/{cacheName}/contents")
    fun getCacheContents(@PathVariable cacheName: String): Map<String, Any?> {
        // TODO: Return all keys and values in specified cache
        // HINT: Use RedisTemplate to scan keys
        return emptyMap()
    }
}
```

---

## 🛠️ Step 7: Create Performance Test

### **📝 TODO: Implement Performance Test**

Update `src/test/kotlin/com/learning/caching/CachingApplicationTests.kt`:

```kotlin
package com.learning.caching

import com.learning.caching.service.BlogService
import com.learning.caching.service.UserService
import com.learning.caching.service.SearchService
import org.junit.jupiter.api.Test
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
    fun `contextLoads`() {
        // Basic Spring Boot test
    }
    
    // 🔥 TODO: Test cache performance
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
    }
    
    // 🔥 TODO: Test cache eviction
    @Test
    fun `should evict cache when user updated`() {
        // TODO: Get user to prime cache
        // TODO: Update user
        // TODO: Verify cache was evicted
        // TODO: Verify updated data is returned
    }
    
    // 🔥 TODO: Test search caching
    @Test
    fun `should cache search results`() {
        // TODO: Search for posts
        // TODO: Verify results are cached
        // TODO: Verify second search is faster
    }
}
```

---

## 🚀 Step 8: Test Your Implementation

### **1. Start Redis (if using Docker)**
```bash
docker run -d --name redis-cache -p 6379:6379 redis:7-alpine
```

### **2. Run the Application**
```bash
cd class/workshop/lesson_13
./gradlew bootRun
```

### **3. Test Cache Performance**

**Create some test data:**
```bash
# Create a user
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"john_doe","email":"john@example.com","firstName":"John","lastName":"Doe"}'

# Create a blog post
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{"title":"Redis Caching Guide","content":"Learn how to cache with Redis...","authorId":1}'
```

**Test caching:**
```bash
# First call (cache miss) - watch the logs
curl http://localhost:8080/api/users/1

# Second call (cache hit) - should be faster
curl http://localhost:8080/api/users/1

# Check cache statistics
curl http://localhost:8080/api/cache/stats

# Search posts (cache miss first time)
curl "http://localhost:8080/api/search?query=redis&page=0&size=5"

# Same search (cache hit)
curl "http://localhost:8080/api/search?query=redis&page=0&size=5"
```

### **4. Monitor Redis (Optional)**
```bash
# Connect to Redis CLI
redis-cli

# View cached keys
KEYS kickstarter:*

# View cache content
GET "kickstarter:user-profiles::1"

# Monitor Redis operations
MONITOR
```

---

## 🎯 Expected Results

After implementing caching, you should see:

### **Performance Improvements**
- **First database call**: 50-200ms (depending on query complexity)
- **Subsequent cached calls**: 1-5ms
- **90%+ performance improvement** for cached data

### **Log Output**
```
# Cache miss
2024-01-15 10:30:15.123 INFO  --- UserService: Fetching user profile for ID: 1 from database

# Cache hit (no database log)
2024-01-15 10:30:16.456 DEBUG --- Spring Cache: Cache hit for key 'user-profiles::1'
```

### **Cache Statistics**
```json
{
  "user-profiles": {
    "hits": 15,
    "misses": 3,
    "hitRatio": 0.83
  },
  "blog-posts": {
    "hits": 42,
    "misses": 8,
    "hitRatio": 0.84
  }
}
```

---

## 🏆 Challenge Extensions

### **🔥 Bonus Challenge 1: Cache Warming**
Implement a service that pre-loads popular content into cache on application startup.

### **🔥 Bonus Challenge 2: Advanced TTL**
Implement dynamic TTL based on content popularity - popular content gets longer cache time.

### **🔥 Bonus Challenge 3: Cache Health Check**
Create a health check endpoint that verifies Redis connectivity and cache performance.

### **🔥 Bonus Challenge 4: Distributed Cache Events**
Implement cache invalidation across multiple application instances.

---

## 🎓 Learning Outcomes

Upon completion, you'll have:

✅ **Implemented Redis caching** with Spring Boot and proper configuration  
✅ **Applied cache annotations** (@Cacheable, @CacheEvict, @CachePut) strategically  
✅ **Designed cache strategies** with appropriate TTL for different data types  
✅ **Built cache monitoring** with hit ratios and performance metrics  
✅ **Created management endpoints** for cache administration  
✅ **Achieved 90%+ performance improvement** for frequently accessed data

**🚀 Next Lesson**: Scheduled Tasks & Async Processing - handling background jobs and scheduled operations!