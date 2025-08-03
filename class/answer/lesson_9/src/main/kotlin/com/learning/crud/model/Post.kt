/**
 * Lesson 9 Complete Solution: Post Entity
 * 
 * Complete Post entity with complex relationships and comprehensive business logic
 */

package com.learning.crud.model

import com.learning.crud.audit.AuditableEntity
import jakarta.persistence.*
import jakarta.validation.constraints.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "posts",
    indexes = [
        Index(name = "idx_post_status", columnList = "status"),
        Index(name = "idx_post_author", columnList = "author_id"),
        Index(name = "idx_post_published", columnList = "published_at"),
        Index(name = "idx_post_created", columnList = "created_at"),
        Index(name = "idx_post_title", columnList = "title")
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
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    val author: Author,
    
    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    val comments: MutableList<Comment> = mutableListOf(),
    
    @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE], fetch = FetchType.LAZY)
    @JoinTable(
        name = "post_categories",
        joinColumns = [JoinColumn(name = "post_id")],
        inverseJoinColumns = [JoinColumn(name = "category_id")]
    )
    val categories: MutableSet<Category> = mutableSetOf(),
    
    @Column(name = "view_count", nullable = false)
    val viewCount: Long = 0,
    
    @Column(name = "like_count", nullable = false)
    val likeCount: Long = 0
    
) : AuditableEntity() {
    
    fun isPublished(): Boolean {
        return status == PostStatus.PUBLISHED
    }
    
    fun isDraft(): Boolean {
        return status == PostStatus.DRAFT
    }
    
    fun isArchived(): Boolean {
        return status == PostStatus.ARCHIVED
    }
    
    fun canBeEditedBy(userId: Long): Boolean {
        // Author can always edit, or if it's still a draft
        return author.id == userId || isDraft()
    }
    
    fun canBePublished(): Boolean {
        // Must be draft and have required content
        return isDraft() && title.isNotBlank() && content.isNotBlank()
    }
    
    fun canBeDeleted(): Boolean {
        // Can delete if not published or if it's archived
        return !isPublished() || isArchived()
    }
    
    fun addComment(comment: Comment) {
        comments.add(comment)
        // Note: In a real implementation, you'd also set comment.post = this
    }
    
    fun removeComment(comment: Comment) {
        comments.remove(comment)
    }
    
    fun addCategory(category: Category) {
        categories.add(category)
        // Note: Bidirectional relationship would also update category.posts
    }
    
    fun removeCategory(category: Category) {
        categories.remove(category)
    }
    
    fun incrementViewCount(): Post {
        return copy(viewCount = viewCount + 1)
    }
    
    fun incrementLikeCount(): Post {
        return copy(likeCount = likeCount + 1)
    }
    
    fun decrementLikeCount(): Post {
        return copy(likeCount = maxOf(0, likeCount - 1))
    }
    
    fun getApprovedComments(): List<Comment> {
        return comments.filter { it.status == CommentStatus.APPROVED }
    }
    
    fun getTopLevelComments(): List<Comment> {
        return comments.filter { it.parent == null }
    }
    
    fun getPendingComments(): List<Comment> {
        return comments.filter { it.status == CommentStatus.PENDING }
    }
    
    fun hasCategory(categoryName: String): Boolean {
        return categories.any { it.name.equals(categoryName, ignoreCase = true) }
    }
    
    fun getCategoryNames(): List<String> {
        return categories.map { it.name }.sorted()
    }
    
    fun getWordCount(): Int {
        return content.split("\\s+".toRegex()).size
    }
    
    fun getReadingTimeMinutes(): Int {
        val wordsPerMinute = 200
        return maxOf(1, (getWordCount() + wordsPerMinute - 1) / wordsPerMinute)
    }
    
    fun getCommentCount(): Int {
        return comments.size
    }
    
    fun getApprovedCommentCount(): Int {
        return getApprovedComments().size
    }
    
    fun isPopular(): Boolean {
        // Consider popular if has more than 100 views or 10 likes
        return viewCount > 100 || likeCount > 10
    }
    
    fun getEngagementScore(): Double {
        // Simple engagement calculation
        val views = viewCount.toDouble()
        val likes = likeCount.toDouble()
        val commentsCount = getApprovedCommentCount().toDouble()
        
        return if (views > 0) {
            ((likes + commentsCount * 2) / views) * 100
        } else {
            0.0
        }
    }
    
    fun isRecent(days: Int = 7): Boolean {
        val daysAgo = LocalDateTime.now().minusDays(days.toLong())
        return createdAt.isAfter(daysAgo)
    }
    
    fun getEffectiveSummary(): String {
        return summary ?: content.take(200) + if (content.length > 200) "..." else ""
    }
    
    fun publish(): Post {
        return copy(
            status = PostStatus.PUBLISHED,
            publishedAt = LocalDateTime.now()
        )
    }
    
    fun archive(): Post {
        return copy(status = PostStatus.ARCHIVED)
    }
    
    fun unpublish(): Post {
        return copy(
            status = PostStatus.DRAFT,
            publishedAt = null
        )
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