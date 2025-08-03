/**
 * Lesson 10 Complete Solution: Post Criteria Builder
 * 
 * Complete fluent criteria builder for complex query construction
 */

package com.learning.pagination.specification

import com.learning.pagination.model.Post
import com.learning.pagination.model.PostStatus
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDateTime

class PostCriteriaBuilder {
    
    private val specifications = mutableListOf<Specification<Post>>()
    
    fun withStatus(status: PostStatus?): PostCriteriaBuilder {
        status?.let { specifications.add(PostSpecifications.hasStatus(it)) }
        return this
    }
    
    fun withAuthor(authorId: Long?): PostCriteriaBuilder {
        authorId?.let { specifications.add(PostSpecifications.hasAuthor(it)) }
        return this
    }
    
    fun withTitle(title: String?): PostCriteriaBuilder {
        if (!title.isNullOrBlank()) {
            specifications.add(PostSpecifications.titleContains(title))
        }
        return this
    }
    
    fun withContent(content: String?): PostCriteriaBuilder {
        if (!content.isNullOrBlank()) {
            specifications.add(PostSpecifications.contentContains(content))
        }
        return this
    }
    
    fun withTextSearch(searchText: String?): PostCriteriaBuilder {
        if (!searchText.isNullOrBlank()) {
            val searchTerms = parseSearchTerms(searchText)
            specifications.add(PostSpecifications.fullTextSearch(searchTerms))
        }
        return this
    }
    
    fun withCategories(categoryNames: List<String>): PostCriteriaBuilder {
        if (categoryNames.isNotEmpty()) {
            specifications.add(PostSpecifications.hasCategoryIn(categoryNames))
        }
        return this
    }
    
    fun withCategory(categoryName: String?): PostCriteriaBuilder {
        if (!categoryName.isNullOrBlank()) {
            specifications.add(PostSpecifications.hasCategory(categoryName))
        }
        return this
    }
    
    fun withDateRange(start: LocalDateTime?, end: LocalDateTime?): PostCriteriaBuilder {
        when {
            start != null && end != null -> {
                specifications.add(PostSpecifications.createdBetween(start, end))
            }
            start != null -> {
                specifications.add(PostSpecifications.createdAfter(start))
            }
            end != null -> {
                specifications.add(PostSpecifications.createdBefore(end))
            }
        }
        return this
    }
    
    fun withCreatedAfter(date: LocalDateTime?): PostCriteriaBuilder {
        date?.let { specifications.add(PostSpecifications.createdAfter(it)) }
        return this
    }
    
    fun withCreatedBefore(date: LocalDateTime?): PostCriteriaBuilder {
        date?.let { specifications.add(PostSpecifications.createdBefore(it)) }
        return this
    }
    
    fun withPublishedAfter(date: LocalDateTime?): PostCriteriaBuilder {
        date?.let { specifications.add(PostSpecifications.publishedAfter(it)) }
        return this
    }
    
    fun withPublishedBefore(date: LocalDateTime?): PostCriteriaBuilder {
        date?.let { specifications.add(PostSpecifications.publishedBefore(it)) }
        return this
    }
    
    fun withViewCountRange(min: Long?, max: Long?): PostCriteriaBuilder {
        when {
            min != null && max != null -> {
                specifications.add(PostSpecifications.viewCountBetween(min, max))
            }
            min != null -> {
                specifications.add(PostSpecifications.viewCountGreaterThan(min))
            }
        }
        return this
    }
    
    fun withMinViewCount(minViews: Long?): PostCriteriaBuilder {
        minViews?.let { specifications.add(PostSpecifications.viewCountGreaterThan(it)) }
        return this
    }
    
    fun withLikeCountRange(min: Long?, max: Long?): PostCriteriaBuilder {
        when {
            min != null && max != null -> {
                specifications.add(PostSpecifications.likeCountBetween(min, max))
            }
            min != null -> {
                specifications.add(PostSpecifications.likeCountGreaterThan(min))
            }
        }
        return this
    }
    
    fun withMinLikeCount(minLikes: Long?): PostCriteriaBuilder {
        minLikes?.let { specifications.add(PostSpecifications.likeCountGreaterThan(it)) }
        return this
    }
    
    fun withAuthorName(authorName: String?): PostCriteriaBuilder {
        if (!authorName.isNullOrBlank()) {
            specifications.add(PostSpecifications.authorNameContains(authorName))
        }
        return this
    }
    
    fun withMinimumComments(count: Int?): PostCriteriaBuilder {
        count?.let { specifications.add(PostSpecifications.hasMinimumComments(it)) }
        return this
    }
    
