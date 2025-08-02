/**
 * Lesson 5 Workshop: Room DTOs (Data Transfer Objects)
 * 
 * TODO: Complete these DTO classes
 * This demonstrates:
 * - Separation between domain models and API contracts
 * - Request/Response DTOs for different operations
 * - Validation annotations
 * - DTO mapping patterns
 */

package com.learning.booking.dto

import com.learning.booking.model.Room
import com.learning.booking.model.RoomType
import jakarta.validation.constraints.*
import java.time.LocalDateTime

// TODO: Complete the Room response DTO
data class RoomResponseDTO(
    val id: String,
    val name: String,
    val roomType: String,
    val capacity: Int,
    val pricePerHour: Double,
    val amenities: List<String>,
    val isActive: Boolean,
    val createdAt: String // ISO formatted date
) {
    companion object {
        // TODO: Implement factory method to create DTO from domain model
        fun from(room: Room): RoomResponseDTO {
            return TODO("Convert Room domain model to RoomResponseDTO")
        }
    }
}

// TODO: Complete the Room creation request DTO
data class CreateRoomRequestDTO(
    // TODO: Add validation annotations
    @field:NotBlank(message = "Room name is required")
    @field:Size(min = 3, max = 50, message = "Room name must be between 3 and 50 characters")
    val name: String,
    
    @field:NotNull(message = "Room type is required")
    val roomType: String, // Will be converted to RoomType enum
    
    @field:Min(value = 1, message = "Capacity must be at least 1")
    @field:Max(value = 100, message = "Capacity cannot exceed 100")
    val capacity: Int,
    
    @field:DecimalMin(value = "0.0", message = "Price per hour cannot be negative")
    val pricePerHour: Double,
    
    val amenities: List<String> = emptyList()
) {
    // TODO: Implement method to convert to domain model
    fun toRoom(id: String): Room {
        return TODO("Convert CreateRoomRequestDTO to Room domain model")
    }
    
    // TODO: Add validation method for room type
    fun isValidRoomType(): Boolean {
        return TODO("Validate that roomType string corresponds to a valid RoomType enum")
    }
}

// TODO: Complete the Room update request DTO
data class UpdateRoomRequestDTO(
    @field:Size(min = 3, max = 50, message = "Room name must be between 3 and 50 characters")
    val name: String?,
    
    @field:Min(value = 1, message = "Capacity must be at least 1")
    @field:Max(value = 100, message = "Capacity cannot exceed 100")
    val capacity: Int?,
    
    @field:DecimalMin(value = "0.0", message = "Price per hour cannot be negative")
    val pricePerHour: Double?,
    
    val amenities: List<String>?,
    
    val isActive: Boolean?
) {
    // TODO: Implement method to apply updates to existing room
    fun applyTo(existingRoom: Room): Room {
        return TODO("Apply non-null fields to existing room and return updated room")
    }
}

// TODO: Complete the Room search/filter DTO
data class RoomSearchDTO(
    val roomType: String?,
    
    @field:Min(value = 1, message = "Minimum capacity must be at least 1")
    val minCapacity: Int?,
    
    @field:DecimalMax(value = "1000.0", message = "Maximum price per hour cannot exceed 1000")
    val maxPricePerHour: Double?,
    
    val amenities: List<String>?,
    
    val availableOnly: Boolean = true
) {
    // TODO: Add method to check if room matches search criteria
    fun matches(room: Room): Boolean {
        return TODO("Check if the given room matches all specified search criteria")
    }
}

// TODO: Complete the Room summary DTO (for list views)
data class RoomSummaryDTO(
    val id: String,
    val name: String,
    val roomType: String,
    val capacity: Int,
    val pricePerHour: Double,
    val currentStatus: String // AVAILABLE, OCCUPIED, etc.
) {
    companion object {
        fun from(room: Room, currentStatus: String): RoomSummaryDTO {
            return TODO("Convert Room to summary DTO with current status")
        }
    }
}