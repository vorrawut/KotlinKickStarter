/**
 * Lesson 5 Complete Solution: Room Controller
 */

package com.learning.booking.controller

import com.learning.booking.dto.*
import com.learning.booking.model.RoomType
import com.learning.booking.service.RoomService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/api/rooms")
class RoomController(
    private val roomService: RoomService
) {
    
    @PostMapping
    fun createRoom(@Valid @RequestBody request: CreateRoomRequest): ResponseEntity<RoomResponse> {
        val response = roomService.createRoom(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
    
    @GetMapping
    fun getAllRooms(): ResponseEntity<List<RoomResponse>> {
        val rooms = roomService.getAllRooms()
        return ResponseEntity.ok(rooms)
    }
    
    @GetMapping("/{id}")
    fun getRoomById(@PathVariable id: Long): ResponseEntity<RoomResponse> {
        val room = roomService.getRoomById(id)
        return if (room != null) {
            ResponseEntity.ok(room)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @PutMapping("/{id}")
    fun updateRoom(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateRoomRequest
    ): ResponseEntity<RoomResponse> {
        val updatedRoom = roomService.updateRoom(id, request)
        return if (updatedRoom != null) {
            ResponseEntity.ok(updatedRoom)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @DeleteMapping("/{id}")
    fun deleteRoom(@PathVariable id: Long): ResponseEntity<Void> {
        val deleted = roomService.deleteRoom(id)
        return if (deleted) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @GetMapping("/search")
    fun searchRooms(
        @RequestParam(required = false) type: RoomType?,
        @RequestParam(required = false) minCapacity: Int?,
        @RequestParam(required = false) maxPricePerHour: BigDecimal?
    ): ResponseEntity<List<RoomResponse>> {
        val rooms = roomService.searchRooms(type, minCapacity, maxPricePerHour)
        return ResponseEntity.ok(rooms)
    }
    
    @PostMapping("/{id}/check-availability")
    fun checkAvailability(
        @PathVariable id: Long,
        @Valid @RequestBody request: RoomAvailabilityRequest
    ): ResponseEntity<Map<String, Any>> {
        val isAvailable = roomService.checkAvailability(id, request)
        val response = mapOf(
            "roomId" to id,
            "available" to isAvailable,
            "message" to if (isAvailable) "Room is available" else "Room is not available"
        )
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/type/{type}")
    fun getRoomsByType(@PathVariable type: RoomType): ResponseEntity<List<RoomResponse>> {
        val rooms = roomService.getRoomsByType(type)
        return ResponseEntity.ok(rooms)
    }
    
    @GetMapping("/available")
    fun getAvailableRooms(): ResponseEntity<List<RoomResponse>> {
        val rooms = roomService.getAvailableRooms()
        return ResponseEntity.ok(rooms)
    }
}