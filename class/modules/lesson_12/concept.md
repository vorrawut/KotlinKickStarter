# 📚 Lesson 12: Authentication & JWT Security - Enterprise-Grade Security Implementation

## 🎯 Learning Objectives

By the end of this lesson, you will be able to:
- Master authentication vs authorization concepts and implement both securely
- Design and implement JWT-based authentication systems with token refresh
- Configure Spring Security for enterprise-grade security requirements
- Implement role-based access control (RBAC) with fine-grained permissions
- Secure passwords using modern hashing algorithms and salt strategies
- Create comprehensive security testing with @WithMockUser and security contexts
- Implement OAuth2-style authentication flows with proper token management
- Design secure APIs that follow security best practices and industry standards

## 🔐 Security Fundamentals

### Authentication vs Authorization

**Authentication** (AuthN): *"Who are you?"*
- Verifying the identity of a user or system
- Confirming credentials (username/password, tokens, certificates)
- Establishing a security context for subsequent requests
- Typically happens once per session or token lifetime

**Authorization** (AuthZ): *"What can you do?"*
- Determining what an authenticated user is allowed to do
- Checking permissions, roles, and access rights
- Enforced on every protected resource access
- Can be role-based, attribute-based, or resource-based

### Security Principles

**Defense in Depth:**
- Multiple layers of security controls
- No single point of failure
- Each layer provides independent protection
- Combination of preventive, detective, and corrective controls

**Principle of Least Privilege:**
- Users get minimal access needed for their role
- Permissions are granted explicitly, not by default
- Regular access reviews and permission audits
- Time-limited access for sensitive operations

## 🎟️ JSON Web Tokens (JWT) Deep Dive

### JWT Structure and Components

```
JWT = Base64UrlEncode(Header) + "." + Base64UrlEncode(Payload) + "." + Base64UrlEncode(Signature)
```

**Header:**
```json
{
  "alg": "HS256",      // Algorithm used for signing
  "typ": "JWT"         // Token type
}
```

**Payload (Claims):**
```json
{
  "sub": "1234567890",           // Subject (user ID)
  "name": "John Doe",            // Custom claim
  "iat": 1516239022,             // Issued at
  "exp": 1516242622,             // Expiration time
  "aud": "my-app",               // Audience
  "iss": "my-auth-server",       // Issuer
  "roles": ["USER", "ADMIN"]     // Custom roles claim
}
```

### JWT Advantages

✅ **Stateless Authentication:**
- No server-side session storage required
- Horizontal scaling without session affinity
- Reduced database load for authentication checks
- Self-contained tokens with all necessary information

✅ **Cross-Domain Support:**
- CORS-friendly authentication mechanism
- Suitable for microservices architecture
- Mobile and SPA application support
- Third-party service integration

## 🛡️ Spring Security Architecture

### Spring Security Filter Chain

Spring Security operates through a chain of filters that intercept HTTP requests:

```
HTTP Request
    ↓
SecurityContextPersistenceFilter    // Establishes SecurityContext
    ↓
LogoutFilter                       // Handles logout requests
    ↓
UsernamePasswordAuthenticationFilter // Processes login attempts
    ↓
JwtAuthenticationFilter           // Custom JWT validation (our addition)
    ↓
ExceptionTranslationFilter        // Handles security exceptions
    ↓
FilterSecurityInterceptor         // Authorization decisions
    ↓
Application Controller
```

### Core Spring Security Components

**SecurityContext & SecurityContextHolder:**
```kotlin
// Get current authenticated user
val authentication = SecurityContextHolder.getContext().authentication
val username = authentication.name
val authorities = authentication.authorities

// Set authentication programmatically
val authToken = UsernamePasswordAuthenticationToken(
    user, null, user.authorities
)
SecurityContextHolder.getContext().authentication = authToken
```

**UserDetailsService:**
```kotlin
@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {
    
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("User not found: $username")
        
        return User.builder()
            .username(user.username)
            .password(user.password)
            .authorities(user.roles.map { SimpleGrantedAuthority("ROLE_${it.name}") })
            .accountExpired(!user.isActive)
            .accountLocked(user.isLocked)
            .credentialsExpired(user.isCredentialsExpired)
            .disabled(!user.isEnabled)
            .build()
    }
}
```

## 🔧 JWT Implementation with Spring Security

### JWT Utility Service

