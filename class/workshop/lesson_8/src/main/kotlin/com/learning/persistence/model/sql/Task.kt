/**
 * Lesson 8 Workshop: SQL Entity - Task
 * 
 * TODO: Create a complete JPA entity for Task management
 * This entity represents the core task data stored in SQL database
 */

package com.learning.persistence.model.sql

// TODO: Import necessary JPA annotations
// TODO: Import validation annotations
// TODO: Import LocalDateTime for date handling
import java.time.LocalDateTime

// TODO: Add @Entity annotation
// TODO: Add @Table annotation with name "tasks"
// TODO: Add indexes for frequently queried fields (status, assignee_id, project_id, due_date)
data class Task(
    // TODO: Add @Id and @GeneratedValue annotations for primary key
    val id: Long? = null,
    
    // TODO: Add @Column annotation with nullable = false
    val title: String,
    
    // TODO: Add @Column annotation for description (can be nullable)
    val description: String?,
    
    // TODO: Add @Enumerated annotation with EnumType.STRING
    val status: TaskStatus,
    
    // TODO: Add @Enumerated annotation with EnumType.STRING
    val priority: TaskPriority,
    
    // TODO: Add @Column annotation for assignee (foreign key to user)
    val assigneeId: String?,
    
    // TODO: Add @Column annotation for project (foreign key to project)
    val projectId: Long?,
    
    // TODO: Add @Column annotation with name "created_at", nullable = false
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    // TODO: Add @Column annotation with name "updated_at", nullable = false
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    // TODO: Add @Column annotation with name "due_date" (nullable)
    val dueDate: LocalDateTime?
) {
    // TODO: Add business logic methods
    fun isOverdue(): Boolean {
        // TODO: Implement logic to check if task is overdue
        return false
    }
    
    fun canBeAssignedTo(userId: String): Boolean {
        // TODO: Implement logic to check if task can be assigned to user
        return true
    }
    
    fun isHighPriority(): Boolean {
        // TODO: Implement logic to check if task is high priority
        return false
    }
}

// TODO: Create TaskStatus enum
enum class TaskStatus {
    // TODO: Add status values: CREATED, IN_PROGRESS, IN_REVIEW, COMPLETED, CANCELLED
}

// TODO: Create TaskPriority enum with business logic
enum class TaskPriority(val displayName: String, val level: Int) {
    // TODO: Add priority values with display names and numeric levels
    // LOW("Low", 1), MEDIUM("Medium", 2), HIGH("High", 3), CRITICAL("Critical", 4)
}