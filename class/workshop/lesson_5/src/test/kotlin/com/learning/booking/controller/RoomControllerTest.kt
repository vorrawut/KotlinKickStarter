/**
 * Lesson 5 Workshop: Room Controller Tests
 * 
 * TODO: Complete these controller tests
 * This demonstrates:
 * - @WebMvcTest for testing REST controllers
 * - MockMvc for HTTP request simulation
 * - @MockBean for mocking service dependencies
 * - JSON serialization/deserialization testing
 * - HTTP status code verification
 */

package com.learning.booking.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.learning.booking.dto.CreateRoomRequestDTO
import com.learning.booking.model.Room
import com.learning.booking.model.RoomType
import com.learning.booking.service.RoomService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime

// TODO: Add @WebMvcTest(RoomController::class) annotation
class RoomControllerTest {
    
    // TODO: Add @Autowired annotation
    lateinit var mockMvc: MockMvc
    
    // TODO: Add @MockBean annotation
    lateinit var roomService: RoomService
    
    // TODO: Add @Autowired annotation
    lateinit var objectMapper: ObjectMapper
    
    @Test
    fun `should get all rooms successfully`() {
        // TODO: Setup mock data
        val sampleRooms = listOf(
            TODO("Create sample Room object"),
            TODO("Create another sample Room object")
        )
        
        // TODO: Mock service call
        TODO("Use given(roomService.getAllRooms()).willReturn(sampleRooms)")
        
        // TODO: Perform GET request and verify response
        TODO("Use mockMvc.perform(get(\"/api/rooms\")) and verify status and content")
    }
    
    @Test
    fun `should get room by id successfully`() {
        // TODO: Setup mock data
        val roomId = "room-1"
        val sampleRoom = TODO("Create sample Room object")
        
        // TODO: Mock service call
        TODO("Mock roomService.getRoomById to return sampleRoom")
        
        // TODO: Perform GET request and verify response
        TODO("Use mockMvc.perform(get(\"/api/rooms/{id}\", roomId)) and verify")
    }
    
    @Test
    fun `should return 404 when room not found`() {
        // TODO: Setup mock to return null
        val roomId = "non-existent"
        TODO("Mock roomService.getRoomById to return null")
        
        // TODO: Perform GET request and verify 404
        TODO("Use mockMvc.perform(get(\"/api/rooms/{id}\", roomId)) and expect NOT_FOUND")
    }
    
    @Test
    fun `should create room successfully`() {
        // TODO: Setup request DTO
        val createRequest = CreateRoomRequestDTO(
            name = "Test Conference Room",
            roomType = "CONFERENCE",
            capacity = 20,
            pricePerHour = 75.0,
            amenities = listOf("Projector", "Whiteboard")
        )
        
        // TODO: Setup mock response
        val createdRoom = TODO("Create expected Room object that would be returned")
        
        // TODO: Mock service call
        TODO("Mock roomService.createRoom to return createdRoom")
        
        // TODO: Perform POST request and verify response
        TODO("Use mockMvc.perform(post(\"/api/rooms\")) with JSON content and verify CREATED status")
    }
    
    @Test
    fun `should return 400 for invalid room creation request`() {
        // TODO: Setup invalid request (e.g., empty name)
        val invalidRequest = CreateRoomRequestDTO(
            name = "", // Invalid: empty name
            roomType = "CONFERENCE",
            capacity = 20,
            pricePerHour = 75.0
        )
        
        // TODO: Perform POST request and verify 400
        TODO("Use mockMvc.perform(post(\"/api/rooms\")) with invalid JSON and expect BAD_REQUEST")
    }
    
    @Test
    fun `should update room successfully`() {
        // TODO: Setup update request and mock response
        val roomId = "room-1"
        val updateRequest = TODO("Create UpdateRoomRequestDTO")
        val updatedRoom = TODO("Create updated Room object")
        
        // TODO: Mock service call
        TODO("Mock roomService.updateRoom to return updatedRoom")
        
        // TODO: Perform PUT request and verify response
        TODO("Use mockMvc.perform(put(\"/api/rooms/{id}\", roomId)) and verify")
    }
    
    @Test
    fun `should delete room successfully`() {
        // TODO: Setup mock for successful deletion
        val roomId = "room-1"
        TODO("Mock roomService.deleteRoom to return true")
        
        // TODO: Perform DELETE request and verify response
        TODO("Use mockMvc.perform(delete(\"/api/rooms/{id}\", roomId)) and expect NO_CONTENT")
    }
    
    @Test
    fun `should search rooms with parameters`() {
        // TODO: Setup search parameters and mock response
        val filteredRooms = listOf(TODO("Create filtered Room objects"))
        
        // TODO: Mock service call with search criteria
        TODO("Mock roomService.searchRooms to return filteredRooms")
        
        // TODO: Perform GET request with query parameters
        TODO("Use mockMvc.perform(get(\"/api/rooms\").param(...)) and verify")
    }
    
    @Test
    fun `should get room statistics`() {
        // TODO: Setup mock statistics
        val statistics = mapOf(
            "totalRooms" to 5,
            "averageCapacity" to 25.0,
            "roomsByType" to mapOf("CONFERENCE" to 2, "MEETING" to 3)
        )
        
        // TODO: Mock service call
        TODO("Mock roomService.getRoomStatistics to return statistics")
        
        // TODO: Perform GET request and verify response
        TODO("Use mockMvc.perform(get(\"/api/rooms/statistics\")) and verify JSON content")
    }
    
    // TODO: Helper method to convert object to JSON
    private fun asJsonString(obj: Any): String {
        return TODO("Use objectMapper.writeValueAsString to convert object to JSON")
    }
    
    // TODO: Helper method to create sample room
    private fun createSampleRoom(
        id: String = "room-1",
        name: String = "Conference Room A",
        roomType: RoomType = RoomType.CONFERENCE,
        capacity: Int = 20,
        pricePerHour: Double = 50.0
    ): Room {
        return TODO("Create and return Room object with given parameters")
    }
}