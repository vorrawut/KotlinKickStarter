/**
 * Lesson 4 Workshop: Main Configuration Class
 * 
 * TODO: Complete this configuration class
 * This demonstrates:
 * - @Configuration annotation
 * - @EnableConfigurationProperties
 * - @Bean definitions
 * - Conditional configuration
 */

package com.learning.payment.config

import com.learning.payment.processor.PaymentProcessor
import com.learning.payment.processor.CreditCardProcessor
import com.learning.payment.processor.BankTransferProcessor
import com.learning.payment.processor.DigitalWalletProcessor
import com.learning.payment.audit.Auditable
import com.learning.payment.audit.PaymentAuditor
import com.learning.payment.audit.ComplianceAuditor
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

// TODO: Add @Configuration annotation
// TODO: Add @EnableConfigurationProperties(PaymentConfigurationProperties::class)
class PaymentConfiguration(
    private val paymentConfig: PaymentConfigurationProperties
) {
    
    // TODO: Create bean for CreditCardProcessor
    // Add @Bean annotation
    // Use configuration properties to set fee rate and max amount
    fun creditCardProcessor(): PaymentProcessor {
        return TODO("Create CreditCardProcessor with configuration")
    }
    
    // TODO: Create bean for BankTransferProcessor
    // Add @Bean annotation
    fun bankTransferProcessor(): PaymentProcessor {
        return TODO("Create BankTransferProcessor with configuration")
    }
    
    // TODO: Create bean for DigitalWalletProcessor
    // Add @Bean annotation
    fun digitalWalletProcessor(): PaymentProcessor {
        return TODO("Create DigitalWalletProcessor with configuration")
    }
    
    // TODO: Create bean for PaymentAuditor (default)
    // Add @Bean annotation
    // Add @Primary annotation to make it the default
    // Add @Profile("!production") to exclude in production
    fun paymentAuditor(): Auditable {
        return TODO("Create PaymentAuditor")
    }
    
    // TODO: Create bean for ComplianceAuditor (production)
    // Add @Bean annotation
    // Add @Profile("production") to only include in production
    fun complianceAuditor(): Auditable {
        return TODO("Create ComplianceAuditor")
    }
    
    // TODO: Create bean for processor map
    // Add @Bean annotation
    // This will be used by PaymentService
    fun paymentProcessors(): Map<String, PaymentProcessor> {
        return TODO("Create map of payment processors using the beans above")
        
        // Hint: You can inject the processor beans as parameters
        // fun paymentProcessors(
        //     creditCardProcessor: PaymentProcessor,
        //     bankTransferProcessor: PaymentProcessor,
        //     digitalWalletProcessor: PaymentProcessor
        // ): Map<String, PaymentProcessor>
    }
}