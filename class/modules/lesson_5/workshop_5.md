# Workshop 5: REST Controllers & DTOs

## 🎯 Learning Objectives

By the end of this workshop, you will:
- Design and implement RESTful APIs following industry standards
- Master Data Transfer Objects (DTOs) for clean API contracts
- Use proper HTTP methods, status codes, and URL patterns
- Implement request/response validation with Spring Boot
- Build controller tests with MockMvc
- Understand separation between domain models and API contracts
- Handle errors and exceptions properly in REST APIs

## 📋 Prerequisites

- Completed Lessons 1-4 (Kotlin fundamentals, Spring Boot setup)
- Understanding of HTTP protocol and REST principles
- Basic knowledge of JSON and API design

## 🏗️ Project Overview

You'll build a **Room Booking System** REST API that demonstrates:
- Complete CRUD operations for rooms and reservations
- Advanced search and filtering capabilities
- Business logic validation and error handling
- Proper DTOs for different operations
- Statistical and reporting endpoints

## 🚀 Workshop Steps

### Step 1: Complete the Application Setup

Open `BookingApplication.kt` and:

1. **Add the Spring Boot annotation**
   ```kotlin
   @SpringBootApplication
   class BookingApplication
   ```

2. **Implement the main function**
   ```kotlin
   fun main(args: Array<String>) {
       runApplication<BookingApplication>(*args)
   }
   ```

**💡 Key Learning:** This creates a REST API application with Spring Boot's web starter.

### Step 2: Complete the Domain Models

#### Room Model (`Room.kt`)

1. **Define the RoomType enum**
   ```kotlin
   enum class RoomType(val displayName: String, val baseCapacity: Int) {
       CONFERENCE("Conference Room", 20),
       MEETING("Meeting Room", 8),
       WORKSHOP("Workshop Room", 30),
       TRAINING("Training Room", 25),
       PRESENTATION("Presentation Room", 50)
   }
   ```

2. **Define the RoomStatus enum**
   ```kotlin
   enum class RoomStatus {
       AVAILABLE,
       OCCUPIED,
       MAINTENANCE,
       RESERVED
   }
   ```

3. **Complete the Room validation methods**
   ```kotlin
   fun isAvailable(): Boolean {
       return isActive
   }
   
   fun canAccommodate(guestCount: Int): Boolean {
       return guestCount <= capacity
   }
   
   fun calculateCost(hours: Int): Double {
       return hours * pricePerHour
   }
   ```

#### Reservation Model (`Reservation.kt`)

1. **Define the ReservationStatus enum**
   ```kotlin
   enum class ReservationStatus(val displayName: String) {
       PENDING("Pending Confirmation"),
       CONFIRMED("Confirmed"),
       CANCELLED("Cancelled"),
       COMPLETED("Completed"),
       NO_SHOW("No Show")
   }
   ```

2. **Complete the computed properties**
   ```kotlin
   val durationHours: Long
       get() = ChronoUnit.HOURS.between(startTime, endTime)
   
   val isUpcoming: Boolean
       get() = startTime.isAfter(LocalDateTime.now())
   
   val isActive: Boolean
       get() = LocalDateTime.now().let { now ->
           now.isAfter(startTime) && now.isBefore(endTime)
       }
   ```

3. **Implement validation methods**
   ```kotlin
   fun isValidTimeSlot(): Boolean {
       return startTime.isBefore(endTime) && startTime.isAfter(LocalDateTime.now())
   }
   
   fun overlaps(other: Reservation): Boolean {
       return roomId == other.roomId &&
               startTime.isBefore(other.endTime) &&
               endTime.isAfter(other.startTime)
   }
   ```

**💡 Key Learning:** Domain models contain business logic and validation rules.

### Step 3: Implement DTOs

#### Room DTOs (`RoomDTOs.kt`)

1. **Complete the RoomResponseDTO factory method**
   ```kotlin
   fun from(room: Room): RoomResponseDTO {
       return RoomResponseDTO(
           id = room.id,
           name = room.name,
           roomType = room.roomType.name,
           capacity = room.capacity,
           pricePerHour = room.pricePerHour,
           amenities = room.amenities,
           isActive = room.isActive,
           createdAt = room.createdAt.toString()
       )
   }
   ```

