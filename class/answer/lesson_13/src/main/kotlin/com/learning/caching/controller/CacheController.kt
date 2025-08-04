package com.learning.caching.controller

import com.learning.caching.service.CacheMetricsService
import org.springframework.cache.CacheManager
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/cache")
class CacheController(
    private val cacheManager: CacheManager,
    private val cacheMetricsService: CacheMetricsService,
    private val redisTemplate: RedisTemplate<String, Any>
) {
    
    // TODO: Endpoint to get all cache statistics
    @GetMapping("/stats")
    fun getCacheStats(): Map<String, Any> {
        // TODO: Return cache statistics and hit ratios
        val stats = cacheMetricsService.getAllCacheStats()
        val cacheNames = cacheMetricsService.getCacheNames()
        
        return mapOf(
            "caches" to stats,
            "availableCaches" to cacheNames,
            "totalCaches" to cacheNames.size
        )
    }
    
    // TODO: Endpoint to clear specific cache
    @DeleteMapping("/{cacheName}")
    fun clearCache(@PathVariable cacheName: String): Map<String, String> {
        // TODO: Clear specified cache
        // TODO: Return success/failure message
        return try {
            val cache = cacheManager.getCache(cacheName)
            if (cache != null) {
                cache.clear()
                mapOf(
                    "status" to "success",
                    "message" to "Cache '$cacheName' cleared successfully"
                )
            } else {
                mapOf(
                    "status" to "error",
                    "message" to "Cache '$cacheName' not found"
                )
            }
        } catch (e: Exception) {
            mapOf(
                "status" to "error",
                "message" to "Failed to clear cache '$cacheName': ${e.message}"
            )
        }
    }
    
    // TODO: Endpoint to clear all caches
    @DeleteMapping("/all")
    fun clearAllCaches(): Map<String, String> {
        // TODO: Clear all caches
        // TODO: Clear metrics
        // TODO: Return success message
        return try {
            cacheManager.cacheNames.forEach { cacheName ->
                cacheManager.getCache(cacheName)?.clear()
            }
            cacheMetricsService.clearStats()
            
            mapOf(
                "status" to "success",
                "message" to "All caches cleared successfully",
                "clearedCaches" to cacheManager.cacheNames.joinToString(", ")
            )
        } catch (e: Exception) {
            mapOf(
                "status" to "error",
                "message" to "Failed to clear all caches: ${e.message}"
            )
        }
    }
    
    // TODO: Endpoint to get cache contents (for debugging)
    @GetMapping("/{cacheName}/contents")
    fun getCacheContents(@PathVariable cacheName: String): Map<String, Any?> {
        // TODO: Return all keys and values in specified cache
        // HINT: Use RedisTemplate to scan keys
        return try {
            val keys = redisTemplate.keys("kickstarter:$cacheName::*")
            val contents = mutableMapOf<String, Any?>()
            
            keys.forEach { key ->
                val value = redisTemplate.opsForValue().get(key)
                contents[key] = value
            }
            
            mapOf(
                "cacheName" to cacheName,
                "keyCount" to keys.size,
                "contents" to contents
            )
        } catch (e: Exception) {
            mapOf(
                "error" to "Failed to get cache contents: ${e.message}"
            )
        }
    }
    
    @GetMapping("/info")
    fun getCacheInfo(): Map<String, Any> {
        return mapOf(
            "cacheManager" to cacheManager.javaClass.simpleName,
            "availableCaches" to cacheManager.cacheNames,
            "cacheCount" to cacheManager.cacheNames.size
        )
    }
}