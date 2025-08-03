/**
 * Lesson 10 Complete Solution: Pagination Configuration
 * 
 * Complete configuration for pagination, caching, and search functionality
 */

package com.learning.pagination.config

import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer

@Configuration
@EnableCaching
class PaginationConfiguration {
    
    companion object {
        const val DEFAULT_PAGE_SIZE = 20
        const val MAX_PAGE_SIZE = 100
        const val DEFAULT_SORT_PROPERTY = "createdAt"
        val DEFAULT_SORT_DIRECTION = Sort.Direction.DESC
    }
    
    @Bean
    fun pageableCustomizer(): PageableHandlerMethodArgumentResolverCustomizer {
        return PageableHandlerMethodArgumentResolverCustomizer { resolver ->
            resolver.setPageParameterName("page")
            resolver.setSizeParameterName("size")
            resolver.setMaxPageSize(MAX_PAGE_SIZE)
            resolver.setFallbackPageable(
                PageRequest.of(0, DEFAULT_PAGE_SIZE, DEFAULT_SORT_DIRECTION, DEFAULT_SORT_PROPERTY)
            )
        }
    }
}