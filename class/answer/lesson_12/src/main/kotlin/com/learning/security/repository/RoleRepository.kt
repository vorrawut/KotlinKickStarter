/**
 * Lesson 12 Complete Solution: Role Repository
 * 
 * Complete repository for role management and role-based queries
 */

package com.learning.security.repository

import com.learning.security.model.Role
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface RoleRepository : JpaRepository<Role, Long> {
    
    // Basic role queries
    fun findByName(name: String): Role?
    
    fun existsByName(name: String): Boolean
    
    fun findAllByNameIn(names: List<String>): List<Role>
    
    // Role hierarchy and relationship queries
    @Query("SELECT r FROM Role r WHERE r.name IN ('USER', 'ADMIN', 'MODERATOR')")
    fun findStandardRoles(): List<Role>
    
    @Query("SELECT r FROM Role r WHERE r.name = 'USER'")
    fun findDefaultRole(): Role?
    
    // User-role relationship queries
    @Query("""
        SELECT r FROM Role r 
        JOIN User u ON r MEMBER OF u.roles 
        WHERE u.id = :userId
    """)
    fun findRolesByUserId(@Param("userId") userId: Long): Set<Role>
    
    @Query("""
        SELECT r FROM Role r 
        JOIN User u ON r MEMBER OF u.roles 
        WHERE u.username = :username
    """)
    fun findRolesByUsername(@Param("username") username: String): Set<Role>
    
    // Statistics and counts
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.id = :roleId")
    fun countUsersByRole(@Param("roleId") roleId: Long): Long
    
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
    fun countUsersByRoleName(@Param("roleName") roleName: String): Long
    
    // Administrative queries
    @Query("""
        SELECT r.name, COUNT(u) 
        FROM Role r 
        LEFT JOIN User u ON r MEMBER OF u.roles 
        GROUP BY r.name
    """)
    fun getRoleUsageStatistics(): List<Array<Any>>
}