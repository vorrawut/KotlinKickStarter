/**
 * Lesson 9 Complete Solution: Author Entity
 * 
 * Complete Author entity with relationships and business logic
 */

package com.learning.crud.model

import com.learning.crud.audit.AuditableEntity
import jakarta.persistence.*
import jakarta.validation.constraints.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "authors",
    indexes = [
        Index(name = "idx_author_username", columnList = "username"),
        Index(name = "idx_author_email", columnList = "email"),
        Index(name = "idx_author_active", columnList = "is_active")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_author_username", columnNames = ["username"]),
        UniqueConstraint(name = "uk_author_email", columnNames = ["email"])
    ]
)
data class Author(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, unique = true, length = 50)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    val username: String,
    
    @Column(nullable = false, unique = true, length = 100)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    val email: String,
    
    @Column(nullable = false, length = 50)
    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 50, message = "First name must be 1-50 characters")
    val firstName: String,
    
    @Column(nullable = false, length = 50)
    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 50, message = "Last name must be 1-50 characters")
    val lastName: String,
    
    @Column(columnDefinition = "TEXT")
    @Size(max = 1000, message = "Bio cannot exceed 1000 characters")
    val bio: String? = null,
    
    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,
    
    @OneToMany(mappedBy = "author", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val posts: List<Post> = emptyList()
    
) : AuditableEntity() {
    
    fun getFullName(): String {
        return "$firstName $lastName"
    }
    
    fun getPostCount(): Int {
        return posts.size
    }
    
    fun getActivePostCount(): Int {
        return posts.count { it.status == PostStatus.PUBLISHED }
    }
    
    fun canCreatePost(): Boolean {
        return isActive
    }
    
    fun hasPostWithTitle(title: String): Boolean {
        return posts.any { it.title.equals(title, ignoreCase = true) }
    }
    
    fun getRecentPosts(days: Int = 30): List<Post> {
        val cutoffDate = LocalDateTime.now().minusDays(days.toLong())
        return posts.filter { it.createdAt.isAfter(cutoffDate) }
    }
    
    fun getDraftPosts(): List<Post> {
        return posts.filter { it.status == PostStatus.DRAFT }
    }
    
    fun getPublishedPosts(): List<Post> {
        return posts.filter { it.status == PostStatus.PUBLISHED }
    }
    
    fun getTotalViews(): Long {
        return posts.sumOf { it.viewCount }
    }
    
    fun getTotalLikes(): Long {
        return posts.sumOf { it.likeCount }
    }
    
    fun getDisplayName(): String {
        return "$firstName $lastName (@$username)"
    }
    
    fun isNewAuthor(dayThreshold: Int = 30): Boolean {
        return getAgeInDays() <= dayThreshold
    }
}