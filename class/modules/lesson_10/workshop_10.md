# 🛠️ Workshop 10: Pagination & Filtering - Advanced Query Patterns

## 🎯 Workshop Objectives

In this hands-on workshop, you will:
1. Master Spring Data JPA pagination with Pageable and Page interfaces
2. Implement dynamic queries using the Specification API
3. Build flexible filtering and search functionality for large datasets
4. Create efficient pagination for blog posts with complex relationships
5. Implement multi-field search with relevance scoring
6. Design reusable query criteria builders
7. Optimize query performance with proper indexing and projections
8. Build frontend-friendly pagination responses
9. Handle complex sorting and ordering requirements
10. Test pagination and filtering functionality comprehensively

## 🏗️ Project Structure

```
src/main/kotlin/com/learning/pagination/
├── PaginationApplication.kt
├── config/
│   └── PaginationConfiguration.kt
├── model/
│   ├── Author.kt
│   ├── Post.kt
│   ├── Comment.kt
│   └── Category.kt
├── repository/
│   ├── PostRepository.kt
│   ├── AuthorRepository.kt
│   └── CategoryRepository.kt
├── specification/
│   ├── PostSpecifications.kt
│   └── PostCriteriaBuilder.kt
├── dto/
│   ├── PostDTOs.kt
│   ├── SearchDTOs.kt
│   └── PageResponseDTOs.kt
├── service/
│   ├── PostService.kt
│   └── SearchService.kt
├── controller/
│   ├── PostController.kt
│   └── SearchController.kt
└── criteria/
    └── SearchCriteria.kt
```

---

## 📋 Step 1: Project Setup and Dependencies

### 1.1 Create build.gradle.kts

```kotlin
// TODO: Add Spring Boot parent and dependencies for:
// - Spring Boot Web
// - Spring Data JPA with Specification support
// - H2 Database
// - PostgreSQL
// - Kotlin Jackson module
// - Validation
// - Cache abstraction
// - Testing dependencies

plugins {
    // TODO: Add Kotlin and Spring Boot plugins
}

dependencies {
    // TODO: Add all required dependencies
    // Key: spring-boot-starter-data-jpa includes Specification support
    // Add: spring-boot-starter-cache for search result caching
}
```

### 1.2 Create settings.gradle.kts

```kotlin
// TODO: Set the project name
rootProject.name = "lesson-10-pagination-filtering"
```

---

## 📋 Step 2: Database Configuration

### 2.1 Create application.yml

```yaml
# TODO: Create configuration for:
# - H2 database with large dataset support
# - JPA settings optimized for pagination
# - Caching configuration
# - Query logging for performance analysis

spring:
  profiles:
    active: dev
    
  # TODO: Configure H2 for development with larger memory
  datasource:
    # Add H2 configuration with memory optimization
    
  # TODO: Configure JPA/Hibernate for pagination
  jpa:
    # Add pagination-optimized settings
    # Enable batch loading
    # Configure query logging
    
  # TODO: Configure cache settings
  cache:
    # Add cache configuration for search results

# TODO: Configure logging for SQL analysis
logging:
  level:
    # Add SQL and performance logging
```

### 2.2 Create PaginationConfiguration.kt

```kotlin
// TODO: Create configuration class for pagination and caching
// Hints:
// - Configure cache manager for search results
// - Set up pagination defaults
// - Configure query optimization settings

@Configuration
class PaginationConfiguration {
    
    // TODO: Configure cache manager for search results
    // TODO: Set up default page size and max page size
    // TODO: Configure query timeout settings
}
```

---

## 📋 Step 3: Entity Models (Building on Lesson 9)

### 3.1 Create Author Entity

```kotlin
// TODO: Create Author entity optimized for pagination queries
// This builds on Lesson 9 but adds pagination-specific optimizations

@Entity
@Table(
    name = "authors",
    indexes = [
        // TODO: Add indexes for common search and filter fields
        // Consider: name searches, status filters
    ]
)
data class Author(
    // TODO: Implement entity with pagination-optimized fields
    // Include: id, username, email, firstName, lastName, isActive
    // Add: createdAt for sorting, postCount for filtering
) {
    
    // TODO: Add business logic methods useful for pagination
    fun getDisplayName(): String {
        // TODO: Return formatted display name
    }
    
    fun isEligibleForListing(): Boolean {
        // TODO: Check if author should appear in public listings
    }
}
```

### 3.2 Create Post Entity with Pagination Optimizations

```kotlin
// TODO: Create Post entity with extensive indexing for efficient pagination
@Entity
@Table(
    name = "posts",
    indexes = [
        // TODO: Add strategic indexes for pagination performance
        // Consider: status, createdAt, publishedAt, viewCount, author
        // Add composite indexes for common filter combinations
    ]
)
data class Post(
    // TODO: Implement entity with all pagination-relevant fields
    // Include: id, title, content, summary, status, publishedAt, viewCount, likeCount
    // Add: searchVector for full-text search, featured flag
    
    // TODO: Add relationships optimized for pagination
    // author (with fetch optimization)
    // categories (with pagination support)
    // comments (with counting queries)
    
) {
    
    // TODO: Add methods useful for search and filtering
    fun getSearchableContent(): String {
        // TODO: Return combined searchable text
    }
    
    fun isVisibleToPublic(): Boolean {
        // TODO: Check if post should appear in public searches
    }
    
    fun getRelevanceScore(searchTerms: List<String>): Double {
        // TODO: Calculate search relevance based on term matches
    }
    
    fun getEngagementMetrics(): PostEngagement {
        // TODO: Return engagement statistics for sorting
    }
}

// TODO: Create PostStatus enum with search-friendly methods
enum class PostStatus {
    // TODO: Add status values with search implications
}

// TODO: Create engagement metrics data class
data class PostEngagement(
    // TODO: Add metrics useful for popularity sorting
)
```

