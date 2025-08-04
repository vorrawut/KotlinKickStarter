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
    
    // TODO: Add @Cacheable annotation
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
            ).content
        }
    }
    
    // TODO: Add @CacheEvict to clear search cache when new posts added
    // HINT: Clear all entries in "post-search" cache
    fun clearSearchCache() {
        logger.info("Clearing search cache")
        // TODO: Add cache annotation here
    }
    
    // TODO: Add @Cacheable for tag-based search
    // HINT: Cache name "tag-search", key = "#tag + ':' + #page"
    fun searchByTag(tag: String, page: Int = 0): List<BlogPost> {
        logger.info("Searching posts by tag: '$tag', page: $page")
        // TODO: Add cache annotation here
        return blogPostRepository.findByTagsContaining(tag, PageRequest.of(page, 10)).content
    }
    
    fun getRecommendedPosts(userId: Long, limit: Int = 5): List<BlogPost> {
        logger.info("Getting recommended posts for user $userId")
        // Simple recommendation: just get popular posts
        return blogPostRepository.findTopByOrderByViewCountDesc(PageRequest.of(0, limit)).content
    }
}