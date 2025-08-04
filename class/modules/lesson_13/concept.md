# 🚀 Lesson 13: Caching with Redis - Concept Guide

## 🎯 Learning Objectives

By the end of this lesson, you will:
- **Understand caching principles** and when to apply them effectively
- **Configure Redis** as a centralized cache store for Spring Boot applications
- **Implement cache annotations** (@Cacheable, @CacheEvict, @CachePut) for performance optimization
- **Design cache strategies** with appropriate TTL policies and eviction strategies
- **Monitor cache performance** with metrics and hit/miss ratios
- **Handle cache failures** gracefully with fallback mechanisms

---

## 🔍 Why Caching Matters

### **Performance Impact**
```kotlin
// Without caching: 500ms database query
fun getUserProfile(userId: Long): UserProfile {
    return userRepository.findByIdWithDetails(userId) // Slow DB query
}

// With caching: 2ms memory lookup after first call
@Cacheable("user-profiles", key = "#userId")
fun getUserProfile(userId: Long): UserProfile {
    return userRepository.findByIdWithDetails(userId) // Only called once
}
```

### **Real-World Benefits**
- **90%+ faster response times** for frequently accessed data
- **Reduced database load** and improved scalability
- **Better user experience** with instant data retrieval
- **Cost savings** through reduced infrastructure requirements

---

## 🏗️ Redis Fundamentals

### **What is Redis?**
**Redis (Remote Dictionary Server)** is an in-memory data structure store used as:
- **Cache**: Fast temporary storage for frequently accessed data
- **Message Broker**: Pub/sub messaging between services
- **Session Store**: Distributed session management
- **Database**: Persistent key-value storage

### **Redis Data Types**
```bash
# Strings - Simple key-value pairs
SET user:1001 "John Doe"
GET user:1001

# Hashes - Object-like structures
HSET user:1001:profile name "John" email "john@example.com"
HGET user:1001:profile name

# Lists - Ordered collections
LPUSH recent_posts:user:1001 "post:123" "post:124"
LRANGE recent_posts:user:1001 0 9

# Sets - Unique collections
SADD user:1001:tags "kotlin" "spring" "redis"
SMEMBERS user:1001:tags

# Sorted Sets - Ranked collections
ZADD leaderboard 1500 "user:1001" 1200 "user:1002"
ZREVRANGE leaderboard 0 9 WITHSCORES
```

### **Redis vs Other Caching Solutions**

| Feature | Redis | Caffeine (Local) | Hazelcast | Memcached |
|---------|--------|------------------|-----------|-----------|
| **Distribution** | ✅ Multi-server | ❌ Single JVM | ✅ Multi-server | ✅ Multi-server |
| **Data Types** | ✅ Rich types | ❌ Objects only | ✅ Rich types | ❌ Key-value only |
| **Persistence** | ✅ Optional | ❌ Memory only | ✅ Optional | ❌ Memory only |
| **Performance** | ⚡ Very fast | ⚡ Fastest | ⚡ Fast | ⚡ Very fast |
| **Complexity** | 🔶 Medium | 🟢 Simple | 🔴 Complex | 🟢 Simple |

---

## 🌟 Spring Boot Cache Annotations

### **@Cacheable - Cache Results**
```kotlin
@Service
class BlogService {
    
    @Cacheable("blog-posts", key = "#postId")
    fun getPost(postId: Long): BlogPost {
        logger.info("Fetching post $postId from database") // Only logged on cache miss
        return blogRepository.findById(postId).orElseThrow()
    }
    
    @Cacheable("blog-posts", key = "#author + ':' + #status")
    fun getPostsByAuthorAndStatus(author: String, status: PostStatus): List<BlogPost> {
        return blogRepository.findByAuthorAndStatus(author, status)
    }
    
    // Conditional caching - only cache successful results
    @Cacheable("user-posts", condition = "#userId > 0", unless = "#result.isEmpty()")
    fun getUserPosts(userId: Long): List<BlogPost> {
        return blogRepository.findByUserId(userId)
    }
}
```

### **@CacheEvict - Remove Cache Entries**
```kotlin
@Service
class BlogService {
    
    @CacheEvict("blog-posts", key = "#post.id")
    fun updatePost(post: BlogPost): BlogPost {
        val updated = blogRepository.save(post)
        logger.info("Evicted cache for post ${post.id}")
        return updated
    }
    
    @CacheEvict("blog-posts", allEntries = true)
    fun deleteAllPosts() {
        blogRepository.deleteAll()
        logger.info("Cleared entire blog-posts cache")
    }
    
    // Multiple cache evictions
    @Caching(evict = [
        CacheEvict("blog-posts", key = "#postId"),
        CacheEvict("user-posts", key = "#userId"),
        CacheEvict("featured-posts", allEntries = true)
    ])
    fun deletePost(postId: Long, userId: Long) {
        blogRepository.deleteById(postId)
    }
}
```

