# 🛠️ Workshop 12: Authentication & JWT Security - Enterprise-Grade Security Implementation

## 🎯 Workshop Objectives

In this hands-on workshop, you will:
1. Implement JWT-based authentication with Spring Security
2. Create secure user registration and login endpoints
3. Configure role-based access control (RBAC) with multiple user roles
4. Implement password hashing and validation with BCrypt
5. Create custom authentication filters and security configurations
6. Build comprehensive security testing with @WithMockUser
7. Implement token refresh mechanisms and logout functionality
8. Apply security best practices for production-ready applications

## 🏗️ Project Structure

```
src/main/kotlin/com/learning/security/
├── SecurityApplication.kt
├── config/
│   ├── SecurityConfig.kt
│   └── JwtConfig.kt
├── model/
│   ├── User.kt
│   ├── Role.kt
│   └── RefreshToken.kt
├── repository/
│   ├── UserRepository.kt
│   ├── RoleRepository.kt
│   └── RefreshTokenRepository.kt
├── service/
│   ├── UserService.kt
│   ├── AuthService.kt
│   ├── JwtService.kt
│   └── CustomUserDetailsService.kt
├── controller/
│   ├── AuthController.kt
│   └── UserController.kt
├── dto/
│   ├── AuthDTOs.kt
│   └── UserDTOs.kt
├── filter/
│   └── JwtAuthenticationFilter.kt
└── util/
    └── SecurityUtils.kt

src/test/kotlin/com/learning/security/
├── integration/
│   └── SecurityIntegrationTest.kt
├── unit/
│   ├── JwtServiceTest.kt
│   └── AuthServiceTest.kt
└── web/
    ├── AuthControllerTest.kt
    └── UserControllerTest.kt
```

---

## 📋 Step 1: Project Setup and Dependencies

### 1.1 Create build.gradle.kts

```kotlin
// TODO: Add Spring Boot and Security dependencies

plugins {
    // TODO: Add Kotlin and Spring Boot plugins
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("plugin.jpa") version "1.9.25"
    id("org.springframework.boot") version "3.5.4"
    id("io.spring.dependency-management") version "1.1.6"
}

dependencies {
    // TODO: Add Spring Boot starters
    // - spring-boot-starter-web
    // - spring-boot-starter-data-jpa
    // - spring-boot-starter-security
    // - spring-boot-starter-validation
    
    // TODO: Add JWT dependencies
    // - io.jsonwebtoken:jjwt-api:0.12.3
    // - io.jsonwebtoken:jjwt-impl:0.12.3
    // - io.jsonwebtoken:jjwt-jackson:0.12.3
    
    // TODO: Add Kotlin support
    // - jackson-module-kotlin
    // - kotlin-reflect
    
    // TODO: Add database drivers
    // - H2 for testing
    // - PostgreSQL for production
    
    // TODO: Add testing dependencies
    // - spring-boot-starter-test
    // - spring-security-test
    // - mockk
}
```

### 1.2 Create settings.gradle.kts

```kotlin
// TODO: Set the project name
rootProject.name = "lesson-12-authentication-security"
```

---

## 📋 Step 2: Security Configuration

### 2.1 Create application.yml

```yaml
# TODO: Create comprehensive security configuration
spring:
  profiles:
    active: dev
    
  # TODO: Configure H2 database for development
  datasource:
    # Add H2 configuration for development
    # Add PostgreSQL configuration for production
    
  # TODO: Configure JPA/Hibernate
  jpa:
    # Add JPA settings for security entities
    
  # TODO: Configure security properties
  security:
    # Add basic security configuration
    
# TODO: Configure JWT properties
jwt:
  secret: # Add a strong secret key (min 256 bits for HS256)
  expiration: # Token expiration time in milliseconds (e.g., 86400000 for 24 hours)
  refresh:
    expiration: # Refresh token expiration (e.g., 604800000 for 7 days)

# TODO: Configure logging for security
logging:
  level:
    # Add appropriate logging levels for security components

---
# TODO: Test profile configuration
spring:
  config:
    activate:
      on-profile: test
  # Add test-specific security configuration
```

