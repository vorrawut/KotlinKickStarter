# 🔍 Lesson 10: Pagination & Filtering - Advanced Query Patterns

## 🎯 **Learning Objectives**

Master advanced query patterns for handling large datasets efficiently:

- **Spring Data Pagination**: Master Page and Pageable abstractions for consistent data handling
- **Specification API**: Build dynamic, type-safe queries at runtime
- **Advanced Filtering**: Create composable filter chains with complex criteria
- **Full-Text Search**: Implement multi-field search with relevance scoring
- **Performance Optimization**: Optimize queries for large datasets with proper indexing
- **Frontend Integration**: Design pagination APIs that work seamlessly with modern UIs

## 📚 **What You'll Build**

A sophisticated **Search and Pagination System** demonstrating enterprise-scale query patterns:

### **Advanced Query Capabilities**
- **Dynamic Filtering**: Runtime query construction with the Specification API
- **Multi-Field Search**: Intelligent search across posts, authors, categories, and content
- **Flexible Sorting**: Multi-column sorting with custom ordering strategies
- **Range Queries**: Date ranges, numeric ranges, and engagement metrics filtering
- **Relationship Filtering**: Complex joins across multiple entities

### **Performance-Optimized Pagination**
- **Strategic Indexing**: Comprehensive database indexes for fast query execution
- **Query Projections**: Lightweight data loading for list views
- **Batch Processing**: Efficient handling of large result sets
- **Cursor Pagination**: Alternative pagination for real-time data streams
- **Cache Integration**: Search result caching for improved performance

### **Enterprise Features**
- **Search Analytics**: Query metrics and popular search terms tracking
- **Relevance Scoring**: Intelligent ranking of search results
- **Frontend-Friendly APIs**: Pagination responses optimized for modern web applications
- **Error Handling**: Comprehensive validation and graceful error recovery

## 🏗️ **System Architecture**

```
┌─────────────────────────────────────────────────┐
│           REST Controllers                      │
│    (PostController, SearchController)           │
└─────────────────┬───────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────┐
│            Service Layer                        │
│  (PostService, SearchService, CacheService)     │
│         Business Logic & Caching               │
└─────────────────┬───────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────┐
│          Specification Layer                    │
│   (PostSpecifications, PostCriteriaBuilder)    │
│          Dynamic Query Construction             │
└─────────────────┬───────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────┐
│           Repository Layer                      │
│    (JpaRepository + JpaSpecificationExecutor)   │
│        Optimized Query Execution               │
└─────────────────┬───────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────┐
│           Database Layer                        │
│    Strategic Indexes & Query Optimization      │
└─────────────────────────────────────────────────┘
```

## 📊 **Pagination Fundamentals**

### **Core Concepts**
```kotlin
// Spring Data Pagination
val pageable = PageRequest.of(
    page = 0,           // Page number (0-based)
    size = 20,          // Items per page
    sort = Sort.by("createdAt").descending()
)

// Page Response
interface Page<T> {
    val content: List<T>          // Current page items
    val totalElements: Long       // Total items across all pages
    val totalPages: Int           // Total number of pages
    val hasNext: Boolean          // Navigation flags
    val hasPrevious: Boolean
}
```

### **Repository Integration**
```kotlin
interface PostRepository : JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {
    
    // Method name pagination
    fun findByStatusOrderByCreatedAtDesc(
        status: PostStatus,
        pageable: Pageable
    ): Page<Post>
    
    // Custom queries with pagination
    @Query("SELECT p FROM Post p WHERE p.title LIKE %:title%")
    fun findByTitleContaining(
        @Param("title") title: String,
        pageable: Pageable
    ): Page<Post>
}
```

## 🔍 **Specification API Mastery**

### **Dynamic Query Construction**
```kotlin
class PostSpecifications {
    companion object {
        fun hasStatus(status: PostStatus): Specification<Post> {
            return Specification { root, _, criteriaBuilder ->
                criteriaBuilder.equal(root.get<PostStatus>("status"), status)
            }
        }
        
        fun titleContains(title: String): Specification<Post> {
            return Specification { root, _, criteriaBuilder ->
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get<String>("title")),
                    "%${title.lowercase()}%"
                )
            }
        }
    }
}
```

### **Composable Specifications**
```kotlin
// Combining specifications
fun searchPosts(criteria: SearchCriteria): Page<Post> {
    var spec = Specification.where<Post>(null)
    
    criteria.status?.let { status ->
        spec = spec.and(PostSpecifications.hasStatus(status))
    }
    
    criteria.title?.let { title ->
        spec = spec.and(PostSpecifications.titleContains(title))
    }
    
    return postRepository.findAll(spec, criteria.toPageable())
}
```

