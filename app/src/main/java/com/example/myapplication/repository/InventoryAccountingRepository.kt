package com.example.myapplication.repository

import com.example.myapplication.model.Product
import com.example.myapplication.model.Transaction
import com.example.myapplication.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class InventoryAccountingRepository {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    init {
        // Sample initial data for immediate demo and usage
        val initialProducts = listOf(
            Product(
                id = UUID.randomUUID().toString(),
                name = "Laptop Gamer 15\"",
                sku = "TECH-001",
                category = "Electrónica",
                stockQuantity = 8,
                minStockQuantity = 3,
                purchasePrice = 750.0,
                sellingPrice = 999.99,
                description = "Procesador i7, 16GB RAM, SSD 512GB"
            ),
            Product(
                id = UUID.randomUUID().toString(),
                name = "Smartphone 5G",
                sku = "TECH-002",
                category = "Electrónica",
                stockQuantity = 3,
                minStockQuantity = 5, // Triggers low stock alert
                purchasePrice = 300.0,
                sellingPrice = 450.00,
                description = "Pantalla OLED 120Hz"
            ),
            Product(
                id = UUID.randomUUID().toString(),
                name = "Cuaderno Universitario 100 Hojas",
                sku = "PAP-010",
                category = "Papelería",
                stockQuantity = 45,
                minStockQuantity = 10,
                purchasePrice = 1.20,
                sellingPrice = 2.50,
                description = "Pasta dura, cuadriculado"
            ),
            Product(
                id = UUID.randomUUID().toString(),
                name = "Camiseta Tipo Polo",
                sku = "ROP-005",
                category = "Ropa y Calzado",
                stockQuantity = 12,
                minStockQuantity = 5,
                purchasePrice = 8.0,
                sellingPrice = 18.00,
                description = "100% Algodón, Talla M"
            ),
            Product(
                id = UUID.randomUUID().toString(),
                name = "Café Orgánico 500g",
                sku = "ALI-001",
                category = "Alimentos y Bebidas",
                stockQuantity = 2,
                minStockQuantity = 5, // Triggers low stock alert
                purchasePrice = 4.50,
                sellingPrice = 9.00,
                description = "Café molido origen Colombia"
            )
        )

        val now = System.currentTimeMillis()
        val day = 86400000L

        val initialTransactions = listOf(
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.INCOME,
                title = "Venta Laptop Gamer 15\"",
                amount = 999.99,
                category = "Electrónica",
                dateMillis = now - (day * 1),
                notes = "Cliente pagó en efectivo"
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.INCOME,
                title = "Venta Lote de Cuadernos (x10)",
                amount = 25.00,
                category = "Papelería",
                dateMillis = now - (day * 2),
                notes = "Venta rápida"
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.EXPENSE,
                title = "Pago de Servicios de Local",
                amount = 120.00,
                category = "Servicios",
                dateMillis = now - (day * 3),
                notes = "Luz y Agua del mes"
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.EXPENSE,
                title = "Compra de Inventario Café Orgánico",
                amount = 45.00,
                category = "Alimentos y Bebidas",
                dateMillis = now - (day * 4),
                notes = "Proveedor local"
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                type = TransactionType.INCOME,
                title = "Venta Camiseta Polo",
                amount = 18.00,
                category = "Ropa y Calzado",
                dateMillis = now - (day * 5)
            )
        )

        _products.value = initialProducts
        _transactions.value = initialTransactions
    }

    fun addProduct(product: Product) {
        _products.update { it + product }
    }

    fun updateProduct(updatedProduct: Product) {
        _products.update { list ->
            list.map { if (it.id == updatedProduct.id) updatedProduct else it }
        }
    }

    fun deleteProduct(productId: String) {
        _products.update { list ->
            list.filterNot { it.id == productId }
        }
    }

    fun adjustStock(productId: String, delta: Int, recordTransaction: Boolean = true) {
        val product = _products.value.find { it.id == productId } ?: return
        val newStock = (product.stockQuantity + delta).coerceAtLeast(0)
        val updatedProduct = product.copy(stockQuantity = newStock)

        updateProduct(updatedProduct)

        if (recordTransaction && delta != 0) {
            val isSale = delta < 0
            val quantity = kotlin.math.abs(delta)
            val amount = if (isSale) product.sellingPrice * quantity else product.purchasePrice * quantity
            val type = if (isSale) TransactionType.INCOME else TransactionType.EXPENSE
            val title = if (isSale) "Venta: ${product.name} (x$quantity)" else "Reabastecimiento: ${product.name} (x$quantity)"

            addTransaction(
                Transaction(
                    id = UUID.randomUUID().toString(),
                    type = type,
                    title = title,
                    amount = amount,
                    category = product.category,
                    productId = product.id,
                    quantity = quantity,
                    notes = if (isSale) "Venta directa de inventario" else "Aumento de stock en inventario"
                )
            )
        }
    }

    fun addTransaction(transaction: Transaction) {
        _transactions.update { listOf(transaction) + it }
    }

    fun deleteTransaction(transactionId: String) {
        _transactions.update { list ->
            list.filterNot { it.id == transactionId }
        }
    }
}
