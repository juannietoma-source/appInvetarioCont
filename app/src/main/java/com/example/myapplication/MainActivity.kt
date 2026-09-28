package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.model.Product
import com.example.myapplication.model.Transaction
import com.example.myapplication.model.TransactionType
import com.example.myapplication.ui.components.AddProductDialog
import com.example.myapplication.ui.components.AddTransactionDialog
import com.example.myapplication.ui.screens.DashboardScreen
import com.example.myapplication.ui.screens.InventoryScreen
import com.example.myapplication.ui.screens.TransactionsScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.viewmodel.FinancialSummary
import com.example.myapplication.ui.viewmodel.MainViewModel

enum class NavigationTab(
    val title: String,
    val icon: ImageVector
) {
    DASHBOARD("Inicio", Icons.Default.Dashboard),
    INVENTORY("Inventario", Icons.Default.Inventory2),
    TRANSACTIONS("Contabilidad", Icons.Default.AccountBalanceWallet)
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainApp(viewModel: MainViewModel) {
    val summary by viewModel.summary.collectAsState()
    val products by viewModel.products.collectAsState()
    val filteredProducts by viewModel.filteredProducts.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    MainAppContent(
        summary = summary,
        products = products,
        filteredProducts = filteredProducts,
        transactions = transactions,
        searchQuery = searchQuery,
        selectedCategory = selectedCategory,
        onSearchQueryChange = { viewModel.setSearchQuery(it) },
        onCategorySelect = { viewModel.setSelectedCategory(it) },
        onStockChange = { productId, delta ->
            viewModel.adjustStock(productId = productId, delta = delta, recordTransaction = true)
        },
        onAddProduct = { viewModel.addProduct(it) },
        onUpdateProduct = { viewModel.updateProduct(it) },
        onDeleteProduct = { viewModel.deleteProduct(it) },
        onAddTransaction = { viewModel.addTransaction(it) },
        onDeleteTransaction = { viewModel.deleteTransaction(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(
    summary: FinancialSummary,
    products: List<Product>,
    filteredProducts: List<Product>,
    transactions: List<Transaction>,
    searchQuery: String,
    selectedCategory: String,
    onSearchQueryChange: (String) -> Unit = {},
    onCategorySelect: (String) -> Unit = {},
    onStockChange: (productId: String, delta: Int) -> Unit = { _, _ -> },
    onAddProduct: (Product) -> Unit = {},
    onUpdateProduct: (Product) -> Unit = {},
    onDeleteProduct: (String) -> Unit = {},
    onAddTransaction: (Transaction) -> Unit = {},
    onDeleteTransaction: (String) -> Unit = {}
) {
    var selectedTabItem by remember { mutableIntStateOf(0) }
    val tabs = NavigationTab.entries

    var showAddProductDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var showAddTransactionDialog by remember { mutableStateOf(false) }

    val lowStockProducts = remember(products) {
        products.filter { it.isLowStock }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Inventario & Contabilidad",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTabItem == index,
                        onClick = { selectedTabItem = index },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        when (tabs[selectedTabItem]) {
            NavigationTab.DASHBOARD -> {
                DashboardScreen(
                    summary = summary,
                    lowStockProducts = lowStockProducts,
                    recentTransactions = transactions,
                    onRestockProduct = { productId ->
                        onStockChange(productId, 1)
                    },
                    onNavigateToInventory = { selectedTabItem = 1 },
                    onNavigateToTransactions = { selectedTabItem = 2 },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            NavigationTab.INVENTORY -> {
                InventoryScreen(
                    products = filteredProducts,
                    searchQuery = searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    selectedCategory = selectedCategory,
                    onCategorySelect = onCategorySelect,
                    onStockChange = onStockChange,
                    onEditProduct = { product ->
                        editingProduct = product
                        showAddProductDialog = true
                    },
                    onDeleteProduct = onDeleteProduct,
                    onAddProductClick = {
                        editingProduct = null
                        showAddProductDialog = true
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            NavigationTab.TRANSACTIONS -> {
                TransactionsScreen(
                    transactions = transactions,
                    onDeleteTransaction = onDeleteTransaction,
                    onAddTransactionClick = {
                        showAddTransactionDialog = true
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        // Dialogs
        if (showAddProductDialog) {
            AddProductDialog(
                initialProduct = editingProduct,
                onDismiss = {
                    showAddProductDialog = false
                    editingProduct = null
                },
                onConfirm = { product ->
                    if (editingProduct != null) {
                        onUpdateProduct(product)
                    } else {
                        onAddProduct(product)
                    }
                    showAddProductDialog = false
                    editingProduct = null
                }
            )
        }

        if (showAddTransactionDialog) {
            AddTransactionDialog(
                onDismiss = { showAddTransactionDialog = false },
                onConfirm = { transaction ->
                    onAddTransaction(transaction)
                    showAddTransactionDialog = false
                }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainAppPreview() {
    val sampleProducts = listOf(
        Product(
            id = "1",
            name = "Laptop Gamer 15\"",
            sku = "TECH-001",
            category = "Electrónica",
            stockQuantity = 8,
            minStockQuantity = 3,
            purchasePrice = 750.0,
            sellingPrice = 999.99,
            description = "Procesador i7, 16GB RAM"
        ),
        Product(
            id = "2",
            name = "Smartphone 5G",
            sku = "TECH-002",
            category = "Electrónica",
            stockQuantity = 3,
            minStockQuantity = 5,
            purchasePrice = 300.0,
            sellingPrice = 450.0,
            description = "Pantalla OLED 120Hz"
        )
    )

    val sampleTransactions = listOf(
        Transaction(
            id = "1",
            type = TransactionType.INCOME,
            title = "Venta Laptop Gamer 15\"",
            amount = 999.99,
            category = "Electrónica"
        ),
        Transaction(
            id = "2",
            type = TransactionType.EXPENSE,
            title = "Pago de Servicios",
            amount = 120.00,
            category = "Servicios"
        )
    )

    val sampleSummary = FinancialSummary(
        totalIncome = 999.99,
        totalExpense = 120.00,
        netBalance = 879.99,
        inventoryValue = 9349.92,
        inventoryCostValue = 6900.00,
        totalProductsCount = 2,
        lowStockCount = 1
    )

    MyApplicationTheme {
        MainAppContent(
            summary = sampleSummary,
            products = sampleProducts,
            filteredProducts = sampleProducts,
            transactions = sampleTransactions,
            searchQuery = "",
            selectedCategory = "Todas"
        )
    }
}
