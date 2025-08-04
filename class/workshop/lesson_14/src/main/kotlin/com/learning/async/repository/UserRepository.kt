package com.learning.async.repository

import com.learning.async.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface UserRepository : JpaRepository<User, Long> {
    
    fun findByEmail(email: String): User?
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    fun countByCreatedAtBetween(
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): Long
    
    @Query("SELECT u FROM User u WHERE u.createdAt >= :since ORDER BY u.createdAt DESC")
    fun findRecentUsers(@Param("since") since: LocalDateTime): List<User>
}