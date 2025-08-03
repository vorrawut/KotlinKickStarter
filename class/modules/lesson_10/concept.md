# 📚 Lesson 10: Pagination & Filtering - Advanced Query Patterns

## 🎯 Learning Objectives

By the end of this lesson, you will be able to:
- Master Spring Data JPA pagination with Pageable and Page interfaces
- Implement dynamic queries using the Specification API
- Build flexible filtering and search functionality
- Design efficient pagination for large datasets
- Optimize query performance with proper indexing and fetching strategies
- Create frontend-friendly pagination responses
- Handle complex sorting and ordering requirements
- Implement full-text search capabilities
- Build reusable query criteria builders
- Test pagination and filtering functionality comprehensively

## 🏗️ Architecture Overview

This lesson demonstrates **advanced query patterns** for handling large datasets efficiently:

- **Pagination Framework**: Spring Data's Page and Pageable abstractions
- **Dynamic Queries**: Specification API for runtime query construction
- **Search Functionality**: Multi-field search with ranking and relevance
- **Filter Chains**: Composable filtering with AND/OR logic
- **Performance Optimization**: Query optimization for large datasets
- **Flexible Sorting**: Multi-column sorting with custom orders
- **Cursor Pagination**: Alternative pagination for real-time data
- **Search Analytics**: Query metrics and performance monitoring

## 📊 Pagination Fundamentals

### Core Concepts

**Pagination**: Breaking large datasets into manageable chunks
**Filtering**: Reducing datasets based on specific criteria  
**Sorting**: Ordering results by one or more fields
**Search**: Finding relevant records using text queries

### Spring Data Pagination

```kotlin
// Pageable - Request specification
val pageable = PageRequest.of(
    page = 0,           // Page number (0-based)
    size = 20,          // Items per page
    sort = Sort.by("createdAt").descending()
)

// Page - Response with metadata
interface Page<T> {
    val content: List<T>          // Current page items
    val totalElements: Long       // Total items across all pages
    val totalPages: Int           // Total number of pages
    val numberOfElements: Int     // Items in current page
    val number: Int               // Current page number
    val size: Int                 // Page size
    val hasNext: Boolean          // Has next page
    val hasPrevious: Boolean      // Has previous page
    val isFirst: Boolean          // Is first page
    val isLast: Boolean           // Is last page
}
```

### Repository Integration

```kotlin
interface PostRepository : JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {
    
    // Method name pagination
    fun findByStatusOrderByCreatedAtDesc(
        status: PostStatus,
        pageable: Pageable
    ): Page<Post>
    
    // Custom query with pagination
    @Query("SELECT p FROM Post p WHERE p.title LIKE %:title%")
    fun findByTitleContaining(
        @Param("title") title: String,
        pageable: Pageable
    ): Page<Post>
    
    // Count query for complex pagination
    @Query(
        value = "SELECT p FROM Post p JOIN p.categories c WHERE c.name = :category",
        countQuery = "SELECT COUNT(p) FROM Post p JOIN p.categories c WHERE c.name = :category"
    )
    fun findByCategory(
        @Param("category") category: String,
        pageable: Pageable
    ): Page<Post>
}
```

## 🔍 Specification API

### Dynamic Query Construction

The Specification API allows building queries at runtime:

```kotlin
import org.springframework.data.jpa.domain.Specification
import jakarta.persistence.criteria.*

// Basic specification
class PostSpecifications {
    
    companion object {
        
        fun hasStatus(status: PostStatus): Specification<Post> {
            return Specification { root, query, criteriaBuilder ->
                criteriaBuilder.equal(root.get<PostStatus>("status"), status)
            }
        }
        
        fun hasAuthor(authorId: Long): Specification<Post> {
            return Specification { root, query, criteriaBuilder ->
                criteriaBuilder.equal(root.get<Author>("author").get<Long>("id"), authorId)
            }
        }
        
        fun titleContains(title: String): Specification<Post> {
            return Specification { root, query, criteriaBuilder ->
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get<String>("title")),
                    "%${title.lowercase()}%"
                )
            }
        }
        
        fun createdAfter(date: LocalDateTime): Specification<Post> {
            return Specification { root, query, criteriaBuilder ->
                criteriaBuilder.greaterThanOrEqualTo(
                    root.get<LocalDateTime>("createdAt"),
                    date
                )
            }
        }
        
        fun hasCategory(categoryName: String): Specification<Post> {
            return Specification { root, query, criteriaBuilder ->
                val categoriesJoin = root.join<Post, Category>("categories")
                criteriaBuilder.equal(categoriesJoin.get<String>("name"), categoryName)
            }
        }
        
        fun isPublishedAndVisible(): Specification<Post> {
            return Specification { root, query, criteriaBuilder ->
                criteriaBuilder.and(
                    criteriaBuilder.equal(root.get<PostStatus>("status"), PostStatus.PUBLISHED),
                    criteriaBuilder.isNotNull(root.get<LocalDateTime>("publishedAt"))
                )
            }
        }
    }
}
```

