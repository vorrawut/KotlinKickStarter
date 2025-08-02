/**
 * Lesson 5 Complete Solution: Room Domain Model
 */

package com.learning.booking.model

import java.time.LocalDateTime

data class Room(
    val id: String,
    val name: String,
    val roomType: RoomType,
    val capacity: Int,
    val pricePerHour: Double,
    val amenities: List<String> = emptyList(),
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    
    fun isAvailable(): Boolean {
        return isActive
    }
    
    fun canAccommodate(guestCount: Int): Boolean {
        return guestCount <= capacity
    }
    
    fun calculateCost(hours: Int): Double {
        return hours * pricePerHour
    }
}

enum class RoomType(val displayName: String, val baseCapacity: Int) {
    CONFERENCE("Conference Room", 20),
    MEETING("Meeting Room", 8),
    WORKSHOP("Workshop Room", 30),
    TRAINING("Training Room", 25),
    PRESENTATION("Presentation Room", 50)
}

enum class RoomStatus {
    AVAILABLE,
    OCCUPIED,
    MAINTENANCE,
    RESERVED
}