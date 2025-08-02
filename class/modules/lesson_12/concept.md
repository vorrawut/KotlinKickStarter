# 🎯 Lesson 12: Authentication & JWT Security

## Objective

Implement robust authentication and authorization using JWT tokens and Spring Security. Learn to secure API endpoints, manage user sessions, and implement role-based access control for production applications.

## Key Concepts

### 1. Spring Security Configuration

```kotlin
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig {
    
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
    
    @Bean
    fun jwtAuthenticationEntryPoint(): AuthenticationEntryPoint {
        return AuthenticationEntryPoint { request, response, authException ->
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
        }
    }
    
    @Bean
    fun filterChain(
        http: HttpSecurity,
        jwtAuthenticationEntryPoint: AuthenticationEntryPoint,
        jwtRequestFilter: JwtRequestFilter
    ): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/public/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/payments/**").hasAnyRole("USER", "ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/payments/**").hasAnyRole("USER", "ADMIN")
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
            }
            .exceptionHandling { it.authenticationEntryPoint(jwtAuthenticationEntryPoint) }
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }
}
```

### 2. JWT Token Management

```kotlin
@Component
class JwtTokenUtil {
    
    @Value("\${jwt.secret}")
    private lateinit var secret: String
    
    @Value("\${jwt.expiration}")
    private var expiration: Int = 86400 // 24 hours
    
    fun generateToken(userDetails: UserDetails): String {
        val claims = mapOf<String, Any>(
            "sub" to userDetails.username,
            "roles" to userDetails.authorities.map { it.authority },
            "iat" to Date().time / 1000,
            "exp" to (Date().time / 1000) + expiration
        )
        
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(userDetails.username)
            .setIssuedAt(Date())
            .setExpiration(Date(Date().time + expiration * 1000))
            .signWith(SignatureAlgorithm.HS256, secret)
            .compact()
    }
    
    fun validateToken(token: String, userDetails: UserDetails): Boolean {
        val username = getUsernameFromToken(token)
        return username == userDetails.username && !isTokenExpired(token)
    }
    
    fun getUsernameFromToken(token: String): String {
        return getClaimFromToken(token) { it.subject }
    }
    
    fun getExpirationDateFromToken(token: String): Date {
        return getClaimFromToken(token) { it.expiration }
    }
    
    private fun <T> getClaimFromToken(token: String, claimsResolver: (Claims) -> T): T {
        val claims = getAllClaimsFromToken(token)
        return claimsResolver(claims)
    }
    
    private fun getAllClaimsFromToken(token: String): Claims {
        return Jwts.parser()
            .setSigningKey(secret)
            .parseClaimsJws(token)
            .body
    }
    
    private fun isTokenExpired(token: String): Boolean {
        val expiration = getExpirationDateFromToken(token)
        return expiration.before(Date())
    }
}
```

### 3. JWT Filter Implementation

```kotlin
@Component
class JwtRequestFilter(
    private val userDetailsService: UserDetailsService,
    private val jwtTokenUtil: JwtTokenUtil
) : OncePerRequestFilter() {
    
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestTokenHeader = request.getHeader("Authorization")
        
        var username: String? = null
        var jwtToken: String? = null
        
        if (requestTokenHeader?.startsWith("Bearer ") == true) {
            jwtToken = requestTokenHeader.substring(7)
            try {
                username = jwtTokenUtil.getUsernameFromToken(jwtToken)
            } catch (e: IllegalArgumentException) {
                logger.error("Unable to get JWT Token")
            } catch (e: ExpiredJwtException) {
                logger.error("JWT Token has expired")
            }
        }
        
        if (username != null && SecurityContextHolder.getContext().authentication == null) {
            val userDetails = userDetailsService.loadUserByUsername(username)
            
            if (jwtTokenUtil.validateToken(jwtToken!!, userDetails)) {
                val usernamePasswordAuthenticationToken = UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.authorities
                )
                usernamePasswordAuthenticationToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = usernamePasswordAuthenticationToken
            }
        }
        
        filterChain.doFilter(request, response)
    }
}
```

### 4. Authentication Controller

