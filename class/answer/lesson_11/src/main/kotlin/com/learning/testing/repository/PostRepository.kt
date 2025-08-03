/**
 * Lesson 11 Complete Solution: Post Repository
 * 
 * Complete repository with complex queries for testing
 */

package com.learning.testing.repository

import com.learning.testing.model.Post
import com.learning.testing.model.PostStatus
import com.learning.testing.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface PostRepository : JpaRepository<Post, Long> {
    
    // Method name queries for testing
    fun findByStatus(status: PostStatus): List<Post>
    
    fun findByAuthor(author: User): List<Post>
    
    fun findByAuthorId(authorId: Long): List<Post>
    
    fun findByTitleContainingIgnoreCase(title: String): List<Post>
    
    fun findByStatusAndAuthor(status: PostStatus, author: User): List<Post>
    
    fun findByViewCountGreaterThan(viewCount: Long): List<Post>
    
    fun findByCreatedAtAfter(date: LocalDateTime): List<Post>
    
    fun findByStatusOrderByCreatedAtDesc(status: PostStatus): List<Post>
    
    fun countByStatus(status: PostStatus): Long
    
    fun countByAuthor(author: User): Long
    
    fun existsByTitleAndAuthor(title: String, author: User): Boolean
    
    // Custom JPQL queries with fetch joins for testing
    @Query("SELECT p FROM Post p JOIN FETCH p.author WHERE p.status = :status")
    fun findByStatusWithAuthor(@Param("status") status: PostStatus): List<Post>
    
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.comments WHERE p.id = :id")
    fun findByIdWithComments(@Param("id") id: Long): Post?
    
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.author LEFT JOIN FETCH p.comments WHERE p.id = :id")
    fun findByIdWithAuthorAndComments(@Param("id") id: Long): Post?
    
    // Aggregation queries for testing
    @Query("SELECT COUNT(p) FROM Post p WHERE p.author.id = :authorId AND p.status = :status")
    fun countByAuthorAndStatus(@Param("authorId") authorId: Long, @Param("status") status: PostStatus): Long
    
    @Query("SELECT AVG(p.viewCount) FROM Post p WHERE p.status = :status")
    fun getAverageViewCountByStatus(@Param("status") status: PostStatus): Double?
    
    @Query("SELECT MAX(p.viewCount) FROM Post p WHERE p.author.id = :authorId")
    fun getMaxViewCountByAuthor(@Param("authorId") authorId: Long): Long?
    
    @Query("SELECT SUM(p.viewCount) FROM Post p WHERE p.status = 'PUBLISHED'")
    fun getTotalViewsForPublishedPosts(): Long?
    
    // Complex queries for testing business logic
    @Query("""
        SELECT p FROM Post p 
        WHERE p.status = :status 
        AND p.viewCount > :minViews 
        AND p.publishedAt > :since
        ORDER BY p.viewCount DESC
    """)
    fun findPopularPostsSince(
        @Param("status") status: PostStatus,
        @Param("minViews") minViews: Long,
        @Param("since") since: LocalDateTime
    ): List<Post>
    
    @Query("""
        SELECT p FROM Post p 
        WHERE p.author.isActive = true 
        AND p.status = 'PUBLISHED' 
        AND SIZE(p.comments) > :minComments
    """)
    fun findPostsWithMinComments(@Param("minComments") minComments: Int): List<Post>
    
    @Query("""
        SELECT p FROM Post p 
        WHERE p.status = 'PUBLISHED' 
        AND p.publishedAt BETWEEN :start AND :end
        ORDER BY p.viewCount DESC, p.publishedAt DESC
    """)
    fun findPublishedPostsBetweenDates(
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime
    ): List<Post>
    
    // Update queries for testing transaction behavior
    @Modifying
    @Query("UPDATE Post p SET p.status = :status WHERE p.id = :id")
    fun updatePostStatus(@Param("id") id: Long, @Param("status") status: PostStatus): Int
    
    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    fun incrementViewCount(@Param("id") id: Long): Int
    
    @Modifying
    @Query("UPDATE Post p SET p.status = 'ARCHIVED' WHERE p.status = 'PUBLISHED' AND p.publishedAt < :date")
    fun archiveOldPublishedPosts(@Param("date") date: LocalDateTime): Int
    
    // Subqueries for testing complex scenarios
    @Query("""
        SELECT p FROM Post p 
        WHERE p.author.id IN (
            SELECT DISTINCT c.author.id FROM Comment c 
            WHERE c.post.id = p.id AND c.status = 'APPROVED'
        )
    """)
    fun findPostsWhereAuthorHasCommented(): List<Post>
    
    @Query("""
        SELECT p FROM Post p 
        WHERE p.viewCount > (
            SELECT AVG(p2.viewCount) FROM Post p2 WHERE p2.status = 'PUBLISHED'
        )
    """)
    fun findPostsAboveAverageViews(): List<Post>
    
    // Pagination methods for testing
    fun findByStatusOrderByCreatedAtDesc(status: PostStatus, pageable: Pageable): Page<Post>
    
    fun findByAuthorOrderByCreatedAtDesc(author: User, pageable: Pageable): Page<Post>
    
    @Query("SELECT p FROM Post p WHERE p.status = 'PUBLISHED' ORDER BY p.viewCount DESC")
    fun findPublishedPostsByPopularity(pageable: Pageable): Page<Post>
    
    // Native queries for testing database-specific features
    @Query(
        value = """
            SELECT p.*, 
                   (SELECT COUNT(*) FROM comments c WHERE c.post_id = p.id AND c.status = 'APPROVED') as comment_count
            FROM posts p 
            WHERE p.status = :status
            ORDER BY comment_count DESC, p.view_count DESC
        """,
        nativeQuery = true
    )
    fun findPostsWithCommentCounts(@Param("status") status: String): List<Any>
    
    @Query(
        value = """
            SELECT p.*, 
                   EXTRACT(DAY FROM (CURRENT_TIMESTAMP - p.published_at)) as days_since_published
            FROM posts p 
            WHERE p.status = 'PUBLISHED' 
            AND p.published_at > :since
            ORDER BY days_since_published ASC
        """,
        nativeQuery = true
    )
    fun findRecentlyPublishedWithAge(@Param("since") since: LocalDateTime): List<Any>
}