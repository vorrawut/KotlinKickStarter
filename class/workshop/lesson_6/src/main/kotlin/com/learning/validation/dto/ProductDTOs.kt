/**
 * Lesson 6 Workshop: Product DTOs with Complex Validation
 * 
 * TODO: Complete these DTO classes with advanced validation patterns
 * This demonstrates:
 * - Complex business validation rules
 * - Conditional validation based on product type
 * - Cross-field validation
 * - Custom validation groups
 */

package com.learning.validation.dto

import com.learning.validation.model.ProductCategory
import com.learning.validation.model.ProductStatus
import java.math.BigDecimal

// TODO: Add comprehensive validation for product creation
data class CreateProductRequest(
    // TODO: Add @field:NotBlank, @field:Size for product name
    val name: String,
    
    // TODO: Add @field:NotBlank, @field:Size for description
    val description: String,
    
    // TODO: Add @field:NotNull, @field:Positive, @field:DecimalMax for price
    val price: BigDecimal,
    
    // TODO: Add @field:NotNull for category
    val category: ProductCategory,
    
    // TODO: Add @field:Size for tags collection
    val tags: Set<String>,
    
    // TODO: Add @field:Valid for nested validation
    val dimensions: CreateProductDimensionsRequest?,
    
    // TODO: Add @field:Positive for weight validation
    val weight: Double?,
    
    // TODO: Add boolean validation
    val isDigital: Boolean,
    
    // TODO: Add @field:Min for stock quantity
    val stockQuantity: Int,
    
    // TODO: Add @field:Min for minimum order quantity
    val minOrderQuantity: Int,
    
    // TODO: Add @field:Min for maximum order quantity (optional)
    val maxOrderQuantity: Int?,
    
    // TODO: Add manufacturer validation (required for some categories)
    val manufacturerId: String?
) {
    // TODO: Add complex cross-field validation
    fun isValidProduct(): Boolean {
        return TODO("Validate complex business rules across fields")
    }
    
    // TODO: Add conditional validation for digital vs physical products
    fun isValidForProductType(): Boolean {
        return TODO("Validate specific rules for digital vs physical products")
    }
    
    // TODO: Add category-specific validation
    fun isValidForCategory(): Boolean {
        return TODO("Validate category-specific requirements")
    }
    
    // TODO: Add pricing validation based on category
    fun isValidPricing(): Boolean {
        return TODO("Validate price is within category limits")
    }
}

// TODO: Add validation for product updates
data class UpdateProductRequest(
    val name: String?,
    val description: String?,
    val price: BigDecimal?,
    val tags: Set<String>?,
    val dimensions: UpdateProductDimensionsRequest?,
    val weight: Double?,
    val stockQuantity: Int?,
    val minOrderQuantity: Int?,
    val maxOrderQuantity: Int?,
    val status: ProductStatus?,
    val manufacturerId: String?
) {
    // TODO: Add validation for partial updates
    fun hasValidUpdates(): Boolean {
        return TODO("Ensure at least one valid field is provided")
    }
    
    // TODO: Add validation for status change business rules
    fun isValidStatusChange(currentStatus: ProductStatus): Boolean {
        return TODO("Validate status transition rules")
    }
}

// TODO: Add validation for product dimensions
data class CreateProductDimensionsRequest(
    // TODO: Add @field:Positive, @field:Max for dimensions
    val length: Double,
    val width: Double,
    val height: Double
) {
    // TODO: Add business validation for dimensions
    fun isWithinShippingLimits(): Boolean {
        return TODO("Check if dimensions are within shipping limits")
    }
    
    // TODO: Add volume calculation validation
    fun isValidVolume(): Boolean {
        return TODO("Validate volume is reasonable for the product type")
    }
}

// TODO: Add validation for dimension updates
data class UpdateProductDimensionsRequest(
    val length: Double?,
    val width: Double?,
    val height: Double?
)

// Response DTOs
data class ProductResponse(
    val id: String,
    val name: String,
    val description: String,
    val price: BigDecimal,
    val discountedPrice: BigDecimal?,
    val category: ProductCategory,
    val tags: Set<String>,
    val dimensions: ProductDimensionsResponse?,
    val weight: Double?,
    val isDigital: Boolean,
    val stockQuantity: Int,
    val minOrderQuantity: Int,
    val maxOrderQuantity: Int?,
    val status: ProductStatus,
    val manufacturerId: String?,
    val isAvailable: Boolean,
    val shippingWeight: Double,
    val createdAt: String,
    val updatedAt: String
)

data class ProductDimensionsResponse(
    val length: Double,
    val width: Double,
    val height: Double,
    val volume: Double,
    val isWithinShippingLimits: Boolean
)

// TODO: Add validation for bulk operations
data class BulkUpdatePriceRequest(
    // TODO: Add @field:NotEmpty for product IDs
    val productIds: Set<String>,
    
    // TODO: Add @field:NotNull, @field:Positive for percentage
    val discountPercent: Double,
    
    // TODO: Add optional validation for reason
    val reason: String?
) {
    // TODO: Add validation for bulk operation limits
    fun isValidBulkOperation(): Boolean {
        return TODO("Validate bulk operation constraints")
    }
}

// TODO: Add validation for product search/filtering
data class ProductSearchRequest(
    val name: String?,
    val category: ProductCategory?,
    val status: ProductStatus?,
    val isDigital: Boolean?,
    val minPrice: BigDecimal?,
    val maxPrice: BigDecimal?,
    val tags: Set<String>?,
    val manufacturerId: String?,
    val inStock: Boolean?,
    
    // Pagination
    val page: Int = 0,
    val size: Int = 20,
    val sortBy: String = "createdAt",
    val sortDirection: String = "DESC"
) {
    // TODO: Add search validation
    fun isValidSearchCriteria(): Boolean {
        return TODO("Validate search parameters and pagination")
    }
}

// TODO: Add validation for order quantity requests
data class ValidateOrderQuantityRequest(
    // TODO: Add @field:NotBlank for product ID
    val productId: String,
    
    // TODO: Add @field:Positive for quantity
    val quantity: Int
)

data class OrderQuantityValidationResponse(
    val isValid: Boolean,
    val availableQuantity: Int,
    val minOrderQuantity: Int,
    val maxOrderQuantity: Int?,
    val validationMessage: String?
)