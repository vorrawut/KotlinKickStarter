/**
 * Lesson 3 Workshop: Payment Result Sealed Class
 */

import java.time.Instant
import java.time.format.DateTimeFormatter

sealed class PaymentResult {
    
    data class Success(
        val transactionId: String,
        val amount: Double,
        val fee: Double,
        val total: Double,
        val paymentMethod: PaymentMethod,
        val timestamp: Long = System.currentTimeMillis()
    ) : PaymentResult() {
        
        fun getFormattedAmount(): String = TODO("Format amount as currency")
        fun getFormattedTimestamp(): String = TODO("Format timestamp as readable date")
    }
    
    data class Failed(
        val errorCode: String,
        val errorMessage: String,
        val paymentMethod: PaymentMethod?,
        val amount: Double,
        val timestamp: Long = System.currentTimeMillis()
    ) : PaymentResult() {
        
        fun isRetryable(): Boolean = TODO("Determine if this error allows retry")
        fun getRetryDelay(): Long = TODO("Get recommended retry delay in milliseconds")
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

fun PaymentResult.isSuccessful(): Boolean = TODO("Return true only for Success results")

fun PaymentResult.getAmount(): Double = TODO("Extract amount from any result type")

fun PaymentResult.formatSummary(): String = TODO("Return formatted summary string")