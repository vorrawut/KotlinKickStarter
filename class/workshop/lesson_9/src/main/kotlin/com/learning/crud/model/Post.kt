/**
 * Lesson 9 Workshop: Post Entity
 * 
 * TODO: Create Post entity with complex relationships and business logic
 * This entity represents blog posts with relationships to authors, comments, and categories
 */

package com.learning.crud.model

// TODO: Import necessary JPA annotations
// TODO: Import validation annotations
// TODO: Import LocalDateTime
// TODO: Import AuditableEntity
import com.learning.crud.audit.AuditableEntity
import java.time.LocalDateTime

// TODO: Add @Entity annotation
// TODO: Add @Table annotation with name "posts" and appropriate indexes
// Suggested indexes: status, author_id, published_at, created_at
data class Post(
    // TODO: Add @Id and @GeneratedValue annotations for primary key
    val id: Long? = null,
    
    // TODO: Add @Column annotation with nullable = false
    // TODO: Add validation annotations (@NotBlank, @Size)
    val title: String,
    
    // TODO: Add @Column annotation with columnDefinition = "TEXT"
    // TODO: Add validation annotations (@NotBlank, @Size)
    val content: String,
    
    // TODO: Add @Column annotation for summary (nullable)
    // TODO: Add validation annotation (@Size)
    val summary: String? = null,
    
    // TODO: Add @Enumerated annotation with EnumType.STRING
    // TODO: Add @Column annotation with nullable = false
    val status: PostStatus = PostStatus.DRAFT,
    
    // TODO: Add @Column annotation for published date (nullable)
    val publishedAt: LocalDateTime? = null,
    
    // TODO: Add @ManyToOne relationship with Author
    // TODO: Configure fetch type and join column
    // TODO: Add @JoinColumn annotation with name "author_id"
    val author: Author,
    
    // TODO: Add @OneToMany relationship with Comment
    // TODO: Configure mappedBy, cascade, and fetch properties
    // TODO: Set orphanRemoval = true for comment cleanup
    val comments: MutableList<Comment> = mutableListOf(),
    
    // TODO: Add @ManyToMany relationship with Category
    // TODO: Configure @JoinTable with appropriate join columns
    // TODO: Set up cascade operations (PERSIST, MERGE)
    val categories: MutableSet<Category> = mutableSetOf(),
    
    // TODO: Add @Column annotation with default value 0
    val viewCount: Long = 0,
    
    // TODO: Add @Column annotation with default value 0
    val likeCount: Long = 0
    
) : AuditableEntity() {
    
    // TODO: Add business logic methods
    
    fun isPublished(): Boolean {
        // TODO: Check if post status is PUBLISHED
        TODO("Implement isPublished method")
    }
    
    fun isDraft(): Boolean {
        // TODO: Check if post status is DRAFT
        TODO("Implement isDraft method")
    }
    
    fun canBeEditedBy(userId: Long): Boolean {
        // TODO: Check if user can edit this post
        // Rules: Author can edit, or if status is DRAFT
        TODO("Implement canBeEditedBy method")
    }
    
    fun canBePublished(): Boolean {
        // TODO: Check if post can be published
        // Rules: Must be DRAFT status, have title and content
        TODO("Implement canBePublished method")
    }
    
    fun addComment(comment: Comment) {
        // TODO: Add comment with proper relationship setup
        // Set the post reference in the comment
        TODO("Implement addComment method")
    }
    
    fun removeComment(comment: Comment) {
        // TODO: Remove comment with proper cleanup
        TODO("Implement removeComment method")
    }
    
    fun addCategory(category: Category) {
        // TODO: Add category with proper relationship setup
        TODO("Implement addCategory method")
    }
    
    fun removeCategory(category: Category) {
        // TODO: Remove category from post
        TODO("Implement removeCategory method")
    }
    
    fun incrementViewCount(): Post {
        // TODO: Increment view count and return updated post
        // Note: In real applications, this might be done asynchronously
        TODO("Implement incrementViewCount method")
    }
    
    fun incrementLikeCount(): Post {
        // TODO: Increment like count and return updated post
        TODO("Implement incrementLikeCount method")
    }
    
    fun getApprovedComments(): List<Comment> {
        // TODO: Return only approved comments
        TODO("Implement getApprovedComments method")
    }
    
    fun getTopLevelComments(): List<Comment> {
        // TODO: Return comments that are not replies (parent is null)
        TODO("Implement getTopLevelComments method")
    }
    
    fun hasCategory(categoryName: String): Boolean {
        // TODO: Check if post has category with given name
        TODO("Implement hasCategory method")
    }
    
    fun getCategoryNames(): List<String> {
        // TODO: Return list of category names
        TODO("Implement getCategoryNames method")
    }
    
    fun getWordCount(): Int {
        // TODO: Calculate approximate word count of content
        TODO("Implement getWordCount method")
    }
    
    fun getReadingTimeMinutes(): Int {
        // TODO: Calculate estimated reading time (assume 200 words per minute)
        TODO("Implement getReadingTimeMinutes method")
    }
}

// TODO: Create PostStatus enum with appropriate values
enum class PostStatus {
    // TODO: Add status values: DRAFT, PUBLISHED, ARCHIVED, DELETED
    // Add display names and business logic if needed
}