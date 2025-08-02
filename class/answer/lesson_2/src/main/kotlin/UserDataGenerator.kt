/**
 * Lesson 2 Complete Solution: User Data Generator
 * 
 * This demonstrates:
 * - Working with ranges and random data
 * - Creating realistic test datasets
 * - Object declaration and functions
 * - Data generation patterns
 */

import java.time.LocalDate
import kotlin.random.Random

object UserDataGenerator {
    
    // Sample data for realistic generation
    private val departments = listOf("Engineering", "Marketing", "Sales", "HR", "Finance", "Operations")
    
    private val firstNames = listOf(
        "Alice", "Bob", "Carol", "David", "Emma", "Frank", "Grace", "Henry", 
        "Ivy", "Jack", "Kate", "Liam", "Maya", "Noah", "Olivia", "Paul",
        "Quinn", "Rachel", "Sam", "Tina", "Uma", "Victor", "Wendy", "Xavier", "Yara", "Zoe"
    )
    
    private val lastNames = listOf(
        "Johnson", "Chen", "Davis", "Wilson", "Brown", "Taylor", "Anderson", "Thomas",
        "Jackson", "White", "Harris", "Martin", "Garcia", "Rodriguez", "Lewis", "Lee",
        "Walker", "Hall", "Allen", "Young", "King", "Wright", "Lopez", "Hill", "Scott", "Green"
    )
    
    // Salary ranges by department (base ranges)
    private val departmentSalaryRanges = mapOf(
        "Engineering" to (70000.0 to 180000.0),
        "Marketing" to (45000.0 to 120000.0),
        "Sales" to (40000.0 to 150000.0),
        "HR" to (50000.0 to 110000.0),
        "Finance" to (55000.0 to 140000.0),
        "Operations" to (45000.0 to 100000.0)
    )
    
    // Generate realistic test data
    fun generateUsers(count: Int): List<User> {
        return (1..count).map { index ->
            val firstName = firstNames.random()
            val lastName = lastNames.random()
            val department = departments.random()
            val age = Random.nextInt(22, 66)
            val joinDate = generateJoinDate()
            val yearsExp = java.time.Period.between(joinDate, LocalDate.now()).years
            val salary = generateRealisticSalary(department, yearsExp)
            val projects = generateProjectCount(yearsExp, Random.nextDouble() < 0.9)
            val performance = generateRealisticPerformanceScore(yearsExp, projects, salary)
            
            User(
                id = "USER_$index",
                name = "$firstName $lastName",
                email = generateRealisticEmail(firstName, lastName),
                age = age,
                department = department,
                salary = salary,
                isActive = Random.nextDouble() < 0.9, // 90% active
                performanceScore = performance,
                joinDate = joinDate,
                projectsCompleted = projects
            )
        }
    }
    
    // Generate users for specific testing scenarios
    fun generateTestScenarios(): List<User> {
        return listOf(
            // High performer in Engineering
            User("TEST_1", "Alice Star", "alice.star@example.com", 32, "Engineering", 150000.0, true, 98.5, LocalDate.of(2019, 1, 15), 25),
            // Recent high-potential hire
            User("TEST_2", "Bob Rising", "bob.rising@example.com", 26, "Marketing", 65000.0, true, 92.0, LocalDate.now().minusDays(45), 3),
            // Veteran with declining performance
            User("TEST_3", "Carol Veteran", "carol.veteran@example.com", 45, "Sales", 95000.0, true, 68.5, LocalDate.of(2015, 6, 10), 35),
            // Inactive high-salary user
            User("TEST_4", "David Inactive", "david.inactive@example.com", 38, "Finance", 110000.0, false, 75.0, LocalDate.of(2020, 3, 20), 12),
            // Low performer
            User("TEST_5", "Emma Struggling", "emma.struggling@example.com", 29, "HR", 55000.0, true, 58.0, LocalDate.of(2022, 1, 5), 2)
        )
    }
    
