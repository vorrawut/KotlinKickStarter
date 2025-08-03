/**
 * Lesson 11 Complete Solution: Application Tests
 * 
 * Basic tests to verify application startup and context loading
 */

package com.learning.testing

import com.learning.testing.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class TestingApplicationTests {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    fun contextLoads() {
        // Test that Spring context loads with all testing configurations
        assertThat(userRepository).isNotNull
    }
    
    @Test
    fun applicationStartsSuccessfully() {
        // Test that the application starts without errors
        // This test passes if the Spring context loads successfully
    }
}