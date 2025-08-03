/**
 * Lesson 9 Complete Solution: Auditing Configuration
 * 
 * Complete configuration for JPA auditing with automatic tracking
 */

package com.learning.crud.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import java.util.*

@Configuration
@EnableJpaAuditing
class AuditingConfiguration {
    
    @Bean
    fun auditorProvider(): AuditorAware<Long> {
        return AuditorAware {
            // In a real application, get current user from SecurityContext
            // For demonstration purposes, return a fixed user ID
            Optional.of(1L)
        }
    }
}