/**
 * Lesson 6 Workshop: Custom Exception Classes
 * 
 * TODO: Complete these exception classes for comprehensive error handling
 * This demonstrates:
 * - Custom exception hierarchy
 * - Domain-specific exceptions
 * - Error codes and messages
 * - Exception metadata
 */

package com.learning.validation.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

// Base exception for all validation-related errors
abstract class ValidationException(
    message: String,
    val errorCode: String,
    val field: String? = null,
    val value: Any? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause) {
    
    // TODO: Add metadata collection for detailed error reporting
    open fun getErrorMetadata(): Map<String, Any?> {
        return TODO("Return error metadata including field, value, errorCode")
    }
    
    // TODO: Add method to determine HTTP status code
    abstract fun getHttpStatus(): HttpStatus
}

// TODO: Create specific validation exception for business rule violations
@ResponseStatus(HttpStatus.BAD_REQUEST)
class BusinessRuleViolationException(
    message: String,
    val ruleName: String,
    field: String? = null,
    value: Any? = null
) : ValidationException(message, "BUSINESS_RULE_VIOLATION", field, value) {
    
    override fun getHttpStatus(): HttpStatus = HttpStatus.BAD_REQUEST
    
    override fun getErrorMetadata(): Map<String, Any?> {
        return TODO("Return metadata including ruleName and validation details")
    }
}

// TODO: Create exception for invalid field values
@ResponseStatus(HttpStatus.BAD_REQUEST)
class InvalidFieldValueException(
    field: String,
    value: Any?,
    expectedFormat: String,
    message: String = "Invalid value for field '$field'"
) : ValidationException(message, "INVALID_FIELD_VALUE", field, value) {
    
    val expectedFormat: String = expectedFormat
    
    override fun getHttpStatus(): HttpStatus = HttpStatus.BAD_REQUEST
    
    override fun getErrorMetadata(): Map<String, Any?> {
        return TODO("Return metadata including expectedFormat and field validation info")
    }
}

// TODO: Create exception for resource not found scenarios
@ResponseStatus(HttpStatus.NOT_FOUND)
class ResourceNotFoundException(
    resourceType: String,
    identifier: String,
    message: String = "$resourceType not found with identifier: $identifier"
) : ValidationException(message, "RESOURCE_NOT_FOUND") {
    
    val resourceType: String = resourceType
    val identifier: String = identifier
    
    override fun getHttpStatus(): HttpStatus = HttpStatus.NOT_FOUND
    
    override fun getErrorMetadata(): Map<String, Any?> {
        return TODO("Return metadata for resource lookup details")
    }
}

// TODO: Create exception for duplicate resources
@ResponseStatus(HttpStatus.CONFLICT)
class DuplicateResourceException(
    resourceType: String,
    field: String,
    value: Any,
    message: String = "$resourceType already exists with $field: $value"
) : ValidationException(message, "DUPLICATE_RESOURCE", field, value) {
    
    val resourceType: String = resourceType
    
    override fun getHttpStatus(): HttpStatus = HttpStatus.CONFLICT
    
    override fun getErrorMetadata(): Map<String, Any?> {
        return TODO("Return metadata for duplicate resource conflict")
    }
}

// TODO: Create exception for operation not allowed
@ResponseStatus(HttpStatus.FORBIDDEN)
class OperationNotAllowedException(
    operation: String,
    reason: String,
    resourceType: String? = null,
    resourceId: String? = null
) : ValidationException("Operation '$operation' not allowed: $reason", "OPERATION_NOT_ALLOWED") {
    
    val operation: String = operation
    val reason: String = reason
    val resourceType: String? = resourceType
    val resourceId: String? = resourceId
    
    override fun getHttpStatus(): HttpStatus = HttpStatus.FORBIDDEN
    
    override fun getErrorMetadata(): Map<String, Any?> {
        return TODO("Return metadata for operation restriction details")
    }
}

// TODO: Create exception for invalid state transitions
@ResponseStatus(HttpStatus.BAD_REQUEST)
class InvalidStateTransitionException(
    entity: String,
    currentState: String,
    targetState: String,
    message: String = "Cannot transition $entity from $currentState to $targetState"
) : ValidationException(message, "INVALID_STATE_TRANSITION") {
    
    val entity: String = entity
    val currentState: String = currentState
    val targetState: String = targetState
    
    override fun getHttpStatus(): HttpStatus = HttpStatus.BAD_REQUEST
    
    override fun getErrorMetadata(): Map<String, Any?> {
        return TODO("Return state transition metadata")
    }
}

// TODO: Create exception for quota/limit violations
@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
class QuotaExceededException(
    quotaType: String,
    currentValue: Int,
    maxAllowed: Int,
    message: String = "$quotaType quota exceeded: $currentValue/$maxAllowed"
) : ValidationException(message, "QUOTA_EXCEEDED") {
    
    val quotaType: String = quotaType
    val currentValue: Int = currentValue
    val maxAllowed: Int = maxAllowed
    
    override fun getHttpStatus(): HttpStatus = HttpStatus.TOO_MANY_REQUESTS
    
    override fun getErrorMetadata(): Map<String, Any?> {
        return TODO("Return quota violation metadata")
    }
}

// TODO: Create exception for external service errors
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
class ExternalServiceException(
    serviceName: String,
    operation: String,
    message: String = "External service '$serviceName' failed during '$operation'",
    cause: Throwable? = null
) : ValidationException(message, "EXTERNAL_SERVICE_ERROR", cause = cause) {
    
    val serviceName: String = serviceName
    val operation: String = operation
    
    override fun getHttpStatus(): HttpStatus = HttpStatus.SERVICE_UNAVAILABLE
    
    override fun getErrorMetadata(): Map<String, Any?> {
        return TODO("Return external service error metadata")
    }
}

// TODO: Create exception for configuration errors
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
class ConfigurationException(
    configKey: String,
    issue: String,
    message: String = "Configuration error for '$configKey': $issue"
) : ValidationException(message, "CONFIGURATION_ERROR") {
    
    val configKey: String = configKey
    val issue: String = issue
    
    override fun getHttpStatus(): HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR
    
    override fun getErrorMetadata(): Map<String, Any?> {
        return TODO("Return configuration error metadata")
    }
}