    // Generate users for a specific department
    fun generateDepartmentUsers(department: String, count: Int): List<User> {
        return (1..count).map { index ->
            val firstName = firstNames.random()
            val lastName = lastNames.random()
            val age = Random.nextInt(22, 66)
            val joinDate = generateJoinDate()
            val yearsExp = java.time.Period.between(joinDate, LocalDate.now()).years
            val salary = generateRealisticSalary(department, yearsExp)
            val projects = generateProjectCount(yearsExp, Random.nextDouble() < 0.9)
            val performance = generateRealisticPerformanceScore(yearsExp, projects, salary)
            
            User(
                id = "${department.uppercase()}_$index",
                name = "$firstName $lastName",
                email = generateRealisticEmail(firstName, lastName),
                age = age,
                department = department,
                salary = salary,
                isActive = Random.nextDouble() < 0.9,
                performanceScore = performance,
                joinDate = joinDate,
                projectsCompleted = projects
            )
        }
    }
    
    // Utility functions for realistic data generation
    
    private fun generateRealisticEmail(firstName: String, lastName: String): String {
        val formats = listOf(
            "$firstName.$lastName@company.com",
            "$firstName.${lastName.first()}@company.com",
            "${firstName.first()}.$lastName@company.com",
            "$firstName$lastName@company.com"
        )
        return formats.random().lowercase()
    }
    
    private fun generateRealisticSalary(department: String, experience: Int): Double {
        val (minSalary, maxSalary) = departmentSalaryRanges[department] ?: (50000.0 to 100000.0)
        val baseRange = maxSalary - minSalary
        val experienceMultiplier = 1.0 + (experience * 0.05) // 5% increase per year
        val randomFactor = 0.8 + Random.nextDouble() * 0.4 // ±20% variation
        
        val salary = minSalary + (baseRange * randomFactor) * experienceMultiplier
        return salary.coerceIn(minSalary, maxSalary * 1.2) // Allow some upward variance
    }
    
    private fun generateRealisticPerformanceScore(
        experience: Int,
        projectsCompleted: Int,
        salary: Double
    ): Double {
        // Base score influenced by experience and projects
        val baseScore = 65.0 + (experience.toDouble() * 2.0) + (projectsCompleted.toDouble() * 0.5)
        // Add some randomness (-10 to +10)
        val randomVariation = (Random.nextDouble() - 0.5) * 20.0
        // Higher salary might correlate with better performance
        val salaryBonus = if (salary > 100000) 5.0 else 0.0
        
        return (baseScore + randomVariation + salaryBonus).coerceIn(50.0, 100.0)
    }
    
    private fun generateJoinDate(maxYearsAgo: Int = 5): LocalDate {
        val daysAgo = Random.nextInt(0, maxYearsAgo * 365)
        return LocalDate.now().minusDays(daysAgo.toLong())
    }
    
    private fun generateProjectCount(yearsOfExperience: Int, isActive: Boolean): Int {
        val baseProjects = yearsOfExperience * 3 // 3 projects per year on average
        val activityMultiplier = if (isActive) 1.0 else 0.5
        val randomVariation = (Random.nextDouble() - 0.5) * 4.0 // -2 to +2 variation
        
        return ((baseProjects.toDouble() * activityMultiplier) + randomVariation).toInt().coerceAtLeast(0)
    }
    
    // Generate a balanced dataset across all departments
    fun generateBalancedDataset(usersPerDepartment: Int = 50): List<User> {
        return departments.flatMap { dept ->
            generateDepartmentUsers(dept, usersPerDepartment)
        }
    }
    
    // Generate dataset with realistic organizational distribution
    fun generateRealisticDistribution(totalUsers: Int): List<User> {
        val distribution = mapOf(
            "Engineering" to 0.35,
            "Sales" to 0.25,
            "Marketing" to 0.15,
            "Operations" to 0.15,
            "Finance" to 0.06,
            "HR" to 0.04
        )
        
        return distribution.flatMap { (dept, percentage) ->
            val deptSize = (totalUsers * percentage).toInt()
            generateDepartmentUsers(dept, deptSize)
        }
    }
    
    // Data validation
    private fun validateUserData(user: User): Boolean {
        return user.email.contains("@") &&
               user.salary > 0 &&
               user.performanceScore in 0.0..100.0 &&
               user.age in 18..70 &&
               user.projectsCompleted >= 0
    }
    
    // Generate high-quality validated dataset
    fun generateValidatedDataset(count: Int): List<User> {
        return generateUsers(count).filter { validateUserData(it) }
    }
}