/**
 * Lesson 12 Complete Solution: JWT Authentication Entry Point
 * 
 * Complete implementation of AuthenticationEntryPoint that handles
 * authentication errors and returns appropriate JSON responses
 */

package com.learning.security.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.learning.security.dto.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class JwtAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper
) : AuthenticationEntryPoint {
    
    private val logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint::class.java)
    
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        logger.warn("Authentication failed: {} for request: {} {}", 
            authException.message, request.method, request.requestURI)
        
        // Log additional details for debugging
        logRequestDetails(request, authException)
        
        // Determine error details based on exception type and request attributes
        val errorDetails = determineErrorDetails(request, authException)
        
        // Set response headers
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.characterEncoding = "UTF-8"
        
        // Add security headers
        addSecurityHeaders(response)
        
        // Create error response
        val errorResponse = createErrorResponse(request, authException, errorDetails)
        
        // Write JSON response
        try {
            response.writer.write(objectMapper.writeValueAsString(errorResponse))
            response.writer.flush()
        } catch (e: Exception) {
            logger.error("Failed to write authentication error response", e)
            // Fallback to simple text response
            response.writer.write("""{"error":"UNAUTHORIZED","message":"Authentication required"}""")
        }
    }
    
    /**
     * Determine error details based on the authentication failure
     */
    private fun determineErrorDetails(
        request: HttpServletRequest,
        authException: AuthenticationException
    ): ErrorDetails {
        // Check for JWT-specific errors from the JWT filter
        val jwtError = request.getAttribute("jwt.error") as? String
        val jwtErrorType = request.getAttribute("jwt.error.type") as? String
        
        return when {
            jwtError != null -> {
                when (jwtErrorType) {
                    "ExpiredJwtException" -> ErrorDetails(
                        error = "TOKEN_EXPIRED",
                        message = "JWT token has expired",
                        suggestion = "Please refresh your token or login again"
                    )
                    "MalformedJwtException" -> ErrorDetails(
                        error = "INVALID_TOKEN",
                        message = "JWT token is malformed",
                        suggestion = "Please provide a valid JWT token"
                    )
                    "SignatureException" -> ErrorDetails(
                        error = "INVALID_SIGNATURE",
                        message = "JWT signature is invalid",
                        suggestion = "Please login again to get a new token"
                    )
                    "UnsupportedJwtException" -> ErrorDetails(
                        error = "UNSUPPORTED_TOKEN",
                        message = "JWT token is not supported",
                        suggestion = "Please use a supported token format"
                    )
                    else -> ErrorDetails(
                        error = "TOKEN_ERROR",
                        message = jwtError,
                        suggestion = "Please check your authentication token"
                    )
                }
            }
            hasAuthorizationHeader(request) -> ErrorDetails(
                error = "INVALID_TOKEN",
                message = "Invalid or expired authentication token",
                suggestion = "Please login again or refresh your token"
            )
            else -> ErrorDetails(
                error = "AUTHENTICATION_REQUIRED",
                message = "Authentication is required to access this resource",
                suggestion = "Please provide a valid JWT token in the Authorization header"
            )
        }
    }
    
    /**
     * Create comprehensive error response
     */
    private fun createErrorResponse(
        request: HttpServletRequest,
        authException: AuthenticationException,
        errorDetails: ErrorDetails
    ): ErrorResponse {
        val details = mutableMapOf<String, Any>()
        
        // Add path information
        details["path"] = request.requestURI
        details["method"] = request.method
        
        // Add timestamp with more readable format
        details["timestamp"] = LocalDateTime.now().toString()
        
        // Add suggestion for client
        details["suggestion"] = errorDetails.suggestion
        
        // Add correlation ID if available (for distributed tracing)
        request.getHeader("X-Correlation-ID")?.let { correlationId ->
            details["correlationId"] = correlationId
        }
        
        // Add client information (for logging/monitoring)
        details["clientIp"] = getClientIpAddress(request)
        
        return ErrorResponse(
            error = errorDetails.error,
            message = errorDetails.message,
            timestamp = System.currentTimeMillis(),
            path = request.requestURI,
            details = details
        )
    }
    
    /**
     * Add security headers to response
     */
    private fun addSecurityHeaders(response: HttpServletResponse) {
        // Prevent caching of error responses
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate")
        response.setHeader("Pragma", "no-cache")
        response.setHeader("Expires", "0")
        
        // Security headers
        response.setHeader("X-Content-Type-Options", "nosniff")
        response.setHeader("X-Frame-Options", "DENY")
        response.setHeader("X-XSS-Protection", "1; mode=block")
        
        // CORS headers for API endpoints
        response.setHeader("Access-Control-Allow-Origin", "*")
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, X-Requested-With")
    }
    
    /**
     * Log request details for security monitoring
     */
    private fun logRequestDetails(request: HttpServletRequest, authException: AuthenticationException) {
        if (logger.isDebugEnabled) {
            val details = mapOf(
                "method" to request.method,
                "uri" to request.requestURI,
                "query" to (request.queryString ?: "none"),
                "userAgent" to (request.getHeader("User-Agent") ?: "none"),
                "referer" to (request.getHeader("Referer") ?: "none"),
                "clientIp" to getClientIpAddress(request),
                "hasAuthHeader" to hasAuthorizationHeader(request),
                "exception" to authException.javaClass.simpleName,
                "message" to authException.message
            )
            
            logger.debug("Authentication failure details: {}", details)
        }
        
        // Log security events for monitoring
        logger.info("Authentication failed for request: {} {} from IP: {} - {}",
            request.method,
            request.requestURI,
            getClientIpAddress(request),
            authException.message
        )
    }
    
    /**
     * Check if request has Authorization header
     */
    private fun hasAuthorizationHeader(request: HttpServletRequest): Boolean {
        val authHeader = request.getHeader("Authorization")
        return !authHeader.isNullOrBlank()
    }
    
    /**
     * Get client IP address considering proxy headers
     */
    private fun getClientIpAddress(request: HttpServletRequest): String {
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        if (!xForwardedFor.isNullOrBlank()) {
            return xForwardedFor.split(",")[0].trim()
        }
        
        val xRealIp = request.getHeader("X-Real-IP")
        if (!xRealIp.isNullOrBlank()) {
            return xRealIp.trim()
        }
        
        return request.remoteAddr ?: "unknown"
    }
    
    /**
     * Data class to hold error details
     */
    private data class ErrorDetails(
        val error: String,
        val message: String,
        val suggestion: String
    )
}