/**
 * Lesson 3 Complete Solution: Payment Processor Interface
 */

interface PaymentProcessor {
    
    val processorName: String
    val supportedMethods: Set<String>
    val processingFeeRate: Double
    
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
    
    fun logTransaction(result: PaymentResult) {
        when (result) {
            is PaymentResult.Success -> {
                println("✅ $processorName: Successful transaction ${result.transactionId} for ${result.getFormattedAmount()}")
            }
            is PaymentResult.Failed -> {
                println("❌ $processorName: Failed transaction - ${result.errorCode}: ${result.errorMessage}")
            }
            is PaymentResult.Pending -> {
                println("⏳ $processorName: Pending transaction ${result.transactionId}")
            }
            is PaymentResult.Cancelled -> {
                println("🚫 $processorName: Cancelled transaction - ${result.reason}")
            }
        }
    }
    
    suspend fun executePayment(method: PaymentMethod, amount: Double): PaymentResult {
        // Template method pattern implementation
        try {
            // Step 1: Validate payment method
            if (!validatePaymentMethod(method)) {
                return PaymentResult.Failed(
                    "VALIDATION_ERROR",
                    "Payment method validation failed",
                    method,
                    amount
                )
            }
            
            // Step 2: Process payment
            val result = processPayment(method, amount)
            
            // Step 3: Log result
            logTransaction(result)
            
            return result
            
        } catch (e: Exception) {
            val errorResult = PaymentResult.Failed(
                "PROCESSING_ERROR",
                "Payment processing failed: ${e.message}",
                method,
                amount
            )
            logTransaction(errorResult)
            return errorResult
        }
    }
}