2. **Complete the CreateRoomRequestDTO conversion**
   ```kotlin
   fun toRoom(id: String): Room {
       return Room(
           id = id,
           name = name,
           roomType = RoomType.valueOf(roomType),
           capacity = capacity,
           pricePerHour = pricePerHour,
           amenities = amenities
       )
   }
   ```

3. **Implement validation methods**
   ```kotlin
   fun isValidRoomType(): Boolean {
       return try {
           RoomType.valueOf(roomType)
           true
       } catch (e: IllegalArgumentException) {
           false
       }
   }
   ```

**💡 Key Learning:** DTOs provide stable API contracts independent of domain model changes.

### Step 4: Implement Services

#### Room Service (`RoomService.kt`)

1. **Add the @Service annotation**
   ```kotlin
   @Service
   class RoomService {
   ```

2. **Implement CRUD operations**
   ```kotlin
   fun createRoom(request: CreateRoomRequestDTO): Room {
       val roomId = generateRoomId()
       val room = request.toRoom(roomId)
       rooms[roomId] = room
       return room
   }
   
   fun getAllRooms(): List<Room> {
       return rooms.values.filter { it.isActive }
   }
   
   fun getRoomById(id: String): Room? {
       return rooms[id]?.takeIf { it.isActive }
   }
   ```

3. **Implement search functionality**
   ```kotlin
   fun searchRooms(searchCriteria: RoomSearchDTO): List<Room> {
       return getAllRooms().filter { room ->
           searchCriteria.matches(room)
       }
   }
   ```

#### Reservation Service (`ReservationService.kt`)

1. **Add the @Service annotation**
   ```kotlin
   @Service
   class ReservationService(
       private val roomService: RoomService
   ) {
   ```

2. **Implement reservation creation with validation**
   ```kotlin
   fun createReservation(request: CreateReservationRequestDTO): Reservation {
       // Validate time range
       if (!request.isValidTimeRange()) {
           throw IllegalArgumentException("Invalid time range")
       }
       
       // Check room exists and can accommodate
       val room = roomService.getRoomById(request.roomId)
           ?: throw IllegalArgumentException("Room not found")
           
       if (!room.canAccommodate(request.guestCount)) {
           throw IllegalArgumentException("Room cannot accommodate guest count")
       }
       
       // Check for conflicts
       val startTime = request.parseStartTime()
       val endTime = request.parseEndTime()
       
       if (hasConflictingReservation(request.roomId, startTime, endTime)) {
           throw IllegalArgumentException("Time slot conflicts with existing reservation")
       }
       
       // Calculate cost and create reservation
       val totalCost = calculateReservationCost(request.roomId, startTime, endTime)
       val reservationId = generateReservationId()
       
       val reservation = request.toReservation(reservationId, totalCost)
       reservations[reservationId] = reservation
       
       return reservation
   }
   ```

**💡 Key Learning:** Services contain business logic and orchestrate operations between entities.

### Step 5: Implement REST Controllers

#### Room Controller (`RoomController.kt`)

1. **Add controller annotations**
   ```kotlin
   @RestController
   @RequestMapping("/api/rooms")
   class RoomController(
       private val roomService: RoomService
   ) {
   ```

