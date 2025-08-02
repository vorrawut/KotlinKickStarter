/**
 * Lesson 3 Workshop: Specific Payment Processors
 */

class CreditCardProcessor : PaymentProcessor {
    
    override val processorName = "CreditCardProcessor"
    override val supportedMethods = setOf("VISA", "MASTERCARD", "AMEX", "DISCOVER")
    override val processingFeeRate = 0.029 // 2.9%
    
    override suspend fun processPayment(method: PaymentMethod, amount: Double): PaymentResult {
        return when (method) {
            is PaymentMethod.CreditCard -> {
                TODO("Implement credit card processing logic")
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
        return TODO("Implement credit card fee calculation with min/max limits")
    }
}

class BankTransferProcessor : PaymentProcessor {
    
    override val processorName = "BankTransferProcessor"
    override val supportedMethods = setOf("CHECKING", "SAVINGS", "BUSINESS")
    override val processingFeeRate = 0.0 // No fees for bank transfers
    
    override suspend fun processPayment(method: PaymentMethod, amount: Double): PaymentResult {
        return when (method) {
            is PaymentMethod.BankAccount -> {
                TODO("Implement bank transfer processing logic")
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
                TODO("Implement digital wallet processing logic")
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