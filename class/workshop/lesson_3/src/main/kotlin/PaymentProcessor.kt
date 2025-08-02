/**
 * Lesson 3 Workshop: Payment Processor Interface
 */

interface PaymentProcessor {
    
    val processorName: String
    val supportedMethods: Set<String>
    val processingFeeRate: Double
    
    suspend fun processPayment(method: PaymentMethod, amount: Double): PaymentResult
    
    fun calculateFee(amount: Double): Double {
        return TODO("Calculate fee as percentage of amount")
    }
    
    fun calculateTotal(amount: Double): Double {
        return TODO("Return amount + calculated fee")
    }
    
    fun validatePaymentMethod(method: PaymentMethod): Boolean {
        return TODO("Implement basic payment method validation")
    }
    
    fun generateTransactionId(method: PaymentMethod): String {
        return TODO("Generate unique transaction ID")
    }
    
    fun logTransaction(result: PaymentResult) {
        when (result) {
            is PaymentResult.Success -> TODO("Log successful transaction")
            is PaymentResult.Failed -> TODO("Log failed transaction")
            is PaymentResult.Pending -> TODO("Log pending transaction")
            is PaymentResult.Cancelled -> TODO("Log cancelled transaction")
        }
    }
    
    suspend fun executePayment(method: PaymentMethod, amount: Double): PaymentResult {
        return TODO("Implement template method for payment execution")
    }
}