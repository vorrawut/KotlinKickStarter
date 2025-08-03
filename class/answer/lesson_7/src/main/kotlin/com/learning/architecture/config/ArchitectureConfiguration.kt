/**
 * Lesson 7 Complete Solution: Clean Architecture Configuration
 */

package com.learning.architecture.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableTransactionManagement
class ArchitectureConfiguration {
    
    @Bean
    @ConfigurationProperties(prefix = "architecture")
    fun architectureProperties(): ArchitectureProperties {
        return ArchitectureProperties()
    }
    
    @Bean
    @Profile("dev")
    fun developmentConfiguration(): DevelopmentConfig {
        return DevelopmentConfig()
    }
    
    @Bean
    @Profile("prod")
    fun productionConfiguration(): ProductionConfig {
        return ProductionConfig()
    }
}

@ConfigurationProperties(prefix = "architecture")
data class ArchitectureProperties(
    val service: ServiceProperties = ServiceProperties(),
    val repository: RepositoryProperties = RepositoryProperties(),
    val validation: ValidationProperties = ValidationProperties()
)

data class ServiceProperties(
    val cacheEnabled: Boolean = true,
    val batchSize: Int = 100,
    val timeoutSeconds: Int = 30,
    val enableAsyncProcessing: Boolean = false,
    val maxConcurrentOperations: Int = 10
)

data class RepositoryProperties(
    val enableAuditing: Boolean = true,
    val defaultPageSize: Int = 20,
    val maxPageSize: Int = 100,
    val enableQueryLogging: Boolean = false,
    val connectionPoolSize: Int = 10
)

data class ValidationProperties(
    val strictMode: Boolean = true,
    val enableCrossFieldValidation: Boolean = true,
    val enableBusinessRuleValidation: Boolean = true,
    val validationCacheSize: Int = 1000
)

data class DevelopmentConfig(
    val enableDebugLogging: Boolean = true,
    val enableH2Console: Boolean = true,
    val enableTestData: Boolean = true,
    val enableMockServices: Boolean = false
)

data class ProductionConfig(
    val enableMetrics: Boolean = true,
    val enableHealthChecks: Boolean = true,
    val enableSecurityHeaders: Boolean = true,
    val enableRateLimiting: Boolean = true
)