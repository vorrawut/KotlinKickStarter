/**
 * Lesson 4 Complete Solution: Compliance Auditor (Production Profile)
 * 
 * This demonstrates:
 * - @Component with @Profile annotation for environment-specific beans
 * - Enhanced compliance features for production
 * - Data collection and reporting capabilities
 * - Advanced logging and monitoring
 */

package com.learning.payment.audit

import com.learning.payment.model.PaymentMethod
import com.learning.payment.model.PaymentResult
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("production")
class ComplianceAuditor : Auditable {
    
    private val logger = LoggerFactory.getLogger(ComplianceAuditor::class.java)
    private val complianceEvents = mutableListOf<ComplianceEvent>()
    
    override fun auditPaymentAttempt(method: PaymentMethod, amount: Double) {
        // Check for compliance requirements
        val flags = mutableListOf<String>()
        
        // Flag large transactions (>$10,000)
        if (amount > 10000.0) {
            flags.add("LARGE_TRANSACTION")
        }
        
        // Flag international payments (simplified check)
        if (method is PaymentMethod.CreditCard && method.cardType.name == "AMEX") {
            flags.add("POTENTIAL_INTERNATIONAL")
        }
        
        // Flag high-risk wallets
        if (method is PaymentMethod.DigitalWallet && method.walletType.name == "VENMO" && amount > 500) {
            flags.add("HIGH_RISK_WALLET")
        }
        
        val event = ComplianceEvent(
            type = "PAYMENT_ATTEMPT",
            amount = amount,
            flags = flags,
            methodType = method::class.simpleName ?: "Unknown",
            timestamp = System.currentTimeMillis()
        )
        
        complianceEvents.add(event)
        
        if (flags.isNotEmpty()) {
            logger.warn(
                "Compliance flags raised: flags={}, amount={}, methodType={}",
                flags.joinToString(", "), amount, method::class.simpleName
            )
        }
        
        logger.info(
            "Compliance audit - Payment attempt: methodType={}, amount={}, flags={}",
            method::class.simpleName, amount, flags.size
        )
    }
    
    override fun auditPaymentResult(result: PaymentResult) {
        when (result) {
            is PaymentResult.Success -> {
                val event = ComplianceEvent(
                    type = "PAYMENT_SUCCESS",
                    amount = result.amount,
                    flags = emptyList(),
                    methodType = result.paymentMethod::class.simpleName ?: "Unknown",
                    timestamp = System.currentTimeMillis()
                )
                complianceEvents.add(event)
                
                logger.info(
                    "Compliance audit - Successful transaction: transactionId={}, amount={}",
                    result.transactionId, result.amount
                )
            }
            is PaymentResult.Failed -> {
                val flags = if (result.errorCode == "FRAUD_DETECTED") {
                    listOf("FRAUD_ATTEMPT")
                } else {
                    emptyList()
                }
                
                val event = ComplianceEvent(
                    type = "PAYMENT_FAILURE",
                    amount = result.amount,
                    flags = flags,
                    methodType = result.paymentMethod?.let { it::class.simpleName } ?: "Unknown",
                    timestamp = System.currentTimeMillis()
                )
                complianceEvents.add(event)
                
                if (flags.isNotEmpty()) {
                    logger.error(
                        "Compliance alert - Fraud detected: errorCode={}, amount={}",
                        result.errorCode, result.amount
                    )
                }
            }
            else -> {
                // Handle other result types if needed for compliance
                logger.debug("Compliance audit - Other result type: {}", result::class.simpleName)
            }
        }
    }
    
    override fun auditSecurityEvent(event: String, details: Map<String, Any>) {
        val complianceEvent = ComplianceEvent(
            type = "SECURITY_EVENT",
            amount = details["amount"] as? Double ?: 0.0,
            flags = listOf("SECURITY_INCIDENT"),
            methodType = details["methodType"] as? String ?: "Unknown",
            timestamp = System.currentTimeMillis()
        )
        
        complianceEvents.add(complianceEvent)
        
        logger.error(
            "Compliance security event: event={}, details={}",
            event, details
        )
    }
    
    fun generateComplianceReport(): String {
        val totalTransactions = complianceEvents.size
        val flaggedTransactions = complianceEvents.count { it.flags.isNotEmpty() }
        val totalAmount = complianceEvents.sumOf { it.amount }
        
        return """
            === COMPLIANCE REPORT ===
            Report Generated: ${java.time.Instant.now()}
            Total Transactions: $totalTransactions
            Flagged Transactions: $flaggedTransactions
            Flag Rate: ${if (totalTransactions > 0) "%.2f%%".format(flaggedTransactions.toDouble() / totalTransactions * 100) else "0.00%"}
            Total Amount Processed: $${"%.2f".format(totalAmount)}
            
            Flagged Events:
            ${complianceEvents
                .filter { it.flags.isNotEmpty() }
                .joinToString("\n") { "- ${it.type}: ${it.flags.joinToString(", ")} ($${"%.2f".format(it.amount)})" }
            }
            
            Transaction Types:
            ${complianceEvents.groupBy { it.methodType }
                .map { (type, events) -> "- $type: ${events.size} transactions" }
                .joinToString("\n")
            }
        """.trimIndent()
    }
    
    fun getComplianceMetrics(): Map<String, Any> {
        return mapOf(
            "totalTransactions" to complianceEvents.size,
            "flaggedTransactions" to complianceEvents.count { it.flags.isNotEmpty() },
            "totalAmount" to complianceEvents.sumOf { it.amount },
            "recentFlags" to complianceEvents
                .filter { it.timestamp > System.currentTimeMillis() - 24 * 60 * 60 * 1000 } // Last 24 hours
                .flatMap { it.flags }
                .groupBy { it }
                .mapValues { it.value.size }
        )
    }
}

data class ComplianceEvent(
    val type: String,
    val amount: Double,
    val flags: List<String>,
    val methodType: String,
    val timestamp: Long
)