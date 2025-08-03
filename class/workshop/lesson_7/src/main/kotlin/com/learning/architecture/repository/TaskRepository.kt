/**
 * Lesson 7 Workshop: Task Repository Interface
 * 
 * TODO: Complete this repository interface for clean architecture
 * This demonstrates:
 * - Repository pattern for data access abstraction
 * - Interface segregation principle
 * - Dependency inversion principle
 * - Domain-focused query methods
 */

package com.learning.architecture.repository

import com.learning.architecture.model.Task
import com.learning.architecture.model.TaskPriority
import com.learning.architecture.model.TaskStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

// TODO: Add @Repository annotation and extend JpaRepository
interface TaskRepository : JpaRepository<Task, Long> {
    
    // Basic query methods
    // TODO: Add method to find tasks by status
    fun findByStatus(status: TaskStatus): List<Task>
    
    // TODO: Add method to find tasks by priority
    fun findByPriority(priority: TaskPriority): List<Task>
    
    // TODO: Add method to find tasks by assignee
    fun findByAssigneeUserId(userId: String): List<Task>
    
    // TODO: Add method to find tasks by project ID
    fun findByProjectId(projectId: Long): List<Task>
    
    // Complex query methods for business logic
    // TODO: Add method to find overdue tasks
    fun findOverdueTasks(): List<Task>
    
    // TODO: Add method to find tasks due within specified days
    fun findTasksDueWithinDays(days: Int): List<Task>
    
    // TODO: Add method to find high priority tasks
    fun findHighPriorityTasks(): List<Task>
    
    // TODO: Add method to find tasks by multiple criteria
    fun findTasksByCriteria(
        status: TaskStatus?,
        priority: TaskPriority?,
        assigneeId: String?,
        projectId: Long?,
        pageable: Pageable
    ): Page<Task>
    
    // Custom queries with @Query annotation
    // TODO: Add custom query for task statistics
    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = :status")
    fun countByStatus(@Param("status") status: TaskStatus): Long
    
    // TODO: Add custom query for assignee workload
    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignee.userId = :userId AND t.status != 'COMPLETED'")
    fun countActiveTasksByAssignee(@Param("userId") userId: String): Long
    
    // TODO: Add custom query for project progress
    @Query("SELECT COUNT(t) FROM Task t WHERE t.projectId = :projectId AND t.status = 'COMPLETED'")
    fun countCompletedTasksByProject(@Param("projectId") projectId: Long): Long
    
    // TODO: Add custom query for overdue tasks by project
    @Query("SELECT t FROM Task t WHERE t.projectId = :projectId AND t.dueDate < :currentDate AND t.status != 'COMPLETED'")
    fun findOverdueTasksByProject(
        @Param("projectId") projectId: Long,
        @Param("currentDate") currentDate: LocalDateTime
    ): List<Task>
    
    // TODO: Add custom query for task metrics
    @Query("""
        SELECT new map(
            t.status as status,
            COUNT(t) as count,
            AVG(CAST(DATEDIFF(t.updatedAt, t.createdAt) AS double)) as avgDuration
        )
        FROM Task t 
        WHERE t.projectId = :projectId 
        GROUP BY t.status
    """)
    fun getTaskMetricsByProject(@Param("projectId") projectId: Long): List<Map<String, Any>>
    
    // Advanced query methods
    // TODO: Add method to find tasks requiring attention
    @Query("""
        SELECT t FROM Task t 
        WHERE (t.dueDate < :warningDate AND t.status != 'COMPLETED') 
        OR (t.priority = 'CRITICAL' AND t.status = 'CREATED')
        OR (t.status = 'IN_PROGRESS' AND t.updatedAt < :staleDate)
    """)
    fun findTasksRequiringAttention(
        @Param("warningDate") warningDate: LocalDateTime,
        @Param("staleDate") staleDate: LocalDateTime
    ): List<Task>
    
    // TODO: Add method for workload distribution
    @Query("""
        SELECT new map(
            t.assignee.userId as assigneeId,
            t.assignee.name as assigneeName,
            COUNT(t) as taskCount,
            SUM(CASE WHEN t.priority = 'HIGH' OR t.priority = 'CRITICAL' THEN 1 ELSE 0 END) as highPriorityCount
        )
        FROM Task t 
        WHERE t.status != 'COMPLETED' AND t.assignee IS NOT NULL
        GROUP BY t.assignee.userId, t.assignee.name
    """)
    fun getWorkloadDistribution(): List<Map<String, Any>>
    
    // TODO: Add method for task trend analysis
    @Query("""
        SELECT new map(
            DATE(t.createdAt) as date,
            COUNT(t) as created,
            SUM(CASE WHEN t.status = 'COMPLETED' THEN 1 ELSE 0 END) as completed
        )
        FROM Task t 
        WHERE t.createdAt >= :startDate 
        GROUP BY DATE(t.createdAt)
        ORDER BY DATE(t.createdAt)
    """)
    fun getTaskTrends(@Param("startDate") startDate: LocalDateTime): List<Map<String, Any>>
}

// TODO: Create specialized repository interface for task search operations
interface TaskSearchRepository {
    
    // TODO: Add method for advanced search with multiple criteria
    fun searchTasks(
        title: String?,
        status: TaskStatus?,
        priority: TaskPriority?,
        assigneeId: String?,
        projectId: Long?,
        category: String?,
        tags: Set<String>?,
        isOverdue: Boolean?,
        dueBefore: LocalDateTime?,
        dueAfter: LocalDateTime?,
        pageable: Pageable
    ): Page<Task>
    
    // TODO: Add method for full-text search
    fun searchTasksByText(
        searchText: String,
        pageable: Pageable
    ): Page<Task>
    
    // TODO: Add method for similar tasks recommendation
    fun findSimilarTasks(task: Task, limit: Int): List<Task>
}

// TODO: Create repository interface for task analytics
interface TaskAnalyticsRepository {
    
    // TODO: Add method for completion rate analytics
    fun getCompletionRateByPeriod(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        groupBy: String
    ): List<Map<String, Any>>
    
    // TODO: Add method for bottleneck analysis
    fun identifyBottlenecks(): List<Map<String, Any>>
    
    // TODO: Add method for performance metrics
    fun getPerformanceMetrics(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Map<String, Any>
    
    // TODO: Add method for predictive analytics
    fun predictTaskCompletion(taskId: Long): Map<String, Any>
}