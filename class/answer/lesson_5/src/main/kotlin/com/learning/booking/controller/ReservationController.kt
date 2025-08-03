/**
 * Lesson 5 Complete Solution: Reservation Controller
 */

package com.learning.booking.controller

import com.learning.booking.dto.*
import com.learning.booking.service.ReservationService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/reservations")
class ReservationController(
    private val reservationService: ReservationService
) {
    
    @PostMapping
    fun createReservation(@Valid @RequestBody request: CreateReservationRequest): ResponseEntity<Any> {
        return try {
            val response = reservationService.createReservation(request)
            if (response != null) {
                ResponseEntity.status(HttpStatus.CREATED).body(response)
            } else {
                ResponseEntity.badRequest().body(mapOf("error" to "Room not found"))
            }
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }
    
    @GetMapping
    fun getAllReservations(): ResponseEntity<List<ReservationResponse>> {
        val reservations = reservationService.getAllReservations()
        return ResponseEntity.ok(reservations)
    }
    
    @GetMapping("/{id}")
    fun getReservationById(@PathVariable id: Long): ResponseEntity<ReservationResponse> {
        val reservation = reservationService.getReservationById(id)
        return if (reservation != null) {
            ResponseEntity.ok(reservation)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @PutMapping("/{id}")
    fun updateReservation(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateReservationRequest
    ): ResponseEntity<Any> {
        return try {
            val updatedReservation = reservationService.updateReservation(id, request)
            if (updatedReservation != null) {
                ResponseEntity.ok(updatedReservation)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }
    
    @PostMapping("/{id}/cancel")
    fun cancelReservation(@PathVariable id: Long): ResponseEntity<Any> {
        return try {
            val cancelledReservation = reservationService.cancelReservation(id)
            if (cancelledReservation != null) {
                ResponseEntity.ok(cancelledReservation)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }
    
    @DeleteMapping("/{id}")
    fun deleteReservation(@PathVariable id: Long): ResponseEntity<Void> {
        val deleted = reservationService.deleteReservation(id)
        return if (deleted) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @GetMapping("/room/{roomId}")
    fun getReservationsByRoom(@PathVariable roomId: Long): ResponseEntity<List<ReservationResponse>> {
        val reservations = reservationService.getReservationsByRoom(roomId)
        return ResponseEntity.ok(reservations)
    }
    
    @GetMapping("/guest")
    fun getReservationsByGuest(@RequestParam email: String): ResponseEntity<List<ReservationResponse>> {
        val reservations = reservationService.getReservationsByGuest(email)
        return ResponseEntity.ok(reservations)
    }
    
    @GetMapping("/upcoming")
    fun getUpcomingReservations(): ResponseEntity<List<ReservationResponse>> {
        val reservations = reservationService.getUpcomingReservations()
        return ResponseEntity.ok(reservations)
    }
    
    @GetMapping("/active")
    fun getActiveReservations(): ResponseEntity<List<ReservationResponse>> {
        val reservations = reservationService.getActiveReservations()
        return ResponseEntity.ok(reservations)
    }
}