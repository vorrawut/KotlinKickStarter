/**
 * Lesson 2 Workshop: User Data Generator
 * 
 * TODO: Complete this data generator to demonstrate:
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
    
    // TODO: Generate realistic test data
    fun generateUsers(count: Int): List<User> {
        // TODO: Create a list of 'count' users with realistic, varied data
        // Requirements:
        // - Unique IDs (use "USER_${index}")
        // - Random names from the lists above
        // - Realistic ages (22-65)
        // - Random departments
        // - Salaries appropriate for departments (use departmentSalaryRanges)
        // - Performance scores between 50-100 (normal distribution preferred)
        // - Join dates spread over last 5 years
        // - Project counts 0-25 (higher for longer-tenured employees)
        // - 90% active users (randomly distributed)
        // - Valid email addresses based on names
        
        return TODO("Generate $count realistic users with varied, consistent data")
        
        // Hint structure:
        // (1..count).map { index ->
        //     val firstName = firstNames.random()
        //     val lastName = lastNames.random() 
        //     val department = departments.random()
        //     val age = Random.nextInt(22, 66)
        //     // ... generate other fields
        //     User(id = "USER_$index", name = "$firstName $lastName", ...)
        // }
    }
    
    // TODO: Generate users for specific testing scenarios
    fun generateTestScenarios(): List<User> {
        // TODO: Create specific users for testing various edge cases and scenarios
        // Include:
        // - High performers in each department
        // - New joiners (last 30 days) vs veterans (5+ years)
        // - Different salary ranges within departments
        // - Mix of active/inactive users
        // - Users with varying project completion rates
        // - Edge cases: very young/old, very high/low performance, etc.
        
        return TODO("Generate specific test scenario users")
        
        // Consider creating users like:
        // - Top performer in Engineering
        // - Recent hire with high potential
        // - Veteran with declining performance
        // - Inactive high-salary user
        // - etc.
    }
    
    // TODO: Generate users for a specific department
    fun generateDepartmentUsers(department: String, count: Int): List<User> {
        // TODO: Generate users all belonging to the specified department
        // Use appropriate salary ranges and realistic distribution
        
        return TODO("Generate $count users for $department department")
    }
    
    // TODO: Generate users with specific characteristics
    fun generateUsersWithCriteria(
        count: Int,
        department: String? = null,
        minAge: Int? = null,
        maxAge: Int? = null,
        minSalary: Double? = null,
        maxSalary: Double? = null,
        isActive: Boolean? = null
    ): List<User> {
        // TODO: Generate users matching the specified criteria
        // All generated users should meet the given constraints
        
        return TODO("Generate users matching specified criteria")
    }
    
    // TODO: Utility functions for realistic data generation
    
    private fun generateRealisticEmail(firstName: String, lastName: String): String {
        // TODO: Generate realistic email addresses
        // Formats: firstname.lastname@company.com, firstname.lastinitial@company.com, etc.
        
        return TODO("Generate realistic email from name components")
    }
    
    private fun generateRealisticSalary(department: String, experience: Int): Double {
        // TODO: Generate salary based on department and years of experience
        // Use departmentSalaryRanges as base and adjust for experience
        
        return TODO("Generate salary based on department and experience")
    }
    
    private fun generateRealisticPerformanceScore(
        experience: Int,
        projectsCompleted: Int,
        salary: Double,
        departmentAvg: Double = 75.0
    ): Double {
        // TODO: Generate performance score based on multiple factors
        // Consider experience, project completion, and some randomness
        // Should generally correlate with other factors but include variation
        
        return TODO("Generate realistic performance score")
    }
    
    private fun generateJoinDate(maxYearsAgo: Int = 5): LocalDate {
        // TODO: Generate random join date within the last maxYearsAgo years
        // Should be evenly distributed with slight bias toward more recent dates
        
        return TODO("Generate realistic join date")
    }
    
    private fun generateProjectCount(yearsOfExperience: Int, isActive: Boolean): Int {
        // TODO: Generate realistic project count based on experience and activity
        // More experienced and active users should generally have more projects
        
        return TODO("Generate realistic project count")
    }
    
    // TODO: Bulk generation utilities
    
    // Generate a balanced dataset across all departments
    fun generateBalancedDataset(usersPerDepartment: Int = 50): List<User> {
        // TODO: Generate equal numbers of users for each department
        // Ensure good distribution of characteristics within each department
        
        return TODO("Generate balanced dataset with $usersPerDepartment users per department")
    }
    
    // Generate dataset with specific distribution patterns
    fun generateRealisticDistribution(totalUsers: Int): List<User> {
        // TODO: Generate users with realistic organizational distribution:
        // - Engineering: 35% of users
        // - Sales: 25% of users  
        // - Marketing: 15% of users
        // - Operations: 15% of users
        // - Finance: 6% of users
        // - HR: 4% of users
        
        return TODO("Generate users with realistic departmental distribution")
    }
    
    // TODO: Data validation and quality checks
    
    private fun validateUserData(user: User): Boolean {
        // TODO: Validate that generated user data is realistic and consistent
        // Check things like:
        // - Email format is valid
        // - Salary is within reasonable range for department
        // - Performance score is between 0-100
        // - Age is reasonable
        // - Project count makes sense for experience level
        
        return TODO("Validate user data consistency and realism")
    }
    
    // Generate high-quality validated dataset
    fun generateValidatedDataset(count: Int): List<User> {
        // TODO: Generate dataset and validate all users
        // Retry generation for any invalid users
        
        return TODO("Generate and validate $count users")
    }
}