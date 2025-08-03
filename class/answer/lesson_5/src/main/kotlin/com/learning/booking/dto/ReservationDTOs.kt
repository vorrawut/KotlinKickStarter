/**
 * Lesson 5 Complete Solution: Reservation DTOs
 */

package com.learning.booking.dto

import com.learning.booking.model.Reservation
import com.learning.booking.model.ReservationStatus
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Request DTOs
data class CreateReservationRequest(
    @field:NotNull(message = "Room ID is required")
    @field:Positive(message = "Room ID must be positive")
    val roomId: Long,
    
    @field:NotBlank(message = "Guest name is required")
    @field:Size(min = 2, max = 100, message = "Guest name must be 2-100 characters")
    val guestName: String,
    
    @field:NotBlank(message = "Guest email is required")
    @field:Email(message = "Invalid email format")
    val guestEmail: String,
    
    @field:NotNull(message = "Start time is required")
    val startTime: String,
    
    @field:NotNull(message = "End time is required")
    val endTime: String,
    
    @field:Min(value = 1, message = "Number of guests must be at least 1")
    val numberOfGuests: Int,
    
    @field:NotBlank(message = "Purpose is required")
    @field:Size(min = 5, max = 500, message = "Purpose must be 5-500 characters")
    val purpose: String
) {
    fun getStartDateTime(): LocalDateTime {
        return LocalDateTime.parse(startTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }
    
    fun getEndDateTime(): LocalDateTime {
        return LocalDateTime.parse(endTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }
    
    fun isValidTimeRange(): Boolean {
        return try {
            val start = getStartDateTime()
            val end = getEndDateTime()
            start.isBefore(end) && start.isAfter(LocalDateTime.now())
        } catch (e: Exception) {
            false
        }
    }
}

data class UpdateReservationRequest(
    @field:Size(min = 2, max = 100, message = "Guest name must be 2-100 characters")
    val guestName: String?,
    
    @field:Email(message = "Invalid email format")
    val guestEmail: String?,
    
    val startTime: String?,
    val endTime: String?,
    
    @field:Min(value = 1, message = "Number of guests must be at least 1")
    val numberOfGuests: Int?,
    
    @field:Size(min = 5, max = 500, message = "Purpose must be 5-500 characters")
    val purpose: String?,
    
    val status: ReservationStatus?
)

// Response DTOs
data class ReservationResponse(
    val id: Long,
    val roomId: Long,
    val roomName: String,
    val guestName: String,
    val guestEmail: String,
    val startTime: String,
    val endTime: String,
    val numberOfGuests: Int,
    val purpose: String,
    val status: String,
    val totalCost: BigDecimal,
    val durationHours: Long,
    val createdAt: String,
    val isActive: Boolean,
    val isUpcoming: Boolean,
    val canBeCancelled: Boolean
) {
    companion object {
        fun from(reservation: Reservation, roomName: String): ReservationResponse {
            return ReservationResponse(
                id = reservation.id,
                roomId = reservation.roomId,
                roomName = roomName,
                guestName = reservation.guestName,
                guestEmail = reservation.guestEmail,
                startTime = reservation.startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                endTime = reservation.endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                numberOfGuests = reservation.numberOfGuests,
                purpose = reservation.purpose,
                status = reservation.status.displayName,
                totalCost = reservation.totalCost,
                durationHours = reservation.getDurationHours(),
                createdAt = reservation.createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                isActive = reservation.isActive(),
                isUpcoming = reservation.isUpcoming(),
                canBeCancelled = reservation.canBeCancelled()
            )
        }
    }
}

data class ReservationSummaryResponse(
    val id: Long,
    val roomName: String,
    val guestName: String,
    val startTime: String,
    val endTime: String,
    val status: String
)