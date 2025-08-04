package com.learning.async.repository

import com.learning.async.model.AuditLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Repository
interface AuditLogRepository : JpaRepository<AuditLog, Long> {
    
    fun findByAction(action: String): List<AuditLog>
    
    fun findByEntityTypeAndEntityId(entityType: String, entityId: Long): List<AuditLog>
    
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.action = :action AND a.createdAt BETWEEN :startDate AND :endDate")
    fun countByActionAndCreatedAtBetween(
        @Param("action") action: String,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): Long
    
    @Modifying
    @Transactional
    @Query("DELETE FROM AuditLog a WHERE a.createdAt < :cutoffDate")
    fun deleteByCreatedAtBefore(@Param("cutoffDate") cutoffDate: LocalDateTime): Int
    
    @Query("SELECT a FROM AuditLog a WHERE a.createdAt >= :since ORDER BY a.createdAt DESC")
    fun findRecentLogs(@Param("since") since: LocalDateTime): List<AuditLog>
}