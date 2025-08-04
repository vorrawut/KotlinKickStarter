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
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
// TODO: Add @EnableCaching annotation
@ConfigurationProperties(prefix = "cache.ttl")
class RedisConfig {
    
    // TODO: Add TTL properties
    var userProfiles: Long = 3600000    // 1 hour
    var blogPosts: Long = 1800000       // 30 minutes
    var postSearch: Long = 600000       // 10 minutes
    var userPosts: Long = 900000        // 15 minutes
    var popularPosts: Long = 7200000    // 2 hours
    
    // TODO: Create Redis connection factory
    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory {
        // TODO: Return LettuceConnectionFactory with proper configuration
        // HINT: Use RedisStandaloneConfiguration for localhost:6379
        val config = RedisStandaloneConfiguration()
        config.hostName = "localhost"
        config.port = 6379
        // config.password = RedisPassword.of("your-password") // if needed
        
        return LettuceConnectionFactory(config)
    }
    
    // TODO: Configure RedisTemplate for manual operations
    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        // TODO: Create RedisTemplate with JSON serialization
        // HINT: Use StringRedisSerializer for keys, GenericJackson2JsonRedisSerializer for values
        val template = RedisTemplate<String, Any>()
        
        // TODO: Set connection factory
        // TODO: Set key and value serializers
        // TODO: Call afterPropertiesSet()
        
        return template
    }
    
    // TODO: Configure cache manager with different TTLs
    @Bean
    fun cacheManager(connectionFactory: RedisConnectionFactory): CacheManager {
        // TODO: Create RedisCacheManager with custom TTL for each cache
        // HINT: Use RedisCacheConfiguration.defaultCacheConfig() as base
        // HINT: Create map of cache names to configurations with specific TTLs
        
        val config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(5)) // Default TTL
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(GenericJackson2JsonRedisSerializer()))
            .disableCachingNullValues()
        
        // TODO: Create cache configurations map with different TTLs
        val cacheConfigurations = mapOf<String, RedisCacheConfiguration>(
            // TODO: Add cache configurations with different TTLs
        )
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build()
    }
}