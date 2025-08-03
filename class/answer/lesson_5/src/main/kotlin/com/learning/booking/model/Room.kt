/**
 * Lesson 5 Complete Solution: Room Domain Model
 */

package com.learning.booking.model

import java.math.BigDecimal

data class Room(
    val id: Long,
    val name: String,
    val type: RoomType,
    val capacity: Int,
    val pricePerHour: BigDecimal,
    val amenities: List<String>,
    val isAvailable: Boolean = true
) {
    fun canAccommodate(guests: Int): Boolean {
        return guests <= capacity
    }
    
    fun calculateCost(hours: Int): BigDecimal {
        return pricePerHour.multiply(BigDecimal(hours))
    }
}

enum class RoomType(val displayName: String) {
    CONFERENCE("Conference Room"),
    MEETING("Meeting Room"),
    TRAINING("Training Room"),
    BOARDROOM("Board Room"),
    PHONE_BOOTH("Phone Booth")
}