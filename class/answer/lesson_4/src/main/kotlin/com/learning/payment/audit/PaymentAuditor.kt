/**
 * Lesson 4 Complete Solution: Payment Auditor (Spring Component)
 * 
 * This demonstrates:
 * - @Component annotation for Spring-managed bean
 * - SLF4J logging integration
 * - Structured logging with parameters
 * - Professional audit trail implementation
 */

package com.learning.payment.audit

import com.learning.payment.model.PaymentMethod
import com.learning.payment.model.PaymentResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.format.DateTimeFormatter

@Component
class PaymentAuditor : Auditable {
    
    private val logger = LoggerFactory.getLogger(PaymentAuditor::class.java)
    
    override fun auditPaymentAttempt(method: PaymentMethod, amount: Double) {
        val methodType = when (method) {
            is PaymentMethod.CreditCard -> "Credit Card (${method.cardType.displayName})"
            is PaymentMethod.BankAccount -> "Bank Account (${method.accountType.displayName})"
            is PaymentMethod.DigitalWallet -> "Digital Wallet (${method.walletType.displayName})"
        }
        
        logger.info(
            "Payment attempt: method={}, amount={}, methodId={}",
            methodType, amount, method.id
        )
    }
    
    override fun auditPaymentResult(result: PaymentResult) {
        when (result) {
            is PaymentResult.Success -> {
                logger.info(
                    "Payment successful: transactionId={}, amount={}, fee={}, total={}",
                    result.transactionId, result.amount, result.fee, result.total
                )
            }
            is PaymentResult.Failed -> {
                logger.warn(
                    "Payment failed: errorCode={}, errorMessage={}, amount={}, retryable={}",
                    result.errorCode, result.errorMessage, result.amount, result.isRetryable()
                )
            }
            is PaymentResult.Pending -> {
                logger.info(
                    "Payment pending: transactionId={}, amount={}, estimatedCompletion={}",
                    result.transactionId, result.amount, result.estimatedCompletionTime
                )
            }
            is PaymentResult.Cancelled -> {
                logger.warn(
                    "Payment cancelled: reason={}, amount={}",
                    result.reason, result.amount
                )
            }
        }
    }
    
    override fun auditSecurityEvent(event: String, details: Map<String, Any>) {
        logger.warn(
            "Security event: event={}, details={}",
            event, details
        )
    }
    
    private fun formatAuditEntry(level: String, message: String, details: Any? = null): String {
        val timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        val detailsStr = if (details != null) " | Details: $details" else ""
        return "[$timestamp] [$level] AUDIT: $message$detailsStr"
    }
}