/**
 * Lesson 9 Complete Solution: Auditable Entity Base Class
 * 
 * Complete base class for all auditable entities with automatic tracking
 */

package com.learning.crud.audit

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class AuditableEntity {
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime
    
    @LastModifiedDate
    @Column(nullable = false)
    lateinit var updatedAt: LocalDateTime
    
    @CreatedBy
    @Column(updatable = false)
    var createdBy: Long? = null
    
    @LastModifiedBy
    var updatedBy: Long? = null
    
    fun isCreatedBy(userId: Long): Boolean {
        return createdBy == userId
    }
    
    fun isModifiedRecently(hours: Long = 24): Boolean {
        if (!::updatedAt.isInitialized) return false
        val hoursAgo = LocalDateTime.now().minusHours(hours)
        return updatedAt.isAfter(hoursAgo)
    }
    
    fun getAgeInDays(): Long {
        if (!::createdAt.isInitialized) return 0
        return ChronoUnit.DAYS.between(createdAt, LocalDateTime.now())
    }
    
    fun wasModifiedAfterCreation(): Boolean {
        if (!::createdAt.isInitialized || !::updatedAt.isInitialized) return false
        return !createdAt.truncatedTo(ChronoUnit.SECONDS)
            .equals(updatedAt.truncatedTo(ChronoUnit.SECONDS))
    }
}