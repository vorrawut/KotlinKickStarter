# 🏆 Lesson 20 Workshop: Complete Booking System Capstone

## 🎯 Workshop Objective

Build a comprehensive, production-ready booking system that integrates all 19 lessons into a single, enterprise-grade application. This capstone project demonstrates mastery of Kotlin, Spring Boot, microservices architecture, security, caching, monitoring, testing, and cloud deployment - everything needed for professional backend development.

**⏱️ Estimated Time**: 3-4 hours (can be completed over multiple sessions)

---

## 🏗️ What You'll Build

### **Enterprise Booking Platform**
A complete booking system with:
- **Multi-tenant resource management** (rooms, equipment, services)
- **Advanced user authentication** with JWT and role-based access control
- **Real-time availability checking** with intelligent caching
- **Payment processing** with multiple payment methods and fraud detection
- **Comprehensive notifications** via email, SMS, and in-app messaging
- **Advanced analytics** with real-time dashboards and business intelligence
- **Production deployment** with monitoring, scaling, and disaster recovery

### **Real-World Features**
```kotlin
// Complete domain model with business rules
@Entity
@Table(name = "bookings")
data class Booking(
    @Id val id: UUID = UUID.randomUUID(),
    @ManyToOne val user: User,
    @ManyToOne val resource: BookableResource,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    @Enumerated(EnumType.STRING) val status: BookingStatus,
    val totalPrice: BigDecimal,
    @OneToMany(cascade = [CascadeType.ALL]) val payments: Set<Payment>,
    @OneToMany(cascade = [CascadeType.ALL]) val notifications: Set<BookingNotification>
) : BaseAuditEntity()
```

**🎯 Success Metrics**: 
- Complete booking flow from search to confirmation in <30 seconds
- Real-time availability updates with Redis caching
- JWT authentication with refresh token rotation
- Comprehensive monitoring with custom business metrics
- Auto-scaling deployment handling 1000+ concurrent users

---

## 📁 Project Structure

```
class/workshop/lesson_20/
├── booking-system/
│   ├── src/main/kotlin/com/learning/booking/
│   │   ├── BookingSystemApplication.kt     # TODO: Main application
│   │   ├── config/                         # TODO: Configuration classes
│   │   │   ├── SecurityConfig.kt
│   │   │   ├── CacheConfig.kt
│   │   │   ├── DatabaseConfig.kt
│   │   │   └── MonitoringConfig.kt
│   │   ├── domain/                         # TODO: Domain models
│   │   │   ├── user/
│   │   │   ├── booking/
│   │   │   ├── resource/
│   │   │   ├── payment/
│   │   │   └── notification/
│   │   ├── application/                    # TODO: Application services
│   │   │   ├── BookingService.kt
│   │   │   ├── PaymentService.kt
│   │   │   ├── NotificationService.kt
│   │   │   └── AnalyticsService.kt
│   │   ├── infrastructure/                 # TODO: Infrastructure layer
│   │   │   ├── repository/
│   │   │   ├── external/
│   │   │   └── messaging/
│   │   └── api/                           # TODO: API controllers
│   │       ├── BookingController.kt
│   │       ├── ResourceController.kt
│   │       ├── AuthController.kt
│   │       └── AnalyticsController.kt
│   ├── src/test/kotlin/                   # TODO: Comprehensive tests
│   ├── docker/                            # TODO: Docker configuration
│   ├── k8s/                              # TODO: Kubernetes manifests
│   ├── terraform/                         # TODO: Infrastructure as code
│   └── monitoring/                        # TODO: Monitoring configuration
├── docs/
│   ├── api-documentation.md               # TODO: API documentation
│   ├── deployment-guide.md                # TODO: Deployment guide
│   └── architecture-decisions.md          # TODO: Architecture decisions
└── scripts/
    ├── setup-development.sh               # TODO: Development setup
    ├── run-tests.sh                       # TODO: Test execution
    └── deploy-production.sh               # TODO: Production deployment
```

---

## 🛠️ Step 1: Project Setup and Domain Architecture

### **📝 TODO: Create BookingSystemApplication.kt**
```kotlin
// src/main/kotlin/com/learning/booking/BookingSystemApplication.kt
package com.learning.booking

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.transaction.annotation.EnableTransactionManagement

@SpringBootApplication
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = ["com.learning.booking.infrastructure.repository.jpa"])
@EnableMongoRepositories(basePackages = ["com.learning.booking.infrastructure.repository.mongo"])
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
@ConfigurationPropertiesScan
class BookingSystemApplication

fun main(args: Array<String>) {
    runApplication<BookingSystemApplication>(*args)
}
```

### **📝 TODO: Create Domain Models**
```kotlin
// src/main/kotlin/com/learning/booking/domain/common/BaseEntity.kt
package com.learning.booking.domain.common

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseAuditEntity {
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime
        private set
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: LocalDateTime
        private set
    
    @Column(name = "created_by")
    var createdBy: String? = null
        private set
    
    @Column(name = "updated_by")
    var updatedBy: String? = null
        private set
}

// src/main/kotlin/com/learning/booking/domain/user/User.kt
package com.learning.booking.domain.user

import com.learning.booking.domain.common.BaseAuditEntity
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "users")
data class User(
    @Id
    val id: UUID = UUID.randomUUID(),
    
    @Column(unique = true, nullable = false)
    val email: String,
    
    @Column(nullable = false)
    val firstName: String,
    
    @Column(nullable = false)
    val lastName: String,
    
    @Column(nullable = false)
    val passwordHash: String,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: UserRole = UserRole.CUSTOMER,
    
    @Column(nullable = false)
    val isActive: Boolean = true,
    
    @Column(nullable = false)
    val isEmailVerified: Boolean = false,
    
    @Column
    val phoneNumber: String? = null,
    
    @Column
    val lastLoginAt: LocalDateTime? = null,
    
    @Column(nullable = false)
    val failedLoginAttempts: Int = 0,
    
    @Column
    val lockedUntil: LocalDateTime? = null,
    
    @Embedded
    val preferences: UserPreferences = UserPreferences()
) : BaseAuditEntity() {
    
    val fullName: String
        get() = "$firstName $lastName"
    
    val isLocked: Boolean
        get() = lockedUntil?.isAfter(LocalDateTime.now()) == true
    
    fun isEnabled(): Boolean = isActive && isEmailVerified && !isLocked
}

enum class UserRole {
    CUSTOMER, RESOURCE_MANAGER, SYSTEM_ADMIN
}

@Embeddable
data class UserPreferences(
    @Column(name = "preferred_language")
    val preferredLanguage: String = "en",
    
    @Column(name = "timezone")
    val timezone: String = "UTC",
    
    @Column(name = "email_notifications")
    val emailNotifications: Boolean = true,
    
    @Column(name = "sms_notifications")
    val smsNotifications: Boolean = false
)

// src/main/kotlin/com/learning/booking/domain/resource/BookableResource.kt
package com.learning.booking.domain.resource

import com.learning.booking.domain.common.BaseAuditEntity
import com.learning.booking.domain.user.User
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.*

@Entity
@Table(name = "bookable_resources")
data class BookableResource(
    @Id
    val id: UUID = UUID.randomUUID(),
    
    @Column(nullable = false)
    val name: String,
    
    @Column(length = 1000)
    val description: String?,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: ResourceType,
    
    @Column(nullable = false)
    val capacity: Int,
    
    @Column(precision = 10, scale = 2, nullable = false)
    val pricePerHour: BigDecimal,
    
    @Embedded
    val location: Location,
    
    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "resource_amenities")
    val amenities: Set<Amenity> = emptySet(),
    
    @OneToMany(mappedBy = "resource", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val availabilityRules: Set<AvailabilityRule> = emptySet(),
    
    @Column(nullable = false)
    val isActive: Boolean = true,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    val manager: User,
    
    @Column(name = "booking_lead_time_hours")
    val bookingLeadTimeHours: Int = 2,
    
    @Column(name = "max_booking_duration_hours")
    val maxBookingDurationHours: Int = 8,
    
    @Column(name = "min_booking_duration_minutes")
    val minBookingDurationMinutes: Int = 30
) : BaseAuditEntity()

enum class ResourceType {
    MEETING_ROOM, EQUIPMENT, VENUE, SERVICE, ACCOMMODATION
}

enum class Amenity {
    WIFI, PROJECTOR, WHITEBOARD, COFFEE, PARKING, 
    AIR_CONDITIONING, KITCHEN, ACCESSIBILITY
}

@Embeddable
data class Location(
    @Column(nullable = false)
    val address: String,
    
    @Column(nullable = false)
    val city: String,
    
    @Column(nullable = false)
    val country: String,
    
    @Column(name = "postal_code")
    val postalCode: String?,
    
    @Column
    val latitude: Double?,
    
    @Column
    val longitude: Double?
)

@Entity
@Table(name = "availability_rules")
data class AvailabilityRule(
    @Id
    val id: UUID = UUID.randomUUID(),
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    val resource: BookableResource,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val dayOfWeek: DayOfWeek,
    
    @Column(nullable = false)
    val startTime: LocalTime,
    
    @Column(nullable = false)
    val endTime: LocalTime,
    
    @Column(nullable = false)
    val isAvailable: Boolean = true
) : BaseAuditEntity()
```

