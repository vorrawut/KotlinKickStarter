/**
 * Lesson 12 Complete Solution: Basic Application Tests
 * 
 * Basic tests to ensure the security application starts correctly
 * and core components are properly configured
 */

package com.learning.security

import com.learning.security.repository.RoleRepository
import com.learning.security.repository.UserRepository
import com.learning.security.service.AuthService
import com.learning.security.service.JwtService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
@ActiveProfiles("test")
class SecurityApplicationTests {
    
    @Autowired
    private lateinit var authService: AuthService
    
    @Autowired
    private lateinit var jwtService: JwtService
    
    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder
    
    @Autowired
    private lateinit var userRepository: UserRepository
    
    @Autowired
    private lateinit var roleRepository: RoleRepository
    
    @Test
    fun contextLoads() {
        // Test that Spring context loads successfully
        assertNotNull(authService)
        assertNotNull(jwtService)
        assertNotNull(passwordEncoder)
        assertNotNull(userRepository)
        assertNotNull(roleRepository)
    }
    
    @Test
    fun passwordEncoderWorks() {
        val rawPassword = "TestPassword123!"
        val encodedPassword = passwordEncoder.encode(rawPassword)
        
        assertNotNull(encodedPassword)
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword))
    }
    
    @Test
    fun defaultRolesAreCreated() {
        val userRole = roleRepository.findByName("USER")
        val adminRole = roleRepository.findByName("ADMIN")
        
        assertNotNull(userRole)
        assertNotNull(adminRole)
    }
    
    @Test
    fun adminUserIsCreated() {
        val adminUser = userRepository.findByUsername("admin")
        assertNotNull(adminUser)
        assertTrue(adminUser!!.hasRole("ADMIN"))
    }
}