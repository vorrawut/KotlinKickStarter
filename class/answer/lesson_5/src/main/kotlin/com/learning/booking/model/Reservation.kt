/**
 * Lesson 5 Complete Solution: Reservation Domain Model
 */

package com.learning.booking.model

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

data class Reservation(
    val id: String,
    val roomId: String,
    val customerName: String,
    val customerEmail: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val guestCount: Int,
    val totalCost: Double,
    val status: ReservationStatus,
    val notes: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    
    val durationHours: Long
        get() = ChronoUnit.HOURS.between(startTime, endTime)
    
    val isUpcoming: Boolean
        get() = startTime.isAfter(LocalDateTime.now())
    
    val isActive: Boolean
        get() = LocalDateTime.now().let { now ->
            now.isAfter(startTime) && now.isBefore(endTime)
        }
    
    fun isValidTimeSlot(): Boolean {
        return startTime.isBefore(endTime) && startTime.isAfter(LocalDateTime.now())
    }
    
    fun overlaps(other: Reservation): Boolean {
        return roomId == other.roomId &&
                startTime.isBefore(other.endTime) &&
                endTime.isAfter(other.startTime)
    }
    
    fun canBeModified(): Boolean {
        return status == ReservationStatus.PENDING && isUpcoming
    }
    
    fun canBeCancelled(): Boolean {
        return status in listOf(ReservationStatus.PENDING, ReservationStatus.CONFIRMED) && 
               startTime.isAfter(LocalDateTime.now().plusHours(1))
    }
}

enum class ReservationStatus(val displayName: String) {
    PENDING("Pending Confirmation"),
    CONFIRMED("Confirmed"),
    CANCELLED("Cancelled"),
    COMPLETED("Completed"),
    NO_SHOW("No Show")
}