### Composable Specifications

```kotlin
// Combining specifications with AND/OR logic
class PostService {
    
    fun searchPosts(criteria: SearchCriteria): Page<Post> {
        var spec = Specification.where<Post>(null)
        
        // Build specification dynamically
        criteria.status?.let { status ->
            spec = spec.and(PostSpecifications.hasStatus(status))
        }
        
        criteria.authorId?.let { authorId ->
            spec = spec.and(PostSpecifications.hasAuthor(authorId))
        }
        
        criteria.title?.let { title ->
            spec = spec.and(PostSpecifications.titleContains(title))
        }
        
        criteria.categoryName?.let { category ->
            spec = spec.and(PostSpecifications.hasCategory(category))
        }
        
        criteria.createdAfter?.let { date ->
            spec = spec.and(PostSpecifications.createdAfter(date))
        }
        
        // Always filter for published posts unless specified
        if (!criteria.includeAllStatuses) {
            spec = spec.and(PostSpecifications.isPublishedAndVisible())
        }
        
        return postRepository.findAll(spec, criteria.toPageable())
    }
}
```

### Advanced Specification Patterns

```kotlin
class AdvancedPostSpecifications {
    
    companion object {
        
        // OR conditions
        fun titleOrContentContains(searchText: String): Specification<Post> {
            return Specification { root, query, criteriaBuilder ->
                val lowerSearchText = "%${searchText.lowercase()}%"
                criteriaBuilder.or(
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get<String>("title")), 
                        lowerSearchText
                    ),
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get<String>("content")), 
                        lowerSearchText
                    )
                )
            }
        }
        
        // Subqueries
        fun hasMoreThanXComments(commentCount: Long): Specification<Post> {
            return Specification { root, query, criteriaBuilder ->
                val subquery = query.subquery(Long::class.java)
                val subRoot = subquery.from(Comment::class.java)
                subquery.select(criteriaBuilder.count(subRoot))
                subquery.where(criteriaBuilder.equal(subRoot.get<Post>("post"), root))
                
                criteriaBuilder.greaterThan(subquery, commentCount)
            }
        }
        
        // EXISTS subquery
        fun hasApprovedComments(): Specification<Post> {
            return Specification { root, query, criteriaBuilder ->
                val subquery = query.subquery(Comment::class.java)
                val subRoot = subquery.from(Comment::class.java)
                subquery.select(subRoot)
                subquery.where(
                    criteriaBuilder.and(
                        criteriaBuilder.equal(subRoot.get<Post>("post"), root),
                        criteriaBuilder.equal(subRoot.get<CommentStatus>("status"), CommentStatus.APPROVED)
                    )
                )
                
                criteriaBuilder.exists(subquery)
            }
        }
        
        // Range queries
        fun viewCountBetween(min: Long, max: Long): Specification<Post> {
            return Specification { root, query, criteriaBuilder ->
                criteriaBuilder.between(root.get<Long>("viewCount"), min, max)
            }
        }
        
        // Collection size
        fun hasCategoryCount(count: Int): Specification<Post> {
            return Specification { root, query, criteriaBuilder ->
                criteriaBuilder.equal(
                    criteriaBuilder.size(root.get<Set<Category>>("categories")), 
                    count
                )
            }
        }
        
        // Complex joins with conditions
        fun hasAuthorFromCity(city: String): Specification<Post> {
            return Specification { root, query, criteriaBuilder ->
                val authorJoin = root.join<Post, Author>("author")
                val profileJoin = authorJoin.join<Author, UserProfile>("profile")
                criteriaBuilder.equal(profileJoin.get<String>("city"), city)
            }
        }
    }
}
```

