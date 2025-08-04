package com.learning.caching.controller

import com.learning.caching.model.User
import com.learning.caching.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {
    
    @GetMapping("/{id}")
    fun getUserProfile(@PathVariable id: Long): ResponseEntity<User> {
        val user = userService.getUserProfile(id)
        return if (user != null) {
            ResponseEntity.ok(user)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @PostMapping
    fun createUser(@RequestBody createRequest: CreateUserRequest): ResponseEntity<User> {
        val user = User(
            username = createRequest.username,
            email = createRequest.email,
            firstName = createRequest.firstName,
            lastName = createRequest.lastName
        )
        val created = userService.createUser(user)
        return ResponseEntity.ok(created)
    }
    
    @PutMapping("/{id}")
    fun updateUser(@PathVariable id: Long, @RequestBody updateRequest: UpdateUserRequest): ResponseEntity<User> {
        val existingUser = userService.getUserProfile(id) ?: return ResponseEntity.notFound().build()
        
        val updatedUser = existingUser.copy(
            firstName = updateRequest.firstName ?: existingUser.firstName,
            lastName = updateRequest.lastName ?: existingUser.lastName,
            updatedAt = LocalDateTime.now()
        )
        
        val result = userService.updateUser(updatedUser)
        return ResponseEntity.ok(result)
    }
    
    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long): ResponseEntity<Void> {
        userService.deleteUser(id)
        return ResponseEntity.noContent().build()
    }
    
    @GetMapping("/{id}/posts")
    fun getUserPosts(@PathVariable id: Long, @RequestParam(defaultValue = "0") page: Int): ResponseEntity<Any> {
        val posts = userService.getUserPosts(id, page)
        return ResponseEntity.ok(mapOf(
            "posts" to posts,
            "page" to page,
            "total" to posts.size
        ))
    }
    
    @GetMapping("/username/{username}")
    fun getUserByUsername(@PathVariable username: String): ResponseEntity<User> {
        val user = userService.findByUsername(username)
        return if (user != null) {
            ResponseEntity.ok(user)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}

data class CreateUserRequest(
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String
)

data class UpdateUserRequest(
    val firstName: String?,
    val lastName: String?
)