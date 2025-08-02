/**
 * Lesson 5 Workshop: Room Domain Model
 * 
 * TODO: Complete this domain model class
 * This demonstrates:
 * - Data classes for domain modeling
 * - Enums for type safety
 * - Property validation
 */

package com.learning.booking.model

import java.time.LocalDateTime

data class Room(
    // TODO: Add room properties
    val id: String,
    val name: String,
    val roomType: TODO("Define room type enum"),
    val capacity: Int,
    val pricePerHour: Double,
    val amenities: List<String> = emptyList(),
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    
    // TODO: Add validation methods
    fun isAvailable(): Boolean {
        return TODO("Check if room is active and available")
    }
    
    fun canAccommodate(guestCount: Int): Boolean {
        return TODO("Check if room can accommodate the number of guests")
    }
    
    fun calculateCost(hours: Int): Double {
        return TODO("Calculate total cost for given hours")
    }
}

// TODO: Define RoomType enum
enum class RoomType(val displayName: String, val baseCapacity: Int) {
    TODO("Define room types like CONFERENCE, MEETING, WORKSHOP, etc.")
}

// TODO: Define RoomStatus enum for availability
enum class RoomStatus {
    TODO("Define statuses like AVAILABLE, OCCUPIED, MAINTENANCE, etc.")
}