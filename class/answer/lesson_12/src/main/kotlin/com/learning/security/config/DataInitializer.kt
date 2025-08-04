/**
 * Lesson 12 Complete Solution: Data Initializer
 * 
 * Initializes default data including roles and admin user
 * when the application starts up
 */

package com.learning.security.config

import com.learning.security.model.Role
import com.learning.security.model.User
import com.learning.security.repository.RefreshTokenRepository
import com.learning.security.repository.RoleRepository
import com.learning.security.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
@Transactional
class DataInitializer(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder
) : ApplicationRunner {
    
    private val logger = LoggerFactory.getLogger(DataInitializer::class.java)
    
    @Value("\${security.admin.username:admin}")
    private lateinit var adminUsername: String
    
    @Value("\${security.admin.email:admin@kotlinkickstarter.com}")
    private lateinit var adminEmail: String
    
    @Value("\${security.admin.password:Admin123!}")
    private lateinit var adminPassword: String
    
    @Value("\${security.admin.first-name:System}")
    private lateinit var adminFirstName: String
    
    @Value("\${security.admin.last-name:Administrator}")
    private lateinit var adminLastName: String
    
    @Value("\${security.initialization.enabled:true}")
    private var initializationEnabled: Boolean = true
    
    override fun run(args: ApplicationArguments) {
        if (!initializationEnabled) {
            logger.info("Data initialization is disabled")
            return
        }
        
        logger.info("Starting data initialization...")
        
        try {
            createDefaultRoles()
            createDefaultUsers()
            cleanupExpiredTokens()
            
            logger.info("Data initialization completed successfully")
            
        } catch (e: Exception) {
            logger.error("Data initialization failed", e)
            throw e
        }
    }
    
    /**
     * Create default roles if they don't exist
     */
    private fun createDefaultRoles() {
        logger.info("Initializing default roles...")
        
        val defaultRoles = listOf(
            Role.createUserRole(),
            Role.createAdminRole(),
            Role.createModeratorRole()
        )
        
        defaultRoles.forEach { role ->
            if (!roleRepository.existsByName(role.name)) {
                val savedRole = roleRepository.save(role)
                logger.info("Created role: ${savedRole.name} - ${savedRole.description}")
            } else {
                logger.debug("Role already exists: ${role.name}")
            }
        }
        
        logger.info("Default roles initialization completed")
    }
    
    /**
     * Create default users if they don't exist
     */
    private fun createDefaultUsers() {
        logger.info("Initializing default users...")
        
        createAdminUser()
        createTestUsers()
        
        logger.info("Default users initialization completed")
    }
    
    /**
     * Create admin user if it doesn't exist
     */
    private fun createAdminUser() {
        if (userRepository.existsByUsername(adminUsername) || userRepository.existsByEmail(adminEmail)) {
            logger.info("Admin user already exists: $adminUsername")
            return
        }
        
        val adminRole = roleRepository.findByName(Role.ROLE_ADMIN)
            ?: throw IllegalStateException("Admin role not found. Roles must be created first.")
        
        val userRole = roleRepository.findByName(Role.ROLE_USER)
            ?: throw IllegalStateException("User role not found. Roles must be created first.")
        
        val adminUser = User(
            username = adminUsername,
            email = adminEmail,
            password = passwordEncoder.encode(adminPassword),
            firstName = adminFirstName,
            lastName = adminLastName,
            roles = setOf(adminRole, userRole), // Admin also has user role
            isActive = true,
            isEnabled = true,
            isLocked = false,
            isCredentialsExpired = false
        )
        
        val savedAdmin = userRepository.save(adminUser)
        logger.warn("Created admin user: {} (ID: {}) - Password: {}", 
            savedAdmin.username, savedAdmin.id, adminPassword)
        logger.warn("SECURITY WARNING: Please change the default admin password immediately!")
    }
    
    /**
     * Create test users for development/demo purposes
     */
    private fun createTestUsers() {
        if (isProductionEnvironment()) {
            logger.info("Skipping test user creation in production environment")
            return
        }
        
        logger.info("Creating test users for development...")
        
        val userRole = roleRepository.findByName(Role.ROLE_USER)
            ?: return
        
        val moderatorRole = roleRepository.findByName(Role.ROLE_MODERATOR)
            ?: return
        
        val testUsers = listOf(
            TestUserData("testuser", "user@test.com", "User123!", "Test", "User", setOf(userRole)),
            TestUserData("moderator", "moderator@test.com", "Mod123!", "Test", "Moderator", setOf(userRole, moderatorRole)),
            TestUserData("john.doe", "john.doe@example.com", "John123!", "John", "Doe", setOf(userRole)),
            TestUserData("jane.smith", "jane.smith@example.com", "Jane123!", "Jane", "Smith", setOf(userRole))
        )
        
        testUsers.forEach { testUserData ->
            if (!userRepository.existsByUsername(testUserData.username) && 
                !userRepository.existsByEmail(testUserData.email)) {
                
                val user = User(
                    username = testUserData.username,
                    email = testUserData.email,
                    password = passwordEncoder.encode(testUserData.password),
                    firstName = testUserData.firstName,
                    lastName = testUserData.lastName,
                    roles = testUserData.roles,
                    isActive = true,
                    isEnabled = true
                )
                
                val savedUser = userRepository.save(user)
                logger.info("Created test user: {} ({})", savedUser.username, savedUser.email)
            }
        }
        
        logger.info("Test users creation completed")
    }
    
    /**
     * Clean up expired refresh tokens
     */
    private fun cleanupExpiredTokens() {
        logger.info("Cleaning up expired refresh tokens...")
        
        try {
            val cutoffDate = LocalDateTime.now().minusDays(7) // Remove tokens older than 7 days
            val deletedCount = refreshTokenRepository.deleteExpiredTokens(cutoffDate)
            
            if (deletedCount > 0) {
                logger.info("Cleaned up {} expired refresh tokens", deletedCount)
            } else {
                logger.debug("No expired refresh tokens to clean up")
            }
            
        } catch (e: Exception) {
            logger.warn("Failed to clean up expired tokens", e)
        }
    }
    
    /**
     * Display initialization summary
     */
    private fun displayInitializationSummary() {
        logger.info("=== Security Initialization Summary ===")
        logger.info("Total users: {}", userRepository.count())
        logger.info("Active users: {}", userRepository.countActiveUsers())
        logger.info("Total roles: {}", roleRepository.count())
        
        // Display role distribution
        roleRepository.findAll().forEach { role ->
            val userCount = userRepository.countByRoleName(role.name)
            logger.info("Role '{}': {} users", role.name, userCount)
        }
        
        logger.info("Refresh tokens: {}", refreshTokenRepository.count())
        logger.info("=======================================")
    }
    
    /**
     * Check if we're running in a production environment
     */
    private fun isProductionEnvironment(): Boolean {
        val profiles = System.getProperty("spring.profiles.active") ?: ""
        return profiles.contains("prod") || profiles.contains("production")
    }
    
    /**
     * Data class for test user information
     */
    private data class TestUserData(
        val username: String,
        val email: String,
        val password: String,
        val firstName: String,
        val lastName: String,
        val roles: Set<Role>
    )
}

/**
 * Configuration for data initialization settings
 */
@Component
class DataInitializationConfig {
    
    private val logger = LoggerFactory.getLogger(DataInitializationConfig::class.java)
    
    @Value("\${security.initialization.create-test-data:true}")
    private var createTestData: Boolean = true
    
    @Value("\${security.initialization.cleanup-on-startup:true}")
    private var cleanupOnStartup: Boolean = true
    
    fun shouldCreateTestData(): Boolean = createTestData
    
    fun shouldCleanupOnStartup(): Boolean = cleanupOnStartup
}