### **📝 TODO: Create Booking Domain**
```kotlin
// src/main/kotlin/com/learning/booking/domain/booking/Booking.kt
package com.learning.booking.domain.booking

import com.learning.booking.domain.common.BaseAuditEntity
import com.learning.booking.domain.resource.BookableResource
import com.learning.booking.domain.user.User
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "bookings")
data class Booking(
    @Id
    val id: UUID = UUID.randomUUID(),
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    val resource: BookableResource,
    
    @Column(nullable = false)
    val startTime: LocalDateTime,
    
    @Column(nullable = false)
    val endTime: LocalDateTime,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: BookingStatus = BookingStatus.PENDING,
    
    @Column(precision = 10, scale = 2, nullable = false)
    val totalPrice: BigDecimal,
    
    @Column(length = 500)
    val notes: String? = null,
    
    @OneToMany(mappedBy = "booking", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val payments: MutableSet<Payment> = mutableSetOf(),
    
    @OneToMany(mappedBy = "booking", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val notifications: MutableSet<BookingNotification> = mutableSetOf(),
    
    @Version
    var version: Long = 0,
    
    @Column(name = "cancellation_reason")
    var cancellationReason: String? = null,
    
    @Column(name = "cancelled_at")
    var cancelledAt: LocalDateTime? = null
) : BaseAuditEntity() {
    
    val duration: Long
        get() = java.time.Duration.between(startTime, endTime).toMinutes()
    
    val isPaid: Boolean
        get() = payments.any { it.status == PaymentStatus.COMPLETED }
    
    val isActive: Boolean
        get() = status in listOf(BookingStatus.PENDING, BookingStatus.CONFIRMED)
    
    fun canBeCancelled(): Boolean {
        return isActive && startTime.isAfter(LocalDateTime.now().plusHours(24))
    }
    
    fun cancel(reason: String?) {
        if (!canBeCancelled()) {
            throw IllegalStateException("Booking cannot be cancelled")
        }
        status = BookingStatus.CANCELLED
        cancellationReason = reason
        cancelledAt = LocalDateTime.now()
    }
}

enum class BookingStatus {
    PENDING, CONFIRMED, CANCELLED, COMPLETED, NO_SHOW
}

// src/main/kotlin/com/learning/booking/domain/booking/Payment.kt
@Entity
@Table(name = "payments")
data class Payment(
    @Id
    val id: UUID = UUID.randomUUID(),
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    val booking: Booking,
    
    @Column(precision = 10, scale = 2, nullable = false)
    val amount: BigDecimal,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val method: PaymentMethod,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PaymentStatus = PaymentStatus.PENDING,
    
    @Column(name = "external_payment_id")
    val externalPaymentId: String? = null,
    
    @Column(name = "payment_gateway")
    val paymentGateway: String? = null,
    
    @Column(name = "processed_at")
    var processedAt: LocalDateTime? = null,
    
    @Column(name = "failure_reason")
    var failureReason: String? = null
) : BaseAuditEntity()

enum class PaymentMethod {
    CREDIT_CARD, PAYPAL, BANK_TRANSFER, WALLET
}

enum class PaymentStatus {
    PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED
}

// src/main/kotlin/com/learning/booking/domain/booking/BookingNotification.kt
@Entity
@Table(name = "booking_notifications")
data class BookingNotification(
    @Id
    val id: UUID = UUID.randomUUID(),
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    val booking: Booking,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: NotificationType,
    
    @Column(nullable = false)
    val recipient: String,
    
    @Column(nullable = false)
    val subject: String,
    
    @Column(length = 2000, nullable = false)
    val content: String,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: NotificationStatus = NotificationStatus.PENDING,
    
    @Column(name = "sent_at")
    var sentAt: LocalDateTime? = null,
    
    @Column(name = "failure_reason")
    var failureReason: String? = null,
    
    @Column(name = "retry_count")
    var retryCount: Int = 0
) : BaseAuditEntity()

enum class NotificationType {
    EMAIL, SMS, PUSH_NOTIFICATION
}

enum class NotificationStatus {
    PENDING, SENT, FAILED, DELIVERED
}
```

---

## 🛠️ Step 2: Advanced Service Layer Implementation

