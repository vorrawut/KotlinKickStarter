/**
 * Lesson 9 Complete Solution: Comment Entity
 * 
 * Complete Comment entity with self-referencing relationships for threaded comments
 */

package com.learning.crud.model

import com.learning.crud.audit.AuditableEntity
import jakarta.persistence.*
import jakarta.validation.constraints.*

@Entity
@Table(
    name = "comments",
    indexes = [
        Index(name = "idx_comment_post", columnList = "post_id"),
        Index(name = "idx_comment_author", columnList = "author_id"),
        Index(name = "idx_comment_parent", columnList = "parent_id"),
        Index(name = "idx_comment_status", columnList = "status"),
        Index(name = "idx_comment_approved", columnList = "is_approved")
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
    val author: Author,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    val post: Post,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    val parent: Comment? = null,
    
    @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    val replies: MutableList<Comment> = mutableListOf(),
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: CommentStatus = CommentStatus.PENDING,
    
    @Column(name = "is_approved", nullable = false)
    val isApproved: Boolean = false
    
) : AuditableEntity() {
    
    fun isReply(): Boolean {
        return parent != null
    }
    
    fun isTopLevel(): Boolean {
        return parent == null
    }
    
    fun canBeDeletedBy(userId: Long): Boolean {
        // Author can delete their own comments, or post author can delete any comment on their post
        return author.id == userId || post.author.id == userId
    }
    
    fun canBeEditedBy(userId: Long): Boolean {
        // Only the comment author can edit, and only if not approved yet
        return author.id == userId && !isApproved
    }
    
    fun addReply(reply: Comment) {
        replies.add(reply)
        // Note: In a real implementation, you'd also set reply.parent = this
    }
    
    fun removeReply(reply: Comment) {
        replies.remove(reply)
    }
    
    fun getReplyCount(): Int {
        return replies.size
    }
    
    fun getTotalReplyCount(): Int {
        // Count all nested replies recursively
        return replies.size + replies.sumOf { it.getTotalReplyCount() }
    }
    
    fun getApprovedReplies(): List<Comment> {
        return replies.filter { it.isApproved }
    }
    
    fun approve(): Comment {
        return copy(status = CommentStatus.APPROVED, isApproved = true)
    }
    
    fun reject(): Comment {
        return copy(status = CommentStatus.REJECTED, isApproved = false)
    }
    
    fun isPending(): Boolean {
        return status == CommentStatus.PENDING
    }
    
    fun isRejected(): Boolean {
        return status == CommentStatus.REJECTED
    }
    
    fun getDepth(): Int {
        var depth = 0
        var current = parent
        while (current != null) {
            depth++
            current = current.parent
        }
        return depth
    }
    
    fun getThreadRoot(): Comment {
        var current = this
        while (current.parent != null) {
            current = current.parent!!
        }
        return current
    }
    
    fun getAllRepliesFlat(): List<Comment> {
        val allReplies = mutableListOf<Comment>()
        fun collectReplies(comment: Comment) {
            allReplies.addAll(comment.replies)
            comment.replies.forEach { collectReplies(it) }
        }
        collectReplies(this)
        return allReplies
    }
    
    fun getWordCount(): Int {
        return content.split("\\s+".toRegex()).size
    }
    
    fun isLongComment(): Boolean {
        return getWordCount() > 100
    }
    
    fun getPreview(length: Int = 100): String {
        return if (content.length <= length) {
            content
        } else {
            content.take(length) + "..."
        }
    }
    
    fun canHaveReplies(): Boolean {
        // Limit reply depth to prevent infinite nesting
        return getDepth() < 5 && isApproved
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