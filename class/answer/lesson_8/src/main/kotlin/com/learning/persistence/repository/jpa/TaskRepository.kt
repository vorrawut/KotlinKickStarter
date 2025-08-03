/**
 * Lesson 8 Complete Solution: JPA Repository - TaskRepository
 * 
 * Comprehensive JPA repository demonstrating SQL database capabilities
 */

package com.learning.persistence.repository.jpa

import com.learning.persistence.model.sql.Task
import com.learning.persistence.model.sql.TaskStatus
import com.learning.persistence.model.sql.TaskPriority
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface TaskRepository : JpaRepository<Task, Long> {
    
    // Method name queries - Spring Data JPA automatically implements these
    fun findByStatus(status: TaskStatus): List<Task>
    
    fun findByAssigneeId(assigneeId: String): List<Task>
    
    fun findByProjectId(projectId: Long): List<Task>
    
    fun findByStatusAndPriority(status: TaskStatus, priority: TaskPriority): List<Task>
    
    fun findByDueDateBefore(date: LocalDateTime): List<Task>
    
    fun findByStatusOrderByCreatedAtDesc(status: TaskStatus): List<Task>
    
    fun findByAssigneeIdAndStatusNot(assigneeId: String, status: TaskStatus): List<Task>
    
    fun findByPriorityInAndStatus(priorities: List<TaskPriority>, status: TaskStatus): List<Task>
    
    fun findByCreatedAtBetween(start: LocalDateTime, end: LocalDateTime): List<Task>
    
    fun findByTitleContainingIgnoreCase(titlePart: String): List<Task>
    
    // Custom JPQL queries
    @Query("SELECT t FROM Task t WHERE t.dueDate BETWEEN :start AND :end")
    fun findTasksInDateRange(
        @Param("start") start: LocalDateTime, 
        @Param("end") end: LocalDateTime
    ): List<Task>
    
    @Query("SELECT t FROM Task t WHERE t.assigneeId = :assigneeId AND t.status != 'COMPLETED'")
    fun findActiveTasksByAssignee(@Param("assigneeId") assigneeId: String): List<Task>
    
    @Query("""
        SELECT t FROM Task t 
        WHERE t.status = :status 
        AND t.priority = :priority 
        ORDER BY t.createdAt DESC
    """)
    fun findTasksByStatusAndPriorityOrdered(
        @Param("status") status: TaskStatus,
        @Param("priority") priority: TaskPriority
    ): List<Task>
    
    @Query("""
        SELECT t FROM Task t 
        WHERE t.assigneeId = :assigneeId 
        AND t.dueDate < :now 
        AND t.status NOT IN ('COMPLETED', 'CANCELLED')
    """)
    fun findOverdueTasksByAssignee(
        @Param("assigneeId") assigneeId: String,
        @Param("now") now: LocalDateTime = LocalDateTime.now()
    ): List<Task>
    
    @Query("""
        SELECT t FROM Task t 
        WHERE (:status IS NULL OR t.status = :status)
        AND (:priority IS NULL OR t.priority = :priority)
        AND (:assigneeId IS NULL OR t.assigneeId = :assigneeId)
        AND (:projectId IS NULL OR t.projectId = :projectId)
    """)
    fun findTasksByDynamicCriteria(
        @Param("status") status: TaskStatus?,
        @Param("priority") priority: TaskPriority?,
        @Param("assigneeId") assigneeId: String?,
        @Param("projectId") projectId: Long?
    ): List<Task>
    
    // Pagination queries
    fun findByStatus(status: TaskStatus, pageable: Pageable): Page<Task>
    
    fun findByProjectId(projectId: Long, pageable: Pageable): Page<Task>
    
    @Query("SELECT t FROM Task t WHERE t.assigneeId = :assigneeId")
    fun findByAssigneeId(
        @Param("assigneeId") assigneeId: String, 
        pageable: Pageable
    ): Page<Task>
    
    // Native SQL queries for complex operations
    @Query(
        value = """
            SELECT * FROM tasks 
            WHERE priority = ?1 
            AND created_at > ?2 
            ORDER BY due_date ASC NULLS LAST
        """,
        nativeQuery = true
    )
    fun findTasksByPriorityAndDate(
        priority: String, 
        date: LocalDateTime
    ): List<Task>
    
    @Query(
        value = """
            SELECT t.* FROM tasks t 
            WHERE t.status IN ('CREATED', 'IN_PROGRESS') 
            AND (t.due_date IS NULL OR t.due_date > NOW())
            AND t.assignee_id = ?1
            ORDER BY 
                CASE t.priority 
                    WHEN 'CRITICAL' THEN 1 
                    WHEN 'HIGH' THEN 2 
                    WHEN 'MEDIUM' THEN 3 
                    ELSE 4 
                END,
                t.created_at DESC
        """,
        nativeQuery = true
    )
    fun findActiveTasksByAssigneeOrderByPriority(assigneeId: String): List<Task>
    
    // Aggregate queries
    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = :status")
    fun countByStatus(@Param("status") status: TaskStatus): Long
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.assigneeId = :assigneeId AND t.status != 'COMPLETED'")
    fun countActiveTasksByAssignee(@Param("assigneeId") assigneeId: String): Long
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.dueDate < :now AND t.status NOT IN ('COMPLETED', 'CANCELLED')")
    fun countOverdueTasks(@Param("now") now: LocalDateTime = LocalDateTime.now()): Long
    
    @Query("""
        SELECT t.assigneeId, COUNT(t) 
        FROM Task t 
        WHERE t.status != 'COMPLETED' AND t.assigneeId IS NOT NULL
        GROUP BY t.assigneeId
        ORDER BY COUNT(t) DESC
    """)
    fun getActiveTaskCountByAssignee(): List<Array<Any>>
    
    @Query("""
        SELECT t.status, COUNT(t), AVG(CAST(t.priority AS int))
        FROM Task t 
        GROUP BY t.status
    """)
    fun getTaskStatsByStatus(): List<Array<Any>>
    
    @Query("""
        SELECT DATE(t.createdAt), COUNT(t)
        FROM Task t 
        WHERE t.createdAt >= :fromDate
        GROUP BY DATE(t.createdAt)
        ORDER BY DATE(t.createdAt) DESC
    """)
    fun getDailyTaskCreationCount(@Param("fromDate") fromDate: LocalDateTime): List<Array<Any>>
    
    // Update queries
    @Query("UPDATE Task t SET t.status = :newStatus, t.updatedAt = :now WHERE t.id IN :ids")
    fun bulkUpdateStatus(
        @Param("ids") ids: List<Long>,
        @Param("newStatus") newStatus: TaskStatus,
        @Param("now") now: LocalDateTime = LocalDateTime.now()
    ): Int
    
    @Query("UPDATE Task t SET t.assigneeId = :assigneeId, t.updatedAt = :now WHERE t.id IN :ids")
    fun bulkAssignTasks(
        @Param("ids") ids: List<Long>,
        @Param("assigneeId") assigneeId: String,
        @Param("now") now: LocalDateTime = LocalDateTime.now()
    ): Int
    
    // Complex analytical queries
    @Query("""
        SELECT 
            t.projectId,
            COUNT(t) as totalTasks,
            SUM(CASE WHEN t.status = 'COMPLETED' THEN 1 ELSE 0 END) as completedTasks,
            AVG(CASE WHEN t.status = 'COMPLETED' THEN 
                EXTRACT(DAY FROM (t.updatedAt - t.createdAt)) ELSE NULL END) as avgCompletionDays
        FROM Task t 
        WHERE t.projectId IS NOT NULL
        GROUP BY t.projectId
        HAVING COUNT(t) > :minTasks
        ORDER BY completedTasks DESC
    """)
    fun getProjectTaskStatistics(@Param("minTasks") minTasks: Long = 1): List<Array<Any>>
    
    @Query("""
        SELECT 
            t.assigneeId,
            COUNT(t) as totalAssigned,
            COUNT(CASE WHEN t.status = 'COMPLETED' THEN 1 END) as completed,
            COUNT(CASE WHEN t.dueDate < :now AND t.status NOT IN ('COMPLETED', 'CANCELLED') THEN 1 END) as overdue
        FROM Task t 
        WHERE t.assigneeId IS NOT NULL
        GROUP BY t.assigneeId
        ORDER BY completed DESC, overdue ASC
    """)
    fun getAssigneePerformanceStats(@Param("now") now: LocalDateTime = LocalDateTime.now()): List<Array<Any>>
    
    fun findByPriority(priority: TaskPriority): List<Task>
}