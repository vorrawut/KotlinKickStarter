/**
 * Lesson 3 Complete Solution: Payment Service with Delegation
 */

class PaymentService(
    private val processors: Map<String, PaymentProcessor>,
    auditor: Auditable
) : Auditable by auditor { // Delegation in action!
    
    constructor(auditor: Auditable = PaymentAuditor()) : this(
        processors = mapOf(
            "CREDIT_CARD" to CreditCardProcessor(),
            "BANK_TRANSFER" to BankTransferProcessor(), 
            "DIGITAL_WALLET" to DigitalWalletProcessor()
        ),
        auditor = auditor
    )
    
    suspend fun processPayment(method: PaymentMethod, amount: Double): PaymentResult {
        try {
            // Audit the attempt (uses delegated method)
            auditPaymentAttempt(method, amount)
            
            // Validate inputs
            val validationResult = validatePaymentRequest(method, amount)
            if (validationResult != null) {
                return validationResult // Return validation error
            }
            
            // Select processor
            val processor = selectProcessor(method)
                ?: return PaymentResult.Failed(
                    "NO_PROCESSOR",
                    "No processor available for ${method::class.simpleName}",
                    method,
                    amount
                )
            
            // Process payment
            val result = processor.executePayment(method, amount)
            
            // Audit result (uses delegated method)
            auditPaymentResult(result)
            
            return result
            
        } catch (e: Exception) {
            val errorResult = PaymentResult.Failed(
                "PROCESSING_ERROR",
                "Payment processing failed: ${e.message}",
                method,
                amount
            )
            
            auditPaymentResult(errorResult)
            return errorResult
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
            }
        }
        
        return null // No validation errors
    }
    
    suspend fun processBatchPayments(payments: List<Pair<PaymentMethod, Double>>): List<PaymentResult> {
        return payments.map { (method, amount) ->
            processPayment(method, amount)
        }
    }
    
    fun addProcessor(type: String, processor: PaymentProcessor) {
        // In a real implementation, this would require making processors mutable
        // For now, we'll demonstrate the concept
        println("Would add processor $type: ${processor.processorName}")
        // processors[type] = processor // This would require mutable map
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
                    "supportedMethods" to processor.supportedMethods
                )
            }
        )
    }
    
    // Convenience methods for creating specific payment types
    fun createCreditCardPayment(
        id: String,
        cardNumber: String,
        expiryMonth: Int,
        expiryYear: Int,
        cardType: CardType,
        holderName: String
    ): PaymentMethod.CreditCard {
        return PaymentMethod.CreditCard(
            id = id,
            cardNumber = cardNumber,
            expiryMonth = expiryMonth,
            expiryYear = expiryYear,
            cardType = cardType,
            holderName = holderName
        )
    }
    
    fun createBankAccountPayment(
        id: String,
        accountNumber: String,
        routingNumber: String,
        accountType: AccountType,
        bankName: String,
        balance: Double
    ): PaymentMethod.BankAccount {
        return PaymentMethod.BankAccount(
            id = id,
            accountNumber = accountNumber,
            routingNumber = routingNumber,
            accountType = accountType,
            bankName = bankName,
            balance = balance
        )
    }
    
    fun createDigitalWalletPayment(
        id: String,
        walletType: WalletType,
        email: String,
        balance: Double,
        currency: String = "USD"
    ): PaymentMethod.DigitalWallet {
        return PaymentMethod.DigitalWallet(
            id = id,
            walletType = walletType,
            email = email,
            balance = balance,
            currency = currency
        )
    }
}