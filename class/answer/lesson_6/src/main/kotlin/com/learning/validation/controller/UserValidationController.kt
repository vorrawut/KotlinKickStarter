/**
 * Lesson 6 Complete Solution: User Validation Controller
 */

package com.learning.validation.controller

import com.learning.validation.dto.*
import com.learning.validation.model.UserStatus
import com.learning.validation.service.UserValidationService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserValidationController(
    private val userValidationService: UserValidationService
) {
    
    @PostMapping
    fun createUser(@Valid @RequestBody request: CreateUserRequest): ResponseEntity<UserResponse> {
        val response = userValidationService.createUser(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
    
    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: String): ResponseEntity<UserResponse> {
        val response = userValidationService.getUserById(id)
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/username/{username}")
    fun getUserByUsername(@PathVariable username: String): ResponseEntity<UserResponse> {
        val response = userValidationService.getUserByUsername(username)
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/email/{email}")
    fun getUserByEmail(@PathVariable email: String): ResponseEntity<UserResponse> {
        val response = userValidationService.getUserByEmail(email)
        return ResponseEntity.ok(response)
    }
    
    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: String,
        @Valid @RequestBody request: UpdateUserRequest
    ): ResponseEntity<UserResponse> {
        val response = userValidationService.updateUser(id, request)
        return ResponseEntity.ok(response)
    }
    
    @PatchMapping("/{id}/status")
    fun changeUserStatus(
        @PathVariable id: String,
        @Valid @RequestBody request: ChangeUserStatusRequest
    ): ResponseEntity<UserResponse> {
        val response = userValidationService.changeUserStatus(id, request)
        return ResponseEntity.ok(response)
    }
    
    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: String): ResponseEntity<Void> {
        userValidationService.deleteUser(id)
        return ResponseEntity.noContent().build()
    }
    
    @GetMapping("/search")
    fun searchUsers(@Valid request: UserSearchRequest): ResponseEntity<List<UserResponse>> {
        val response = userValidationService.searchUsers(request)
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/status/{status}")
    fun getUsersByStatus(@PathVariable status: UserStatus): ResponseEntity<List<UserResponse>> {
        val response = userValidationService.getUsersWithStatus(status)
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/{id}/permissions/{operation}")
    fun checkUserPermission(
        @PathVariable id: String,
        @PathVariable operation: String
    ): ResponseEntity<Map<String, Any>> {
        val hasPermission = userValidationService.validateUserOperationPermission(id, operation)
        val response = mapOf(
            "userId" to id,
            "operation" to operation,
            "hasPermission" to hasPermission
        )
        return ResponseEntity.ok(response)
    }
}