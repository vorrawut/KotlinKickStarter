/**
 * Lesson 7 Workshop: Project Service Interface
 * 
 * TODO: Complete this service interface for project management
 * This demonstrates:
 * - Aggregate service patterns
 * - Service composition
 * - Business workflow orchestration
 * - Domain service interfaces
 */

package com.learning.architecture.service

import com.learning.architecture.dto.*
import com.learning.architecture.model.Project
import com.learning.architecture.model.ProjectStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDate

// TODO: Create main project service interface
interface ProjectService {
    
    // Core CRUD operations
    // TODO: Add method to create a new project
    fun createProject(request: CreateProjectRequest): ProjectResponse
    
    // TODO: Add method to get project by ID
    fun getProjectById(id: Long): ProjectResponse
    
    // TODO: Add method to get project by code
    fun getProjectByCode(code: String): ProjectResponse
    
    // TODO: Add method to update project
    fun updateProject(id: Long, request: UpdateProjectRequest): ProjectResponse
    
    // TODO: Add method to delete project
    fun deleteProject(id: Long)
    
    // TODO: Add method to get all projects with pagination
    fun getAllProjects(pageable: Pageable): Page<ProjectResponse>
    
    // Project lifecycle operations
    // TODO: Add method to start project
    fun startProject(projectId: Long): ProjectResponse
    
    // TODO: Add method to complete project
    fun completeProject(projectId: Long, request: CompleteProjectRequest): ProjectResponse
    
    // TODO: Add method to pause/hold project
    fun pauseProject(projectId: Long, reason: String): ProjectResponse
    
    // TODO: Add method to cancel project
    fun cancelProject(projectId: Long, reason: String): ProjectResponse
    
    // TODO: Add method to change project status
    fun changeProjectStatus(projectId: Long, newStatus: ProjectStatus, reason: String?): ProjectResponse
    
    // Project-task management
    // TODO: Add method to add task to project
    fun addTaskToProject(projectId: Long, request: AddTaskToProjectRequest): TaskResponse
    
    // TODO: Add method to remove task from project
    fun removeTaskFromProject(projectId: Long, taskId: Long, reason: String?): ProjectResponse
    
    // TODO: Add method to assign project task
    fun assignProjectTask(projectId: Long, request: AssignProjectTaskRequest): TaskResponse
    
    // TODO: Add method to get project tasks
    fun getProjectTasks(projectId: Long, pageable: Pageable): Page<TaskResponse>
    
    // TODO: Add method to get project tasks by status
    fun getProjectTasksByStatus(projectId: Long, status: com.learning.architecture.model.TaskStatus, pageable: Pageable): Page<TaskResponse>
    
    // Query operations
    // TODO: Add method to search projects
    fun searchProjects(searchRequest: ProjectSearchRequest): Page<ProjectResponse>
    
    // TODO: Add method to get projects by status
    fun getProjectsByStatus(status: ProjectStatus, pageable: Pageable): Page<ProjectResponse>
    
    // TODO: Add method to get projects by owner
    fun getProjectsByOwner(ownerId: String, pageable: Pageable): Page<ProjectResponse>
    
    // TODO: Add method to get overdue projects
    fun getOverdueProjects(pageable: Pageable): Page<ProjectResponse>
    
    // TODO: Add method to get projects ending soon
    fun getProjectsEndingSoon(days: Int, pageable: Pageable): Page<ProjectResponse>
    
    // Bulk operations
    // TODO: Add method for bulk project updates
    fun bulkUpdateProjects(request: BulkProjectUpdateRequest): List<ProjectResponse>
    
    // TODO: Add method for bulk status change
    fun bulkChangeStatus(projectIds: Set<Long>, status: ProjectStatus, reason: String?): List<ProjectResponse>
    
    // Analytics and reporting
    // TODO: Add method to get project dashboard
    fun getProjectDashboard(): ProjectDashboardResponse
    
    // TODO: Add method to generate project report
    fun generateProjectReport(request: ProjectReportRequest): ProjectReportResponse
    
    // TODO: Add method to get project metrics
    fun getProjectMetrics(projectId: Long): ProjectMetricsResponse
}

// TODO: Create specialized service interface for project validation
interface ProjectValidationService {
    
    // TODO: Add method to validate project creation
    fun validateProjectCreation(request: CreateProjectRequest): ValidationResult
    
    // TODO: Add method to validate project code uniqueness
    fun validateProjectCodeUniqueness(code: String): ValidationResult
    
    // TODO: Add method to validate project dates
    fun validateProjectDates(startDate: LocalDate, endDate: LocalDate): ValidationResult
    
    // TODO: Add method to validate project completion
    fun validateProjectCompletion(projectId: Long): ValidationResult
    
    // TODO: Add method to validate project status transition
    fun validateStatusTransition(projectId: Long, newStatus: ProjectStatus): ValidationResult
    
    // TODO: Add method to validate task addition to project
    fun validateTaskAddition(projectId: Long, request: AddTaskToProjectRequest): ValidationResult
    
    // TODO: Add method to validate bulk operations
    fun validateBulkOperation(request: BulkProjectUpdateRequest): ValidationResult
}

