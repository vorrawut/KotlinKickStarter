/**
 * Lesson 4 Workshop: Bank Transfer Processor (Spring Component)
 */

package com.learning.payment.processor

import com.learning.payment.config.PaymentConfigurationProperties
import com.learning.payment.model.PaymentMethod
import com.learning.payment.model.PaymentResult
import kotlinx.coroutines.delay
import org.springframework.stereotype.Component

// TODO: Add @Component annotation
class BankTransferProcessor(
    private val config: PaymentConfigurationProperties
) : PaymentProcessor {
    
    override val processorName = "BankTransferProcessor"
    override val supportedMethods = setOf("CHECKING", "SAVINGS", "BUSINESS")
    
    override val processingFeeRate: Double
        get() = TODO("Get fee rate from config.processors.bankTransfer.feeRate")
    
    override val maxAmount: Double
        get() = TODO("Get max amount from config.processors.bankTransfer.maxAmount")
    
    override suspend fun processPayment(method: PaymentMethod, amount: Double): PaymentResult {
        return when (method) {
            is PaymentMethod.BankAccount -> {
                if (!TODO("Check if enabled in config")) {
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
                        estimatedCompletionTime = 24 * 60 * 60 * 1000L
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