/**
 * Lesson 8 Workshop: Task DTOs
 * 
 * TODO: Create comprehensive DTOs for Task operations
 * These DTOs should work with data from both SQL and MongoDB sources
 */

package com.learning.persistence.dto

// TODO: Import validation annotations
// TODO: Import LocalDateTime
import java.time.LocalDateTime

// TODO: Create request DTOs for creating and updating tasks
data class CreateTaskRequest(
    // TODO: Add validation annotations and properties:
    // - title: String (required, size 1-100)
    // - description: String? (optional, max 1000 chars)
    // - priority: String (required, must be valid TaskPriority)
    // - assigneeId: String? (optional)
    // - projectId: Long? (optional)
    // - dueDate: LocalDateTime? (optional, must be future)
    // - tags: List<String> (optional, max 10 tags)
    // - metadata: Map<String, Any>? (optional flexible data)
)

data class UpdateTaskRequest(
    // TODO: Add optional properties for partial updates:
    // - title: String?
    // - description: String?
    // - status: String?
    // - priority: String?
    // - assigneeId: String?
    // - dueDate: LocalDateTime?
    // - tags: List<String>?
    // - metadata: Map<String, Any>?
)

// TODO: Create response DTOs that combine SQL and MongoDB data
data class TaskResponse(
    // TODO: Add properties from both SQL entity and MongoDB document:
    // - id: Long (from SQL)
    // - mongoId: String? (from MongoDB)
    // - title: String
    // - description: String?
    // - status: String
    // - priority: String
    // - assigneeInfo: AssigneeResponse? (rich data from MongoDB)
    // - projectInfo: ProjectResponse? (rich data from MongoDB)
    // - createdAt: String
    // - updatedAt: String
    // - dueDate: String?
    // - tags: List<String> (from MongoDB)
    // - metadata: Map<String, Any> (from MongoDB)
    // - isOverdue: Boolean (computed)
    // - daysSinceCreated: Long (computed)
)

data class AssigneeResponse(
    // TODO: Add assignee properties from MongoDB:
    // - userId: String
    // - name: String
    // - email: String
    // - department: String
    // - role: String
)

data class ProjectResponse(
    // TODO: Add project properties:
    // - projectId: String
    // - name: String
    // - description: String
    // - status: String
)

// TODO: Create search and filter DTOs
data class TaskSearchRequest(
    // TODO: Add search criteria:
    // - status: String?
    // - priority: String?
    // - assigneeId: String?
    // - projectId: Long?
    // - tags: List<String>?
    // - createdAfter: LocalDateTime?
    // - createdBefore: LocalDateTime?
    // - dueBefore: LocalDateTime?
    // - textSearch: String? (for MongoDB full-text search)
    // - department: String? (from MongoDB assignee info)
    // - customFields: Map<String, Any>? (MongoDB metadata search)
    // - page: Int = 0
    // - size: Int = 20
    // - sortBy: String = "createdAt"
    // - sortDirection: String = "DESC"
)

// TODO: Create analytics and reporting DTOs
data class TaskAnalyticsResponse(
    // TODO: Add analytics data:
    // - totalTasks: Long
    // - tasksByStatus: Map<String, Long>
    // - tasksByPriority: Map<String, Long>
    // - overdueTasks: Long
    // - averageCompletionDays: Double?
    // - topTags: List<TagCount>
    // - departmentStats: List<DepartmentStats>
    // - monthlyTrends: List<MonthlyTrend>
)

data class TagCount(
    val tag: String,
    val count: Int
)

data class DepartmentStats(
    val department: String,
    val activeTaskCount: Int,
    val completedTaskCount: Int,
    val averageCompletionTime: Double?
)

data class MonthlyTrend(
    val year: Int,
    val month: Int,
    val createdCount: Int,
    val completedCount: Int
)

// TODO: Create DTOs for database synchronization
data class SyncRequest(
    // TODO: Add sync operation properties:
    // - direction: String ("sql-to-mongo", "mongo-to-sql", "bidirectional")
    // - dryRun: Boolean = true
    // - batchSize: Int = 100
    // - onConflict: String = "skip" // "skip", "overwrite", "merge"
)

data class SyncResult(
    // TODO: Add sync result properties:
    // - success: Boolean
    // - processedCount: Int
    // - successCount: Int
    // - errorCount: Int
    // - errors: List<SyncError>
    // - duration: Long (milliseconds)
)

data class SyncError(
    val entityId: String,
    val errorType: String,
    val message: String,
    val details: Map<String, Any>?
)

// TODO: Create DTOs for data consistency checking
data class ConsistencyReport(
    // TODO: Add consistency check properties:
    // - totalChecked: Int
    // - consistentCount: Int
    // - inconsistentCount: Int
    // - missingInSql: Int
    // - missingInMongo: Int
    // - differences: List<DataDifference>
)

data class DataDifference(
    val entityId: String,
    val field: String,
    val sqlValue: Any?,
    val mongoValue: Any?,
    val severity: String // "minor", "major", "critical"
)