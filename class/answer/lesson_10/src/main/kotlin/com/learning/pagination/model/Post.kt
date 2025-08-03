/**
 * Lesson 10 Complete Solution: Post Entity with Pagination Optimizations
 * 
 * Complete Post entity with comprehensive indexing and business logic for pagination
 */

package com.learning.pagination.model

import jakarta.persistence.*
import jakarta.validation.constraints.*
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Entity
@Table(
    name = "posts",
    indexes = [
        // Single column indexes for common filters
        Index(name = "idx_posts_status", columnList = "status"),
        Index(name = "idx_posts_created_at", columnList = "created_at"),
        Index(name = "idx_posts_published_at", columnList = "published_at"),
        Index(name = "idx_posts_view_count", columnList = "view_count"),
        Index(name = "idx_posts_like_count", columnList = "like_count"),
        Index(name = "idx_posts_featured", columnList = "featured"),
        
        // Composite indexes for common query patterns
        Index(name = "idx_posts_status_created", columnList = "status, created_at DESC"),
        Index(name = "idx_posts_author_status", columnList = "author_id, status"),
        Index(name = "idx_posts_status_published", columnList = "status, published_at DESC"),
        Index(name = "idx_posts_featured_status", columnList = "featured, status, created_at DESC"),
        
        // Popularity sorting indexes
        Index(name = "idx_posts_popularity", columnList = "view_count DESC, like_count DESC, created_at DESC")
    ]
)
data class Post(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, length = 200)
    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 200, message = "Title must be 5-200 characters")
    val title: String,
    
    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Content is required")
    @Size(min = 10, message = "Content must be at least 10 characters")
    val content: String,
    
    @Column(length = 500)
    @Size(max = 500, message = "Summary cannot exceed 500 characters")
    val summary: String? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: PostStatus = PostStatus.DRAFT,
    
    @Column(name = "published_at")
    val publishedAt: LocalDateTime? = null,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    val author: Author,
    
    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val comments: List<Comment> = emptyList(),
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "post_categories",
        joinColumns = [JoinColumn(name = "post_id")],
        inverseJoinColumns = [JoinColumn(name = "category_id")]
    )
    val categories: Set<Category> = emptySet(),
    
    @Column(name = "view_count", nullable = false)
    val viewCount: Long = 0,
    
    @Column(name = "like_count", nullable = false)
    val likeCount: Long = 0,
    
    @Column(nullable = false)
    val featured: Boolean = false
    
) {
    
    fun getSearchableContent(): String {
        val titleWeight = title.repeat(3) // Give title higher weight in search
        val summaryText = summary ?: ""
        return "$titleWeight $summaryText $content".lowercase()
    }
    
    fun isVisibleToPublic(): Boolean {
        return status == PostStatus.PUBLISHED && publishedAt != null
    }
    
    fun getRelevanceScore(searchTerms: List<String>): Double {
        var score = 0.0
        val titleLower = title.lowercase()
        val contentLower = content.lowercase()
        val summaryLower = summary?.lowercase() ?: ""
        
        searchTerms.forEach { term ->
            val termLower = term.lowercase()
            
            // Title matches (weight: 5.0)
            if (titleLower.contains(termLower)) {
                score += 5.0
                // Exact title match bonus
                if (titleLower == termLower) score += 10.0
            }
            
            // Summary matches (weight: 3.0)
            if (summaryLower.contains(termLower)) {
                score += 3.0
            }
            
            // Content matches (weight: 1.0)
            val contentMatches = contentLower.split(termLower).size - 1
            score += contentMatches * 1.0
            
            // Category matches (weight: 2.0)
            categories.forEach { category ->
                if (category.name.lowercase().contains(termLower)) {
                    score += 2.0
                }
            }
        }
        
        // Recency boost (newer posts get slight boost)
        val daysSinceCreation = ChronoUnit.DAYS.between(createdAt, LocalDateTime.now())
        val recencyBoost = maxOf(0.0, 1.0 - (daysSinceCreation / 365.0))
        score *= (1.0 + recencyBoost * 0.1)
        
        // Popularity boost
        val popularityScore = (viewCount + likeCount * 2 + comments.size * 3) / 100.0
        score *= (1.0 + popularityScore * 0.05)
        
        return score
    }
    
    fun getEngagementMetrics(): PostEngagement {
        val totalEngagements = likeCount + comments.size * 2
        val engagementRate = if (viewCount > 0) totalEngagements.toDouble() / viewCount else 0.0
        
        return PostEngagement(
            viewCount = viewCount,
            likeCount = likeCount,
            commentCount = comments.size,
            engagementScore = engagementRate,
            popularityScore = calculatePopularityScore()
        )
    }
    
    fun isPopular(): Boolean {
        return viewCount > 100 || likeCount > 10 || comments.size > 5
    }
    
    fun isRecent(days: Int = 30): Boolean {
        val daysAgo = LocalDateTime.now().minusDays(days.toLong())
        return createdAt.isAfter(daysAgo)
    }
    
    fun getWordCount(): Int {
        return content.split("\\s+".toRegex()).size
    }
    
    fun getReadingTimeMinutes(): Int {
        val wordsPerMinute = 200
        return maxOf(1, (getWordCount() + wordsPerMinute - 1) / wordsPerMinute)
    }
    
    fun getCategoryNames(): List<String> {
        return categories.map { it.name }.sorted()
    }
    
    fun getEffectiveSummary(): String {
        return summary ?: content.take(200) + if (content.length > 200) "..." else ""
    }
    
    private fun calculatePopularityScore(): Double {
        // Weighted popularity score considering all engagement metrics
        val viewWeight = 1.0
        val likeWeight = 5.0
        val commentWeight = 10.0
        
        return (viewCount * viewWeight + likeCount * likeWeight + comments.size * commentWeight) / 100.0
    }
}

enum class PostStatus(val displayName: String) {
    DRAFT("Draft"),
    PUBLISHED("Published"),
    ARCHIVED("Archived"),
    DELETED("Deleted");
    
    fun isVisible(): Boolean = this in listOf(PUBLISHED, ARCHIVED)
    fun isPublic(): Boolean = this == PUBLISHED
}

data class PostEngagement(
    val viewCount: Long,
    val likeCount: Long,
    val commentCount: Int,
    val engagementScore: Double,
    val popularityScore: Double
)