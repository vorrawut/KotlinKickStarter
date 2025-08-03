/**
 * Lesson 9 Complete Solution: Category Entity
 * 
 * Complete Category entity for organizing posts with many-to-many relationships
 */

package com.learning.crud.model

import com.learning.crud.audit.AuditableEntity
import jakarta.persistence.*
import jakarta.validation.constraints.*

@Entity
@Table(
    name = "categories",
    indexes = [
        Index(name = "idx_category_name", columnList = "name"),
        Index(name = "idx_category_active", columnList = "is_active")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_category_name", columnNames = ["name"])
    ]
)
data class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, unique = true, length = 100)
    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Category name must be 2-100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-_]+$", message = "Category name can only contain letters, numbers, spaces, hyphens, and underscores")
    val name: String,
    
    @Column(length = 500)
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    val description: String? = null,
    
    @Column(length = 7)
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex color code")
    val color: String? = null,
    
    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,
    
    @ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY)
    val posts: MutableSet<Post> = mutableSetOf()
    
) : AuditableEntity() {
    
    fun getPostCount(): Long {
        return posts.size.toLong()
    }
    
    fun getPublishedPostCount(): Long {
        return posts.count { it.status == PostStatus.PUBLISHED }.toLong()
    }
    
    fun getDraftPostCount(): Long {
        return posts.count { it.status == PostStatus.DRAFT }.toLong()
    }
    
    fun hasPublishedPosts(): Boolean {
        return posts.any { it.status == PostStatus.PUBLISHED }
    }
    
    fun getRecentPosts(days: Int = 30): List<Post> {
        val cutoffDate = java.time.LocalDateTime.now().minusDays(days.toLong())
        return posts.filter { it.createdAt.isAfter(cutoffDate) }
    }
    
    fun getPublishedPosts(): List<Post> {
        return posts.filter { it.status == PostStatus.PUBLISHED }
    }
    
    fun getMostViewedPosts(limit: Int = 10): List<Post> {
        return posts
            .filter { it.status == PostStatus.PUBLISHED }
            .sortedByDescending { it.viewCount }
            .take(limit)
    }
    
    fun getMostLikedPosts(limit: Int = 10): List<Post> {
        return posts
            .filter { it.status == PostStatus.PUBLISHED }
            .sortedByDescending { it.likeCount }
            .take(limit)
    }
    
    fun getTotalViews(): Long {
        return posts.sumOf { it.viewCount }
    }
    
    fun getTotalLikes(): Long {
        return posts.sumOf { it.likeCount }
    }
    
    fun getAveragePostViews(): Double {
        val publishedPosts = getPublishedPosts()
        return if (publishedPosts.isNotEmpty()) {
            publishedPosts.map { it.viewCount }.average()
        } else {
            0.0
        }
    }
    
    fun isPopular(): Boolean {
        // Consider popular if has more than 10 published posts or high view count
        return getPublishedPostCount() > 10 || getTotalViews() > 1000
    }
    
    fun canBeDeleted(): Boolean {
        // Can only delete if no published posts
        return getPublishedPostCount() == 0L
    }
    
    fun getSlug(): String {
        // Generate URL-friendly slug from name
        return name.lowercase()
            .replace("[^a-z0-9\\s\\-_]".toRegex(), "")
            .replace("\\s+".toRegex(), "-")
            .trim('-')
    }
    
    fun getDisplayName(): String {
        return if (description != null) {
            "$name - $description"
        } else {
            name
        }
    }
    
    fun isEmpty(): Boolean {
        return posts.isEmpty()
    }
    
    fun hasColor(): Boolean {
        return !color.isNullOrBlank()
    }
    
    fun getColorOrDefault(): String {
        return color ?: "#6c757d" // Bootstrap secondary color as default
    }
}