/**
 * Lesson 3 Workshop: Payment Method Sealed Classes
 */

import java.time.LocalDate

sealed class PaymentMethod {
    abstract val id: String
    abstract val isActive: Boolean
    
    data class CreditCard(
        override val id: String,
        override val isActive: Boolean = true,
        val cardNumber: String,
        val expiryMonth: Int,
        val expiryYear: Int,
        val cardType: CardType,
        val holderName: String
    ) : PaymentMethod() {
        
        val lastFourDigits: String
            get() = TODO("Return last 4 digits of card number")
        
        val isExpired: Boolean
            get() = TODO("Check if card is expired based on current date")
        
        val maskedNumber: String
            get() = TODO("Return masked card number like ****1234")
    }
    
    data class BankAccount(
        override val id: String,
        override val isActive: Boolean = true,
        val accountNumber: String,
        val routingNumber: String,
        val accountType: AccountType,
        val bankName: String,
        val balance: Double
    ) : PaymentMethod() {
        
        val maskedAccountNumber: String
            get() = TODO("Return masked account number")
            
        val hasSufficientFunds: Boolean
            get() = TODO("Check if balance > 0")
    }
    
    data class DigitalWallet(
        override val id: String,  
        override val isActive: Boolean = true,
        val walletType: WalletType,
        val email: String,
        val balance: Double,
        val currency: String = "USD"
    ) : PaymentMethod() {
        
        val displayName: String
            get() = TODO("Return formatted display name with wallet type and email")
    }
}

enum class CardType(val displayName: String) {
    VISA("Visa"),
    MASTERCARD("Mastercard"), 
    AMEX("American Express"),
    DISCOVER("Discover")
}

enum class AccountType(val displayName: String) {
    CHECKING("Checking"),
    SAVINGS("Savings"),
    BUSINESS("Business")
}

enum class WalletType(val displayName: String) {
    PAYPAL("PayPal"),
    APPLE_PAY("Apple Pay"),
    GOOGLE_PAY("Google Pay"),
    VENMO("Venmo")
}