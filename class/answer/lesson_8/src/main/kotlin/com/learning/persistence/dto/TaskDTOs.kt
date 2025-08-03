/**
 * Lesson 8 Complete Solution: Task DTOs
 * 
 * Complete DTOs for Task operations that work with data from both SQL and MongoDB sources
 */

package com.learning.persistence.dto

import jakarta.validation.constraints.*
import java.time.LocalDateTime

// Request DTOs for creating and updating tasks
data class CreateTaskRequest(
    @field:NotBlank(message = "Title is required")
    @field:Size(min = 1, max = 100, message = "Title must be 1-100 characters")
    val title: String,
    
    @field:Size(max = 1000, message = "Description cannot exceed 1000 characters")
    val description: String?,
    
    @field:NotBlank(message = "Priority is required")
    @field:Pattern(regexp = "LOW|MEDIUM|HIGH|CRITICAL", message = "Priority must be LOW, MEDIUM, HIGH, or CRITICAL")
    val priority: String,
    
    val assigneeId: String?,
    
    val projectId: Long?,
    
    @field:Future(message = "Due date must be in the future")
    val dueDate: LocalDateTime?,
    
    @field:Size(max = 10, message = "Maximum 10 tags allowed")
    val tags: List<String> = emptyList(),
    
    val metadata: Map<String, Any> = emptyMap(),
    
    val complexity: String?,
    
    val source: String?,
    
    val labels: Map<String, String> = emptyMap(),
    
    val estimatedHours: Int?,
    
    val category: String?,
    
    val externalId: String?,
    
    val dependencies: List<String> = emptyList(),
    
    val watchers: List<String> = emptyList()
)

data class UpdateTaskRequest(
    @field:Size(min = 1, max = 100, message = "Title must be 1-100 characters")
    val title: String?,
    
    @field:Size(max = 1000, message = "Description cannot exceed 1000 characters")
    val description: String?,
    
    @field:Pattern(regexp = "CREATED|IN_PROGRESS|IN_REVIEW|COMPLETED|CANCELLED", message = "Invalid status")
    val status: String?,
    
    @field:Pattern(regexp = "LOW|MEDIUM|HIGH|CRITICAL", message = "Priority must be LOW, MEDIUM, HIGH, or CRITICAL")
    val priority: String?,
    
    val assigneeId: String?,
    
    val dueDate: LocalDateTime?,
    
    @field:Size(max = 10, message = "Maximum 10 tags allowed")
    val tags: List<String>?,
    
    val metadata: Map<String, Any>?,
    
    val complexity: String?,
    
    val estimatedHours: Int?,
    
    val actualHours: Int?,
    
    val labels: Map<String, String>?,
    
    val dependencies: List<String>?,
    
    val watchers: List<String>?
)

// Response DTOs that combine SQL and MongoDB data
data class TaskResponse(
    val id: Long,
    val mongoId: String?,
    val title: String,
    val description: String?,
    val status: String,
    val priority: String,
    val priorityLevel: Int,
    val assigneeInfo: AssigneeResponse?,
    val projectInfo: ProjectResponse?,
    val createdAt: String,
    val updatedAt: String,
    val dueDate: String?,
    val tags: List<String>,
    val metadata: TaskMetadataResponse,
    val isOverdue: Boolean,
    val daysSinceCreated: Long,
    val estimatedEffortHours: Int,
    val workloadScore: Int?
)

data class AssigneeResponse(
    val userId: String,
    val name: String,
    val email: String,
    val department: String,
    val role: String,
    val displayName: String
)

data class ProjectResponse(
    val projectId: String,
    val name: String,
    val description: String,
    val status: String,
    val isActive: Boolean,
    val displayInfo: String
)

data class TaskMetadataResponse(
    val estimatedHours: Int?,
    val actualHours: Int?,
    val complexity: String?,
    val source: String?,
    val labels: Map<String, String>,
    val customFields: Map<String, Any>,
    val category: String?,
    val externalId: String?,
    val dependencies: List<String>,
    val watchers: List<String>,
    val attachments: List<AttachmentResponse>,
    val isEstimateAccurate: Boolean,
    val efficiencyRatio: Double?,
    val hasAttachments: Boolean,
    val hasDependencies: Boolean,
    val hasWatchers: Boolean
)

data class AttachmentResponse(
    val fileName: String,
    val fileSize: Long,
    val fileSizeInMB: Double,
    val contentType: String,
    val uploadedBy: String,
    val uploadedAt: String,
    val url: String?,
    val isImage: Boolean,
    val isDocument: Boolean,
    val displayName: String
)

// Search and filter DTOs
data class TaskSearchRequest(
    val status: String?,
    val priority: String?,
    val assigneeId: String?,
    val projectId: Long?,
    val tags: List<String>?,
    val createdAfter: LocalDateTime?,
    val createdBefore: LocalDateTime?,
    val dueBefore: LocalDateTime?,
    val textSearch: String?,
    val department: String?,
    val complexity: String?,
    val source: String?,
    val hasAttachments: Boolean?,
    val hasDependencies: Boolean?,
    val customFields: Map<String, Any>?,
    val labels: Map<String, String>?,
    
    @field:Min(value = 0, message = "Page cannot be negative")
    val page: Int = 0,
    
    @field:Min(value = 1, message = "Page size must be at least 1")
    @field:Max(value = 100, message = "Page size cannot exceed 100")
    val size: Int = 20,
    
    val sortBy: String = "createdAt",
    val sortDirection: String = "DESC"
)

