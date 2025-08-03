/**
 * Lesson 6 Complete Solution: Custom Exception Classes
 */

package com.learning.validation.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

abstract class ValidationException(
    message: String,
    val errorCode: String,
    val field: String? = null,
    val value: Any? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause) {
    
    open fun getErrorMetadata(): Map<String, Any?> {
        return mapOf(
            "errorCode" to errorCode,
            "field" to field,
            "value" to value,
            "timestamp" to java.time.LocalDateTime.now().toString()
        )
    }
    
    abstract fun getHttpStatus(): HttpStatus
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
class BusinessRuleViolationException(
    message: String,
    val ruleName: String,
    field: String? = null,
    value: Any? = null
) : ValidationException(message, "BUSINESS_RULE_VIOLATION", field, value) {
    
    override fun getHttpStatus(): HttpStatus = HttpStatus.BAD_REQUEST
    
    override fun getErrorMetadata(): Map<String, Any?> {
        return super.getErrorMetadata() + mapOf("ruleName" to ruleName)
    }
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
class InvalidFieldValueException(
    field: String,
    value: Any?,
    val expectedFormat: String,
    message: String = "Invalid value for field '$field'"
) : ValidationException(message, "INVALID_FIELD_VALUE", field, value) {
    
    override fun getHttpStatus(): HttpStatus = HttpStatus.BAD_REQUEST
    
    override fun getErrorMetadata(): Map<String, Any?> {
        return super.getErrorMetadata() + mapOf("expectedFormat" to expectedFormat)
    }
}

@ResponseStatus(HttpStatus.NOT_FOUND)
class ResourceNotFoundException(
    val resourceType: String,
    val identifier: String,
    message: String = "$resourceType not found with identifier: $identifier"
) : ValidationException(message, "RESOURCE_NOT_FOUND") {
    
    override fun getHttpStatus(): HttpStatus = HttpStatus.NOT_FOUND
    
    override fun getErrorMetadata(): Map<String, Any?> {
        return super.getErrorMetadata() + mapOf(
            "resourceType" to resourceType,
            "identifier" to identifier
        )
    }
}

@ResponseStatus(HttpStatus.CONFLICT)
class DuplicateResourceException(
    val resourceType: String,
    field: String,
    value: Any,
    message: String = "$resourceType already exists with $field: $value"
) : ValidationException(message, "DUPLICATE_RESOURCE", field, value) {
    
    override fun getHttpStatus(): HttpStatus = HttpStatus.CONFLICT
    
    override fun getErrorMetadata(): Map<String, Any?> {
        return super.getErrorMetadata() + mapOf("resourceType" to resourceType)
    }
}

@ResponseStatus(HttpStatus.FORBIDDEN)
class OperationNotAllowedException(
    val operation: String,
    val reason: String,
    val resourceType: String? = null,
    val resourceId: String? = null
) : ValidationException("Operation '$operation' not allowed: $reason", "OPERATION_NOT_ALLOWED") {
    
    override fun getHttpStatus(): HttpStatus = HttpStatus.FORBIDDEN
    
    override fun getErrorMetadata(): Map<String, Any?> {
        return super.getErrorMetadata() + mapOf(
            "operation" to operation,
            "reason" to reason,
            "resourceType" to resourceType,
            "resourceId" to resourceId
        )
    }
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
class InvalidStateTransitionException(
    val entity: String,
    val currentState: String,
    val targetState: String,
    message: String = "Cannot transition $entity from $currentState to $targetState"
) : ValidationException(message, "INVALID_STATE_TRANSITION") {
    
    override fun getHttpStatus(): HttpStatus = HttpStatus.BAD_REQUEST
    
    override fun getErrorMetadata(): Map<String, Any?> {
        return super.getErrorMetadata() + mapOf(
            "entity" to entity,
            "currentState" to currentState,
            "targetState" to targetState
        )
    }
}

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
class QuotaExceededException(
    val quotaType: String,
    val currentValue: Int,
    val maxAllowed: Int,
    message: String = "$quotaType quota exceeded: $currentValue/$maxAllowed"
) : ValidationException(message, "QUOTA_EXCEEDED") {
    
    override fun getHttpStatus(): HttpStatus = HttpStatus.TOO_MANY_REQUESTS
    
    override fun getErrorMetadata(): Map<String, Any?> {
        return super.getErrorMetadata() + mapOf(
            "quotaType" to quotaType,
            "currentValue" to currentValue,
            "maxAllowed" to maxAllowed
        )
    }
}

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
class ExternalServiceException(
    val serviceName: String,
    val operation: String,
    message: String = "External service '$serviceName' failed during '$operation'",
    cause: Throwable? = null
) : ValidationException(message, "EXTERNAL_SERVICE_ERROR", cause = cause) {
    
    override fun getHttpStatus(): HttpStatus = HttpStatus.SERVICE_UNAVAILABLE
    
    override fun getErrorMetadata(): Map<String, Any?> {
        return super.getErrorMetadata() + mapOf(
            "serviceName" to serviceName,
            "operation" to operation
        )
    }
}

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
class ConfigurationException(
    val configKey: String,
    val issue: String,
    message: String = "Configuration error for '$configKey': $issue"
) : ValidationException(message, "CONFIGURATION_ERROR") {
    
    override fun getHttpStatus(): HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR
    
    override fun getErrorMetadata(): Map<String, Any?> {
        return super.getErrorMetadata() + mapOf(
            "configKey" to configKey,
            "issue" to issue
        )
    }
}