/**
 * Lesson 4 Complete Solution: Payment Controller (REST API)
 * 
 * This demonstrates:
 * - @RestController annotation for REST endpoints
 * - @RequestMapping for URL mapping
 * - Dependency injection in controllers
 * - Request/Response DTOs
 * - Error handling in controllers
 * - Kotlin coroutines with Spring WebFlux
 */

package com.learning.payment.controller

import com.learning.payment.model.*
import com.learning.payment.service.PaymentService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/payments")
class PaymentController(
    private val paymentService: PaymentService
) {
    
    private val logger = LoggerFactory.getLogger(PaymentController::class.java)
    
    @GetMapping("/processors")
    fun getSupportedMethods(): Map<String, Any> {
        return mapOf(
            "supportedMethods" to paymentService.getSupportedPaymentMethods(),
            "processorStats" to paymentService.getProcessorStats()
        )
    }
    
    @PostMapping("/process")
    suspend fun processPayment(
        @RequestBody request: PaymentRequest
    ): ResponseEntity<PaymentResponse> {
        return try {
            logger.info("Processing payment request: amount={}, methodType={}", request.amount, request.methodType)
            
            // Convert request to PaymentMethod
            val paymentMethod = convertToPaymentMethod(request)
            
            // Process payment
            val result = paymentService.processPayment(paymentMethod, request.amount)
            
            // Convert result to response
            val response = convertToPaymentResponse(result)
            
            // Return appropriate HTTP status
            val status = when (result) {
                is PaymentResult.Success -> HttpStatus.OK
                is PaymentResult.Pending -> HttpStatus.ACCEPTED
                is PaymentResult.Failed -> HttpStatus.BAD_REQUEST
                is PaymentResult.Cancelled -> HttpStatus.CONFLICT
            }
            
            ResponseEntity.status(status).body(response)
            
        } catch (e: Exception) {
            logger.error("Error processing payment", e)
            
            val errorResponse = PaymentResponse(
                success = false,
                transactionId = null,
                amount = request.amount,
                fee = null,
                total = null,
                errorMessage = "Payment processing failed: ${e.message}"
            )
            
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
        }
    }
    
    @PostMapping("/batch")
    suspend fun processBatchPayments(
        @RequestBody requests: List<PaymentRequest>
    ): List<PaymentResponse> {
        logger.info("Processing batch payment: {} requests", requests.size)
        
        val payments = requests.map { request ->
            convertToPaymentMethod(request) to request.amount
        }
        
        val results = paymentService.processBatchPayments(payments)
        
        return results.map { result ->
            convertToPaymentResponse(result)
        }
    }
    
    @GetMapping("/health")
    fun healthCheck(): Map<String, Any> {
        return paymentService.getHealthStatus()
    }
    
    @GetMapping("/methods/supported")
    fun getSupportedPaymentMethods(): Set<String> {
        return paymentService.getSupportedPaymentMethods()
    }
    
    private fun convertToPaymentMethod(request: PaymentRequest): PaymentMethod {
        return when (request.methodType.uppercase()) {
            "CREDIT_CARD" -> PaymentMethod.CreditCard(
                id = request.methodData["id"] as? String ?: "cc-${System.currentTimeMillis()}",
                cardNumber = request.methodData["cardNumber"] as? String ?: throw IllegalArgumentException("Card number required"),
                expiryMonth = (request.methodData["expiryMonth"] as? Number)?.toInt() ?: throw IllegalArgumentException("Expiry month required"),
                expiryYear = (request.methodData["expiryYear"] as? Number)?.toInt() ?: throw IllegalArgumentException("Expiry year required"),
                cardType = CardType.valueOf((request.methodData["cardType"] as? String ?: "VISA").uppercase()),
                holderName = request.methodData["holderName"] as? String ?: "Unknown"
            )
            "BANK_ACCOUNT" -> PaymentMethod.BankAccount(
                id = request.methodData["id"] as? String ?: "ba-${System.currentTimeMillis()}",
                accountNumber = request.methodData["accountNumber"] as? String ?: throw IllegalArgumentException("Account number required"),
                routingNumber = request.methodData["routingNumber"] as? String ?: throw IllegalArgumentException("Routing number required"),
                accountType = AccountType.valueOf((request.methodData["accountType"] as? String ?: "CHECKING").uppercase()),
                bankName = request.methodData["bankName"] as? String ?: "Unknown Bank",
                balance = (request.methodData["balance"] as? Number)?.toDouble() ?: 10000.0
            )
            "DIGITAL_WALLET" -> PaymentMethod.DigitalWallet(
                id = request.methodData["id"] as? String ?: "dw-${System.currentTimeMillis()}",
                walletType = WalletType.valueOf((request.methodData["walletType"] as? String ?: "PAYPAL").uppercase()),
                email = request.methodData["email"] as? String ?: throw IllegalArgumentException("Email required"),
                balance = (request.methodData["balance"] as? Number)?.toDouble() ?: 1000.0,
                currency = request.methodData["currency"] as? String ?: "USD"
            )
            else -> throw IllegalArgumentException("Unsupported payment method type: ${request.methodType}")
        }
    }
    
    private fun convertToPaymentResponse(result: PaymentResult): PaymentResponse {
        return when (result) {
            is PaymentResult.Success -> PaymentResponse(
                success = true,
                transactionId = result.transactionId,
                amount = result.amount,
                fee = result.fee,
                total = result.total,
                errorMessage = null,
                timestamp = result.timestamp
            )
            is PaymentResult.Failed -> PaymentResponse(
                success = false,
                transactionId = null,
                amount = result.amount,
                fee = null,
                total = null,
                errorMessage = "${result.errorCode}: ${result.errorMessage}",
                timestamp = result.timestamp
            )
            is PaymentResult.Pending -> PaymentResponse(
                success = true,
                transactionId = result.transactionId,
                amount = result.amount,
                fee = null,
                total = null,
                errorMessage = "Payment is pending processing"
            )
            is PaymentResult.Cancelled -> PaymentResponse(
                success = false,
                transactionId = null,
                amount = result.amount,
                fee = null,
                total = null,
                errorMessage = "Payment cancelled: ${result.reason}",
                timestamp = result.timestamp
            )
        }
    }
}

// Data Transfer Objects
data class PaymentRequest(
    val methodType: String,
    val methodData: Map<String, Any>,
    val amount: Double
)

data class PaymentResponse(
    val success: Boolean,
    val transactionId: String?,
    val amount: Double,
    val fee: Double?,
    val total: Double?,
    val errorMessage: String?,
    val timestamp: Long = System.currentTimeMillis()
)