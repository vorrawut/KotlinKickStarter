/**
 * Lesson 7 Complete Solution: Clean Architecture Application Tests
 */

package com.learning.architecture

import com.learning.architecture.service.TaskService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.assertj.core.api.Assertions.assertThat

@SpringBootTest
@ActiveProfiles("test")
class ArchitectureApplicationTests {

    @Autowired
    private lateinit var taskService: TaskService

    @Test
    fun contextLoads() {
        // Test that Spring context loads successfully with clean architecture configuration
    }
    
    @Test
    fun serviceLayerIntegrationTest() {
        // Test service layer integration and dependency injection
        val summary = taskService.getTaskSummary()
        assertThat(summary).isNotNull
        assertThat(summary.totalTasks).isGreaterThanOrEqualTo(0)
    }
}