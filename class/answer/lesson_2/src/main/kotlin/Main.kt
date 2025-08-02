/**
 * Lesson 2 Complete Solution: Main Application
 * 
 * This demonstrates:
 * - Using collection operations in real scenarios
 * - Chaining functional operations
 * - Data processing pipelines
 * - Analytics and reporting
 */

fun main() {
    println("=== User Analytics Dashboard ===")
    println("🚀 Welcome to Lesson 2: Collections & Functional Programming!\n")
    
    val analyticsService = UserAnalyticsService()
    
    // Generate and display sample data
    println("📊 Generating sample data...")
    val users = UserDataGenerator.generateUsers(1000)
    println("✓ Generated ${users.size} users")
    println("✓ Departments: ${users.getUniqueDepartments().joinToString(", ")}\n")
    
    // Demonstrate basic collection operations
    println("🔍 Basic Collection Operations:")
    
    // Show total users and active users
    val activeUsers = users.getActiveUsers()
    val activePercentage = (activeUsers.size.toDouble() / users.size) * 100
    println("Total Users: ${users.size}")
    println("Active Users: ${activeUsers.size} (${"%.1f".format(activePercentage)}%)")
    
    // Show users by department
    val departmentCounts = users.departmentCounts()
    println("Users by Department:")
    departmentCounts.forEach { (dept, count) -> 
        println("  $dept: $count users") 
    }
    
    // Demonstrate department analysis
    println("\n🎯 Department Analysis:")
    val departmentStats = analyticsService.analyzeDepartments(users)
    
    // Print department statistics
    departmentStats.values.forEach { 
        println("  ${it.formatSummary()}") 
    }
    
    // Find and display the best performing department
    val bestDepartment = departmentStats.values
        .maxByOrNull { it.averagePerformanceScore }?.departmentName
    println("🏆 Best Performing Department: $bestDepartment")
    
    // Show top performers across all departments
    println("\n🏆 Top Performers (Company-wide):")
    val topPerformers = analyticsService.getTopPerformers(users, 5)
    
    topPerformers.forEachIndexed { index, user ->
        println("${index + 1}. ${user.name} (${user.department}) - Score: ${user.performanceScore}")
    }
    
    // Display activity insights
    println("\n📈 Activity Insights:")
    val insights = analyticsService.generateActivityInsights(users)
    println(insights.formatSummary())
    
    // Demonstrate advanced filtering
    println("\n🔍 Advanced Search Examples:")
    
    // Find high-performing engineers
    val topEngineers = analyticsService.findUsersMatching(
        users = users,
        department = "Engineering",
        minPerformanceScore = 90.0,
        isActive = true
    )
    println("🔧 Top Engineers (90+ score): ${topEngineers.size}")
    
    // Find high-salary recent joiners
    val highSalaryNewbies = users
        .recentJoiners(90) // Last 90 days
        .filter { it.salary > 80000 }
    println("💰 High-salary recent joiners: ${highSalaryNewbies.size}")
    
    // Find productive mid-level employees
    val productiveMidLevel = users
        .filter { it.experienceLevel == "Mid" }
        .withMinProjects(10)
        .getHighPerformers()
    println("📊 Productive mid-level employees: ${productiveMidLevel.size}")
    
    // Demonstrate collection chaining
    println("\n⛓️ Collection Chaining Examples:")
    
    // Complex data transformation pipeline
    val seniorHighPerformers = users
        .getActiveUsers()
        .filter { it.experienceLevel == "Senior" }
        .getHighPerformers()
        .getBySalaryDescending()
        .take(10)
    
    println("👑 Senior high performers (top 10 by salary): ${seniorHighPerformers.size}")
    
    // Multi-step analysis pipeline
    val departmentLeaders = users
        .groupBy { it.department }
        .mapValues { (_, deptUsers) -> deptUsers.maxByOrNull { it.performanceScore } }
        .values
        .filterNotNull()
    
    println("🎖️ Department leaders identified: ${departmentLeaders.size}")
    
    // Show performance trends by experience level
    println("\n📈 Performance Trends:")
    val performanceTrends = analyticsService.getPerformanceTrends(users)
    
    performanceTrends.forEach { (level, avgScore) ->
        println("$level: Average score ${"%.1f".format(avgScore)}")
    }
    
    // Demonstrate salary analysis
    println("\n💰 Salary Analysis:")
    val salaryStats = analyticsService.getSalaryStatsByDepartment(users)
    
    salaryStats.values.forEach { stats ->
        println("  ${stats.formatSummary()}")
    }
    
    // Find departments with high salary variance
    val highVarianceDepts = salaryStats.values
        .filter { it.hasHighVariance() }
        .map { it.department }
    
    if (highVarianceDepts.isNotEmpty()) {
        println("⚠️ Departments with high salary variance: ${highVarianceDepts.joinToString(", ")}")
    }
    
    // Show aggregation examples
    println("\n📊 Aggregation Examples:")
    
    // Calculate company-wide statistics
    val totalPayroll = users.totalSalary()
    val averageAge = users.averageAge()
    val averagePerformance = users.averagePerformanceScore()
    
    println("💵 Total Payroll: $${"%,.0f".format(totalPayroll)}")
    println("👥 Average Age: ${"%.1f".format(averageAge)} years")
    println("⭐ Average Performance: ${"%.1f".format(averagePerformance)}")
    
    // Find extreme values
    val highestPaid = users.highestPaid()
    val topPerformer = users.topPerformer()
    
    println("💎 Highest Paid: ${highestPaid?.name} ($${"%,.0f".format(highestPaid?.salary ?: 0.0)})")
    println("🌟 Top Performer: ${topPerformer?.name} (Score: ${topPerformer?.performanceScore})")
    
    // Demonstrate grouping operations
    println("\n📋 Grouping & Distribution:")
    
    // Show performance distribution
    val performanceDistribution = users.performanceDistribution()
    println("Performance Distribution:")
    performanceDistribution.forEach { (category, count) ->
        println("  $category: $count users")
    }
    
    // Show salary bracket distribution
    val salaryDistribution = users.groupBySalaryBracket()
    println("Salary Bracket Distribution:")
    salaryDistribution.forEach { (bracket, userList) ->
        println("  $bracket: ${userList.size} users")
    }
    
    // Show experience level distribution
    val experienceDistribution = users.groupByExperienceLevel()
    println("Experience Level Distribution:")
    experienceDistribution.forEach { (level, userList) ->
        println("  $level: ${userList.size} users")
    }
    
    // Generate executive summary
    println("\n📋 Executive Summary:")
    val executiveSummary = analyticsService.generateExecutiveSummary(users)
    println(executiveSummary.formatReport())
    
    // Show recommendations
    println("\n💡 Recommendations:")
    val departmentsNeedingAttention = analyticsService.getDepartmentsNeedingAttention(users)
    if (departmentsNeedingAttention.isNotEmpty()) {
        println("🔴 Departments needing attention: ${departmentsNeedingAttention.joinToString(", ")}")
    }
    
    val hiringRecommendations = analyticsService.getHiringRecommendations(users)
    if (hiringRecommendations.isNotEmpty()) {
        println("👥 Hiring recommendations:")
        hiringRecommendations.forEach { (dept, count) ->
            println("  $dept: $count additional hires recommended")
        }
    }
    
    println("\n🎉 Workshop complete! You've mastered Kotlin collections!")
    println("📚 Next: Lesson 3 - OOP + Kotlin Features")
    
    // Bonus demonstrations
    println("\n🌟 Bonus: Advanced Techniques")
    
    // Demonstrate sequence operations for performance
    println("🚀 Performance optimization with sequences:")
    val time1 = System.currentTimeMillis()
    val result1 = users
        .filter { it.isActive }
        .map { it.salary }
        .filter { it > 75000 }
        .take(100)
        .toList()
    val time2 = System.currentTimeMillis()
    
    val time3 = System.currentTimeMillis()
    val result2 = users.asSequence()
        .filter { it.isActive }
        .map { it.salary }
        .filter { it > 75000 }
        .take(100)
        .toList()
    val time4 = System.currentTimeMillis()
    
    println("Regular operations: ${time2 - time1}ms, Sequence operations: ${time4 - time3}ms")
    println("Results identical: ${result1 == result2}")
    
    // Show functional composition
    println("\n🧮 Functional Composition:")
    val isHighPotential: (User) -> Boolean = { user ->
        user.isActive && 
        user.isHighPerformer() && 
        user.yearsOfExperience() >= 2 &&
        user.projectsCompleted > 5
    }
    
    val highPotentialEmployees = users.filter(isHighPotential)
    println("High potential employees: ${highPotentialEmployees.size}")
    
    // Display final statistics
    println("\n📊 Final Statistics Summary:")
    val finalStats = """
        |🎯 Data Processing Complete:
        |   Users Analyzed: ${users.size}
        |   Departments: ${departmentStats.size}
        |   Active Users: ${activeUsers.size} (${"%.1f".format(activePercentage)}%)
        |   Top Performer: ${topPerformers.firstOrNull()?.name ?: "N/A"}
        |   Best Department: ${bestDepartment ?: "N/A"}
        |   
        |💡 Key Insights Discovered: ${executiveSummary.keyInsights.size}
        |📋 Recommendations Generated: ${executiveSummary.recommendations.size}
    """.trimMargin()
    
    println(finalStats)
}