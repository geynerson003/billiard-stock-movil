package com.lealcode.inventariobillar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.OnBackPressedCallback
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.lealcode.inventariobillar.data.model.*
import com.lealcode.inventariobillar.ui.feature.dashboard.*
import com.lealcode.inventariobillar.ui.feature.expenses.*
import com.lealcode.inventariobillar.ui.feature.inventory.*
import com.lealcode.inventariobillar.ui.feature.main.MainViewModel
import com.lealcode.inventariobillar.ui.feature.reports.*
import com.lealcode.inventariobillar.ui.feature.sales.*
import com.lealcode.inventariobillar.ui.feature.tables.*
import com.lealcode.inventariobillar.ui.feature.clients.ClientsListScreen
import com.lealcode.inventariobillar.ui.feature.clients.ClientFormScreen
import com.lealcode.inventariobillar.ui.feature.clients.ClientViewModel
import com.lealcode.inventariobillar.ui.feature.clients.ClientDebtScreen
import com.lealcode.inventariobillar.ui.feature.clients.ClientDebtViewModel
import com.lealcode.inventariobillar.ui.feature.auth.LoginScreen
import com.lealcode.inventariobillar.ui.feature.auth.RegisterScreen
import com.lealcode.inventariobillar.ui.feature.auth.AuthViewModel
import com.lealcode.inventariobillar.ui.theme.InventarioBillarTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import com.lealcode.inventariobillar.ui.theme.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.outlined.EventSeat
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.MoneyOff
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.ui.graphics.vector.ImageVector
import com.lealcode.inventariobillar.ui.feature.auth.ForgotPasswordScreen

@AndroidEntryPoint
/**
 * Actividad principal que aloja toda la aplicacion Compose.
 *
 * Se encarga de configurar el ciclo de vida base, el manejo de la navegacion y la
 * integracion con Hilt.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setupBackHandler()

        setContent {
            InventarioBillarTheme {
                MainApp()
            }
        }
    }

    /**
     * Registra un callback de retroceso legacy para versiones anteriores a Android 13.
     */
    private fun setupBackHandler() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // El sistema maneja automáticamente el back button
        } else {
            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (supportFragmentManager.backStackEntryCount > 0) {
                        supportFragmentManager.popBackStack()
                    } else {
                        finish()
                    }
                }
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Punto de entrada Compose.
 *
 * Orquesta el flujo de autenticacion, la pantalla de arranque y el contenido principal
 * de la aplicacion.
 */
fun MainApp(
    viewModel: MainViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val isAppReady by viewModel.isAppReady.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    // Estado de autenticación reactivo
    val authState by authViewModel.currentUserId.collectAsState(initial = null)
    val isAuthenticated = authState != null
    
    // Enum para manejar las pantallas de autenticación
    var currentAuthScreen by remember { mutableStateOf(AuthScreen.LOGIN) }

    when {
        !isAppReady -> SplashScreen(isLoading = isLoading)
        !isAuthenticated -> {
            when (currentAuthScreen) {
                AuthScreen.LOGIN -> LoginScreen(
                    onLoginSuccess = { /* Automático */ },
                    onNavigateToRegister = { currentAuthScreen = AuthScreen.REGISTER },
                    onNavigateToForgotPassword = { currentAuthScreen = AuthScreen.FORGOT_PASSWORD }
                )
                AuthScreen.REGISTER -> RegisterScreen(
                    onRegisterSuccess = { /* Automático */ },
                    onNavigateToLogin = { currentAuthScreen = AuthScreen.LOGIN }
                )
                AuthScreen.FORGOT_PASSWORD -> ForgotPasswordScreen(
                    onNavigateBack = { currentAuthScreen = AuthScreen.LOGIN }
                )
            }
        }
        else -> MainContent(
            onLogout = { 
                authViewModel.logout()
            }
        )
    }
}

/**
 * Pantallas disponibles dentro del flujo de autenticacion.
 */
enum class AuthScreen {
    LOGIN, REGISTER, FORGOT_PASSWORD
}


