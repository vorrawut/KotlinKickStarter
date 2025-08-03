/**
 * Lesson 7 Workshop: Task DTOs for Clean Architecture
 * 
 * TODO: Complete these DTOs for service layer separation
 * This demonstrates:
 * - API/Domain separation with DTOs
 * - Input validation at boundaries
 * - Response formatting
 * - Clean interfaces between layers
 */

package com.learning.architecture.dto

import com.learning.architecture.model.TaskPriority
import com.learning.architecture.model.TaskStatus
import jakarta.validation.Valid
import jakarta.validation.constraints.*
import java.time.LocalDateTime

// TODO: Add comprehensive validation annotations
data class CreateTaskRequest(
    // TODO: Add @NotBlank, @Size validation
    val title: String,
    
    // TODO: Add @NotBlank, @Size validation  
    val description: String,
    
    // TODO: Add @NotNull validation
    val priority: TaskPriority,
    
    // TODO: Add @Future validation for due date
    val dueDate: LocalDateTime?,
    
    // TODO: Add @Valid for nested object validation
    val assignee: CreateAssigneeRequest?,
    
    // TODO: Add @Valid for metadata
    val metadata: CreateTaskMetadataRequest?
) {
    // TODO: Add cross-field validation methods
    fun isValidForCreation(): Boolean {
        return TODO("Validate business rules for task creation")
    }
}

// TODO: Add validation annotations
data class UpdateTaskRequest(
    val title: String?,
    val description: String?,
    val priority: TaskPriority?,
    val status: TaskStatus?,
    val dueDate: LocalDateTime?,
    val assignee: UpdateAssigneeRequest?,
    val metadata: UpdateTaskMetadataRequest?
) {
    // TODO: Add validation methods
    fun hasValidUpdates(): Boolean {
        return TODO("Ensure at least one field is provided for update")
    }
    
    fun isValidStatusTransition(currentStatus: TaskStatus): Boolean {
        return TODO("Validate status transition rules")
    }
}

// TODO: Add validation for assignee creation
data class CreateAssigneeRequest(
    // TODO: Add @NotBlank validation
    val userId: String,
    
    // TODO: Add @NotBlank, @Size validation
    val name: String,
    
    // TODO: Add @Email validation
    val email: String,
    
    // TODO: Add @NotBlank validation
    val department: String
)

// TODO: Add validation for assignee updates
data class UpdateAssigneeRequest(
    val name: String?,
    val email: String?,
    val department: String?
)

// TODO: Add validation for task metadata
data class CreateTaskMetadataRequest(
    // TODO: Add @Size validation for tags
    val tags: Set<String>,
    
    // TODO: Add @NotBlank validation
    val category: String,
    
    // TODO: Add @Positive validation
    val estimatedHours: Int?
)

// TODO: Add validation for metadata updates
data class UpdateTaskMetadataRequest(
    val tags: Set<String>?,
    val category: String?,
    val estimatedHours: Int?,
    val actualHours: Int?
)

// Response DTOs (no validation needed, only for output)
data class TaskResponse(
    val id: Long,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val priority: TaskPriority,
    val assignee: AssigneeResponse?,
    val metadata: TaskMetadataResponse?,
    val projectId: Long?,
    val createdAt: String,
    val updatedAt: String,
    val dueDate: String?,
    val isOverdue: Boolean,
    val daysUntilDue: Long?,
    val estimatedEffort: Int
)

data class AssigneeResponse(
    val userId: String,
    val name: String,
    val email: String,
    val department: String
)

data class TaskMetadataResponse(
    val tags: Set<String>,
    val category: String,
    val estimatedHours: Int?,
    val actualHours: Int?,
    val efficiencyRatio: Double?
)

// TODO: Add validation for task search/filter requests
data class TaskSearchRequest(
    val title: String?,
    val status: TaskStatus?,
    val priority: TaskPriority?,
    val assigneeId: String?,
    val projectId: Long?,
    val category: String?,
    val tags: Set<String>?,
    val isOverdue: Boolean?,
    val dueBefore: LocalDateTime?,
    val dueAfter: LocalDateTime?,
    
    // Pagination
    // TODO: Add @Min, @Max validation
    val page: Int = 0,
    
    // TODO: Add @Min, @Max validation  
    val size: Int = 20,
    
    val sortBy: String = "createdAt",
    val sortDirection: String = "DESC"
) {
    // TODO: Add search validation
    fun isValidSearchCriteria(): Boolean {
        return TODO("Validate search parameters")
    }
}

// TODO: Add validation for bulk operations
data class BulkTaskUpdateRequest(
    // TODO: Add @NotEmpty validation
    val taskIds: Set<Long>,
    
    val status: TaskStatus?,
    val priority: TaskPriority?,
    val assignee: UpdateAssigneeRequest?,
    
    // TODO: Add @Size validation for reason
    val reason: String?
) {
    // TODO: Add bulk operation validation
    fun isValidBulkOperation(): Boolean {
        return TODO("Validate bulk operation constraints")
    }
}

// TODO: Add validation for task assignment
data class AssignTaskRequest(
    // TODO: Add @NotNull validation
    val assigneeId: String,
    
    // TODO: Add @Future validation
    val newDueDate: LocalDateTime?,
    
    val reason: String?
)

// TODO: Add validation for task completion
data class CompleteTaskRequest(
    // TODO: Add @Positive validation
    val actualHours: Int?,
    
    val completionNotes: String?
)

// Summary and reporting DTOs
data class TaskSummaryResponse(
    val totalTasks: Int,
    val tasksByStatus: Map<TaskStatus, Int>,
    val tasksByPriority: Map<TaskPriority, Int>,
    val overdueTasks: Int,
    val averageCompletionTime: Double,
    val upcomingDueTasks: List<TaskResponse>
)

data class TaskMetricsResponse(
    val completionRate: Double,
    val averageEfficiency: Double,
    val mostCommonCategory: String,
    val topPerformers: List<AssigneeResponse>,
    val bottlenecks: List<String>
)