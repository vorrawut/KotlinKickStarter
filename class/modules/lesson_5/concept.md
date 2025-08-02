# 🎯 Lesson 5: REST Controllers & DTOs

## Objective

Build RESTful APIs with Spring Boot that follow industry best practices. Learn to design clean API contracts, handle HTTP methods properly, and create robust data transfer objects for client-server communication.

## Key Concepts

### 1. REST Controller Basics

```kotlin
@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = ["http://localhost:3000"])
class PaymentController(
    private val paymentService: PaymentService
) {
    
    @PostMapping
    fun createPayment(@RequestBody request: CreatePaymentRequest): ResponseEntity<PaymentResponse> {
        val result = paymentService.processPayment(request.toPaymentRequest())
        return when (result) {
            is PaymentResult.Success -> ResponseEntity.ok(result.toResponse())
            is PaymentResult.Failed -> ResponseEntity.badRequest().body(result.toErrorResponse())
        }
    }
    
    @GetMapping("/{id}")
    fun getPayment(@PathVariable id: String): ResponseEntity<PaymentResponse> {
        return paymentService.findPayment(id)
            ?.let { ResponseEntity.ok(it.toResponse()) }
            ?: ResponseEntity.notFound().build()
    }
}
```

### 2. Data Transfer Objects (DTOs)

```kotlin
data class CreatePaymentRequest(
    @field:NotNull
    @field:Positive
    val amount: Double,
    
    @field:NotBlank
    val currency: String,
    
    @field:Valid
    val paymentMethod: PaymentMethodDto,
    
    @field:Email
    val customerEmail: String
)

data class PaymentResponse(
    val id: String,
    val amount: Double,
    val currency: String,
    val status: String,
    val createdAt: Instant,
    val transactionId: String?
)

data class PaymentMethodDto(
    @field:NotBlank
    val type: String,
    
    val cardNumber: String?,
    val expiryMonth: Int?,
    val expiryYear: Int?
)
```

### 3. HTTP Status Codes & Error Handling

```kotlin
@PostMapping
fun createPayment(@Valid @RequestBody request: CreatePaymentRequest): ResponseEntity<*> {
    return try {
        val result = paymentService.processPayment(request)
        when (result) {
            is PaymentResult.Success -> ResponseEntity.status(HttpStatus.CREATED).body(result.toResponse())
            is PaymentResult.Failed -> ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(result.toErrorResponse())
            is PaymentResult.Pending -> ResponseEntity.status(HttpStatus.ACCEPTED).body(result.toResponse())
        }
    } catch (e: ValidationException) {
        ResponseEntity.badRequest().body(ErrorResponse("VALIDATION_ERROR", e.message))
    } catch (e: PaymentException) {
        ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ErrorResponse("PAYMENT_ERROR", e.message))
    }
}
```

### 4. Content Negotiation & Serialization

```kotlin
@GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
fun getPayments(
    @RequestParam(defaultValue = "0") page: Int,
    @RequestParam(defaultValue = "20") size: Int,
    @RequestParam(required = false) status: String?
): ResponseEntity<PagedResponse<PaymentResponse>> {
    
    val payments = paymentService.findPayments(page, size, status)
    return ResponseEntity.ok(payments.toPagedResponse())
}

@PostMapping(
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
fun processPayment(@RequestBody request: CreatePaymentRequest) = // ...
```

## API Design Best Practices

### ✅ Do:
- **Use proper HTTP methods** - GET for reads, POST for creates, PUT for updates
- **Return appropriate status codes** - 201 for created, 404 for not found
- **Version your APIs** - `/api/v1/payments`
- **Use consistent naming** - plural nouns for collections
- **Validate all inputs** - use Bean Validation annotations

### ❌ Avoid:
- **Exposing internal models** - always use DTOs for API contracts
- **Inconsistent response formats** - standardize error responses
- **Missing validation** - validate all user inputs
- **Overly complex endpoints** - keep URLs simple and predictable

## Next Steps

This completes Phase 1 foundations. Phase 2 (Lessons 6-11) builds production-ready APIs with validation, persistence, and testing.