```kotlin
@Service
class JwtService {
    
    @Value("\${jwt.secret}")
    private lateinit var secret: String
    
    @Value("\${jwt.expiration}")
    private var expiration: Long = 86400000 // 24 hours
    
    fun generateToken(userDetails: UserDetails): String {
        val claims = HashMap<String, Any>()
        
        // Add custom claims
        if (userDetails is CustomUserDetails) {
            claims["userId"] = userDetails.getId()
            claims["email"] = userDetails.getEmail()
            claims["roles"] = userDetails.authorities.map { it.authority }
        }
        
        return createToken(claims, userDetails.username)
    }
    
    private fun createToken(claims: Map<String, Any>, subject: String): String {
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + expiration))
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact()
    }
    
    fun validateToken(token: String, userDetails: UserDetails): Boolean {
        val username = extractUsername(token)
        return username == userDetails.username && !isTokenExpired(token)
    }
    
    fun extractUsername(token: String): String {
        return extractClaim(token, Claims::getSubject)
    }
}
```

## 👥 Role-Based Access Control (RBAC)

### User and Role Models

```kotlin
@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(unique = true, nullable = false)
    val username: String,
    
    @Column(unique = true, nullable = false)
    val email: String,
    
    @Column(nullable = false)
    val password: String,
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    val roles: Set<Role> = setOf()
) {
    
    fun getAuthorities(): List<GrantedAuthority> {
        return roles.map { role ->
            SimpleGrantedAuthority("ROLE_${role.name}")
        }
    }
    
    fun hasRole(roleName: String): Boolean {
        return roles.any { it.name == roleName }
    }
}

@Entity
@Table(name = "roles")
data class Role(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(unique = true, nullable = false)
    val name: String,
    
    val description: String
)
```

### Method-Level Security

```kotlin
@Service
class UserService(
    private val userRepository: UserRepository
) {
    
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }
    
    @PreAuthorize("hasRole('ADMIN') or authentication.name == #username")
    fun getUserByUsername(username: String): User? {
        return userRepository.findByUsername(username)
    }
    
    @PostAuthorize("hasRole('ADMIN') or returnObject.username == authentication.name")
    fun findUserById(id: Long): User? {
        return userRepository.findById(id).orElse(null)
    }
}
```

## 🔐 Password Security

### Modern Password Hashing

```kotlin
@Service
class PasswordService(
    private val passwordEncoder: PasswordEncoder
) {
    
    fun hashPassword(rawPassword: String): String {
        // BCrypt automatically handles salt generation
        return passwordEncoder.encode(rawPassword)
    }
    
    fun verifyPassword(rawPassword: String, hashedPassword: String): Boolean {
        return passwordEncoder.matches(rawPassword, hashedPassword)
    }
    
    fun isPasswordStrong(password: String): Boolean {
        return password.length >= 8 &&
                password.any { it.isDigit() } &&
                password.any { it.isUpperCase() } &&
                password.any { it.isLowerCase() } &&
                password.any { !it.isLetterOrDigit() }
    }
}
```

## 🧪 Security Testing Strategies

### Authentication Controller Testing

```kotlin
@WebMvcTest(AuthController::class)
class AuthControllerTest {
    
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @MockBean
    private lateinit var authService: AuthService
    
    @Test
    fun `should authenticate user with valid credentials`() {
        // Given
        val loginRequest = LoginRequest("testuser", "password123")
        val authResponse = AuthResponse(
            token = "jwt-token",
            user = UserResponse(1L, "testuser", "test@example.com")
        )
        
        every { authService.authenticate(any()) } returns authResponse
        
        // When & Then
        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.user.username").value("testuser"))
    }
}
```

### Security Configuration Testing

```kotlin
@SpringBootTest
class SecurityConfigIntegrationTest {
    
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @Test
    fun `should allow access to public endpoints without authentication`() {
        mockMvc.perform(get("/api/public/health"))
            .andExpect(status().isOk)
    }
    
    @Test
    @WithMockUser(roles = ["USER"])
    fun `should allow USER role access to user endpoints`() {
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk)
    }
    
    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should allow ADMIN role access to all endpoints`() {
        mockMvc.perform(get("/api/admin/users"))
            .andExpect(status().isOk)
    }
}
```

## 🎯 Key Security Best Practices

1. **JWT Security**: Implement proper token validation, short expiration, and secure storage
2. **Spring Security**: Configure filter chain, authentication providers, and method security
3. **Password Security**: Use strong hashing (BCrypt), enforce policies, prevent common attacks
4. **Role-Based Access**: Design flexible RBAC with roles and permissions
5. **Security Testing**: Test authentication, authorization, and security configurations
6. **Production Security**: Implement rate limiting, audit logging, and security headers

This comprehensive security implementation provides enterprise-grade authentication and authorization suitable for production Spring Boot applications.