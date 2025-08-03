/**
 * Lesson 7 Complete Solution: Task Service Implementation
 */

package com.learning.architecture.service

import com.learning.architecture.dto.*
import com.learning.architecture.model.*
import com.learning.architecture.repository.TaskRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Service
@Transactional
class TaskServiceImpl : TaskService {
    
    private val logger = LoggerFactory.getLogger(TaskServiceImpl::class.java)
    
    // In-memory storage for this demo (would be replaced by actual repository)
    private val tasks = ConcurrentHashMap<Long, Task>()
    private val idCounter = AtomicLong(1)
    
    init {
        createSampleTasks()
    }
    
    override fun createTask(request: CreateTaskRequest): TaskResponse {
        logger.info("Creating task: {}", request.title)
        
        val task = if (request.dueDate != null) {
            Task.createWithDueDate(request.title, request.description, request.priority, request.dueDate)
        } else {
            Task.createNew(request.title, request.description, request.priority)
        }.copy(id = idCounter.getAndIncrement())
        
        tasks[task.id!!] = task
        logTaskOperation("CREATE", task.id, mapOf("title" to task.title))
        
        return mapToTaskResponse(task)
    }
    
    @Transactional(readOnly = true)
    override fun getTaskById(id: Long): TaskResponse {
        val task = findTaskOrThrow(id)
        return mapToTaskResponse(task)
    }
    
    override fun updateTask(id: Long, request: UpdateTaskRequest): TaskResponse {
        logger.info("Updating task: {}", id)
        
        val existingTask = findTaskOrThrow(id)
        val updatedTask = updateTaskFromRequest(existingTask, request)
        
        tasks[id] = updatedTask
        logTaskOperation("UPDATE", id, mapOf("changes" to request.toString()))
        
        return mapToTaskResponse(updatedTask)
    }
    
    override fun deleteTask(id: Long) {
        logger.info("Deleting task: {}", id)
        
        val task = findTaskOrThrow(id)
        
        if (task.status == TaskStatus.COMPLETED) {
            throw RuntimeException("Cannot delete completed tasks")
        }
        
        tasks.remove(id)
        logTaskOperation("DELETE", id, emptyMap())
    }
    
    @Transactional(readOnly = true)
    override fun getAllTasks(pageable: Pageable): Page<TaskResponse> {
        val taskList = tasks.values.toList()
        val start = (pageable.pageNumber * pageable.pageSize).coerceAtMost(taskList.size)
        val end = ((pageable.pageNumber + 1) * pageable.pageSize).coerceAtMost(taskList.size)
        
        val content = taskList.subList(start, end).map { mapToTaskResponse(it) }
        return PageImpl(content, pageable, taskList.size.toLong())
    }
    
    override fun assignTask(taskId: Long, request: AssignTaskRequest): TaskResponse {
        val task = findTaskOrThrow(taskId)
        val assignee = Assignee(request.assigneeId, "User Name", "user@example.com", "Development")
        
        if (!task.canBeAssignedTo(assignee)) {
            throw RuntimeException("Task cannot be assigned in current state")
        }
        
        val updatedTask = task.copy(
            assignee = assignee,
            dueDate = request.newDueDate ?: task.dueDate,
            status = TaskStatus.IN_PROGRESS,
            updatedAt = LocalDateTime.now()
        )
        
        tasks[taskId] = updatedTask
        triggerNotifications(updatedTask, "ASSIGN")
        
        return mapToTaskResponse(updatedTask)
    }
    
    override fun completeTask(taskId: Long, request: CompleteTaskRequest): TaskResponse {
        val task = findTaskOrThrow(taskId)
        
        val updatedTask = task.copy(
            status = TaskStatus.COMPLETED,
            updatedAt = LocalDateTime.now()
        )
        
        tasks[taskId] = updatedTask
        triggerNotifications(updatedTask, "COMPLETE")
        
        return mapToTaskResponse(updatedTask)
    }
    
