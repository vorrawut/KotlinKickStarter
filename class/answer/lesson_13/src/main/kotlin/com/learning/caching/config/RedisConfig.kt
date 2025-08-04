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
@EnableCaching
@ConfigurationProperties(prefix = "cache.ttl")
class RedisConfig {
    
    // TTL properties
    var userProfiles: Long = 3600000    // 1 hour
    var blogPosts: Long = 1800000       // 30 minutes
    var postSearch: Long = 600000       // 10 minutes
    var userPosts: Long = 900000        // 15 minutes
    var popularPosts: Long = 7200000    // 2 hours
    var tagSearch: Long = 1200000       // 20 minutes
    
    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory {
        val config = RedisStandaloneConfiguration()
        config.hostName = "localhost"
        config.port = 6379
        // config.password = RedisPassword.of("your-password") // if needed
        
        return LettuceConnectionFactory(config)
    }
    
    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        
        template.connectionFactory = connectionFactory
        
        // Use String serialization for keys
        template.keySerializer = StringRedisSerializer()
        template.hashKeySerializer = StringRedisSerializer()
        
        // Use JSON serialization for values
        template.valueSerializer = GenericJackson2JsonRedisSerializer()
        template.hashValueSerializer = GenericJackson2JsonRedisSerializer()
        
        template.afterPropertiesSet()
        
        return template
    }
    
    @Bean
    fun cacheManager(connectionFactory: RedisConnectionFactory): CacheManager {
        val config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(5)) // Default TTL
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(GenericJackson2JsonRedisSerializer()))
            .disableCachingNullValues()
        
        // Create cache configurations with different TTLs
        val cacheConfigurations = mapOf(
            "user-profiles" to config.entryTtl(Duration.ofMillis(userProfiles)),
            "blog-posts" to config.entryTtl(Duration.ofMillis(blogPosts)),
            "post-search" to config.entryTtl(Duration.ofMillis(postSearch)),
            "user-posts" to config.entryTtl(Duration.ofMillis(userPosts)),
            "popular-posts" to config.entryTtl(Duration.ofMillis(popularPosts)),
            "tag-search" to config.entryTtl(Duration.ofMillis(tagSearch))
        )
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build()
    }
}