### 2.2 Create SecurityConfig.kt

```kotlin
// TODO: Create comprehensive Spring Security configuration
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, jsr250Enabled = true)
class SecurityConfig(
    // TODO: Inject required dependencies
    // - jwtAuthenticationFilter: JwtAuthenticationFilter
    // - userDetailsService: UserDetailsService
    // - authenticationEntryPoint: JwtAuthenticationEntryPoint
) {
    
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        // TODO: Configure BCrypt password encoder with appropriate strength
        TODO("Configure BCrypt with 12 rounds for strong security")
    }
    
    @Bean
    fun authenticationManager(
        authConfig: AuthenticationConfiguration
    ): AuthenticationManager {
        // TODO: Configure authentication manager
        TODO("Return authentication manager from configuration")
    }
    
    @Bean
    fun authenticationProvider(): DaoAuthenticationProvider {
        // TODO: Configure DAO authentication provider
        // 1. Create DaoAuthenticationProvider
        // 2. Set user details service
        // 3. Set password encoder
        TODO("Configure authentication provider")
    }
    
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            // TODO: Disable CSRF for stateless API
            .csrf { TODO("Disable CSRF") }
            
            // TODO: Configure session management as stateless
            .sessionManagement { 
                TODO("Set session creation policy to STATELESS") 
            }
            
            // TODO: Configure authorization rules
            .authorizeHttpRequests { auth ->
                auth
                    // TODO: Allow public access to auth endpoints
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/public/**").permitAll()
                    .requestMatchers("/h2-console/**").permitAll()
                    
                    // TODO: Configure role-based access
                    // - GET /api/users - requires USER role
                    // - POST /api/users - requires ADMIN role
                    // - DELETE /api/users/** - requires ADMIN role
                    // - /api/admin/** - requires ADMIN role
                    
                    // TODO: All other requests require authentication
                    .anyRequest().authenticated()
            }
            
            // TODO: Configure exception handling
            .exceptionHandling { 
                TODO("Set authentication entry point") 
            }
            
            // TODO: Add JWT filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(TODO("Add JWT filter"), UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }
}
```

---

## 📋 Step 3: User and Role Models

### 3.1 Create User Entity

```kotlin
// TODO: Create comprehensive User entity with security features
@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    // TODO: Add user identification fields
    @Column(unique = true, nullable = false)
    val username: String,
    
    @Column(unique = true, nullable = false)
    val email: String,
    
    @Column(nullable = false)
    val password: String, // Hashed password
    
    // TODO: Add user profile fields
    val firstName: String,
    val lastName: String,
    
    // TODO: Add account status fields
    @Column(nullable = false)
    val isActive: Boolean = true,
    
    @Column(nullable = false)
    val isLocked: Boolean = false,
    
    @Column(nullable = false)
    val isCredentialsExpired: Boolean = false,
    
    @Column(nullable = false)
    val isEnabled: Boolean = true,
    
    // TODO: Add role relationships (Many-to-Many)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    val roles: Set<Role> = setOf(),
    
    // TODO: Add audit fields
    @CreationTimestamp
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @UpdateTimestamp
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    
    // TODO: Add security helper methods
    fun getAuthorities(): List<GrantedAuthority> {
        // TODO: Convert roles to Spring Security authorities
        // Return list of SimpleGrantedAuthority with "ROLE_" prefix
        TODO("Convert roles to authorities")
    }
    
    fun hasRole(roleName: String): Boolean {
        // TODO: Check if user has specific role
        TODO("Check if user has role")
    }
    
    fun isAccountNonExpired(): Boolean = isActive
    fun isAccountNonLocked(): Boolean = !isLocked
    fun isCredentialsNonExpired(): Boolean = !isCredentialsExpired
    fun isEnabled(): Boolean = isEnabled
    
    fun getFullName(): String = "$firstName $lastName"
    
    fun canLogin(): Boolean {
        // TODO: Check if user can login (active, not locked, enabled)
        TODO("Implement login eligibility check")
    }
}
```