2. **Implement GET endpoints**
   ```kotlin
   @GetMapping
   fun getAllRooms(
       @RequestParam(required = false) roomType: String?,
       @RequestParam(required = false) minCapacity: Int?,
       @RequestParam(required = false) maxPricePerHour: Double?,
       @RequestParam(required = false) amenities: List<String>?,
       @RequestParam(defaultValue = "true") availableOnly: Boolean
   ): ResponseEntity<List<RoomResponseDTO>> {
       
       val searchCriteria = RoomSearchDTO(
           roomType = roomType,
           minCapacity = minCapacity,
           maxPricePerHour = maxPricePerHour,
           amenities = amenities,
           availableOnly = availableOnly
       )
       
       val rooms = if (searchCriteria.hasAnyCriteria()) {
           roomService.searchRooms(searchCriteria)
       } else {
           roomService.getAllRooms()
       }
       
       val roomDTOs = rooms.map { RoomResponseDTO.from(it) }
       
       return ResponseEntity.ok(roomDTOs)
   }
   
   @GetMapping("/{id}")
   fun getRoomById(@PathVariable id: String): ResponseEntity<RoomResponseDTO> {
       val room = roomService.getRoomById(id)
           ?: return ResponseEntity.notFound().build()
           
       return ResponseEntity.ok(RoomResponseDTO.from(room))
   }
   ```

3. **Implement POST endpoint**
   ```kotlin
   @PostMapping
   fun createRoom(
       @Valid @RequestBody request: CreateRoomRequestDTO
   ): ResponseEntity<RoomResponseDTO> {
       return try {
           val createdRoom = roomService.createRoom(request)
           val responseDTO = RoomResponseDTO.from(createdRoom)
           
           ResponseEntity.status(HttpStatus.CREATED).body(responseDTO)
           
       } catch (e: IllegalArgumentException) {
           ResponseEntity.badRequest().build()
       }
   }
   ```

4. **Implement PUT and DELETE endpoints**
   ```kotlin
   @PutMapping("/{id}")
   fun updateRoom(
       @PathVariable id: String,
       @Valid @RequestBody request: UpdateRoomRequestDTO
   ): ResponseEntity<RoomResponseDTO> {
       val updatedRoom = roomService.updateRoom(id, request)
           ?: return ResponseEntity.notFound().build()
           
       return ResponseEntity.ok(RoomResponseDTO.from(updatedRoom))
   }
   
   @DeleteMapping("/{id}")
   fun deleteRoom(@PathVariable id: String): ResponseEntity<Void> {
       val deleted = roomService.deleteRoom(id)
       
       return if (deleted) {
           ResponseEntity.noContent().build()
       } else {
           ResponseEntity.notFound().build()
       }
   }
   ```

**💡 Key Learning:** Controllers handle HTTP concerns and delegate business logic to services.

### Step 6: Implement Advanced Endpoints

1. **Add business-specific endpoints**
   ```kotlin
   @PostMapping("/check-availability")
   fun checkAvailability(
       @Valid @RequestBody request: AvailabilityCheckDTO
   ): ResponseEntity<List<RoomSummaryDTO>> {
       return try {
           val availableRooms = reservationService.checkAvailability(request)
           val summaries = availableRooms.map { 
               RoomSummaryDTO.from(it, "AVAILABLE") 
           }
           
           ResponseEntity.ok(summaries)
           
       } catch (e: IllegalArgumentException) {
           ResponseEntity.badRequest().build()
       }
   }
   
   @GetMapping("/statistics")
   fun getRoomStatistics(): ResponseEntity<Map<String, Any>> {
       val statistics = roomService.getRoomStatistics()
       return ResponseEntity.ok(statistics)
   }
   ```

### Step 7: Write Controller Tests

1. **Set up test class**
   ```kotlin
   @WebMvcTest(RoomController::class)
   class RoomControllerTest {
       
       @Autowired
       lateinit var mockMvc: MockMvc
       
       @MockBean
       lateinit var roomService: RoomService
       
       @Autowired
       lateinit var objectMapper: ObjectMapper
   ```

2. **Test GET endpoints**
   ```kotlin
   @Test
   fun `should get all rooms successfully`() {
       val sampleRooms = listOf(createSampleRoom())
       given(roomService.getAllRooms()).willReturn(sampleRooms)
       
       mockMvc.perform(get("/api/rooms"))
           .andExpect(status().isOk)
           .andExpect(content().contentType(MediaType.APPLICATION_JSON))
           .andExpect(jsonPath("$").isArray)
           .andExpect(jsonPath("$[0].name").value("Conference Room A"))
   }
   ```

