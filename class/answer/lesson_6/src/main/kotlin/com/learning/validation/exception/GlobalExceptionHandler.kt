/**
 * Lesson 6 Complete Solution: Global Exception Handler
 */

package com.learning.validation.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {
    
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    
    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(
        ex: ValidationException, 
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Validation exception: ${ex.message}", ex)
        
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = ex.getHttpStatus().value(),
            error = ex.getHttpStatus().reasonPhrase,
            message = ex.message ?: "Validation failed",
            path = getPath(request),
            details = ex.getErrorMetadata()
        )
        
        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse)
    }
    
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Validation failed: ${ex.message}")
        
        val fieldErrors = ex.bindingResult.allErrors.map { error ->
            when (error) {
                is FieldError -> ValidationFieldError(
                    field = error.field,
                    rejectedValue = error.rejectedValue,
                    message = error.defaultMessage ?: "Validation failed"
                )
                else -> ValidationFieldError(
                    field = error.objectName,
                    rejectedValue = null,
                    message = error.defaultMessage ?: "Validation failed"
                )
            }
        }
        
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Validation Failed",
            message = "Request validation failed",
            path = getPath(request),
            details = mapOf(
                "fieldErrors" to fieldErrors,
                "errorCount" to fieldErrors.size
            )
        )
        
        return ResponseEntity.badRequest().body(errorResponse)
    }
    
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Invalid JSON request: ${ex.message}")
        
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = "Invalid JSON format or missing required fields",
            path = getPath(request),
            details = mapOf(
                "cause" to (ex.cause?.message ?: "JSON parsing error")
            )
        )
        
        return ResponseEntity.badRequest().body(errorResponse)
    }
    
    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingServletRequestParameter(
        ex: MissingServletRequestParameterException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Missing request parameter: ${ex.parameterName}")
        
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = "Required request parameter '${ex.parameterName}' is missing",
            path = getPath(request),
            details = mapOf(
                "parameterName" to ex.parameterName,
                "parameterType" to ex.parameterType
            )
        )
        
        return ResponseEntity.badRequest().body(errorResponse)
    }
    
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatch(
        ex: MethodArgumentTypeMismatchException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Type mismatch for parameter: ${ex.name}")
        
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = "Invalid value for parameter '${ex.name}'. Expected type: ${ex.requiredType?.simpleName}",
            path = getPath(request),
            details = mapOf(
                "parameterName" to ex.name,
                "rejectedValue" to ex.value,
                "expectedType" to ex.requiredType?.simpleName
            )
        )
        
        return ResponseEntity.badRequest().body(errorResponse)
    }
    
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(
        ex: IllegalArgumentException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Illegal argument: ${ex.message}")
        
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = ex.message ?: "Invalid argument provided",
            path = getPath(request)
        )
        
        return ResponseEntity.badRequest().body(errorResponse)
    }
    
    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(
        ex: RuntimeException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected runtime exception: ${ex.message}", ex)
        
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = "An unexpected error occurred",
            path = getPath(request),
            details = mapOf(
                "exceptionType" to ex.javaClass.simpleName
            )
        )
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
    
    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected exception: ${ex.message}", ex)
        
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error", 
            message = "An unexpected error occurred",
            path = getPath(request)
        )
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
    
    private fun getPath(request: WebRequest): String {
        return request.getDescription(false).removePrefix("uri=")
    }
}

data class ErrorResponse(
    val timestamp: LocalDateTime,
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val details: Map<String, Any?> = emptyMap()
)

data class ValidationFieldError(
    val field: String,
    val rejectedValue: Any?,
    val message: String
)