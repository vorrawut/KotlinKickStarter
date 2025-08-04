package com.learning.caching.controller

import com.learning.caching.service.SearchService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/search")
class SearchController(
    private val searchService: SearchService
) {
    
    @GetMapping
    fun searchPosts(
        @RequestParam query: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Any> {
        val posts = searchService.searchPosts(query, page, size)
        return ResponseEntity.ok(mapOf(
            "posts" to posts,
            "query" to query,
            "page" to page,
            "size" to size,
            "total" to posts.size
        ))
    }
    
    @GetMapping("/by-tag")
    fun searchByTag(
        @RequestParam tag: String,
        @RequestParam(defaultValue = "0") page: Int
    ): ResponseEntity<Any> {
        val posts = searchService.searchByTag(tag, page)
        return ResponseEntity.ok(mapOf(
            "posts" to posts,
            "tag" to tag,
            "page" to page,
            "total" to posts.size
        ))
    }
    
    @GetMapping("/recommended/{userId}")
    fun getRecommendedPosts(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "5") limit: Int
    ): ResponseEntity<Any> {
        val posts = searchService.getRecommendedPosts(userId, limit)
        return ResponseEntity.ok(mapOf(
            "posts" to posts,
            "userId" to userId,
            "limit" to limit,
            "total" to posts.size
        ))
    }
    
    @PostMapping("/clear-cache")
    fun clearSearchCache(): ResponseEntity<String> {
        searchService.clearSearchCache()
        return ResponseEntity.ok("Search cache cleared")
    }
}