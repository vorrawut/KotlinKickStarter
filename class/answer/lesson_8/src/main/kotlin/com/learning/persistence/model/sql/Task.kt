/**
 * Lesson 8 Complete Solution: SQL Entity - Task
 * 
 * Complete JPA entity for Task management with proper annotations and business logic
 */

package com.learning.persistence.model.sql

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

@Entity
@Table(
    name = "tasks",
    indexes = [
        Index(name = "idx_task_status", columnList = "status"),
        Index(name = "idx_task_assignee", columnList = "assignee_id"),
        Index(name = "idx_task_project", columnList = "project_id"),
        Index(name = "idx_task_due_date", columnList = "due_date"),
        Index(name = "idx_task_created", columnList = "created_at")
    ]
)
data class Task(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, length = 100)
    @NotBlank
    @Size(min = 1, max = 100)
    val title: String,
    
    @Column(length = 1000)
    @Size(max = 1000)
    val description: String?,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: TaskStatus,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val priority: TaskPriority,
    
    @Column(name = "assignee_id", length = 50)
    val assigneeId: String?,
    
    @Column(name = "project_id")
    val projectId: Long?,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "due_date")
    val dueDate: LocalDateTime?
) {
    
    fun isOverdue(): Boolean {
        return dueDate?.isBefore(LocalDateTime.now()) == true && 
               status != TaskStatus.COMPLETED && status != TaskStatus.CANCELLED
    }
    
    fun canBeAssignedTo(userId: String): Boolean {
        return status in listOf(TaskStatus.CREATED, TaskStatus.IN_PROGRESS) &&
               userId.isNotBlank()
    }
    
    fun isHighPriority(): Boolean {
        return priority in listOf(TaskPriority.HIGH, TaskPriority.CRITICAL)
    }
    
    fun canTransitionTo(newStatus: TaskStatus): Boolean {
        return when (status) {
            TaskStatus.CREATED -> newStatus in listOf(TaskStatus.IN_PROGRESS, TaskStatus.CANCELLED)
            TaskStatus.IN_PROGRESS -> newStatus in listOf(TaskStatus.IN_REVIEW, TaskStatus.COMPLETED, TaskStatus.CANCELLED)
            TaskStatus.IN_REVIEW -> newStatus in listOf(TaskStatus.IN_PROGRESS, TaskStatus.COMPLETED)
            TaskStatus.COMPLETED -> false
            TaskStatus.CANCELLED -> false
        }
    }
    
    fun getDaysUntilDue(): Long? {
        return dueDate?.let { due ->
            val now = LocalDateTime.now()
            if (due.isAfter(now)) {
                java.time.Duration.between(now, due).toDays()
            } else {
                -java.time.Duration.between(due, now).toDays()
            }
        }
    }
    
    fun getEstimatedEffortHours(): Int {
        return when (priority) {
            TaskPriority.LOW -> 2
            TaskPriority.MEDIUM -> 4
            TaskPriority.HIGH -> 8
            TaskPriority.CRITICAL -> 16
        }
    }
}

enum class TaskStatus {
    CREATED,
    IN_PROGRESS,
    IN_REVIEW,
    COMPLETED,
    CANCELLED
}

enum class TaskPriority(val displayName: String, val level: Int) {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3),
    CRITICAL("Critical", 4)
}