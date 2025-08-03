/**
 * Lesson 5 Complete Solution: Room Service
 */

package com.learning.booking.service

import com.learning.booking.dto.*
import com.learning.booking.model.Room
import com.learning.booking.model.RoomType
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicLong

@Service
class RoomService {
    
    private val rooms = mutableMapOf<Long, Room>()
    private val idCounter = AtomicLong(1)
    
    init {
        // Initialize with sample data
        val sampleRooms = listOf(
            Room(1L, "Conference Room A", RoomType.CONFERENCE, 12, BigDecimal("25.00"), 
                 listOf("Projector", "Whiteboard", "Video Conferencing")),
            Room(2L, "Meeting Room B", RoomType.MEETING, 6, BigDecimal("15.00"), 
                 listOf("TV Screen", "Whiteboard")),
            Room(3L, "Training Room C", RoomType.TRAINING, 20, BigDecimal("30.00"), 
                 listOf("Projector", "Audio System", "Flipchart")),
            Room(4L, "Board Room", RoomType.BOARDROOM, 8, BigDecimal("50.00"), 
                 listOf("Conference Table", "Projector", "Video Conferencing", "Catering Setup")),
            Room(5L, "Phone Booth 1", RoomType.PHONE_BOOTH, 1, BigDecimal("5.00"), 
                 listOf("Soundproof", "Phone"))
        )
        
        sampleRooms.forEach { room ->
            rooms[room.id] = room
            idCounter.set(maxOf(idCounter.get(), room.id + 1))
        }
    }
    
    fun createRoom(request: CreateRoomRequest): RoomResponse {
        val room = Room(
            id = idCounter.getAndIncrement(),
            name = request.name,
            type = request.type,
            capacity = request.capacity,
            pricePerHour = request.pricePerHour,
            amenities = request.amenities,
            isAvailable = true
        )
        
        rooms[room.id] = room
        return RoomResponse.from(room)
    }
    
    fun getAllRooms(): List<RoomResponse> {
        return rooms.values.map { RoomResponse.from(it) }
    }
    
    fun getRoomById(id: Long): RoomResponse? {
        return rooms[id]?.let { RoomResponse.from(it) }
    }
    
    fun updateRoom(id: Long, request: UpdateRoomRequest): RoomResponse? {
        val existingRoom = rooms[id] ?: return null
        
        val updatedRoom = existingRoom.copy(
            name = request.name ?: existingRoom.name,
            capacity = request.capacity ?: existingRoom.capacity,
            pricePerHour = request.pricePerHour ?: existingRoom.pricePerHour,
            amenities = request.amenities ?: existingRoom.amenities,
            isAvailable = request.isAvailable ?: existingRoom.isAvailable
        )
        
        rooms[id] = updatedRoom
        return RoomResponse.from(updatedRoom)
    }
    
    fun deleteRoom(id: Long): Boolean {
        return rooms.remove(id) != null
    }
    
    fun searchRooms(type: RoomType?, minCapacity: Int?, maxPricePerHour: BigDecimal?): List<RoomResponse> {
        return rooms.values
            .filter { room ->
                (type == null || room.type == type) &&
                (minCapacity == null || room.capacity >= minCapacity) &&
                (maxPricePerHour == null || room.pricePerHour <= maxPricePerHour) &&
                room.isAvailable
            }
            .map { RoomResponse.from(it) }
    }
    
    fun checkAvailability(roomId: Long, request: RoomAvailabilityRequest): Boolean {
        val room = rooms[roomId] ?: return false
        
        if (!room.isAvailable || !room.canAccommodate(request.numberOfGuests)) {
            return false
        }
        
        // For this lesson, we'll simulate availability check
        // In a real application, this would check against actual reservations
        return try {
            val startTime = LocalDateTime.parse(request.startTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val endTime = LocalDateTime.parse(request.endTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            startTime.isBefore(endTime) && startTime.isAfter(LocalDateTime.now())
        } catch (e: Exception) {
            false
        }
    }
    
    fun getRoomsByType(type: RoomType): List<RoomResponse> {
        return rooms.values
            .filter { it.type == type && it.isAvailable }
            .map { RoomResponse.from(it) }
    }
    
    fun getAvailableRooms(): List<RoomResponse> {
        return rooms.values
            .filter { it.isAvailable }
            .map { RoomResponse.from(it) }
    }
    
    // Internal method for other services
    internal fun getRoomModel(id: Long): Room? {
        return rooms[id]
    }
}