@Composable
/**
 * Pantalla de carga mostrada mientras la aplicacion termina su inicializacion basica.
 */
fun SplashScreen(isLoading: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Billiard Stock",
                style = MaterialTheme.typography.headlineMedium
            )
            if (isLoading) {
                Text(
                    text = "Inicializando...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Contenedor raiz del area autenticada.
 *
 * Define la barra de navegacion inferior, el NavHost principal y las rutas de cada
 * modulo funcional.
 */
fun MainContent(onLogout: () -> Unit = {}) {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = true

    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
        )
        systemUiController.setNavigationBarColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
        )
    }

    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStack?.destination?.route

    var showSaleForm by remember { mutableStateOf(false) }
    var selectedReportForDetail by remember { mutableStateOf<ReportResult?>(null) }



    val items = listOf(
        NavBarItem("dashboard", Icons.Outlined.Home, "Inicio", AzulAcento),
        NavBarItem("inventory", Icons.Outlined.Inventory2, "Inventario", AzulAcento),
        NavBarItem("sales", Icons.Outlined.ShoppingCart, "Ventas", OrangeBright),
        NavBarItem("tables", Icons.Outlined.EventSeat, "Mesas", PurpleFun),
        NavBarItem("expenses", Icons.Outlined.MoneyOff, "Gastos", RojoAcento),
        NavBarItem("reports", Icons.Outlined.BarChart, "Reportes", VerdeAcento),
        NavBarItem("clients", Icons.Outlined.List, "Clientes", PurpleFun)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = FondoClaro,
        bottomBar = {
            ModernNavigationBar(
                items = items,
                selectedRoute = currentDestination,
                onItemSelected = { route ->
                    // Evitar navegación innecesaria si ya estamos en la misma ruta
                    if (currentDestination != route) {
                        navController.navigate(route) {
                            // Usar una estrategia más eficiente que popUpTo(0)
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier
                .fillMaxSize()
                .background(FondoClaro)
                .padding(innerPadding)
        ) {
            composable("dashboard") { 
                DashboardScreen(onLogout = onLogout) 
            }
            composable("inventory") {
                val inventoryViewModel: InventoryViewModel = hiltViewModel()
                InventoryListScreen(
                    onAddProduct = { navController.navigate("productForm/new") },
                    onEditProduct = { product ->
                        navController.navigate("productForm/${product.id}")
                    },
                    onDeleteProduct = { product ->
                        inventoryViewModel.deleteProduct(product.id)
                    },
                    onProductClick = { product ->
                        navController.navigate("productDetail/${product.id}")
                    }
                )
            }
            composable(
                route = "productDetail/{productId}",
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: ""
                val inventoryViewModel: InventoryViewModel = hiltViewModel()
                val products by inventoryViewModel.products.collectAsState()
                val product = products.find { it.id == productId } ?: products.firstOrNull()
                if (product != null) {
                    ProductDetailScreen(
                        product = product,
                        onEdit = { navController.navigate("productForm/${product.id}") },
                        onBack = { navController.popBackStack("inventory", false) }
                    )
                }
            }
            composable(
                route = "productForm/{productId}",
                arguments = listOf(navArgument("productId") {
                    type = NavType.StringType
                })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: "new"
                val inventoryViewModel: InventoryViewModel = hiltViewModel()
                val products by inventoryViewModel.products.collectAsState()
                val product = if (productId == "new") null else products.find { it.id == productId }

                ProductFormScreen(
                    initialProduct = product,
                    onSave = { newProduct ->
                        if (product == null) {
                            inventoryViewModel.addProduct(newProduct)
                        } else {
                            inventoryViewModel.updateProduct(newProduct)
                        }
                        navController.popBackStack("inventory", false)
                    },
                    onCancel = { navController.popBackStack("inventory", false) }
                )
            }
            composable("sales") {
                val salesViewModel: SalesViewModel = hiltViewModel()
                val inventoryViewModel: InventoryViewModel = hiltViewModel()
                val clientViewModel: ClientViewModel = hiltViewModel()
                val tablesViewModel: TablesViewModel = hiltViewModel()
                val products by inventoryViewModel.products.collectAsState()
                val clients by clientViewModel.clients.collectAsState()
                val tables by tablesViewModel.tables.collectAsState()
                var showSaleForm by remember { mutableStateOf(false) }
                
                // Usar remember para evitar recreaciones innecesarias
                SalesListScreen(
                    onAddSale = { showSaleForm = true },
                    onSaleClick = { sale ->
                        navController.navigate("saleDetail/${sale.id}")
                    },
                    clients = clients,
                    tables = tables
                )

                if (showSaleForm) {
                    SaleFormScreen(
                        products = products,
                        clients = clients,
                        tables = tables.map { it.name },
                        onSave = { sale ->
                            salesViewModel.addSale(sale)
                            showSaleForm = false
                        },
                        onCancel = { showSaleForm = false }
                    )
                }
            }
            composable(
                route = "saleDetail/{saleId}",
                arguments = listOf(navArgument("saleId") { type = NavType.StringType })
            ) { backStackEntry ->
                val saleId = backStackEntry.arguments?.getString("saleId") ?: "0"
                val salesViewModel: SalesViewModel = hiltViewModel()
                val inventoryViewModel: InventoryViewModel = hiltViewModel()
                val clientViewModel: ClientViewModel = hiltViewModel()
                val tablesViewModel: TablesViewModel = hiltViewModel()
                
                // Obtener la venta directamente del repositorio para evitar problemas con filtros
                val sale by salesViewModel.getSaleById(saleId).collectAsState(initial = null)
                val products by inventoryViewModel.products.collectAsState()
                val clients by clientViewModel.clients.collectAsState()
                val tables by tablesViewModel.tables.collectAsState()
                
                sale?.let { currentSale ->
                    SaleDetailScreen(
                        sale = currentSale,
                        onDelete = { 
                            salesViewModel.deleteSale(currentSale.id)
                            navController.popBackStack("sales", false) 
                        },
                        onBack = { navController.popBackStack("sales", false) },
                        products = products,
                        clients = clients,
                        tables = tables
                    )
                } ?: run {
                    // Mostrar pantalla de carga o error si la venta no se encuentra
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Venta no encontrada", color = Color.Gray)
                    }
                }
            }
            composable("tables") {
                val tablesViewModel: TablesViewModel = hiltViewModel()
                TablesListScreen(
                    onTableClick = { table ->
                        navController.navigate("tableForm/${table.id}")
                    },
                    onAddTable = { navController.navigate("tableForm/new") },
                    onStartGame = { tableId, sessionId ->
                        navController.navigate("game/$tableId/$sessionId")
                    },
                    onDeleteTable = { table ->
                        tablesViewModel.deleteTable(table.id)
                    }
                )
            }
            composable(
                route = "tableForm/{tableId}",
                arguments = listOf(navArgument("tableId") { type = NavType.StringType })
            ) { backStackEntry ->
                val tableId = backStackEntry.arguments?.getString("tableId") ?: "new"
                val tablesViewModel: TablesViewModel = hiltViewModel()
                val tables by tablesViewModel.tables.collectAsState()
                val table = if (tableId == "new") null else tables.find { it.id == tableId }

                TableFormScreen(
                    initialTable = table,
                    onSave = { newTable ->
                        if (table == null) {
                            tablesViewModel.addTable(newTable)
                        } else {
                            tablesViewModel.updateTable(newTable)
                        }
                        navController.popBackStack("tables", false)
                    },
                    onCancel = { navController.popBackStack("tables", false) }
                )
            }
            composable(
                route = "tableSession/{tableId}",
                arguments = listOf(navArgument("tableId") { type = NavType.StringType })
            ) { backStackEntry ->
                val tableId = backStackEntry.arguments?.getString("tableId") ?: "0"
                val table = Table(tableId, "Mesa $tableId", 0.0, null)
                val session = TableSession(tableId, "Session for Table $tableId")

                TableSessionScreen(
                    session = session,
                    table = table,
                    onEndSession = { navController.popBackStack("tables", false) },
                    onStartGame = { 
                        navController.navigate("game/$tableId/${session.id}")
                    },
                    onBack = { navController.popBackStack("tables", false) }
                )
            }
            composable(
                route = "game/{tableId}/{sessionId}",
                arguments = listOf(
                    navArgument("tableId") { type = NavType.StringType },
                    navArgument("sessionId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val tableId = backStackEntry.arguments?.getString("tableId") ?: ""
                val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
                
                GameScreen(
                    tableId = tableId,
                    sessionId = sessionId,
                    onBack = { navController.popBackStack("tables", false) }
                )
            }
            composable("expenses") {
                val expensesViewModel: ExpensesViewModel = hiltViewModel()
                ExpensesListScreen(
                    onAddExpense = { navController.navigate("expenseForm/new") },
                    onExpenseClick = { expense ->
                        navController.navigate("expenseForm/${expense.id}")
                    },
                    onDeleteExpense = { expense ->
                        expensesViewModel.deleteExpense(expense.id)
                    }
                )
            }
            composable(
                route = "expenseForm/{expenseId}",
                arguments = listOf(navArgument("expenseId") {
                    type = NavType.StringType
                })
            ) { backStackEntry ->
                val expenseId = backStackEntry.arguments?.getString("expenseId") ?: "new"
                val expensesViewModel: ExpensesViewModel = hiltViewModel()
                val expenses by expensesViewModel.expenses.collectAsState()
                val expense = if (expenseId == "new") null else expenses.find { it.id == expenseId }

                ExpenseFormScreen(
                    initialExpense = expense,
                    onSave = { newExpense ->
                        if (expense == null) {
                            expensesViewModel.addExpense(newExpense)
                        } else {
                            expensesViewModel.updateExpense(newExpense)
                        }
                        navController.popBackStack("expenses", false)
                    },
                    onCancel = { navController.popBackStack("expenses", false) }
                )
            }

            composable("reports") {
                ReportsScreen(
                    onReportDetail = { report -> selectedReportForDetail = report }
                )
                selectedReportForDetail?.let { report ->
                    ReportDetailScreen(
                        report = report,
                        onBack = { selectedReportForDetail = null }
                    )
                }
            }
            composable("clients") {
                val clientViewModel: ClientViewModel = hiltViewModel()
                val salesViewModel: SalesViewModel = hiltViewModel()
                val sales by salesViewModel.filteredSales.collectAsState()
                
                // Calcular deudas con validación null-safe y optimización de rendimiento
                val clientDebts by remember(sales) {
                    derivedStateOf {
                        try {
                            // Por ahora mantener la lógica anterior hasta que se implemente completamente
                            sales.filter { !it.isPaid && it.clientId.isNotBlank() }
                                .groupBy { it.clientId }
                                .mapValues { entry -> 
                                    entry.value.sumOf { sale ->
                                        // Usar totalAmount si es nueva estructura, sino usar price
                                        if (sale.items.isNotEmpty()) sale.totalAmount else sale.price
                                    }
                                }
                        } catch (e: Exception) {
                            emptyMap()
                        }
                    }
                }
                
                val filteredClients by clientViewModel.filteredClients.collectAsState()
                val searchQuery by clientViewModel.searchQuery.collectAsState()
                
                ClientsListScreen(
                    clients = filteredClients,
                    clientDebts = clientDebts,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { query -> clientViewModel.updateSearchQuery(query) },
                    onAddClient = { navController.navigate("clientForm/new") },
                    onEditClient = { client -> navController.navigate("clientForm/${client.id}") },
                    onDeleteClient = { client -> clientViewModel.deleteClient(client.id) },
                    onClientClick = { client -> navController.navigate("clientDebt/${client.id}") }
                )
            }
            composable(
                route = "clientForm/new"
            ) {
                val clientViewModel: ClientViewModel = hiltViewModel()
                ClientFormScreen(
                    onSave = { client ->
                        clientViewModel.addClient(client)
                        navController.popBackStack("clients", false)
                    },
                    onCancel = { navController.popBackStack("clients", false) }
                )
            }
            composable(
                route = "clientForm/{clientId}",
                arguments = listOf(navArgument("clientId") { type = NavType.StringType })
            ) { backStackEntry ->
                val clientId = backStackEntry.arguments?.getString("clientId") ?: ""
                val clientViewModel: ClientViewModel = hiltViewModel()
                val clients by clientViewModel.clients.collectAsState()
                val client = clients.find { it.id == clientId }
                ClientFormScreen(
                    initialClient = client,
                    onSave = { updatedClient ->
                        clientViewModel.updateClient(updatedClient)
                        navController.popBackStack("clients", false)
                    },
                    onCancel = { navController.popBackStack("clients", false) }
                )
            }
            composable(
                route = "clientDebt/{clientId}",
                arguments = listOf(navArgument("clientId") { type = NavType.StringType })
            ) { backStackEntry ->
                val clientId = backStackEntry.arguments?.getString("clientId") ?: ""
                val clientViewModel: ClientViewModel = hiltViewModel()
                val clientDebtViewModel: ClientDebtViewModel = hiltViewModel()
                val tablesViewModel: TablesViewModel = hiltViewModel()
                val clients by clientViewModel.clients.collectAsState()
                val tables by tablesViewModel.tables.collectAsState()
                val client = clients.find { it.id == clientId }
                
                client?.let { currentClient ->
                    LaunchedEffect(currentClient) {
                        clientDebtViewModel.setClient(currentClient)
                    }
                    
                    val pendingSales by clientDebtViewModel.pendingSales.collectAsState()
                    
                    val debtInfo by clientDebtViewModel.clientDebtInfo.collectAsState()
                    val payments by clientDebtViewModel.payments.collectAsState()
                    
                    ClientDebtScreen(
                        client = currentClient,
                        pendingSales = pendingSales,
                        debtInfo = debtInfo,
                        payments = payments,
                        tables = tables,
                        onBack = { navController.popBackStack("clients", false) },
                        onPayDebt = { paymentAmount, description, notes ->
                            clientDebtViewModel.payDebt(paymentAmount, description, notes)
                        }
                    )
                }
            }
        }
    }
}

// Modelo para cada ítem del NavigationBar
/**
 * Representa un elemento navegable dentro de la barra inferior.
 */
data class NavBarItem(
    val route: String,
    val icon: ImageVector,
    val label: String,
    val accent: Color
)

@Composable
/**
 * Barra de navegacion inferior personalizada de la app.
 */
fun ModernNavigationBar(
    items: List<NavBarItem>,
    selectedRoute: String?,
    onItemSelected: (String) -> Unit
) {
    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 16.dp,
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .navigationBarsPadding()
            .fillMaxWidth()
            .height(76.dp)
    ) {
        Row(
            Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = selectedRoute == item.route
                val transition = updateTransition(selected, label = item.route)
                val iconColor by transition.animateColor(label = "iconColor") {
                    if (it) item.accent else Color(0xFFB0B0B0)
                }
                val scale by transition.animateFloat(label = "scale") {
                    if (it) 1.25f else 1f
                }
                val indicatorAlpha by transition.animateFloat(label = "indicatorAlpha") {
                    if (it) 1f else 0f
                }
                Box(
                    Modifier
                        .weight(1f)
                        .clickable(
                            onClick = { onItemSelected(item.route) },
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                item.icon,
                                contentDescription = item.label,
                                tint = iconColor,
                                modifier = Modifier
                                    .size(30.dp)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    }
                            )
                            // Indicador animado
                            Box(
                                Modifier
                                    .align(Alignment.BottomCenter)
                                    .offset(y = 22.dp)
                                    .size(18.dp, 6.dp)
                                    .graphicsLayer { alpha = indicatorAlpha }
                                    .background(
                                        color = item.accent,
                                        shape = RoundedCornerShape(3.dp)
                                    )
                            )
                        }
                        AnimatedVisibility(visible = selected) {
                            Text(
                                item.label,
                                color = item.accent,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
/**
 * Vista previa del contenedor principal con el tema de la aplicacion.
 */
fun DefaultPreview() {
    InventarioBillarTheme {
        MainApp()
    }
}
