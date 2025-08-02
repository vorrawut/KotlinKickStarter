/**
 * Lesson 5 Workshop: Reservation DTOs (Data Transfer Objects)
 * 
 * TODO: Complete these DTO classes
 * This demonstrates:
 * - Complex DTOs with date/time handling
 * - Cross-validation between fields
 * - Different DTOs for different API operations
 */

package com.learning.booking.dto

import com.learning.booking.model.Reservation
import com.learning.booking.model.ReservationStatus
import jakarta.validation.constraints.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// TODO: Complete the Reservation response DTO
data class ReservationResponseDTO(
    val id: String,
    val roomId: String,
    val roomName: String, // Include room info for convenience
    val customerName: String,
    val customerEmail: String,
    val startTime: String, // ISO formatted
    val endTime: String, // ISO formatted
    val guestCount: Int,
    val totalCost: Double,
    val status: String,
    val notes: String?,
    val durationHours: Long,
    val createdAt: String
) {
    companion object {
        // TODO: Implement factory method to create DTO from domain model
        fun from(reservation: Reservation, roomName: String): ReservationResponseDTO {
            return TODO("Convert Reservation domain model to ReservationResponseDTO")
        }
    }
}

// TODO: Complete the Reservation creation request DTO
data class CreateReservationRequestDTO(
    @field:NotBlank(message = "Room ID is required")
    val roomId: String,
    
    @field:NotBlank(message = "Customer name is required")
    @field:Size(min = 2, max = 100, message = "Customer name must be between 2 and 100 characters")
    val customerName: String,
    
    @field:NotBlank(message = "Customer email is required")
    @field:Email(message = "Customer email must be valid")
    val customerEmail: String,
    
    @field:NotNull(message = "Start time is required")
    val startTime: String, // ISO formatted, will be parsed to LocalDateTime
    
    @field:NotNull(message = "End time is required")
    val endTime: String, // ISO formatted, will be parsed to LocalDateTime
    
    @field:Min(value = 1, message = "Guest count must be at least 1")
    @field:Max(value = 100, message = "Guest count cannot exceed 100")
    val guestCount: Int,
    
    @field:Size(max = 500, message = "Notes cannot exceed 500 characters")
    val notes: String? = null
) {
    // TODO: Implement method to convert to domain model
    fun toReservation(id: String, totalCost: Double): Reservation {
        return TODO("Convert CreateReservationRequestDTO to Reservation domain model")
    }
    
    // TODO: Add validation methods
    fun parseStartTime(): LocalDateTime {
        return TODO("Parse startTime string to LocalDateTime")
    }
    
    fun parseEndTime(): LocalDateTime {
        return TODO("Parse endTime string to LocalDateTime")
    }
    
    fun isValidTimeRange(): Boolean {
        return TODO("Validate that start time is before end time and both are in the future")
    }
    
    fun getDurationHours(): Long {
        return TODO("Calculate duration in hours between start and end time")
    }
}

// TODO: Complete the Reservation update request DTO
data class UpdateReservationRequestDTO(
    @field:Size(min = 2, max = 100, message = "Customer name must be between 2 and 100 characters")
    val customerName: String?,
    
    @field:Email(message = "Customer email must be valid")
    val customerEmail: String?,
    
    val startTime: String?, // ISO formatted
    val endTime: String?, // ISO formatted
    
    @field:Min(value = 1, message = "Guest count must be at least 1")
    @field:Max(value = 100, message = "Guest count cannot exceed 100")
    val guestCount: Int?,
    
    @field:Size(max = 500, message = "Notes cannot exceed 500 characters")
    val notes: String?
) {
    // TODO: Implement method to apply updates to existing reservation
    fun applyTo(existingReservation: Reservation): Reservation {
        return TODO("Apply non-null fields to existing reservation")
    }
    
    fun hasTimeChanges(): Boolean {
        return TODO("Check if startTime or endTime is being updated")
    }
}

// TODO: Complete the Reservation search/filter DTO
data class ReservationSearchDTO(
    val roomId: String?,
    val customerEmail: String?,
    val status: String?,
    val startDate: String?, // Date only, for filtering by date range
    val endDate: String?, // Date only, for filtering by date range
    val minGuestCount: Int?,
    val includeCompleted: Boolean = false
) {
    // TODO: Add method to check if reservation matches search criteria
    fun matches(reservation: Reservation): Boolean {
        return TODO("Check if the given reservation matches all specified search criteria")
    }
    
    fun parseStartDate(): LocalDateTime? {
        return TODO("Parse startDate string to LocalDateTime (start of day)")
    }
    
    fun parseEndDate(): LocalDateTime? {
        return TODO("Parse endDate string to LocalDateTime (end of day)")
    }
}

// TODO: Complete the Reservation summary DTO (for calendar views)
data class ReservationSummaryDTO(
    val id: String,
    val roomName: String,
    val customerName: String,
    val startTime: String,
    val endTime: String,
    val guestCount: Int,
    val status: String
) {
    companion object {
        fun from(reservation: Reservation, roomName: String): ReservationSummaryDTO {
            return TODO("Convert Reservation to summary DTO")
        }
    }
}

// TODO: Complete availability check DTO
data class AvailabilityCheckDTO(
    @field:NotNull(message = "Start time is required")
    val startTime: String,
    
    @field:NotNull(message = "End time is required")
    val endTime: String,
    
    @field:Min(value = 1, message = "Guest count must be at least 1")
    val guestCount: Int,
    
    val roomType: String? = null
) {
    fun parseStartTime(): LocalDateTime = TODO("Parse start time")
    fun parseEndTime(): LocalDateTime = TODO("Parse end time")
    fun isValidTimeRange(): Boolean = TODO("Validate time range")
}