## 🔧 Advanced Filtering Patterns

### Criteria Builder Pattern

```kotlin
class PostCriteriaBuilder {
    private val specifications = mutableListOf<Specification<Post>>()
    
    fun withStatus(status: PostStatus): PostCriteriaBuilder {
        specifications.add(PostSpecifications.hasStatus(status))
        return this
    }
    
    fun withAuthor(authorId: Long): PostCriteriaBuilder {
        specifications.add(PostSpecifications.hasAuthor(authorId))
        return this
    }
    
    fun withTitle(title: String): PostCriteriaBuilder {
        if (title.isNotBlank()) {
            specifications.add(PostSpecifications.titleContains(title))
        }
        return this
    }
    
    fun withDateRange(start: LocalDateTime?, end: LocalDateTime?): PostCriteriaBuilder {
        start?.let { 
            specifications.add(PostSpecifications.createdAfter(it))
        }
        end?.let {
            specifications.add(PostSpecifications.createdBefore(it))
        }
        return this
    }
    
    fun withCategories(categoryNames: List<String>): PostCriteriaBuilder {
        if (categoryNames.isNotEmpty()) {
            val categorySpecs = categoryNames.map { PostSpecifications.hasCategory(it) }
            val combinedSpec = categorySpecs.reduce { acc, spec -> acc.or(spec) }
            specifications.add(combinedSpec)
        }
        return this
    }
    
    fun withMinViewCount(minViews: Long): PostCriteriaBuilder {
        specifications.add(PostSpecifications.viewCountGreaterThan(minViews))
        return this
    }
    
    fun withTextSearch(searchText: String): PostCriteriaBuilder {
        if (searchText.isNotBlank()) {
            specifications.add(AdvancedPostSpecifications.titleOrContentContains(searchText))
        }
        return this
    }
    
    fun build(): Specification<Post> {
        return specifications.fold(Specification.where<Post>(null)) { acc, spec ->
            acc.and(spec)
        }
    }
}

// Usage
val criteria = PostCriteriaBuilder()
    .withStatus(PostStatus.PUBLISHED)
    .withTextSearch("spring boot")
    .withCategories(listOf("technology", "programming"))
    .withDateRange(LocalDateTime.now().minusDays(30), null)
    .withMinViewCount(100)
    .build()

val results = postRepository.findAll(criteria, pageRequest)
```

### Filter Request DTOs

```kotlin
data class PostFilterRequest(
    val status: PostStatus? = null,
    val authorId: Long? = null,
    val authorName: String? = null,
    val title: String? = null,
    val content: String? = null,
    val searchText: String? = null,
    val categoryNames: List<String> = emptyList(),
    val tagNames: List<String> = emptyList(),
    val createdAfter: LocalDateTime? = null,
    val createdBefore: LocalDateTime? = null,
    val publishedAfter: LocalDateTime? = null,
    val publishedBefore: LocalDateTime? = null,
    val minViewCount: Long? = null,
    val maxViewCount: Long? = null,
    val minLikeCount: Long? = null,
    val maxLikeCount: Long? = null,
    val minCommentCount: Int? = null,
    val maxCommentCount: Int? = null,
    val includeDeleted: Boolean = false,
    val includeUnpublished: Boolean = false
) {
    
    fun toSpecification(): Specification<Post> {
        return PostCriteriaBuilder().apply {
            status?.let { withStatus(it) }
            authorId?.let { withAuthor(it) }
            title?.let { withTitle(it) }
            searchText?.let { withTextSearch(it) }
            withCategories(categoryNames)
            withDateRange(createdAfter, createdBefore)
            minViewCount?.let { withMinViewCount(it) }
            // Add more criteria as needed
        }.build()
    }
    
    fun toPageable(
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "createdAt",
        sortDirection: String = "desc"
    ): Pageable {
        val sort = Sort.by(
            if (sortDirection.lowercase() == "desc") Sort.Direction.DESC else Sort.Direction.ASC,
            sortBy
        )
        return PageRequest.of(page, size, sort)
    }
}
```

