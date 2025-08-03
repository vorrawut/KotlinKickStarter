/**
 * Lesson 9 Workshop: Author Entity
 * 
 * TODO: Create Author entity with proper relationships and business logic
 * This entity represents blog authors and their relationship to posts
 */

package com.learning.crud.model

// TODO: Import necessary JPA annotations
// TODO: Import validation annotations
// TODO: Import AuditableEntity
import com.learning.crud.audit.AuditableEntity

// TODO: Add @Entity annotation
// TODO: Add @Table annotation with name "authors" and appropriate indexes
data class Author(
    // TODO: Add @Id and @GeneratedValue annotations for primary key
    val id: Long? = null,
    
    // TODO: Add @Column annotation with unique = true and nullable = false
    // TODO: Add validation annotations (@NotBlank, @Size)
    val username: String,
    
    // TODO: Add @Column annotation with unique = true and nullable = false
    // TODO: Add validation annotations (@NotBlank, @Email)
    val email: String,
    
    // TODO: Add @Column annotation with nullable = false
    // TODO: Add validation annotations (@NotBlank, @Size)
    val firstName: String,
    
    // TODO: Add @Column annotation with nullable = false
    // TODO: Add validation annotations (@NotBlank, @Size)
    val lastName: String,
    
    // TODO: Add @Column annotation for bio (nullable, longer text)
    // TODO: Add validation annotation (@Size with appropriate max length)
    val bio: String? = null,
    
    // TODO: Add @Column annotation with default value
    val isActive: Boolean = true,
    
    // TODO: Add @OneToMany relationship with Post
    // TODO: Configure mappedBy, cascade, and fetch properties
    // TODO: Use appropriate cascade types (CascadeType.ALL for demonstration)
    val posts: List<Post> = emptyList()
    
) : AuditableEntity() {
    
    // TODO: Add business logic methods
    
    fun getFullName(): String {
        // TODO: Return concatenated first and last name
        TODO("Implement getFullName method")
    }
    
    fun getPostCount(): Int {
        // TODO: Return number of posts for this author
        TODO("Implement getPostCount method")
    }
    
    fun getActivePostCount(): Int {
        // TODO: Return number of published posts
        TODO("Implement getActivePostCount method")
    }
    
    fun canCreatePost(): Boolean {
        // TODO: Check if author can create new posts (is active)
        TODO("Implement canCreatePost method")
    }
    
    fun hasPostWithTitle(title: String): Boolean {
        // TODO: Check if author has a post with given title
        TODO("Implement hasPostWithTitle method")
    }
    
    fun getRecentPosts(days: Int = 30): List<Post> {
        // TODO: Return posts created in the last specified days
        TODO("Implement getRecentPosts method")
    }
}