### **📝 TODO: Create BookingService.kt**
```kotlin
// src/main/kotlin/com/learning/booking/application/BookingService.kt
package com.learning.booking.application

import com.learning.booking.domain.booking.*
import com.learning.booking.domain.resource.BookableResource
import com.learning.booking.domain.user.User
import com.learning.booking.infrastructure.repository.jpa.BookingRepository
import com.learning.booking.infrastructure.repository.jpa.BookableResourceRepository
import com.learning.booking.infrastructure.repository.jpa.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class BookingService(
    private val bookingRepository: BookingRepository,
    private val resourceRepository: BookableResourceRepository,
    private val userRepository: UserRepository,
    private val paymentService: PaymentService,
    private val notificationService: NotificationService,
    private val analyticsService: AnalyticsService,
    private val eventPublisher: ApplicationEventPublisher
) {
    
    private val logger = LoggerFactory.getLogger(javaClass)
    
    // Data classes for requests and responses
    data class CreateBookingRequest(
        val userId: UUID,
        val resourceId: UUID,
        val startTime: LocalDateTime,
        val endTime: LocalDateTime,
        val notes: String? = null,
        val paymentMethod: PaymentMethod = PaymentMethod.CREDIT_CARD
    )
    
    data class AvailabilityResult(
        val isAvailable: Boolean,
        val conflicts: List<Booking> = emptyList(),
        val suggestedAlternatives: List<TimeSlot> = emptyList(),
        val reason: String? = null
    )
    
    data class TimeSlot(
        val startTime: LocalDateTime,
        val endTime: LocalDateTime,
        val price: BigDecimal
    )
    
    data class BookingResult(
        val booking: Booking?,
        val isSuccess: Boolean,
        val errorMessage: String? = null,
        val suggestedAlternatives: List<TimeSlot> = emptyList()
    ) {
        companion object {
            fun success(booking: Booking) = BookingResult(booking, true)
            fun failure(message: String, alternatives: List<TimeSlot> = emptyList()) = 
                BookingResult(null, false, message, alternatives)
        }
    }
    
    @Cacheable("resource-availability", key = "#resourceId + ':' + #startTime.toLocalDate()")
    suspend fun checkAvailability(
        resourceId: UUID,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): AvailabilityResult = withContext(Dispatchers.IO) {
        logger.debug("Checking availability for resource $resourceId from $startTime to $endTime")
        
        val resource = resourceRepository.findById(resourceId)
            ?: return@withContext AvailabilityResult(
                isAvailable = false,
                reason = "Resource not found"
            )
        
        if (!resource.isActive) {
            return@withContext AvailabilityResult(
                isAvailable = false,
                reason = "Resource is not active"
            )
        }
        
        // Check booking constraints
        val duration = Duration.between(startTime, endTime)
        if (duration.toMinutes() < resource.minBookingDurationMinutes) {
            return@withContext AvailabilityResult(
                isAvailable = false,
                reason = "Booking duration too short (minimum ${resource.minBookingDurationMinutes} minutes)"
            )
        }
        
        if (duration.toHours() > resource.maxBookingDurationHours) {
            return@withContext AvailabilityResult(
                isAvailable = false,
                reason = "Booking duration too long (maximum ${resource.maxBookingDurationHours} hours)"
            )
        }
        
        // Check lead time
        val leadTime = Duration.between(LocalDateTime.now(), startTime)
        if (leadTime.toHours() < resource.bookingLeadTimeHours) {
            return@withContext AvailabilityResult(
                isAvailable = false,
                reason = "Booking must be made at least ${resource.bookingLeadTimeHours} hours in advance"
            )
        }
        
        // Check for conflicts
        val conflicts = bookingRepository.findConflictingBookings(
            resourceId, startTime, endTime, 
            listOf(BookingStatus.CONFIRMED, BookingStatus.PENDING)
        )
        
        if (conflicts.isNotEmpty()) {
            val alternatives = findAlternativeSlots(resourceId, startTime, endTime, resource)
            return@withContext AvailabilityResult(
                isAvailable = false,
                conflicts = conflicts,
                suggestedAlternatives = alternatives,
                reason = "Resource is already booked during this time"
            )
        }
        
        // Check availability rules
        val isWithinBusinessHours = resource.availabilityRules.any { rule ->
            rule.dayOfWeek == startTime.dayOfWeek &&
            !startTime.toLocalTime().isBefore(rule.startTime) &&
            !endTime.toLocalTime().isAfter(rule.endTime) &&
            rule.isAvailable
        }
        
        if (!isWithinBusinessHours) {
            return@withContext AvailabilityResult(
                isAvailable = false,
                reason = "Booking is outside business hours"
            )
        }
        
        AvailabilityResult(isAvailable = true)
    }
    
    @Transactional(isolation = Isolation.SERIALIZABLE)
    @CacheEvict("resource-availability", key = "#request.resourceId + ':' + #request.startTime.toLocalDate()")
    suspend fun createBooking(request: CreateBookingRequest): BookingResult = withContext(Dispatchers.IO) {
        logger.info("Creating booking for user ${request.userId}, resource ${request.resourceId}")
        
        try {
            // Validate request
            validateBookingRequest(request)
            
            // Double-check availability with database lock
            val availability = checkAvailability(request.resourceId, request.startTime, request.endTime)
            if (!availability.isAvailable) {
                return@withContext BookingResult.failure(
                    availability.reason ?: "Resource not available",
                    availability.suggestedAlternatives
                )
            }
            
            // Load entities
            val user = userRepository.findById(request.userId)
                ?: throw IllegalArgumentException("User not found: ${request.userId}")
            
            val resource = resourceRepository.findById(request.resourceId)
                ?: throw IllegalArgumentException("Resource not found: ${request.resourceId}")
            
            // Calculate price
            val totalPrice = calculateBookingPrice(resource, request.startTime, request.endTime, user)
            
            // Create booking
            val booking = Booking(
                user = user,
                resource = resource,
                startTime = request.startTime,
                endTime = request.endTime,
                totalPrice = totalPrice,
                notes = request.notes,
                status = BookingStatus.PENDING
            )
            
            val savedBooking = bookingRepository.save(booking)
            
            // Process payment
            val paymentResult = paymentService.processPayment(
                PaymentRequest(
                    bookingId = savedBooking.id,
                    amount = totalPrice,
                    method = request.paymentMethod,
                    userId = user.id
                )
            )
            
            // Update booking status based on payment
            when (paymentResult.status) {
                PaymentStatus.COMPLETED -> {
                    savedBooking.status = BookingStatus.CONFIRMED
                    bookingRepository.save(savedBooking)
                    
                    // Send confirmation notification
                    notificationService.sendBookingConfirmation(savedBooking)
                    
                    // Publish booking confirmed event
                    eventPublisher.publishEvent(BookingConfirmedEvent(savedBooking))
                    
                    logger.info("Booking ${savedBooking.id} confirmed and payment completed")
                }
                PaymentStatus.PENDING -> {
                    // Payment is processing, keep booking as pending
                    notificationService.sendPaymentPendingNotification(savedBooking)
                    
                    logger.info("Booking ${savedBooking.id} created, payment pending")
                }
                PaymentStatus.FAILED -> {
                    savedBooking.status = BookingStatus.CANCELLED
                    savedBooking.cancellationReason = "Payment failed"
                    bookingRepository.save(savedBooking)
                    
                    notificationService.sendPaymentFailedNotification(savedBooking)
                    
                    logger.warn("Booking ${savedBooking.id} cancelled due to payment failure")
                    
                    return@withContext BookingResult.failure("Payment failed")
                }
                else -> {
                    logger.warn("Unexpected payment status: ${paymentResult.status}")
                }
            }
            
            // Record analytics
            analyticsService.recordBookingCreated(savedBooking)
            
            BookingResult.success(savedBooking)
            
        } catch (ex: Exception) {
            logger.error("Failed to create booking", ex)
            BookingResult.failure("Failed to create booking: ${ex.message}")
        }
    }
    
    private suspend fun calculateBookingPrice(
        resource: BookableResource,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        user: User
    ): BigDecimal {
        val duration = Duration.between(startTime, endTime)
        val hours = duration.toMinutes() / 60.0
        
        var basePrice = resource.pricePerHour.multiply(BigDecimal.valueOf(hours))
        
        // Apply dynamic pricing
        basePrice = applyDynamicPricing(basePrice, startTime, resource)
        
        // Apply user discounts
        basePrice = applyUserDiscounts(basePrice, user)
        
        return basePrice.setScale(2, BigDecimal.ROUND_HALF_UP)
    }
    
    private fun applyDynamicPricing(
        basePrice: BigDecimal,
        startTime: LocalDateTime,
        resource: BookableResource
    ): BigDecimal {
        var multiplier = 1.0
        
        // Peak hours pricing (9 AM - 5 PM on weekdays)
        val isPeakHour = startTime.hour in 9..17 && 
                        startTime.dayOfWeek.value <= 5
        if (isPeakHour) {
            multiplier *= 1.3
        }
        
        // Demand-based pricing
        val nearbyBookings = bookingRepository.countBookingsInTimeRange(
            resource.id,
            startTime.minusHours(2),
            startTime.plusHours(2)
        )
        
        when {
            nearbyBookings > 10 -> multiplier *= 1.5
            nearbyBookings > 5 -> multiplier *= 1.2
        }
        
        return basePrice.multiply(BigDecimal.valueOf(multiplier))
    }
    
    private fun applyUserDiscounts(basePrice: BigDecimal, user: User): BigDecimal {
        // Loyalty discount based on booking history
        val bookingCount = bookingRepository.countByUserAndStatus(user.id, BookingStatus.COMPLETED)
        
        val discountPercent = when {
            bookingCount > 50 -> 0.15 // 15% discount for VIP users
            bookingCount > 20 -> 0.10 // 10% discount for frequent users
            bookingCount > 5 -> 0.05  // 5% discount for regular users
            else -> 0.0
        }
        
        return basePrice.multiply(BigDecimal.valueOf(1.0 - discountPercent))
    }
    
    private suspend fun findAlternativeSlots(
        resourceId: UUID,
        desiredStartTime: LocalDateTime,
        desiredEndTime: LocalDateTime,
        resource: BookableResource
    ): List<TimeSlot> {
        val duration = Duration.between(desiredStartTime, desiredEndTime)
        val alternatives = mutableListOf<TimeSlot>()
        
        // Check same day alternatives
        var checkTime = desiredStartTime.toLocalDate().atStartOfDay()
        val endOfDay = checkTime.plusDays(1)
        
        while (checkTime.isBefore(endOfDay) && alternatives.size < 5) {
            val slotEnd = checkTime.plus(duration)
            
            if (slotEnd.isAfter(endOfDay)) break
            
            val availability = checkAvailability(resourceId, checkTime, slotEnd)
            if (availability.isAvailable) {
                val price = calculateBookingPrice(resource, checkTime, slotEnd, 
                    userRepository.findById(UUID.randomUUID())!!) // Dummy user for price calculation
                
                alternatives.add(TimeSlot(checkTime, slotEnd, price))
            }
            
            checkTime = checkTime.plusMinutes(30) // Check every 30 minutes
        }
        
        return alternatives
    }
    
    private fun validateBookingRequest(request: CreateBookingRequest) {
        require(request.startTime.isAfter(LocalDateTime.now())) {
            "Booking start time must be in the future"
        }
        
        require(request.endTime.isAfter(request.startTime)) {
            "Booking end time must be after start time"
        }
        
        require(Duration.between(request.startTime, request.endTime).toMinutes() >= 30) {
            "Minimum booking duration is 30 minutes"
        }
    }
    
    @Transactional(readOnly = true)
    fun getUserBookings(
        userId: UUID,
        status: BookingStatus? = null,
        pageable: Pageable
    ): Page<Booking> {
        return if (status != null) {
            bookingRepository.findByUserIdAndStatus(userId, status, pageable)
        } else {
            bookingRepository.findByUserId(userId, pageable)
        }
    }
    
    @Transactional
    @CacheEvict("resource-availability", key = "#bookingId")
    fun cancelBooking(bookingId: UUID, reason: String?, userId: UUID): Boolean {
        val booking = bookingRepository.findById(bookingId)
            ?: throw IllegalArgumentException("Booking not found")
        
        if (booking.user.id != userId) {
            throw IllegalArgumentException("User not authorized to cancel this booking")
        }
        
        if (!booking.canBeCancelled()) {
            return false
        }
        
        booking.cancel(reason)
        bookingRepository.save(booking)
        
        // Process refund if payment was made
        if (booking.isPaid) {
            paymentService.processRefund(booking.id, "Booking cancelled by user")
        }
        
        // Send cancellation notification
        notificationService.sendBookingCancellation(booking)
        
        // Record analytics
        analyticsService.recordBookingCancelled(booking)
        
        logger.info("Booking ${bookingId} cancelled by user ${userId}")
        return true
    }
}

// Event classes
data class BookingConfirmedEvent(val booking: Booking)
data class BookingCancelledEvent(val booking: Booking)
```

