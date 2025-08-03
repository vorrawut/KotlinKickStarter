/**
 * Lesson 7 Workshop: Project Repository Interface
 * 
 * TODO: Complete this repository interface for project management
 * This demonstrates:
 * - Aggregate repository patterns
 * - Complex query interfaces
 * - Repository composition
 * - Domain-specific data access
 */

package com.learning.architecture.repository

import com.learning.architecture.model.Project
import com.learning.architecture.model.ProjectStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime

// TODO: Add @Repository annotation and extend JpaRepository
interface ProjectRepository : JpaRepository<Project, Long> {
    
    // Basic query methods
    // TODO: Add method to find projects by status
    fun findByStatus(status: ProjectStatus): List<Project>
    
    // TODO: Add method to find projects by owner
    fun findByOwnerUserId(userId: String): List<Project>
    
    // TODO: Add method to find project by unique code
    fun findByCode(code: String): Project?
    
    // TODO: Add method to check if code exists
    fun existsByCode(code: String): Boolean
    
    // Business logic query methods
    // TODO: Add method to find active projects
    fun findActiveProjects(): List<Project>
    
    // TODO: Add method to find overdue projects
    fun findOverdueProjects(): List<Project>
    
    // TODO: Add method to find projects ending soon
    fun findProjectsEndingSoon(days: Int): List<Project>
    
    // TODO: Add method to find projects by multiple criteria
    fun findProjectsByCriteria(
        name: String?,
        code: String?,
        status: ProjectStatus?,
        ownerId: String?,
        startDateAfter: LocalDate?,
        startDateBefore: LocalDate?,
        endDateAfter: LocalDate?,
        endDateBefore: LocalDate?,
        isOverdue: Boolean?,
        pageable: Pageable
    ): Page<Project>
    
    // Custom queries for project analytics
    // TODO: Add query for project statistics
    @Query("SELECT COUNT(p) FROM Project p WHERE p.status = :status")
    fun countByStatus(@Param("status") status: ProjectStatus): Long
    
    // TODO: Add query for owner workload
    @Query("SELECT COUNT(p) FROM Project p WHERE p.owner.userId = :userId AND p.status IN ('IN_PROGRESS', 'PLANNING')")
    fun countActiveProjectsByOwner(@Param("userId") userId: String): Long
    
    // TODO: Add query for project completion rate
    @Query("""
        SELECT new map(
            p.status as status,
            COUNT(p) as count,
            AVG(CAST(DATEDIFF(p.actualEndDate, p.startDate) AS double)) as avgDuration
        )
        FROM Project p 
        WHERE p.actualEndDate IS NOT NULL
        GROUP BY p.status
    """)
    fun getProjectCompletionMetrics(): List<Map<String, Any>>
    
    // TODO: Add query for project health analysis
    @Query("""
        SELECT p FROM Project p 
        WHERE p.status = 'IN_PROGRESS' 
        AND (
            p.targetEndDate < :currentDate 
            OR (
                SELECT COUNT(t) FROM Task t 
                WHERE t.projectId = p.id AND t.status = 'COMPLETED'
            ) * 100.0 / (
                SELECT COUNT(t) FROM Task t 
                WHERE t.projectId = p.id
            ) < 50.0
        )
    """)
    fun findProjectsNeedingAttention(@Param("currentDate") currentDate: LocalDate): List<Project>
    
    // TODO: Add query for project timeline analysis
    @Query("""
        SELECT new map(
            DATE(p.startDate) as month,
            COUNT(p) as projectsStarted,
            SUM(CASE WHEN p.status = 'COMPLETED' THEN 1 ELSE 0 END) as projectsCompleted,
            AVG(p.tasks.size) as avgTaskCount
        )
        FROM Project p 
        WHERE p.startDate >= :startDate 
        GROUP BY DATE(p.startDate)
        ORDER BY DATE(p.startDate)
    """)
    fun getProjectTimeline(@Param("startDate") LocalDate: LocalDate): List<Map<String, Any>>
    
    // TODO: Add query for resource utilization
    @Query("""
        SELECT new map(
            p.owner.userId as ownerId,
            p.owner.name as ownerName,
            COUNT(p) as projectCount,
            SUM(p.tasks.size) as totalTasks,
            AVG(CASE WHEN p.status = 'COMPLETED' THEN 
                DATEDIFF(p.actualEndDate, p.startDate) 
                ELSE DATEDIFF(:currentDate, p.startDate) 
            END) as avgProjectDuration
        )
        FROM Project p 
        GROUP BY p.owner.userId, p.owner.name
    """)
    fun getResourceUtilization(@Param("currentDate") currentDate: LocalDate): List<Map<String, Any>>
}

// TODO: Create specialized repository for project search operations
interface ProjectSearchRepository {
    
    // TODO: Add method for advanced project search
    fun searchProjects(
        name: String?,
        code: String?,
        status: ProjectStatus?,
        ownerId: String?,
        startDateAfter: LocalDate?,
        startDateBefore: LocalDate?,
        endDateAfter: LocalDate?,
        endDateBefore: LocalDate?,
        isOverdue: Boolean?,
        hasTasksInStatus: String?,
        minCompletionRate: Double?,
        maxCompletionRate: Double?,
        pageable: Pageable
    ): Page<Project>
    
    // TODO: Add method for full-text search across projects
    fun searchProjectsByText(
        searchText: String,
        includeTaskContent: Boolean,
        pageable: Pageable
    ): Page<Project>
    
    // TODO: Add method for finding similar projects
    fun findSimilarProjects(
        project: Project,
        similarityFactors: List<String>,
        limit: Int
    ): List<Project>
}

// TODO: Create repository interface for project analytics and reporting
interface ProjectAnalyticsRepository {
    
    // TODO: Add method for project success rate analysis
    fun getProjectSuccessRates(
        startDate: LocalDate,
        endDate: LocalDate,
        groupBy: String
    ): List<Map<String, Any>>
    
    // TODO: Add method for project risk assessment
    fun assessProjectRisks(): List<Map<String, Any>>
    
    // TODO: Add method for capacity planning
    fun getCapacityPlanningMetrics(
        timeframe: String
    ): Map<String, Any>
    
    // TODO: Add method for deadline prediction
    fun predictProjectDeadlines(projectId: Long): Map<String, Any>
    
    // TODO: Add method for portfolio analysis
    fun getPortfolioAnalysis(
        ownerId: String?,
        departmentFilter: String?
    ): Map<String, Any>
    
    // TODO: Add method for trend analysis
    fun getProjectTrends(
        startDate: LocalDate,
        endDate: LocalDate,
        metrics: List<String>
    ): List<Map<String, Any>>
    
    // TODO: Add method for performance benchmarks
    fun getPerformanceBenchmarks(
        projectType: String?,
        timeframe: String
    ): Map<String, Any>
    
    // TODO: Add method for resource optimization recommendations
    fun getResourceOptimizationRecommendations(): List<Map<String, Any>>
}

// TODO: Create repository interface for project dashboard data
interface ProjectDashboardRepository {
    
    // TODO: Add method for dashboard overview
    fun getDashboardOverview(): Map<String, Any>
    
    // TODO: Add method for urgent items
    fun getUrgentItems(): Map<String, List<Any>>
    
    // TODO: Add method for team performance
    fun getTeamPerformanceMetrics(): List<Map<String, Any>>
    
    // TODO: Add method for upcoming deadlines
    fun getUpcomingDeadlines(days: Int): List<Map<String, Any>>
    
    // TODO: Add method for health score distribution
    fun getHealthScoreDistribution(): Map<String, Int>
}