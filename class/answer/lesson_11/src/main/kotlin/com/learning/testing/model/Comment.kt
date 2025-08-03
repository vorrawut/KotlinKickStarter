/**
 * Lesson 11 Complete Solution: Comment Entity
 * 
 * Complete Comment entity for testing relationships and business logic
 */

package com.learning.testing.model

import jakarta.persistence.*
import jakarta.validation.constraints.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "comments",
    indexes = [
        Index(name = "idx_comments_post", columnList = "post_id"),
        Index(name = "idx_comments_author", columnList = "author_id"),
        Index(name = "idx_comments_status", columnList = "status"),
        Index(name = "idx_comments_created", columnList = "created_at")
    ]
)
data class Comment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Comment content is required")
    @Size(min = 1, max = 2000, message = "Comment must be 1-2000 characters")
    val content: String,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    val author: User,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    val post: Post,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: CommentStatus = CommentStatus.PENDING,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
    
) {
    
    // Business logic methods for testing
    fun isApproved(): Boolean = status == CommentStatus.APPROVED
    
    fun isPending(): Boolean = status == CommentStatus.PENDING
    
    fun isRejected(): Boolean = status == CommentStatus.REJECTED
    
    fun canBeDeletedBy(userId: Long): Boolean {
        // Business rule: Author can delete their own comments, or post author can delete any comment
        return author.id == userId || post.author.id == userId
    }
    
    fun canBeEditedBy(userId: Long): Boolean {
        // Business rule: Only comment author can edit, and only if not approved yet
        return author.id == userId && !isApproved()
    }
    
    fun getWordCount(): Int {
        return content.split("\\s+".toRegex()).size
    }
    
    fun isLongComment(): Boolean {
        return getWordCount() > 50
    }
    
    fun getPreview(length: Int = 100): String {
        return if (content.length <= length) content else content.take(length) + "..."
    }
    
    fun isRecent(hours: Int = 24): Boolean {
        val hoursAgo = LocalDateTime.now().minusHours(hours.toLong())
        return createdAt.isAfter(hoursAgo)
    }
    
    fun needsModeration(): Boolean {
        return status == CommentStatus.PENDING
    }
    
    fun getAgeInHours(): Long {
        return java.time.Duration.between(createdAt, LocalDateTime.now()).toHours()
    }
}

enum class CommentStatus(val displayName: String) {
    PENDING("Pending Approval"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    DELETED("Deleted");
    
    fun isVisible(): Boolean = this == APPROVED
    fun needsModeration(): Boolean = this == PENDING
}