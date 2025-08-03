/**
 * Lesson 8 Workshop: JPA Repository - TaskRepository
 * 
 * TODO: Create comprehensive JPA repository for Task entity
 * Demonstrate different query methods and JPA features
 */

package com.learning.persistence.repository.jpa

// TODO: Import necessary Spring Data JPA classes
// TODO: Import JpaRepository
// TODO: Import @Query and @Param annotations
// TODO: Import Pageable and Page for pagination
// TODO: Import your Task entity and enums
import com.learning.persistence.model.sql.Task
import com.learning.persistence.model.sql.TaskStatus
import com.learning.persistence.model.sql.TaskPriority
import java.time.LocalDateTime

// TODO: Create interface extending JpaRepository<Task, Long>
interface TaskRepository {
    
    // TODO: Add method name queries (Spring Data will implement automatically)
    // Examples:
    // - findByStatus(status: TaskStatus): List<Task>
    // - findByAssigneeId(assigneeId: String): List<Task>
    // - findByProjectId(projectId: Long): List<Task>
    // - findByStatusAndPriority(status: TaskStatus, priority: TaskPriority): List<Task>
    // - findByDueDateBefore(date: LocalDateTime): List<Task>
    // - findByStatusOrderByCreatedAtDesc(status: TaskStatus): List<Task>
    
    // TODO: Add custom JPQL queries using @Query annotation
    // Examples:
    // @Query("SELECT t FROM Task t WHERE t.dueDate BETWEEN :start AND :end")
    // fun findTasksInDateRange(@Param("start") start: LocalDateTime, @Param("end") end: LocalDateTime): List<Task>
    
    // @Query("SELECT t FROM Task t WHERE t.assigneeId = :assigneeId AND t.status != 'COMPLETED'")
    // fun findActiveTasksByAssignee(@Param("assigneeId") assigneeId: String): List<Task>
    
    // TODO: Add query with pagination
    // fun findByStatus(status: TaskStatus, pageable: Pageable): Page<Task>
    
    // TODO: Add native SQL queries for complex operations
    // @Query(value = "SELECT * FROM tasks WHERE priority = ?1 AND created_at > ?2", nativeQuery = true)
    // fun findTasksByPriorityAndDate(priority: String, date: LocalDateTime): List<Task>
    
    // TODO: Add aggregate queries
    // @Query("SELECT COUNT(t) FROM Task t WHERE t.status = :status")
    // fun countByStatus(@Param("status") status: TaskStatus): Long
    
    // @Query("SELECT t.assigneeId, COUNT(t) FROM Task t WHERE t.status != 'COMPLETED' GROUP BY t.assigneeId")
    // fun getActiveTaskCountByAssignee(): List<Array<Any>>
}