### 3.3 Create Category Entity for Filtering

```kotlin
// TODO: Create Category entity optimized for filter queries
@Entity
@Table(
    name = "categories",
    indexes = [
        // TODO: Add indexes for category filtering
    ]
)
data class Category(
    // TODO: Implement entity for category-based filtering
    // Include: id, name, description, isActive, postCount
) {
    
    // TODO: Add filtering helper methods
    fun isAvailableForFiltering(): Boolean {
        // TODO: Check if category should appear in filter lists
    }
    
    fun getPostCountInStatus(status: PostStatus): Long {
        // TODO: Return count of posts in specific status
    }
}
```

---

## 📋 Step 4: Repository Layer with Advanced Queries

### 4.1 Create Base Repository Interface

```kotlin
// TODO: Create base repository with pagination and specification support
interface PostRepository : JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {
    
    // TODO: Add method name queries with pagination
    fun findByStatusOrderByCreatedAtDesc(
        status: PostStatus,
        pageable: Pageable
    ): Page<Post>
    
    // TODO: Add method for finding by author with pagination
    // TODO: Add method for finding by category with pagination
    // TODO: Add method for finding featured posts
    
    // TODO: Add custom JPQL queries optimized for pagination
    @Query("""
        SELECT p FROM Post p 
        LEFT JOIN FETCH p.author 
        WHERE p.status = :status
    """)
    fun findByStatusWithAuthor(
        @Param("status") status: PostStatus,
        pageable: Pageable
    ): Page<Post>
    
    // TODO: Add projection query for list views
    @Query("""
        SELECT new com.learning.pagination.dto.PostSummaryProjection(
            p.id, p.title, p.summary, p.status, p.createdAt, p.viewCount,
            a.firstName, a.lastName, SIZE(p.comments)
        )
        FROM Post p JOIN p.author a 
        WHERE p.status = :status
    """)
    fun findPostSummaries(
        @Param("status") status: PostStatus,
        pageable: Pageable
    ): Page<PostSummaryProjection>
    
    // TODO: Add count queries for statistics
    @Query("SELECT COUNT(p) FROM Post p WHERE p.status = :status")
    fun countByStatus(@Param("status") status: PostStatus): Long
    
    // TODO: Add native query for complex analytics
    @Query(
        value = """
            SELECT p.*, COUNT(c.id) as comment_count, AVG(c.rating) as avg_rating
            FROM posts p LEFT JOIN comments c ON p.id = c.post_id
            WHERE p.status = :status
            GROUP BY p.id
        """,
        countQuery = "SELECT COUNT(*) FROM posts WHERE status = :status",
        nativeQuery = true
    )
    fun findPostsWithStats(
        @Param("status") status: String,
        pageable: Pageable
    ): Page<PostStatsProjection>
}
```

### 4.2 Create Author and Category Repositories

```kotlin
// TODO: Create AuthorRepository with pagination support
interface AuthorRepository : JpaRepository<Author, Long> {
    
    // TODO: Add pagination methods for author listings
    fun findByIsActiveTrueOrderByFirstNameAsc(pageable: Pageable): Page<Author>
    
    // TODO: Add search by name with pagination
    @Query("""
        SELECT a FROM Author a 
        WHERE LOWER(CONCAT(a.firstName, ' ', a.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))
    """)
    fun findByNameContaining(
        @Param("name") name: String,
        pageable: Pageable
    ): Page<Author>
    
    // TODO: Add method to find authors with post count
    @Query("""
        SELECT a FROM Author a 
        WHERE SIZE(a.posts) >= :minPosts
        ORDER BY SIZE(a.posts) DESC
    """)
    fun findAuthorsWithMinPosts(
        @Param("minPosts") minPosts: Int,
        pageable: Pageable
    ): Page<Author>
}

// TODO: Create CategoryRepository with filtering support
interface CategoryRepository : JpaRepository<Category, Long> {
    
    // TODO: Add methods for category-based filtering
    fun findByIsActiveTrueOrderByNameAsc(): List<Category>
    
    // TODO: Add method to find categories with post counts
    @Query("""
        SELECT c FROM Category c 
        WHERE SIZE(c.posts) > 0 
        ORDER BY SIZE(c.posts) DESC
    """)
    fun findCategoriesWithPosts(pageable: Pageable): Page<Category>
}
```

---

## 📋 Step 5: Specification API Implementation

### 5.1 Create PostSpecifications