```kotlin
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val userService: UserService,
    private val jwtTokenUtil: JwtTokenUtil
) {
    
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        try {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(request.email, request.password)
            )
        } catch (e: BadCredentialsException) {
            throw BadCredentialsException("Invalid credentials", e)
        }
        
        val userDetails = userService.loadUserByUsername(request.email)
        val token = jwtTokenUtil.generateToken(userDetails)
        
        return ResponseEntity.ok(
            AuthResponse(
                token = token,
                type = "Bearer",
                email = userDetails.username,
                roles = userDetails.authorities.map { it.authority }
            )
        )
    }
    
    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> {
        if (userService.existsByEmail(request.email)) {
            throw UserAlreadyExistsException("Email already registered: ${request.email}")
        }
        
        val user = userService.createUser(request)
        val userDetails = userService.loadUserByUsername(user.email)
        val token = jwtTokenUtil.generateToken(userDetails)
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
            AuthResponse(
                token = token,
                type = "Bearer",
                email = userDetails.username,
                roles = userDetails.authorities.map { it.authority }
            )
        )
    }
    
    @PostMapping("/refresh")
    @PreAuthorize("hasRole('USER')")
    fun refreshToken(authentication: Authentication): ResponseEntity<AuthResponse> {
        val userDetails = authentication.principal as UserDetails
        val token = jwtTokenUtil.generateToken(userDetails)
        
        return ResponseEntity.ok(
            AuthResponse(
                token = token,
                type = "Bearer",
                email = userDetails.username,
                roles = userDetails.authorities.map { it.authority }
            )
        )
    }
}

data class LoginRequest(
    @field:Email(message = "Invalid email format")
    val email: String,
    
    @field:NotBlank(message = "Password is required")
    val password: String
)

data class RegisterRequest(
    @field:NotBlank(message = "Name is required")
    val name: String,
    
    @field:Email(message = "Invalid email format")
    val email: String,
    
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val password: String
)

data class AuthResponse(
    val token: String,
    val type: String,
    val email: String,
    val roles: List<String>
)
```

### 5. Role-Based Access Control

```kotlin
@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,
    
    @Column(unique = true, nullable = false)
    val email: String,
    
    @Column(nullable = false)
    val name: String,
    
    @Column(nullable = false)
    val password: String,
    
    @Column(nullable = false)
    val isEnabled: Boolean = true,
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    val roles: Set<Role> = emptySet()
)

@Entity
@Table(name = "roles")
data class Role(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    val name: RoleName
)

enum class RoleName {
    ROLE_USER, ROLE_ADMIN, ROLE_MODERATOR
}

@Component
class UserDetailsServiceImpl(
    private val userRepository: UserRepository
) : UserDetailsService {
    
    override fun loadUserByUsername(email: String): UserDetails {
        val user = userRepository.findByEmail(email)
            ?: throw UsernameNotFoundException("User not found: $email")
        
        return UserPrincipal.create(user)
    }
}

data class UserPrincipal(
    val id: String,
    val name: String,
    private val email: String,
    private val password: String,
    private val authorities: Collection<GrantedAuthority>
) : UserDetails {
    
    override fun getAuthorities(): Collection<GrantedAuthority> = authorities
    override fun getPassword(): String = password
    override fun getUsername(): String = email
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true
    
    companion object {
        fun create(user: User): UserPrincipal {
            val authorities = user.roles.map { 
                SimpleGrantedAuthority(it.name.name) 
            }
            
            return UserPrincipal(
                id = user.id!!,
                name = user.name,
                email = user.email,
                password = user.password,
                authorities = authorities
            )
        }
    }
}
```

## Security Best Practices

### ✅ Do:
- **Use strong, unique JWT secrets** - store in environment variables
- **Implement proper token expiration** - short-lived access tokens
- **Hash passwords securely** - use BCrypt with high work factor
- **Validate all inputs** - prevent injection attacks
- **Use HTTPS in production** - encrypt all communications

### ❌ Avoid:
- **Storing sensitive data in JWT** - tokens can be decoded
- **Using weak secrets** - use cryptographically strong keys
- **Ignoring token expiration** - implement refresh token strategy
- **Exposing internal user details** - sanitize responses

This lesson teaches you to build secure, production-ready authentication systems that protect user data and API endpoints.