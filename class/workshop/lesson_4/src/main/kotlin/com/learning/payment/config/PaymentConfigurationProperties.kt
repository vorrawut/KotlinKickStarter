/**
 * Lesson 4 Workshop: Configuration Properties
 * 
 * TODO: Complete this configuration properties class
 * This demonstrates:
 * - @ConfigurationProperties for type-safe configuration
 * - @ConstructorBinding for immutable configuration
 * - Nested configuration classes
 */

package com.learning.payment.config

import org.springframework.boot.context.properties.ConfigurationProperties

// TODO: Add @ConfigurationProperties annotation
// Bind to "payment" prefix in application.yml
// Note: @ConstructorBinding is no longer needed in Spring Boot 3.x
data class PaymentConfigurationProperties(
    val processors: ProcessorConfig,
    val audit: AuditConfig,
    val retry: RetryConfig
)

data class ProcessorConfig(
    val creditCard: ProcessorSettings,
    val bankTransfer: ProcessorSettings,
    val digitalWallet: ProcessorSettings
)

data class ProcessorSettings(
    // TODO: Add properties for processor configuration
    // - enabled: Boolean
    // - feeRate: Double
    // - maxAmount: Double
    
    val enabled: Boolean = TODO("Default enabled value"),
    val feeRate: Double = TODO("Default fee rate"),
    val maxAmount: Double = TODO("Default max amount")
)

data class AuditConfig(
    // TODO: Add audit configuration properties
    // - enabled: Boolean
    // - complianceMode: Boolean
    
    val enabled: Boolean = TODO("Default audit enabled"),
    val complianceMode: Boolean = TODO("Default compliance mode")
)

data class RetryConfig(
    // TODO: Add retry configuration properties
    // - maxAttempts: Int
    // - delayMs: Long
    
    val maxAttempts: Int = TODO("Default max attempts"),
    val delayMs: Long = TODO("Default delay in milliseconds")
)