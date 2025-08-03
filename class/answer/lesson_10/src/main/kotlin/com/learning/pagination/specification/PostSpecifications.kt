/**
 * Lesson 10 Complete Solution: Post Specifications
 * 
 * Complete specifications for dynamic post querying with advanced patterns
 */

package com.learning.pagination.specification

import com.learning.pagination.model.*
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDateTime

class PostSpecifications {
    
    companion object {
        
        // Basic field specifications
        fun hasStatus(status: PostStatus): Specification<Post> {
            return Specification { root, _, criteriaBuilder ->
                criteriaBuilder.equal(root.get<PostStatus>("status"), status)
            }
        }
        
        fun hasAuthor(authorId: Long): Specification<Post> {
            return Specification { root, _, criteriaBuilder ->
                criteriaBuilder.equal(root.get<Author>("author").get<Long>("id"), authorId)
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
        
        fun contentContains(content: String): Specification<Post> {
            return Specification { root, _, criteriaBuilder ->
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get<String>("content")),
                    "%${content.lowercase()}%"
                )
            }
        }
        
        // Date range specifications
        fun createdBetween(start: LocalDateTime, end: LocalDateTime): Specification<Post> {
            return Specification { root, _, criteriaBuilder ->
                criteriaBuilder.between(root.get<LocalDateTime>("createdAt"), start, end)
            }
        }
        
        fun createdAfter(date: LocalDateTime): Specification<Post> {
            return Specification { root, _, criteriaBuilder ->
                criteriaBuilder.greaterThanOrEqualTo(root.get<LocalDateTime>("createdAt"), date)
            }
        }
        
        fun createdBefore(date: LocalDateTime): Specification<Post> {
            return Specification { root, _, criteriaBuilder ->
                criteriaBuilder.lessThanOrEqualTo(root.get<LocalDateTime>("createdAt"), date)
            }
        }
        
        fun publishedAfter(date: LocalDateTime): Specification<Post> {
            return Specification { root, _, criteriaBuilder ->
                criteriaBuilder.greaterThanOrEqualTo(root.get<LocalDateTime>("publishedAt"), date)
            }
        }
        
        fun publishedBefore(date: LocalDateTime): Specification<Post> {
            return Specification { root, _, criteriaBuilder ->
                criteriaBuilder.lessThanOrEqualTo(root.get<LocalDateTime>("publishedAt"), date)
            }
        }
        
        // Numeric range specifications
        fun viewCountBetween(min: Long, max: Long): Specification<Post> {
            return Specification { root, _, criteriaBuilder ->
                criteriaBuilder.between(root.get<Long>("viewCount"), min, max)
            }
        }
        
        fun viewCountGreaterThan(count: Long): Specification<Post> {
            return Specification { root, _, criteriaBuilder ->
                criteriaBuilder.greaterThan(root.get<Long>("viewCount"), count)
            }
        }
        
        fun likeCountGreaterThan(count: Long): Specification<Post> {
            return Specification { root, _, criteriaBuilder ->
                criteriaBuilder.greaterThan(root.get<Long>("likeCount"), count)
            }
        }
        
        fun likeCountBetween(min: Long, max: Long): Specification<Post> {
            return Specification { root, _, criteriaBuilder ->
                criteriaBuilder.between(root.get<Long>("likeCount"), min, max)
            }
        }
        
        // Relationship specifications
        fun hasCategory(categoryName: String): Specification<Post> {
            return Specification { root, _, criteriaBuilder ->
                val categoriesJoin = root.join<Post, Category>("categories")
                criteriaBuilder.equal(categoriesJoin.get<String>("name"), categoryName)
            }
        }
        
        fun hasCategoryIn(categoryNames: List<String>): Specification<Post> {
            return Specification { root, _, criteriaBuilder ->
                val categoriesJoin = root.join<Post, Category>("categories")
                categoriesJoin.get<String>("name").`in`(categoryNames)
            }
        }
        
        fun hasMinimumComments(count: Int): Specification<Post> {
            return Specification { root, _, criteriaBuilder ->
                criteriaBuilder.greaterThanOrEqualTo(
                    criteriaBuilder.size(root.get<List<Comment>>("comments")),
                    count
                )
            }
        }
        
        fun hasCommentCountBetween(min: Int, max: Int): Specification<Post> {
            return Specification { root, _, criteriaBuilder ->
                val commentSize = criteriaBuilder.size(root.get<List<Comment>>("comments"))
                criteriaBuilder.between(commentSize, min, max)
            }
        }
        
        // Advanced specifications
        fun titleOrContentContains(searchText: String): Specification<Post> {
            return Specification { root, _, criteriaBuilder ->
                val lowerSearchText = "%${searchText.lowercase()}%"
                criteriaBuilder.or(
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get<String>("title")),
                        lowerSearchText
                    ),
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get<String>("content")),
                        lowerSearchText
                    ),
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get<String>("summary")),
                        lowerSearchText
                    )
                )
            }
        }
        
        fun authorNameContains(authorName: String): Specification<Post> {
            return Specification { root, _, criteriaBuilder ->
                val authorJoin = root.join<Post, Author>("author")
                val lowerName = "%${authorName.lowercase()}%"
                criteriaBuilder.or(
                    criteriaBuilder.like(
                        criteriaBuilder.lower(authorJoin.get<String>("firstName")),
                        lowerName
                    ),
                    criteriaBuilder.like(
                        criteriaBuilder.lower(authorJoin.get<String>("lastName")),
                        lowerName
                    ),
                    criteriaBuilder.like(
                        criteriaBuilder.lower(authorJoin.get<String>("username")),
                        lowerName
                    )
                )
            }
        }
        
        fun isPopular(): Specification<Post> {
            return Specification { root, _, criteriaBuilder ->
                criteriaBuilder.or(
                    criteriaBuilder.greaterThan(root.get<Long>("viewCount"), 100),
                    criteriaBuilder.greaterThan(root.get<Long>("likeCount"), 10),
                    criteriaBuilder.greaterThan(
                        criteriaBuilder.size(root.get<List<Comment>>("comments")),
                        5
                    )
                )
            }
        }
        
        fun isRecent(days: Int): Specification<Post> {
            return Specification { root, _, criteriaBuilder ->
                val cutoffDate = LocalDateTime.now().minusDays(days.toLong())
                criteriaBuilder.greaterThanOrEqualTo(root.get<LocalDateTime>("createdAt"), cutoffDate)
            }
        }
        
        fun isFeatured(): Specification<Post> {
            return Specification { root, _, criteriaBuilder ->
                criteriaBuilder.isTrue(root.get<Boolean>("featured"))
            }
        }
        
        fun isPublished(): Specification<Post> {
            return Specification { root, _, criteriaBuilder ->
                criteriaBuilder.and(
                    criteriaBuilder.equal(root.get<PostStatus>("status"), PostStatus.PUBLISHED),
                    criteriaBuilder.isNotNull(root.get<LocalDateTime>("publishedAt"))
                )
            }
        }
        
        fun isVisibleToPublic(): Specification<Post> {
            return Specification { root, _, criteriaBuilder ->
                criteriaBuilder.equal(root.get<PostStatus>("status"), PostStatus.PUBLISHED)
            }
        }
        
        // Complex specifications with subqueries
        fun hasApprovedComments(): Specification<Post> {
            return Specification { root, query, criteriaBuilder ->
                val subquery = query?.subquery(Comment::class.java)
                val subRoot = subquery?.from(Comment::class.java)
                subquery?.select(subRoot)
                subquery?.where(
                    criteriaBuilder.and(
                        criteriaBuilder.equal(subRoot?.get<Post>("post"), root),
                        criteriaBuilder.equal(subRoot?.get<CommentStatus>("status"), CommentStatus.APPROVED)
                    )
                )
                criteriaBuilder.exists(subquery)
            }
        }
        
        fun hasMoreCommentsThan(count: Long): Specification<Post> {
            return Specification { root, query, criteriaBuilder ->
                val subquery = query?.subquery(Long::class.java)
                val subRoot = subquery?.from(Comment::class.java)
                subquery?.select(criteriaBuilder.count(subRoot))
                subquery?.where(criteriaBuilder.equal(subRoot?.get<Post>("post"), root))
                
                criteriaBuilder.greaterThan(subquery, count)
            }
        }
        
        // Combined search specification
        fun fullTextSearch(searchTerms: List<String>): Specification<Post> {
            return searchTerms.fold(Specification.where<Post>(null)) { spec, term ->
                val termSpec = Specification<Post> { root, _, criteriaBuilder ->
                    val lowerTerm = "%${term.lowercase()}%"
                    criteriaBuilder.or(
                        // Title search (highest priority)
                        criteriaBuilder.like(
                            criteriaBuilder.lower(root.get<String>("title")),
                            lowerTerm
                        ),
                        // Content search
                        criteriaBuilder.like(
                            criteriaBuilder.lower(root.get<String>("content")),
                            lowerTerm
                        ),
                        // Summary search
                        criteriaBuilder.like(
                            criteriaBuilder.lower(root.get<String>("summary")),
                            lowerTerm
                        ),
                        // Author name search
                        criteriaBuilder.like(
                            criteriaBuilder.lower(root.get<Author>("author").get<String>("firstName")),
                            lowerTerm
                        ),
                        criteriaBuilder.like(
                            criteriaBuilder.lower(root.get<Author>("author").get<String>("lastName")),
                            lowerTerm
                        ),
                        // Category search
                        criteriaBuilder.like(
                            criteriaBuilder.lower(root.join<Post, Category>("categories").get<String>("name")),
                            lowerTerm
                        )
                    )
                }
                
                if (spec == null) termSpec else spec.and(termSpec)
            }
        }
    }
}