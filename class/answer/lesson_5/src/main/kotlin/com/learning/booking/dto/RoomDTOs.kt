/**
 * Lesson 5 Complete Solution: Room DTOs
 */

package com.learning.booking.dto

import com.learning.booking.model.Room
import com.learning.booking.model.RoomType
import jakarta.validation.constraints.*
import java.math.BigDecimal

// Request DTOs
data class CreateRoomRequest(
    @field:NotBlank(message = "Room name is required")
    @field:Size(min = 2, max = 100, message = "Room name must be 2-100 characters")
    val name: String,
    
    @field:NotNull(message = "Room type is required")
    val type: RoomType,
    
    @field:Min(value = 1, message = "Capacity must be at least 1")
    @field:Max(value = 100, message = "Capacity cannot exceed 100")
    val capacity: Int,
    
    @field:NotNull(message = "Price per hour is required")
    @field:Positive(message = "Price must be positive")
    val pricePerHour: BigDecimal,
    
    @field:Size(max = 10, message = "Maximum 10 amenities allowed")
    val amenities: List<String> = emptyList()
)

data class UpdateRoomRequest(
    @field:Size(min = 2, max = 100, message = "Room name must be 2-100 characters")
    val name: String?,
    
    @field:Min(value = 1, message = "Capacity must be at least 1")
    @field:Max(value = 100, message = "Capacity cannot exceed 100")
    val capacity: Int?,
    
    @field:Positive(message = "Price must be positive")
    val pricePerHour: BigDecimal?,
    
    @field:Size(max = 10, message = "Maximum 10 amenities allowed")
    val amenities: List<String>?,
    
    val isAvailable: Boolean?
)

// Response DTOs
data class RoomResponse(
    val id: Long,
    val name: String,
    val type: String,
    val capacity: Int,
    val pricePerHour: BigDecimal,
    val amenities: List<String>,
    val isAvailable: Boolean
) {
    companion object {
        fun from(room: Room): RoomResponse {
            return RoomResponse(
                id = room.id,
                name = room.name,
                type = room.type.displayName,
                capacity = room.capacity,
                pricePerHour = room.pricePerHour,
                amenities = room.amenities,
                isAvailable = room.isAvailable
            )
        }
    }
}

data class RoomSummaryResponse(
    val id: Long,
    val name: String,
    val type: String,
    val capacity: Int,
    val isAvailable: Boolean
)

data class RoomAvailabilityRequest(
    @field:NotNull(message = "Start time is required")
    val startTime: String,
    
    @field:NotNull(message = "End time is required")
    val endTime: String,
    
    @field:Min(value = 1, message = "Number of guests must be at least 1")
    val numberOfGuests: Int
)