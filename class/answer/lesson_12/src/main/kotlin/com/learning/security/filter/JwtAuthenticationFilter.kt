/**
 * Lesson 12 Complete Solution: JWT Authentication Filter
 * 
 * Complete JWT authentication filter that intercepts requests,
 * validates JWT tokens, and sets up Spring Security context
 */

package com.learning.security.filter

import com.learning.security.service.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userDetailsService: UserDetailsService
) : OncePerRequestFilter() {
    
    private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)
    
    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
        private val EXCLUDED_PATHS = setOf(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh",
            "/api/public",
            "/h2-console",
            "/actuator/health",
            "/error"
        )
    }
    
    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            // Skip JWT processing for excluded paths
            if (shouldSkipJwtProcessing(request)) {
                logger.debug("Skipping JWT processing for path: ${request.requestURI}")
                filterChain.doFilter(request, response)
                return
            }
            
            // Extract JWT token from request
            val jwtToken = extractTokenFromRequest(request)
            
            if (jwtToken != null && SecurityContextHolder.getContext().authentication == null) {
                authenticateUser(jwtToken, request)
            } else {
                logger.debug("No JWT token found or authentication already set")
            }
            
        } catch (e: Exception) {
            logger.error("JWT authentication failed", e)
            // Clear security context on any error
            SecurityContextHolder.clearContext()
            
            // Set error details in request attributes for potential use by exception handlers
            request.setAttribute("jwt.error", e.message)
            request.setAttribute("jwt.error.type", e.javaClass.simpleName)
        }
        
        filterChain.doFilter(request, response)
    }
    
    /**
     * Extract JWT token from Authorization header
     */
    private fun extractTokenFromRequest(request: HttpServletRequest): String? {
        val authorizationHeader = request.getHeader(AUTHORIZATION_HEADER)
        
        return when {
            authorizationHeader == null -> {
                logger.debug("No Authorization header found")
                null
            }
            !authorizationHeader.startsWith(BEARER_PREFIX) -> {
                logger.debug("Authorization header does not start with Bearer")
                null
            }
            authorizationHeader.length <= BEARER_PREFIX.length -> {
                logger.debug("Authorization header contains empty token")
                null
            }
            else -> {
                val token = authorizationHeader.substring(BEARER_PREFIX.length).trim()
                if (token.isBlank()) {
                    logger.debug("Extracted token is blank")
                    null
                } else {
                    logger.debug("JWT token extracted successfully")
                    token
                }
            }
        }
    }
    
    /**
     * Authenticate user using JWT token
     */
    private fun authenticateUser(jwtToken: String, request: HttpServletRequest) {
        try {
            // Extract username from token
            val username = jwtService.extractUsername(jwtToken)
            
            if (username.isBlank()) {
                logger.warn("JWT token contains blank username")
                return
            }
            
            logger.debug("Authenticating user from JWT: $username")
            
            // Load user details
            val userDetails = userDetailsService.loadUserByUsername(username)
            
            // Validate token against user details
            if (jwtService.validateToken(jwtToken, userDetails)) {
                // Create authentication token
                val authentication = UsernamePasswordAuthenticationToken(
                    userDetails,
                    null, // No credentials needed for JWT
                    userDetails.authorities
                )
                
                // Set authentication details
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                
                // Set authentication in security context
                SecurityContextHolder.getContext().authentication = authentication
                
                logger.debug("User authenticated successfully: $username")
                
                // Add useful attributes to request
                request.setAttribute("jwt.username", username)
                request.setAttribute("jwt.userId", jwtService.extractUserId(jwtToken))
                request.setAttribute("jwt.roles", jwtService.extractRoles(jwtToken))
                
            } else {
                logger.warn("JWT token validation failed for user: $username")
                SecurityContextHolder.clearContext()
            }
            
        } catch (e: Exception) {
            logger.error("Error during JWT authentication", e)
            SecurityContextHolder.clearContext()
            throw e
        }
    }
    
    /**
     * Determine if JWT processing should be skipped for this request
     */
    private fun shouldSkipJwtProcessing(request: HttpServletRequest): Boolean {
        val requestPath = request.requestURI
        val method = request.method
        
        // Skip for excluded paths
        if (EXCLUDED_PATHS.any { requestPath.startsWith(it) }) {
            return true
        }
        
        // Skip for OPTIONS requests (CORS preflight)
        if ("OPTIONS".equals(method, ignoreCase = true)) {
            return true
        }
        
        // Skip for static resources
        if (requestPath.matches(Regex(".*\\.(css|js|png|jpg|jpeg|gif|ico|woff|woff2|ttf|eot|svg)$"))) {
            return true
        }
        
        return false
    }
    
    /**
     * Check if request path is excluded from JWT processing
     */
    private fun isExcludedPath(requestPath: String): Boolean {
        return EXCLUDED_PATHS.any { excludedPath ->
            requestPath.startsWith(excludedPath)
        }
    }
    
    /**
     * Log request details for debugging
     */
    private fun logRequestDetails(request: HttpServletRequest) {
        if (logger.isDebugEnabled) {
            logger.debug("""
                |JWT Filter Request Details:
                |  Method: ${request.method}
                |  URI: ${request.requestURI}
                |  Query: ${request.queryString ?: "none"}
                |  Auth Header: ${request.getHeader(AUTHORIZATION_HEADER)?.let { 
                    if (it.startsWith(BEARER_PREFIX)) "Bearer [REDACTED]" else it 
                } ?: "none"}
                |  Remote Addr: ${request.remoteAddr}
                |  User Agent: ${request.getHeader("User-Agent") ?: "none"}
            """.trimMargin())
        }
    }
    
    /**
     * Check if the current request has valid authentication
     */
    private fun hasValidAuthentication(): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication != null && authentication.isAuthenticated
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
     * Extract user agent from request
     */
    private fun getUserAgent(request: HttpServletRequest): String {
        return request.getHeader("User-Agent") ?: "unknown"
    }
}