/**
 * Lesson 4 Workshop: Payment Controller (REST API)
 * 
 * TODO: Complete this Spring REST controller
 * This demonstrates:
 * - @RestController annotation
 * - Dependency injection in controllers
 * - Basic REST endpoints
 * - Request/Response handling
 */

package com.learning.payment.controller

import com.learning.payment.model.PaymentMethod
import com.learning.payment.model.PaymentResult
import com.learning.payment.service.PaymentService
import org.springframework.web.bind.annotation.*

// TODO: Add @RestController annotation
// TODO: Add @RequestMapping("/api/payments") annotation
class PaymentController(
    private val paymentService: PaymentService
) {
    
    // TODO: Add @GetMapping("/processors") annotation
    fun getSupportedMethods(): Map<String, Any> {
        return mapOf(
            "supportedMethods" to TODO("Get supported methods from service"),
            "processorStats" to TODO("Get processor statistics from service")
        )
    }
    
    // TODO: Add @PostMapping("/process") annotation
    suspend fun processPayment(
        // TODO: Add @RequestBody annotation
        request: PaymentRequest
    ): PaymentResponse {
        // TODO: Convert request to PaymentMethod and amount
        // TODO: Call service to process payment
        // TODO: Convert result to PaymentResponse
        
        return TODO("Process payment and return response")
    }
    
    // TODO: Add @PostMapping("/batch") annotation
    suspend fun processBatchPayments(
        // TODO: Add @RequestBody annotation
        requests: List<PaymentRequest>
    ): List<PaymentResponse> {
        // TODO: Convert requests to payment method/amount pairs
        // TODO: Call service for batch processing
        // TODO: Convert results to responses
        
        return TODO("Process batch payments")
    }
    
    // TODO: Add @GetMapping("/health") annotation
    fun healthCheck(): Map<String, String> {
        return mapOf(
            "status" to "UP",
            "service" to "PaymentService",
            "timestamp" to System.currentTimeMillis().toString()
        )
    }
}

// TODO: Define PaymentRequest data class
data class PaymentRequest(
    val method: TODO("Define payment method fields"),
    val amount: Double
)

// TODO: Define PaymentResponse data class
data class PaymentResponse(
    val success: Boolean,
    val transactionId: String?,
    val amount: Double,
    val fee: Double?,
    val total: Double?,
    val errorMessage: String?,
    val timestamp: Long = System.currentTimeMillis()
)