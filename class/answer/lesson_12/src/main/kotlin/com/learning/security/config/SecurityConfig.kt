/**
 * Lesson 12 Complete Solution: Security Configuration
 * 
 * Complete Spring Security configuration with JWT authentication,
 * role-based access control, and comprehensive security settings
 */

package com.learning.security.config

import com.learning.security.filter.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import com.learning.security.service.CustomUserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
    prePostEnabled = true,    // Enable @PreAuthorize and @PostAuthorize
    jsr250Enabled = true,     // Enable @RolesAllowed
    securedEnabled = true     // Enable @Secured
)
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val userDetailsService: CustomUserDetailsService,
    private val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint
) {
    
    /**
     * Password encoder bean with BCrypt (12 rounds for strong security)
     */
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder(12)
    }
    
    /**
     * Authentication manager bean
     */
    @Bean
    fun authenticationManager(authConfig: AuthenticationConfiguration): AuthenticationManager {
        return authConfig.authenticationManager
    }
    
    /**
     * DAO authentication provider for username/password authentication
     */
    @Bean
    fun authenticationProvider(): DaoAuthenticationProvider {
        val authProvider = DaoAuthenticationProvider()
        authProvider.setUserDetailsService(userDetailsService)
        authProvider.setPasswordEncoder(passwordEncoder())
        authProvider.setHideUserNotFoundExceptions(false) // Better error messages for debugging
        return authProvider
    }
    
    /**
     * CORS configuration
     */
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        
        // Allow specific origins (configure as needed for your environment)
        configuration.allowedOriginPatterns = listOf(
            "http://localhost:*",
            "https://localhost:*",
            "http://127.0.0.1:*",
            "https://127.0.0.1:*"
        )
        
        // Allow all HTTP methods
        configuration.allowedMethods = listOf(
            HttpMethod.GET.name(),
            HttpMethod.POST.name(),
            HttpMethod.PUT.name(),
            HttpMethod.DELETE.name(),
            HttpMethod.OPTIONS.name(),
            HttpMethod.PATCH.name()
        )
        
        // Allow all headers
        configuration.allowedHeaders = listOf("*")
        
        // Allow credentials (needed for cookies, authorization headers)
        configuration.allowCredentials = true
        
        // Cache preflight response for 1 hour
        configuration.maxAge = 3600L
        
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
    
    /**
     * Main security filter chain configuration
     */
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            // Disable CSRF as we're using stateless JWT authentication
            .csrf { csrf -> csrf.disable() }
            
            // Enable CORS with our configuration
            .cors { cors -> cors.configurationSource(corsConfigurationSource()) }
            
            // Set session management to stateless (no server-side sessions)
            .sessionManagement { session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) 
            }
            
            // Configure authorization rules
            .authorizeHttpRequests { auth ->
                auth
                    // Public endpoints - no authentication required
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/public/**").permitAll()
                    .requestMatchers("/h2-console/**").permitAll()
                    .requestMatchers("/actuator/health").permitAll()
                    .requestMatchers("/error").permitAll()
                    
                    // Static resources
                    .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                    
                    // API documentation (if using Swagger/OpenAPI)
                    .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                    
                    // User endpoints - role-based access
                    .requestMatchers(HttpMethod.GET, "/api/users/me").hasRole("USER")
                    .requestMatchers(HttpMethod.PUT, "/api/users/me").hasRole("USER")
                    .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/users").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/users/**").hasAnyRole("ADMIN", "USER")
                    .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
                    
                    // Admin endpoints - admin only
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    
                    // User management endpoints with specific permissions
                    .requestMatchers(HttpMethod.POST, "/api/users/*/lock").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/users/*/unlock").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/users/*/audit").hasRole("ADMIN")
                    
                    // Password change endpoints
                    .requestMatchers(HttpMethod.POST, "/api/users/*/change-password").hasRole("USER")
                    .requestMatchers(HttpMethod.POST, "/api/auth/change-password").hasRole("USER")
                    
                    // All other requests require authentication
                    .anyRequest().authenticated()
            }
            
            // Configure exception handling
            .exceptionHandling { exceptions ->
                exceptions
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                    .accessDeniedHandler { request, response, accessDeniedException ->
                        response.status = 403
                        response.contentType = "application/json"
                        response.writer.write("""
                            {
                                "error": "FORBIDDEN",
                                "message": "Access denied - insufficient privileges",
                                "timestamp": ${System.currentTimeMillis()},
                                "path": "${request.requestURI}"
                            }
                        """.trimIndent())
                    }
            }
            
            // Add JWT filter before username/password authentication filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            
            // Configure authentication provider
            .authenticationProvider(authenticationProvider())
            
            // H2 Console specific configuration (development only)
            .headers { headers ->
                headers.frameOptions().sameOrigin() // Allow H2 console iframe
            }
            
            .build()
    }
}

/**
 * Additional security configuration for method-level security
 */
@Configuration
class MethodSecurityConfig {
    
    /**
     * Custom method security expressions can be added here
     * For example: @PreAuthorize("@customSecurityService.canAccess(authentication, #id)")
     */
}