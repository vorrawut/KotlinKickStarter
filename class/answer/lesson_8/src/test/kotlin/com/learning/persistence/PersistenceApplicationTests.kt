/**
 * Lesson 8 Complete Solution: Application Tests
 * 
 * Basic tests to verify dual database functionality
 */

package com.learning.persistence

import com.learning.persistence.service.TaskService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.assertj.core.api.Assertions.assertThat

@SpringBootTest
@ActiveProfiles("test")
class PersistenceApplicationTests {

    @Autowired
    private lateinit var taskService: TaskService

    @Test
    fun contextLoads() {
        // Test that Spring context loads with both databases configured
        assertThat(taskService).isNotNull
    }
    
    @Test
    fun taskServiceIsInjected() {
        // Verify that the task service is properly injected
        assertThat(taskService).isNotNull
    }
}