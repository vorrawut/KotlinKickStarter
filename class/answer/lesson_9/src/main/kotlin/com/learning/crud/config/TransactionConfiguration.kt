/**
 * Lesson 9 Complete Solution: Transaction Configuration
 * 
 * Complete configuration for transaction management with custom settings
 */

package com.learning.crud.config

import org.springframework.context.annotation.Configuration
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableTransactionManagement
class TransactionConfiguration {
    
    // Spring Boot auto-configuration handles most transaction setup
    // This class can be extended with custom transaction managers,
    // interceptors, or advisors if needed
    
    // Example of custom transaction configuration:
    /*
    @Bean
    fun transactionManager(entityManagerFactory: EntityManagerFactory): PlatformTransactionManager {
        val transactionManager = JpaTransactionManager()
        transactionManager.entityManagerFactory = entityManagerFactory
        transactionManager.setDefaultTimeout(300) // 5 minutes
        return transactionManager
    }
    */
}