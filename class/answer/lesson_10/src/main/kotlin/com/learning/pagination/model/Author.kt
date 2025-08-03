/**
 * Lesson 10 Complete Solution: Author Entity
 * 
 * Complete Author entity optimized for search and filtering
 */

package com.learning.pagination.model

import jakarta.persistence.*
import jakarta.validation.constraints.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "authors",
    indexes = [
        Index(name = "idx_authors_username", columnList = "username"),
        Index(name = "idx_authors_email", columnList = "email"),
        Index(name = "idx_authors_name", columnList = "first_name, last_name"),
        Index(name = "idx_authors_active", columnList = "is_active"),
        Index(name = "idx_authors_created", columnList = "created_at")
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
    val username: String,
    
    @Column(nullable = false, unique = true, length = 100)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    val email: String,
    
    @Column(name = "first_name", nullable = false, length = 50)
    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 50)
    val firstName: String,
    
    @Column(name = "last_name", nullable = false, length = 50)
    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 50)
    val lastName: String,
    
    @Column(columnDefinition = "TEXT")
    @Size(max = 1000)
    val bio: String? = null,
    
    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    val posts: List<Post> = emptyList()
    
) {
    
    fun getDisplayName(): String = "$firstName $lastName"
    
    fun getFullDisplayName(): String = "$firstName $lastName (@$username)"
    
    fun isEligibleForListing(): Boolean = isActive
    
    fun getPublishedPostCount(): Int = posts.count { it.status == PostStatus.PUBLISHED }
    
    fun getTotalViews(): Long = posts.sumOf { it.viewCount }
    
    fun getTotalLikes(): Long = posts.sumOf { it.likeCount }
    
    fun getPopularityScore(): Double {
        val publishedPosts = getPublishedPostCount()
        if (publishedPosts == 0) return 0.0
        
        val avgViews = getTotalViews().toDouble() / publishedPosts
        val avgLikes = getTotalLikes().toDouble() / publishedPosts
        
        return avgViews + (avgLikes * 5)
    }
}