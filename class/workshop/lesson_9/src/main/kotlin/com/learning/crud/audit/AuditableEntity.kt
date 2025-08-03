/**
 * Lesson 9 Workshop: Auditable Entity Base Class
 * 
 * TODO: Create base class for all auditable entities
 * This class should provide automatic tracking of creation and modification timestamps
 */

package com.learning.crud.audit

// TODO: Import necessary JPA annotations
// TODO: Import Spring Data JPA auditing annotations
// TODO: Import @EntityListeners
// TODO: Import AuditingEntityListener
// TODO: Import LocalDateTime
import java.time.LocalDateTime

// TODO: Add @MappedSuperclass annotation
// TODO: Add @EntityListeners(AuditingEntityListener::class) annotation
abstract class AuditableEntity {
    
    // TODO: Add @CreatedDate annotation
    // TODO: Add @Column annotation with appropriate settings
    lateinit var createdAt: LocalDateTime
    
    // TODO: Add @LastModifiedDate annotation
    // TODO: Add @Column annotation with appropriate settings
    lateinit var updatedAt: LocalDateTime
    
    // TODO: Add @CreatedBy annotation
    // TODO: Add @Column annotation with appropriate settings
    var createdBy: Long? = null
    
    // TODO: Add @LastModifiedBy annotation
    // TODO: Add @Column annotation with appropriate settings
    var updatedBy: Long? = null
    
    // TODO: Add utility methods for auditing
    fun isCreatedBy(userId: Long): Boolean {
        // TODO: Check if entity was created by specified user
        return false
    }
    
    fun isModifiedRecently(hours: Long = 24): Boolean {
        // TODO: Check if entity was modified within specified hours
        return false
    }
    
    fun getAgeInDays(): Long {
        // TODO: Calculate age of entity in days since creation
        return 0
    }
}