### **@CachePut - Always Update Cache**
```kotlin
@Service
class UserService {
    
    // Always executes method AND updates cache
    @CachePut("user-profiles", key = "#user.id")
    fun updateUserProfile(user: User): User {
        val updated = userRepository.save(user)
        logger.info("Updated cache for user ${user.id}")
        return updated
    }
    
    // Combine with @CacheEvict for complex scenarios
    @CachePut("user-profiles", key = "#result.id")
    @CacheEvict("user-search", allEntries = true)
    fun createUser(userRequest: CreateUserRequest): User {
        val user = User(
            username = userRequest.username,
            email = userRequest.email
        )
        return userRepository.save(user)
    }
}
```

---

## ⚙️ Redis Configuration in Spring Boot

### **Dependencies**
```kotlin
// build.gradle.kts
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    
    // Optional: Lettuce connection pooling
    implementation("org.apache.commons:commons-pool2")
    
    // Optional: JSON serialization
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
}
```

### **Application Configuration**
```yaml
# application.yml
spring:
  redis:
    host: localhost
    port: 6379
    password: ${REDIS_PASSWORD:}
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 1
        max-wait: 2000ms
  
  cache:
    type: redis
    redis:
      time-to-live: 300000 # 5 minutes default TTL
      cache-null-values: false
      key-prefix: "kickstarter:"
      use-key-prefix: true

# Cache-specific configurations
cache:
  ttl:
    user-profiles: 3600000    # 1 hour
    blog-posts: 1800000       # 30 minutes
    user-search: 600000       # 10 minutes
    featured-posts: 43200000  # 12 hours
```

### **Redis Configuration Class**
```kotlin
@Configuration
@EnableCaching
class RedisConfig {
    
    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory {
        return LettuceConnectionFactory(
            RedisStandaloneConfiguration().apply {
                hostName = "localhost"
                port = 6379
                // password = "your-redis-password"
            }
        )
    }
    
    @Bean
    fun redisTemplate(): RedisTemplate<String, Any> {
        return RedisTemplate<String, Any>().apply {
            connectionFactory = redisConnectionFactory()
            
            // Use JSON serialization for human-readable cache values
            keySerializer = StringRedisSerializer()
            valueSerializer = GenericJackson2JsonRedisSerializer()
            hashKeySerializer = StringRedisSerializer()
            hashValueSerializer = GenericJackson2JsonRedisSerializer()
            
            afterPropertiesSet()
        }
    }
    
    @Bean
    fun cacheManager(): CacheManager {
        val config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(5)) // Default TTL
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(GenericJackson2JsonRedisSerializer()))
            .disableCachingNullValues()
        
        val cacheConfigurations = mapOf(
            "user-profiles" to config.entryTtl(Duration.ofHours(1)),
            "blog-posts" to config.entryTtl(Duration.ofMinutes(30)),
            "user-search" to config.entryTtl(Duration.ofMinutes(10)),
            "featured-posts" to config.entryTtl(Duration.ofHours(12))
        )
        
        return RedisCacheManager.builder(redisConnectionFactory())
            .cacheDefaults(config)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build()
    }
}
```

---

## 🎯 Cache Strategy Design

### **Cache Patterns**

#### **1. Cache-Aside (Lazy Loading)**
```kotlin
@Service
class ProductService {
    
    @Cacheable("products", key = "#productId")
    fun getProduct(productId: Long): Product {
        // Only called on cache miss
        return productRepository.findById(productId).orElseThrow()
    }
    
    @CacheEvict("products", key = "#product.id")
    fun updateProduct(product: Product): Product {
        return productRepository.save(product)
    }
}
```

#### **2. Write-Through**
```kotlin
@Service
class InventoryService {
    
    @CachePut("inventory", key = "#item.productId")
    fun updateInventory(item: InventoryItem): InventoryItem {
        // Always writes to database AND cache
        return inventoryRepository.save(item)
    }
}
```

#### **3. Write-Behind (Write-Back)**
```kotlin
@Service
class ViewCountService {
    
    private val writeQueue = mutableMapOf<Long, Int>()
    
    fun incrementViewCount(postId: Long) {
        // Update cache immediately
        redisTemplate.opsForValue().increment("views:$postId")
        
        // Queue for batch database update
        writeQueue[postId] = writeQueue.getOrDefault(postId, 0) + 1
    }
    
    @Scheduled(fixedDelay = 60000) // Every minute
    fun flushViewCounts() {
        writeQueue.forEach { (postId, count) ->
            blogRepository.incrementViewCount(postId, count)
        }
        writeQueue.clear()
    }
}
```

