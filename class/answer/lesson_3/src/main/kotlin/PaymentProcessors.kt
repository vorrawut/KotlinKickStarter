/**
 * Lesson 3 Complete Solution: Specific Payment Processors
 */

import kotlinx.coroutines.delay

class CreditCardProcessor : PaymentProcessor {
    
    override val processorName = "CreditCardProcessor"
    override val supportedMethods = setOf("VISA", "MASTERCARD", "AMEX", "DISCOVER")
    override val processingFeeRate = 0.029 // 2.9%
    
    override suspend fun processPayment(method: PaymentMethod, amount: Double): PaymentResult {
        return when (method) {
            is PaymentMethod.CreditCard -> {
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
                
                // Check amount limits (credit cards typically have higher limits)
                if (amount > 10000.0) {
                    return PaymentResult.Failed(
                        "AMOUNT_EXCEEDS_LIMIT",
                        "Amount exceeds credit card limit",
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
                val total = amount + fee
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

class BankTransferProcessor : PaymentProcessor {
    
    override val processorName = "BankTransferProcessor"
    override val supportedMethods = setOf("CHECKING", "SAVINGS", "BUSINESS")
    override val processingFeeRate = 0.0 // No fees for bank transfers
    
    override suspend fun processPayment(method: PaymentMethod, amount: Double): PaymentResult {
        return when (method) {
            is PaymentMethod.BankAccount -> {
                // Simulate processing delay (bank transfers are slower)
                delay(500)
                
                // Validate account details
                if (!method.hasSufficientFunds) {
                    return PaymentResult.Failed(
                        "INSUFFICIENT_FUNDS",
                        "Account has insufficient funds",
                        method,
                        amount
                    )
                }
                
                // Check if amount exceeds account balance
                if (amount > method.balance) {
                    return PaymentResult.Failed(
                        "INSUFFICIENT_FUNDS",
                        "Amount exceeds account balance",
                        method,
                        amount
                    )
                }
                
                // Large transfers (>$5000) go to pending for review
                if (amount > 5000.0) {
                    val transactionId = generateTransactionId(method)
                    return PaymentResult.Pending(
                        transactionId = transactionId,
                        amount = amount,
                        paymentMethod = method,
                        estimatedCompletionTime = 24 * 60 * 60 * 1000L, // 24 hours
                        statusCheckUrl = "https://bank.example.com/status/$transactionId"
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
                
                val fee = calculateFee(amount) // 0.0 for bank transfers
                val total = amount + fee
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
                "Bank transfer processor cannot handle ${method::class.simpleName}",
                method,
                amount
            )
        }
    }
}

class DigitalWalletProcessor : PaymentProcessor {
    
    override val processorName = "DigitalWalletProcessor"
    override val supportedMethods = setOf("PAYPAL", "APPLE_PAY", "GOOGLE_PAY", "VENMO")
    override val processingFeeRate = 0.03 // 3%
    
    override suspend fun processPayment(method: PaymentMethod, amount: Double): PaymentResult {
        return when (method) {
            is PaymentMethod.DigitalWallet -> {
                // Simulate processing delay
                delay(200)
                
                // Validate wallet details
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
                
                // Simulate random failures (3% chance)
                if (Math.random() < 0.03) {
                    return PaymentResult.Failed(
                        "WALLET_SERVICE_ERROR",
                        "${method.walletType.displayName} service temporarily unavailable",
                        method,
                        amount
                    )
                }
                
                val fee = calculateFee(amount)
                val total = amount + fee
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
                "Digital wallet processor cannot handle ${method::class.simpleName}",
                method,
                amount
            )
        }
    }
}