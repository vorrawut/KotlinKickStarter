/**
 * Lesson 4 Complete Solution: Payment Method Sealed Classes
 */

package com.learning.payment.model

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
            get() = cardNumber.takeLast(4)
        
        val isExpired: Boolean
            get() {
                val currentDate = LocalDate.now()
                val expiryDate = LocalDate.of(expiryYear, expiryMonth, 1).plusMonths(1).minusDays(1)
                return currentDate.isAfter(expiryDate)
            }
        
        val maskedNumber: String
            get() = "*".repeat(cardNumber.length - 4) + lastFourDigits
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
            get() = "*".repeat(accountNumber.length - 4) + accountNumber.takeLast(4)
            
        val hasSufficientFunds: Boolean
            get() = balance > 0
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
            get() = "${walletType.displayName} (${email})"
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