## 🔧 **Advanced Filtering Patterns**

### **Criteria Builder Pattern**
```kotlin
class PostCriteriaBuilder {
    private val specifications = mutableListOf<Specification<Post>>()
    
    fun withStatus(status: PostStatus): PostCriteriaBuilder {
        specifications.add(PostSpecifications.hasStatus(status))
        return this
    }
    
    fun withTextSearch(searchText: String): PostCriteriaBuilder {
        if (searchText.isNotBlank()) {
            specifications.add(PostSpecifications.titleOrContentContains(searchText))
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
    .build()
```

### **Advanced Specifications**
```kotlin
// OR conditions
fun titleOrContentContains(searchText: String): Specification<Post> {
    return Specification { root, _, criteriaBuilder ->
        criteriaBuilder.or(
            criteriaBuilder.like(
                criteriaBuilder.lower(root.get<String>("title")), 
                "%${searchText.lowercase()}%"
            ),
            criteriaBuilder.like(
                criteriaBuilder.lower(root.get<String>("content")), 
                "%${searchText.lowercase()}%"
            )
        )
    }
}

// Subqueries
fun hasMoreThanXComments(commentCount: Long): Specification<Post> {
    return Specification { root, query, criteriaBuilder ->
        val subquery = query?.subquery(Long::class.java)
        val subRoot = subquery?.from(Comment::class.java)
        subquery?.select(criteriaBuilder.count(subRoot))
        subquery?.where(criteriaBuilder.equal(subRoot?.get<Post>("post"), root))
        
        criteriaBuilder.greaterThan(subquery, commentCount)
    }
}
```

## 📄 **Frontend-Friendly Pagination**

### **Custom Page Response**
```kotlin
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
        val empty: Boolean
    )
    
    companion object {
        fun <T> from(page: Page<T>): PagedResponse<T> {
            return PagedResponse(
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
                )
            )
        }
    }
}
```

## 🔍 **Full-Text Search Implementation**

### **Multi-Field Search**
```kotlin
fun fullTextSearch(searchTerms: List<String>): Specification<Post> {
    return searchTerms.fold(Specification.where<Post>(null)) { spec, term ->
        val termSpec = Specification<Post> { root, _, criteriaBuilder ->
            criteriaBuilder.or(
                // Title search (highest priority)
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get<String>("title")),
                    "%${term.lowercase()}%"
                ),
                // Content search
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get<String>("content")),
                    "%${term.lowercase()}%"
                ),
                // Author name search
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get<Author>("author").get<String>("firstName")),
                    "%${term.lowercase()}%"
                ),
                // Category search
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.join<Post, Category>("categories").get<String>("name")),
                    "%${term.lowercase()}%"
                )
            )
        }
        
        if (spec == null) termSpec else spec.and(termSpec)
    }
}
```

### **Relevance Scoring**
```kotlin
fun getRelevanceScore(searchTerms: List<String>): Double {
    var score = 0.0
    
    searchTerms.forEach { term ->
        // Title matches (weight: 5.0)
        if (title.lowercase().contains(term.lowercase())) {
            score += 5.0
        }
        
        // Content matches (weight: 1.0)
        val contentMatches = content.lowercase().split(term.lowercase()).size - 1
        score += contentMatches * 1.0
        
        // Category matches (weight: 2.0)
        categories.forEach { category ->
            if (category.name.lowercase().contains(term.lowercase())) {
                score += 2.0
            }
        }
    }
    
    // Apply popularity and recency boosts
    return score * (1.0 + popularityBoost) * (1.0 + recencyBoost)
}
```

## ⚡ **Performance Optimization**

### **Strategic Indexing**
```kotlin
@Entity
@Table(
    name = "posts",
    indexes = [
        // Single column indexes
        Index(name = "idx_posts_status", columnList = "status"),
        Index(name = "idx_posts_created_at", columnList = "created_at"),
        
        // Composite indexes for common query patterns
        Index(name = "idx_posts_status_created", columnList = "status, created_at DESC"),
        Index(name = "idx_posts_author_status", columnList = "author_id, status"),
        
        // Popularity indexes
        Index(name = "idx_posts_popularity", columnList = "view_count DESC, like_count DESC")
    ]
)
```

### **Query Projections**
```kotlin
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
```

