/**
 * Lesson 4 Complete Solution: Payment Service Integration Tests
 * 
 * This demonstrates:
 * - @SpringBootTest for full application context testing
 * - @Autowired dependency injection in tests
 * - Integration testing with real Spring context
 * - Coroutine testing with runTest
 */

package com.learning.payment

import com.learning.payment.model.*
import com.learning.payment.service.PaymentService
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class PaymentServiceTest {
    
    @Autowired
    lateinit var paymentService: PaymentService
    
    @Test
    fun `should inject dependencies correctly`() {
        assertNotNull(paymentService)
    }
    
    @Test
    fun `should process credit card payment successfully`() = runTest {
        // Given
        val creditCard = PaymentMethod.CreditCard(
            id = "test-cc-001",
            cardNumber = "4532123456789012",
            expiryMonth = 12,
            expiryYear = 2025,
            cardType = CardType.VISA,
            holderName = "Test User"
        )
        val amount = 100.0
        
        // When
        val result = paymentService.processPayment(creditCard, amount)
        
        // Then
        assertTrue(result is PaymentResult.Success)
        if (result is PaymentResult.Success) {
            assertEquals(amount, result.amount)
            assertTrue(result.transactionId.isNotEmpty())
            assertTrue(result.fee >= 0)
            assertEquals(amount + result.fee, result.total)
        }
    }
    
    @Test
    fun `should handle invalid amount gracefully`() = runTest {
        // Given
        val creditCard = createTestCreditCard()
        val invalidAmount = -50.0
        
        // When
        val result = paymentService.processPayment(creditCard, invalidAmount)
        
        // Then
        assertTrue(result is PaymentResult.Failed)
        if (result is PaymentResult.Failed) {
            assertEquals("INVALID_AMOUNT", result.errorCode)
            assertEquals(invalidAmount, result.amount)
        }
    }
    
    @Test
    fun `should handle expired credit card`() = runTest {
        // Given
        val expiredCard = PaymentMethod.CreditCard(
            id = "expired-cc-001",
            cardNumber = "4532123456789012",
            expiryMonth = 1,
            expiryYear = 2020, // Expired
            cardType = CardType.VISA,
            holderName = "Test User"
        )
        val amount = 100.0
        
        // When
        val result = paymentService.processPayment(expiredCard, amount)
        
        // Then
        assertTrue(result is PaymentResult.Failed)
        if (result is PaymentResult.Failed) {
            assertEquals("CARD_EXPIRED", result.errorCode)
        }
    }
    
    @Test
    fun `should process bank transfer with large amount as pending`() = runTest {
        // Given
        val bankAccount = PaymentMethod.BankAccount(
            id = "test-ba-001",
            accountNumber = "123456789012",
            routingNumber = "987654321",
            accountType = AccountType.CHECKING,
            bankName = "Test Bank",
            balance = 10000.0
        )
        val largeAmount = 7500.0 // Should trigger pending status
        
        // When
        val result = paymentService.processPayment(bankAccount, largeAmount)
        
        // Then
        assertTrue(result is PaymentResult.Pending)
        if (result is PaymentResult.Pending) {
            assertEquals(largeAmount, result.amount)
            assertTrue(result.transactionId.isNotEmpty())
        }
    }
    
    @Test
    fun `should process digital wallet payment successfully`() = runTest {
        // Given
        val digitalWallet = PaymentMethod.DigitalWallet(
            id = "test-dw-001",
            walletType = WalletType.PAYPAL,
            email = "test@example.com",
            balance = 500.0
        )
        val amount = 75.0
        
        // When
        val result = paymentService.processPayment(digitalWallet, amount)
        
        // Then
        assertTrue(result is PaymentResult.Success)
        if (result is PaymentResult.Success) {
            assertEquals(amount, result.amount)
            assertTrue(result.fee > 0) // Digital wallets have fees
        }
    }
    
    @Test
    fun `should process batch payments correctly`() = runTest {
        // Given
        val payments = listOf(
            createTestCreditCard() to 100.0,
            createTestBankAccount() to 200.0,
            createTestDigitalWallet() to 50.0
        )
        
        // When
        val results = paymentService.processBatchPayments(payments)
        
        // Then
        assertEquals(3, results.size)
        assertTrue(results.all { it.isSuccessful() })
    }
    
    @Test
    fun `should return correct supported payment methods`() {
        // When
        val supportedMethods = paymentService.getSupportedPaymentMethods()
        
        // Then
        assertTrue(supportedMethods.contains("VISA"))
        assertTrue(supportedMethods.contains("MASTERCARD"))
        assertTrue(supportedMethods.contains("CHECKING"))
        assertTrue(supportedMethods.contains("PAYPAL"))
    }
    
    @Test
    fun `should return processor statistics`() {
        // When
        val stats = paymentService.getProcessorStats()
        
        // Then
        assertTrue(stats.containsKey("totalProcessors"))
        assertTrue(stats.containsKey("supportedMethods"))
        assertTrue(stats.containsKey("processors"))
        
        assertEquals(3, stats["totalProcessors"])
    }
    
    @Test
    fun `should return health status`() {
        // When
        val health = paymentService.getHealthStatus()
        
        // Then
        assertEquals("UP", health["status"])
        assertTrue(health.containsKey("processorsAvailable"))
        assertTrue(health.containsKey("timestamp"))
    }
    
    // Test helper methods
    private fun createTestCreditCard() = PaymentMethod.CreditCard(
        id = "test-cc-${System.currentTimeMillis()}",
        cardNumber = "4532123456789012",
        expiryMonth = 12,
        expiryYear = 2025,
        cardType = CardType.VISA,
        holderName = "Test User"
    )
    
    private fun createTestBankAccount() = PaymentMethod.BankAccount(
        id = "test-ba-${System.currentTimeMillis()}",
        accountNumber = "123456789012",
        routingNumber = "987654321",
        accountType = AccountType.CHECKING,
        bankName = "Test Bank",
        balance = 5000.0
    )
    
    private fun createTestDigitalWallet() = PaymentMethod.DigitalWallet(
        id = "test-dw-${System.currentTimeMillis()}",
        walletType = WalletType.PAYPAL,
        email = "test@example.com",
        balance = 1000.0
    )
}