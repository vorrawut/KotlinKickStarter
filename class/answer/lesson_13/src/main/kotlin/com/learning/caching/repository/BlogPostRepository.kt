package com.learning.caching.repository

import com.learning.caching.model.BlogPost
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface BlogPostRepository : JpaRepository<BlogPost, Long> {
    
    fun findByAuthorId(authorId: Long, pageable: Pageable): Page<BlogPost>
    
    fun findByPublishedTrue(pageable: Pageable): Page<BlogPost>
    
    @Query("SELECT p FROM BlogPost p WHERE p.title LIKE %:query% OR p.content LIKE %:query%")
    fun findByTitleContainingOrContentContaining(
        @Param("query") titleQuery: String,
        @Param("query") contentQuery: String,
        pageable: Pageable
    ): Page<BlogPost>
    
    @Query("SELECT p FROM BlogPost p JOIN p.tags t WHERE t = :tag")
    fun findByTagsContaining(@Param("tag") tag: String, pageable: Pageable): Page<BlogPost>
    
    @Query("SELECT p FROM BlogPost p WHERE p.published = true ORDER BY p.viewCount DESC")
    fun findTopByOrderByViewCountDesc(pageable: Pageable): Page<BlogPost>
    
    @Query("SELECT p FROM BlogPost p WHERE p.published = true ORDER BY p.createdAt DESC")
    fun findRecentPublishedPosts(pageable: Pageable): Page<BlogPost>
}