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
    
    // TODO: Add @Cacheable annotation
    // HINT: Cache name "blog-posts", key = "#postId"
    fun getBlogPost(postId: Long): BlogPost? {
        logger.info("Fetching blog post ID: $postId from database")
        return blogPostRepository.findById(postId).orElse(null)
    }
    
    // TODO: Add @CachePut to update cache
    // HINT: Use key = "#result.id" to use the returned post's ID
    fun updateBlogPost(post: BlogPost): BlogPost {
        logger.info("Updating blog post: ${post.id}")
        // TODO: Add cache annotation here
        return blogPostRepository.save(post)
    }
    
    // TODO: Add @Caching with multiple cache evictions
    // HINT: Evict from "blog-posts" and clear "popular-posts"
    fun deleteBlogPost(postId: Long) {
        logger.info("Deleting blog post: $postId")
        blogPostRepository.deleteById(postId)
        // TODO: Add cache annotations here
    }
    
    // TODO: Add @Cacheable for popular posts
    // HINT: Cache name "popular-posts", key = "'popular:' + #limit"
    fun getPopularPosts(limit: Int = 10): List<BlogPost> {
        logger.info("Fetching $limit popular posts from database")
        return blogPostRepository.findTopByOrderByViewCountDesc(PageRequest.of(0, limit)).content
    }
    
    // TODO: Add @CacheEvict to refresh popular posts
    // HINT: Use allEntries = true for "popular-posts"
    fun refreshPopularPosts() {
        logger.info("Refreshing popular posts cache")
        // This method triggers cache refresh
    }
    
    fun createBlogPost(post: BlogPost): BlogPost {
        logger.info("Creating new blog post: ${post.title}")
        return blogPostRepository.save(post)
    }
    
    fun getRecentPosts(page: Int = 0, size: Int = 10): List<BlogPost> {
        logger.info("Fetching recent posts, page: $page, size: $size")
        return blogPostRepository.findRecentPublishedPosts(PageRequest.of(page, size)).content
    }
    
    fun getPostsByAuthor(authorId: Long, page: Int = 0): List<BlogPost> {
        logger.info("Fetching posts by author $authorId, page: $page")
        return blogPostRepository.findByAuthorId(authorId, PageRequest.of(page, 10)).content
    }
}