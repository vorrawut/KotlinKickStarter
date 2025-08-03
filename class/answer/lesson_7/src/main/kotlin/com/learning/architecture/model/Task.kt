/**
 * Lesson 7 Complete Solution: Task Domain Model with Rich Business Logic
 */

package com.learning.architecture.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Entity
@Table(name = "tasks")
data class Task(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, length = 100)
    val title: String,
    
    @Column(nullable = false, length = 1000)
    val description: String,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: TaskStatus,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val priority: TaskPriority,
    
    @Embedded
    val assignee: Assignee?,
    
    @Column(name = "project_id")
    val projectId: Long? = null,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "due_date")
    val dueDate: LocalDateTime?
) {
    
    fun isOverdue(): Boolean {
        return dueDate?.isBefore(LocalDateTime.now()) == true && 
               status != TaskStatus.COMPLETED
    }
    
    fun canBeAssignedTo(assignee: Assignee): Boolean {
        return status in listOf(TaskStatus.CREATED, TaskStatus.IN_PROGRESS) &&
               assignee.isValid()
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
    
    fun isHighPriority(): Boolean {
        return priority in listOf(TaskPriority.HIGH, TaskPriority.CRITICAL)
    }
    
    fun getDaysUntilDue(): Long? {
        return dueDate?.let { 
            ChronoUnit.DAYS.between(LocalDateTime.now(), it) 
        }
    }
    
    fun getEstimatedEffort(): Int {
        return when (priority) {
            TaskPriority.LOW -> 2
            TaskPriority.MEDIUM -> 4
            TaskPriority.HIGH -> 8
            TaskPriority.CRITICAL -> 12
        }
    }
    
    companion object {
        fun createNew(title: String, description: String, priority: TaskPriority): Task {
            return Task(
                title = title,
                description = description,
                status = TaskStatus.CREATED,
                priority = priority,
                assignee = null,
                dueDate = null
            )
        }
        
        fun createWithDueDate(title: String, description: String, priority: TaskPriority, dueDate: LocalDateTime): Task {
            return Task(
                title = title,
                description = description,
                status = TaskStatus.CREATED,
                priority = priority,
                assignee = null,
                dueDate = dueDate
            )
        }
    }
}

enum class TaskStatus(val displayName: String, val isTerminal: Boolean) {
    CREATED("Created", false),
    IN_PROGRESS("In Progress", false),
    IN_REVIEW("In Review", false),
    COMPLETED("Completed", true),
    CANCELLED("Cancelled", true)
}

enum class TaskPriority(val displayName: String, val level: Int) {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3),
    CRITICAL("Critical", 4)
}

@Embeddable
data class Assignee(
    @Column(name = "assignee_user_id")
    val userId: String,
    
    @Column(name = "assignee_name")
    val name: String,
    
    @Column(name = "assignee_email")
    val email: String,
    
    @Column(name = "assignee_department")
    val department: String
) {
    fun isValid(): Boolean {
        return userId.isNotBlank() && 
               name.isNotBlank() && 
               email.contains("@") &&
               department.isNotBlank()
    }
    
    fun canHandlePriority(priority: TaskPriority): Boolean {
        return true // Simple business rule
    }
}

@Embeddable
data class TaskMetadata(
    @ElementCollection
    @Column(name = "tag")
    val tags: Set<String> = emptySet(),
    
    @Column(name = "category")
    val category: String = "",
    
    @Column(name = "estimated_hours")
    val estimatedHours: Int? = null,
    
    @Column(name = "actual_hours")
    val actualHours: Int? = null
) {
    fun isEstimateAccurate(): Boolean {
        return if (estimatedHours != null && actualHours != null) {
            val difference = kotlin.math.abs(estimatedHours - actualHours)
            difference <= (estimatedHours * 0.2) // Within 20% is considered accurate
        } else false
    }
    
    fun getEfficiencyRatio(): Double? {
        return if (estimatedHours != null && actualHours != null && actualHours > 0) {
            estimatedHours.toDouble() / actualHours
        } else null
    }
}