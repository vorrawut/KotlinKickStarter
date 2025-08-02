/**
 * Lesson 3 Complete Solution: Main Application
 */

import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    println("=== Payment Processing System ===")
    println("🚀 Welcome to Lesson 3: OOP + Kotlin Features!\n")
    
    val paymentService = PaymentService()
    
    // Create sample payment methods
    val creditCard = paymentService.createCreditCardPayment(
        id = "CC-001",
        cardNumber = "4532123456789012",
        expiryMonth = 12,
        expiryYear = 2025,
        cardType = CardType.VISA,
        holderName = "John Doe"
    )
    
    val bankAccount = paymentService.createBankAccountPayment(
        id = "BA-001",
        accountNumber = "123456789012",
        routingNumber = "987654321",
        accountType = AccountType.CHECKING,
        bankName = "First National Bank",
        balance = 5000.0
    )
    
    val digitalWallet = paymentService.createDigitalWalletPayment(
        id = "DW-001",
        walletType = WalletType.PAYPAL,
        email = "john.doe@example.com",
        balance = 500.0
    )
    
    val paymentMethods = listOf(creditCard, bankAccount, digitalWallet)
    val amounts = listOf(250.0, 1500.0, 75.0)
    
    // Process each payment method
    paymentMethods.zip(amounts).forEachIndexed { index, (method, amount) ->
        println("${index + 1}. ${getPaymentMethodIcon(method)} Processing ${getPaymentMethodName(method)}:")
        
        val result = paymentService.processPayment(method, amount)
        
        displayPaymentResult(result)
        
        println()
    }
    
    // Demonstrate smart casting
    println("🔍 Smart Casting Demonstrations:")
    demonstrateSmartCasting(paymentMethods)
    
    // Demonstrate when expressions
    println("\n🎯 When Expression Demonstrations:")
    demonstrateWhenExpressions(paymentMethods)
    
    // Demonstrate sealed class exhaustiveness
    println("\n✅ Exhaustive When Expressions:")
    demonstrateExhaustiveWhen(paymentMethods)
    
    // Show processor statistics
    println("\n📊 Processor Statistics:")
    val stats = paymentService.getProcessorStats()
    println("Total Processors: ${stats["totalProcessors"]}")
    println("Supported Methods: ${stats["supportedMethods"]}")
    
    // Demonstrate compliance auditing
    println("\n📋 Compliance Reporting:")
    val complianceAuditor = ComplianceAuditor()
    val complianceService = PaymentService(complianceAuditor)
    
    // Process a large transaction that will be flagged
    complianceService.processPayment(creditCard, 15000.0)
    println(complianceAuditor.generateComplianceReport())
    
    println("\n🎉 Workshop complete! You've mastered Kotlin OOP features!")
    println("📚 Next: Lesson 4 - Spring Boot Setup & Dependency Injection")
}

// Smart casting demonstration
fun demonstrateSmartCasting(methods: List<PaymentMethod>) {
    methods.forEach { method ->
        when (method) {
            is PaymentMethod.CreditCard -> {
                // method is automatically cast to CreditCard here
                println("Credit Card: ${method.maskedNumber} expires ${method.expiryMonth}/${method.expiryYear}")
                println("  Card Type: ${method.cardType.displayName}")
                println("  Holder: ${method.holderName}")
                if (method.isExpired) {
                    println("  ⚠️ Card is expired!")
                }
            }
            is PaymentMethod.BankAccount -> {
                // method is automatically cast to BankAccount here
                println("Bank Account: ${method.maskedAccountNumber} at ${method.bankName}")
                println("  Account Type: ${method.accountType.displayName}")
                println("  Available balance: $${method.balance}")
                println("  Sufficient funds: ${if (method.hasSufficientFunds) "✅" else "❌"}")
            }
            is PaymentMethod.DigitalWallet -> {
                // method is automatically cast to DigitalWallet here
                println("Digital Wallet: ${method.displayName}")
                println("  Wallet Type: ${method.walletType.displayName}")
                println("  Balance: $${method.balance} ${method.currency}")
            }
        }
        println()
    }
}

