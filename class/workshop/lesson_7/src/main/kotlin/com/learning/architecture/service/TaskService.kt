/**
 * Lesson 7 Workshop: Task Service Interface
 * 
 * TODO: Complete this service interface for clean architecture
 * This demonstrates:
 * - Service layer abstraction
 * - Business logic encapsulation
 * - Interface segregation principle
 * - Dependency inversion principle
 */

package com.learning.architecture.service

import com.learning.architecture.dto.*
import com.learning.architecture.model.Task
import com.learning.architecture.model.TaskPriority
import com.learning.architecture.model.TaskStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

// TODO: Create main task service interface
interface TaskService {
    
    // Core CRUD operations
    // TODO: Add method to create a new task
    fun createTask(request: CreateTaskRequest): TaskResponse
    
    // TODO: Add method to get task by ID
    fun getTaskById(id: Long): TaskResponse
    
    // TODO: Add method to update task
    fun updateTask(id: Long, request: UpdateTaskRequest): TaskResponse
    
    // TODO: Add method to delete task
    fun deleteTask(id: Long)
    
    // TODO: Add method to get all tasks with pagination
    fun getAllTasks(pageable: Pageable): Page<TaskResponse>
    
    // Business operations
    // TODO: Add method to assign task to user
    fun assignTask(taskId: Long, request: AssignTaskRequest): TaskResponse
    
    // TODO: Add method to complete task
    fun completeTask(taskId: Long, request: CompleteTaskRequest): TaskResponse
    
    // TODO: Add method to change task status
    fun changeTaskStatus(taskId: Long, newStatus: TaskStatus, reason: String?): TaskResponse
    
    // TODO: Add method to change task priority
    fun changeTaskPriority(taskId: Long, newPriority: TaskPriority, reason: String?): TaskResponse
    
    // Query operations
    // TODO: Add method to search tasks
    fun searchTasks(searchRequest: TaskSearchRequest): Page<TaskResponse>
    
    // TODO: Add method to get tasks by status
    fun getTasksByStatus(status: TaskStatus, pageable: Pageable): Page<TaskResponse>
    
    // TODO: Add method to get tasks by assignee
    fun getTasksByAssignee(assigneeId: String, pageable: Pageable): Page<TaskResponse>
    
    // TODO: Add method to get overdue tasks
    fun getOverdueTasks(pageable: Pageable): Page<TaskResponse>
    
    // TODO: Add method to get high priority tasks
    fun getHighPriorityTasks(pageable: Pageable): Page<TaskResponse>
    
    // Bulk operations
    // TODO: Add method for bulk task updates
    fun bulkUpdateTasks(request: BulkTaskUpdateRequest): List<TaskResponse>
    
    // TODO: Add method for bulk task assignment
    fun bulkAssignTasks(taskIds: Set<Long>, assigneeId: String, reason: String?): List<TaskResponse>
    
    // TODO: Add method for bulk status change
    fun bulkChangeStatus(taskIds: Set<Long>, status: TaskStatus, reason: String?): List<TaskResponse>
    
    // Analytics and reporting
    // TODO: Add method to get task summary
    fun getTaskSummary(): TaskSummaryResponse
    
    // TODO: Add method to get task metrics
    fun getTaskMetrics(): TaskMetricsResponse
    
    // TODO: Add method to get workload distribution
    fun getWorkloadDistribution(): List<Map<String, Any>>
}

// TODO: Create specialized service interface for task validation
interface TaskValidationService {
    
    // TODO: Add method to validate task creation
    fun validateTaskCreation(request: CreateTaskRequest): ValidationResult
    
    // TODO: Add method to validate task assignment
    fun validateTaskAssignment(taskId: Long, assigneeId: String): ValidationResult
    
    // TODO: Add method to validate status transition
    fun validateStatusTransition(taskId: Long, newStatus: TaskStatus): ValidationResult
    
    // TODO: Add method to validate priority change
    fun validatePriorityChange(taskId: Long, newPriority: TaskPriority): ValidationResult
    
    // TODO: Add method to validate task completion
    fun validateTaskCompletion(taskId: Long): ValidationResult
    
    // TODO: Add method to validate bulk operations
    fun validateBulkOperation(request: BulkTaskUpdateRequest): ValidationResult
}

// TODO: Create service interface for task analytics
interface TaskAnalyticsService {
    
    // TODO: Add method for completion rate analysis
    fun analyzeCompletionRates(
        startDate: java.time.LocalDate,
        endDate: java.time.LocalDate
    ): Map<String, Any>
    
    // TODO: Add method for bottleneck identification
    fun identifyBottlenecks(): List<Map<String, Any>>
    
    // TODO: Add method for performance trends
    fun getPerformanceTrends(
        timeframe: String,
        granularity: String
    ): List<Map<String, Any>>
    
    // TODO: Add method for predictive analytics
    fun predictTaskCompletion(taskId: Long): Map<String, Any>
    
    // TODO: Add method for workload optimization
    fun optimizeWorkloadDistribution(): List<Map<String, Any>>
}

// TODO: Create service interface for task notifications
interface TaskNotificationService {
    
    // TODO: Add method to notify task assignment
    fun notifyTaskAssignment(task: Task, assigneeId: String)
    
    // TODO: Add method to notify task completion
    fun notifyTaskCompletion(task: Task)
    
    // TODO: Add method to notify overdue tasks
    fun notifyOverdueTasks()
    
    // TODO: Add method to notify high priority tasks
    fun notifyHighPriorityTasks()
    
    // TODO: Add method to notify deadline approaching
    fun notifyDeadlineApproaching(task: Task, daysUntilDue: Int)
    
    // TODO: Add method for custom notifications
    fun sendCustomNotification(
        recipients: List<String>,
        subject: String,
        message: String,
        priority: String
    )
}

// TODO: Create service interface for task import/export
interface TaskImportExportService {
    
    // TODO: Add method to export tasks
    fun exportTasks(
        filters: TaskSearchRequest,
        format: String
    ): ByteArray
    
    // TODO: Add method to import tasks
    fun importTasks(
        data: ByteArray,
        format: String,
        options: Map<String, Any>
    ): ImportResult
    
    // TODO: Add method to validate import data
    fun validateImportData(
        data: ByteArray,
        format: String
    ): ValidationResult
}

// TODO: Create data class for validation results
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<ValidationError> = emptyList(),
    val warnings: List<ValidationWarning> = emptyList()
) {
    fun hasErrors(): Boolean = errors.isNotEmpty()
    fun hasWarnings(): Boolean = warnings.isNotEmpty()
}

// TODO: Create data class for validation errors
data class ValidationError(
    val field: String?,
    val message: String,
    val errorCode: String
)

// TODO: Create data class for validation warnings
data class ValidationWarning(
    val field: String?,
    val message: String,
    val warningCode: String
)

// TODO: Create data class for import results
data class ImportResult(
    val totalRecords: Int,
    val successfulImports: Int,
    val failedImports: Int,
    val warnings: List<String>,
    val errors: List<String>,
    val importedTaskIds: List<Long>
)