## 📄 Advanced Pagination Patterns

### Custom Page Implementation

```kotlin
data class PageResponse<T>(
    val content: List<T>,
    val page: PageMetadata,
    val sort: SortMetadata
) {
    
    data class PageMetadata(
        val number: Int,
        val size: Int,
        val totalElements: Long,
        val totalPages: Int,
        val first: Boolean,
        val last: Boolean,
        val numberOfElements: Int,
        val empty: Boolean
    )
    
    data class SortMetadata(
        val sorted: Boolean,
        val orders: List<SortOrder>
    )
    
    data class SortOrder(
        val property: String,
        val direction: String,
        val ignoreCase: Boolean
    )
    
    companion object {
        fun <T> from(page: Page<T>): PageResponse<T> {
            return PageResponse(
                content = page.content,
                page = PageMetadata(
                    number = page.number,
                    size = page.size,
                    totalElements = page.totalElements,
                    totalPages = page.totalPages,
                    first = page.isFirst,
                    last = page.isLast,
                    numberOfElements = page.numberOfElements,
                    empty = page.isEmpty
                ),
                sort = SortMetadata(
                    sorted = page.sort.isSorted,
                    orders = page.sort.map { order ->
                        SortOrder(
                            property = order.property,
                            direction = order.direction.name,
                            ignoreCase = order.isIgnoreCase
                        )
                    }
                )
            )
        }
    }
}
```

### Cursor-Based Pagination

```kotlin
// For real-time data where new items are frequently added
data class CursorPageRequest(
    val size: Int = 20,
    val cursor: String? = null,  // Base64 encoded cursor
    val direction: CursorDirection = CursorDirection.FORWARD
)

enum class CursorDirection {
    FORWARD, BACKWARD
}

data class CursorPage<T>(
    val content: List<T>,
    val hasNext: Boolean,
    val hasPrevious: Boolean,
    val nextCursor: String?,
    val previousCursor: String?
)

class CursorPagination {
    
    fun findPostsAfterCursor(
        cursor: String?,
        size: Int
    ): CursorPage<Post> {
        val spec = if (cursor != null) {
            val decodedCursor = decodeCursor(cursor)
            PostSpecifications.idGreaterThan(decodedCursor.id)
                .and(PostSpecifications.createdAfter(decodedCursor.timestamp))
        } else {
            Specification.where<Post>(null)
        }
        
        val sort = Sort.by(Sort.Direction.ASC, "createdAt", "id")
        val pageable = PageRequest.of(0, size + 1, sort)
        
        val results = postRepository.findAll(spec, pageable)
        val content = results.content.take(size)
        val hasNext = results.content.size > size
        
        return CursorPage(
            content = content,
            hasNext = hasNext,
            hasPrevious = cursor != null,
            nextCursor = if (hasNext) encodeCursor(content.last()) else null,
            previousCursor = if (content.isNotEmpty()) encodeCursor(content.first()) else null
        )
    }
    
    private fun encodeCursor(post: Post): String {
        val cursorData = "${post.id}:${post.createdAt}"
        return Base64.getEncoder().encodeToString(cursorData.toByteArray())
    }
    
    private fun decodeCursor(cursor: String): CursorData {
        val decoded = String(Base64.getDecoder().decode(cursor))
        val parts = decoded.split(":")
        return CursorData(
            id = parts[0].toLong(),
            timestamp = LocalDateTime.parse(parts[1])
        )
    }
}

data class CursorData(val id: Long, val timestamp: LocalDateTime)
```

## 🔍 Full-Text Search Implementation

### Search Service