3. **Test POST endpoint**
   ```kotlin
   @Test
   fun `should create room successfully`() {
       val createRequest = CreateRoomRequestDTO(
           name = "Test Room",
           roomType = "CONFERENCE",
           capacity = 20,
           pricePerHour = 75.0
       )
       
       val createdRoom = createSampleRoom()
       given(roomService.createRoom(any())).willReturn(createdRoom)
       
       mockMvc.perform(post("/api/rooms")
           .contentType(MediaType.APPLICATION_JSON)
           .content(asJsonString(createRequest)))
           .andExpect(status().isCreated)
           .andExpect(jsonPath("$.name").value("Conference Room A"))
   }
   ```

### Step 8: Test Your API

1. **Build and run the application**
   ```bash
   ./gradlew bootRun
   ```

2. **Test endpoints with curl**
   ```bash
   # Get all rooms
   curl http://localhost:8080/api/rooms
   
   # Create a room
   curl -X POST http://localhost:8080/api/rooms \
     -H "Content-Type: application/json" \
     -d '{
       "name": "Conference Room Alpha",
       "roomType": "CONFERENCE",
       "capacity": 25,
       "pricePerHour": 80.0,
       "amenities": ["Projector", "Whiteboard", "Video Conference"]
     }'
   
   # Get room by ID
   curl http://localhost:8080/api/rooms/{room-id}
   
   # Search rooms
   curl "http://localhost:8080/api/rooms?roomType=CONFERENCE&minCapacity=20"
   ```

3. **Test reservation endpoints**
   ```bash
   # Check availability
   curl -X POST http://localhost:8080/api/reservations/check-availability \
     -H "Content-Type: application/json" \
     -d '{
       "startTime": "2024-08-10T09:00:00",
       "endTime": "2024-08-10T11:00:00",
       "guestCount": 15
     }'
   ```

## 🎯 Expected Outcomes

After completing this workshop, you should have:

1. **Working REST API**
   - Complete CRUD operations for rooms and reservations
   - Proper HTTP status codes and error handling
   - Search and filtering capabilities

2. **Clean Architecture**
   - Separation between controllers, services, and models
   - DTOs for API contracts
   - Validation at appropriate layers

3. **Professional API Design**
   - RESTful URL patterns
   - Consistent request/response formats
   - Business-specific endpoints

4. **Comprehensive Testing**
   - Controller tests with MockMvc
   - Mocking of service dependencies
   - JSON serialization testing

## 🔍 Key Concepts Demonstrated

### REST API Design
- **Resource-based URLs**: `/api/rooms`, `/api/reservations`
- **HTTP Methods**: GET for retrieval, POST for creation, PUT for updates, DELETE for removal
- **Status Codes**: 200 OK, 201 Created, 404 Not Found, 400 Bad Request

### DTOs vs Domain Models
- **DTOs**: Stable API contracts, validation, serialization-friendly
- **Domain Models**: Business logic, relationships, internal operations
- **Mapping**: Converting between DTOs and domain models

### Validation
- **Bean Validation**: `@Valid`, `@NotBlank`, `@Min`, `@Max`
- **Custom Validation**: Business rules in services
- **Error Responses**: Proper error messages and status codes

### Testing
- **@WebMvcTest**: Testing web layer in isolation
- **MockMvc**: Simulating HTTP requests
- **@MockBean**: Mocking service dependencies

## 🚀 Next Steps

Ready for **Lesson 6: Request Validation & Error Handling**? You'll learn:
- Advanced validation techniques
- Global exception handling
- Custom validators
- Error response standardization

## 💡 Pro Tips

1. **Keep DTOs Simple**: Only include fields needed for API contract
2. **Use HTTP Status Codes**: Match the actual outcome of operations
3. **Validate Early**: Check inputs at the controller level
4. **Test Error Cases**: Don't just test happy paths
5. **Document APIs**: Use OpenAPI/Swagger for documentation
6. **Version APIs**: Plan for API evolution from the start

---

**🎉 Congratulations!** You've mastered REST API design with Spring Boot. Your booking system now provides a professional API interface that follows industry best practices!