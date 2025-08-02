/**
 * Lesson 6 Workshop: Product Domain Model
 * 
 * TODO: Complete this domain model class
 * This demonstrates:
 * - Complex business validation rules
 * - Cross-field validation
 * - Conditional validation
 */

package com.learning.validation.model

import java.math.BigDecimal
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
    
    // TODO: Add business validation methods
    fun isAvailable(): Boolean {
        return TODO("Check if product is available (status = ACTIVE and stock > 0)")
    }
    
    fun canOrderQuantity(quantity: Int): Boolean {
        return TODO("Validate if requested quantity is within min/max bounds and available stock")
    }
    
    fun calculateShippingWeight(): Double {
        return TODO("Calculate shipping weight (product weight + packaging)")
    }
    
    fun isValidPriceRange(): Boolean {
        return TODO("Validate price is within acceptable range for category")
    }
    
    fun requiresManufacturer(): Boolean {
        return TODO("Check if product category requires manufacturer information")
    }
    
    fun isPhysicalProductValid(): Boolean {
        return TODO("For physical products, validate dimensions and weight are provided")
    }
    
    fun isDigitalProductValid(): Boolean {
        return TODO("For digital products, validate no physical constraints")
    }
    
    fun getDiscountedPrice(discountPercent: Double): BigDecimal {
        return TODO("Calculate discounted price with validation")
    }
}

data class ProductDimensions(
    val length: Double, // in cm
    val width: Double,  // in cm
    val height: Double  // in cm
) {
    // TODO: Add dimension validation
    fun isValid(): Boolean {
        return TODO("Validate all dimensions are positive")
    }
    
    fun getVolume(): Double {
        return TODO("Calculate volume in cubic cm")
    }
    
    fun exceedsShippingLimits(maxLength: Double, maxWidth: Double, maxHeight: Double): Boolean {
        return TODO("Check if dimensions exceed shipping limits")
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