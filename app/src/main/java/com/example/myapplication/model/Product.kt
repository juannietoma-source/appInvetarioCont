package com.example.myapplication.model

data class Product(
    val id: String,
    val name: String,
    val sku: String,
    val category: String,
    val stockQuantity: Int,
    val minStockQuantity: Int = 5,
    val purchasePrice: Double,
    val sellingPrice: Double,
    val description: String = ""
) {
    val isLowStock: Boolean
        get() = stockQuantity <= minStockQuantity

    val totalValue: Double
        get() = stockQuantity * sellingPrice

    val totalCostValue: Double
        get() = stockQuantity * purchasePrice
}
