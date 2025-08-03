/**
 * Lesson 11 Complete Solution: User Entity
 * 
 * Complete User entity with comprehensive business logic for testing
 */

package com.learning.testing.model

import jakarta.persistence.*
import jakarta.validation.constraints.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "users",
    indexes = [
        Index(name = "idx_users_username", columnList = "username"),
        Index(name = "idx_users_email", columnList = "email"),
        Index(name = "idx_users_active", columnList = "is_active"),
        Index(name = "idx_users_created", columnList = "created_at")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_user_username", columnNames = ["username"]),
        UniqueConstraint(name = "uk_user_email", columnNames = ["email"])
    ]
)
data class User(
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
    
    @Column(name = "first_name", nullable = false, length = 50)
    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 50, message = "First name must be 1-50 characters")
    val firstName: String,
    
    @Column(name = "last_name", nullable = false, length = 50)
    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 50, message = "Last name must be 1-50 characters")
    val lastName: String,
    
    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val posts: List<Post> = emptyList()
    
) {
    
    // Business logic methods for testing
    fun getFullName(): String = "$firstName $lastName"
    
    fun getDisplayName(): String = "$firstName $lastName (@$username)"
    
    fun isEligibleForPosting(): Boolean = isActive
    
    fun getPostCount(): Int = posts.size
    
    fun getPublishedPostCount(): Int = posts.count { it.isPublished() }
    
    fun hasActivePosts(): Boolean = posts.any { it.status == PostStatus.PUBLISHED }
    
    fun isNewUser(dayThreshold: Int = 30): Boolean {
        return createdAt.isAfter(LocalDateTime.now().minusDays(dayThreshold.toLong()))
    }
    
    fun getEmailDomain(): String {
        return email.substringAfter("@")
    }
    
    fun canBeDeactivated(): Boolean {
        // Business rule: Cannot deactivate user with published posts
        return !hasActivePosts()
    }
    
    fun getInitials(): String {
        return "${firstName.first()}${lastName.first()}".uppercase()
    }
}