---

## 🛠️ Step 3: Payment Processing Implementation

### **📝 TODO: Create PaymentService.kt**
```kotlin
// src/main/kotlin/com/learning/booking/application/PaymentService.kt
package com.learning.booking.application

import com.learning.booking.domain.booking.Payment
import com.learning.booking.domain.booking.PaymentMethod
import com.learning.booking.domain.booking.PaymentStatus
import com.learning.booking.infrastructure.external.PaymentGateway
import com.learning.booking.infrastructure.repository.jpa.PaymentRepository
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.retry.annotation.Retry
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class PaymentRequest(
    val bookingId: UUID,
    val amount: BigDecimal,
    val method: PaymentMethod,
    val userId: UUID,
    val description: String? = null
)

data class PaymentResult(
    val paymentId: UUID?,
    val status: PaymentStatus,
    val message: String? = null,
    val externalTransactionId: String? = null
)

@Service
@Transactional
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val paymentGateway: PaymentGateway,
    private val notificationService: NotificationService
) {
    
    private val logger = LoggerFactory.getLogger(javaClass)
    
    @CircuitBreaker(name = "payment-service", fallbackMethod = "fallbackProcessPayment")
    @Retry(name = "payment-service")
    suspend fun processPayment(request: PaymentRequest): PaymentResult {
        logger.info("Processing payment for booking ${request.bookingId}, amount: ${request.amount}")
        
        try {
            // Create payment record
            val payment = Payment(
                booking = bookingRepository.findById(request.bookingId)!!,
                amount = request.amount,
                method = request.method,
                status = PaymentStatus.PROCESSING
            )
            
            val savedPayment = paymentRepository.save(payment)
            
            // Process with external gateway
            val gatewayResult = when (request.method) {
                PaymentMethod.CREDIT_CARD -> paymentGateway.processCreditCardPayment(
                    amount = request.amount,
                    currency = "USD",
                    customerId = request.userId.toString(),
                    description = request.description ?: "Booking payment"
                )
                PaymentMethod.PAYPAL -> paymentGateway.processPayPalPayment(
                    amount = request.amount,
                    customerId = request.userId.toString()
                )
                PaymentMethod.BANK_TRANSFER -> paymentGateway.processBankTransfer(
                    amount = request.amount,
                    customerId = request.userId.toString()
                )
                PaymentMethod.WALLET -> paymentGateway.processWalletPayment(
                    amount = request.amount,
                    customerId = request.userId.toString()
                )
            }
            
            // Update payment with result
            savedPayment.status = if (gatewayResult.isSuccess) PaymentStatus.COMPLETED else PaymentStatus.FAILED
            savedPayment.externalPaymentId = gatewayResult.transactionId
            savedPayment.paymentGateway = gatewayResult.gateway
            savedPayment.processedAt = LocalDateTime.now()
            
            if (!gatewayResult.isSuccess) {
                savedPayment.failureReason = gatewayResult.errorMessage
            }
            
            paymentRepository.save(savedPayment)
            
            logger.info("Payment ${savedPayment.id} processed with status: ${savedPayment.status}")
            
            return PaymentResult(
                paymentId = savedPayment.id,
                status = savedPayment.status,
                message = gatewayResult.message,
                externalTransactionId = gatewayResult.transactionId
            )
            
        } catch (ex: Exception) {
            logger.error("Payment processing failed for booking ${request.bookingId}", ex)
            return PaymentResult(
                paymentId = null,
                status = PaymentStatus.FAILED,
                message = "Payment processing failed: ${ex.message}"
            )
        }
    }
    
    private fun fallbackProcessPayment(request: PaymentRequest, ex: Exception): PaymentResult {
        logger.warn("Payment service unavailable, using fallback for booking ${request.bookingId}", ex)
        
        // Create pending payment for retry later
        val payment = Payment(
            booking = bookingRepository.findById(request.bookingId)!!,
            amount = request.amount,
            method = request.method,
            status = PaymentStatus.PENDING,
            failureReason = "Payment service temporarily unavailable"
        )
        
        val savedPayment = paymentRepository.save(payment)
        
        // Schedule retry
        schedulePaymentRetry(savedPayment.id, request)
        
        return PaymentResult(
            paymentId = savedPayment.id,
            status = PaymentStatus.PENDING,
            message = "Payment temporarily unavailable. Will retry shortly."
        )
    }
    
    @Async
    fun schedulePaymentRetry(paymentId: UUID, originalRequest: PaymentRequest) {
        // TODO: Implement payment retry logic with exponential backoff
        logger.info("Scheduling payment retry for payment $paymentId")
        
        // This would typically use a job queue like Redis or database-based scheduler
        // For now, we'll just log the intention
        notificationService.notifyPaymentRetryScheduled(paymentId, originalRequest)
    }
    
    @Transactional
    fun processRefund(bookingId: UUID, reason: String): PaymentResult {
        logger.info("Processing refund for booking $bookingId")
        
        val payments = paymentRepository.findByBookingIdAndStatus(bookingId, PaymentStatus.COMPLETED)
        
        if (payments.isEmpty()) {
            return PaymentResult(
                paymentId = null,
                status = PaymentStatus.FAILED,
                message = "No completed payments found for refund"
            )
        }
        
        val payment = payments.first()
        
        try {
            // Process refund with gateway
            val refundResult = paymentGateway.processRefund(
                originalTransactionId = payment.externalPaymentId!!,
                amount = payment.amount,
                reason = reason
            )
            
            if (refundResult.isSuccess) {
                // Create refund payment record
                val refundPayment = Payment(
                    booking = payment.booking,
                    amount = payment.amount.negate(),
                    method = payment.method,
                    status = PaymentStatus.REFUNDED,
                    externalPaymentId = refundResult.transactionId,
                    paymentGateway = refundResult.gateway,
                    processedAt = LocalDateTime.now()
                )
                
                paymentRepository.save(refundPayment)
                
                logger.info("Refund processed successfully for booking $bookingId")
                
                return PaymentResult(
                    paymentId = refundPayment.id,
                    status = PaymentStatus.REFUNDED,
                    message = "Refund processed successfully",
                    externalTransactionId = refundResult.transactionId
                )
            } else {
                logger.error("Refund failed for booking $bookingId: ${refundResult.errorMessage}")
                
                return PaymentResult(
                    paymentId = null,
                    status = PaymentStatus.FAILED,
                    message = "Refund failed: ${refundResult.errorMessage}"
                )
            }
            
        } catch (ex: Exception) {
            logger.error("Refund processing failed for booking $bookingId", ex)
            
            return PaymentResult(
                paymentId = null,
                status = PaymentStatus.FAILED,
                message = "Refund processing failed: ${ex.message}"
            )
        }
    }
    
    @Transactional(readOnly = true)
    fun getPaymentHistory(userId: UUID): List<Payment> {
        return paymentRepository.findByBookingUserIdOrderByCreatedAtDesc(userId)
    }
    
    @Transactional(readOnly = true)
    fun getPaymentDetails(paymentId: UUID): Payment? {
        return paymentRepository.findById(paymentId)
    }
}
```

---

## 🛠️ Step 4: API Controllers and Security