### **Cache Integration**
```kotlin
@Service
class CachedSearchService {
    
    @Cacheable(
        value = ["post_search"],
        key = "#query + '_' + #filters.hashCode() + '_' + #pageable.pageNumber"
    )
    fun searchPostsCached(
        query: String,
        filters: PostSearchRequest,
        pageable: Pageable
    ): Page<PostSearchResult> {
        return searchService.searchPosts(query, filters, pageable)
    }
}
```

## 🧪 **Testing Strategies**

### **Repository Tests**
```kotlin
@DataJpaTest
class PostRepositoryPaginationTest {
    
    @Test
    fun `should paginate posts correctly`() {
        val pageRequest = PageRequest.of(0, 10, Sort.by("title"))
        
        val page = postRepository.findByStatus(PostStatus.PUBLISHED, pageRequest)
        
        assertThat(page.content).hasSize(10)
        assertThat(page.totalElements).isEqualTo(50)
        assertThat(page.isFirst).isTrue()
        assertThat(page.hasNext()).isTrue()
    }
}
```

### **Specification Tests**
```kotlin
@SpringBootTest
class PostSpecificationTest {
    
    @Test
    fun `should filter by title containing text`() {
        val spec = PostSpecifications.titleContains("Spring")
        
        val results = postRepository.findAll(spec)
        
        assertThat(results).allMatch { 
            it.title.contains("Spring", ignoreCase = true) 
        }
    }
}
```

## 🚀 **Sample API Endpoints**

### **Basic Pagination**
```http
GET /api/posts?page=0&size=20&sortBy=createdAt&sortDirection=desc
```

### **Advanced Filtering**
```http
GET /api/posts?status=PUBLISHED&authorName=john&categoryNames=technology,programming&minViewCount=100&page=0&size=10
```

### **Full-Text Search**
```http
GET /api/posts/search?query=spring%20boot%20tutorial&page=0&size=10&sortBy=relevance
```

### **Category-Based Listing**
```http
GET /api/posts/category/technology?page=0&size=20&sortBy=popularity
```

### **Popular Posts**
```http
GET /api/posts/popular?timeframe=LAST_MONTH&page=0&size=10
```

## 🎯 **Learning Outcomes**

After completing this lesson, you'll be able to:

- **Master Spring Data Pagination** with Page and Pageable abstractions
- **Build Dynamic Queries** using the Specification API for runtime query construction
- **Implement Advanced Filtering** with composable criteria and complex conditions
- **Create Full-Text Search** with multi-field search and relevance scoring
- **Optimize Performance** with strategic indexing, projections, and caching
- **Design Frontend-Friendly APIs** with comprehensive pagination metadata
- **Handle Large Datasets** efficiently with proper query optimization

## 📊 **Real-World Applications**

This lesson demonstrates patterns used in:

- **E-commerce Platforms**: Product catalogs with advanced filtering and search
- **Content Management Systems**: Article/blog search with pagination
- **Social Media**: Post feeds with infinite scroll and filtering
- **Enterprise Applications**: Data tables with sorting, filtering, and search
- **Documentation Sites**: Knowledge base search with categorization

## 🛠️ **Technologies Demonstrated**

- **Spring Data JPA**: Pagination, Specification API, custom repositories
- **Hibernate**: Query optimization, indexing strategies, performance tuning
- **Criteria API**: Type-safe dynamic query construction
- **Spring Cache**: Result caching for improved performance
- **H2/PostgreSQL**: Database configuration and optimization

## 📁 **Project Structure**

```
lesson_10/
├── workshop/           # Student starter code with comprehensive TODOs
├── answer/            # Complete working solution with all features
├── modules/
│   ├── concept.md     # Advanced pagination and filtering theory
│   ├── workshop_10.md # Step-by-step implementation guide
│   └── README.md      # This overview
└── tests/             # Comprehensive testing examples
```

## 🎉 **Ready to Start?**

1. **Study the Concepts**: Begin with `concept.md` for comprehensive pagination theory
2. **Follow the Workshop**: Use `workshop_10.md` for hands-on implementation
3. **Reference the Solution**: Check your work against the complete `answer/` implementation
4. **Test Thoroughly**: Run all tests and experiment with different query patterns

## 💡 **Pro Tips**

- **Start with Simple Pagination**: Master basic Page/Pageable before moving to Specifications
- **Index Strategically**: Create indexes based on your most common query patterns
- **Test Performance**: Use large datasets to verify pagination performance
- **Cache Wisely**: Cache expensive search operations but consider cache invalidation
- **Think Frontend**: Design APIs that make frontend pagination implementation easy

---

**🔍 Master advanced query patterns that power modern, data-driven applications!**