### 3.2 Create Role Entity

```kotlin
// TODO: Create Role entity for RBAC
@Entity
@Table(name = "roles")
data class Role(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(unique = true, nullable = false)
    val name: String, // e.g., "USER", "ADMIN", "MODERATOR"
    
    val description: String,
    
    @CreationTimestamp
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    
    companion object {
        // TODO: Define standard role constants
        const val ROLE_USER = "USER"
        const val ROLE_ADMIN = "ADMIN"
        const val ROLE_MODERATOR = "MODERATOR"
    }
}
```

### 3.3 Create RefreshToken Entity

```kotlin
// TODO: Create RefreshToken entity for token refresh mechanism
@Entity
@Table(name = "refresh_tokens")
data class RefreshToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    // TODO: Add token fields
    @Column(unique = true, nullable = false)
    val token: String,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    
    @Column(nullable = false)
    val expiryDate: LocalDateTime,
    
    @Column(nullable = false)
    val isRevoked: Boolean = false,
    
    @CreationTimestamp
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    
    // TODO: Add helper methods
    fun isExpired(): Boolean {
        // TODO: Check if token is expired
        TODO("Check if token is expired")
    }
    
    fun isValid(): Boolean {
        // TODO: Check if token is valid (not expired and not revoked)
        TODO("Check if token is valid")
    }
}
```

---

## 📋 Step 4: Repository Layer

### 4.1 Create UserRepository

```kotlin
// TODO: Create UserRepository with security-specific queries
interface UserRepository : JpaRepository<User, Long> {
    
    // TODO: Add method for authentication
    fun findByUsername(username: String): User?
    
    fun findByEmail(email: String): User?
    
    // TODO: Add methods for user management
    fun findByUsernameOrEmail(username: String, email: String): User?
    
    fun existsByUsername(username: String): Boolean
    
    fun existsByEmail(email: String): Boolean
    
    // TODO: Add methods for active users
    fun findByIsActiveTrue(): List<User>
    
    fun countByIsActiveTrue(): Long
    
    // TODO: Add methods for role-based queries
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    fun findByRoleName(@Param("roleName") roleName: String): List<User>
    
    @Query("SELECT u FROM User u WHERE u.isActive = true AND u.isLocked = false AND u.isEnabled = true")
    fun findAllActiveUsers(): List<User>
}
```

### 4.2 Create RoleRepository

```kotlin
// TODO: Create RoleRepository for role management
interface RoleRepository : JpaRepository<Role, Long> {
    
    fun findByName(name: String): Role?
    
    fun existsByName(name: String): Boolean
    
    // TODO: Add query to find roles by user
    @Query("SELECT r FROM Role r JOIN r.users u WHERE u.id = :userId")
    fun findByUserId(@Param("userId") userId: Long): Set<Role>
}
```

### 4.3 Create RefreshTokenRepository

```kotlin
// TODO: Create RefreshTokenRepository for token management
interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    
    fun findByToken(token: String): RefreshToken?
    
    fun findByUser(user: User): List<RefreshToken>
    
    fun deleteByUser(user: User): Int
    
    // TODO: Add method to revoke all tokens for a user
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.user = :user")
    fun revokeAllByUser(@Param("user") user: User): Int
    
    // TODO: Add method to clean up expired tokens
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    fun deleteExpiredTokens(@Param("now") now: LocalDateTime): Int
}
```

---

## 📋 Step 5: JWT Service Implementation

### 5.1 Create JwtService

