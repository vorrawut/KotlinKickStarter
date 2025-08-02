/**
 * Lesson 2 Complete Solution: User Analytics Service
 * 
 * This demonstrates:
 * - Complex collection operations
 * - Functional programming patterns
 * - Data aggregation and analysis
 * - Method chaining and composition
 */

class UserAnalyticsService {
    
    // Generate comprehensive department analysis
    fun analyzeDepartments(users: List<User>): Map<String, DepartmentStats> {
        return users.groupBy { it.department }
            .mapValues { (deptName, deptUsers) -> 
                DepartmentStats.fromUsers(deptName, deptUsers) 
            }
    }
    
    // Find top performers across all departments
    fun getTopPerformers(users: List<User>, count: Int = 10): List<User> {
        return users
            .filter { it.isActive }
            .sortedByDescending { it.performanceScore }
            .take(count)
    }
    
    // Generate comprehensive activity insights
    fun generateActivityInsights(users: List<User>): ActivityInsights {
        val totalUsers = users.size
        val activeUsers = users.count { it.isActive }
        val usersWithProjects = users.count { it.projectsCompleted > 0 }
        val recentJoiners = users.count { it.isRecentHire(30) }
        val totalProjects = users.sumOf { it.projectsCompleted }
        
        return ActivityInsights(
            totalUsers = totalUsers,
            activeUsers = activeUsers,
            activePercentage = if (totalUsers == 0) 0.0 else (activeUsers.toDouble() / totalUsers) * 100,
            usersWithProjects = usersWithProjects,
            projectsPercentage = if (totalUsers == 0) 0.0 else (usersWithProjects.toDouble() / totalUsers) * 100,
            recentJoiners = recentJoiners,
            recentJoinersPercentage = if (totalUsers == 0) 0.0 else (recentJoiners.toDouble() / totalUsers) * 100,
            averageProjectsPerUser = if (totalUsers == 0) 0.0 else totalProjects.toDouble() / totalUsers
        )
    }
    
    // Find users matching multiple criteria (advanced filtering)
    fun findUsersMatching(
        users: List<User>,
        department: String? = null,
        minAge: Int? = null,
        maxAge: Int? = null,
        minSalary: Double? = null,
        maxSalary: Double? = null,
        isActive: Boolean? = null,
        minPerformanceScore: Double? = null,
        experienceLevel: String? = null
    ): List<User> {
        return users.filter { user ->
            (department?.let { user.department.equals(it, ignoreCase = true) } ?: true) &&
            (minAge?.let { user.age >= it } ?: true) &&
            (maxAge?.let { user.age <= it } ?: true) &&
            (minSalary?.let { user.salary >= it } ?: true) &&
            (maxSalary?.let { user.salary <= it } ?: true) &&
            (isActive?.let { user.isActive == it } ?: true) &&
            (minPerformanceScore?.let { user.performanceScore >= it } ?: true) &&
            (experienceLevel?.let { user.experienceLevel.equals(it, ignoreCase = true) } ?: true)
        }
    }
    
    // Calculate salary statistics by department
    fun getSalaryStatsByDepartment(users: List<User>): Map<String, SalaryStats> {
        return users.groupBy { it.department }
            .mapValues { (deptName, deptUsers) ->
                if (deptUsers.isEmpty()) {
                    SalaryStats(deptName, 0.0, 0.0, 0.0, 0.0, 0)
                } else {
                    val salaries = deptUsers.map { it.salary }.sorted()
                    val median = if (salaries.size % 2 == 0) {
                        (salaries[salaries.size / 2 - 1] + salaries[salaries.size / 2]) / 2
                    } else {
                        salaries[salaries.size / 2]
                    }
                    
                    SalaryStats(
                        department = deptName,
                        minSalary = salaries.minOrNull() ?: 0.0,
                        maxSalary = salaries.maxOrNull() ?: 0.0,
                        averageSalary = salaries.average(),
                        medianSalary = median,
                        userCount = deptUsers.size
                    )
                }
            }
    }
    
    // Generate performance trends by experience level
    fun getPerformanceTrends(users: List<User>): Map<String, Double> {
        return users.groupBy { it.experienceLevel }
            .mapValues { (_, levelUsers) -> 
                if (levelUsers.isEmpty()) 0.0 
                else levelUsers.map { it.performanceScore }.average() 
            }
    }
    
    // Find departments needing attention
    fun getDepartmentsNeedingAttention(users: List<User>): List<String> {
        val deptStats = analyzeDepartments(users)
        return deptStats.values
            .filter { !it.isHealthy() }
            .map { it.departmentName }
    }
    
