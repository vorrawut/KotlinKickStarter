package com.learning.caching.repository

import com.learning.caching.model.Comment
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CommentRepository : JpaRepository<Comment, Long> {
    
    fun findByPostId(postId: Long, pageable: Pageable): Page<Comment>
    
    fun findByAuthorId(authorId: Long, pageable: Pageable): Page<Comment>
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.postId = :postId")
    fun countByPostId(@Param("postId") postId: Long): Long
    
    @Query("SELECT c FROM Comment c WHERE c.postId = :postId ORDER BY c.createdAt DESC")
    fun findRecentCommentsByPostId(@Param("postId") postId: Long, pageable: Pageable): Page<Comment>
}