package com.learning.async.model

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "daily_statistics")
data class DailyStatistics(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, unique = true)
    val date: LocalDate,
    
    @Column(nullable = false)
    val userRegistrations: Long = 0,
    
    @Column(nullable = false)
    val emailsSent: Long = 0,
    
    @Column(nullable = false)
    val loginAttempts: Long = 0,
    
    @Column(nullable = false)
    val tasksExecuted: Long = 0,
    
    @Column(nullable = false)
    val generatedAt: LocalDateTime = LocalDateTime.now()
)