    // Generate hiring recommendations
    fun getHiringRecommendations(users: List<User>): Map<String, Int> {
        val deptStats = analyzeDepartments(users)
        val avgDeptSize = deptStats.values.map { it.userCount }.average()
        
        return deptStats.mapValues { (_, stats) ->
            when {
                stats.userCount < avgDeptSize * 0.7 -> 2 // Understaffed
                stats.averagePerformanceScore < 70 -> 1 // Performance issues
                stats.activityPercentage < 80 -> 1 // Activity issues
                else -> 0
            }
        }.filter { it.value > 0 }
    }
    
    // Generate executive summary
    fun generateExecutiveSummary(users: List<User>): ExecutiveSummary {
        val deptStats = analyzeDepartments(users)
        val insights = generateActivityInsights(users)
        val performanceTrends = getPerformanceTrends(users)
        
        val topDept = deptStats.values.maxByOrNull { it.averagePerformanceScore }?.departmentName ?: "N/A"
        val keyInsights = buildList {
            add("Company has ${users.size} employees across ${deptStats.size} departments")
            add("Overall activity rate: ${"%.1f".format(insights.activePercentage)}%")
            if (insights.recentJoinersPercentage > 20) {
                add("High recent hiring activity: ${"%.1f".format(insights.recentJoinersPercentage)}% new joiners")
            }
            val avgPerformance = users.averagePerformanceScore()
            add("Company average performance: ${"%.1f".format(avgPerformance)}")
        }
        
        val recommendations = buildList {
            if (insights.activePercentage < 85) {
                add("Focus on employee engagement - activity rate below target")
            }
            val underperformingDepts = getDepartmentsNeedingAttention(users)
            if (underperformingDepts.isNotEmpty()) {
                add("Review departments: ${underperformingDepts.joinToString(", ")}")
            }
            val hiringNeeds = getHiringRecommendations(users)
            if (hiringNeeds.isNotEmpty()) {
                add("Consider hiring in: ${hiringNeeds.keys.joinToString(", ")}")
            }
        }
        
        return ExecutiveSummary(
            totalEmployees = users.size,
            departmentCount = deptStats.size,
            overallPerformanceScore = users.averagePerformanceScore(),
            topPerformingDepartment = topDept,
            keyInsights = keyInsights,
            recommendations = recommendations
        )
    }
    
    // Performance ranking within departments
    fun getDepartmentRankings(users: List<User>): Map<String, List<User>> {
        return users.groupBy { it.department }
            .mapValues { (_, deptUsers) ->
                deptUsers.sortedByDescending { it.performanceScore }
            }
    }
    
    // Identify high-potential employees
    fun getHighPotentialEmployees(users: List<User>): List<User> {
        return users.filter { user ->
            user.isActive &&
            user.isHighPerformer() &&
            user.projectsCompleted >= 5 &&
            user.yearsOfExperience() >= 1
        }.sortedByDescending { it.performanceScore }
    }
}

// Supporting data classes for analytics

data class ActivityInsights(
    val totalUsers: Int,
    val activeUsers: Int,
    val activePercentage: Double,
    val usersWithProjects: Int,
    val projectsPercentage: Double,
    val recentJoiners: Int,
    val recentJoinersPercentage: Double,
    val averageProjectsPerUser: Double
) {
    fun formatSummary(): String = """
        Total Users: $totalUsers
        Active Users: $activeUsers (${"%.1f".format(activePercentage)}%)
        Users with Projects: $usersWithProjects (${"%.1f".format(projectsPercentage)}%)
        Recent Joiners: $recentJoiners (${"%.1f".format(recentJoinersPercentage)}%)
        Average Projects per User: ${"%.1f".format(averageProjectsPerUser)}
    """.trimIndent()
}

data class SalaryStats(
    val department: String,
    val minSalary: Double,
    val maxSalary: Double,
    val averageSalary: Double,
    val medianSalary: Double,
    val userCount: Int,
    val salaryRange: Double = maxSalary - minSalary
) {
    fun hasHighVariance(): Boolean = salaryRange > averageSalary * 0.5
    
    fun formatSummary(): String = 
        "$department: $${"%,.0f".format(averageSalary)} avg ($${"%,.0f".format(minSalary)}-$${"%,.0f".format(maxSalary)}), $userCount users"
}

data class ExecutiveSummary(
    val totalEmployees: Int,
    val departmentCount: Int,
    val overallPerformanceScore: Double,
    val topPerformingDepartment: String,
    val keyInsights: List<String>,
    val recommendations: List<String>
) {
    fun formatReport(): String = """
        === Executive Summary ===
        Total Employees: $totalEmployees
        Departments: $departmentCount
        Overall Performance: ${"%.1f".format(overallPerformanceScore)}
        Top Department: $topPerformingDepartment
        
        Key Insights:
        ${keyInsights.joinToString("\n") { "• $it" }}
        
        Recommendations:
        ${recommendations.joinToString("\n") { "• $it" }}
    """.trimIndent()
}