### **TTL Strategy Guidelines**

| Data Type | TTL | Reasoning |
|-----------|-----|-----------|
| **User Profiles** | 1-4 hours | Changes infrequently, acceptable staleness |
| **Product Catalog** | 30-60 minutes | Regular updates, business critical |
| **Search Results** | 5-15 minutes | Dynamic content, personalized |
| **Static Content** | 24+ hours | Rarely changes, high traffic |
| **Session Data** | Session lifetime | Security and consistency critical |
| **Real-time Data** | 30 seconds - 2 minutes | Balance freshness vs performance |

---

## 📊 Cache Monitoring & Observability

### **Cache Metrics**
```kotlin
@Component
class CacheMetricsService {
    
    private val cacheHitCounter = Counter.builder("cache.hits")
        .description("Number of cache hits")
        .register(Metrics.globalRegistry)
    
    private val cacheMissCounter = Counter.builder("cache.misses")
        .description("Number of cache misses")
        .register(Metrics.globalRegistry)
    
    @EventListener
    fun handleCacheHit(event: CacheHitEvent) {
        cacheHitCounter.increment(
            Tags.of(
                "cache", event.cacheName,
                "key", event.key.toString()
            )
        )
    }
    
    @EventListener
    fun handleCacheMiss(event: CacheMissEvent) {
        cacheMissCounter.increment(
            Tags.of(
                "cache", event.cacheName,
                "key", event.key.toString()
            )
        )
    }
}
```

### **Cache Health Monitoring**
```kotlin
@Component
@ConfigurationProperties("cache.monitoring")
class CacheHealthIndicator : HealthIndicator {
    
    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, Any>
    
    override fun health(): Health {
        return try {
            val info = redisTemplate.connectionFactory?.connection?.info()
            val hitRatio = calculateHitRatio()
            
            when {
                hitRatio >= 0.8 -> Health.up()
                    .withDetail("hitRatio", hitRatio)
                    .withDetail("status", "Optimal performance")
                    .build()
                    
                hitRatio >= 0.6 -> Health.up()
                    .withDetail("hitRatio", hitRatio)
                    .withDetail("status", "Acceptable performance")
                    .withDetail("recommendation", "Consider cache warming")
                    .build()
                    
                else -> Health.down()
                    .withDetail("hitRatio", hitRatio)
                    .withDetail("status", "Poor cache performance")
                    .withDetail("action", "Review cache strategy")
                    .build()
            }
        } catch (e: Exception) {
            Health.down()
                .withDetail("error", e.message)
                .withException(e)
                .build()
        }
    }
    
    private fun calculateHitRatio(): Double {
        // Implementation to calculate cache hit ratio
        return 0.85 // Placeholder
    }
}
```

---

## 🛡️ Error Handling & Resilience

### **Cache Fallback Strategies**
```kotlin
@Service
class ResilientCacheService {
    
    @Cacheable("user-data", key = "#userId")
    @Retryable(value = [Exception::class], maxAttempts = 3)
    fun getUserData(userId: Long): UserData {
        return try {
            userRepository.findById(userId).orElseThrow()
        } catch (e: Exception) {
            logger.warn("Cache operation failed for user $userId", e)
            // Fallback to direct database call
            userRepository.findById(userId).orElseThrow()
        }
    }
    
    @Recover
    fun recoverGetUserData(ex: Exception, userId: Long): UserData {
        logger.error("All cache attempts failed for user $userId", ex)
        return userRepository.findById(userId).orElseThrow()
    }
}
```

### **Circuit Breaker Pattern**
```kotlin
@Component
class CircuitBreakerCacheService {
    
    private val circuitBreaker = CircuitBreaker.ofDefaults("redis-cache")
    
    fun getWithCircuitBreaker(key: String): Any? {
        return circuitBreaker.executeSupplier {
            redisTemplate.opsForValue().get(key)
        }
    }
    
    fun setWithCircuitBreaker(key: String, value: Any) {
        circuitBreaker.executeSupplier {
            redisTemplate.opsForValue().set(key, value)
            null
        }
    }
}
```

---

## 🧪 Testing Cache Implementations