```kotlin
// TODO: Create comprehensive JWT service
@Service
class JwtService {
    
    @Value("\${jwt.secret}")
    private lateinit var secret: String
    
    @Value("\${jwt.expiration}")
    private var expiration: Long = 86400000 // 24 hours
    
    // TODO: Implement token generation
    fun generateToken(userDetails: UserDetails): String {
        val claims = HashMap<String, Any>()
        
        // TODO: Add custom claims
        if (userDetails is CustomUserDetails) {
            // Add user ID, email, roles to claims
            TODO("Add custom claims to JWT")
        }
        
        return createToken(claims, userDetails.username)
    }
    
    // TODO: Implement token creation
    private fun createToken(claims: Map<String, Any>, subject: String): String {
        // TODO: Use JJWT library to create signed JWT
        // 1. Set claims and subject
        // 2. Set issued date and expiration
        // 3. Sign with HS512 algorithm
        TODO("Create JWT token with JJWT")
    }
    
    // TODO: Implement token validation
    fun validateToken(token: String, userDetails: UserDetails): Boolean {
        // TODO: Validate token against user details
        // 1. Extract username from token
        // 2. Check if username matches
        // 3. Check if token is not expired
        TODO("Validate JWT token")
    }
    
    // TODO: Implement claim extraction
    fun extractUsername(token: String): String {
        // TODO: Extract username (subject) from token
        TODO("Extract username from JWT")
    }
    
    fun extractExpiration(token: String): Date {
        // TODO: Extract expiration date from token
        TODO("Extract expiration from JWT")
    }
    
    fun <T> extractClaim(token: String, claimsResolver: (Claims) -> T): T {
        // TODO: Extract specific claim from token
        TODO("Extract claim from JWT")
    }
    
    // TODO: Implement token parsing
    private fun extractAllClaims(token: String): Claims {
        // TODO: Parse JWT token and extract all claims
        // Use JJWT parser with secret key
        TODO("Parse JWT and extract claims")
    }
    
    private fun isTokenExpired(token: String): Boolean {
        // TODO: Check if token is expired
        TODO("Check token expiration")
    }
    
    // TODO: Implement token refresh
    fun refreshToken(token: String): String? {
        // TODO: Create new token from existing token claims
        TODO("Refresh JWT token")
    }
}
```

### 5.2 Create CustomUserDetails

```kotlin
// TODO: Create custom UserDetails implementation
class CustomUserDetails(
    private val user: User
) : UserDetails {
    
    // TODO: Implement UserDetails methods
    override fun getAuthorities(): Collection<GrantedAuthority> {
        // TODO: Return user authorities from roles
        TODO("Return user authorities")
    }
    
    override fun getPassword(): String = user.password
    
    override fun getUsername(): String = user.username
    
    override fun isAccountNonExpired(): Boolean = user.isAccountNonExpired()
    
    override fun isAccountNonLocked(): Boolean = user.isAccountNonLocked()
    
    override fun isCredentialsNonExpired(): Boolean = user.isCredentialsNonExpired()
    
    override fun isEnabled(): Boolean = user.isEnabled()
    
    // TODO: Add custom methods
    fun getId(): Long? = user.id
    
    fun getEmail(): String = user.email
    
    fun getUser(): User = user
}
```

---

## 📋 Step 6: Authentication Services

### 6.1 Create CustomUserDetailsService

```kotlin
// TODO: Create custom UserDetailsService
@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {
    
    override fun loadUserByUsername(username: String): UserDetails {
        // TODO: Load user by username or email
        // 1. Find user by username
        // 2. If not found, throw UsernameNotFoundException
        // 3. Return CustomUserDetails instance
        TODO("Load user for authentication")
    }
}
```

### 6.2 Create AuthService

