/**
 * Lesson 4 Complete Solution: Auditing Interface (Spring-ready)
 */

package com.learning.payment.audit

import com.learning.payment.model.PaymentMethod
import com.learning.payment.model.PaymentResult

interface Auditable {
    fun auditPaymentAttempt(method: PaymentMethod, amount: Double)
    fun auditPaymentResult(result: PaymentResult)
    fun auditSecurityEvent(event: String, details: Map<String, Any>)
}