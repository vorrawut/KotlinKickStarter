/**
 * Lesson 3 Workshop: Main Application
 */

suspend fun main() {
    println("=== Payment Processing System ===")
    println("🚀 Welcome to Lesson 3: OOP + Kotlin Features!\n")
    
    val paymentService = PaymentService()
    
    // TODO: Create sample payment methods
    val creditCard = TODO("Create sample credit card")
    val bankAccount = TODO("Create sample bank account") 
    val digitalWallet = TODO("Create sample digital wallet")
    
    val paymentMethods = listOf(creditCard, bankAccount, digitalWallet)
    val amounts = listOf(250.0, 1500.0, 75.0)
    
    // TODO: Process each payment method
    paymentMethods.zip(amounts).forEachIndexed { index, (method, amount) ->
        println("${index + 1}. ${getPaymentMethodIcon(method)} Processing ${getPaymentMethodName(method)}:")
        
        val result = paymentService.processPayment(method, amount)
        
        displayPaymentResult(result)
        
        println()
    }
    
    // TODO: Demonstrate smart casting
    println("🔍 Smart Casting Demonstrations:")
    demonstrateSmartCasting(paymentMethods)
    
    // TODO: Demonstrate when expressions
    println("\n🎯 When Expression Demonstrations:")
    demonstrateWhenExpressions(paymentMethods)
    
    // TODO: Demonstrate sealed class exhaustiveness
    println("\n✅ Exhaustive When Expressions:")
    demonstrateExhaustiveWhen(paymentMethods)
    
    println("\n🎉 Workshop complete! You've mastered Kotlin OOP features!")
    println("📚 Next: Lesson 4 - Spring Boot Setup & Dependency Injection")
}

// TODO: Smart casting demonstration
fun demonstrateSmartCasting(methods: List<PaymentMethod>) {
    methods.forEach { method ->
        when (method) {
            is PaymentMethod.CreditCard -> {
                println("Credit Card: ${method.maskedNumber} expires ${method.expiryMonth}/${method.expiryYear}")
                if (method.isExpired) {
                    println("⚠️ Card is expired!")
                }
            }
            is PaymentMethod.BankAccount -> {
                println("Bank Account: ${method.maskedAccountNumber} at ${method.bankName}")
                println("Available balance: $${method.balance}")
            }
            is PaymentMethod.DigitalWallet -> {
                println("Digital Wallet: ${method.displayName}")
                println("Balance: $${method.balance} ${method.currency}")
            }
        }
    }
}

// TODO: When expression demonstrations
fun demonstrateWhenExpressions(methods: List<PaymentMethod>) {
    methods.forEach { method ->
        val description = when (method) {
            is PaymentMethod.CreditCard -> TODO("Return credit card description")
            is PaymentMethod.BankAccount -> TODO("Return bank account description")
            is PaymentMethod.DigitalWallet -> TODO("Return digital wallet description")
        }
        
        println("Description: $description")
        
        val riskLevel = when {
            TODO("Implement risk level logic based on method and amount")
        }
        
        println("Risk Level: $riskLevel")
    }
}

// TODO: Exhaustive when demonstration
fun demonstrateExhaustiveWhen(methods: List<PaymentMethod>) {
    methods.forEach { method ->
        val processingTime = when (method) {
            is PaymentMethod.CreditCard -> "Instant"
            is PaymentMethod.BankAccount -> "1-3 business days"
            is PaymentMethod.DigitalWallet -> "Instant"
        }
        
        println("${getPaymentMethodName(method)} processing time: $processingTime")
    }
}

// TODO: Helper functions using smart casting
fun getPaymentMethodName(method: PaymentMethod): String = when (method) {
    is PaymentMethod.CreditCard -> TODO("Return credit card name with type")
    is PaymentMethod.BankAccount -> TODO("Return bank account name")
    is PaymentMethod.DigitalWallet -> TODO("Return wallet name with type")
}

fun getPaymentMethodIcon(method: PaymentMethod): String = when (method) {
    is PaymentMethod.CreditCard -> TODO("Return appropriate credit card icon")
    is PaymentMethod.BankAccount -> "🏦"
    is PaymentMethod.DigitalWallet -> TODO("Return wallet-specific icon")
}

// TODO: Result display function
fun displayPaymentResult(result: PaymentResult) {
    when (result) {
        is PaymentResult.Success -> {
            println("✅ Payment Successful!")
            println("   Transaction ID: ${result.transactionId}")
            println("   Amount: ${result.getFormattedAmount()}")
            println("   Fee: $${result.fee}")
            println("   Total: $${result.total}")
        }
        is PaymentResult.Failed -> {
            println("❌ Payment Failed!")
            println("   Error: ${result.errorMessage}")
            println("   Code: ${result.errorCode}")
            if (result.isRetryable()) {
                println("   💡 Retry recommended in ${result.getRetryDelay()}ms")
            }
        }
        is PaymentResult.Pending -> {
            println("⏳ Payment Pending")
            println("   Transaction ID: ${result.transactionId}")
            println("   Estimated completion: ${result.estimatedCompletionTime}ms")
        }
        is PaymentResult.Cancelled -> {
            println("🚫 Payment Cancelled")
            println("   Reason: ${result.reason}")
        }
    }
}