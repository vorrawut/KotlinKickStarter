/**
 * Lesson 4 Complete Solution: Payment Service (Spring Service Layer)
 * 
 * This demonstrates:
 * - @Service annotation for business logic components
 * - Constructor dependency injection with multiple dependencies
 * - Business logic orchestration
 * - Exception handling and validation
 * - Integration with multiple Spring components
 */

package com.learning.payment.service

import com.learning.payment.audit.Auditable
import com.learning.payment.model.PaymentMethod
import com.learning.payment.model.PaymentResult
import com.learning.payment.processor.PaymentProcessor
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PaymentService(
    private val processors: Map<String, PaymentProcessor>,
    private val auditor: Auditable
) {
    
    private val logger = LoggerFactory.getLogger(PaymentService::class.java)
    
    suspend fun processPayment(method: PaymentMethod, amount: Double): PaymentResult {
        return try {
            logger.debug("Processing payment: method={}, amount={}", method::class.simpleName, amount)
            
            // Audit the payment attempt
            auditor.auditPaymentAttempt(method, amount)
            
            // Validate the payment request
            val validationResult = validatePaymentRequest(method, amount)
            if (validationResult != null) {
                return validationResult
            }
            
            // Select the appropriate processor
            val processor = selectProcessor(method)
                ?: return PaymentResult.Failed(
                    "NO_PROCESSOR",
                    "No processor available for ${method::class.simpleName}",
                    method,
                    amount
                )
            
            logger.debug("Using processor: {}", processor.processorName)
            
            // Execute the payment
            val result = processor.executePayment(method, amount)
            
            // Audit the payment result
            auditor.auditPaymentResult(result)
            
            logger.info("Payment processed: result={}", result::class.simpleName)
            
            result
            
        } catch (e: Exception) {
            logger.error("Payment processing failed", e)
            
            val errorResult = PaymentResult.Failed(
                "PROCESSING_ERROR",
                "Payment processing failed: ${e.message}",
                method,
                amount
            )
            
            auditor.auditPaymentResult(errorResult)
            
            errorResult
        }
    }
    
    private fun selectProcessor(method: PaymentMethod): PaymentProcessor? {
        return when (method) {
            is PaymentMethod.CreditCard -> processors["CREDIT_CARD"]
            is PaymentMethod.BankAccount -> processors["BANK_TRANSFER"]
            is PaymentMethod.DigitalWallet -> processors["DIGITAL_WALLET"]
        }
    }
    
    private fun validatePaymentRequest(method: PaymentMethod, amount: Double): PaymentResult? {
        // Validate amount
        if (amount <= 0) {
            return PaymentResult.Failed(
                "INVALID_AMOUNT",
                "Amount must be greater than zero",
                method,
                amount
            )
        }
        
        // Validate maximum amount (global limit)
        if (amount > 100000.0) {
            return PaymentResult.Failed(
                "AMOUNT_TOO_LARGE",
                "Amount exceeds maximum allowed limit",
                method,
                amount
            )
        }
        
        // Validate payment method is active
        if (!method.isActive) {
            return PaymentResult.Failed(
                "INACTIVE_METHOD",
                "Payment method is not active",
                method,
                amount
            )
        }
        
        // Method-specific validations
        when (method) {
            is PaymentMethod.CreditCard -> {
                if (method.isExpired) {
                    return PaymentResult.Failed(
                        "CARD_EXPIRED",
                        "Credit card has expired",
                        method,
                        amount
                    )
                }
                if (method.cardNumber.length < 13) {
                    return PaymentResult.Failed(
                        "INVALID_CARD_NUMBER",
                        "Invalid card number format",
                        method,
                        amount
                    )
                }
            }
            is PaymentMethod.BankAccount -> {
                if (!method.hasSufficientFunds) {
                    return PaymentResult.Failed(
                        "INSUFFICIENT_FUNDS",
                        "Bank account has insufficient funds",
                        method,
                        amount
                    )
                }
                if (method.routingNumber.length != 9) {
                    return PaymentResult.Failed(
                        "INVALID_ROUTING_NUMBER",
                        "Invalid routing number format",
                        method,
                        amount
                    )
                }
            }
            is PaymentMethod.DigitalWallet -> {
                if (method.balance < amount) {
                    return PaymentResult.Failed(
                        "INSUFFICIENT_WALLET_BALANCE",
                        "Digital wallet has insufficient balance",
                        method,
                        amount
                    )
                }
                if (!method.email.contains("@")) {
                    return PaymentResult.Failed(
                        "INVALID_EMAIL",
                        "Invalid email address for wallet",
                        method,
                        amount
                    )
                }
            }
        }
        
        return null // No validation errors
    }
    
    suspend fun processBatchPayments(payments: List<Pair<PaymentMethod, Double>>): List<PaymentResult> {
        return coroutineScope {
            payments.map { (method, amount) ->
                async {
                    processPayment(method, amount)
                }
            }.awaitAll()
        }
    }
    
    fun getSupportedPaymentMethods(): Set<String> {
        return processors.values.flatMap { it.supportedMethods }.toSet()
    }
    
    fun getProcessorStats(): Map<String, Any> {
        return mapOf(
            "totalProcessors" to processors.size,
            "supportedMethods" to getSupportedPaymentMethods().size,
            "processors" to processors.map { (type, processor) ->
                mapOf(
                    "type" to type,
                    "name" to processor.processorName,
                    "feeRate" to processor.processingFeeRate,
                    "maxAmount" to processor.maxAmount,
                    "supportedMethods" to processor.supportedMethods
                )
            }
        )
    }
    
    fun getHealthStatus(): Map<String, Any> {
        return mapOf(
            "status" to "UP",
            "processorsAvailable" to processors.size,
            "auditingEnabled" to true,
            "timestamp" to System.currentTimeMillis()
        )
    }
}