    override fun changeTaskStatus(taskId: Long, newStatus: TaskStatus, reason: String?): TaskResponse {
        val task = findTaskOrThrow(taskId)
        
        if (!task.canTransitionTo(newStatus)) {
            throw RuntimeException("Invalid status transition from ${task.status} to $newStatus")
        }
        
        val updatedTask = task.copy(
            status = newStatus,
            updatedAt = LocalDateTime.now()
        )
        
        tasks[taskId] = updatedTask
        logTaskOperation("STATUS_CHANGE", taskId, mapOf("from" to task.status, "to" to newStatus, "reason" to (reason ?: "")))
        
        return mapToTaskResponse(updatedTask)
    }
    
    override fun changeTaskPriority(taskId: Long, newPriority: TaskPriority, reason: String?): TaskResponse {
        val task = findTaskOrThrow(taskId)
        
        val updatedTask = task.copy(
            priority = newPriority,
            updatedAt = LocalDateTime.now()
        )
        
        tasks[taskId] = updatedTask
        logTaskOperation("PRIORITY_CHANGE", taskId, mapOf("from" to task.priority, "to" to newPriority, "reason" to (reason ?: "")))
        
        return mapToTaskResponse(updatedTask)
    }
    
    @Transactional(readOnly = true)
    override fun searchTasks(searchRequest: TaskSearchRequest): Page<TaskResponse> {
        val filteredTasks = tasks.values.filter { task ->
            (searchRequest.title == null || task.title.contains(searchRequest.title, ignoreCase = true)) &&
            (searchRequest.status == null || task.status == searchRequest.status) &&
            (searchRequest.priority == null || task.priority == searchRequest.priority) &&
            (searchRequest.assigneeId == null || task.assignee?.userId == searchRequest.assigneeId) &&
            (searchRequest.projectId == null || task.projectId == searchRequest.projectId) &&
            (searchRequest.isOverdue == null || task.isOverdue() == searchRequest.isOverdue)
        }
        
        val start = (searchRequest.page * searchRequest.size).coerceAtMost(filteredTasks.size)
        val end = ((searchRequest.page + 1) * searchRequest.size).coerceAtMost(filteredTasks.size)
        
        val content = filteredTasks.toList().subList(start, end).map { mapToTaskResponse(it) }
        return PageImpl(content, Pageable.ofSize(searchRequest.size).withPage(searchRequest.page), filteredTasks.size.toLong())
    }
    
    @Transactional(readOnly = true)
    override fun getTasksByStatus(status: TaskStatus, pageable: Pageable): Page<TaskResponse> {
        val taskList = tasks.values.filter { it.status == status }
        val start = (pageable.pageNumber * pageable.pageSize).coerceAtMost(taskList.size)
        val end = ((pageable.pageNumber + 1) * pageable.pageSize).coerceAtMost(taskList.size)
        
        val content = taskList.subList(start, end).map { mapToTaskResponse(it) }
        return PageImpl(content, pageable, taskList.size.toLong())
    }
    
    @Transactional(readOnly = true)
    override fun getTasksByAssignee(assigneeId: String, pageable: Pageable): Page<TaskResponse> {
        val taskList = tasks.values.filter { it.assignee?.userId == assigneeId }
        val start = (pageable.pageNumber * pageable.pageSize).coerceAtMost(taskList.size)
        val end = ((pageable.pageNumber + 1) * pageable.pageSize).coerceAtMost(taskList.size)
        
        val content = taskList.subList(start, end).map { mapToTaskResponse(it) }
        return PageImpl(content, pageable, taskList.size.toLong())
    }
    
    @Transactional(readOnly = true)
    override fun getOverdueTasks(pageable: Pageable): Page<TaskResponse> {
        val taskList = tasks.values.filter { it.isOverdue() }
        val start = (pageable.pageNumber * pageable.pageSize).coerceAtMost(taskList.size)
        val end = ((pageable.pageNumber + 1) * pageable.pageSize).coerceAtMost(taskList.size)
        
        val content = taskList.subList(start, end).map { mapToTaskResponse(it) }
        return PageImpl(content, pageable, taskList.size.toLong())
    }
    
