# 🎯 Lesson 8: Persistence with Spring Data JPA

## Objective

Replace in-memory storage with robust database persistence using Spring Data JPA. Learn entity modeling, repository patterns, and database relationships for scalable data management.

## Key Concepts

### 1. Entity Modeling

```kotlin
@Entity
@Table(name = "payments")
data class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,
    
    @Column(nullable = false, precision = 10, scale = 2)
    val amount: BigDecimal,
    
    @Column(nullable = false, length = 3)
    val currency: String,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: PaymentStatus,
    
    @Column(name = "customer_email", nullable = false)
    val customerEmail: String,
    
    @Column(name = "transaction_id", unique = true)
    val transactionId: String? = null,
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant? = null,
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant? = null
)

enum class PaymentStatus {
    PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED
}
```

### 2. Repository Interfaces

```kotlin
@Repository
interface PaymentRepository : JpaRepository<Payment, String> {
    
    // Derived query methods
    fun findByCustomerEmail(email: String): List<Payment>
    fun findByStatus(status: PaymentStatus): List<Payment>
    fun findByCreatedAtBetween(start: Instant, end: Instant): List<Payment>
    
    // Custom query with @Query
    @Query("SELECT p FROM Payment p WHERE p.amount >= :minAmount AND p.status = :status")
    fun findLargePaymentsByStatus(
        @Param("minAmount") minAmount: BigDecimal,
        @Param("status") status: PaymentStatus
    ): List<Payment>
    
    // Native SQL query
    @Query(
        value = "SELECT * FROM payments WHERE created_at >= :since ORDER BY amount DESC LIMIT :limit",
        nativeQuery = true
    )
    fun findRecentLargePayments(
        @Param("since") since: Instant,
        @Param("limit") limit: Int
    ): List<Payment>
    
    // Modifying query
    @Modifying
    @Query("UPDATE Payment p SET p.status = :newStatus WHERE p.status = :oldStatus")
    fun updatePaymentStatus(
        @Param("oldStatus") oldStatus: PaymentStatus,
        @Param("newStatus") newStatus: PaymentStatus
    ): Int
}
```

### 3. Database Configuration

```kotlin
@Configuration
@EnableJpaRepositories(basePackages = ["com.learning.payment.repository"])
@EnableTransactionManagement
class DatabaseConfig {
    
    @Bean
    @ConfigurationProperties("spring.datasource")
    fun dataSource(): DataSource = DataSourceBuilder.create().build()
    
    @Bean
    fun entityManagerFactory(dataSource: DataSource): LocalContainerEntityManagerFactoryBean {
        return LocalContainerEntityManagerFactoryBean().apply {
            setDataSource(dataSource)
            setPackagesToScan("com.learning.payment.entity")
            jpaVendorAdapter = HibernateJpaVendorAdapter().apply {
                setGenerateDdl(true)
                setShowSql(true)
                setDatabasePlatform("org.hibernate.dialect.PostgreSQLDialect")
            }
        }
    }
}

# application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/payments
    username: payment_user
    password: payment_pass
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect
  
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.xml
```

### 4. Entity Relationships

```kotlin
@Entity
@Table(name = "customers")
data class Customer(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,
    
    @Column(nullable = false)
    val name: String,
    
    @Column(nullable = false, unique = true)
    val email: String,
    
    @OneToMany(mappedBy = "customer", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val payments: MutableList<Payment> = mutableListOf(),
    
    @OneToMany(mappedBy = "customer", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val paymentMethods: MutableList<PaymentMethod> = mutableListOf()
)

@Entity
@Table(name = "payment_methods")
data class PaymentMethod(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    val customer: Customer,
    
    @Enumerated(EnumType.STRING)
    val type: PaymentMethodType,
    
    @Column(name = "card_last_four")
    val cardLastFour: String? = null,
    
    @Column(name = "expiry_month")
    val expiryMonth: Int? = null,
    
    @Column(name = "expiry_year")
    val expiryYear: Int? = null,
    
    @Column(name = "is_default")
    val isDefault: Boolean = false
)

@Entity
@Table(name = "payments")
data class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    val customer: Customer,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id")
    val paymentMethod: PaymentMethod? = null,
    
    @Column(nullable = false, precision = 10, scale = 2)
    val amount: BigDecimal,
    
    @OneToMany(mappedBy = "payment", cascade = [CascadeType.ALL])
    val auditLogs: MutableList<PaymentAuditLog> = mutableListOf()
)
```

## Best Practices

### ✅ Do:
- **Use appropriate fetch types** - LAZY for collections, EAGER sparingly
- **Define proper indexes** - on frequently queried columns
- **Use database constraints** - enforce data integrity at DB level
- **Version your schema** - with Liquibase or Flyway migrations
- **Monitor query performance** - use query logging in development

### ❌ Avoid:
- **N+1 query problems** - use @EntityGraph or JOIN FETCH
- **Exposing entities directly** - always use DTOs for API responses
- **Auto-generating schema in production** - use explicit migrations
- **Ignoring database transactions** - understand transaction boundaries

## Repository Service Pattern

```kotlin
@Service
@Transactional(readOnly = true)
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val customerRepository: CustomerRepository
) {
    
    @Transactional
    fun createPayment(request: CreatePaymentRequest): PaymentResponse {
        val customer = customerRepository.findByEmail(request.customerEmail)
            ?: throw CustomerNotFoundException("Customer not found: ${request.customerEmail}")
        
        val payment = Payment(
            customer = customer,
            amount = request.amount,
            currency = request.currency,
            status = PaymentStatus.PENDING
        )
        
        val savedPayment = paymentRepository.save(payment)
        return savedPayment.toResponse()
    }
    
    fun findPaymentsByCustomer(customerEmail: String, pageable: Pageable): Page<PaymentResponse> {
        return paymentRepository.findByCustomerEmailOrderByCreatedAtDesc(customerEmail, pageable)
            .map { it.toResponse() }
    }
}
```

This lesson teaches you to build scalable, persistent data layers that form the backbone of production applications.