/**
 * Lesson 3 Complete Solution: Auditing System
 */

import java.time.Instant
import java.time.format.DateTimeFormatter

interface Auditable {
    fun auditPaymentAttempt(method: PaymentMethod, amount: Double)
    fun auditPaymentResult(result: PaymentResult)
    fun auditSecurityEvent(event: String, details: Map<String, Any>)
}

class PaymentAuditor : Auditable {
    
    override fun auditPaymentAttempt(method: PaymentMethod, amount: Double) {
        val methodType = when (method) {
            is PaymentMethod.CreditCard -> "Credit Card (${method.cardType.displayName})"
            is PaymentMethod.BankAccount -> "Bank Account (${method.accountType.displayName})"
            is PaymentMethod.DigitalWallet -> "Digital Wallet (${method.walletType.displayName})"
        }
        
        val entry = formatAuditEntry(
            level = "INFO",
            message = "Payment attempt",
            details = mapOf(
                "method" to methodType,
                "amount" to amount,
                "methodId" to method.id
            )
        )
        println(entry)
    }
    
    override fun auditPaymentResult(result: PaymentResult) {
        when (result) {
            is PaymentResult.Success -> {
                val entry = formatAuditEntry(
                    level = "INFO",
                    message = "Payment successful",
                    details = mapOf(
                        "transactionId" to result.transactionId,
                        "amount" to result.amount,
                        "fee" to result.fee,
                        "total" to result.total
                    )
                )
                println(entry)
            }
            is PaymentResult.Failed -> {
                val entry = formatAuditEntry(
                    level = "WARN",
                    message = "Payment failed",
                    details = mapOf(
                        "errorCode" to result.errorCode,
                        "errorMessage" to result.errorMessage,
                        "amount" to result.amount,
                        "retryable" to result.isRetryable()
                    )
                )
                println(entry)
            }
            is PaymentResult.Pending -> {
                val entry = formatAuditEntry(
                    level = "INFO",
                    message = "Payment pending",
                    details = mapOf(
                        "transactionId" to result.transactionId,
                        "amount" to result.amount,
                        "estimatedCompletion" to result.estimatedCompletionTime
                    )
                )
                println(entry)
            }
            is PaymentResult.Cancelled -> {
                val entry = formatAuditEntry(
                    level = "WARN",
                    message = "Payment cancelled",
                    details = mapOf(
                        "reason" to result.reason,
                        "amount" to result.amount
                    )
                )
                println(entry)
            }
        }
    }
    
    override fun auditSecurityEvent(event: String, details: Map<String, Any>) {
        val entry = formatAuditEntry(
            level = "SECURITY",
            message = "Security event: $event",
            details = details
        )
        println(entry)
        
        // Additional security-specific logging could go here
        // e.g., send to security monitoring system
    }
    
    private fun formatAuditEntry(level: String, message: String, details: Any? = null): String {
        val timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        val detailsStr = if (details != null) " | Details: $details" else ""
        return "[$timestamp] [$level] AUDIT: $message$detailsStr"
    }
}

class ComplianceAuditor : Auditable {
    
    private val complianceEvents = mutableListOf<ComplianceEvent>()
    
    override fun auditPaymentAttempt(method: PaymentMethod, amount: Double) {
        // Check for compliance requirements
        val flags = mutableListOf<String>()
        
        // Flag large transactions (>$10,000)
        if (amount > 10000.0) {
            flags.add("LARGE_TRANSACTION")
        }
        
        // Flag international payments (simplified check)
        if (method is PaymentMethod.CreditCard && method.cardType == CardType.AMEX) {
            flags.add("POTENTIAL_INTERNATIONAL")
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
            println("[COMPLIANCE] Payment flagged: ${flags.joinToString(", ")} - Amount: $amount")
        }
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
                
                // Track successful transactions for reporting
                println("[COMPLIANCE] Successful transaction recorded: ${result.transactionId}")
            }
            is PaymentResult.Failed -> {
                // Track failed transactions for compliance monitoring
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
            }
            else -> {
                // Handle other result types if needed for compliance
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
        println("[COMPLIANCE] Security event logged: $event")
    }
    
    fun generateComplianceReport(): String {
        val totalTransactions = complianceEvents.size
        val flaggedTransactions = complianceEvents.count { it.flags.isNotEmpty() }
        val totalAmount = complianceEvents.sumOf { it.amount }
        
        return """
            === COMPLIANCE REPORT ===
            Total Transactions: $totalTransactions
            Flagged Transactions: $flaggedTransactions
            Total Amount Processed: ${"$%.2f".format(totalAmount)}
            
            Flagged Events:
            ${complianceEvents
                .filter { it.flags.isNotEmpty() }
                .joinToString("\n") { "- ${it.type}: ${it.flags.joinToString(", ")} (${"$%.2f".format(it.amount)})" }
            }
        """.trimIndent()
    }
}

data class ComplianceEvent(
    val type: String,
    val amount: Double,
    val flags: List<String>,
    val methodType: String,
    val timestamp: Long
)