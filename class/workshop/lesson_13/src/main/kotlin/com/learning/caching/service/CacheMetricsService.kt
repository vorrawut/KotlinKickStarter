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
    
    // TODO: Implement cache statistics
    fun recordCacheHit(cacheName: String) {
        // TODO: Increment hit count for cache
        // TODO: Log cache hit
        hitCounts.merge(cacheName, 1L, Long::plus)
        logger.debug("Cache hit recorded for cache: $cacheName")
    }
    
    fun recordCacheMiss(cacheName: String) {
        // TODO: Increment miss count for cache
        // TODO: Log cache miss
        missCounts.merge(cacheName, 1L, Long::plus)
        logger.debug("Cache miss recorded for cache: $cacheName")
    }
    
    // TODO: Calculate hit ratio
    fun getHitRatio(cacheName: String): Double {
        // TODO: Calculate hits / (hits + misses)
        // TODO: Handle division by zero
        val hits = hitCounts[cacheName] ?: 0L
        val misses = missCounts[cacheName] ?: 0L
        val total = hits + misses
        
        return if (total == 0L) 0.0 else hits.toDouble() / total.toDouble()
    }
    
    // TODO: Get all cache statistics
    fun getAllCacheStats(): Map<String, Map<String, Any>> {
        // TODO: Return comprehensive cache statistics
        // TODO: Include hit counts, miss counts, hit ratios
        val stats = mutableMapOf<String, Map<String, Any>>()
        
        val allCacheNames = (hitCounts.keys + missCounts.keys).toSet()
        
        allCacheNames.forEach { cacheName ->
            val hits = hitCounts[cacheName] ?: 0L
            val misses = missCounts[cacheName] ?: 0L
            val hitRatio = getHitRatio(cacheName)
            
            stats[cacheName] = mapOf(
                "hits" to hits,
                "misses" to misses,
                "hitRatio" to hitRatio,
                "total" to (hits + misses)
            )
        }
        
        return stats
    }
    
    // TODO: Clear cache statistics
    fun clearStats() {
        // TODO: Reset all counters
        hitCounts.clear()
        missCounts.clear()
        logger.info("Cache statistics cleared")
    }
    
    fun getCacheNames(): Set<String> {
        return cacheManager.cacheNames
    }
}