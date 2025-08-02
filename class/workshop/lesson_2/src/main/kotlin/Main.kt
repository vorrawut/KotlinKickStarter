/**
 * Lesson 2 Workshop: Main Application
 * 
 * TODO: Complete this main application to demonstrate:
 * - Using collection operations in real scenarios
 * - Chaining functional operations
 * - Data processing pipelines
 * - Analytics and reporting
 */

fun main() {
    println("=== User Analytics Dashboard ===")
    println("🚀 Welcome to Lesson 2: Collections & Functional Programming!\n")
    
    val analyticsService = UserAnalyticsService()
    
    // TODO: Generate and display sample data
    println("📊 Generating sample data...")
    val users = UserDataGenerator.generateUsers(1000)
    println("✓ Generated ${users.size} users")
    println("✓ Departments: ${users.getUniqueDepartments().joinToString(", ")}\n")
    
    // TODO: Demonstrate basic collection operations
    println("🔍 Basic Collection Operations:")
    
    // TODO: Show total users and active users
    val activeUsers = users.getActiveUsers()
    println("Total Users: ${users.size}")
    println("Active Users: ${activeUsers.size} (${TODO("Calculate percentage")}%)")
    
    // TODO: Show users by department
    val departmentCounts = users.departmentCounts()
    println("Users by Department:")
    // TODO: Print each department and its count
    // Hint: departmentCounts.forEach { (dept, count) -> println("  $dept: $count") }
    
    // TODO: Demonstrate department analysis
    println("\n🎯 Department Analysis:")
    val departmentStats = analyticsService.analyzeDepartments(users)
    
    // TODO: Print department statistics
    // Show summary for each department
    // Hint: departmentStats.values.forEach { println("  ${it.formatSummary()}") }
    
    // TODO: Find and display the best performing department
    val bestDepartment = TODO("Find department with highest average performance score")
    println("🏆 Best Performing Department: $bestDepartment")
    
    // TODO: Show top performers across all departments
    println("\n🏆 Top Performers (Company-wide):")
    val topPerformers = analyticsService.getTopPerformers(users, 5)
    
    // TODO: Print top performers with ranking
    // Format: "1. Alice Johnson (Engineering) - Score: 95.5"
    // Hint: topPerformers.forEachIndexed { index, user -> ... }
    
    // TODO: Display activity insights
    println("\n📈 Activity Insights:")
    val insights = analyticsService.generateActivityInsights(users)
    // TODO: Print the insights summary
    // Use the formatSummary() function on ActivityInsights
    
    // TODO: Demonstrate advanced filtering
    println("\n🔍 Advanced Search Examples:")
    
    // TODO: Find high-performing engineers
    val topEngineers = analyticsService.findUsersMatching(
        users = users,
        department = "Engineering",
        minPerformanceScore = 90.0,
        isActive = true
    )
    println("🔧 Top Engineers (90+ score): ${topEngineers.size}")
    
    // TODO: Find high-salary recent joiners
    val highSalaryNewbies = users
        .recentJoiners(90) // Last 90 days
        .filter { it.salary > 80000 }
    println("💰 High-salary recent joiners: ${highSalaryNewbies.size}")
    
    // TODO: Find productive mid-level employees
    val productiveMidLevel = users
        .filter { it.experienceLevel == "Mid" }
        .withMinProjects(10)
        .getHighPerformers()
    println("📊 Productive mid-level employees: ${productiveMidLevel.size}")
    
    // TODO: Demonstrate collection chaining
    println("\n⛓️ Collection Chaining Examples:")
    
    // TODO: Complex data transformation pipeline
    val seniorHighPerformers = users
        .getActiveUsers()
        .filter { it.experienceLevel == "Senior" }
        .getHighPerformers()
        .getBySalaryDescending()
        .take(10)
    
    println("👑 Senior high performers (top 10 by salary): ${seniorHighPerformers.size}")
    
    // TODO: Multi-step analysis pipeline
    val departmentLeaders = TODO("Find the top performer in each department")
    // Hint: Group by department, then get top performer from each group
    println("🎖️ Department leaders identified: ${departmentLeaders.size}")
    
    // TODO: Show performance trends by experience level
    println("\n📈 Performance Trends:")
    val performanceTrends = analyticsService.getPerformanceTrends(users)
    
    // TODO: Print performance by experience level
    // Format: "Junior: Average score 72.5"
    // Hint: performanceTrends.forEach { (level, avgScore) -> ... }
    
    // TODO: Demonstrate salary analysis
    println("\n💰 Salary Analysis:")
    val salaryStats = analyticsService.getSalaryStatsByDepartment(users)
    
    // TODO: Print salary statistics for each department
    // Use the formatSummary() function on SalaryStats
    
    // TODO: Find departments with high salary variance
    val highVarianceDepts = TODO("Find departments with high salary variance")
    if (highVarianceDepts.isNotEmpty()) {
        println("⚠️ Departments with high salary variance: ${highVarianceDepts.joinToString(", ")}")
    }
    
    // TODO: Show aggregation examples
    println("\n📊 Aggregation Examples:")
    
    // TODO: Calculate company-wide statistics
    val totalPayroll = users.totalSalary()
    val averageAge = users.averageAge()
    val averagePerformance = users.averagePerformanceScore()
    
    println("💵 Total Payroll: $${TODO("Format totalPayroll as currency")}")
    println("👥 Average Age: ${TODO("Format averageAge to 1 decimal")} years")
    println("⭐ Average Performance: ${TODO("Format averagePerformance to 1 decimal")}")
    
    // TODO: Find extreme values
    val highestPaid = users.highestPaid()
    val topPerformer = users.topPerformer()
    
    println("💎 Highest Paid: ${highestPaid?.name} ($${highestPaid?.salary})")
    println("🌟 Top Performer: ${topPerformer?.name} (Score: ${topPerformer?.performanceScore})")
    
    // TODO: Demonstrate grouping operations
    println("\n📋 Grouping & Distribution:")
    
    // TODO: Show performance distribution
    val performanceDistribution = users.performanceDistribution()
    println("Performance Distribution:")
    // TODO: Print each category and count
    
    // TODO: Show salary bracket distribution
    val salaryDistribution = users.groupBySalaryBracket()
    println("Salary Bracket Distribution:")
    salaryDistribution.forEach { (bracket, userList) ->
        println("  $bracket: ${userList.size} users")
    }
    
    // TODO: Show experience level distribution
    val experienceDistribution = users.groupByExperienceLevel()
    println("Experience Level Distribution:")
    // TODO: Print each level and count
    
    // TODO: Generate executive summary
    println("\n📋 Executive Summary:")
    val executiveSummary = analyticsService.generateExecutiveSummary(users)
    // TODO: Print the executive summary report
    
    // TODO: Show recommendations
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
    
    // TODO: Bonus demonstrations
    println("\n🌟 Bonus: Advanced Techniques")
    
    // TODO: Demonstrate sequence operations for performance
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
    
    // TODO: Show functional composition
    println("\n🧮 Functional Composition:")
    val isHighPotential: (User) -> Boolean = { user ->
        user.isActive && 
        user.isHighPerformer() && 
        user.yearsOfExperience() >= 2 &&
        user.projectsCompleted > 5
    }
    
    val highPotentialEmployees = users.filter(isHighPotential)
    println("High potential employees: ${highPotentialEmployees.size}")
    
    // TODO: Display final statistics
    println("\n📊 Final Statistics Summary:")
    val finalStats = """
        |🎯 Data Processing Complete:
        |   Users Analyzed: ${users.size}
        |   Departments: ${departmentStats.size}
        |   Active Users: ${activeUsers.size} (${String.format("%.1f", activeUsers.size.toDouble() / users.size * 100)}%)
        |   Top Performer: ${topPerformers.firstOrNull()?.name ?: "N/A"}
        |   Best Department: ${bestDepartment ?: "N/A"}
        |   
        |💡 Key Insights Discovered: ${executiveSummary.keyInsights.size}
        |📋 Recommendations Generated: ${executiveSummary.recommendations.size}
    """.trimMargin()
    
    println(finalStats)
}