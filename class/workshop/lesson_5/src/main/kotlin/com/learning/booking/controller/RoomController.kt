/**
 * Lesson 5 Workshop: Room REST Controller
 * 
 * TODO: Complete this REST controller
 * This demonstrates:
 * - @RestController and @RequestMapping annotations
 * - HTTP methods mapping (GET, POST, PUT, DELETE)
 * - Path variables and request parameters
 * - Request/Response DTOs
 * - HTTP status codes
 * - Exception handling
 */

package com.learning.booking.controller

import com.learning.booking.dto.*
import com.learning.booking.model.Room
import com.learning.booking.service.RoomService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

// TODO: Add @RestController annotation
// TODO: Add @RequestMapping("/api/rooms") annotation
class RoomController(
    private val roomService: RoomService
) {
    
    // TODO: GET /api/rooms - Get all rooms or search
    // Add @GetMapping annotation
    fun getAllRooms(
        @RequestParam(required = false) roomType: String?,
        @RequestParam(required = false) minCapacity: Int?,
        @RequestParam(required = false) maxPricePerHour: Double?,
        @RequestParam(required = false) amenities: List<String>?,
        @RequestParam(defaultValue = "true") availableOnly: Boolean
    ): ResponseEntity<List<RoomResponseDTO>> {
        
        // TODO: Create search criteria from request parameters
        val searchCriteria = TODO("Create RoomSearchDTO from request parameters")
        
        // TODO: Search rooms using service
        val rooms = TODO("Call roomService.searchRooms or getAllRooms")
        
        // TODO: Convert to response DTOs
        val roomDTOs = TODO("Convert List<Room> to List<RoomResponseDTO>")
        
        // TODO: Return ResponseEntity with appropriate status
        return TODO("Return ResponseEntity.ok(roomDTOs)")
    }
    
    // TODO: GET /api/rooms/{id} - Get room by ID
    // Add @GetMapping("/{id}") annotation
    fun getRoomById(@PathVariable id: String): ResponseEntity<RoomResponseDTO> {
        
        // TODO: Find room by ID
        val room = TODO("Call roomService.getRoomById")
        
        // TODO: Return room or 404 if not found
        return TODO("Return ResponseEntity with room DTO or NOT_FOUND status")
    }
    
    // TODO: POST /api/rooms - Create new room
    // Add @PostMapping annotation
    fun createRoom(
        @Valid @RequestBody request: CreateRoomRequestDTO
    ): ResponseEntity<RoomResponseDTO> {
        
        return try {
            // TODO: Create room using service
            val createdRoom = TODO("Call roomService.createRoom")
            
            // TODO: Convert to response DTO
            val responseDTO = TODO("Convert Room to RoomResponseDTO")
            
            // TODO: Return 201 CREATED with location header
            TODO("Return ResponseEntity with CREATED status and response DTO")
            
        } catch (e: Exception) {
            // TODO: Handle creation errors
            TODO("Return appropriate error response")
        }
    }
    
    // TODO: PUT /api/rooms/{id} - Update existing room
    // Add @PutMapping("/{id}") annotation
    fun updateRoom(
        @PathVariable id: String,
        @Valid @RequestBody request: UpdateRoomRequestDTO
    ): ResponseEntity<RoomResponseDTO> {
        
        // TODO: Update room using service
        val updatedRoom = TODO("Call roomService.updateRoom")
        
        // TODO: Return updated room or 404 if not found
        return TODO("Return ResponseEntity with updated room DTO or NOT_FOUND status")
    }
    
    // TODO: DELETE /api/rooms/{id} - Delete room
    // Add @DeleteMapping("/{id}") annotation
    fun deleteRoom(@PathVariable id: String): ResponseEntity<Void> {
        
        // TODO: Delete room using service
        val deleted = TODO("Call roomService.deleteRoom")
        
        // TODO: Return appropriate status
        return TODO("Return NO_CONTENT if deleted, NOT_FOUND if room doesn't exist")
    }
    
    // TODO: GET /api/rooms/summary - Get room summaries
    // Add @GetMapping("/summary") annotation
    fun getRoomSummaries(): ResponseEntity<List<RoomSummaryDTO>> {
        
        // TODO: Get all rooms and convert to summaries
        val rooms = TODO("Get all rooms from service")
        
        // TODO: Convert to summary DTOs (you'll need to determine current status)
        val summaries = TODO("Convert to RoomSummaryDTO list")
        
        return TODO("Return ResponseEntity.ok(summaries)")
    }
    
    // TODO: GET /api/rooms/statistics - Get room statistics
    // Add @GetMapping("/statistics") annotation
    fun getRoomStatistics(): ResponseEntity<Map<String, Any>> {
        
        // TODO: Get statistics from service
        val statistics = TODO("Call roomService.getRoomStatistics")
        
        return TODO("Return ResponseEntity.ok(statistics)")
    }
    
    // TODO: GET /api/rooms/types - Get available room types
    // Add @GetMapping("/types") annotation
    fun getRoomTypes(): ResponseEntity<List<Map<String, Any>>> {
        
        // TODO: Return available room types with their details
        val roomTypes = TODO("Create list of room type information")
        
        return TODO("Return ResponseEntity.ok(roomTypes)")
    }
}