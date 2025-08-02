/**
 * Lesson 3 Workshop: Auditing System
 */

interface Auditable {
    fun auditPaymentAttempt(method: PaymentMethod, amount: Double)
    fun auditPaymentResult(result: PaymentResult)
    fun auditSecurityEvent(event: String, details: Map<String, Any>)
}

class PaymentAuditor : Auditable {
    
    override fun auditPaymentAttempt(method: PaymentMethod, amount: Double) {
        TODO("Implement payment attempt auditing")
    }
    
    override fun auditPaymentResult(result: PaymentResult) {
        when (result) {
            is PaymentResult.Success -> TODO("Audit successful payment")
            is PaymentResult.Failed -> TODO("Audit failed payment")  
            is PaymentResult.Pending -> TODO("Audit pending payment")
            is PaymentResult.Cancelled -> TODO("Audit cancelled payment")
        }
    }
    
    override fun auditSecurityEvent(event: String, details: Map<String, Any>) {
        TODO("Implement security event auditing")
    }
    
    private fun formatAuditEntry(level: String, message: String, details: Any? = null): String {
        return TODO("Format audit entry with timestamp and details")
    }
}

class ComplianceAuditor : Auditable {
    
    override fun auditPaymentAttempt(method: PaymentMethod, amount: Double) {
        TODO("Implement compliance auditing for payment attempts")
    }
    
    override fun auditPaymentResult(result: PaymentResult) {
        TODO("Implement compliance auditing for payment results")
    }
    
    override fun auditSecurityEvent(event: String, details: Map<String, Any>) {
        TODO("Implement compliance security auditing")
    }
    
    fun generateComplianceReport(): String {
        return TODO("Generate compliance report from audit data")
    }
}