/**
 * Lesson 10 Complete Solution: Category Entity
 * 
 * Complete Category entity for organizing and filtering posts
 */

package com.learning.pagination.model

import jakarta.persistence.*
import jakarta.validation.constraints.*

@Entity
@Table(
    name = "categories",
    indexes = [
        Index(name = "idx_categories_name", columnList = "name"),
        Index(name = "idx_categories_active", columnList = "is_active")
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
    @Size(min = 2, max = 100)
    val name: String,
    
    @Column(length = 500)
    @Size(max = 500)
    val description: String? = null,
    
    @Column(length = 7)
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex color")
    val color: String? = null,
    
    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,
    
    @ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY)
    val posts: Set<Post> = emptySet()
    
) {
    
    fun isAvailableForFiltering(): Boolean = isActive
    
    fun getPostCount(): Long = posts.size.toLong()
    
    fun getPublishedPostCount(): Long = posts.count { it.status == PostStatus.PUBLISHED }.toLong()
    
    fun getPopularityScore(): Double {
        val publishedPosts = getPublishedPostCount()
        if (publishedPosts == 0L) return 0.0
        
        val totalViews = posts.filter { it.status == PostStatus.PUBLISHED }.sumOf { it.viewCount }
        return totalViews.toDouble() / publishedPosts
    }
    
    fun getSlug(): String {
        return name.lowercase()
            .replace("[^a-z0-9\\s\\-_]".toRegex(), "")
            .replace("\\s+".toRegex(), "-")
            .trim('-')
    }
    
    fun getColorOrDefault(): String = color ?: "#6c757d"
}