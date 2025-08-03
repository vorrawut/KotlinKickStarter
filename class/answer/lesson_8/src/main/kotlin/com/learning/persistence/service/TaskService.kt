/**
 * Lesson 8 Complete Solution: Task Service - Dual Database Implementation
 * 
 * Complete service demonstrating both SQL and MongoDB usage with proper integration
 */

package com.learning.persistence.service

import com.learning.persistence.dto.*
import com.learning.persistence.model.sql.Task
import com.learning.persistence.model.sql.TaskPriority
import com.learning.persistence.model.sql.TaskStatus
import com.learning.persistence.model.mongo.*
import com.learning.persistence.repository.jpa.TaskRepository
import com.learning.persistence.repository.mongo.TaskDocumentRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Service
@Transactional
class TaskService(
    private val taskRepository: TaskRepository,
    private val taskDocumentRepository: TaskDocumentRepository
) {
    
    private val logger = LoggerFactory.getLogger(TaskService::class.java)
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    fun createTask(request: CreateTaskRequest): TaskResponse {
        logger.info("Creating task: {}", request.title)
        
        // 1. Create and save SQL entity (structured data)
        val sqlTask = Task(
            title = request.title,
            description = request.description,
            status = TaskStatus.CREATED,
            priority = TaskPriority.valueOf(request.priority),
            assigneeId = request.assigneeId,
            projectId = request.projectId,
            dueDate = request.dueDate
        )
        val savedSqlTask = taskRepository.save(sqlTask)
        
        // 2. Create and save MongoDB document (flexible metadata)
        val mongoTask = TaskDocument(
            id = savedSqlTask.id?.toString(),
            title = request.title,
            description = request.description,
            status = TaskStatus.CREATED.name,
            priority = TaskPriority.valueOf(request.priority).level,
            assignee = request.assigneeId?.let { 
                createAssigneeInfo(it) 
            },
            projectInfo = request.projectId?.let { 
                createProjectInfo(it) 
            },
            metadata = TaskMetadata(
                estimatedHours = request.estimatedHours,
                complexity = request.complexity,
                source = request.source,
                labels = request.labels,
                customFields = request.metadata,
                dueDate = request.dueDate,
                category = request.category,
                externalId = request.externalId,
                dependencies = request.dependencies,
                watchers = request.watchers
            ),
            tags = request.tags
        )
        val savedMongoTask = taskDocumentRepository.save(mongoTask)
        
        logger.info("Task created successfully with SQL ID: {} and MongoDB ID: {}", 
                   savedSqlTask.id, savedMongoTask.id)
        
        return mapToTaskResponse(savedSqlTask, savedMongoTask)
    }
    
    @Transactional(readOnly = true)
    fun findTask(id: Long): TaskResponse? {
        logger.debug("Finding task with ID: {}", id)
        
        // Fetch from SQL database
        val sqlTask = taskRepository.findById(id).orElse(null) ?: return null
        
        // Fetch corresponding MongoDB document
        val mongoTask = taskDocumentRepository.findById(id.toString()).orElse(null)
        
        return mapToTaskResponse(sqlTask, mongoTask)
    }
    
    fun updateTask(id: Long, request: UpdateTaskRequest): TaskResponse {
        logger.info("Updating task with ID: {}", id)
        
        // Update SQL entity
        val existingSqlTask = taskRepository.findById(id).orElseThrow {
            RuntimeException("Task not found with ID: $id")
        }
        
        val updatedSqlTask = existingSqlTask.copy(
            title = request.title ?: existingSqlTask.title,
            description = request.description ?: existingSqlTask.description,
            status = request.status?.let { TaskStatus.valueOf(it) } ?: existingSqlTask.status,
            priority = request.priority?.let { TaskPriority.valueOf(it) } ?: existingSqlTask.priority,
            assigneeId = request.assigneeId ?: existingSqlTask.assigneeId,
            dueDate = request.dueDate ?: existingSqlTask.dueDate,
            updatedAt = LocalDateTime.now()
        )
        val savedSqlTask = taskRepository.save(updatedSqlTask)
        
        // Update MongoDB document
        val existingMongoTask = taskDocumentRepository.findById(id.toString()).orElse(null)
        val updatedMongoTask = if (existingMongoTask != null) {
            existingMongoTask.copy(
                title = request.title ?: existingMongoTask.title,
                description = request.description ?: existingMongoTask.description,
                status = request.status ?: existingMongoTask.status,
                priority = request.priority?.let { TaskPriority.valueOf(it).level } ?: existingMongoTask.priority,
                assignee = request.assigneeId?.let { createAssigneeInfo(it) } ?: existingMongoTask.assignee,
                metadata = updateMetadata(existingMongoTask.metadata, request),
                tags = request.tags ?: existingMongoTask.tags,
                updatedAt = LocalDateTime.now()
            )
        } else {
            // Create new MongoDB document if it doesn't exist
            createMongoDocumentFromSqlTask(savedSqlTask, request)
        }
        
        val savedMongoTask = taskDocumentRepository.save(updatedMongoTask)
        
        logger.info("Task updated successfully: {}", id)
        return mapToTaskResponse(savedSqlTask, savedMongoTask)
    }
    
    fun deleteTask(id: Long): Boolean {
        logger.info("Deleting task with ID: {}", id)
        
        return try {
            // Delete from both databases
            taskRepository.deleteById(id)
            taskDocumentRepository.deleteById(id.toString())
            logger.info("Task deleted successfully: {}", id)
            true
        } catch (e: Exception) {
            logger.error("Error deleting task: {}", id, e)
            false
        }
    }
    
    @Transactional(readOnly = true)
    fun searchTasks(request: TaskSearchRequest): List<TaskResponse> {
        logger.debug("Searching tasks with criteria: {}", request)
        
        // Use MongoDB for flexible search, then fetch SQL data
        val mongoResults = if (request.textSearch != null) {
            // Use MongoDB's full-text search capability
            taskDocumentRepository.findByTextSearch(request.textSearch)
        } else {
            // Use MongoDB for complex criteria matching
            findMongoTasksByCriteria(request)
        }
        
        // Extract SQL IDs and fetch corresponding SQL entities
        val sqlIds = mongoResults.mapNotNull { it.id?.toLongOrNull() }
        val sqlTasks = if (sqlIds.isNotEmpty()) {
            taskRepository.findAllById(sqlIds)
        } else {
            emptyList()
        }
        
        // Combine results
        return mongoResults.mapNotNull { mongoTask ->
            val sqlTask = sqlTasks.find { it.id?.toString() == mongoTask.id }
            if (sqlTask != null) {
                mapToTaskResponse(sqlTask, mongoTask)
            } else {
                // Handle case where MongoDB document exists but SQL entity doesn't
                mapToTaskResponse(null, mongoTask)
            }
        }
    }
    
    @Transactional(readOnly = true)
    fun getAllTasks(page: Int, size: Int, sortBy: String, sortDirection: String): List<TaskResponse> {
        logger.debug("Getting all tasks - page: {}, size: {}, sortBy: {}, direction: {}", 
                    page, size, sortBy, sortDirection)
        
        val direction = if (sortDirection.uppercase() == "DESC") Sort.Direction.DESC else Sort.Direction.ASC
        val pageable = PageRequest.of(page, size, Sort.by(direction, sortBy))
        
        // Paginate from SQL (better for structured sorting)
        val sqlTasks = taskRepository.findAll(pageable).content
        val mongoIds = sqlTasks.mapNotNull { it.id?.toString() }
        val mongoTasks = taskDocumentRepository.findAllById(mongoIds)
        
        return sqlTasks.map { sqlTask ->
            val mongoTask = mongoTasks.find { it.id == sqlTask.id?.toString() }
            mapToTaskResponse(sqlTask, mongoTask)
        }
    }
    
    // Showcase SQL database strengths
    @Transactional(readOnly = true)
    fun getTasksWithProjectDetails(projectId: Long): List<TaskResponse> {
        logger.debug("Getting tasks with project details for project: {}", projectId)
        
        // Use SQL for relational queries
        val sqlTasks = taskRepository.findByProjectId(projectId)
        val mongoIds = sqlTasks.mapNotNull { it.id?.toString() }
        val mongoTasks = taskDocumentRepository.findAllById(mongoIds)
        
        return sqlTasks.map { sqlTask ->
            val mongoTask = mongoTasks.find { it.id == sqlTask.id?.toString() }
            mapToTaskResponse(sqlTask, mongoTask)
        }
    }
    
    fun bulkUpdateTaskStatus(taskIds: List<Long>, newStatus: String, reason: String): BulkUpdateResult {
        logger.info("Bulk updating task status for {} tasks to {}", taskIds.size, newStatus)
        
        val successIds = mutableListOf<Long>()
        val failedIds = mutableListOf<Long>()
        val errors = mutableListOf<String>()
        
        taskIds.forEach { taskId ->
            try {
                // Update SQL
                val sqlTask = taskRepository.findById(taskId).orElse(null)
                if (sqlTask != null && sqlTask.canTransitionTo(TaskStatus.valueOf(newStatus))) {
                    val updatedSqlTask = sqlTask.copy(
                        status = TaskStatus.valueOf(newStatus),
                        updatedAt = LocalDateTime.now()
                    )
                    taskRepository.save(updatedSqlTask)
                    
                    // Update MongoDB
                    taskDocumentRepository.findById(taskId.toString()).ifPresent { mongoTask ->
                        val updatedMongoTask = mongoTask.copy(
                            status = newStatus,
                            updatedAt = LocalDateTime.now()
                        )
                        taskDocumentRepository.save(updatedMongoTask)
                    }
                    
                    successIds.add(taskId)
                } else {
                    errors.add("Task $taskId: Invalid status transition or task not found")
                    failedIds.add(taskId)
                }
            } catch (e: Exception) {
                logger.error("Error updating task {}: {}", taskId, e.message)
                errors.add("Task $taskId: ${e.message}")
                failedIds.add(taskId)
            }
        }
        
        return BulkUpdateResult(
            successCount = successIds.size,
            failureCount = failedIds.size,
            errors = errors,
            updatedTaskIds = successIds,
            failedTaskIds = failedIds
        )
    }
    
    // Showcase MongoDB strengths
    @Transactional(readOnly = true)
    fun getTaskAnalytics(): TaskAnalyticsResponse {
        logger.debug("Getting task analytics using MongoDB aggregation")
        
        // Use MongoDB aggregation pipelines for complex analytics
        val departmentStats = taskDocumentRepository.getActiveTaskCountByDepartment()
        val priorityStats = taskDocumentRepository.getAverageCompletionTimeByPriority()
        val topTags = taskDocumentRepository.getTopTags()
        val monthlyTrends = taskDocumentRepository.getTaskCountByMonth(
            LocalDateTime.now().minusMonths(12),
            LocalDateTime.now()
        )
        val complexityAnalysis = taskDocumentRepository.getComplexityAnalysis()
        val projectProgress = taskDocumentRepository.getProjectProgress()
        
        // Combine with SQL data for total counts
        val totalTasks = taskRepository.count()
        val tasksByStatus = TaskStatus.values().associateWith { status ->
            taskRepository.countByStatus(status)
        }
        val tasksByPriority = TaskPriority.values().associateWith { priority ->
            taskRepository.findByPriority(priority).size.toLong()
        }
        val overdueTasks = taskRepository.countOverdueTasks()
        
        return TaskAnalyticsResponse(
            totalTasks = totalTasks,
            tasksByStatus = tasksByStatus.mapKeys { it.key.name },
            tasksByPriority = tasksByPriority.mapKeys { it.key.name },
            overdueTasks = overdueTasks,
            averageCompletionDays = calculateAverageCompletionDays(),
            topTags = topTags.map { TagCountResponse(it._id, it.count) },
            departmentStats = departmentStats.map { 
                DepartmentStatsResponse(
                    department = it._id,
                    activeTaskCount = it.count,
                    completedTaskCount = 0, // Would need additional query
                    averageCompletionTime = null
                )
            },
            monthlyTrends = monthlyTrends.map {
                MonthlyTrendResponse(
                    year = it._id.year,
                    month = it._id.month,
                    createdCount = it.count,
                    completedCount = 0 // Would need additional query
                )
            },
            complexityAnalysis = complexityAnalysis.map {
                ComplexityStatsResponse(
                    complexity = it._id,
                    taskCount = it.count,
                    averageEstimatedHours = it.avgEstimatedHours,
                    averageActualHours = it.avgActualHours,
                    estimateAccuracy = it.estimateAccuracy
                )
            },
            projectProgress = projectProgress.map {
                ProjectProgressResponse(
                    projectId = it._id,
                    projectName = it.projectName,
                    totalTasks = it.totalTasks,
                    completedTasks = it.completedTasks,
                    completionRate = it.completionRate,
                    averagePriority = it.avgPriority
                )
            }
        )
    }
    
    @Transactional(readOnly = true)
    fun searchTasksByText(searchText: String): List<TaskResponse> {
        logger.debug("Searching tasks by text: {}", searchText)
        
        // Use MongoDB's full-text search capabilities
        val mongoTasks = taskDocumentRepository.findByTextSearchSortedByRelevance(searchText)
        val sqlIds = mongoTasks.mapNotNull { it.id?.toLongOrNull() }
        val sqlTasks = if (sqlIds.isNotEmpty()) {
            taskRepository.findAllById(sqlIds)
        } else {
            emptyList()
        }
        
        return mongoTasks.mapNotNull { mongoTask ->
            val sqlTask = sqlTasks.find { it.id?.toString() == mongoTask.id }
            if (sqlTask != null) {
                mapToTaskResponse(sqlTask, mongoTask)
            } else {
                mapToTaskResponse(null, mongoTask)
            }
        }
    }
    
    @Transactional(readOnly = true)
    fun findTasksByCustomCriteria(criteria: Map<String, Any>): List<TaskResponse> {
        logger.debug("Finding tasks by custom criteria: {}", criteria)
        
        // This would require dynamic query building with MongoDB
        // For simplicity, implementing basic criteria matching
        val mongoTasks = taskDocumentRepository.findAll().filter { task ->
            criteria.all { (key, value) ->
                when (key) {
                    "complexity" -> task.metadata.complexity == value
                    "source" -> task.metadata.source == value
                    "category" -> task.metadata.category == value
                    else -> task.metadata.customFields[key] == value
                }
            }
        }
        
        val sqlIds = mongoTasks.mapNotNull { it.id?.toLongOrNull() }
        val sqlTasks = if (sqlIds.isNotEmpty()) {
            taskRepository.findAllById(sqlIds)
        } else {
            emptyList()
        }
        
        return mongoTasks.mapNotNull { mongoTask ->
            val sqlTask = sqlTasks.find { it.id?.toString() == mongoTask.id }
            if (sqlTask != null) {
                mapToTaskResponse(sqlTask, mongoTask)
            } else {
                mapToTaskResponse(null, mongoTask)
            }
        }
    }
    
    // Data consistency and synchronization methods
    @Transactional(readOnly = true)
    fun validateDataConsistency(): ConsistencyReport {
        logger.info("Validating data consistency between SQL and MongoDB")
        
        val sqlTasks = taskRepository.findAll()
        val mongoTasks = taskDocumentRepository.findAll()
        
        val sqlIds = sqlTasks.mapNotNull { it.id }.toSet()
        val mongoIds = mongoTasks.mapNotNull { it.id?.toLongOrNull() }.toSet()
        
        val missingInMongo = sqlIds - mongoIds
        val missingInSql = mongoIds - sqlIds
        val commonIds = sqlIds intersect mongoIds
        
        val differences = mutableListOf<DataDifference>()
        
        // Check for data differences in common records
        commonIds.forEach { id ->
            val sqlTask = sqlTasks.find { it.id == id }
            val mongoTask = mongoTasks.find { it.id == id.toString() }
            
            if (sqlTask != null && mongoTask != null) {
                // Compare basic fields
                if (sqlTask.title != mongoTask.title) {
                    differences.add(DataDifference(id.toString(), "title", sqlTask.title, mongoTask.title, "major"))
                }
                if (sqlTask.status.name != mongoTask.status) {
                    differences.add(DataDifference(id.toString(), "status", sqlTask.status.name, mongoTask.status, "critical"))
                }
                if (sqlTask.priority.level != mongoTask.priority) {
                    differences.add(DataDifference(id.toString(), "priority", sqlTask.priority.level, mongoTask.priority, "major"))
                }
            }
        }
        
        val totalChecked = maxOf(sqlTasks.size, mongoTasks.size)
        val consistentCount = commonIds.size - differences.size
        val inconsistentCount = differences.size + missingInMongo.size + missingInSql.size
        
        return ConsistencyReport(
            totalChecked = totalChecked,
            consistentCount = consistentCount,
            inconsistentCount = inconsistentCount,
            missingInSql = missingInSql.size,
            missingInMongo = missingInMongo.size,
            differences = differences,
            checkTimestamp = LocalDateTime.now(),
            overallConsistencyPercentage = if (totalChecked > 0) {
                (consistentCount.toDouble() / totalChecked) * 100
            } else {
                100.0
            }
        )
    }
    
    // Helper methods
    private fun mapToTaskResponse(sqlTask: Task?, mongoTask: TaskDocument?): TaskResponse {
        val task = sqlTask ?: throw IllegalArgumentException("SQL task is required")
        
        return TaskResponse(
            id = task.id!!,
            mongoId = mongoTask?.id,
            title = task.title,
            description = task.description,
            status = task.status.name,
            priority = task.priority.name,
            priorityLevel = task.priority.level,
            assigneeInfo = mongoTask?.assignee?.let {
                AssigneeResponse(
                    userId = it.userId,
                    name = it.name,
                    email = it.email,
                    department = it.department,
                    role = it.role,
                    displayName = it.getDisplayName()
                )
            },
            projectInfo = mongoTask?.projectInfo?.let {
                ProjectResponse(
                    projectId = it.projectId,
                    name = it.name,
                    description = it.description,
                    status = it.status,
                    isActive = it.isActive(),
                    displayInfo = it.getDisplayInfo()
                )
            },
            createdAt = task.createdAt.format(formatter),
            updatedAt = task.updatedAt.format(formatter),
            dueDate = task.dueDate?.format(formatter),
            tags = mongoTask?.tags ?: emptyList(),
            metadata = mapToMetadataResponse(mongoTask?.metadata),
            isOverdue = task.isOverdue(),
            daysSinceCreated = ChronoUnit.DAYS.between(task.createdAt, LocalDateTime.now()),
            estimatedEffortHours = task.getEstimatedEffortHours(),
            workloadScore = mongoTask?.getWorkloadScore()
        )
    }
    
    private fun mapToMetadataResponse(metadata: TaskMetadata?): TaskMetadataResponse {
        return if (metadata != null) {
            TaskMetadataResponse(
                estimatedHours = metadata.estimatedHours,
                actualHours = metadata.actualHours,
                complexity = metadata.complexity,
                source = metadata.source,
                labels = metadata.labels,
                customFields = metadata.customFields,
                category = metadata.category,
                externalId = metadata.externalId,
                dependencies = metadata.dependencies,
                watchers = metadata.watchers,
                attachments = metadata.attachments.map { attachment ->
                    AttachmentResponse(
                        fileName = attachment.fileName,
                        fileSize = attachment.fileSize,
                        fileSizeInMB = attachment.getFileSizeInMB(),
                        contentType = attachment.contentType,
                        uploadedBy = attachment.uploadedBy,
                        uploadedAt = attachment.uploadedAt.format(formatter),
                        url = attachment.url,
                        isImage = attachment.isImage(),
                        isDocument = attachment.isDocument(),
                        displayName = attachment.getDisplayName()
                    )
                },
                isEstimateAccurate = metadata.isEstimateAccurate(),
                efficiencyRatio = metadata.getEfficiencyRatio(),
                hasAttachments = metadata.hasAttachments(),
                hasDependencies = metadata.hasDependencies(),
                hasWatchers = metadata.hasWatchers()
            )
        } else {
            TaskMetadataResponse(
                estimatedHours = null,
                actualHours = null,
                complexity = null,
                source = null,
                labels = emptyMap(),
                customFields = emptyMap(),
                category = null,
                externalId = null,
                dependencies = emptyList(),
                watchers = emptyList(),
                attachments = emptyList(),
                isEstimateAccurate = false,
                efficiencyRatio = null,
                hasAttachments = false,
                hasDependencies = false,
                hasWatchers = false
            )
        }
    }
    
    private fun createAssigneeInfo(userId: String): AssigneeInfo {
        // In a real implementation, this would fetch user details from a user service
        return AssigneeInfo(
            userId = userId,
            name = "User $userId",
            email = "$userId@company.com",
            department = "Development",
            role = "Developer"
        )
    }
    
    private fun createProjectInfo(projectId: Long): ProjectInfo {
        // In a real implementation, this would fetch project details from a project service
        return ProjectInfo(
            projectId = projectId.toString(),
            name = "Project $projectId",
            description = "Project description for $projectId",
            status = "ACTIVE"
        )
    }
    
    private fun updateMetadata(existing: TaskMetadata, request: UpdateTaskRequest): TaskMetadata {
        return existing.copy(
            estimatedHours = request.estimatedHours ?: existing.estimatedHours,
            actualHours = request.actualHours ?: existing.actualHours,
            complexity = request.complexity ?: existing.complexity,
            labels = request.labels ?: existing.labels,
            customFields = request.metadata ?: existing.customFields,
            dependencies = request.dependencies ?: existing.dependencies,
            watchers = request.watchers ?: existing.watchers
        )
    }
    
    private fun createMongoDocumentFromSqlTask(sqlTask: Task, request: UpdateTaskRequest): TaskDocument {
        return TaskDocument(
            id = sqlTask.id?.toString(),
            title = sqlTask.title,
            description = sqlTask.description,
            status = sqlTask.status.name,
            priority = sqlTask.priority.level,
            assignee = sqlTask.assigneeId?.let { createAssigneeInfo(it) },
            projectInfo = sqlTask.projectId?.let { createProjectInfo(it) },
            metadata = TaskMetadata(
                estimatedHours = request.estimatedHours,
                actualHours = request.actualHours,
                complexity = request.complexity,
                labels = request.labels ?: emptyMap(),
                customFields = request.metadata ?: emptyMap(),
                dueDate = sqlTask.dueDate,
                dependencies = request.dependencies ?: emptyList(),
                watchers = request.watchers ?: emptyList()
            ),
            tags = request.tags ?: emptyList(),
            createdAt = sqlTask.createdAt,
            updatedAt = LocalDateTime.now()
        )
    }
    
    private fun findMongoTasksByCriteria(request: TaskSearchRequest): List<TaskDocument> {
        // Simplified implementation - in a real app, you'd use Criteria API
        return taskDocumentRepository.findAll().filter { task ->
            (request.status == null || task.status == request.status) &&
            (request.department == null || task.assignee?.department == request.department) &&
            (request.complexity == null || task.metadata.complexity == request.complexity) &&
            (request.hasAttachments == null || task.metadata.hasAttachments() == request.hasAttachments) &&
            (request.hasDependencies == null || task.metadata.hasDependencies() == request.hasDependencies) &&
            (request.tags.isNullOrEmpty() || request.tags.any { tag -> task.hasTag(tag) })
        }
    }
    
    private fun calculateAverageCompletionDays(): Double? {
        // Simplified calculation - would be more complex in real implementation
        val completedTasks = taskRepository.findByStatus(TaskStatus.COMPLETED)
        if (completedTasks.isEmpty()) return null
        
        val totalDays = completedTasks.sumOf { task ->
            ChronoUnit.DAYS.between(task.createdAt, task.updatedAt)
        }
        
        return totalDays.toDouble() / completedTasks.size
    }
}