### **Cache Testing Strategies**
```kotlin
@SpringBootTest
@TestPropertySource(properties = ["spring.cache.type=simple"])
class CacheServiceTest {
    
    @Autowired
    private lateinit var blogService: BlogService
    
    @Autowired
    private lateinit var cacheManager: CacheManager
    
    @Test
    fun `should cache blog post on first call`() {
        // Given
        val postId = 1L
        
        // When - First call
        val post1 = blogService.getPost(postId)
        val post2 = blogService.getPost(postId)
        
        // Then
        assertThat(post1).isEqualTo(post2)
        
        val cache = cacheManager.getCache("blog-posts")
        assertThat(cache?.get(postId)?.get()).isEqualTo(post1)
    }
    
    @Test
    fun `should evict cache when post updated`() {
        // Given
        val postId = 1L
        blogService.getPost(postId) // Prime cache
        
        // When
        val updatedPost = BlogPost(id = postId, title = "Updated Title")
        blogService.updatePost(updatedPost)
        
        // Then
        val cache = cacheManager.getCache("blog-posts")
        assertThat(cache?.get(postId)).isNull()
    }
}
```

### **Redis Integration Testing**
```kotlin
@SpringBootTest
@Testcontainers
class RedisIntegrationTest {
    
    companion object {
        @Container
        @JvmStatic
        val redis = GenericContainer<Nothing>("redis:7-alpine")
            .apply { withExposedPorts(6379) }
    }
    
    @DynamicPropertySource
    companion object {
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.redis.host", redis::getHost)
            registry.add("spring.redis.port", redis::getFirstMappedPort)
        }
    }
    
    @Test
    fun `should store and retrieve from Redis`() {
        // Test Redis connectivity and operations
    }
}
```

---

## 🎯 Best Practices

### **Do's**
- ✅ **Cache frequently accessed, slowly changing data**
- ✅ **Use appropriate TTL for each data type**
- ✅ **Monitor cache hit ratios and performance**
- ✅ **Implement fallback strategies for cache failures**
- ✅ **Use meaningful cache keys with proper namespacing**
- ✅ **Test cache behavior in integration tests**

### **Don'ts**
- ❌ **Don't cache frequently changing data**
- ❌ **Don't use cache for critical real-time data**
- ❌ **Don't ignore cache memory limits**
- ❌ **Don't cache large objects unnecessarily**
- ❌ **Don't forget to handle cache eviction**
- ❌ **Don't cache sensitive data without encryption**

### **Performance Tips**
- 🚀 **Warm critical caches on application startup**
- 🚀 **Use async cache warming for non-blocking performance**
- 🚀 **Implement cache-aside pattern for most use cases**
- 🚀 **Consider Redis clustering for high availability**
- 🚀 **Use Redis pipelines for bulk operations**

---

## 🚀 Real-World Cache Scenarios

### **E-commerce Product Catalog**
```kotlin
@Service
class ProductCatalogService {
    
    @Cacheable("products", key = "#productId")
    fun getProduct(productId: Long): Product {
        return productRepository.findById(productId).orElseThrow()
    }
    
    @Cacheable("product-search", key = "#category + ':' + #page + ':' + #size")
    fun searchProducts(category: String, page: Int, size: Int): Page<Product> {
        return productRepository.findByCategory(category, PageRequest.of(page, size))
    }
    
    @CacheEvict("product-search", allEntries = true)
    @CacheEvict("products", key = "#product.id")
    fun updateProduct(product: Product): Product {
        return productRepository.save(product)
    }
}
```

### **Social Media Feed**
```kotlin
@Service
class SocialFeedService {
    
    @Cacheable("user-feed", key = "#userId + ':' + #page")
    fun getUserFeed(userId: Long, page: Int): List<Post> {
        return feedRepository.getFeedForUser(userId, page)
    }
    
    @CacheEvict("user-feed", key = "#userId + ':*'") // Wildcard eviction
    fun refreshUserFeed(userId: Long) {
        // Clear all pages of user's feed
    }
    
    @Scheduled(fixedDelay = 300000) // 5 minutes
    fun preloadPopularFeeds() {
        val popularUsers = userRepository.findMostActiveUsers(100)
        popularUsers.forEach { user ->
            getUserFeed(user.id, 0) // Warm cache
        }
    }
}
```

---

## 🎓 Summary

Caching with Redis provides:

- **🚀 Performance**: 90%+ faster response times for cached data
- **📈 Scalability**: Reduced database load and improved throughput  
- **💰 Cost Efficiency**: Lower infrastructure requirements
- **🛡️ Resilience**: Graceful degradation when cache unavailable
- **📊 Observability**: Rich metrics and monitoring capabilities

**Key Takeaways**:
1. **Strategic Caching**: Cache the right data with appropriate TTL
2. **Spring Integration**: Leverage @Cacheable annotations for clean implementation
3. **Redis Power**: Use Redis's rich data types and performance
4. **Monitoring**: Track hit ratios and performance metrics
5. **Resilience**: Implement fallbacks and error handling

Next lesson: **Scheduled Tasks & Async Processing** for background job management!