/**
 * Lesson 12 Complete Solution: User Entity
 * 
 * Complete User entity with comprehensive security features, role relationships,
 * and account status management for Spring Security integration
 */

package com.learning.security.model

import jakarta.persistence.*
import jakarta.validation.constraints.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
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
    
    @Column(unique = true, nullable = false, length = 50)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    val username: String,
    
    @Column(unique = true, nullable = false, length = 100)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    val email: String,
    
    @Column(nullable = false)
    @NotBlank(message = "Password is required")
    val password: String, // BCrypt hashed password
    
    @Column(name = "first_name", nullable = false, length = 50)
    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 50, message = "First name must be 1-50 characters")
    val firstName: String,
    
    @Column(name = "last_name", nullable = false, length = 50)
    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 50, message = "Last name must be 1-50 characters")
    val lastName: String,
    
    // Account status fields for Spring Security UserDetails
    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,
    
    @Column(name = "is_locked", nullable = false)
    val isLocked: Boolean = false,
    
    @Column(name = "is_credentials_expired", nullable = false)
    val isCredentialsExpired: Boolean = false,
    
    @Column(name = "is_enabled", nullable = false)
    val isEnabled: Boolean = true,
    
    // Many-to-Many relationship with roles
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    val roles: Set<Role> = setOf(),
    
    // Audit fields
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    // Security tracking fields
    @Column(name = "last_login_at")
    val lastLoginAt: LocalDateTime? = null,
    
    @Column(name = "failed_login_attempts", nullable = false)
    val failedLoginAttempts: Int = 0
    
) {
    
    // Spring Security UserDetails compatibility methods
    fun getAuthorities(): List<GrantedAuthority> {
        return roles.map { role ->
            SimpleGrantedAuthority("ROLE_${role.name}")
        }
    }
    
    fun isAccountNonExpired(): Boolean = isActive
    fun isAccountNonLocked(): Boolean = !isLocked
    fun isCredentialsNonExpired(): Boolean = !isCredentialsExpired
    
    // Business logic methods
    fun getFullName(): String = "$firstName $lastName"
    
    fun getDisplayName(): String = "$firstName $lastName (@$username)"
    
    fun hasRole(roleName: String): Boolean {
        return roles.any { it.name == roleName }
    }
    
    fun hasAnyRole(vararg roleNames: String): Boolean {
        return roleNames.any { hasRole(it) }
    }
    
    fun canLogin(): Boolean {
        return isActive && !isLocked && isEnabled && !isCredentialsExpired
    }
    
    fun isAdmin(): Boolean = hasRole(Role.ROLE_ADMIN)
    
    fun isUser(): Boolean = hasRole(Role.ROLE_USER)
    
    fun getEmailDomain(): String {
        return email.substringAfter("@")
    }
    
    fun getInitials(): String {
        return "${firstName.first()}${lastName.first()}".uppercase()
    }
    
    fun isNewUser(dayThreshold: Int = 30): Boolean {
        return createdAt.isAfter(LocalDateTime.now().minusDays(dayThreshold.toLong()))
    }
    
    fun isRecentlyActive(hourThreshold: Int = 24): Boolean {
        return lastLoginAt?.isAfter(LocalDateTime.now().minusHours(hourThreshold.toLong())) ?: false
    }
    
    fun shouldLockDueToFailedAttempts(maxAttempts: Int = 5): Boolean {
        return failedLoginAttempts >= maxAttempts
    }
    
    fun getRoleNames(): Set<String> {
        return roles.map { it.name }.toSet()
    }
    
    // Copy methods for immutable updates
    fun withUpdatedPassword(newPassword: String): User {
        return copy(password = newPassword, updatedAt = LocalDateTime.now())
    }
    
    fun withUpdatedLoginInfo(loginTime: LocalDateTime, resetFailedAttempts: Boolean = true): User {
        return copy(
            lastLoginAt = loginTime,
            failedLoginAttempts = if (resetFailedAttempts) 0 else failedLoginAttempts,
            updatedAt = LocalDateTime.now()
        )
    }
    
    fun withIncrementedFailedAttempts(): User {
        return copy(
            failedLoginAttempts = failedLoginAttempts + 1,
            updatedAt = LocalDateTime.now()
        )
    }
    
    fun withLockedStatus(locked: Boolean): User {
        return copy(
            isLocked = locked,
            updatedAt = LocalDateTime.now()
        )
    }
    
    fun withActiveStatus(active: Boolean): User {
        return copy(
            isActive = active,
            updatedAt = LocalDateTime.now()
        )
    }
}