```kotlin
// TODO: Create comprehensive specifications for dynamic querying
class PostSpecifications {
    
    companion object {
        
        // TODO: Basic field specifications
        fun hasStatus(status: PostStatus): Specification<Post> {
            // TODO: Return specification that filters by status
            TODO("Implement status specification")
        }
        
        fun hasAuthor(authorId: Long): Specification<Post> {
            // TODO: Return specification that filters by author ID
            TODO("Implement author specification")
        }
        
        fun titleContains(title: String): Specification<Post> {
            // TODO: Return case-insensitive title search specification
            TODO("Implement title search specification")
        }
        
        fun contentContains(content: String): Specification<Post> {
            // TODO: Return case-insensitive content search specification
            TODO("Implement content search specification")
        }
        
        // TODO: Date range specifications
        fun createdBetween(start: LocalDateTime, end: LocalDateTime): Specification<Post> {
            // TODO: Return specification for date range filtering
            TODO("Implement date range specification")
        }
        
        fun publishedAfter(date: LocalDateTime): Specification<Post> {
            // TODO: Return specification for posts published after date
            TODO("Implement published after specification")
        }
        
        // TODO: Numeric range specifications
        fun viewCountBetween(min: Long, max: Long): Specification<Post> {
            // TODO: Return specification for view count range
            TODO("Implement view count range specification")
        }
        
        fun likeCountGreaterThan(count: Long): Specification<Post> {
            // TODO: Return specification for minimum like count
            TODO("Implement like count specification")
        }
        
        // TODO: Relationship specifications
        fun hasCategory(categoryName: String): Specification<Post> {
            // TODO: Return specification for posts in specific category
            // Hint: Use join with categories collection
            TODO("Implement category specification")
        }
        
        fun hasCategoryIn(categoryNames: List<String>): Specification<Post> {
            // TODO: Return specification for posts in any of the categories
            TODO("Implement multiple category specification")
        }
        
        fun hasMinimumComments(count: Int): Specification<Post> {
            // TODO: Return specification for posts with minimum comment count
            // Hint: Use SIZE() function
            TODO("Implement minimum comments specification")
        }
        
        // TODO: Advanced specifications
        fun titleOrContentContains(searchText: String): Specification<Post> {
            // TODO: Return specification that searches both title and content
            // Use OR condition
            TODO("Implement combined title/content search")
        }
        
        fun isPopular(): Specification<Post> {
            // TODO: Return specification for popular posts
            // Define popularity criteria (views + likes + comments)
            TODO("Implement popularity specification")
        }
        
        fun isRecent(days: Int): Specification<Post> {
            // TODO: Return specification for recent posts
            TODO("Implement recent posts specification")
        }
        
        fun authorNameContains(authorName: String): Specification<Post> {
            // TODO: Return specification for author name search
            // Search in firstName and lastName
            TODO("Implement author name search")
        }
    }
}
```

### 5.2 Create Criteria Builder

```kotlin
// TODO: Create fluent criteria builder for complex queries
class PostCriteriaBuilder {
    
    private val specifications = mutableListOf<Specification<Post>>()
    
    // TODO: Add fluent methods for building criteria
    fun withStatus(status: PostStatus?): PostCriteriaBuilder {
        // TODO: Add status filter if not null
        TODO("Implement status criteria")
        return this
    }
    
    fun withAuthor(authorId: Long?): PostCriteriaBuilder {
        // TODO: Add author filter if not null
        TODO("Implement author criteria")
        return this
    }
    
    fun withTitle(title: String?): PostCriteriaBuilder {
        // TODO: Add title search if not blank
        TODO("Implement title criteria")
        return this
    }
    
    fun withTextSearch(searchText: String?): PostCriteriaBuilder {
        // TODO: Add full-text search if not blank
        // Search across title, content, and summary
        TODO("Implement text search criteria")
        return this
    }
    
    fun withCategories(categoryNames: List<String>): PostCriteriaBuilder {
        // TODO: Add category filter if list not empty
        TODO("Implement category criteria")
        return this
    }
    
    fun withDateRange(start: LocalDateTime?, end: LocalDateTime?): PostCriteriaBuilder {
        // TODO: Add date range filter
        TODO("Implement date range criteria")
        return this
    }
    
    fun withViewCountRange(min: Long?, max: Long?): PostCriteriaBuilder {
        // TODO: Add view count range filter
        TODO("Implement view count criteria")
        return this
    }
    
    fun withAuthorName(authorName: String?): PostCriteriaBuilder {
        // TODO: Add author name search if not blank
        TODO("Implement author name criteria")
        return this
    }
    
    fun withMinimumComments(count: Int?): PostCriteriaBuilder {
        // TODO: Add minimum comment count filter
        TODO("Implement comment count criteria")
        return this
    }
    
    fun withPopularityFilter(isPopular: Boolean?): PostCriteriaBuilder {
        // TODO: Add popularity filter if specified
        TODO("Implement popularity criteria")
        return this
    }
    
    fun withRecentFilter(days: Int?): PostCriteriaBuilder {
        // TODO: Add recent posts filter if specified
        TODO("Implement recent criteria")
        return this
    }
    
    // TODO: Build final specification
    fun build(): Specification<Post> {
        // TODO: Combine all specifications with AND logic
        // Return combined specification or null if no criteria
        TODO("Implement build method")
    }
    
    // TODO: Helper method to check if any criteria are set
    fun hasCriteria(): Boolean {
        // TODO: Return true if any specifications were added
        TODO("Implement hasCriteria method")
    }
}
```

