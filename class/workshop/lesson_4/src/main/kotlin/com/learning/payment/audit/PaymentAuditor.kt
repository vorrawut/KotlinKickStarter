/**
 * Lesson 4 Workshop: Payment Auditor (Spring Component)
 * 
 * TODO: Complete this Spring component
 * This demonstrates:
 * - @Component annotation
 * - Implementation of interface
 * - Logging integration
 */

package com.learning.payment.audit

import com.learning.payment.model.PaymentMethod
import com.learning.payment.model.PaymentResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.format.DateTimeFormatter

// TODO: Add @Component annotation
class PaymentAuditor : Auditable {
    
    private val logger = LoggerFactory.getLogger(PaymentAuditor::class.java)
    
    override fun auditPaymentAttempt(method: PaymentMethod, amount: Double) {
        val methodType = when (method) {
            is PaymentMethod.CreditCard -> TODO("Create method type description for credit card")
            is PaymentMethod.BankAccount -> TODO("Create method type description for bank account")
            is PaymentMethod.DigitalWallet -> TODO("Create method type description for digital wallet")
        }
        
        // TODO: Use logger.info to log the payment attempt
        // Include: methodType, amount, methodId
        TODO("Log payment attempt with structured information")
    }
    
    override fun auditPaymentResult(result: PaymentResult) {
        when (result) {
            is PaymentResult.Success -> {
                // TODO: Log successful payment with transaction details
                TODO("Log success: transactionId, amount, fee, total")
            }
            is PaymentResult.Failed -> {
                // TODO: Log failed payment with error details
                TODO("Log failure: errorCode, errorMessage, amount, retryable status")
            }
            is PaymentResult.Pending -> {
                // TODO: Log pending payment
                TODO("Log pending: transactionId, amount, estimated completion")
            }
            is PaymentResult.Cancelled -> {
                // TODO: Log cancelled payment
                TODO("Log cancellation: reason, amount")
            }
        }
    }
    
    override fun auditSecurityEvent(event: String, details: Map<String, Any>) {
        // TODO: Log security event using logger.warn
        TODO("Log security event with details map")
    }
    
    private fun formatAuditEntry(level: String, message: String, details: Any? = null): String {
        return TODO("Format audit entry with timestamp, level, message, and details")
    }
}