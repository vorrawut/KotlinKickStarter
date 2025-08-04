package com.learning.caching.service

import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Service
class CacheMetricsService(
    private val cacheManager: CacheManager
) {
    
    private val logger = LoggerFactory.getLogger(CacheMetricsService::class.java)
    private val hitCounts = ConcurrentHashMap<String, AtomicLong>()
    private val missCounts = ConcurrentHashMap<String, AtomicLong>()
    
    fun recordCacheHit(cacheName: String) {
        hitCounts.computeIfAbsent(cacheName) { AtomicLong(0) }.incrementAndGet()
        logger.debug("Cache hit recorded for cache: $cacheName")
    }
    
    fun recordCacheMiss(cacheName: String) {
        missCounts.computeIfAbsent(cacheName) { AtomicLong(0) }.incrementAndGet()
        logger.debug("Cache miss recorded for cache: $cacheName")
    }
    
    fun getHitRatio(cacheName: String): Double {
        val hits = hitCounts[cacheName]?.get() ?: 0L
        val misses = missCounts[cacheName]?.get() ?: 0L
        val total = hits + misses
        
        return if (total == 0L) 0.0 else hits.toDouble() / total.toDouble()
    }
    
    fun getAllCacheStats(): Map<String, Map<String, Any>> {
        val stats = mutableMapOf<String, Map<String, Any>>()
        
        val allCacheNames = (hitCounts.keys + missCounts.keys + cacheManager.cacheNames.toSet()).toSet()
        
        allCacheNames.forEach { cacheName ->
            val hits = hitCounts[cacheName]?.get() ?: 0L
            val misses = missCounts[cacheName]?.get() ?: 0L
            val hitRatio = getHitRatio(cacheName)
            val total = hits + misses
            
            stats[cacheName] = mapOf(
                "hits" to hits,
                "misses" to misses,
                "hitRatio" to String.format("%.2f", hitRatio),
                "total" to total,
                "exists" to (cacheManager.getCache(cacheName) != null)
            )
        }
        
        return stats
    }
    
    fun clearStats() {
        hitCounts.clear()
        missCounts.clear()
        logger.info("Cache statistics cleared")
    }
    
    fun getCacheNames(): Set<String> {
        return cacheManager.cacheNames.toSet()
    }
    
    fun getOverallStats(): Map<String, Any> {
        val totalHits = hitCounts.values.sumOf { it.get() }
        val totalMisses = missCounts.values.sumOf { it.get() }
        val totalRequests = totalHits + totalMisses
        val overallHitRatio = if (totalRequests == 0L) 0.0 else totalHits.toDouble() / totalRequests.toDouble()
        
        return mapOf(
            "totalHits" to totalHits,
            "totalMisses" to totalMisses,
            "totalRequests" to totalRequests,
            "overallHitRatio" to String.format("%.2f", overallHitRatio),
            "activeCaches" to cacheManager.cacheNames.size,
            "monitoredCaches" to (hitCounts.keys + missCounts.keys).size
        )
    }
    
    fun getCacheInfo(cacheName: String): Map<String, Any> {
        val cache = cacheManager.getCache(cacheName)
        val hits = hitCounts[cacheName]?.get() ?: 0L
        val misses = missCounts[cacheName]?.get() ?: 0L
        
        return mapOf(
            "name" to cacheName,
            "exists" to (cache != null),
            "hits" to hits,
            "misses" to misses,
            "hitRatio" to getHitRatio(cacheName),
            "total" to (hits + misses)
        )
    }
}