```kotlin
@Service
class SearchService {
    
    fun searchPosts(
        query: String,
        filters: PostFilterRequest,
        pageable: Pageable
    ): Page<PostSearchResult> {
        
        // Combine text search with other filters
        val searchSpec = buildSearchSpecification(query)
        val filterSpec = filters.toSpecification()
        val combinedSpec = searchSpec.and(filterSpec)
        
        val results = postRepository.findAll(combinedSpec, pageable)
        
        return results.map { post ->
            PostSearchResult.from(post, calculateRelevanceScore(post, query))
        }
    }
    
    private fun buildSearchSpecification(query: String): Specification<Post> {
        val terms = query.split("\\s+".toRegex()).filter { it.isNotBlank() }
        
        return terms.fold(Specification.where<Post>(null)) { spec, term ->
            val termSpec = Specification<Post> { root, criteriaQuery, criteriaBuilder ->
                criteriaBuilder.or(
                    // Title search (higher weight)
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get<String>("title")),
                        "%${term.lowercase()}%"
                    ),
                    // Content search
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get<String>("content")),
                        "%${term.lowercase()}%"
                    ),
                    // Summary search
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get<String>("summary")),
                        "%${term.lowercase()}%"
                    ),
                    // Category search
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.join<Post, Category>("categories").get<String>("name")),
                        "%${term.lowercase()}%"
                    ),
                    // Author search
                    criteriaBuilder.or(
                        criteriaBuilder.like(
                            criteriaBuilder.lower(root.get<Author>("author").get<String>("firstName")),
                            "%${term.lowercase()}%"
                        ),
                        criteriaBuilder.like(
                            criteriaBuilder.lower(root.get<Author>("author").get<String>("lastName")),
                            "%${term.lowercase()}%"
                        )
                    )
                )
            }
            
            if (spec == null) termSpec else spec.and(termSpec)
        }
    }
    
    private fun calculateRelevanceScore(post: Post, query: String): Double {
        val terms = query.lowercase().split("\\s+".toRegex())
        var score = 0.0
        
        // Title matches (weight: 3.0)
        terms.forEach { term ->
            if (post.title.lowercase().contains(term)) {
                score += 3.0
            }
        }
        
        // Content matches (weight: 1.0)
        terms.forEach { term ->
            val occurrences = post.content.lowercase().split(term).size - 1
            score += occurrences * 1.0
        }
        
        // Category matches (weight: 2.0)
        terms.forEach { term ->
            post.categories.forEach { category ->
                if (category.name.lowercase().contains(term)) {
                    score += 2.0
                }
            }
        }
        
        // Boost for recent posts
        val daysSinceCreation = ChronoUnit.DAYS.between(post.createdAt, LocalDateTime.now())
        val recencyBoost = maxOf(0.0, 1.0 - (daysSinceCreation / 365.0))
        score *= (1.0 + recencyBoost)
        
        // Boost for popular posts
        val popularityBoost = (post.viewCount + post.likeCount * 5) / 1000.0
        score *= (1.0 + popularityBoost)
        
        return score
    }
}

data class PostSearchResult(
    val post: Post,
    val relevanceScore: Double,
    val highlightedTitle: String,
    val highlightedSummary: String
) {
    companion object {
        fun from(post: Post, relevanceScore: Double): PostSearchResult {
            return PostSearchResult(
                post = post,
                relevanceScore = relevanceScore,
                highlightedTitle = post.title,  // TODO: Add highlighting
                highlightedSummary = post.getEffectiveSummary()  // TODO: Add highlighting
            )
        }
    }
}
```

## ⚡ Performance Optimization

### Query Optimization Strategies