---

## 📋 Step 6: DTOs for Requests and Responses

### 6.1 Create Search and Filter DTOs

```kotlin
// TODO: Create comprehensive search request DTO
data class PostSearchRequest(
    // Basic filters
    val status: PostStatus? = null,
    val authorId: Long? = null,
    val authorName: String? = null,
    
    // Text search
    val title: String? = null,
    val content: String? = null,
    val searchText: String? = null,  // Combined search
    
    // Category filtering
    val categoryNames: List<String> = emptyList(),
    val requireAllCategories: Boolean = false,  // AND vs OR for multiple categories
    
    // Date filtering
    val createdAfter: LocalDateTime? = null,
    val createdBefore: LocalDateTime? = null,
    val publishedAfter: LocalDateTime? = null,
    val publishedBefore: LocalDateTime? = null,
    
    // Numeric filtering
    val minViewCount: Long? = null,
    val maxViewCount: Long? = null,
    val minLikeCount: Long? = null,
    val maxLikeCount: Long? = null,
    val minCommentCount: Int? = null,
    val maxCommentCount: Int? = null,
    
    // Special filters
    val isPopular: Boolean? = null,
    val isFeatured: Boolean? = null,
    val isRecent: Boolean? = null,
    val recentDays: Int = 30,
    
    // Inclusion options
    val includeUnpublished: Boolean = false,
    val includeDeleted: Boolean = false
) {
    
    // TODO: Convert to Specification
    fun toSpecification(): Specification<Post> {
        // TODO: Use PostCriteriaBuilder to build specification
        TODO("Implement toSpecification method")
    }
    
    // TODO: Validate request
    fun validate(): List<String> {
        // TODO: Return list of validation errors
        // Check date ranges, numeric ranges, etc.
        TODO("Implement validation method")
    }
    
    // TODO: Check if request has any search criteria
    fun hasSearchCriteria(): Boolean {
        // TODO: Return true if any search fields are specified
        TODO("Implement hasSearchCriteria method")
    }
}

// TODO: Create pagination request DTO
data class PageRequest(
    val page: Int = 0,
    val size: Int = 20,
    val sortBy: String = "createdAt",
    val sortDirection: String = "desc",
    val maxSize: Int = 100  // Prevent excessive page sizes
) {
    
    // TODO: Convert to Spring Data Pageable
    fun toPageable(): Pageable {
        // TODO: Validate and convert to Pageable
        // Ensure page >= 0, size > 0 and <= maxSize
        // Parse sortDirection and sortBy
        TODO("Implement toPageable method")
    }
    
    // TODO: Validate pagination parameters
    fun validate(): List<String> {
        // TODO: Return validation errors for pagination parameters
        TODO("Implement pagination validation")
    }
}
```

### 6.2 Create Response DTOs

```kotlin
// TODO: Create post summary for list views
data class PostSummaryResponse(
    val id: Long,
    val title: String,
    val summary: String?,
    val status: PostStatus,
    val createdAt: LocalDateTime,
    val publishedAt: LocalDateTime?,
    val viewCount: Long,
    val likeCount: Long,
    val commentCount: Int,
    val author: AuthorSummary,
    val categories: List<CategorySummary>,
    val featured: Boolean = false,
    val relevanceScore: Double? = null  // For search results
) {
    
    // TODO: Add factory method
    companion object {
        fun from(post: Post, commentCount: Int = 0, relevanceScore: Double? = null): PostSummaryResponse {
            // TODO: Convert Post entity to summary response
            TODO("Implement from method")
        }
    }
}

// TODO: Create author summary
data class AuthorSummary(
    val id: Long,
    val username: String,
    val firstName: String,
    val lastName: String,
    val displayName: String
) {
    companion object {
        fun from(author: Author): AuthorSummary {
            // TODO: Convert Author entity to summary
            TODO("Implement from method")
        }
    }
}

// TODO: Create category summary
data class CategorySummary(
    val id: Long,
    val name: String,
    val color: String?
) {
    companion object {
        fun from(category: Category): CategorySummary {
            // TODO: Convert Category entity to summary
            TODO("Implement from method")
        }
    }
}

// TODO: Create paginated response wrapper
data class PagedResponse<T>(
    val content: List<T>,
    val page: PageMetadata,
    val filters: FilterMetadata? = null
) {
    
    data class PageMetadata(
        val number: Int,
        val size: Int,
        val totalElements: Long,
        val totalPages: Int,
        val first: Boolean,
        val last: Boolean,
        val numberOfElements: Int,
        val empty: Boolean,
        val sort: SortMetadata
    )
    
    data class SortMetadata(
        val sorted: Boolean,
        val orders: List<SortOrder>
    )
    
    data class SortOrder(
        val property: String,
        val direction: String
    )
    
    data class FilterMetadata(
        val appliedFilters: Map<String, Any>,
        val filterCount: Int,
        val searchQuery: String?
    )
    
    companion object {
        fun <T> from(page: Page<T>, searchRequest: PostSearchRequest? = null): PagedResponse<T> {
            // TODO: Convert Spring Data Page to custom response
            TODO("Implement from method")
        }
    }
}
```

### 6.3 Create Projection Classes

