/**
 * Lesson 10 Complete Solution: Comment Entity
 * 
 * Complete Comment entity for post engagement tracking
 */

package com.learning.pagination.model

import jakarta.persistence.*
import jakarta.validation.constraints.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "comments",
    indexes = [
        Index(name = "idx_comments_post", columnList = "post_id"),
        Index(name = "idx_comments_author", columnList = "author_id"),
        Index(name = "idx_comments_created", columnList = "created_at"),
        Index(name = "idx_comments_status", columnList = "status")
    ]
)
data class Comment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Comment content is required")
    @Size(min = 1, max = 2000)
    val content: String,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    val author: Author,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    val post: Post,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: CommentStatus = CommentStatus.PENDING,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
    
) {
    
    fun isApproved(): Boolean = status == CommentStatus.APPROVED
    
    fun getWordCount(): Int = content.split("\\s+".toRegex()).size
    
    fun getPreview(length: Int = 100): String {
        return if (content.length <= length) content else content.take(length) + "..."
    }
}

enum class CommentStatus(val displayName: String) {
    PENDING("Pending Approval"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    DELETED("Deleted");
    
    fun isVisible(): Boolean = this == APPROVED
}