```kotlin
interface OptimizedPostRepository : JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {
    
    // Fetch joins to prevent N+1 queries
    @Query("""
        SELECT DISTINCT p FROM Post p 
        LEFT JOIN FETCH p.author 
        LEFT JOIN FETCH p.categories 
        WHERE p.status = :status
    """)
    fun findByStatusWithDetails(
        @Param("status") status: PostStatus,
        pageable: Pageable
    ): Page<Post>
    
    // Projection for list views
    @Query("""
        SELECT new com.learning.pagination.dto.PostSummaryProjection(
            p.id, p.title, p.summary, p.status, p.createdAt, p.viewCount, p.likeCount,
            a.firstName, a.lastName, 
            SIZE(p.comments),
            SIZE(p.categories)
        )
        FROM Post p 
        JOIN p.author a 
        WHERE p.status = :status
    """)
    fun findPostSummaries(
        @Param("status") status: PostStatus,
        pageable: Pageable
    ): Page<PostSummaryProjection>
    
    // Count query optimization
    @Query(
        value = """
            SELECT p FROM Post p 
            JOIN p.categories c 
            WHERE c.name IN :categoryNames
        """,
        countQuery = """
            SELECT COUNT(DISTINCT p.id) FROM Post p 
            JOIN p.categories c 
            WHERE c.name IN :categoryNames
        """
    )
    fun findByCategories(
        @Param("categoryNames") categoryNames: List<String>,
        pageable: Pageable
    ): Page<Post>
    
    // Native query for complex analytics
    @Query(
        value = """
            SELECT p.*, 
                   COUNT(c.id) as comment_count,
                   AVG(c.rating) as avg_rating,
                   RANK() OVER (ORDER BY p.view_count DESC) as popularity_rank
            FROM posts p 
            LEFT JOIN comments c ON p.id = c.post_id AND c.status = 'APPROVED'
            WHERE p.status = :status
            GROUP BY p.id
            ORDER BY p.created_at DESC
        """,
        countQuery = "SELECT COUNT(*) FROM posts WHERE status = :status",
        nativeQuery = true
    )
    fun findPostsWithAnalytics(
        @Param("status") status: String,
        pageable: Pageable
    ): Page<PostAnalyticsProjection>
}
```

### Indexing Strategy

```kotlin
@Entity
@Table(
    name = "posts",
    indexes = [
        // Single column indexes
        Index(name = "idx_posts_status", columnList = "status"),
        Index(name = "idx_posts_created_at", columnList = "created_at"),
        Index(name = "idx_posts_published_at", columnList = "published_at"),
        Index(name = "idx_posts_view_count", columnList = "view_count"),
        
        // Composite indexes for common query patterns
        Index(name = "idx_posts_status_created", columnList = "status, created_at DESC"),
        Index(name = "idx_posts_author_status", columnList = "author_id, status"),
        Index(name = "idx_posts_status_published", columnList = "status, published_at DESC"),
        
        // Full-text search indexes (PostgreSQL)
        Index(name = "idx_posts_title_gin", columnList = "title", unique = false),
        Index(name = "idx_posts_content_gin", columnList = "content", unique = false)
    ]
)
class Post {
    // Entity definition
}

// For advanced full-text search with PostgreSQL
@Query(
    value = """
        SELECT p.*, ts_rank(to_tsvector('english', p.title || ' ' || p.content), plainto_tsquery('english', :query)) as rank
        FROM posts p 
        WHERE to_tsvector('english', p.title || ' ' || p.content) @@ plainto_tsquery('english', :query)
        AND p.status = 'PUBLISHED'
        ORDER BY rank DESC, p.created_at DESC
    """,
    nativeQuery = true
)
fun searchWithFullTextRanking(
    @Param("query") query: String,
    pageable: Pageable
): Page<Post>
```

### Caching Strategies

```kotlin
@Service
class CachedSearchService {
    
    @Autowired
    private lateinit var cacheManager: CacheManager
    
    @Cacheable(
        value = ["post_search"],
        key = "#query + '_' + #filters.hashCode() + '_' + #pageable.pageNumber + '_' + #pageable.pageSize"
    )
    fun searchPostsCached(
        query: String,
        filters: PostFilterRequest,
        pageable: Pageable
    ): Page<PostSearchResult> {
        return searchService.searchPosts(query, filters, pageable)
    }
    
    @Cacheable(
        value = ["popular_posts"],
        key = "#category + '_' + #pageable.pageNumber"
    )
    fun getPopularPostsInCategory(
        category: String,
        pageable: Pageable
    ): Page<Post> {
        return postRepository.findPopularInCategory(category, pageable)
    }
    
    @CacheEvict(value = ["post_search", "popular_posts"], allEntries = true)
    fun clearSearchCache() {
        // Called when posts are created/updated/deleted
    }
}
```

## 🧪 Testing Pagination and Filtering

### Repository Tests

