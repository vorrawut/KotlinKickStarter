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
    
    @Cacheable("blog-posts", key = "#postId", unless = "#result == null")
    fun getBlogPost(postId: Long): BlogPost? {
        logger.info("Fetching blog post ID: $postId from database")
        return blogPostRepository.findById(postId).orElse(null)
    }
    
    @CachePut("blog-posts", key = "#result.id")
    @CacheEvict("popular-posts", allEntries = true) // Clear popular posts as this might affect rankings
    fun updateBlogPost(post: BlogPost): BlogPost {
        logger.info("Updating blog post: ${post.id}")
        val updated = blogPostRepository.save(post)
        logger.debug("Updated blog post cache for ID: ${updated.id}")
        return updated
    }
    
    @Caching(evict = [
        CacheEvict("blog-posts", key = "#postId"),
        CacheEvict("popular-posts", allEntries = true),
        CacheEvict("post-search", allEntries = true),
        CacheEvict("user-posts", allEntries = true)
    ])
    fun deleteBlogPost(postId: Long) {
        logger.info("Deleting blog post: $postId")
        blogPostRepository.deleteById(postId)
        logger.debug("Deleted blog post and cleared all related caches")
    }
    
    @Cacheable("popular-posts", key = "'popular:' + #limit")
    fun getPopularPosts(limit: Int = 10): List<BlogPost> {
        logger.info("Fetching $limit popular posts from database")
        return blogPostRepository.findTopByOrderByViewCountDesc(PageRequest.of(0, limit)).content
    }
    
    @CacheEvict("popular-posts", allEntries = true)
    fun refreshPopularPosts() {
        logger.info("Refreshing popular posts cache")
        // This method triggers cache refresh
    }
    
    @Caching(evict = [
        CacheEvict("post-search", allEntries = true),
        CacheEvict("popular-posts", allEntries = true),
        CacheEvict("user-posts", allEntries = true)
    ])
    fun createBlogPost(post: BlogPost): BlogPost {
        logger.info("Creating new blog post: ${post.title}")
        val created = blogPostRepository.save(post)
        logger.debug("Created blog post and cleared related caches")
        return created
    }
    
    @Cacheable("recent-posts", key = "#page + ':' + #size")
    fun getRecentPosts(page: Int = 0, size: Int = 10): List<BlogPost> {
        logger.info("Fetching recent posts, page: $page, size: $size")
        return blogPostRepository.findRecentPublishedPosts(PageRequest.of(page, size)).content
    }
    
    @Cacheable("user-posts", key = "#authorId + ':author:' + #page")
    fun getPostsByAuthor(authorId: Long, page: Int = 0): List<BlogPost> {
        logger.info("Fetching posts by author $authorId, page: $page")
        return blogPostRepository.findByAuthorId(authorId, PageRequest.of(page, 10)).content
    }
    
    // Conditional caching - only cache if post is published
    @Cacheable(
        value = ["blog-posts"], 
        key = "'published:' + #postId",
        condition = "#postId > 0",
        unless = "#result == null or !#result.published"
    )
    fun getPublishedPost(postId: Long): BlogPost? {
        logger.info("Fetching published post ID: $postId")
        val post = blogPostRepository.findById(postId).orElse(null)
        return if (post?.published == true) post else null
    }
}