// When expression demonstrations
fun demonstrateWhenExpressions(methods: List<PaymentMethod>) {
    methods.forEach { method ->
        // When with is checks
        val description = when (method) {
            is PaymentMethod.CreditCard -> 
                "Credit card payment with ${method.cardType.displayName} ending in ${method.lastFourDigits}"
            is PaymentMethod.BankAccount -> 
                "Bank transfer from ${method.accountType.displayName} account at ${method.bankName}"
            is PaymentMethod.DigitalWallet -> 
                "Digital payment via ${method.walletType.displayName} (${method.email})"
        }
        
        println("Description: $description")
        
        // When with conditions
        val riskLevel = when {
            method is PaymentMethod.CreditCard && method.cardType == CardType.AMEX -> "High"
            method is PaymentMethod.BankAccount && method.balance > 10000 -> "Low"
            method is PaymentMethod.DigitalWallet && method.walletType == WalletType.VENMO -> "Medium"
            else -> "Standard"
        }
        
        println("Risk Level: $riskLevel")
        println()
    }
}

// Exhaustive when demonstration
fun demonstrateExhaustiveWhen(methods: List<PaymentMethod>) {
    methods.forEach { method ->
        // Show exhaustive when that compiler enforces
        val processingTime = when (method) {
            is PaymentMethod.CreditCard -> "Instant"
            is PaymentMethod.BankAccount -> "1-3 business days"
            is PaymentMethod.DigitalWallet -> "Instant"
            // Note: No else clause needed - compiler ensures all cases covered
        }
        
        val securityLevel = when (method) {
            is PaymentMethod.CreditCard -> "High (EMV chip + CVV)"
            is PaymentMethod.BankAccount -> "Very High (Bank authentication)"
            is PaymentMethod.DigitalWallet -> "Medium (2FA + biometrics)"
        }
        
        println("${getPaymentMethodName(method)}:")
        println("  Processing time: $processingTime")
        println("  Security level: $securityLevel")
        println()
    }
}

// Helper functions using smart casting
fun getPaymentMethodName(method: PaymentMethod): String = when (method) {
    is PaymentMethod.CreditCard -> "${method.cardType.displayName} Credit Card"
    is PaymentMethod.BankAccount -> "${method.bankName} ${method.accountType.displayName}"
    is PaymentMethod.DigitalWallet -> method.walletType.displayName
}

fun getPaymentMethodIcon(method: PaymentMethod): String = when (method) {
    is PaymentMethod.CreditCard -> when (method.cardType) {
        CardType.VISA -> "💳"
        CardType.MASTERCARD -> "💳"
        CardType.AMEX -> "💎"
        CardType.DISCOVER -> "🔍"
    }
    is PaymentMethod.BankAccount -> "🏦"
    is PaymentMethod.DigitalWallet -> when (method.walletType) {
        WalletType.PAYPAL -> "🅿️"
        WalletType.APPLE_PAY -> "🍎"
        WalletType.GOOGLE_PAY -> "🔍"
        WalletType.VENMO -> "💰"
    }
}

// Result display function
fun displayPaymentResult(result: PaymentResult) {
    when (result) {
        is PaymentResult.Success -> {
            println("✅ Payment Successful!")
            println("   Transaction ID: ${result.transactionId}")
            println("   Amount: ${result.getFormattedAmount()}")
            println("   Fee: $${String.format("%.2f", result.fee)}")
            println("   Total: $${String.format("%.2f", result.total)}")
            println("   Timestamp: ${result.getFormattedTimestamp()}")
        }
        is PaymentResult.Failed -> {
            println("❌ Payment Failed!")
            println("   Error: ${result.errorMessage}")
            println("   Code: ${result.errorCode}")
            if (result.isRetryable()) {
                println("   💡 Retry recommended in ${result.getRetryDelay()}ms")
            } else {
                println("   🚫 Not retryable")
            }
        }
        is PaymentResult.Pending -> {
            println("⏳ Payment Pending")
            println("   Transaction ID: ${result.transactionId}")
            println("   Amount: $${String.format("%.2f", result.amount)}")
            println("   Estimated completion: ${result.estimatedCompletionTime}ms")
            result.statusCheckUrl?.let { url ->
                println("   Status URL: $url")
            }
        }
        is PaymentResult.Cancelled -> {
            println("🚫 Payment Cancelled")
            println("   Reason: ${result.reason}")
            println("   Amount: $${String.format("%.2f", result.amount)}")
        }
    }
    
    // Demonstrate extension functions
    println("   Summary: ${result.formatSummary()}")
    println("   Amount (extracted): $${String.format("%.2f", result.getAmount())}")
    println("   Is successful: ${result.isSuccessful()}")
}