/**
 * Lesson 8 Complete Solution: Database Configuration
 * 
 * Enables both JPA and MongoDB repositories with proper configuration
 */

package com.learning.persistence.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = ["com.learning.persistence.repository.jpa"],
    entityManagerFactoryRef = "entityManagerFactory",
    transactionManagerRef = "transactionManager"
)
@EnableMongoRepositories(
    basePackages = ["com.learning.persistence.repository.mongo"]
)
@EnableMongoAuditing
class DatabaseConfiguration {
    
    // Additional configuration beans can be added here if needed
    // For example:
    // - Custom transaction managers
    // - Connection pool configurations
    // - MongoDB template customizations
}