    @Transactional(readOnly = true)
    override fun getHighPriorityTasks(pageable: Pageable): Page<TaskResponse> {
        val taskList = tasks.values.filter { it.isHighPriority() }
        val start = (pageable.pageNumber * pageable.pageSize).coerceAtMost(taskList.size)
        val end = ((pageable.pageNumber + 1) * pageable.pageSize).coerceAtMost(taskList.size)
        
        val content = taskList.subList(start, end).map { mapToTaskResponse(it) }
        return PageImpl(content, pageable, taskList.size.toLong())
    }
    
    override fun bulkUpdateTasks(request: BulkTaskUpdateRequest): List<TaskResponse> {
        return request.taskIds.mapNotNull { taskId ->
            try {
                val task = tasks[taskId] ?: return@mapNotNull null
                val updatedTask = task.copy(
                    status = request.status ?: task.status,
                    priority = request.priority ?: task.priority,
                    assignee = request.assignee?.let { 
                        Assignee(it.name ?: "", it.email ?: "", it.department ?: "", "") 
                    } ?: task.assignee,
                    updatedAt = LocalDateTime.now()
                )
                tasks[taskId] = updatedTask
                mapToTaskResponse(updatedTask)
            } catch (e: Exception) {
                logger.warn("Failed to update task $taskId: ${e.message}")
                null
            }
        }
    }
    
    override fun bulkAssignTasks(taskIds: Set<Long>, assigneeId: String, reason: String?): List<TaskResponse> {
        return taskIds.mapNotNull { taskId ->
            try {
                val task = tasks[taskId] ?: return@mapNotNull null
                val assignee = Assignee(assigneeId, "User Name", "user@example.com", "Development")
                val updatedTask = task.copy(
                    assignee = assignee,
                    status = TaskStatus.IN_PROGRESS,
                    updatedAt = LocalDateTime.now()
                )
                tasks[taskId] = updatedTask
                mapToTaskResponse(updatedTask)
            } catch (e: Exception) {
                logger.warn("Failed to assign task $taskId: ${e.message}")
                null
            }
        }
    }
    
    override fun bulkChangeStatus(taskIds: Set<Long>, status: TaskStatus, reason: String?): List<TaskResponse> {
        return taskIds.mapNotNull { taskId ->
            try {
                val task = tasks[taskId] ?: return@mapNotNull null
                if (task.canTransitionTo(status)) {
                    val updatedTask = task.copy(
                        status = status,
                        updatedAt = LocalDateTime.now()
                    )
                    tasks[taskId] = updatedTask
                    mapToTaskResponse(updatedTask)
                } else null
            } catch (e: Exception) {
                logger.warn("Failed to change status for task $taskId: ${e.message}")
                null
            }
        }
    }
    
    @Transactional(readOnly = true)
    override fun getTaskSummary(): TaskSummaryResponse {
        val allTasks = tasks.values
        
        return TaskSummaryResponse(
            totalTasks = allTasks.size,
            tasksByStatus = TaskStatus.values().associateWith { status ->
                allTasks.count { it.status == status }
            },
            tasksByPriority = TaskPriority.values().associateWith { priority ->
                allTasks.count { it.priority == priority }
            },
            overdueTasks = allTasks.count { it.isOverdue() },
            averageCompletionTime = 5.5, // Mock calculation
            upcomingDueTasks = allTasks
                .filter { it.dueDate?.isBefore(LocalDateTime.now().plusDays(7)) == true }
                .map { mapToTaskResponse(it) }
        )
    }
    
    @Transactional(readOnly = true)
    override fun getTaskMetrics(): TaskMetricsResponse {
        val allTasks = tasks.values
        val completedTasks = allTasks.filter { it.status == TaskStatus.COMPLETED }
        
        return TaskMetricsResponse(
            completionRate = if (allTasks.isNotEmpty()) completedTasks.size.toDouble() / allTasks.size else 0.0,
            averageEfficiency = 0.85, // Mock calculation
            mostCommonCategory = "Development",
            topPerformers = listOf(
                AssigneeResponse("user1", "John Doe", "john@example.com", "Development"),
                AssigneeResponse("user2", "Jane Smith", "jane@example.com", "Development")
            ),
            bottlenecks = listOf("Code Review", "Testing")
        )
    }
    
