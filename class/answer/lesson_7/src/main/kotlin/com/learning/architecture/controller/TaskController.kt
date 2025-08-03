/**
 * Lesson 7 Complete Solution: Task Controller with Clean Architecture Integration
 */

package com.learning.architecture.controller

import com.learning.architecture.dto.*
import com.learning.architecture.model.TaskPriority
import com.learning.architecture.model.TaskStatus
import com.learning.architecture.service.TaskService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tasks")
class TaskController(
    private val taskService: TaskService
) {
    
    private val logger = LoggerFactory.getLogger(TaskController::class.java)
    
    @PostMapping
    fun createTask(
        @Valid @RequestBody request: CreateTaskRequest
    ): ResponseEntity<TaskResponse> {
        logRequest("CREATE_TASK", request)
        val startTime = System.currentTimeMillis()
        
        val response = taskService.createTask(request)
        
        logResponse("CREATE_TASK", response, System.currentTimeMillis() - startTime)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
    
    @GetMapping("/{id}")
    fun getTask(
        @PathVariable id: Long
    ): ResponseEntity<TaskResponse> {
        val response = taskService.getTaskById(id)
        return ResponseEntity.ok(response)
    }
    
    @PutMapping("/{id}")
    fun updateTask(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateTaskRequest
    ): ResponseEntity<TaskResponse> {
        val response = taskService.updateTask(id, request)
        return ResponseEntity.ok(response)
    }
    
    @DeleteMapping("/{id}")
    fun deleteTask(
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        taskService.deleteTask(id)
        return ResponseEntity.noContent().build()
    }
    
    @GetMapping
    fun getAllTasks(
        pageable: Pageable
    ): ResponseEntity<Page<TaskResponse>> {
        val response = taskService.getAllTasks(pageable)
        return ResponseEntity.ok(response)
    }
    
    @PostMapping("/{id}/assign")
    fun assignTask(
        @PathVariable id: Long,
        @Valid @RequestBody request: AssignTaskRequest
    ): ResponseEntity<TaskResponse> {
        val response = taskService.assignTask(id, request)
        return ResponseEntity.ok(response)
    }
    
    @PostMapping("/{id}/complete")
    fun completeTask(
        @PathVariable id: Long,
        @Valid @RequestBody request: CompleteTaskRequest
    ): ResponseEntity<TaskResponse> {
        val response = taskService.completeTask(id, request)
        return ResponseEntity.ok(response)
    }
    
    @PatchMapping("/{id}/status")
    fun changeTaskStatus(
        @PathVariable id: Long,
        @RequestParam status: TaskStatus,
        @RequestParam(required = false) reason: String?
    ): ResponseEntity<TaskResponse> {
        val response = taskService.changeTaskStatus(id, status, reason)
        return ResponseEntity.ok(response)
    }
    
    @PatchMapping("/{id}/priority")
    fun changeTaskPriority(
        @PathVariable id: Long,
        @RequestParam priority: TaskPriority,
        @RequestParam(required = false) reason: String?
    ): ResponseEntity<TaskResponse> {
        val response = taskService.changeTaskPriority(id, priority, reason)
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/search")
    fun searchTasks(
        @ModelAttribute searchRequest: TaskSearchRequest
    ): ResponseEntity<Page<TaskResponse>> {
        val response = taskService.searchTasks(searchRequest)
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/status/{status}")
    fun getTasksByStatus(
        @PathVariable status: TaskStatus,
        pageable: Pageable
    ): ResponseEntity<Page<TaskResponse>> {
        val response = taskService.getTasksByStatus(status, pageable)
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/assignee/{assigneeId}")
    fun getTasksByAssignee(
        @PathVariable assigneeId: String,
        pageable: Pageable
    ): ResponseEntity<Page<TaskResponse>> {
        val response = taskService.getTasksByAssignee(assigneeId, pageable)
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/overdue")
    fun getOverdueTasks(
        pageable: Pageable
    ): ResponseEntity<Page<TaskResponse>> {
        val response = taskService.getOverdueTasks(pageable)
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/high-priority")
    fun getHighPriorityTasks(
        pageable: Pageable
    ): ResponseEntity<Page<TaskResponse>> {
        val response = taskService.getHighPriorityTasks(pageable)
        return ResponseEntity.ok(response)
    }
    
    @PostMapping("/bulk-update")
    fun bulkUpdateTasks(
        @Valid @RequestBody request: BulkTaskUpdateRequest
    ): ResponseEntity<List<TaskResponse>> {
        val response = taskService.bulkUpdateTasks(request)
        return ResponseEntity.ok(response)
    }
    
    @PostMapping("/bulk-assign")
    fun bulkAssignTasks(
        @RequestParam taskIds: Set<Long>,
        @RequestParam assigneeId: String,
        @RequestParam(required = false) reason: String?
    ): ResponseEntity<List<TaskResponse>> {
        val response = taskService.bulkAssignTasks(taskIds, assigneeId, reason)
        return ResponseEntity.ok(response)
    }
    
    @PostMapping("/bulk-status")
    fun bulkChangeStatus(
        @RequestParam taskIds: Set<Long>,
        @RequestParam status: TaskStatus,
        @RequestParam(required = false) reason: String?
    ): ResponseEntity<List<TaskResponse>> {
        val response = taskService.bulkChangeStatus(taskIds, status, reason)
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/summary")
    fun getTaskSummary(): ResponseEntity<TaskSummaryResponse> {
        val response = taskService.getTaskSummary()
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/metrics")
    fun getTaskMetrics(): ResponseEntity<TaskMetricsResponse> {
        val response = taskService.getTaskMetrics()
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/workload-distribution")
    fun getWorkloadDistribution(): ResponseEntity<List<Map<String, Any>>> {
        val response = taskService.getWorkloadDistribution()
        return ResponseEntity.ok(response)
    }
    
    private fun logRequest(operation: String, request: Any?) {
        logger.info("Incoming request: {} with payload: {}", operation, request.toString())
    }
    
    private fun logResponse(operation: String, response: Any, duration: Long) {
        logger.info("Outgoing response: {} completed in {}ms", operation, duration)
    }
}