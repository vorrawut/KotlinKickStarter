/**
 * Lesson 5 Complete Solution: Reservation Service
 */

package com.learning.booking.service

import com.learning.booking.dto.*
import com.learning.booking.model.Reservation
import com.learning.booking.model.ReservationStatus
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicLong

@Service
class ReservationService(
    private val roomService: RoomService
) {
    
    private val reservations = mutableMapOf<Long, Reservation>()
    private val idCounter = AtomicLong(1)
    
    fun createReservation(request: CreateReservationRequest): ReservationResponse? {
        // Validate room exists
        val room = roomService.getRoomModel(request.roomId) ?: return null
        
        // Validate time range
        if (!request.isValidTimeRange()) {
            throw IllegalArgumentException("Invalid time range")
        }
        
        val startTime = request.getStartDateTime()
        val endTime = request.getEndDateTime()
        
        // Check room capacity
        if (!room.canAccommodate(request.numberOfGuests)) {
            throw IllegalArgumentException("Room cannot accommodate ${request.numberOfGuests} guests")
        }
        
        // Check for conflicts
        if (hasConflictingReservation(request.roomId, startTime, endTime)) {
            throw IllegalArgumentException("Room is not available for the specified time")
        }
        
        // Calculate cost
        val durationHours = java.time.Duration.between(startTime, endTime).toHours()
        val totalCost = room.calculateCost(durationHours.toInt())
        
        val reservation = Reservation(
            id = idCounter.getAndIncrement(),
            roomId = request.roomId,
            guestName = request.guestName,
            guestEmail = request.guestEmail,
            startTime = startTime,
            endTime = endTime,
            numberOfGuests = request.numberOfGuests,
            purpose = request.purpose,
            status = ReservationStatus.CONFIRMED,
            totalCost = totalCost
        )
        
        reservations[reservation.id] = reservation
        return ReservationResponse.from(reservation, room.name)
    }
    
    fun getAllReservations(): List<ReservationResponse> {
        return reservations.values.map { reservation ->
            val roomName = roomService.getRoomModel(reservation.roomId)?.name ?: "Unknown Room"
            ReservationResponse.from(reservation, roomName)
        }
    }
    
    fun getReservationById(id: Long): ReservationResponse? {
        val reservation = reservations[id] ?: return null
        val roomName = roomService.getRoomModel(reservation.roomId)?.name ?: "Unknown Room"
        return ReservationResponse.from(reservation, roomName)
    }
    
    fun updateReservation(id: Long, request: UpdateReservationRequest): ReservationResponse? {
        val existingReservation = reservations[id] ?: return null
        
        // Create updated start and end times if provided
        val newStartTime = request.startTime?.let { 
            LocalDateTime.parse(it, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) 
        } ?: existingReservation.startTime
        
        val newEndTime = request.endTime?.let { 
            LocalDateTime.parse(it, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) 
        } ?: existingReservation.endTime
        
        // Validate new time range if times are being updated
        if ((request.startTime != null || request.endTime != null)) {
            if (newStartTime.isAfter(newEndTime) || newStartTime.isBefore(LocalDateTime.now())) {
                throw IllegalArgumentException("Invalid time range")
            }
            
            // Check for conflicts (excluding current reservation)
            if (hasConflictingReservationExcluding(existingReservation.roomId, newStartTime, newEndTime, id)) {
                throw IllegalArgumentException("Room is not available for the specified time")
            }
        }
        
        // Recalculate cost if times changed
        val newTotalCost = if (request.startTime != null || request.endTime != null) {
            val room = roomService.getRoomModel(existingReservation.roomId)!!
            val durationHours = java.time.Duration.between(newStartTime, newEndTime).toHours()
            room.calculateCost(durationHours.toInt())
        } else {
            existingReservation.totalCost
        }
        
        val updatedReservation = existingReservation.copy(
            guestName = request.guestName ?: existingReservation.guestName,
            guestEmail = request.guestEmail ?: existingReservation.guestEmail,
            startTime = newStartTime,
            endTime = newEndTime,
            numberOfGuests = request.numberOfGuests ?: existingReservation.numberOfGuests,
            purpose = request.purpose ?: existingReservation.purpose,
            status = request.status ?: existingReservation.status,
            totalCost = newTotalCost
        )
        
        reservations[id] = updatedReservation
        val roomName = roomService.getRoomModel(updatedReservation.roomId)?.name ?: "Unknown Room"
        return ReservationResponse.from(updatedReservation, roomName)
    }
    
    fun cancelReservation(id: Long): ReservationResponse? {
        val reservation = reservations[id] ?: return null
        
        if (!reservation.canBeCancelled()) {
            throw IllegalArgumentException("Reservation cannot be cancelled")
        }
        
        val cancelledReservation = reservation.copy(status = ReservationStatus.CANCELLED)
        reservations[id] = cancelledReservation
        
        val roomName = roomService.getRoomModel(reservation.roomId)?.name ?: "Unknown Room"
        return ReservationResponse.from(cancelledReservation, roomName)
    }
    
    fun deleteReservation(id: Long): Boolean {
        return reservations.remove(id) != null
    }
    
    fun getReservationsByRoom(roomId: Long): List<ReservationResponse> {
        val roomName = roomService.getRoomModel(roomId)?.name ?: "Unknown Room"
        return reservations.values
            .filter { it.roomId == roomId }
            .map { ReservationResponse.from(it, roomName) }
    }
    
    fun getReservationsByGuest(guestEmail: String): List<ReservationResponse> {
        return reservations.values
            .filter { it.guestEmail.equals(guestEmail, ignoreCase = true) }
            .map { reservation ->
                val roomName = roomService.getRoomModel(reservation.roomId)?.name ?: "Unknown Room"
                ReservationResponse.from(reservation, roomName)
            }
    }
    
    fun getUpcomingReservations(): List<ReservationResponse> {
        return reservations.values
            .filter { it.isUpcoming() }
            .sortedBy { it.startTime }
            .map { reservation ->
                val roomName = roomService.getRoomModel(reservation.roomId)?.name ?: "Unknown Room"
                ReservationResponse.from(reservation, roomName)
            }
    }
    
    fun getActiveReservations(): List<ReservationResponse> {
        return reservations.values
            .filter { it.isActive() }
            .map { reservation ->
                val roomName = roomService.getRoomModel(reservation.roomId)?.name ?: "Unknown Room"
                ReservationResponse.from(reservation, roomName)
            }
    }
    
    private fun hasConflictingReservation(roomId: Long, startTime: LocalDateTime, endTime: LocalDateTime): Boolean {
        return reservations.values.any { reservation ->
            reservation.roomId == roomId &&
            reservation.status == ReservationStatus.CONFIRMED &&
            (startTime.isBefore(reservation.endTime) && endTime.isAfter(reservation.startTime))
        }
    }
    
    private fun hasConflictingReservationExcluding(
        roomId: Long, 
        startTime: LocalDateTime, 
        endTime: LocalDateTime,
        excludeReservationId: Long
    ): Boolean {
        return reservations.values.any { reservation ->
            reservation.id != excludeReservationId &&
            reservation.roomId == roomId &&
            reservation.status == ReservationStatus.CONFIRMED &&
            (startTime.isBefore(reservation.endTime) && endTime.isAfter(reservation.startTime))
        }
    }
}