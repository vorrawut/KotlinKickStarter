/**
 * Lesson 11 Complete Solution: Comment Repository
 * 
 * Complete repository for testing relationships and complex queries
 */

package com.learning.testing.repository

import com.learning.testing.model.Comment
import com.learning.testing.model.CommentStatus
import com.learning.testing.model.Post
import com.learning.testing.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface CommentRepository : JpaRepository<Comment, Long> {
    
    // Method name queries for testing relationships
    fun findByPost(post: Post): List<Comment>
    
    fun findByPostId(postId: Long): List<Comment>
    
    fun findByAuthor(author: User): List<Comment>
    
    fun findByAuthorId(authorId: Long): List<Comment>
    
    fun findByStatus(status: CommentStatus): List<Comment>
    
    fun findByPostAndStatus(post: Post, status: CommentStatus): List<Comment>
    
    fun findByAuthorAndStatus(author: User, status: CommentStatus): List<Comment>
    
    fun findByCreatedAtAfter(date: LocalDateTime): List<Comment>
    
    fun findByStatusOrderByCreatedAtDesc(status: CommentStatus): List<Comment>
    
    fun countByPost(post: Post): Long
    
    fun countByStatus(status: CommentStatus): Long
    
    fun countByPostAndStatus(post: Post, status: CommentStatus): Long
    
    // Custom JPQL queries for testing complex relationships
    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.post.id = :postId")
    fun findByPostIdWithAuthor(@Param("postId") postId: Long): List<Comment>
    
    @Query("SELECT c FROM Comment c JOIN FETCH c.post WHERE c.author.id = :authorId")
    fun findByAuthorIdWithPost(@Param("authorId") authorId: Long): List<Comment>
    
    @Query("SELECT c FROM Comment c WHERE c.post.author.id = :authorId")
    fun findCommentsOnAuthorPosts(@Param("authorId") authorId: Long): List<Comment>
    
    @Query("SELECT c FROM Comment c WHERE c.post.status = 'PUBLISHED' AND c.status = 'APPROVED'")
    fun findApprovedCommentsOnPublishedPosts(): List<Comment>
    
    // Aggregation queries for testing
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId AND c.status = :status")
    fun countByPostIdAndStatus(@Param("postId") postId: Long, @Param("status") status: CommentStatus): Long
    
    @Query("SELECT AVG(LENGTH(c.content)) FROM Comment c WHERE c.status = 'APPROVED'")
    fun getAverageCommentLength(): Double?
    
    @Query("""
        SELECT c.author.id, COUNT(c) FROM Comment c 
        WHERE c.status = 'APPROVED' 
        GROUP BY c.author.id 
        ORDER BY COUNT(c) DESC
    """)
    fun findMostActiveCommenters(): List<Any>
    
    // Time-based queries for testing
    @Query("""
        SELECT c FROM Comment c 
        WHERE c.createdAt BETWEEN :start AND :end 
        AND c.status = :status
        ORDER BY c.createdAt DESC
    """)
    fun findCommentsBetweenDates(
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime,
        @Param("status") status: CommentStatus
    ): List<Comment>
    
    @Query("""
        SELECT c FROM Comment c 
        WHERE c.status = 'PENDING' 
        AND c.createdAt < :cutoff
    """)
    fun findOldPendingComments(@Param("cutoff") cutoff: LocalDateTime): List<Comment>
    
    // Update queries for testing transaction behavior
    @Modifying
    @Query("UPDATE Comment c SET c.status = :status WHERE c.id = :id")
    fun updateCommentStatus(@Param("id") id: Long, @Param("status") status: CommentStatus): Int
    
    @Modifying
    @Query("UPDATE Comment c SET c.status = 'APPROVED' WHERE c.id IN :ids")
    fun bulkApproveComments(@Param("ids") ids: List<Long>): Int
    
    @Modifying
    @Query("UPDATE Comment c SET c.status = 'REJECTED' WHERE c.status = 'PENDING' AND c.createdAt < :cutoff")
    fun rejectOldPendingComments(@Param("cutoff") cutoff: LocalDateTime): Int
    
    // Complex queries for testing business rules
    @Query("""
        SELECT c FROM Comment c 
        WHERE c.post.id = :postId 
        AND c.author.id != :excludeAuthorId 
        AND c.status = 'APPROVED'
        ORDER BY c.createdAt ASC
    """)
    fun findApprovedCommentsExcludingAuthor(
        @Param("postId") postId: Long,
        @Param("excludeAuthorId") excludeAuthorId: Long
    ): List<Comment>
    
    @Query("""
        SELECT c FROM Comment c 
        WHERE c.post.status = 'PUBLISHED' 
        AND c.status = 'APPROVED' 
        AND LENGTH(c.content) > :minLength
    """)
    fun findLongApprovedCommentsOnPublishedPosts(@Param("minLength") minLength: Int): List<Comment>
    
    // Subqueries for testing advanced scenarios
    @Query("""
        SELECT c FROM Comment c 
        WHERE c.author.id IN (
            SELECT DISTINCT p.author.id FROM Post p WHERE p.status = 'PUBLISHED'
        )
        AND c.status = 'APPROVED'
    """)
    fun findCommentsFromPublishedAuthors(): List<Comment>
    
    @Query("""
        SELECT c FROM Comment c 
        WHERE c.post.id IN (
            SELECT p.id FROM Post p 
            WHERE p.viewCount > :minViews 
            AND p.status = 'PUBLISHED'
        )
        AND c.status = 'APPROVED'
    """)
    fun findCommentsOnPopularPosts(@Param("minViews") minViews: Long): List<Comment>
    
    // Delete queries for testing cascading behavior
    @Modifying
    @Query("DELETE FROM Comment c WHERE c.post.id = :postId")
    fun deleteAllByPostId(@Param("postId") postId: Long): Int
    
    @Modifying
    @Query("DELETE FROM Comment c WHERE c.status = 'DELETED' AND c.createdAt < :cutoff")
    fun purgeOldDeletedComments(@Param("cutoff") cutoff: LocalDateTime): Int
}