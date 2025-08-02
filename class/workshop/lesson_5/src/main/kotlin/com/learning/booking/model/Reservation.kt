/**
 * Lesson 5 Workshop: Reservation Domain Model
 * 
 * TODO: Complete this domain model class
 * This demonstrates:
 * - Complex data classes with relationships
 * - Date/time handling
 * - Business logic in domain models
 */

package com.learning.booking.model

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

data class Reservation(
    // TODO: Add reservation properties
    val id: String,
    val roomId: String,
    val customerName: String,
    val customerEmail: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val guestCount: Int,
    val totalCost: Double,
    val status: TODO("Define reservation status"),
    val notes: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    
    // TODO: Add computed properties
    val durationHours: Long
        get() = TODO("Calculate duration in hours between start and end time")
    
    val isUpcoming: Boolean
        get() = TODO("Check if reservation is in the future")
    
    val isActive: Boolean
        get() = TODO("Check if reservation is currently active")
    
    // TODO: Add validation methods
    fun isValidTimeSlot(): Boolean {
        return TODO("Validate that start time is before end time and in the future")
    }
    
    fun overlaps(other: Reservation): Boolean {
        return TODO("Check if this reservation overlaps with another reservation")
    }
    
    fun canBeModified(): Boolean {
        return TODO("Check if reservation can still be modified (e.g., not started yet)")
    }
    
    fun canBeCancelled(): Boolean {
        return TODO("Check if reservation can be cancelled")
    }
}

// TODO: Define ReservationStatus enum
enum class ReservationStatus(val displayName: String) {
    TODO("Define statuses like PENDING, CONFIRMED, CANCELLED, COMPLETED, etc.")
}