    @Transactional(readOnly = true)
    override fun getWorkloadDistribution(): List<Map<String, Any>> {
        return tasks.values
            .filter { it.assignee != null }
            .groupBy { it.assignee!!.userId }
            .map { (assigneeId, tasks) ->
                mapOf(
                    "assigneeId" to assigneeId,
                    "assigneeName" to (tasks.firstOrNull()?.assignee?.name ?: "Unknown"),
                    "taskCount" to tasks.size,
                    "highPriorityCount" to tasks.count { it.isHighPriority() }
                )
            }
    }
    
    // Helper methods
    
    private fun mapToTaskResponse(task: Task): TaskResponse {
        return TaskResponse(
            id = task.id!!,
            title = task.title,
            description = task.description,
            status = task.status,
            priority = task.priority,
            assignee = task.assignee?.let { 
                AssigneeResponse(it.userId, it.name, it.email, it.department) 
            },
            metadata = null,
            projectId = task.projectId,
            createdAt = task.createdAt.toString(),
            updatedAt = task.updatedAt.toString(),
            dueDate = task.dueDate?.toString(),
            isOverdue = task.isOverdue(),
            daysUntilDue = task.getDaysUntilDue(),
            estimatedEffort = task.getEstimatedEffort()
        )
    }
    
    private fun updateTaskFromRequest(task: Task, request: UpdateTaskRequest): Task {
        return task.copy(
            title = request.title ?: task.title,
            description = request.description ?: task.description,
            priority = request.priority ?: task.priority,
            status = request.status ?: task.status,
            dueDate = request.dueDate ?: task.dueDate,
            updatedAt = LocalDateTime.now()
        )
    }
    
    private fun findTaskOrThrow(id: Long): Task {
        return tasks[id] ?: throw RuntimeException("Task not found with id: $id")
    }
    
    private fun logTaskOperation(operation: String, taskId: Long, details: Map<String, Any>) {
        logger.info("Task operation: {} for task: {} with details: {}", operation, taskId, details)
    }
    
    private fun triggerNotifications(task: Task, operation: String) {
        try {
            logger.info("Notification: Task '{}' {} for user '{}'", task.title, operation, task.assignee?.userId ?: "unassigned")
        } catch (e: Exception) {
            logger.warn("Failed to send notification for task {}: {}", task.id, e.message)
        }
    }
    
    private fun createSampleTasks() {
        val sampleTasks = listOf(
            Task.createNew("Implement user authentication", "Add JWT-based authentication to the API", TaskPriority.HIGH)
                .copy(id = 1L, assignee = Assignee("user1", "John Doe", "john@example.com", "Development")),
            Task.createNew("Fix database connection issue", "Resolve intermittent database timeout errors", TaskPriority.CRITICAL)
                .copy(id = 2L, status = TaskStatus.IN_PROGRESS),
            Task.createNew("Update documentation", "Update API documentation with latest endpoints", TaskPriority.LOW)
                .copy(id = 3L, status = TaskStatus.COMPLETED),
            Task.createWithDueDate("Code review for payment module", "Review and approve payment processing code", TaskPriority.MEDIUM, LocalDateTime.now().plusDays(2))
                .copy(id = 4L, assignee = Assignee("user2", "Jane Smith", "jane@example.com", "Development")),
            Task.createNew("Setup CI/CD pipeline", "Configure automated testing and deployment", TaskPriority.HIGH)
                .copy(id = 5L, status = TaskStatus.IN_REVIEW)
        )
        
        sampleTasks.forEach { task ->
            tasks[task.id!!] = task
            idCounter.set(maxOf(idCounter.get(), task.id + 1))
        }
    }
}