```kotlin
// TODO: Create projections for optimized queries
data class PostSummaryProjection(
    val id: Long,
    val title: String,
    val summary: String?,
    val status: PostStatus,
    val createdAt: LocalDateTime,
    val viewCount: Long,
    val authorFirstName: String,
    val authorLastName: String,
    val commentCount: Long
) {
    
    fun toResponse(): PostSummaryResponse {
        // TODO: Convert projection to response DTO
        TODO("Implement toResponse method")
    }
}

// TODO: Create statistics projection
data class PostStatsProjection(
    val id: Long,
    val title: String,
    val viewCount: Long,
    val likeCount: Long,
    val commentCount: Long,
    val avgRating: Double?,
    val popularityRank: Int?
)
```

---

## 📋 Step 7: Service Layer Implementation

### 7.1 Create PostService with Pagination

```kotlin
// TODO: Create service with comprehensive pagination and filtering
@Service
@Transactional(readOnly = true)
class PostService(
    private val postRepository: PostRepository,
    private val authorRepository: AuthorRepository,
    private val categoryRepository: CategoryRepository
) {
    
    // TODO: Implement paginated post retrieval
    fun getPosts(
        searchRequest: PostSearchRequest,
        pageRequest: PageRequest
    ): PagedResponse<PostSummaryResponse> {
        
        // TODO: 1. Validate request parameters
        val validationErrors = searchRequest.validate() + pageRequest.validate()
        if (validationErrors.isNotEmpty()) {
            throw ValidationException("Invalid request: ${validationErrors.joinToString(", ")}")
        }
        
        // TODO: 2. Build specification from search criteria
        val specification = searchRequest.toSpecification()
        
        // TODO: 3. Create pageable from page request
        val pageable = pageRequest.toPageable()
        
        // TODO: 4. Execute query with pagination
        val page = if (specification != null) {
            postRepository.findAll(specification, pageable)
        } else {
            postRepository.findAll(pageable)
        }
        
        // TODO: 5. Convert to response DTOs
        val responseContent = page.content.map { post ->
            PostSummaryResponse.from(post, post.comments.size)
        }
        
        // TODO: 6. Build paginated response
        TODO("Implement response building")
    }
    
    // TODO: Implement optimized list view
    fun getPostSummaries(
        status: PostStatus,
        pageRequest: PageRequest
    ): PagedResponse<PostSummaryResponse> {
        
        // TODO: Use projection query for better performance
        val pageable = pageRequest.toPageable()
        val page = postRepository.findPostSummaries(status, pageable)
        
        // TODO: Convert projections to responses
        TODO("Implement projection conversion")
    }
    
    // TODO: Implement search functionality
    fun searchPosts(
        query: String,
        filters: PostSearchRequest,
        pageRequest: PageRequest
    ): PagedResponse<PostSummaryResponse> {
        
        // TODO: Combine text search with other filters
        val searchRequest = filters.copy(searchText = query)
        
        // TODO: Execute search with relevance scoring if needed
        TODO("Implement search functionality")
    }
    
    // TODO: Implement popular posts
    fun getPopularPosts(
        timeframe: PopularTimeframe = PopularTimeframe.ALL_TIME,
        pageRequest: PageRequest
    ): PagedResponse<PostSummaryResponse> {
        
        // TODO: Define popularity criteria based on timeframe
        // Consider view count, like count, comment count, recency
        TODO("Implement popular posts logic")
    }
    
    // TODO: Implement category-based listing
    fun getPostsByCategory(
        categoryName: String,
        pageRequest: PageRequest
    ): PagedResponse<PostSummaryResponse> {
        
        // TODO: Filter posts by category with pagination
        TODO("Implement category filtering")
    }
    
    // TODO: Implement author-based listing
    fun getPostsByAuthor(
        authorId: Long,
        includeUnpublished: Boolean = false,
        pageRequest: PageRequest
    ): PagedResponse<PostSummaryResponse> {
        
        // TODO: Filter posts by author with optional status filtering
        TODO("Implement author filtering")
    }
    
    // TODO: Helper methods
    private fun buildFilterMetadata(searchRequest: PostSearchRequest): FilterMetadata {
        // TODO: Build filter metadata for response
        TODO("Implement filter metadata")
    }
    
    private fun calculateRelevanceScore(post: Post, searchTerms: List<String>): Double {
        // TODO: Calculate search relevance score
        // Consider title matches, content matches, category matches
        // Apply weighting and boosts
        TODO("Implement relevance scoring")
    }
}

// TODO: Create enum for popular post timeframes
enum class PopularTimeframe {
    LAST_WEEK, LAST_MONTH, LAST_YEAR, ALL_TIME
}
```

### 7.2 Create SearchService

