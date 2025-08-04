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
    
    @Cacheable(
        value = ["post-search"], 
        key = "#query + ':' + #page + ':' + #size",
        condition = "#query.length() >= 2", // Only cache if query is at least 2 characters
        unless = "#result.isEmpty()" // Don't cache empty results
    )
    fun searchPosts(query: String, page: Int = 0, size: Int = 10): List<BlogPost> {
        logger.info("Searching posts with query: '$query', page: $page, size: $size")
        return if (query.isBlank()) {
            emptyList()
        } else {
            blogPostRepository.findByTitleContainingOrContentContaining(
                query, query, PageRequest.of(page, size)
            ).content
        }
    }
    
    @CacheEvict("post-search", allEntries = true)
    fun clearSearchCache() {
        logger.info("Clearing search cache")
    }
    
    @Cacheable(
        value = ["tag-search"], 
        key = "#tag + ':' + #page",
        condition = "#tag.length() >= 2",
        unless = "#result.isEmpty()"
    )
    fun searchByTag(tag: String, page: Int = 0): List<BlogPost> {
        logger.info("Searching posts by tag: '$tag', page: $page")
        return blogPostRepository.findByTagsContaining(tag, PageRequest.of(page, 10)).content
    }
    
    @CacheEvict("tag-search", allEntries = true)
    fun clearTagSearchCache() {
        logger.info("Clearing tag search cache")
    }
    
    @Cacheable("recommendations", key = "#userId + ':' + #limit")
    fun getRecommendedPosts(userId: Long, limit: Int = 5): List<BlogPost> {
        logger.info("Getting recommended posts for user $userId")
        // Simple recommendation: just get popular posts
        // In a real system, this would be more sophisticated
        return blogPostRepository.findTopByOrderByViewCountDesc(PageRequest.of(0, limit)).content
    }
    
    // Advanced search with multiple filters
    @Cacheable(
        value = ["advanced-search"],
        key = "#query + ':' + #tags.toString() + ':' + #page",
        condition = "#query.length() >= 3 or !#tags.isEmpty()",
        unless = "#result.isEmpty()"
    )
    fun advancedSearch(query: String, tags: Set<String>, page: Int = 0): List<BlogPost> {
        logger.info("Advanced search - query: '$query', tags: $tags, page: $page")
        
        return when {
            query.isNotBlank() && tags.isNotEmpty() -> {
                // Search by both query and tags (simplified implementation)
                val queryResults = searchPosts(query, page)
                queryResults.filter { post -> 
                    tags.any { tag -> post.tags.contains(tag) }
                }
            }
            query.isNotBlank() -> searchPosts(query, page)
            tags.isNotEmpty() -> {
                // Search by tags only
                tags.flatMap { tag -> searchByTag(tag, page) }
                    .distinctBy { it.id }
            }
            else -> emptyList()
        }
    }
    
    @CacheEvict(value = ["post-search", "tag-search", "advanced-search", "recommendations"], allEntries = true)
    fun clearAllSearchCaches() {
        logger.info("Clearing all search-related caches")
    }
}