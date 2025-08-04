package com.learning.caching.repository

import com.learning.caching.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    
    fun findByUsername(username: String): User?
    
    fun findByEmail(email: String): User?
    
    @Query("SELECT u FROM User u WHERE u.firstName LIKE %:query% OR u.lastName LIKE %:query% OR u.username LIKE %:query%")
    fun searchUsers(@Param("query") query: String, pageable: Pageable): Page<User>
    
    @Query("SELECT u FROM User u ORDER BY u.createdAt DESC")
    fun findRecentUsers(pageable: Pageable): Page<User>
}