```kotlin
// TODO: Create authentication service
@Service
@Transactional
class AuthService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService,
    private val refreshTokenService: RefreshTokenService
) {
    
    // TODO: Implement user registration
    fun register(registerRequest: RegisterRequest): AuthResponse {
        // TODO: Implement user registration
        // 1. Validate registration data
        // 2. Check if username/email already exists
        // 3. Hash password
        // 4. Create user with default role
        // 5. Generate JWT token
        // 6. Return auth response
        TODO("Implement user registration")
    }
    
    // TODO: Implement user authentication
    fun authenticate(loginRequest: LoginRequest): AuthResponse {
        // TODO: Implement user authentication
        // 1. Authenticate with AuthenticationManager
        // 2. Load user details
        // 3. Generate JWT token
        // 4. Create refresh token
        // 5. Return auth response
        TODO("Implement user authentication")
    }
    
    // TODO: Implement token refresh
    fun refreshToken(refreshTokenRequest: RefreshTokenRequest): AuthResponse {
        // TODO: Implement token refresh
        // 1. Validate refresh token
        // 2. Generate new JWT token
        // 3. Return new auth response
        TODO("Implement token refresh")
    }
    
    // TODO: Implement logout
    fun logout(refreshToken: String): Boolean {
        // TODO: Implement logout
        // 1. Revoke refresh token
        // 2. Return success status
        TODO("Implement logout")
    }
    
    // TODO: Helper methods
    private fun validateRegistrationData(request: RegisterRequest) {
        // TODO: Validate registration data
        // Check username length, email format, password strength
        TODO("Validate registration data")
    }
    
    private fun getDefaultRole(): Role {
        // TODO: Get default USER role
        TODO("Get default role")
    }
}
```

---

## 📋 Step 7: JWT Authentication Filter

### 7.1 Create JwtAuthenticationFilter

```kotlin
// TODO: Create JWT authentication filter
@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userDetailsService: UserDetailsService
) : OncePerRequestFilter() {
    
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // TODO: Implement JWT authentication filter
        // 1. Extract JWT token from Authorization header
        // 2. Validate token format (Bearer token)
        // 3. Extract username from token
        // 4. Load user details
        // 5. Validate token
        // 6. Set authentication in SecurityContext
        // 7. Continue filter chain
        TODO("Implement JWT authentication filter")
    }
    
    private fun extractTokenFromRequest(request: HttpServletRequest): String? {
        // TODO: Extract JWT token from Authorization header
        // Check for "Bearer " prefix and extract token
        TODO("Extract JWT token from request")
    }
}
```

### 7.2 Create JwtAuthenticationEntryPoint

```kotlin
// TODO: Create custom authentication entry point
@Component
class JwtAuthenticationEntryPoint : AuthenticationEntryPoint {
    
    private val logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint::class.java)
    
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        // TODO: Handle authentication errors
        // 1. Log the error
        // 2. Set response status to 401
        // 3. Return JSON error response
        TODO("Handle authentication errors")
    }
}
```

---

## 📋 Step 8: REST Controllers

### 8.1 Create DTOs

```kotlin
// TODO: Create authentication DTOs

// Registration request
data class RegisterRequest(
    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 50, message = "Username must be 3-50 characters")
    val username: String,
    
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be valid")
    val email: String,
    
    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val password: String,
    
    @field:NotBlank(message = "First name is required")
    val firstName: String,
    
    @field:NotBlank(message = "Last name is required")
    val lastName: String
)

// Login request
data class LoginRequest(
    @field:NotBlank(message = "Username is required")
    val username: String,
    
    @field:NotBlank(message = "Password is required")
    val password: String
)

// Authentication response
data class AuthResponse(
    val token: String,
    val refreshToken: String,
    val user: UserResponse
)

// User response
data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val roles: Set<String>
) {
    companion object {
        fun from(user: User): UserResponse {
            // TODO: Convert User entity to UserResponse
            TODO("Convert User to UserResponse")
        }
    }
}

// Refresh token request
data class RefreshTokenRequest(
    @field:NotBlank(message = "Refresh token is required")
    val refreshToken: String
)
```

### 8.2 Create AuthController

```kotlin
// TODO: Create authentication controller
@RestController
@RequestMapping("/api/auth")
@Validated
class AuthController(
    private val authService: AuthService
) {
    
    // TODO: Implement registration endpoint
    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> {
        // TODO: Call auth service to register user
        // Return 201 Created with auth response
        TODO("Implement registration endpoint")
    }
    
    // TODO: Implement login endpoint
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        // TODO: Call auth service to authenticate user
        // Return 200 OK with auth response
        TODO("Implement login endpoint")
    }
    
    // TODO: Implement token refresh endpoint
    @PostMapping("/refresh")
    fun refreshToken(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<AuthResponse> {
        // TODO: Call auth service to refresh token
        // Return 200 OK with new auth response
        TODO("Implement token refresh endpoint")
    }
    
    // TODO: Implement logout endpoint
    @PostMapping("/logout")
    fun logout(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<Map<String, String>> {
        // TODO: Call auth service to logout user
        // Return 200 OK with success message
        TODO("Implement logout endpoint")
    }
}
```

