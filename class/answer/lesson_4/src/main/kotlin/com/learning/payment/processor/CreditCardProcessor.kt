/**
 * Lesson 4 Complete Solution: Credit Card Processor (Spring Component)
 * 
 * This demonstrates:
 * - @Component annotation for auto-detection
 * - Constructor injection with configuration
 * - Configuration-driven behavior
 * - Business logic implementation
 */

package com.learning.payment.processor

import com.learning.payment.config.PaymentConfigurationProperties
import com.learning.payment.model.PaymentMethod
import com.learning.payment.model.PaymentResult
import kotlinx.coroutines.delay
class CreditCardProcessor(
    private val config: PaymentConfigurationProperties
) : PaymentProcessor {
    
    override val processorName = "CreditCardProcessor"
    override val supportedMethods = setOf("VISA", "MASTERCARD", "AMEX", "DISCOVER")
    
    override val processingFeeRate: Double
        get() = config.processors.creditCard.feeRate
    
    override val maxAmount: Double
        get() = config.processors.creditCard.maxAmount
    
    override suspend fun processPayment(method: PaymentMethod, amount: Double): PaymentResult {
        return when (method) {
            is PaymentMethod.CreditCard -> {
                // Check if processor is enabled
                if (!config.processors.creditCard.enabled) {
                    return PaymentResult.Failed(
                        "PROCESSOR_DISABLED",
                        "Credit card processing is disabled",
                        method,
                        amount
                    )
                }
                
                // Simulate processing delay
                delay(100)
                
                // Validate card details
                if (method.isExpired) {
                    return PaymentResult.Failed(
                        "CARD_EXPIRED",
                        "Credit card has expired",
                        method,
                        amount
                    )
                }
                
                // Simulate random failures (5% chance)
                if (Math.random() < 0.05) {
                    return PaymentResult.Failed(
                        "NETWORK_ERROR",
                        "Payment network temporarily unavailable",
                        method,
                        amount
                    )
                }
                
                // Calculate fee and total
                val fee = calculateFee(amount)
                val total = calculateTotal(amount)
                val transactionId = generateTransactionId(method)
                
                PaymentResult.Success(
                    transactionId = transactionId,
                    amount = amount,
                    fee = fee,
                    total = total,
                    paymentMethod = method
                )
            }
            else -> PaymentResult.Failed(
                "UNSUPPORTED_METHOD",
                "Credit card processor cannot handle ${method::class.simpleName}",
                method,
                amount
            )
        }
    }
    
    override fun calculateFee(amount: Double): Double {
        val baseFee = amount * processingFeeRate
        // Minimum fee of $0.30, maximum of $50
        return baseFee.coerceIn(0.30, 50.0)
    }
}