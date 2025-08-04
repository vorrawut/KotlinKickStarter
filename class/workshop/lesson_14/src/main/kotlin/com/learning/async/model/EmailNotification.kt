package com.learning.async.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "email_notifications")
data class EmailNotification(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false)
    val recipient: String,
    
    @Column(nullable = false)
    val subject: String,
    
    @Column(nullable = false, columnDefinition = "TEXT")
    val content: String,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: EmailType = EmailType.GENERAL,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: EmailStatus = EmailStatus.PENDING,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column
    val sentAt: LocalDateTime? = null,
    
    @Column
    val error: String? = null
)

enum class EmailType {
    WELCOME, PASSWORD_RESET, NOTIFICATION, GENERAL
}

enum class EmailStatus {
    PENDING, SENT, FAILED, RETRYING
}