### 8.3 Create UserController

```kotlin
// TODO: Create user management controller
@RestController
@RequestMapping("/api/users")
@Validated
class UserController(
    private val userService: UserService
) {
    
    // TODO: Implement get current user endpoint
    @GetMapping("/me")
    fun getCurrentUser(authentication: Authentication): ResponseEntity<UserResponse> {
        // TODO: Get current authenticated user
        // Return user information
        TODO("Implement get current user endpoint")
    }
    
    // TODO: Implement get all users (admin only)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllUsers(): ResponseEntity<List<UserResponse>> {
        // TODO: Get all users (admin only)
        TODO("Implement get all users endpoint")
    }
    
    // TODO: Implement get user by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.id == #id")
    fun getUserById(@PathVariable id: Long): ResponseEntity<UserResponse> {
        // TODO: Get user by ID with authorization check
        TODO("Implement get user by ID endpoint")
    }
    
    // TODO: Implement update user endpoint
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.id == #id")
    fun updateUser(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateUserRequest
    ): ResponseEntity<UserResponse> {
        // TODO: Update user with authorization check
        TODO("Implement update user endpoint")
    }
    
    // TODO: Implement delete user endpoint (admin only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteUser(@PathVariable id: Long): ResponseEntity<Void> {
        // TODO: Delete user (admin only)
        TODO("Implement delete user endpoint")
    }
}
```

---

## 📋 Step 9: Comprehensive Security Testing

### 9.1 Create AuthController Tests

```kotlin
// TODO: Create comprehensive authentication controller tests
@WebMvcTest(AuthController::class)
class AuthControllerTest {
    
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @MockBean
    private lateinit var authService: AuthService
    
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    
    @Test
    fun `should register user successfully`() {
        // TODO: Test successful user registration
        // 1. Create valid registration request
        // 2. Mock auth service response
        // 3. Perform POST request
        // 4. Verify response status and content
        TODO("Test successful user registration")
    }
    
    @Test
    fun `should reject registration with invalid data`() {
        // TODO: Test registration validation
        // Test with invalid username, email, password
        TODO("Test registration validation")
    }
    
    @Test
    fun `should authenticate user with valid credentials`() {
        // TODO: Test successful authentication
        TODO("Test successful authentication")
    }
    
    @Test
    fun `should reject authentication with invalid credentials`() {
        // TODO: Test authentication failure
        TODO("Test authentication failure")
    }
    
    @Test
    fun `should refresh token successfully`() {
        // TODO: Test token refresh
        TODO("Test token refresh")
    }
    
    @Test
    fun `should logout user successfully`() {
        // TODO: Test user logout
        TODO("Test user logout")
    }
}
```

### 9.2 Create Security Integration Tests

```kotlin
// TODO: Create security integration tests
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SecurityIntegrationTest {
    
    @Autowired
    private lateinit var testRestTemplate: TestRestTemplate
    
    @Autowired
    private lateinit var userRepository: UserRepository
    
    @Test
    fun `should allow access to public endpoints`() {
        // TODO: Test public endpoint access
        TODO("Test public endpoint access")
    }
    
    @Test
    fun `should deny access to protected endpoints without token`() {
        // TODO: Test protected endpoint access without authentication
        TODO("Test unauthorized access")
    }
    
    @Test
    @WithMockUser(roles = ["USER"])
    fun `should allow user access to user endpoints`() {
        // TODO: Test user role access
        TODO("Test user role access")
    }
    
    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should allow admin access to all endpoints`() {
        // TODO: Test admin role access
        TODO("Test admin role access")
    }
    
    @Test
    fun `should handle JWT authentication flow`() {
        // TODO: Test complete JWT authentication flow
        // 1. Register user
        // 2. Login and get token
        // 3. Use token to access protected endpoint
        // 4. Refresh token
        // 5. Logout
        TODO("Test complete JWT flow")
    }
}
```

### 9.3 Create JWT Service Tests

```kotlin
// TODO: Create JWT service unit tests
class JwtServiceTest {
    
