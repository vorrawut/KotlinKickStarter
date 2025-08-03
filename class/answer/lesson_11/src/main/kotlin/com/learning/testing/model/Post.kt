/**
 * Lesson 11 Complete Solution: Post Entity
 * 
 * Complete Post entity with comprehensive business logic for testing
 */

package com.learning.testing.model

import jakarta.persistence.*
import jakarta.validation.constraints.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "posts",
    indexes = [
        Index(name = "idx_posts_status", columnList = "status"),
        Index(name = "idx_posts_author", columnList = "author_id"),
        Index(name = "idx_posts_published", columnList = "published_at"),
        Index(name = "idx_posts_created", columnList = "created_at"),
        Index(name = "idx_posts_views", columnList = "view_count")
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
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: PostStatus = PostStatus.DRAFT,
    
    @Column(name = "published_at")
    val publishedAt: LocalDateTime? = null,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "view_count", nullable = false)
    val viewCount: Long = 0,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    val author: User,
    
    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    val comments: List<Comment> = emptyList()
    
) {
    
    // Business logic methods for testing
    fun isPublished(): Boolean = status == PostStatus.PUBLISHED
    
    fun isDraft(): Boolean = status == PostStatus.DRAFT
    
    fun isArchived(): Boolean = status == PostStatus.ARCHIVED
    
    fun canBeEditedBy(userId: Long): Boolean {
        // Business rule: Author can edit, or if it's still a draft
        return author.id == userId || isDraft()
    }
    
    fun canBePublished(): Boolean {
        // Business rule: Must be draft and have valid content
        return isDraft() && title.isNotBlank() && content.isNotBlank()
    }
    
    fun canBeDeleted(): Boolean {
        // Business rule: Can delete if not published or if archived
        return !isPublished() || isArchived()
    }
    
    fun getWordCount(): Int {
        return content.split("\\s+".toRegex()).size
    }
    
    fun getReadingTimeMinutes(): Int {
        val wordsPerMinute = 200
        return maxOf(1, (getWordCount() + wordsPerMinute - 1) / wordsPerMinute)
    }
    
    fun isPopular(): Boolean {
        // Business rule: Popular if has more than 100 views
        return viewCount > 100
    }
    
    fun getCommentCount(): Int = comments.size
    
    fun getApprovedCommentCount(): Int = comments.count { it.isApproved() }
    
    fun hasComments(): Boolean = comments.isNotEmpty()
    
    fun isRecent(days: Int = 7): Boolean {
        val daysAgo = LocalDateTime.now().minusDays(days.toLong())
        return createdAt.isAfter(daysAgo)
    }
    
    fun getEngagementScore(): Double {
        // Simple engagement calculation: views + comments * 5
        return viewCount.toDouble() + (getApprovedCommentCount() * 5)
    }
    
    fun getTitlePreview(maxLength: Int = 50): String {
        return if (title.length <= maxLength) title else title.take(maxLength) + "..."
    }
    
    fun getContentPreview(maxLength: Int = 100): String {
        return if (content.length <= maxLength) content else content.take(maxLength) + "..."
    }
}

enum class PostStatus(val displayName: String) {
    DRAFT("Draft"),
    PUBLISHED("Published"),
    ARCHIVED("Archived"),
    DELETED("Deleted");
    
    fun isVisible(): Boolean = this in listOf(PUBLISHED, ARCHIVED)
    fun isEditable(): Boolean = this in listOf(DRAFT, ARCHIVED)
}