### **📝 TODO: Create AuthController.kt**
```kotlin
// src/main/kotlin/com/learning/booking/api/AuthController.kt
package com.learning.booking.api

import com.learning.booking.application.AuthService
import com.learning.booking.application.UserService
import com.learning.booking.domain.user.User
import com.learning.booking.domain.user.UserRole
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.*

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User authentication and authorization")
class AuthController(
    private val authService: AuthService,
    private val userService: UserService
) {
    
    data class LoginRequest(
        @field:Email(message = "Please provide a valid email address")
        val email: String,
        
        @field:Size(min = 8, message = "Password must be at least 8 characters")
        val password: String,
        
        val rememberMe: Boolean = false
    )
    
    data class RegisterRequest(
        @field:Email(message = "Please provide a valid email address")
        val email: String,
        
        @field:Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        val firstName: String,
        
        @field:Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        val lastName: String,
        
        @field:Size(min = 8, message = "Password must be at least 8 characters")
        val password: String,
        
        val phoneNumber: String? = null,
        val role: UserRole = UserRole.CUSTOMER
    )
    
    data class RefreshTokenRequest(
        val refreshToken: String
    )
    
    data class AuthResponse(
        val accessToken: String,
        val refreshToken: String,
        val expiresAt: Instant,
        val user: UserInfo
    )
    
    data class UserInfo(
        val id: UUID,
        val email: String,
        val firstName: String,
        val lastName: String,
        val role: UserRole,
        val isEmailVerified: Boolean
    )
    
    @Operation(summary = "User login", description = "Authenticate user with email and password")
    @PostMapping("/login")
    suspend fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        val result = authService.authenticate(request.email, request.password, request.rememberMe)
        
        return if (result.isSuccess) {
            ResponseEntity.ok(
                AuthResponse(
                    accessToken = result.tokenPair!!.accessToken,
                    refreshToken = result.tokenPair.refreshToken,
                    expiresAt = result.tokenPair.expiresAt,
                    user = UserInfo(
                        id = result.user!!.id,
                        email = result.user.email,
                        firstName = result.user.firstName,
                        lastName = result.user.lastName,
                        role = result.user.role,
                        isEmailVerified = result.user.isEmailVerified
                    )
                )
            )
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(null)
        }
    }
    
    @Operation(summary = "User registration", description = "Register a new user account")
    @PostMapping("/register")
    suspend fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> {
        val result = authService.register(
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            password = request.password,
            phoneNumber = request.phoneNumber,
            role = request.role
        )
        
        return if (result.isSuccess) {
            ResponseEntity.status(HttpStatus.CREATED).body(
                AuthResponse(
                    accessToken = result.tokenPair!!.accessToken,
                    refreshToken = result.tokenPair.refreshToken,
                    expiresAt = result.tokenPair.expiresAt,
                    user = UserInfo(
                        id = result.user!!.id,
                        email = result.user.email,
                        firstName = result.user.firstName,
                        lastName = result.user.lastName,
                        role = result.user.role,
                        isEmailVerified = result.user.isEmailVerified
                    )
                )
            )
        } else {
            ResponseEntity.badRequest().body(null)
        }
    }
    
    @Operation(summary = "Refresh access token", description = "Get new access token using refresh token")
    @PostMapping("/refresh")
    fun refreshToken(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<AuthResponse> {
        val result = authService.refreshToken(request.refreshToken)
        
        return if (result != null) {
            ResponseEntity.ok(
                AuthResponse(
                    accessToken = result.accessToken,
                    refreshToken = result.refreshToken,
                    expiresAt = result.expiresAt,
                    user = authService.getUserFromToken(result.accessToken)
                )
            )
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)
        }
    }
    
    @Operation(summary = "Logout", description = "Invalidate current session")
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    fun logout(@RequestHeader("Authorization") authHeader: String): ResponseEntity<Void> {
        val token = authHeader.removePrefix("Bearer ")
        authService.logout(token)
        return ResponseEntity.ok().build()
    }
    
    @Operation(summary = "Get current user", description = "Get current authenticated user information")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    fun getCurrentUser(): ResponseEntity<UserInfo> {
        val user = authService.getCurrentUser()
        
        return ResponseEntity.ok(
            UserInfo(
                id = user.id,
                email = user.email,
                firstName = user.firstName,
                lastName = user.lastName,
                role = user.role,
                isEmailVerified = user.isEmailVerified
            )
        )
    }
    
    @Operation(summary = "Verify email", description = "Verify user email address")
    @PostMapping("/verify-email")
    fun verifyEmail(@RequestParam token: String): ResponseEntity<Map<String, String>> {
        val result = authService.verifyEmail(token)
        
        return if (result) {
            ResponseEntity.ok(mapOf("message" to "Email verified successfully"))
        } else {
            ResponseEntity.badRequest().body(mapOf("error" to "Invalid or expired verification token"))
        }
    }
    
    @Operation(summary = "Forgot password", description = "Send password reset email")
    @PostMapping("/forgot-password")
    fun forgotPassword(@RequestParam email: String): ResponseEntity<Map<String, String>> {
        authService.sendPasswordResetEmail(email)
        return ResponseEntity.ok(mapOf("message" to "Password reset email sent"))
    }
    
    @Operation(summary = "Reset password", description = "Reset password using reset token")
    @PostMapping("/reset-password")
    fun resetPassword(
        @RequestParam token: String,
        @RequestParam newPassword: String
    ): ResponseEntity<Map<String, String>> {
        val result = authService.resetPassword(token, newPassword)
        
        return if (result) {
            ResponseEntity.ok(mapOf("message" to "Password reset successfully"))
        } else {
            ResponseEntity.badRequest().body(mapOf("error" to "Invalid or expired reset token"))
        }
    }
}
```

### **📝 TODO: Create BookingController.kt**
```kotlin
// src/main/kotlin/com/learning/booking/api/BookingController.kt
package com.learning.booking.api

import com.learning.booking.application.BookingService
import com.learning.booking.application.BookingService.*
import com.learning.booking.domain.booking.Booking
import com.learning.booking.domain.booking.BookingStatus
import com.learning.booking.domain.booking.PaymentMethod
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.*

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Bookings", description = "Booking management operations")
class BookingController(
    private val bookingService: BookingService
) {
    
    data class CreateBookingRequest(
        @field:NotNull(message = "Resource ID is required")
        val resourceId: UUID,
        
        @field:NotNull(message = "Start time is required")
        @field:Future(message = "Start time must be in the future")
        @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        val startTime: LocalDateTime,
        
        @field:NotNull(message = "End time is required")
        @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        val endTime: LocalDateTime,
        
        @field:Size(max = 500, message = "Notes cannot exceed 500 characters")
        val notes: String? = null,
        
        val paymentMethod: PaymentMethod = PaymentMethod.CREDIT_CARD
    )
    
    data class BookingResponse(
        val id: UUID,
        val resourceId: UUID,
        val resourceName: String,
        val startTime: LocalDateTime,
        val endTime: LocalDateTime,
        val status: BookingStatus,
        val totalPrice: String,
        val notes: String?,
        val canBeCancelled: Boolean,
        val isPaid: Boolean,
        val createdAt: LocalDateTime
    )
    
    data class AvailabilityRequest(
        @field:NotNull(message = "Resource ID is required")
        val resourceId: UUID,
        
        @field:NotNull(message = "Start time is required")
        @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        val startTime: LocalDateTime,
        
        @field:NotNull(message = "End time is required")
        @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        val endTime: LocalDateTime
    )
    
    @Operation(summary = "Check availability", description = "Check if a resource is available for booking")
    @PostMapping("/check-availability")
    @PreAuthorize("hasRole('CUSTOMER')")
    suspend fun checkAvailability(@Valid @RequestBody request: AvailabilityRequest): ResponseEntity<AvailabilityResult> {
        val result = bookingService.checkAvailability(
            resourceId = request.resourceId,
            startTime = request.startTime,
            endTime = request.endTime
        )
        
        return ResponseEntity.ok(result)
    }
    
    @Operation(summary = "Create booking", description = "Create a new booking")
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    suspend fun createBooking(
        @Valid @RequestBody request: CreateBookingRequest,
        authentication: Authentication
    ): ResponseEntity<Any> {
        val userId = getUserId(authentication)
        
        val bookingRequest = BookingService.CreateBookingRequest(
            userId = userId,
            resourceId = request.resourceId,
            startTime = request.startTime,
            endTime = request.endTime,
            notes = request.notes,
            paymentMethod = request.paymentMethod
        )
        
        val result = bookingService.createBooking(bookingRequest)
        
        return if (result.isSuccess) {
            ResponseEntity.status(HttpStatus.CREATED).body(
                result.booking?.let { toBookingResponse(it) }
            )
        } else {
            ResponseEntity.badRequest().body(
                mapOf(
                    "error" to result.errorMessage,
                    "suggestedAlternatives" to result.suggestedAlternatives
                )
            )
        }
    }
    
    @Operation(summary = "Get user bookings", description = "Get paginated list of user's bookings")
    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    fun getMyBookings(
        @RequestParam(required = false) status: BookingStatus?,
        @PageableDefault(size = 20) pageable: Pageable,
        authentication: Authentication
    ): ResponseEntity<Page<BookingResponse>> {
        val userId = getUserId(authentication)
        
        val bookings = bookingService.getUserBookings(userId, status, pageable)
        val response = bookings.map { toBookingResponse(it) }
        
        return ResponseEntity.ok(response)
    }
    
    @Operation(summary = "Get booking details", description = "Get detailed information about a specific booking")
    @GetMapping("/{bookingId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('RESOURCE_MANAGER')")
    fun getBookingDetails(
        @Parameter(description = "Booking ID") @PathVariable bookingId: UUID,
        authentication: Authentication
    ): ResponseEntity<BookingResponse> {
        val booking = bookingService.getBookingById(bookingId)
            ?: return ResponseEntity.notFound().build()
        
        // Check authorization
        val userId = getUserId(authentication)
        val userRole = getUserRole(authentication)
        
        if (userRole != "RESOURCE_MANAGER" && booking.user.id != userId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        
        return ResponseEntity.ok(toBookingResponse(booking))
    }
    
    @Operation(summary = "Cancel booking", description = "Cancel an existing booking")
    @PutMapping("/{bookingId}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    fun cancelBooking(
        @Parameter(description = "Booking ID") @PathVariable bookingId: UUID,
        @RequestParam(required = false) reason: String?,
        authentication: Authentication
    ): ResponseEntity<Map<String, Any>> {
        val userId = getUserId(authentication)
        
        val success = bookingService.cancelBooking(bookingId, reason, userId)
        
        return if (success) {
            ResponseEntity.ok(mapOf("message" to "Booking cancelled successfully"))
        } else {
            ResponseEntity.badRequest().body(
                mapOf("error" to "Booking cannot be cancelled")
            )
        }
    }
    
    @Operation(summary = "Get booking statistics", description = "Get booking statistics for resource managers")
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('RESOURCE_MANAGER') or hasRole('SYSTEM_ADMIN')")
    fun getBookingStatistics(
        @RequestParam(required = false) resourceId: UUID?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDateTime?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDateTime?
    ): ResponseEntity<Map<String, Any>> {
        val statistics = bookingService.getBookingStatistics(resourceId, startDate, endDate)
        return ResponseEntity.ok(statistics)
    }
    
    private fun toBookingResponse(booking: Booking): BookingResponse {
        return BookingResponse(
            id = booking.id,
            resourceId = booking.resource.id,
            resourceName = booking.resource.name,
            startTime = booking.startTime,
            endTime = booking.endTime,
            status = booking.status,
            totalPrice = "$${booking.totalPrice}",
            notes = booking.notes,
            canBeCancelled = booking.canBeCancelled(),
            isPaid = booking.isPaid,
            createdAt = booking.createdAt
        )
    }
    
    private fun getUserId(authentication: Authentication): UUID {
        return UUID.fromString(authentication.name)
    }
    
    private fun getUserRole(authentication: Authentication): String {
        return authentication.authorities.first().authority.removePrefix("ROLE_")
    }
}
```