```kotlin
// TODO: Create specialized search service
@Service
@Transactional(readOnly = true)
class SearchService(
    private val postRepository: PostRepository
) {
    
    // TODO: Implement advanced search with ranking
    fun search(
        query: String,
        filters: PostSearchRequest,
        pageRequest: PageRequest
    ): PagedResponse<PostSummaryResponse> {
        
        // TODO: Parse search query into terms
        val searchTerms = parseSearchQuery(query)
        
        // TODO: Build search specification
        val searchSpec = buildSearchSpecification(searchTerms)
        val filterSpec = filters.toSpecification()
        
        // TODO: Combine specifications
        val combinedSpec = if (filterSpec != null) {
            searchSpec.and(filterSpec)
        } else {
            searchSpec
        }
        
        // TODO: Execute search
        val pageable = pageRequest.toPageable()
        val page = postRepository.findAll(combinedSpec, pageable)
        
        // TODO: Calculate relevance scores and sort by relevance
        TODO("Implement search with relevance ranking")
    }
    
    // TODO: Implement autocomplete suggestions
    fun getSearchSuggestions(
        partialQuery: String,
        limit: Int = 10
    ): List<String> {
        
        // TODO: Return search suggestions based on post titles and content
        // Consider popular search terms, recent searches
        TODO("Implement search suggestions")
    }
    
    // TODO: Implement search analytics
    fun getSearchStats(): SearchStats {
        // TODO: Return search statistics
        // Popular search terms, search volume, etc.
        TODO("Implement search analytics")
    }
    
    private fun parseSearchQuery(query: String): List<String> {
        // TODO: Parse search query into individual terms
        // Handle quoted phrases, remove stop words
        TODO("Implement query parsing")
    }
    
    private fun buildSearchSpecification(searchTerms: List<String>): Specification<Post> {
        // TODO: Build specification that searches across multiple fields
        // Title, content, summary, category names, author names
        TODO("Implement search specification building")
    }
}

// TODO: Create search statistics data class
data class SearchStats(
    val totalSearches: Long,
    val popularTerms: List<PopularTerm>,
    val recentSearches: List<String>
)

data class PopularTerm(
    val term: String,
    val count: Long,
    val resultCount: Long
)
```

---

## 📋 Step 8: REST Controllers

### 8.1 Create PostController with Pagination

```kotlin
// TODO: Create controller with comprehensive pagination endpoints
@RestController
@RequestMapping("/api/posts")
class PostController(
    private val postService: PostService,
    private val searchService: SearchService
) {
    
    // TODO: Implement paginated post listing
    @GetMapping
    fun getPosts(
        // TODO: Add request parameters for filtering
        @RequestParam(required = false) status: PostStatus?,
        @RequestParam(required = false) authorId: Long?,
        @RequestParam(required = false) authorName: String?,
        @RequestParam(required = false) categoryNames: List<String>?,
        @RequestParam(required = false) title: String?,
        @RequestParam(required = false) searchText: String?,
        @RequestParam(required = false) createdAfter: LocalDateTime?,
        @RequestParam(required = false) createdBefore: LocalDateTime?,
        @RequestParam(required = false) minViewCount: Long?,
        @RequestParam(required = false) maxViewCount: Long?,
        @RequestParam(required = false) isPopular: Boolean?,
        @RequestParam(required = false) isRecent: Boolean?,
        
        // TODO: Add pagination parameters
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "desc") sortDirection: String
    ): ResponseEntity<PagedResponse<PostSummaryResponse>> {
        
        // TODO: Build search request from parameters
        val searchRequest = PostSearchRequest(
            // TODO: Map parameters to search request
        )
        
        // TODO: Build page request
        val pageRequest = PageRequest(
            page = page,
            size = size,
            sortBy = sortBy,
            sortDirection = sortDirection
        )
        
        // TODO: Execute service call and return response
        val result = postService.getPosts(searchRequest, pageRequest)
        return ResponseEntity.ok(result)
    }
    
    // TODO: Implement search endpoint
    @GetMapping("/search")
    fun searchPosts(
        @RequestParam query: String,
        @RequestParam(required = false) status: PostStatus?,
        @RequestParam(required = false) categoryNames: List<String>?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "relevance") sortBy: String
    ): ResponseEntity<PagedResponse<PostSummaryResponse>> {
        
        // TODO: Validate search query
        if (query.isBlank()) {
            return ResponseEntity.badRequest().build()
        }
        
        // TODO: Build filter request
        val filters = PostSearchRequest(
            status = status,
            categoryNames = categoryNames ?: emptyList()
        )
        
        // TODO: Build page request
        val pageRequest = PageRequest(
            page = page,
            size = size,
            sortBy = sortBy,
            sortDirection = "desc"
        )
        
        // TODO: Execute search
        TODO("Implement search endpoint")
    }
    
    // TODO: Implement popular posts endpoint
    @GetMapping("/popular")
    fun getPopularPosts(
        @RequestParam(defaultValue = "ALL_TIME") timeframe: PopularTimeframe,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PagedResponse<PostSummaryResponse>> {
        
        // TODO: Implement popular posts endpoint
        TODO("Implement popular posts endpoint")
    }
    
    // TODO: Implement category-based listing
    @GetMapping("/category/{categoryName}")
    fun getPostsByCategory(
        @PathVariable categoryName: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "desc") sortDirection: String
    ): ResponseEntity<PagedResponse<PostSummaryResponse>> {
        
        // TODO: Implement category filtering
        TODO("Implement category endpoint")
    }
    
    // TODO: Implement author-based listing
    @GetMapping("/author/{authorId}")
    fun getPostsByAuthor(
        @PathVariable authorId: Long,
        @RequestParam(defaultValue = "false") includeUnpublished: Boolean,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PagedResponse<PostSummaryResponse>> {
        
        // TODO: Implement author filtering
        TODO("Implement author endpoint")
    }
}
```