// TODO: Create service interface for project analytics
interface ProjectAnalyticsService {
    
    // TODO: Add method for project success rate analysis
    fun analyzeProjectSuccessRates(
        startDate: LocalDate,
        endDate: LocalDate,
        groupBy: String
    ): Map<String, Any>
    
    // TODO: Add method for project risk assessment
    fun assessProjectRisks(projectId: Long? = null): List<Map<String, Any>>
    
    // TODO: Add method for capacity planning
    fun analyzeCapacity(timeframe: String): Map<String, Any>
    
    // TODO: Add method for deadline prediction
    fun predictProjectDeadlines(projectId: Long): Map<String, Any>
    
    // TODO: Add method for portfolio analysis
    fun analyzePortfolio(filters: Map<String, Any>): Map<String, Any>
    
    // TODO: Add method for resource utilization
    fun analyzeResourceUtilization(): List<Map<String, Any>>
    
    // TODO: Add method for trend analysis
    fun analyzeTrends(
        startDate: LocalDate,
        endDate: LocalDate,
        metrics: List<String>
    ): List<Map<String, Any>>
}

// TODO: Create service interface for project planning
interface ProjectPlanningService {
    
    // TODO: Add method for project template creation
    fun createProjectTemplate(project: Project): ProjectTemplate
    
    // TODO: Add method for project creation from template
    fun createProjectFromTemplate(templateId: Long, request: CreateProjectRequest): ProjectResponse
    
    // TODO: Add method for milestone planning
    fun planMilestones(projectId: Long, milestones: List<MilestoneRequest>): List<MilestoneResponse>
    
    // TODO: Add method for resource planning
    fun planResources(projectId: Long, resources: List<ResourceRequest>): List<ResourceResponse>
    
    // TODO: Add method for timeline optimization
    fun optimizeTimeline(projectId: Long): TimelineOptimizationResult
    
    // TODO: Add method for dependency analysis
    fun analyzeDependencies(projectId: Long): DependencyAnalysisResult
}

// TODO: Create service interface for project collaboration
interface ProjectCollaborationService {
    
    // TODO: Add method to add team member
    fun addTeamMember(projectId: Long, userId: String, role: String): ProjectResponse
    
    // TODO: Add method to remove team member
    fun removeTeamMember(projectId: Long, userId: String, reason: String?): ProjectResponse
    
    // TODO: Add method to update team member role
    fun updateTeamMemberRole(projectId: Long, userId: String, newRole: String): ProjectResponse
    
    // TODO: Add method to get team members
    fun getTeamMembers(projectId: Long): List<TeamMemberResponse>
    
    // TODO: Add method for project communication
    fun sendProjectAnnouncement(projectId: Long, message: String, recipients: List<String>)
    
    // TODO: Add method for project updates
    fun postProjectUpdate(projectId: Long, update: ProjectUpdateRequest): ProjectUpdateResponse
}

// Supporting data classes for project services

// TODO: Create project template data class
data class ProjectTemplate(
    val id: Long,
    val name: String,
    val description: String,
    val taskTemplates: List<TaskTemplate>,
    val defaultSettings: Map<String, Any>
)

// TODO: Create task template data class
data class TaskTemplate(
    val title: String,
    val description: String,
    val priority: com.learning.architecture.model.TaskPriority,
    val estimatedHours: Int?,
    val dependencies: List<String>
)

// TODO: Create milestone data classes
data class MilestoneRequest(
    val name: String,
    val description: String,
    val dueDate: LocalDate,
    val criteria: List<String>
)

data class MilestoneResponse(
    val id: Long,
    val name: String,
    val description: String,
    val dueDate: String,
    val criteria: List<String>,
    val isCompleted: Boolean,
    val completedDate: String?
)

// TODO: Create resource data classes
data class ResourceRequest(
    val type: String,
    val name: String,
    val quantity: Int,
    val startDate: LocalDate,
    val endDate: LocalDate
)

data class ResourceResponse(
    val id: Long,
    val type: String,
    val name: String,
    val quantity: Int,
    val startDate: String,
    val endDate: String,
    val isAvailable: Boolean
)

// TODO: Create optimization result data classes
data class TimelineOptimizationResult(
    val originalEndDate: LocalDate,
    val optimizedEndDate: LocalDate,
    val recommendations: List<String>,
    val potentialTimeSavings: Int
)

data class DependencyAnalysisResult(
    val dependencies: List<Dependency>,
    val criticalPath: List<String>,
    val potentialBottlenecks: List<String>
)

// TODO: Create dependency data class
data class Dependency(
    val taskId: Long,
    val dependsOnTaskId: Long,
    val dependencyType: String,
    val isBlocking: Boolean
)

// TODO: Create team member data class
data class TeamMemberResponse(
    val userId: String,
    val name: String,
    val email: String,
    val role: String,
    val joinDate: String,
    val activeTaskCount: Int
)

// TODO: Create project update data classes
data class ProjectUpdateRequest(
    val title: String,
    val content: String,
    val isImportant: Boolean = false
)

data class ProjectUpdateResponse(
    val id: Long,
    val title: String,
    val content: String,
    val authorId: String,
    val authorName: String,
    val createdAt: String,
    val isImportant: Boolean
)