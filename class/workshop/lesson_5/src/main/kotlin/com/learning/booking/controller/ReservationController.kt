/**
 * Lesson 5 Workshop: Reservation REST Controller
 * 
 * TODO: Complete this REST controller
 * This demonstrates:
 * - More complex REST operations
 * - Query parameters and filtering
 * - Custom endpoints for business operations
 * - Error handling for business rules
 */

package com.learning.booking.controller

import com.learning.booking.dto.*
import com.learning.booking.service.ReservationService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

// TODO: Add @RestController annotation
// TODO: Add @RequestMapping("/api/reservations") annotation
class ReservationController(
    private val reservationService: ReservationService
) {
    
    // TODO: GET /api/reservations - Get all reservations with optional filtering
    // Add @GetMapping annotation
    fun getAllReservations(
        @RequestParam(required = false) roomId: String?,
        @RequestParam(required = false) customerEmail: String?,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) startDate: String?,
        @RequestParam(required = false) endDate: String?,
        @RequestParam(required = false) minGuestCount: Int?,
        @RequestParam(defaultValue = "false") includeCompleted: Boolean
    ): ResponseEntity<List<ReservationResponseDTO>> {
        
        // TODO: Create search criteria from request parameters
        val searchCriteria = TODO("Create ReservationSearchDTO from request parameters")
        
        // TODO: Search reservations using service
        val reservations = TODO("Call reservationService.searchReservations or getAllReservations")
        
        // TODO: Convert to response DTOs (you'll need room names)
        val reservationDTOs = TODO("Convert List<Reservation> to List<ReservationResponseDTO>")
        
        return TODO("Return ResponseEntity.ok(reservationDTOs)")
    }
    
    // TODO: GET /api/reservations/{id} - Get reservation by ID
    // Add @GetMapping("/{id}") annotation
    fun getReservationById(@PathVariable id: String): ResponseEntity<ReservationResponseDTO> {
        
        // TODO: Find reservation by ID
        val reservation = TODO("Call reservationService.getReservationById")
        
        // TODO: Convert to response DTO and return
        return TODO("Return ResponseEntity with reservation DTO or NOT_FOUND status")
    }
    
    // TODO: POST /api/reservations - Create new reservation
    // Add @PostMapping annotation
    fun createReservation(
        @Valid @RequestBody request: CreateReservationRequestDTO
    ): ResponseEntity<ReservationResponseDTO> {
        
        return try {
            // TODO: Validate request (you might want to do this in service)
            TODO("Validate time range and other business rules")
            
            // TODO: Create reservation using service
            val createdReservation = TODO("Call reservationService.createReservation")
            
            // TODO: Convert to response DTO
            val responseDTO = TODO("Convert Reservation to ReservationResponseDTO")
            
            // TODO: Return 201 CREATED
            TODO("Return ResponseEntity with CREATED status and response DTO")
            
        } catch (e: IllegalArgumentException) {
            // TODO: Handle validation errors
            TODO("Return BAD_REQUEST with error message")
        } catch (e: Exception) {
            // TODO: Handle other errors
            TODO("Return appropriate error response")
        }
    }
    
    // TODO: PUT /api/reservations/{id} - Update existing reservation
    // Add @PutMapping("/{id}") annotation
    fun updateReservation(
        @PathVariable id: String,
        @Valid @RequestBody request: UpdateReservationRequestDTO
    ): ResponseEntity<ReservationResponseDTO> {
        
        return try {
            // TODO: Update reservation using service
            val updatedReservation = TODO("Call reservationService.updateReservation")
            
            // TODO: Return updated reservation or 404 if not found
            return TODO("Return ResponseEntity with updated reservation DTO or NOT_FOUND status")
            
        } catch (e: IllegalStateException) {
            // TODO: Handle business rule violations (e.g., can't modify past reservation)
            TODO("Return CONFLICT or BAD_REQUEST with error message")
        }
    }
    
    // TODO: DELETE /api/reservations/{id} - Cancel reservation
    // Add @DeleteMapping("/{id}") annotation
    fun cancelReservation(@PathVariable id: String): ResponseEntity<Map<String, String>> {
        
        return try {
            // TODO: Cancel reservation using service
            val cancelled = TODO("Call reservationService.cancelReservation")
            
            if (cancelled) {
                // TODO: Return success message
                TODO("Return OK with cancellation confirmation")
            } else {
                // TODO: Return not found
                TODO("Return NOT_FOUND")
            }
            
        } catch (e: IllegalStateException) {
            // TODO: Handle business rule violations (e.g., can't cancel started reservation)
            TODO("Return CONFLICT with error message")
        }
    }
    
    // TODO: POST /api/reservations/check-availability - Check room availability
    // Add @PostMapping("/check-availability") annotation
    fun checkAvailability(
        @Valid @RequestBody request: AvailabilityCheckDTO
    ): ResponseEntity<List<RoomSummaryDTO>> {
        
        return try {
            // TODO: Check availability using service
            val availableRooms = TODO("Call reservationService.checkAvailability")
            
            // TODO: Convert to summary DTOs
            val roomSummaries = TODO("Convert List<Room> to List<RoomSummaryDTO>")
            
            return TODO("Return ResponseEntity.ok(roomSummaries)")
            
        } catch (e: IllegalArgumentException) {
            // TODO: Handle invalid time range
            TODO("Return BAD_REQUEST with error message")
        }
    }
    
    // TODO: GET /api/reservations/upcoming - Get upcoming reservations
    // Add @GetMapping("/upcoming") annotation
    fun getUpcomingReservations(
        @RequestParam(defaultValue = "7") days: Int
    ): ResponseEntity<List<ReservationSummaryDTO>> {
        
        // TODO: Get upcoming reservations from service
        val upcomingReservations = TODO("Call reservationService.getUpcomingReservations")
        
        // TODO: Convert to summary DTOs
        val summaries = TODO("Convert to ReservationSummaryDTO list")
        
        return TODO("Return ResponseEntity.ok(summaries)")
    }
    
    // TODO: GET /api/reservations/active - Get currently active reservations
    // Add @GetMapping("/active") annotation
    fun getActiveReservations(): ResponseEntity<List<ReservationSummaryDTO>> {
        
        // TODO: Get active reservations from service
        val activeReservations = TODO("Call reservationService.getActiveReservations")
        
        // TODO: Convert to summary DTOs
        val summaries = TODO("Convert to ReservationSummaryDTO list")
        
        return TODO("Return ResponseEntity.ok(summaries)")
    }
    
    // TODO: GET /api/reservations/statistics - Get reservation statistics
    // Add @GetMapping("/statistics") annotation
    fun getReservationStatistics(): ResponseEntity<Map<String, Any>> {
        
        // TODO: Get statistics from service
        val statistics = TODO("Call reservationService.getReservationStatistics")
        
        return TODO("Return ResponseEntity.ok(statistics)")
    }
    
    // TODO: GET /api/reservations/occupancy - Get occupancy rates
    // Add @GetMapping("/occupancy") annotation
    fun getOccupancyRate(
        @RequestParam(required = false) roomId: String?,
        @RequestParam(defaultValue = "30") days: Int
    ): ResponseEntity<Map<String, Any>> {
        
        // TODO: Get occupancy rate from service
        val occupancyRate = TODO("Call reservationService.getOccupancyRate")
        
        // TODO: Format response
        val response = mapOf(
            "roomId" to (roomId ?: "all"),
            "days" to days,
            "occupancyRate" to occupancyRate,
            "occupancyPercentage" to "%.2f%%".format(occupancyRate * 100)
        )
        
        return TODO("Return ResponseEntity.ok(response)")
    }
}