/**
 * Lesson 6 Complete Solution: Application Tests
 */

package com.learning.validation

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class ValidationApplicationTests {

    @Test
    fun contextLoads() {
        // Test that Spring context loads successfully with validation configuration
    }
}