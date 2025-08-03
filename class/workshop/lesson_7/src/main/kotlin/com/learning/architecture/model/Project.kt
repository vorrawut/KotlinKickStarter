/**
 * Lesson 7 Workshop: Project Domain Model
 * 
 * TODO: Complete this aggregate root for clean architecture
 * This demonstrates:
 * - Aggregate patterns
 * - Entity relationships
 * - Domain services integration
 * - Business rule enforcement
 */

package com.learning.architecture.model

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

// TODO: Add JPA annotations for entity mapping
data class Project(
    // TODO: Add @Id and @GeneratedValue annotations
    val id: Long? = null,
    
    // TODO: Add @Column annotations with constraints
    val name: String,
    val description: String,
    val code: String, // Unique project code
    
    // TODO: Add @Enumerated annotation
    val status: ProjectStatus,
    
    // TODO: Add @Embedded annotation for project owner
    val owner: ProjectOwner,
    
    // TODO: Add @OneToMany relationship with tasks
    val tasks: MutableList<Task> = mutableListOf(),
    
    val startDate: LocalDate,
    val targetEndDate: LocalDate,
    val actualEndDate: LocalDate? = null,
    
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    
    // TODO: Add aggregate business logic methods
    fun addTask(task: Task): Project {
        return TODO("Add task to project with validation")
    }
    
    fun removeTask(taskId: Long): Project {
        return TODO("Remove task from project")
    }
    
    fun assignTaskTo(taskId: Long, assignee: Assignee): Project {
        return TODO("Assign specific task to assignee")
    }
    
    fun completeTask(taskId: Long): Project {
        return TODO("Mark task as completed and update project status if needed")
    }
    
    fun getCompletionPercentage(): Double {
        return TODO("Calculate project completion percentage based on tasks")
    }
    
    fun isOverdue(): Boolean {
        return TODO("Check if project is overdue")
    }
    
    fun getOverdueTasks(): List<Task> {
        return TODO("Get all overdue tasks in project")
    }
    
    fun getTasksByStatus(status: TaskStatus): List<Task> {
        return TODO("Filter tasks by status")
    }
    
    fun getTasksByPriority(priority: TaskPriority): List<Task> {
        return TODO("Filter tasks by priority")
    }
    
    fun getTasksByAssignee(assignee: Assignee): List<Task> {
        return TODO("Filter tasks by assignee")
    }
    
    fun canBeCompleted(): Boolean {
        return TODO("Check if project can be marked as completed")
    }
    
    fun getDaysRemaining(): Long {
        return TODO("Calculate days remaining until target end date")
    }
    
    // TODO: Add factory methods for project creation
    companion object {
        fun createNew(name: String, description: String, code: String, owner: ProjectOwner, startDate: LocalDate, endDate: LocalDate): Project {
            return TODO("Create new project with validation")
        }
    }
}

// TODO: Complete project status enum
enum class ProjectStatus(val displayName: String, val isActive: Boolean) {
    PLANNING("Planning", true),
    IN_PROGRESS("In Progress", true),
    ON_HOLD("On Hold", false),
    COMPLETED("Completed", false),
    CANCELLED("Cancelled", false)
}

// TODO: Create value object for project owner
data class ProjectOwner(
    val userId: String,
    val name: String,
    val email: String,
    val role: String
) {
    // TODO: Add validation methods
    fun canManageProject(): Boolean {
        return TODO("Check if owner has sufficient permissions")
    }
    
    fun isActive(): Boolean {
        return TODO("Check if owner is still active in system")
    }
}

// TODO: Create value object for project metrics
data class ProjectMetrics(
    val totalTasks: Int,
    val completedTasks: Int,
    val inProgressTasks: Int,
    val overdueTasks: Int,
    val averageTaskDuration: Double,
    val projectHealthScore: Double
) {
    // TODO: Add business logic for metrics
    fun getCompletionRate(): Double {
        return TODO("Calculate task completion rate")
    }
    
    fun isProjectHealthy(): Boolean {
        return TODO("Determine if project is healthy based on metrics")
    }
    
    fun getNeedsAttention(): List<String> {
        return TODO("Return list of areas needing attention")
    }
}