---

## 🛠️ Step 5: Testing Implementation

### **📝 TODO: Create BookingServiceTest.kt**
```kotlin
// src/test/kotlin/com/learning/booking/application/BookingServiceTest.kt
package com.learning.booking.application

import com.learning.booking.domain.booking.*
import com.learning.booking.domain.resource.*
import com.learning.booking.domain.user.*
import com.learning.booking.infrastructure.repository.jpa.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.DayOfWeek
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BookingServiceTest {
    
    private val bookingRepository = mockk<BookingRepository>()
    private val resourceRepository = mockk<BookableResourceRepository>()
    private val userRepository = mockk<UserRepository>()
    private val paymentService = mockk<PaymentService>()
    private val notificationService = mockk<NotificationService>()
    private val analyticsService = mockk<AnalyticsService>()
    private val eventPublisher = mockk<ApplicationEventPublisher>()
    
    private val bookingService = BookingService(
        bookingRepository,
        resourceRepository,
        userRepository,
        paymentService,
        notificationService,
        analyticsService,
        eventPublisher
    )
    
    private lateinit var testUser: User
    private lateinit var testResource: BookableResource
    private lateinit var testBooking: Booking
    
    @BeforeEach
    fun setUp() {
        clearAllMocks()
        
        testUser = User(
            id = UUID.randomUUID(),
            email = "test@example.com",
            firstName = "Test",
            lastName = "User",
            passwordHash = "hashedPassword",
            role = UserRole.CUSTOMER
        )
        
        testResource = BookableResource(
            id = UUID.randomUUID(),
            name = "Conference Room A",
            description = "Large conference room with projector",
            type = ResourceType.MEETING_ROOM,
            capacity = 12,
            pricePerHour = BigDecimal("50.00"),
            location = Location(
                address = "123 Main St",
                city = "New York",
                country = "USA"
            ),
            amenities = setOf(Amenity.PROJECTOR, Amenity.WIFI),
            manager = testUser,
            availabilityRules = setOf(
                AvailabilityRule(
                    resource = mockk(),
                    dayOfWeek = DayOfWeek.MONDAY,
                    startTime = LocalTime.of(9, 0),
                    endTime = LocalTime.of(17, 0),
                    isAvailable = true
                )
            )
        )
        
        testBooking = Booking(
            id = UUID.randomUUID(),
            user = testUser,
            resource = testResource,
            startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0),
            endTime = LocalDateTime.now().plusDays(1).withHour(12).withMinute(0),
            totalPrice = BigDecimal("100.00"),
            status = BookingStatus.PENDING
        )
    }
    
    @Nested
    @DisplayName("Availability Checking")
    inner class AvailabilityChecking {
        
        @Test
        @DisplayName("Should return available when no conflicts exist")
        fun shouldReturnAvailableWhenNoConflicts() = runBlocking {
            // Given
            val startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0)
            val endTime = startTime.plusHours(2)
            
            every { resourceRepository.findById(testResource.id) } returns testResource
            every { bookingRepository.findConflictingBookings(any(), any(), any(), any()) } returns emptyList()
            
            // When
            val result = bookingService.checkAvailability(testResource.id, startTime, endTime)
            
            // Then
            assertTrue(result.isAvailable)
            assertEquals(emptyList(), result.conflicts)
        }
        
        @Test
        @DisplayName("Should return unavailable when conflicts exist")
        fun shouldReturnUnavailableWhenConflictsExist() = runBlocking {
            // Given
            val startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0)
            val endTime = startTime.plusHours(2)
            val conflictingBooking = testBooking.copy(
                startTime = startTime.plusMinutes(30),
                endTime = endTime.plusMinutes(30),
                status = BookingStatus.CONFIRMED
            )
            
            every { resourceRepository.findById(testResource.id) } returns testResource
            every { bookingRepository.findConflictingBookings(any(), any(), any(), any()) } returns listOf(conflictingBooking)
            
            // When
            val result = bookingService.checkAvailability(testResource.id, startTime, endTime)
            
            // Then
            assertFalse(result.isAvailable)
            assertEquals(1, result.conflicts.size)
            assertEquals("Resource is already booked during this time", result.reason)
        }
        
        @Test
        @DisplayName("Should return unavailable when booking is too short")
        fun shouldReturnUnavailableWhenBookingTooShort() = runBlocking {
            // Given
            val startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0)
            val endTime = startTime.plusMinutes(15) // Less than minimum 30 minutes
            
            every { resourceRepository.findById(testResource.id) } returns testResource
            
            // When
            val result = bookingService.checkAvailability(testResource.id, startTime, endTime)
            
            // Then
            assertFalse(result.isAvailable)
            assertTrue(result.reason!!.contains("too short"))
        }
        
        @Test
        @DisplayName("Should return unavailable when booking is outside business hours")
        fun shouldReturnUnavailableWhenOutsideBusinessHours() = runBlocking {
            // Given
            val startTime = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0) // After 5 PM
            val endTime = startTime.plusHours(1)
            
            every { resourceRepository.findById(testResource.id) } returns testResource
            every { bookingRepository.findConflictingBookings(any(), any(), any(), any()) } returns emptyList()
            
            // When
            val result = bookingService.checkAvailability(testResource.id, startTime, endTime)
            
            // Then
            assertFalse(result.isAvailable)
            assertEquals("Booking is outside business hours", result.reason)
        }
    }
    
    @Nested
    @DisplayName("Booking Creation")
    inner class BookingCreation {
        
        @Test
        @DisplayName("Should create booking successfully with payment")
        fun shouldCreateBookingSuccessfullyWithPayment() = runBlocking {
            // Given
            val request = BookingService.CreateBookingRequest(
                userId = testUser.id,
                resourceId = testResource.id,
                startTime = testBooking.startTime,
                endTime = testBooking.endTime,
                paymentMethod = PaymentMethod.CREDIT_CARD
            )
            
            val paymentResult = PaymentResult(
                paymentId = UUID.randomUUID(),
                status = PaymentStatus.COMPLETED,
                message = "Payment successful"
            )
            
            // Mock all dependencies
            every { userRepository.findById(testUser.id) } returns testUser
            every { resourceRepository.findById(testResource.id) } returns testResource
            every { bookingRepository.findConflictingBookings(any(), any(), any(), any()) } returns emptyList()
            every { bookingRepository.save(any<Booking>()) } returns testBooking.copy(status = BookingStatus.CONFIRMED)
            coEvery { paymentService.processPayment(any()) } returns paymentResult
            every { notificationService.sendBookingConfirmation(any()) } just Runs
            every { analyticsService.recordBookingCreated(any()) } just Runs
            every { eventPublisher.publishEvent(any()) } just Runs
            
            // When
            val result = bookingService.createBooking(request)
            
            // Then
            assertTrue(result.isSuccess)
            assertEquals(BookingStatus.CONFIRMED, result.booking?.status)
            
            verify { bookingRepository.save(any<Booking>()) }
            verify { notificationService.sendBookingConfirmation(any()) }
            verify { analyticsService.recordBookingCreated(any()) }
        }
        
        @Test
        @DisplayName("Should handle payment failure gracefully")
        fun shouldHandlePaymentFailureGracefully() = runBlocking {
            // Given
            val request = BookingService.CreateBookingRequest(
                userId = testUser.id,
                resourceId = testResource.id,
                startTime = testBooking.startTime,
                endTime = testBooking.endTime
            )
            
            val paymentResult = PaymentResult(
                paymentId = null,
                status = PaymentStatus.FAILED,
                message = "Payment failed"
            )
            
            every { userRepository.findById(testUser.id) } returns testUser
            every { resourceRepository.findById(testResource.id) } returns testResource
            every { bookingRepository.findConflictingBookings(any(), any(), any(), any()) } returns emptyList()
            every { bookingRepository.save(any<Booking>()) } returnsArgument 0
            coEvery { paymentService.processPayment(any()) } returns paymentResult
            every { notificationService.sendPaymentFailedNotification(any()) } just Runs
            
            // When
            val result = bookingService.createBooking(request)
            
            // Then
            assertTrue(result.isSuccess)
            assertEquals(BookingStatus.CANCELLED, result.booking?.status)
            
            verify { notificationService.sendPaymentFailedNotification(any()) }
        }
    }
    
    @Nested
    @DisplayName("Booking Cancellation")
    inner class BookingCancellation {
        
        @Test
        @DisplayName("Should cancel booking successfully")
        fun shouldCancelBookingSuccessfully() {
            // Given
            val bookingId = testBooking.id
            val reason = "Change of plans"
            val userId = testUser.id
            
            val cancellableBooking = testBooking.copy(
                startTime = LocalDateTime.now().plusDays(2) // More than 24 hours in future
            )
            
            every { bookingRepository.findById(bookingId) } returns cancellableBooking
            every { bookingRepository.save(any<Booking>()) } returnsArgument 0
            every { paymentService.processRefund(any(), any()) } returns PaymentResult(
                paymentId = UUID.randomUUID(),
                status = PaymentStatus.REFUNDED
            )
            every { notificationService.sendBookingCancellation(any()) } just Runs
            every { analyticsService.recordBookingCancelled(any()) } just Runs
            
            // When
            val result = bookingService.cancelBooking(bookingId, reason, userId)
            
            // Then
            assertTrue(result)
            
            verify { bookingRepository.save(any<Booking>()) }
            verify { notificationService.sendBookingCancellation(any()) }
            verify { analyticsService.recordBookingCancelled(any()) }
        }
        
        @Test
        @DisplayName("Should not cancel booking less than 24 hours before start time")
        fun shouldNotCancelBookingLessThan24Hours() {
            // Given
            val bookingId = testBooking.id
            val userId = testUser.id
            
            val nonCancellableBooking = testBooking.copy(
                startTime = LocalDateTime.now().plusHours(12) // Less than 24 hours
            )
            
            every { bookingRepository.findById(bookingId) } returns nonCancellableBooking
            
            // When
            val result = bookingService.cancelBooking(bookingId, null, userId)
            
            // Then
            assertFalse(result)
            
            verify(exactly = 0) { bookingRepository.save(any<Booking>()) }
        }
    }
    
    @Nested
    @DisplayName("User Bookings Retrieval")
    inner class UserBookingsRetrieval {
        
        @Test
        @DisplayName("Should return paginated user bookings")
        fun shouldReturnPaginatedUserBookings() {
            // Given
            val userId = testUser.id
            val pageable = PageRequest.of(0, 10)
            val bookings = listOf(testBooking)
            val page = PageImpl(bookings, pageable, 1)
            
            every { bookingRepository.findByUserId(userId, pageable) } returns page
            
            // When
            val result = bookingService.getUserBookings(userId, null, pageable)
            
            // Then
            assertEquals(1, result.totalElements)
            assertEquals(testBooking.id, result.content[0].id)
        }
        
        @Test
        @DisplayName("Should filter bookings by status")
        fun shouldFilterBookingsByStatus() {
            // Given
            val userId = testUser.id
            val status = BookingStatus.CONFIRMED
            val pageable = PageRequest.of(0, 10)
            val bookings = listOf(testBooking.copy(status = status))
            val page = PageImpl(bookings, pageable, 1)
            
            every { bookingRepository.findByUserIdAndStatus(userId, status, pageable) } returns page
            
            // When
            val result = bookingService.getUserBookings(userId, status, pageable)
            
            // Then
            assertEquals(1, result.totalElements)
            assertEquals(status, result.content[0].status)
        }
    }
}
```