// Analytics and reporting DTOs
data class TaskAnalyticsResponse(
    val totalTasks: Long,
    val tasksByStatus: Map<String, Long>,
    val tasksByPriority: Map<String, Long>,
    val overdueTasks: Long,
    val averageCompletionDays: Double?,
    val topTags: List<TagCountResponse>,
    val departmentStats: List<DepartmentStatsResponse>,
    val monthlyTrends: List<MonthlyTrendResponse>,
    val complexityAnalysis: List<ComplexityStatsResponse>,
    val projectProgress: List<ProjectProgressResponse>
)

data class TagCountResponse(
    val tag: String,
    val count: Int
)

data class DepartmentStatsResponse(
    val department: String,
    val activeTaskCount: Int,
    val completedTaskCount: Int,
    val averageCompletionTime: Double?
)

data class MonthlyTrendResponse(
    val year: Int,
    val month: Int,
    val createdCount: Int,
    val completedCount: Int
)

data class ComplexityStatsResponse(
    val complexity: String,
    val taskCount: Int,
    val averageEstimatedHours: Double?,
    val averageActualHours: Double?,
    val estimateAccuracy: Double?
)

data class ProjectProgressResponse(
    val projectId: String,
    val projectName: String,
    val totalTasks: Int,
    val completedTasks: Int,
    val completionRate: Double,
    val averagePriority: Double
)

data class AssigneePerformanceResponse(
    val userId: String,
    val totalTasks: Int,
    val completedTasks: Int,
    val completionRate: Double,
    val averagePriority: Double,
    val overdueTasksCount: Int
)

// DTOs for database synchronization
data class SyncRequest(
    @field:Pattern(regexp = "sql-to-mongo|mongo-to-sql|bidirectional", message = "Invalid sync direction")
    val direction: String,
    
    val dryRun: Boolean = true,
    
    @field:Min(value = 1, message = "Batch size must be at least 1")
    @field:Max(value = 1000, message = "Batch size cannot exceed 1000")
    val batchSize: Int = 100,
    
    @field:Pattern(regexp = "skip|overwrite|merge", message = "onConflict must be skip, overwrite, or merge")
    val onConflict: String = "skip"
)

data class SyncResult(
    val success: Boolean,
    val processedCount: Int,
    val successCount: Int,
    val errorCount: Int,
    val errors: List<SyncError>,
    val duration: Long,
    val syncDirection: String,
    val dryRun: Boolean
)

data class SyncError(
    val entityId: String,
    val errorType: String,
    val message: String,
    val details: Map<String, Any>?
)

// DTOs for data consistency checking
data class ConsistencyReport(
    val totalChecked: Int,
    val consistentCount: Int,
    val inconsistentCount: Int,
    val missingInSql: Int,
    val missingInMongo: Int,
    val differences: List<DataDifference>,
    val checkTimestamp: LocalDateTime,
    val overallConsistencyPercentage: Double
)

data class DataDifference(
    val entityId: String,
    val field: String,
    val sqlValue: Any?,
    val mongoValue: Any?,
    val severity: String // "minor", "major", "critical"
)

// Bulk operation DTOs
data class BulkUpdateRequest(
    @field:NotEmpty(message = "Task IDs cannot be empty")
    val taskIds: List<Long>,
    
    val status: String?,
    val priority: String?,
    val assigneeId: String?,
    val tags: List<String>?,
    val addTags: List<String>?,
    val removeTags: List<String>?,
    val metadata: Map<String, Any>?
)

data class BulkUpdateResult(
    val successCount: Int,
    val failureCount: Int,
    val errors: List<String>,
    val updatedTaskIds: List<Long>,
    val failedTaskIds: List<Long>
)

// Health and status DTOs
data class DatabaseHealthResponse(
    val sqlDatabaseStatus: String,
    val mongoDbStatus: String,
    val overallStatus: String,
    val sqlRecordCount: Long,
    val mongoDocumentCount: Long,
    val consistencyStatus: String,
    val lastSyncTime: LocalDateTime?,
    val performanceMetrics: Map<String, Any>
)

data class DatabaseStatsResponse(
    val sqlStats: SqlDatabaseStats,
    val mongoStats: MongoDatabaseStats,
    val comparisonMetrics: ComparisonMetrics
)

data class SqlDatabaseStats(
    val totalRecords: Long,
    val recordsByStatus: Map<String, Long>,
    val averageQueryTime: Double,
    val connectionPoolSize: Int,
    val activeConnections: Int
)

data class MongoDatabaseStats(
    val totalDocuments: Long,
    val documentsByStatus: Map<String, Long>,
    val averageQueryTime: Double,
    val indexUsage: Map<String, Any>,
    val collectionSize: Long
)

data class ComparisonMetrics(
    val recordCountDifference: Long,
    val queryPerformanceComparison: Map<String, Double>,
    val storageEfficiency: Map<String, Double>,
    val dataConsistencyScore: Double
)