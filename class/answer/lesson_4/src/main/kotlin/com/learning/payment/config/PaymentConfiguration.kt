/**
 * Lesson 4 Complete Solution: Main Configuration Class
 * 
 * This demonstrates:
 * - @Configuration annotation for bean definitions
 * - @EnableConfigurationProperties for property binding
 * - @Bean definitions with dependency injection
 * - @Primary and @Profile annotations for conditional beans
 * - Constructor injection in configuration classes
 */

package com.learning.payment.config

import com.learning.payment.audit.Auditable
import com.learning.payment.audit.ComplianceAuditor
import com.learning.payment.audit.PaymentAuditor
import com.learning.payment.processor.*
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
@EnableConfigurationProperties(PaymentConfigurationProperties::class)
class PaymentConfiguration {
    
    @Bean
    fun creditCardProcessor(paymentConfig: PaymentConfigurationProperties): PaymentProcessor {
        return CreditCardProcessor(paymentConfig)
    }
    
    @Bean
    fun bankTransferProcessor(paymentConfig: PaymentConfigurationProperties): PaymentProcessor {
        return BankTransferProcessor(paymentConfig)
    }
    
    @Bean
    fun digitalWalletProcessor(paymentConfig: PaymentConfigurationProperties): PaymentProcessor {
        return DigitalWalletProcessor(paymentConfig)
    }
    
    @Bean
    @Primary
    @Profile("!production")
    fun paymentAuditor(): Auditable {
        return PaymentAuditor()
    }
    
    @Bean
    @Profile("production")
    fun complianceAuditor(): Auditable {
        return ComplianceAuditor()
    }
    
    @Bean
    fun paymentProcessors(
        creditCardProcessor: PaymentProcessor,
        bankTransferProcessor: PaymentProcessor,
        digitalWalletProcessor: PaymentProcessor
    ): Map<String, PaymentProcessor> {
        return mapOf(
            "CREDIT_CARD" to creditCardProcessor,
            "BANK_TRANSFER" to bankTransferProcessor,
            "DIGITAL_WALLET" to digitalWalletProcessor
        )
    }
}