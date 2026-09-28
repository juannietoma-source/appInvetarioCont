package com.example.myapplication.model

data class Transaction(
    val id: String,
    val type: TransactionType,
    val title: String,
    val amount: Double,
    val category: String,
    val dateMillis: Long = System.currentTimeMillis(),
    val productId: String? = null,
    val quantity: Int = 0,
    val notes: String = ""
)