### 8.2 Create SearchController

```kotlin
// TODO: Create dedicated search controller
@RestController
@RequestMapping("/api/search")
class SearchController(
    private val searchService: SearchService
) {
    
    // TODO: Implement advanced search endpoint
    @PostMapping("/posts")
    fun searchPosts(
        @RequestBody @Valid searchRequest: AdvancedSearchRequest
    ): ResponseEntity<PagedResponse<PostSummaryResponse>> {
        
        // TODO: Execute advanced search
        TODO("Implement advanced search endpoint")
    }
    
    // TODO: Implement search suggestions
    @GetMapping("/suggestions")
    fun getSearchSuggestions(
        @RequestParam query: String,
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<List<String>> {
        
        // TODO: Return search suggestions
        TODO("Implement search suggestions")
    }
    
    // TODO: Implement search analytics
    @GetMapping("/stats")
    fun getSearchStats(): ResponseEntity<SearchStats> {
        
        // TODO: Return search statistics
        TODO("Implement search analytics")
    }
}

// TODO: Create advanced search request DTO
data class AdvancedSearchRequest(
    val query: String,
    val filters: PostSearchRequest,
    val pagination: PageRequest,
    val includeHighlights: Boolean = false,
    val includeFacets: Boolean = false
) {
    
    @AssertTrue(message = "Search query cannot be blank")
    fun isQueryValid(): Boolean {
        return query.isNotBlank()
    }
}
```

---

## 📋 Step 9: Testing Implementation

### 9.1 Create Repository Tests

```kotlin
// TODO: Create comprehensive repository tests
@DataJpaTest
class PostRepositoryPaginationTest {
    
    @Autowired
    private lateinit var postRepository: PostRepository
    
    @Autowired
    private lateinit var testEntityManager: TestEntityManager
    
    // TODO: Set up test data
    @BeforeEach
    fun setUp() {
        // TODO: Create test authors, categories, and posts
        // Create enough data to test pagination (50+ posts)
    }
    
    // TODO: Test basic pagination
    @Test
    fun `should paginate posts correctly`() {
        // TODO: Test that pagination returns correct page size
        // TODO: Test that total elements count is correct
        // TODO: Test page navigation (first, last, hasNext, hasPrevious)
    }
    
    // TODO: Test sorting
    @Test
    fun `should sort posts correctly`() {
        // TODO: Test sorting by different fields
        // TODO: Test ascending and descending order
        // TODO: Test multi-column sorting
    }
    
    // TODO: Test method name queries
    @Test
    fun `should find posts by status with pagination`() {
        // TODO: Test findByStatusOrderByCreatedAtDesc
    }
    
    // TODO: Test custom queries
    @Test
    fun `should find posts with author using fetch join`() {
        // TODO: Test findByStatusWithAuthor
        // TODO: Verify no N+1 queries
    }
    
    // TODO: Test projections
    @Test
    fun `should return post summaries efficiently`() {
        // TODO: Test findPostSummaries projection query
    }
    
    // TODO: Test edge cases
    @Test
    fun `should handle empty results gracefully`() {
        // TODO: Test pagination with no results
    }
    
    @Test
    fun `should handle large page requests`() {
        // TODO: Test behavior with very large page sizes
    }
}
```

### 9.2 Create Specification Tests

```kotlin
// TODO: Create specification tests
@SpringBootTest
class PostSpecificationTest {
    
    @Autowired
    private lateinit var postRepository: PostRepository
    
    // TODO: Test individual specifications
    @Test
    fun `should filter by status specification`() {
        // TODO: Test PostSpecifications.hasStatus()
    }
    
    @Test
    fun `should filter by title containing text`() {
        // TODO: Test PostSpecifications.titleContains()
    }
    
    @Test
    fun `should filter by date range`() {
        // TODO: Test PostSpecifications.createdBetween()
    }
    
    @Test
    fun `should filter by category`() {
        // TODO: Test PostSpecifications.hasCategory()
    }
    
    // TODO: Test combined specifications
    @Test
    fun `should combine multiple specifications with AND`() {
        // TODO: Test combining specifications
    }
    
    @Test
    fun `should combine specifications with OR`() {
        // TODO: Test OR combinations
    }
    
    // TODO: Test criteria builder
    @Test
    fun `should build complex criteria correctly`() {
        // TODO: Test PostCriteriaBuilder
    }
}
```

### 9.3 Create Service Tests

```kotlin
// TODO: Create service layer tests
@SpringBootTest
@Transactional
class PostServiceTest {
    
    @Autowired
    private lateinit var postService: PostService
    
    // TODO: Test pagination functionality
    @Test
    fun `should return paginated posts`() {
        // TODO: Test PostService.getPosts()
    }
    
    @Test
    fun `should apply filters correctly`() {
        // TODO: Test filtering functionality
    }
    
    @Test
    fun `should handle search queries`() {
        // TODO: Test PostService.searchPosts()
    }
    
    @Test
    fun `should return popular posts`() {
        // TODO: Test PostService.getPopularPosts()
    }
    
    // TODO: Test error handling
    @Test
    fun `should handle invalid pagination parameters`() {
        // TODO: Test validation error handling
    }
    
    @Test
    fun `should handle empty search results`() {
        // TODO: Test empty result handling
    }
}
```

