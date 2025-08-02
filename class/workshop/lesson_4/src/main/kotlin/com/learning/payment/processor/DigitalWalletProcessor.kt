/**
 * Lesson 4 Workshop: Digital Wallet Processor (Spring Component)
 */

package com.learning.payment.processor

import com.learning.payment.config.PaymentConfigurationProperties
import com.learning.payment.model.PaymentMethod
import com.learning.payment.model.PaymentResult
import com.learning.payment.model.WalletType
import kotlinx.coroutines.delay
import org.springframework.stereotype.Component

// TODO: Add @Component annotation
class DigitalWalletProcessor(
    private val config: PaymentConfigurationProperties
) : PaymentProcessor {
    
    override val processorName = "DigitalWalletProcessor"
    override val supportedMethods = setOf("PAYPAL", "APPLE_PAY", "GOOGLE_PAY", "VENMO")
    
    override val processingFeeRate: Double
        get() = TODO("Get fee rate from config.processors.digitalWallet.feeRate")
    
    override val maxAmount: Double
        get() = TODO("Get max amount from config.processors.digitalWallet.maxAmount")
    
    override suspend fun processPayment(method: PaymentMethod, amount: Double): PaymentResult {
        return when (method) {
            is PaymentMethod.DigitalWallet -> {
                if (!TODO("Check if enabled in config")) {
                    return PaymentResult.Failed(
                        "PROCESSOR_DISABLED",
                        "Digital wallet processing is disabled",
                        method,
                        amount
                    )
                }
                
                delay(200)
                
                if (method.balance < amount) {
                    return PaymentResult.Failed(
                        "INSUFFICIENT_WALLET_BALANCE",
                        "Digital wallet has insufficient balance",
                        method,
                        amount
                    )
                }
                
                // Check wallet-specific limits
                val walletLimit = when (method.walletType) {
                    WalletType.PAYPAL -> 2500.0
                    WalletType.APPLE_PAY -> 10000.0
                    WalletType.GOOGLE_PAY -> 5000.0
                    WalletType.VENMO -> 1000.0
                }
                
                if (amount > walletLimit) {
                    return PaymentResult.Failed(
                        "WALLET_LIMIT_EXCEEDED",
                        "Amount exceeds ${method.walletType.displayName} limit",
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
                "Digital wallet processor cannot handle ${method::class.simpleName}",
                method,
                amount
            )
        }
    }
}