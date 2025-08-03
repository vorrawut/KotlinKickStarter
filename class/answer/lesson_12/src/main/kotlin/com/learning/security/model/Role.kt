/**
 * Lesson 12 Complete Solution: Role Entity
 * 
 * Complete Role entity for implementing role-based access control (RBAC)
 * with predefined roles and relationship management
 */

package com.learning.security.model

import jakarta.persistence.*
import jakarta.validation.constraints.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(
    name = "roles",
    indexes = [
        Index(name = "idx_roles_name", columnList = "name")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_role_name", columnNames = ["name"])
    ]
)
data class Role(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(unique = true, nullable = false, length = 30)
    @NotBlank(message = "Role name is required")
    @Size(min = 2, max = 30, message = "Role name must be 2-30 characters")
    @Pattern(regexp = "^[A-Z_]+$", message = "Role name must be uppercase letters and underscores only")
    val name: String,
    
    @Column(nullable = false, length = 100)
    @NotBlank(message = "Role description is required")
    @Size(max = 100, message = "Role description cannot exceed 100 characters")
    val description: String,
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
    
) {
    
    companion object {
        // Standard role constants
        const val ROLE_USER = "USER"
        const val ROLE_ADMIN = "ADMIN"
        const val ROLE_MODERATOR = "MODERATOR"
        
        // Helper methods for creating standard roles
        fun createUserRole(): Role {
            return Role(
                name = ROLE_USER,
                description = "Standard user with basic access permissions"
            )
        }
        
        fun createAdminRole(): Role {
            return Role(
                name = ROLE_ADMIN,
                description = "Administrator with full system access"
            )
        }
        
        fun createModeratorRole(): Role {
            return Role(
                name = ROLE_MODERATOR,
                description = "Moderator with content management permissions"
            )
        }
        
        fun getDefaultRoles(): List<Role> {
            return listOf(
                createUserRole(),
                createAdminRole(),
                createModeratorRole()
            )
        }
    }
    
    // Business logic methods
    fun isAdminRole(): Boolean = name == ROLE_ADMIN
    
    fun isUserRole(): Boolean = name == ROLE_USER
    
    fun isModeratorRole(): Boolean = name == ROLE_MODERATOR
    
    fun hasHigherPrivilegesThan(otherRole: Role): Boolean {
        return when {
            this.isAdminRole() -> true
            this.isModeratorRole() -> otherRole.isUserRole()
            else -> false
        }
    }
    
    fun getDisplayName(): String {
        return name.lowercase().replaceFirstChar { it.uppercase() }
    }
    
    fun toSpringSecurityRole(): String {
        return "ROLE_$name"
    }
}