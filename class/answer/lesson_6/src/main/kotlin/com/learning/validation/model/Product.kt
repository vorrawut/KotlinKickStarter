/**
 * Lesson 6 Complete Solution: Product Domain Model with Business Validation
 */

package com.learning.validation.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: BigDecimal,
    val category: ProductCategory,
    val tags: Set<String>,
    val dimensions: ProductDimensions?,
    val weight: Double?, // in kg
    val isDigital: Boolean,
    val stockQuantity: Int,
    val minOrderQuantity: Int,
    val maxOrderQuantity: Int?,
    val status: ProductStatus,
    val manufacturerId: String?,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    
    fun isAvailable(): Boolean {
        return status == ProductStatus.ACTIVE && stockQuantity > 0
    }
    
    fun canOrderQuantity(quantity: Int): Boolean {
        return isAvailable() && 
               quantity >= minOrderQuantity && 
               (maxOrderQuantity == null || quantity <= maxOrderQuantity) &&
               quantity <= stockQuantity
    }
    
    fun calculateShippingWeight(): Double {
        return if (isDigital) 0.0 else (weight ?: 0.0) + 0.1 // Add packaging weight
    }
    
    fun isValidPriceRange(): Boolean {
        return price > BigDecimal.ZERO && price <= category.maxPrice
    }
    
    fun requiresManufacturer(): Boolean {
        return category.requiresManufacturer
    }
    
    fun isPhysicalProductValid(): Boolean {
        return if (isDigital) true else weight != null && weight > 0 && dimensions != null
    }
    
    fun isDigitalProductValid(): Boolean {
        return if (isDigital) weight == null && dimensions == null else true
    }
    
    fun getDiscountedPrice(discountPercent: Double): BigDecimal {
        require(discountPercent in 0.0..100.0) { "Discount percent must be between 0 and 100" }
        val discountMultiplier = BigDecimal((100.0 - discountPercent) / 100.0)
        return price.multiply(discountMultiplier).setScale(2, RoundingMode.HALF_UP)
    }
}

data class ProductDimensions(
    val length: Double, // in cm
    val width: Double,  // in cm
    val height: Double  // in cm
) {
    fun isValid(): Boolean {
        return length > 0 && width > 0 && height > 0
    }
    
    fun getVolume(): Double {
        return length * width * height
    }
    
    fun exceedsShippingLimits(maxLength: Double, maxWidth: Double, maxHeight: Double): Boolean {
        return length > maxLength || width > maxWidth || height > maxHeight
    }
}

enum class ProductCategory(
    val displayName: String, 
    val requiresManufacturer: Boolean,
    val maxPrice: BigDecimal
) {
    ELECTRONICS("Electronics", true, BigDecimal("10000.00")),
    CLOTHING("Clothing", false, BigDecimal("1000.00")),
    BOOKS("Books", true, BigDecimal("500.00")),
    HOME_GARDEN("Home & Garden", false, BigDecimal("5000.00")),
    SPORTS("Sports & Outdoors", false, BigDecimal("2000.00")),
    DIGITAL("Digital Products", false, BigDecimal("1000.00"))
}

enum class ProductStatus {
    DRAFT,
    ACTIVE,
    OUT_OF_STOCK,
    DISCONTINUED,
    SUSPENDED
}