/**
 * Lesson 3 Complete Solution: Payment Result Sealed Class
 */

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.text.NumberFormat
import java.util.*

sealed class PaymentResult {
    
    data class Success(
        val transactionId: String,
        val amount: Double,
        val fee: Double,
        val total: Double,
        val paymentMethod: PaymentMethod,
        val timestamp: Long = System.currentTimeMillis()
    ) : PaymentResult() {
        
        fun getFormattedAmount(): String {
            val formatter = NumberFormat.getCurrencyInstance(Locale.US)
            return formatter.format(amount)
        }
        
        fun getFormattedTimestamp(): String {
            val instant = Instant.ofEpochMilli(timestamp)
            return DateTimeFormatter.ISO_INSTANT.format(instant)
        }
    }
    
    data class Failed(
        val errorCode: String,
        val errorMessage: String,
        val paymentMethod: PaymentMethod?,
        val amount: Double,
        val timestamp: Long = System.currentTimeMillis()
    ) : PaymentResult() {
        
        fun isRetryable(): Boolean {
            val retryableCodes = setOf(
                "NETWORK_ERROR", "TIMEOUT", "TEMPORARY_FAILURE", 
                "INSUFFICIENT_FUNDS", "RATE_LIMITED"
            )
            return errorCode in retryableCodes
        }
        
        fun getRetryDelay(): Long {
            return when (errorCode) {
                "RATE_LIMITED" -> 60000L // 1 minute
                "NETWORK_ERROR" -> 5000L  // 5 seconds
                "TIMEOUT" -> 10000L       // 10 seconds
                else -> 30000L            // 30 seconds default
            }
        }
    }
    
    data class Pending(
        val transactionId: String,
        val amount: Double,
        val paymentMethod: PaymentMethod,
        val estimatedCompletionTime: Long,
        val statusCheckUrl: String? = null
    ) : PaymentResult()
    
    data class Cancelled(
        val reason: String,
        val paymentMethod: PaymentMethod?,
        val amount: Double,
        val timestamp: Long = System.currentTimeMillis()
    ) : PaymentResult()
}

// Extension functions for PaymentResult
fun PaymentResult.isSuccessful(): Boolean = this is PaymentResult.Success

fun PaymentResult.getAmount(): Double = when (this) {
    is PaymentResult.Success -> amount
    is PaymentResult.Failed -> amount
    is PaymentResult.Pending -> amount
    is PaymentResult.Cancelled -> amount
}

fun PaymentResult.formatSummary(): String = when (this) {
    is PaymentResult.Success -> "✅ Success: $transactionId - ${getFormattedAmount()}"
    is PaymentResult.Failed -> "❌ Failed: $errorCode - $errorMessage"
    is PaymentResult.Pending -> "⏳ Pending: $transactionId - Estimated completion: ${estimatedCompletionTime}ms"
    is PaymentResult.Cancelled -> "🚫 Cancelled: $reason"
}