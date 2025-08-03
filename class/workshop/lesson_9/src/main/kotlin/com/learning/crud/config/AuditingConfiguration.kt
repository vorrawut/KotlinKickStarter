/**
 * Lesson 9 Workshop: Auditing Configuration
 * 
 * TODO: Configure JPA auditing for automatic tracking of entity changes
 * This class should enable JPA auditing and provide current user information
 */

package com.learning.crud.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
// TODO: Import necessary Spring Data JPA auditing annotations
// TODO: Import @EnableJpaAuditing
// TODO: Import AuditorAware

// TODO: Add @Configuration annotation
// TODO: Add @EnableJpaAuditing annotation
class AuditingConfiguration {
    
    // TODO: Configure AuditorAware bean for tracking current user
    // This bean should return the current user ID for audit fields
    @Bean
    fun auditorProvider(): AuditorAware<Long> {
        // TODO: Return AuditorAware implementation
        // For workshop purposes, you can return a fixed user ID
        // In real applications, get this from SecurityContext
        
        // Hint: return AuditorAware { Optional.of(1L) } // Fixed user for workshop
        TODO("Implement auditorProvider")
    }
    
    // TODO: Configure date/time provider if custom logic is needed
    // Spring Boot auto-configuration handles this by default
    
}