---

## 🛠️ Step 6: Integration Testing

### **📝 TODO: Create BookingIntegrationTest.kt**
```kotlin
// src/test/kotlin/com/learning/booking/integration/BookingIntegrationTest.kt
package com.learning.booking.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.learning.booking.BookingSystemApplication
import com.learning.booking.api.BookingController.*
import com.learning.booking.domain.booking.BookingStatus
import com.learning.booking.domain.booking.PaymentMethod
import com.learning.booking.domain.resource.ResourceType
import com.learning.booking.domain.user.UserRole
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@SpringBootTest(classes = [BookingSystemApplication::class])
@AutoConfigureWebMvc
@ActiveProfiles("test")
@TestPropertySource(properties = [
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.cache.type=simple",
    "logging.level.org.springframework.security=DEBUG"
])
@Transactional
class BookingIntegrationTest {
    
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    
    private lateinit var testResourceId: UUID
    private lateinit var testUserId: UUID
    
    @BeforeEach
    fun setUp() {
        // Setup test data
        testResourceId = UUID.randomUUID()
        testUserId = UUID.randomUUID()
        
        // Create test resource and user through repository or SQL
        setupTestData()
    }
    
    @Test
    @DisplayName("Should check availability successfully")
    @WithMockUser(roles = ["CUSTOMER"])
    fun shouldCheckAvailabilitySuccessfully() {
        // Given
        val request = AvailabilityRequest(
            resourceId = testResourceId,
            startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0),
            endTime = LocalDateTime.now().plusDays(1).withHour(12).withMinute(0)
        )
        
        // When & Then
        mockMvc.perform(
            post("/api/bookings/check-availability")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.isAvailable").value(true))
    }
    
    @Test
    @DisplayName("Should create booking successfully")
    @WithMockUser(roles = ["CUSTOMER"], username = "test-user-id")
    fun shouldCreateBookingSuccessfully() {
        // Given
        val request = CreateBookingRequest(
            resourceId = testResourceId,
            startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0),
            endTime = LocalDateTime.now().plusDays(1).withHour(12).withMinute(0),
            notes = "Team meeting",
            paymentMethod = PaymentMethod.CREDIT_CARD
        )
        
        // When & Then
        mockMvc.perform(
            post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isCreated)
        .andExpect(jsonPath("$.resourceId").value(testResourceId.toString()))
        .andExpect(jsonPath("$.status").value(BookingStatus.CONFIRMED.toString()))
        .andExpect(jsonPath("$.isPaid").value(true))
    }
    
    @Test
    @DisplayName("Should return bad request for invalid booking time")
    @WithMockUser(roles = ["CUSTOMER"])
    fun shouldReturnBadRequestForInvalidBookingTime() {
        // Given
        val request = CreateBookingRequest(
            resourceId = testResourceId,
            startTime = LocalDateTime.now().minusHours(1), // Past time
            endTime = LocalDateTime.now().plusHours(1),
            paymentMethod = PaymentMethod.CREDIT_CARD
        )
        
        // When & Then
        mockMvc.perform(
            post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isBadRequest)
    }
    
    @Test
    @DisplayName("Should get user bookings with pagination")
    @WithMockUser(roles = ["CUSTOMER"], username = "test-user-id")
    fun shouldGetUserBookingsWithPagination() {
        // Given - Create some test bookings
        createTestBookings()
        
        // When & Then
        mockMvc.perform(
            get("/api/bookings/my")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "createdAt,desc")
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.content").isArray)
        .andExpect(jsonPath("$.totalElements").isNumber)
        .andExpect(jsonPath("$.totalPages").isNumber)
    }
    
    @Test
    @DisplayName("Should filter bookings by status")
    @WithMockUser(roles = ["CUSTOMER"], username = "test-user-id")
    fun shouldFilterBookingsByStatus() {
        // Given
        createTestBookings()
        
        // When & Then
        mockMvc.perform(
            get("/api/bookings/my")
                .param("status", BookingStatus.CONFIRMED.toString())
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.content[*].status").value(everyItem(equalTo(BookingStatus.CONFIRMED.toString()))))
    }
    
    @Test
    @DisplayName("Should cancel booking successfully")
    @WithMockUser(roles = ["CUSTOMER"], username = "test-user-id")
    fun shouldCancelBookingSuccessfully() {
        // Given
        val bookingId = createCancellableBooking()
        
        // When & Then
        mockMvc.perform(
            put("/api/bookings/$bookingId/cancel")
                .param("reason", "Meeting cancelled")
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.message").value("Booking cancelled successfully"))
    }
    
    @Test
    @DisplayName("Should return forbidden for unauthorized access")
    @WithMockUser(roles = ["CUSTOMER"], username = "other-user-id")
    fun shouldReturnForbiddenForUnauthorizedAccess() {
        // Given
        val bookingId = createTestBookingForUser(testUserId)
        
        // When & Then
        mockMvc.perform(
            get("/api/bookings/$bookingId")
        )
        .andExpect(status().isForbidden)
    }
    
    @Test
    @DisplayName("Should get booking statistics for resource manager")
    @WithMockUser(roles = ["RESOURCE_MANAGER"])
    fun shouldGetBookingStatisticsForResourceManager() {
        // Given
        createTestBookings()
        
        // When & Then
        mockMvc.perform(
            get("/api/bookings/statistics")
                .param("resourceId", testResourceId.toString())
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.totalBookings").isNumber)
        .andExpect(jsonPath("$.totalRevenue").isNumber)
        .andExpect(jsonPath("$.averageBookingValue").isNumber)
    }
    
    @Test
    @DisplayName("Should return unauthorized for guest user")
    fun shouldReturnUnauthorizedForGuestUser() {
        // When & Then
        mockMvc.perform(
            get("/api/bookings/my")
        )
        .andExpect(status().isUnauthorized)
    }
    
    @Test
    @DisplayName("Should validate request body")
    @WithMockUser(roles = ["CUSTOMER"])
    fun shouldValidateRequestBody() {
        // Given - Invalid request with missing required fields
        val request = mapOf(
            "startTime" to LocalDateTime.now().plusDays(1),
            // Missing resourceId and endTime
        )
        
        // When & Then
        mockMvc.perform(
            post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isBadRequest)
    }
    
    private fun setupTestData() {
        // Setup test user, resource, and other required data
        // This would typically use TestEntityManager or repositories
        // For now, we'll mock the setup
    }
    
    private fun createTestBookings() {
        // Create test bookings for the authenticated user
    }
    
    private fun createCancellableBooking(): UUID {
        // Create a booking that can be cancelled (more than 24 hours in future)
        return UUID.randomUUID()
    }
    
    private fun createTestBookingForUser(userId: UUID): UUID {
        // Create a booking for a specific user
        return UUID.randomUUID()
    }
}
```