    fun withCommentCountRange(min: Int?, max: Int?): PostCriteriaBuilder {
        if (min != null && max != null) {
            specifications.add(PostSpecifications.hasCommentCountBetween(min, max))
        } else if (min != null) {
            specifications.add(PostSpecifications.hasMinimumComments(min))
        }
        return this
    }
    
    fun withPopularityFilter(isPopular: Boolean?): PostCriteriaBuilder {
        if (isPopular == true) {
            specifications.add(PostSpecifications.isPopular())
        }
        return this
    }
    
    fun withRecentFilter(days: Int?): PostCriteriaBuilder {
        days?.let { specifications.add(PostSpecifications.isRecent(it)) }
        return this
    }
    
    fun withFeaturedFilter(isFeatured: Boolean?): PostCriteriaBuilder {
        if (isFeatured == true) {
            specifications.add(PostSpecifications.isFeatured())
        }
        return this
    }
    
    fun withPublishedFilter(publishedOnly: Boolean = true): PostCriteriaBuilder {
        if (publishedOnly) {
            specifications.add(PostSpecifications.isPublished())
        }
        return this
    }
    
    fun withVisibilityFilter(publicOnly: Boolean = true): PostCriteriaBuilder {
        if (publicOnly) {
            specifications.add(PostSpecifications.isVisibleToPublic())
        }
        return this
    }
    
    fun withApprovedCommentsFilter(hasApprovedComments: Boolean?): PostCriteriaBuilder {
        if (hasApprovedComments == true) {
            specifications.add(PostSpecifications.hasApprovedComments())
        }
        return this
    }
    
    // Advanced combinations
    fun withEngagementCriteria(minViews: Long?, minLikes: Long?, minComments: Int?): PostCriteriaBuilder {
        minViews?.let { withMinViewCount(it) }
        minLikes?.let { withMinLikeCount(it) }
        minComments?.let { withMinimumComments(it) }
        return this
    }
    
    fun withContentSearch(title: String?, content: String?, summary: String?): PostCriteriaBuilder {
        title?.let { withTitle(it) }
        content?.let { withContent(it) }
        // Note: summary search can be added if needed
        return this
    }
    
    fun withTimeframe(days: Int?): PostCriteriaBuilder {
        days?.let { withRecentFilter(it) }
        return this
    }
    
    // Build the final specification
    fun build(): Specification<Post>? {
        return if (specifications.isEmpty()) {
            null
        } else {
            specifications.reduce { acc, spec -> acc.and(spec) }
        }
    }
    
    fun buildWithDefaults(): Specification<Post> {
        // If no criteria specified, default to published posts only
        return build() ?: PostSpecifications.isVisibleToPublic()
    }
    
    fun hasCriteria(): Boolean {
        return specifications.isNotEmpty()
    }
    
    fun getCriteriaCount(): Int {
        return specifications.size
    }
    
    // Helper methods
    private fun parseSearchTerms(searchText: String): List<String> {
        return searchText.trim()
            .split("\\s+".toRegex())
            .filter { it.isNotBlank() && it.length >= 2 } // Filter out very short terms
            .map { it.lowercase() }
            .distinct()
    }
    
    // Factory methods for common use cases
    companion object {
        
        fun forPublicSearch(): PostCriteriaBuilder {
            return PostCriteriaBuilder().withVisibilityFilter(true)
        }
        
        fun forAuthorPosts(authorId: Long): PostCriteriaBuilder {
            return PostCriteriaBuilder().withAuthor(authorId)
        }
        
        fun forCategory(categoryName: String): PostCriteriaBuilder {
            return PostCriteriaBuilder()
                .withCategory(categoryName)
                .withVisibilityFilter(true)
        }
        
        fun forPopularPosts(): PostCriteriaBuilder {
            return PostCriteriaBuilder()
                .withPopularityFilter(true)
                .withVisibilityFilter(true)
        }
        
        fun forRecentPosts(days: Int = 30): PostCriteriaBuilder {
            return PostCriteriaBuilder()
                .withRecentFilter(days)
                .withVisibilityFilter(true)
        }
        
        fun forFeaturedPosts(): PostCriteriaBuilder {
            return PostCriteriaBuilder()
                .withFeaturedFilter(true)
                .withVisibilityFilter(true)
        }
        
        fun forSearch(query: String): PostCriteriaBuilder {
            return PostCriteriaBuilder()
                .withTextSearch(query)
                .withVisibilityFilter(true)
        }
    }
}