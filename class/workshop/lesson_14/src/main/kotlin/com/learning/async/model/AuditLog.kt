package com.learning.async.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "audit_logs")
data class AuditLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false)
    val action: String,
    
    @Column(nullable = false)
    val entityType: String,
    
    @Column
    val entityId: Long? = null,
    
    @Column
    val userId: Long? = null,
    
    @Column(columnDefinition = "TEXT")
    val details: String? = null,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)