    private lateinit var jwtService: JwtService
    
    @BeforeEach
    fun setUp() {
        // TODO: Set up JWT service with test configuration
        TODO("Set up JWT service for testing")
    }
    
    @Test
    fun `should generate valid JWT token`() {
        // TODO: Test JWT token generation
        TODO("Test JWT token generation")
    }
    
    @Test
    fun `should validate JWT token correctly`() {
        // TODO: Test JWT token validation
        TODO("Test JWT token validation")
    }
    
    @Test
    fun `should extract username from token`() {
        // TODO: Test username extraction
        TODO("Test username extraction")
    }
    
    @Test
    fun `should detect expired tokens`() {
        // TODO: Test expired token detection
        TODO("Test expired token detection")
    }
    
    @Test
    fun `should refresh token successfully`() {
        // TODO: Test token refresh
        TODO("Test token refresh")
    }
}
```

---

## 📋 Step 10: Data Initialization and Testing

### 10.1 Create Database Initialization

```kotlin
// TODO: Create data initialization for testing
@Component
class DataInitializer(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder
) : ApplicationRunner {
    
    override fun run(args: ApplicationArguments) {
        // TODO: Initialize default roles and admin user
        // 1. Create default roles (USER, ADMIN)
        // 2. Create admin user if not exists
        // 3. Create test users for development
        TODO("Initialize default data")
    }
    
    private fun createDefaultRoles() {
        // TODO: Create default roles
        TODO("Create default roles")
    }
    
    private fun createAdminUser() {
        // TODO: Create admin user
        TODO("Create admin user")
    }
}
```

### 10.2 Create Security Utils

```kotlin
// TODO: Create security utility functions
object SecurityUtils {
    
    fun getCurrentUsername(): String? {
        // TODO: Get current authenticated username
        TODO("Get current username")
    }
    
    fun getCurrentUser(): CustomUserDetails? {
        // TODO: Get current authenticated user details
        TODO("Get current user details")
    }
    
    fun hasRole(roleName: String): Boolean {
        // TODO: Check if current user has specific role
        TODO("Check user role")
    }
    
    fun isAdmin(): Boolean {
        // TODO: Check if current user is admin
        TODO("Check if user is admin")
    }
    
    fun generateSecurePassword(): String {
        // TODO: Generate secure random password
        TODO("Generate secure password")
    }
}
```

---

## 🎯 Expected Results

After completing this workshop, you should have:

1. **Complete JWT Authentication System**: Registration, login, token refresh, and logout
2. **Role-Based Access Control**: Different access levels for users and admins
3. **Secure Password Handling**: BCrypt hashing with proper strength
4. **Custom Security Configuration**: Spring Security configured for stateless JWT
5. **Comprehensive Testing**: Unit tests, integration tests, and security tests
6. **Production-Ready Security**: Error handling, validation, and security headers

## 🚀 Testing Your Implementation

Run these commands to verify your security implementation:

```bash
# Build and test
./gradlew clean build

# Run specific security tests
./gradlew test --tests "*Auth*"
./gradlew test --tests "*Security*"

# Run the application
./gradlew bootRun

# Test endpoints with curl
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"password123","firstName":"Test","lastName":"User"}'

curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'
```

## 💡 Key Learning Points

- **JWT Implementation**: Stateless authentication with secure token handling
- **Spring Security Configuration**: Filter chain, authentication providers, and method security
- **Role-Based Access Control**: Implementing RBAC with fine-grained permissions
- **Password Security**: Modern hashing techniques and password policies
- **Security Testing**: Comprehensive testing strategies for authentication systems
- **Production Security**: Best practices for secure API development

Good luck building a secure, production-ready authentication system!