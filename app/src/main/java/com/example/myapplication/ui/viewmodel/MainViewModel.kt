package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.Product
import com.example.myapplication.model.Transaction
import com.example.myapplication.model.TransactionType
import com.example.myapplication.repository.InventoryAccountingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class FinancialSummary(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netBalance: Double = 0.0,
    val inventoryValue: Double = 0.0,
    val inventoryCostValue: Double = 0.0,
    val totalProductsCount: Int = 0,
    val lowStockCount: Int = 0
)

class MainViewModel(
    private val repository: InventoryAccountingRepository = InventoryAccountingRepository()
) : ViewModel() {

    val products: StateFlow<List<Product>> = repository.products
    val transactions: StateFlow<List<Transaction>> = repository.transactions

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Todas")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val filteredProducts: StateFlow<List<Product>> = combine(
        products,
        searchQuery,
        selectedCategory
    ) { productList, query, category ->
        productList.filter { product ->
            (category == "Todas" || product.category.equals(category, ignoreCase = true)) &&
                    (query.isEmpty() || product.name.contains(query, ignoreCase = true) || product.sku.contains(query, ignoreCase = true))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val summary: StateFlow<FinancialSummary> = combine(
        products,
        transactions
    ) { productList, transactionList ->
        val income = transactionList.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = transactionList.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val invValue = productList.sumOf { it.totalValue }
        val invCost = productList.sumOf { it.totalCostValue }
        val lowStock = productList.count { it.isLowStock }

        FinancialSummary(
            totalIncome = income,
            totalExpense = expense,
            netBalance = income - expense,
            inventoryValue = invValue,
            inventoryCostValue = invCost,
            totalProductsCount = productList.size,
            lowStockCount = lowStock
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummary())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun addProduct(product: Product) {
        repository.addProduct(product)
    }

    fun updateProduct(product: Product) {
        repository.updateProduct(product)
    }

    fun deleteProduct(productId: String) {
        repository.deleteProduct(productId)
    }

    fun adjustStock(productId: String, delta: Int, recordTransaction: Boolean = true) {
        repository.adjustStock(productId, delta, recordTransaction)
    }

    fun addTransaction(transaction: Transaction) {
        repository.addTransaction(transaction)
    }

    fun deleteTransaction(transactionId: String) {
        repository.deleteTransaction(transactionId)
    }
}
