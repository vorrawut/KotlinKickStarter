/**
 * Lesson 4 Complete Solution: Configuration Properties
 * 
 * This demonstrates:
 * - @ConfigurationProperties for type-safe configuration
 * - @ConstructorBinding for immutable configuration
 * - Nested configuration classes
 * - Default values and validation
 */

package com.learning.payment.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("payment")
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
    val enabled: Boolean = true,
    val feeRate: Double = 0.0,
    val maxAmount: Double = 10000.0
)

data class AuditConfig(
    val enabled: Boolean = true,
    val complianceMode: Boolean = false
)

data class RetryConfig(
    val maxAttempts: Int = 3,
    val delayMs: Long = 1000L
)