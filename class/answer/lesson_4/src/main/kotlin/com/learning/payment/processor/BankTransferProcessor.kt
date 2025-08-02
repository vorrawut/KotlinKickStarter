/**
 * Lesson 4 Complete Solution: Bank Transfer Processor (Spring Component)
 */

package com.learning.payment.processor

import com.learning.payment.config.PaymentConfigurationProperties
import com.learning.payment.model.PaymentMethod
import com.learning.payment.model.PaymentResult
import kotlinx.coroutines.delay
class BankTransferProcessor(
    private val config: PaymentConfigurationProperties
) : PaymentProcessor {
    
    override val processorName = "BankTransferProcessor"
    override val supportedMethods = setOf("CHECKING", "SAVINGS", "BUSINESS")
    
    override val processingFeeRate: Double
        get() = config.processors.bankTransfer.feeRate
    
    override val maxAmount: Double
        get() = config.processors.bankTransfer.maxAmount
    
    override suspend fun processPayment(method: PaymentMethod, amount: Double): PaymentResult {
        return when (method) {
            is PaymentMethod.BankAccount -> {
                if (!config.processors.bankTransfer.enabled) {
                    return PaymentResult.Failed(
                        "PROCESSOR_DISABLED",
                        "Bank transfer processing is disabled",
                        method,
                        amount
                    )
                }
                
                delay(500) // Bank transfers are slower
                
                if (!method.hasSufficientFunds || amount > method.balance) {
                    return PaymentResult.Failed(
                        "INSUFFICIENT_FUNDS",
                        "Account has insufficient funds",
                        method,
                        amount
                    )
                }
                
                // Large transfers go to pending
                if (amount > 5000.0) {
                    return PaymentResult.Pending(
                        transactionId = generateTransactionId(method),
                        amount = amount,
                        paymentMethod = method,
                        estimatedCompletionTime = 24 * 60 * 60 * 1000L, // 24 hours
                        statusCheckUrl = "https://bank.example.com/status/${generateTransactionId(method)}"
                    )
                }
                
                // Simulate random failures (2% chance)
                if (Math.random() < 0.02) {
                    return PaymentResult.Failed(
                        "BANK_NETWORK_ERROR",
                        "Bank network temporarily unavailable",
                        method,
                        amount
                    )
                }
                
                val fee = calculateFee(amount)
                val total = calculateTotal(amount)
                
                PaymentResult.Success(
                    transactionId = generateTransactionId(method),
                    amount = amount,
                    fee = fee,
                    total = total,
                    paymentMethod = method
                )
            }
            else -> PaymentResult.Failed(
                "UNSUPPORTED_METHOD",
                "Bank transfer processor cannot handle ${method::class.simpleName}",
                method,
                amount
            )
        }
    }
}