---

## 🛠️ Step 7: Deployment and Final Integration

### **📝 TODO: Create deployment scripts**
```bash
#!/bin/bash
# scripts/deploy-production.sh

set -e

echo "🚀 Deploying Booking System to Production"

# Configuration
PROJECT_NAME="booking-system"
ENVIRONMENT="production"
REGISTRY="ghcr.io/your-org"
VERSION=${1:-latest}

# Step 1: Build and test
echo "🧪 Running tests..."
./gradlew clean test

echo "📦 Building application..."
./gradlew build

# Step 2: Build Docker image
echo "🐳 Building Docker image..."
docker build -t ${REGISTRY}/${PROJECT_NAME}:${VERSION} .
docker push ${REGISTRY}/${PROJECT_NAME}:${VERSION}

# Step 3: Deploy infrastructure
echo "🏗️ Deploying infrastructure..."
cd terraform/environments/production
terraform init
terraform plan
terraform apply -auto-approve

# Step 4: Deploy to Kubernetes
echo "🎛️ Deploying to Kubernetes..."
cd ../../../
kubectl apply -f k8s/production/

# Step 5: Run smoke tests
echo "🔥 Running smoke tests..."
sleep 30 # Wait for deployment to stabilize
./scripts/smoke-tests.sh

echo "✅ Deployment completed successfully!"
```

### **📝 TODO: Create monitoring dashboard**
```yaml
# monitoring/booking-system-dashboard.json
{
  "dashboard": {
    "title": "Booking System - Production Metrics",
    "panels": [
      {
        "title": "Booking Rate",
        "type": "graph",
        "targets": [
          "rate(bookings_created_total[5m])",
          "rate(bookings_cancelled_total[5m])"
        ]
      },
      {
        "title": "Revenue Metrics",
        "type": "singlestat",
        "targets": [
          "sum(booking_revenue_total)"
        ]
      },
      {
        "title": "System Health",
        "type": "graph",
        "targets": [
          "up{job=\"booking-system\"}",
          "http_requests_duration_seconds{quantile=\"0.95\"}"
        ]
      }
    ]
  }
}
```

---

## 🎯 Expected Results

After completing this workshop, you'll have:

### **Complete Production System**
- ✅ **Domain-Driven Architecture** with proper separation of concerns
- ✅ **Advanced Security** with JWT, RBAC, and audit trails
- ✅ **High Performance** with multi-level caching and optimization
- ✅ **Comprehensive Testing** with unit, integration, and end-to-end tests
- ✅ **Production Monitoring** with metrics, logs, and alerts
- ✅ **Cloud Deployment** with auto-scaling and disaster recovery

### **Integration Validation**
```bash
# Test the complete booking flow
curl -X POST "https://api.booking-system.com/api/bookings" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "resourceId": "123e4567-e89b-12d3-a456-426614174000",
    "startTime": "2024-02-15T10:00:00",
    "endTime": "2024-02-15T12:00:00",
    "paymentMethod": "CREDIT_CARD"
  }'

# Expected Response:
{
  "id": "987fcdeb-51a2-43d7-8b9c-426614174000",
  "status": "CONFIRMED",
  "totalPrice": "$75.00",
  "isPaid": true,
  "canBeCancelled": true
}
```

---

## 🏆 Achievement Unlocked: Complete System Mastery

### **🎯 What You've Accomplished**

**Technical Mastery**: 
- Integrated all 19 lesson concepts into a cohesive enterprise system
- Implemented advanced patterns: DDD, CQRS, Event Sourcing, Microservices
- Built production-ready security, monitoring, and deployment infrastructure

**Business Value**: 
- Created a real-world booking platform serving actual business needs
- Demonstrated end-to-end system thinking and architecture skills
- Delivered measurable business metrics: revenue tracking, utilization analytics

**Professional Readiness**: 
- Portfolio-quality project showcasing complete full-stack competency
- Experience with enterprise development practices and tools
- Ready for senior backend developer and architect roles

**🚀 Next Steps**: You're now ready for professional Kotlin + Spring Boot development! Consider contributing to open source, building your own SaaS, or joining a team building enterprise systems.

---

## 🔥 Advanced Extensions

For those wanting to push further:

1. **🌐 Multi-tenancy**: Support multiple organizations with data isolation
2. **🔄 Event Sourcing**: Implement full event sourcing for audit and replay
3. **🤖 AI Integration**: Add AI-powered resource recommendations and pricing
4. **📱 Mobile App**: Build React Native or Flutter mobile application
5. **🌍 Global Scale**: Deploy across multiple regions with data replication

Congratulations on completing the KotlinKickStarter curriculum! 🎉