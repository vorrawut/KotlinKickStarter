/**
 * Lesson 7 Workshop: Project DTOs for Clean Architecture
 * 
 * TODO: Complete these DTOs for project management
 * This demonstrates:
 * - Aggregate boundary DTOs
 * - Complex validation scenarios
 * - Hierarchical data structures
 * - Service composition patterns
 */

package com.learning.architecture.dto

import com.learning.architecture.model.ProjectStatus
import jakarta.validation.Valid
import jakarta.validation.constraints.*
import java.time.LocalDate
import java.time.LocalDateTime

// TODO: Add comprehensive validation annotations
data class CreateProjectRequest(
    // TODO: Add @NotBlank, @Size validation
    val name: String,
    
    // TODO: Add @NotBlank validation
    val description: String,
    
    // TODO: Add @NotBlank, @Pattern validation for code format
    val code: String,
    
    // TODO: Add @Valid for nested object validation
    val owner: CreateProjectOwnerRequest,
    
    // TODO: Add @FutureOrPresent validation
    val startDate: LocalDate,
    
    // TODO: Add @Future validation
    val targetEndDate: LocalDate,
    
    // TODO: Add @Valid for initial tasks
    val initialTasks: List<CreateTaskRequest>?
) {
    // TODO: Add cross-field validation methods
    fun isValidDateRange(): Boolean {
        return TODO("Validate that end date is after start date")
    }
    
    fun isValidProjectCode(): Boolean {
        return TODO("Validate project code format and uniqueness requirements")
    }
}

// TODO: Add validation annotations
data class UpdateProjectRequest(
    val name: String?,
    val description: String?,
    val status: ProjectStatus?,
    val owner: UpdateProjectOwnerRequest?,
    val targetEndDate: LocalDate?
) {
    // TODO: Add validation methods
    fun hasValidUpdates(): Boolean {
        return TODO("Ensure at least one field is provided for update")
    }
    
    fun isValidStatusTransition(currentStatus: ProjectStatus): Boolean {
        return TODO("Validate project status transition rules")
    }
}

// TODO: Add validation for project owner creation
data class CreateProjectOwnerRequest(
    // TODO: Add @NotBlank validation
    val userId: String,
    
    // TODO: Add @NotBlank, @Size validation
    val name: String,
    
    // TODO: Add @Email validation
    val email: String,
    
    // TODO: Add @NotBlank validation
    val role: String
)

// TODO: Add validation for project owner updates
data class UpdateProjectOwnerRequest(
    val name: String?,
    val email: String?,
    val role: String?
)

// Response DTOs
data class ProjectResponse(
    val id: Long,
    val name: String,
    val description: String,
    val code: String,
    val status: ProjectStatus,
    val owner: ProjectOwnerResponse,
    val startDate: String,
    val targetEndDate: String,
    val actualEndDate: String?,
    val createdAt: String,
    val updatedAt: String,
    val completionPercentage: Double,
    val isOverdue: Boolean,
    val daysRemaining: Long,
    val taskSummary: ProjectTaskSummary,
    val metrics: ProjectMetricsResponse
)

data class ProjectOwnerResponse(
    val userId: String,
    val name: String,
    val email: String,
    val role: String
)

data class ProjectTaskSummary(
    val totalTasks: Int,
    val completedTasks: Int,
    val inProgressTasks: Int,
    val overdueTasks: Int,
    val upcomingTasks: Int
)

data class ProjectMetricsResponse(
    val totalTasks: Int,
    val completedTasks: Int,
    val inProgressTasks: Int,
    val overdueTasks: Int,
    val averageTaskDuration: Double,
    val projectHealthScore: Double,
    val needsAttention: List<String>
)

// TODO: Add validation for project search
data class ProjectSearchRequest(
    val name: String?,
    val code: String?,
    val status: ProjectStatus?,
    val ownerId: String?,
    val startDateAfter: LocalDate?,
    val startDateBefore: LocalDate?,
    val endDateAfter: LocalDate?,
    val endDateBefore: LocalDate?,
    val isOverdue: Boolean?,
    
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
        return TODO("Validate search parameters and date ranges")
    }
}

// TODO: Add validation for project completion
data class CompleteProjectRequest(
    // TODO: Add @PastOrPresent validation
    val actualEndDate: LocalDate,
    
    val completionNotes: String?,
    val lessonsLearned: String?
)

// TODO: Add validation for adding tasks to project
data class AddTaskToProjectRequest(
    // TODO: Add @Valid validation
    val task: CreateTaskRequest,
    
    val assignImmediately: Boolean = false
)

// TODO: Add validation for project assignment operations
data class AssignProjectTaskRequest(
    // TODO: Add @NotNull validation
    val taskId: Long,
    
    // TODO: Add @NotBlank validation
    val assigneeId: String,
    
    val newDueDate: LocalDateTime?,
    val reason: String?
)

// TODO: Add validation for bulk project operations
data class BulkProjectUpdateRequest(
    // TODO: Add @NotEmpty validation
    val projectIds: Set<Long>,
    
    val status: ProjectStatus?,
    val owner: UpdateProjectOwnerRequest?,
    val targetEndDate: LocalDate?,
    
    // TODO: Add @Size validation
    val reason: String?
) {
    // TODO: Add bulk validation
    fun isValidBulkOperation(): Boolean {
        return TODO("Validate bulk operation constraints")
    }
}

// Project dashboard and reporting DTOs
data class ProjectDashboardResponse(
    val totalProjects: Int,
    val activeProjects: Int,
    val completedProjects: Int,
    val overdueProjects: Int,
    val projectsByStatus: Map<ProjectStatus, Int>,
    val upcomingDeadlines: List<ProjectResponse>,
    val healthScoreDistribution: Map<String, Int>,
    val topPerformingProjects: List<ProjectResponse>
)

data class ProjectReportRequest(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val includeCompleted: Boolean = true,
    val includeMetrics: Boolean = true,
    val groupBy: String = "status" // status, owner, month
)

data class ProjectReportResponse(
    val reportPeriod: String,
    val totalProjects: Int,
    val projectBreakdown: Map<String, Int>,
    val averageCompletionTime: Double,
    val successRate: Double,
    val commonDelayReasons: List<String>,
    val recommendations: List<String>
)