```kotlin
@DataJpaTest
class PostRepositoryPaginationTest {
    
    @Autowired
    private lateinit var postRepository: PostRepository
    
    @Autowired
    private lateinit var testEntityManager: TestEntityManager
    
    @BeforeEach
    fun setUp() {
        // Create test data
        val author = createTestAuthor()
        repeat(50) { i ->
            val post = createTestPost(author, "Post $i", PostStatus.PUBLISHED)
            testEntityManager.persistAndFlush(post)
        }
    }
    
    @Test
    fun `should paginate posts correctly`() {
        val pageRequest = PageRequest.of(0, 10, Sort.by("title"))
        
        val page = postRepository.findByStatus(PostStatus.PUBLISHED, pageRequest)
        
        assertThat(page.content).hasSize(10)
        assertThat(page.totalElements).isEqualTo(50)
        assertThat(page.totalPages).isEqualTo(5)
        assertThat(page.isFirst).isTrue()
        assertThat(page.hasNext()).isTrue()
    }
    
    @Test
    fun `should handle empty results`() {
        val pageRequest = PageRequest.of(0, 10)
        
        val page = postRepository.findByStatus(PostStatus.DELETED, pageRequest)
        
        assertThat(page.content).isEmpty()
        assertThat(page.totalElements).isEqualTo(0)
        assertThat(page.totalPages).isEqualTo(0)
        assertThat(page.isEmpty).isTrue()
    }
    
    @Test
    fun `should sort posts correctly`() {
        val pageRequest = PageRequest.of(0, 10, Sort.by("title").ascending())
        
        val page = postRepository.findByStatus(PostStatus.PUBLISHED, pageRequest)
        
        val titles = page.content.map { it.title }
        assertThat(titles).isSorted()
    }
}
```

### Specification Tests

```kotlin
@SpringBootTest
class PostSpecificationTest {
    
    @Autowired
    private lateinit var postRepository: PostRepository
    
    @Test
    fun `should filter by title containing text`() {
        val spec = PostSpecifications.titleContains("Spring")
        
        val results = postRepository.findAll(spec)
        
        assertThat(results).allMatch { 
            it.title.contains("Spring", ignoreCase = true) 
        }
    }
    
    @Test
    fun `should combine multiple specifications`() {
        val spec = PostSpecifications.hasStatus(PostStatus.PUBLISHED)
            .and(PostSpecifications.titleContains("Kotlin"))
            .and(PostSpecifications.createdAfter(LocalDateTime.now().minusDays(30)))
        
        val results = postRepository.findAll(spec)
        
        assertThat(results).allMatch { 
            it.status == PostStatus.PUBLISHED && 
            it.title.contains("Kotlin", ignoreCase = true) &&
            it.createdAt.isAfter(LocalDateTime.now().minusDays(30))
        }
    }
}
```

### Performance Tests

```kotlin
@SpringBootTest
class PaginationPerformanceTest {
    
    @Autowired
    private lateinit var postRepository: PostRepository
    
    @Test
    fun `should handle large dataset pagination efficiently`() {
        // Create large dataset
        val author = createTestAuthor()
        repeat(10000) { i ->
            createTestPost(author, "Post $i")
        }
        
        val startTime = System.currentTimeMillis()
        
        // Test pagination performance
        val pageRequest = PageRequest.of(100, 50)
        val page = postRepository.findByStatus(PostStatus.PUBLISHED, pageRequest)
        
        val duration = System.currentTimeMillis() - startTime
        
        assertThat(page.content).hasSize(50)
        assertThat(duration).isLessThan(1000) // Should complete in under 1 second
    }
}
```

## 🎯 Key Takeaways

1. **Pagination**: Use Spring Data's Page and Pageable for consistent pagination
2. **Dynamic Queries**: Leverage Specification API for runtime query construction
3. **Performance**: Optimize with proper indexing, fetch strategies, and projections
4. **Search**: Implement multi-field search with relevance scoring
5. **Filtering**: Build composable filter chains with builder patterns
6. **Caching**: Cache expensive search operations appropriately
7. **Testing**: Test pagination, filtering, and performance comprehensively
8. **Frontend Integration**: Design APIs that work well with frontend pagination components

This lesson provides comprehensive patterns for handling large datasets efficiently while maintaining flexibility and performance.