### 9.4 Create Performance Tests

```kotlin
// TODO: Create performance tests
@SpringBootTest
class PaginationPerformanceTest {
    
    @Autowired
    private lateinit var postService: PostService
    
    // TODO: Test large dataset performance
    @Test
    fun `should handle large dataset pagination efficiently`() {
        // TODO: Create large test dataset (10,000+ posts)
        // TODO: Test pagination performance
        // TODO: Verify query execution time
    }
    
    @Test
    fun `should avoid N+1 queries in pagination`() {
        // TODO: Test that pagination doesn't cause N+1 query problems
    }
    
    @Test
    fun `should perform deep pagination efficiently`() {
        // TODO: Test performance of deep pagination (page 100+)
    }
}
```

---

## 📋 Step 10: Error Handling and Validation

### 10.1 Create Custom Exceptions

```kotlin
// TODO: Create pagination-specific exceptions
class PaginationException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class InvalidSortFieldException(field: String) : PaginationException("Invalid sort field: $field")

class PageSizeExceededException(requestedSize: Int, maxSize: Int) : 
    PaginationException("Requested page size $requestedSize exceeds maximum allowed size $maxSize")

class InvalidSearchQueryException(query: String) : 
    PaginationException("Invalid search query: $query")
```

### 10.2 Create Global Exception Handler

```kotlin
// TODO: Create controller advice for pagination errors
@ControllerAdvice
class PaginationExceptionHandler {
    
    @ExceptionHandler(PaginationException::class)
    fun handlePaginationException(ex: PaginationException): ResponseEntity<ErrorResponse> {
        // TODO: Handle pagination exceptions
        TODO("Implement pagination exception handling")
    }
    
    @ExceptionHandler(InvalidSortFieldException::class)
    fun handleInvalidSortField(ex: InvalidSortFieldException): ResponseEntity<ErrorResponse> {
        // TODO: Handle invalid sort field exceptions
        TODO("Implement sort field exception handling")
    }
    
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        // TODO: Handle validation errors for search requests
        TODO("Implement validation exception handling")
    }
}
```

---

## 📋 Step 11: Sample Data and Demo

### 11.1 Create Data Initialization

```kotlin
// TODO: Create component to initialize large sample dataset
@Component
class PaginationDataInitializer : ApplicationRunner {
    
    override fun run(args: ApplicationArguments) {
        // TODO: Create comprehensive test dataset
        // TODO: Create 10+ authors
        // TODO: Create 20+ categories
        // TODO: Create 500+ posts with various statuses and dates
        // TODO: Create relationships between posts and categories
        // TODO: Vary post popularity (views, likes, comments)
    }
    
    private fun createSampleAuthors(): List<Author> {
        // TODO: Create diverse set of authors
        TODO("Implement sample author creation")
    }
    
    private fun createSampleCategories(): List<Category> {
        // TODO: Create technology, business, lifestyle categories
        TODO("Implement sample category creation")
    }
    
    private fun createSamplePosts(authors: List<Author>, categories: List<Category>): List<Post> {
        // TODO: Create posts with varied content and metadata
        // TODO: Distribute posts across different time periods
        // TODO: Vary post popularity metrics
        TODO("Implement sample post creation")
    }
}
```

---

## 🎯 Expected Results

After completing this workshop, you should have:

1. **Advanced Pagination**: Complete implementation of Spring Data pagination with Page and Pageable
2. **Dynamic Filtering**: Specification API implementation for runtime query construction
3. **Flexible Search**: Multi-field search with relevance scoring and highlighting
4. **Performance Optimization**: Efficient queries with proper indexing and projections
5. **Comprehensive DTOs**: Request/response objects optimized for frontend integration
6. **Error Handling**: Robust validation and exception handling for edge cases
7. **Extensive Testing**: Unit and integration tests covering all pagination scenarios

## 🚀 Testing Your Implementation

Run these commands to verify your implementation:

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Start the application
./gradlew bootRun

# Test basic pagination
curl "http://localhost:8080/api/posts?page=0&size=10&sortBy=title&sortDirection=asc"

# Test filtering
curl "http://localhost:8080/api/posts?status=PUBLISHED&authorName=john&page=0&size=5"

# Test search
curl "http://localhost:8080/api/posts/search?query=spring%20boot&page=0&size=10"

# Test category filtering
curl "http://localhost:8080/api/posts/category/technology?page=0&size=10"

# Test popular posts
curl "http://localhost:8080/api/posts/popular?timeframe=LAST_MONTH&page=0&size=10"
```

## 💡 Key Learning Points

- **Spring Data Pagination**: Master Page/Pageable abstractions for consistent pagination
- **Specification API**: Build complex, dynamic queries at runtime
- **Performance Optimization**: Use projections, fetch joins, and proper indexing
- **Search Implementation**: Multi-field search with relevance scoring
- **Frontend Integration**: Design APIs that work seamlessly with frontend pagination
- **Error Handling**: Comprehensive validation and graceful error handling
- **Testing Strategies**: Test pagination, filtering, and performance scenarios

Good luck implementing sophisticated pagination and filtering that can handle large-scale applications!