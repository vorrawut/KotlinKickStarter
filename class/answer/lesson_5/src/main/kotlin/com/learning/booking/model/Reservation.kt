/**
 * Lesson 5 Complete Solution: Reservation Domain Model
 */

package com.learning.booking.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class Reservation(
    val id: Long,
    val roomId: Long,
    val guestName: String,
    val guestEmail: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val numberOfGuests: Int,
    val purpose: String,
    val status: ReservationStatus,
    val totalCost: BigDecimal,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun getDurationHours(): Long {
        return java.time.Duration.between(startTime, endTime).toHours()
    }
    
    fun isActive(): Boolean {
        return status == ReservationStatus.CONFIRMED && 
               startTime.isBefore(LocalDateTime.now()) && 
               endTime.isAfter(LocalDateTime.now())
    }
    
    fun isUpcoming(): Boolean {
        return status == ReservationStatus.CONFIRMED && 
               startTime.isAfter(LocalDateTime.now())
    }
    
    fun canBeCancelled(): Boolean {
        return status == ReservationStatus.CONFIRMED && 
               startTime.isAfter(LocalDateTime.now())
    }
}

enum class ReservationStatus(val displayName: String) {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    CANCELLED("Cancelled"),
    COMPLETED("Completed"),
    NO_SHOW("No Show")
}