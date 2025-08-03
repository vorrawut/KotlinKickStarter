/**
 * Lesson 7 Workshop: Task Domain Model
 * 
 * TODO: Complete this domain model for clean architecture
 * This demonstrates:
 * - Rich domain models with business logic
 * - Domain-driven design principles
 * - Value objects and entities
 * - Domain invariants
 */

package com.learning.architecture.model

import jakarta.persistence.*
import java.time.LocalDateTime

// TODO: Add JPA annotations for entity mapping
data class Task(
    // TODO: Add @Id and @GeneratedValue annotations
    val id: Long? = null,
    
    // TODO: Add @Column annotations with validation constraints
    val title: String,
    val description: String,
    
    // TODO: Add @Enumerated annotation for status
    val status: TaskStatus,
    
    // TODO: Add @Enumerated annotation for priority  
    val priority: TaskPriority,
    
    // TODO: Add @Embedded annotation for assignee
    val assignee: Assignee?,
    
    // TODO: Add proper date/time mapping
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val dueDate: LocalDateTime?
) {
    
    // TODO: Add domain business logic methods
    fun isOverdue(): Boolean {
        return TODO("Check if task is overdue based on due date and current status")
    }
    
    fun canBeAssignedTo(assignee: Assignee): Boolean {
        return TODO("Validate if task can be assigned to specific person")
    }
    
    fun canTransitionTo(newStatus: TaskStatus): Boolean {
        return TODO("Validate allowed status transitions")
    }
    
    fun isHighPriority(): Boolean {
        return TODO("Check if task has high or critical priority")
    }
    
    fun getDaysUntilDue(): Long? {
        return TODO("Calculate days until due date")
    }
    
    fun getEstimatedEffort(): Int {
        return TODO("Calculate estimated effort based on priority and complexity")
    }
    
    // TODO: Add factory methods for creating tasks
    companion object {
        fun createNew(title: String, description: String, priority: TaskPriority): Task {
            return TODO("Create new task with default values")
        }
        
        fun createWithDueDate(title: String, description: String, priority: TaskPriority, dueDate: LocalDateTime): Task {
            return TODO("Create new task with due date")
        }
    }
}

// TODO: Complete task status enum with business rules
enum class TaskStatus(val displayName: String, val isTerminal: Boolean) {
    CREATED("Created", false),
    IN_PROGRESS("In Progress", false),
    IN_REVIEW("In Review", false),
    COMPLETED("Completed", true),
    CANCELLED("Cancelled", true)
}

// TODO: Complete priority enum with business impact
enum class TaskPriority(val displayName: String, val level: Int) {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3),
    CRITICAL("Critical", 4)
}

// TODO: Create value object for assignee information
data class Assignee(
    val userId: String,
    val name: String,
    val email: String,
    val department: String
) {
    // TODO: Add validation methods
    fun isValid(): Boolean {
        return TODO("Validate assignee information completeness")
    }
    
    fun canHandlePriority(priority: TaskPriority): Boolean {
        return TODO("Check if assignee can handle tasks of given priority")
    }
}

// TODO: Create value object for task metadata
data class TaskMetadata(
    val tags: Set<String>,
    val category: String,
    val estimatedHours: Int?,
    val actualHours: Int?
) {
    // TODO: Add business logic for metadata
    fun isEstimateAccurate(): Boolean {
        return TODO("Compare estimated vs actual hours")
    }
    
    fun getEfficiencyRatio(): Double? {
        return TODO("Calculate efficiency ratio if both estimates exist")
    }
}