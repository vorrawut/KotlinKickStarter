package com.learning.caching.dto

import java.time.LocalDateTime

data class UserProfileDTO(
    val id: Long,
    val username: String,
    val email: String,
    val fullName: String,
    val displayName: String,
    val createdAt: LocalDateTime
)

data class BlogPostSummaryDTO(
    val id: Long,
    val title: String,
    val preview: String,
    val authorName: String,
    val tags: Set<String>,
    val viewCount: Long,
    val readingTimeMinutes: Int,
    val createdAt: LocalDateTime
)

data class SearchResultDTO(
    val posts: List<BlogPostSummaryDTO>,
    val query: String,
    val page: Int,
    val size: Int,
    val totalResults: Int,
    val searchTimeMs: Long
)

data class CacheStatDTO(
    val cacheName: String,
    val hits: Long,
    val misses: Long,
    val hitRatio: Double,
    val totalRequests: Long
)

data class CacheStatsResponse(
    val cacheStats: List<CacheStatDTO>,
    val overallHitRatio: Double,
    val totalHits: Long,
    val totalMisses: Long,
    val generatedAt: LocalDateTime
)