package com.learning.caching.controller

import com.learning.caching.model.BlogPost
import com.learning.caching.service.BlogService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/posts")
class BlogController(
    private val blogService: BlogService
) {
    
    @GetMapping("/{id}")
    fun getBlogPost(@PathVariable id: Long): ResponseEntity<BlogPost> {
        val post = blogService.getBlogPost(id)
        return if (post != null) {
            ResponseEntity.ok(post)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @PostMapping
    fun createBlogPost(@RequestBody createRequest: CreateBlogPostRequest): ResponseEntity<BlogPost> {
        val post = BlogPost(
            title = createRequest.title,
            content = createRequest.content,
            authorId = createRequest.authorId,
            tags = createRequest.tags,
            published = createRequest.published
        )
        val created = blogService.createBlogPost(post)
        return ResponseEntity.ok(created)
    }
    
    @PutMapping("/{id}")
    fun updateBlogPost(@PathVariable id: Long, @RequestBody updateRequest: UpdateBlogPostRequest): ResponseEntity<BlogPost> {
        val existingPost = blogService.getBlogPost(id) ?: return ResponseEntity.notFound().build()
        
        val updatedPost = existingPost.copy(
            title = updateRequest.title ?: existingPost.title,
            content = updateRequest.content ?: existingPost.content,
            tags = updateRequest.tags ?: existingPost.tags,
            published = updateRequest.published ?: existingPost.published,
            updatedAt = LocalDateTime.now()
        )
        
        val result = blogService.updateBlogPost(updatedPost)
        return ResponseEntity.ok(result)
    }
    
    @DeleteMapping("/{id}")
    fun deleteBlogPost(@PathVariable id: Long): ResponseEntity<Void> {
        blogService.deleteBlogPost(id)
        return ResponseEntity.noContent().build()
    }
    
    @GetMapping("/popular")
    fun getPopularPosts(@RequestParam(defaultValue = "10") limit: Int): ResponseEntity<Any> {
        val posts = blogService.getPopularPosts(limit)
        return ResponseEntity.ok(mapOf(
            "posts" to posts,
            "limit" to limit,
            "total" to posts.size
        ))
    }
    
    @GetMapping("/recent")
    fun getRecentPosts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Any> {
        val posts = blogService.getRecentPosts(page, size)
        return ResponseEntity.ok(mapOf(
            "posts" to posts,
            "page" to page,
            "size" to size,
            "total" to posts.size
        ))
    }
    
    @PostMapping("/refresh-popular")
    fun refreshPopularPosts(): ResponseEntity<String> {
        blogService.refreshPopularPosts()
        return ResponseEntity.ok("Popular posts cache refreshed")
    }
}

data class CreateBlogPostRequest(
    val title: String,
    val content: String,
    val authorId: Long,
    val tags: Set<String> = emptySet(),
    val published: Boolean = false
)

data class UpdateBlogPostRequest(
    val title: String?,
    val content: String?,
    val tags: Set<String>?,
    val published: Boolean?
)