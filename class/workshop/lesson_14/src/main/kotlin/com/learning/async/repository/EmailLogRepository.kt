package com.learning.async.repository

import com.learning.async.model.EmailNotification
import com.learning.async.model.EmailStatus
import com.learning.async.model.EmailType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface EmailLogRepository : JpaRepository<EmailNotification, Long> {
    
    fun findByStatus(status: EmailStatus): List<EmailNotification>
    
    fun findByType(type: EmailType): List<EmailNotification>
    
    @Query("SELECT COUNT(e) FROM EmailNotification e WHERE e.sentAt BETWEEN :startDate AND :endDate")
    fun countBySentAtBetween(
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): Long
    
    @Query("SELECT e FROM EmailNotification e WHERE e.status = :status AND e.createdAt < :before")
    fun findFailedEmailsOlderThan(
        @Param("status") status: EmailStatus,
        @Param("before") before: LocalDateTime
    ): List<EmailNotification>
}