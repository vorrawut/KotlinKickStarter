/**
 * Lesson 4 Complete Solution: Payment Processor Interface (Spring-enabled)
 * 
 * This demonstrates:
 * - Interface design for dependency injection
 * - Template method pattern
 * - Spring component integration
 * - Configuration-driven behavior
 */

package com.learning.payment.processor

import com.learning.payment.model.PaymentMethod
import com.learning.payment.model.PaymentResult

interface PaymentProcessor {
    
    val processorName: String
    val supportedMethods: Set<String>
    val processingFeeRate: Double
    val maxAmount: Double
    
    suspend fun processPayment(method: PaymentMethod, amount: Double): PaymentResult
    
    fun calculateFee(amount: Double): Double {
        return amount * processingFeeRate
    }
    
    fun calculateTotal(amount: Double): Double {
        return amount + calculateFee(amount)
    }
    
    fun validatePaymentMethod(method: PaymentMethod): Boolean {
        if (!method.isActive) return false
        
        return when (method) {
            is PaymentMethod.CreditCard -> !method.isExpired
            is PaymentMethod.BankAccount -> method.hasSufficientFunds
            is PaymentMethod.DigitalWallet -> method.balance >= 0
        }
    }
    
    fun generateTransactionId(method: PaymentMethod): String {
        val prefix = when (method) {
            is PaymentMethod.CreditCard -> "CC"
            is PaymentMethod.BankAccount -> "BT"
            is PaymentMethod.DigitalWallet -> "DW"
        }
        return "$prefix-${System.currentTimeMillis()}"
    }
    
    suspend fun executePayment(method: PaymentMethod, amount: Double): PaymentResult {
        try {
            // Validate amount against processor limits
            if (amount > maxAmount) {
                return PaymentResult.Failed(
                    "AMOUNT_EXCEEDS_LIMIT",
                    "Amount exceeds processor limit of $maxAmount",
                    method,
                    amount
                )
            }
            
            // Validate payment method
            if (!validatePaymentMethod(method)) {
                return PaymentResult.Failed(
                    "VALIDATION_ERROR",
                    "Payment method validation failed",
                    method,
                    amount
                )
            }
            
            // Process payment
            return processPayment(method, amount)
            
        } catch (e: Exception) {
            return PaymentResult.Failed(
                "PROCESSING_ERROR",
                "Payment processing failed: ${e.message}",
                method,
                amount
            )
        }
    }
}