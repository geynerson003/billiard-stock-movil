package com.lealcode.inventariobillar.ui.feature.sales

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.lealcode.inventariobillar.data.model.Sale
import com.lealcode.inventariobillar.data.model.SaleType
import com.lealcode.inventariobillar.data.model.DateFilterType
import com.lealcode.inventariobillar.data.model.Client
import com.lealcode.inventariobillar.data.model.Table
import com.lealcode.inventariobillar.ui.theme.*
import androidx.compose.foundation.border
import androidx.compose.material.icons.outlined.*
import com.lealcode.inventariobillar.util.MathUtils
import androidx.compose.runtime.LaunchedEffect
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Pantalla principal del modulo de ventas.
 */
fun SalesListScreen(
    onAddSale: () -> Unit,
    onSaleClick: (Sale) -> Unit,
    viewModel: SalesViewModel = hiltViewModel(),
    clients: List<Client> = emptyList(),
    tables: List<Table> = emptyList()
) {
    var filterType by remember { mutableStateOf<SaleType?>(null) }
    val sales by viewModel.filteredSales.collectAsState()
    val dateFilterType by viewModel.currentDateFilterType.collectAsState()
    val customStartDate by viewModel.currentCustomStartDate.collectAsState()
    val customEndDate by viewModel.currentCustomEndDate.collectAsState()
    var animateList by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }
    var showDateFilterMenu by remember { mutableStateOf(false) }
    var showCustomDateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(sales) { animateList = true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Ventas",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = OrangeBright
                        )
                    )
                },
                actions = {
                    // Icono de filtro de fecha
                    Box {
                        IconButton(
                            onClick = { showDateFilterMenu = true }
                        ) {
                            Icon(
                                imageVector = when (dateFilterType) {
                                    DateFilterType.TODAY -> Icons.Outlined.Today
                                    DateFilterType.WEEK -> Icons.Outlined.DateRange
                                    DateFilterType.MONTH -> Icons.Outlined.CalendarMonth
                                    DateFilterType.CUSTOM -> Icons.Outlined.EditCalendar
                                },
                                contentDescription = "Filtrar por fecha",
                                tint = if (dateFilterType != DateFilterType.TODAY) OrangeBright else GrisTextoSecundario,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        // Indicador de filtro de fecha activo
                        if (dateFilterType != DateFilterType.TODAY) {
                            Badge(
                                containerColor = OrangeBright,
                                contentColor = Color.White,
                                modifier = Modifier.offset(x = 8.dp, y = (-8).dp)
                            ) {
                                Text(
                                    "1",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        // Menú desplegable de filtros de fecha
                        DropdownMenu(
                            expanded = showDateFilterMenu,
                            onDismissRequest = { showDateFilterMenu = false },
                            modifier = Modifier.background(FondoTarjeta)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Outlined.Today,
                                            contentDescription = null,
                                            tint = AzulAcento,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            "Hoy",
                                            fontWeight = if (dateFilterType == DateFilterType.TODAY) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.setDateFilter(DateFilterType.TODAY)
                                    showDateFilterMenu = false
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = if (dateFilterType == DateFilterType.TODAY) AzulAcento else Color.Black
                                )
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Outlined.DateRange,
                                            contentDescription = null,
                                            tint = OrangeBright,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            "Esta semana",
                                            fontWeight = if (dateFilterType == DateFilterType.WEEK) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.setDateFilter(DateFilterType.WEEK)
                                    showDateFilterMenu = false
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = if (dateFilterType == DateFilterType.WEEK) OrangeBright else Color.Black
                                )
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Outlined.CalendarMonth,
                                            contentDescription = null,
                                            tint = VerdeAcento,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            "Este mes",
                                            fontWeight = if (dateFilterType == DateFilterType.MONTH) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.setDateFilter(DateFilterType.MONTH)
                                    showDateFilterMenu = false
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = if (dateFilterType == DateFilterType.MONTH) VerdeAcento else Color.Black
                                )
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Outlined.EditCalendar,
                                            contentDescription = null,
                                            tint = RojoAcento,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            "Personalizado",
                                            fontWeight = if (dateFilterType == DateFilterType.CUSTOM) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                },
                                onClick = {
                                    showCustomDateDialog = true
                                    showDateFilterMenu = false
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = if (dateFilterType == DateFilterType.CUSTOM) RojoAcento else Color.Black
                                )
                            )
                        }
                    }
                    
                    Spacer(Modifier.width(8.dp))
                    
                    // Icono de filtro de tipo de venta
                    Box {
                        IconButton(
                            onClick = { showFilterMenu = true }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FilterList,
                                contentDescription = "Filtrar ventas",
                                tint = if (filterType != null) OrangeBright else GrisTextoSecundario,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        // Indicador de filtro activo
                        if (filterType != null) {
                            Badge(
                                containerColor = when (filterType) {
                                    SaleType.TABLE -> OrangeBright
                                    SaleType.EXTERNAL -> VerdeAcento
                                    else -> AzulAcento
                                },
                                contentColor = Color.White,
                                modifier = Modifier.offset(x = 8.dp, y = (-8).dp)
                            ) {
                                Text(
                                    "1",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        // Menú desplegable de filtros
                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false },
                            modifier = Modifier.background(FondoTarjeta)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Outlined.PointOfSale,
                                            contentDescription = null,
                                            tint = AzulAcento,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            "Todas las ventas",
                                            fontWeight = if (filterType == null) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                },
                                onClick = {
                                    filterType = null
                                    viewModel.setFilter(null, null)
                                    showFilterMenu = false
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = if (filterType == null) AzulAcento else Color.Black
                                )
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Outlined.TableBar,
                                            contentDescription = null,
                                            tint = OrangeBright,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            "Solo ventas de mesa",
                                            fontWeight = if (filterType == SaleType.TABLE) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                },
                                onClick = {
                                    filterType = SaleType.TABLE
                                    viewModel.setFilter(SaleType.TABLE, null)
                                    showFilterMenu = false
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = if (filterType == SaleType.TABLE) OrangeBright else Color.Black
                                )
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Outlined.LocalMall,
                                            contentDescription = null,
                                            tint = VerdeAcento,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            "Solo ventas externas",
                                            fontWeight = if (filterType == SaleType.EXTERNAL) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                },
                                onClick = {
                                    filterType = SaleType.EXTERNAL
                                    viewModel.setFilter(SaleType.EXTERNAL, null)
                                    showFilterMenu = false
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = if (filterType == SaleType.EXTERNAL) VerdeAcento else Color.Black
                                )
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FondoTarjeta
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddSale,
                containerColor = OrangeBright,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(12.dp),
                modifier = Modifier.shadow(12.dp, CircleShape)
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Agregar venta", modifier = Modifier.size(32.dp))
            }
        }
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().background(FondoClaro).padding(innerPadding)) {
            // Indicador del filtro activo (más compacto)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (dateFilterType) {
                        DateFilterType.TODAY -> "Mostrando ventas de hoy"
                        DateFilterType.WEEK -> "Mostrando ventas de esta semana"
                        DateFilterType.MONTH -> "Mostrando ventas de este mes"
                        DateFilterType.CUSTOM -> "Mostrando ventas personalizadas"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = GrisTextoSecundario
                    )
                )
                
                if (filterType != null) {
                    Badge(
                        containerColor = when (filterType) {
                            SaleType.TABLE -> OrangeBright
                            SaleType.EXTERNAL -> VerdeAcento
                            else -> AzulAcento
                        },
                        contentColor = Color.White
                    ) {
                        Text(
                            when (filterType) {
                                SaleType.TABLE -> "Solo Mesa"
                                SaleType.EXTERNAL -> "Solo Externa"
                                else -> ""
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            AnimatedVisibility(
                visible = sales.isEmpty(),
                enter = fadeIn(tween(600)) + scaleIn(tween(600)),
                exit = fadeOut() + scaleOut()
            ) {
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Outlined.PointOfSale, contentDescription = null, tint = OrangeBright, modifier = Modifier.size(64.dp))
                    Text("No hay ventas registradas", color = OrangeBright, fontWeight = FontWeight.Bold)
                }
            }
            AnimatedVisibility(
                visible = sales.isNotEmpty() && animateList,
                enter = fadeIn(tween(700)) + scaleIn(tween(700)),
                exit = fadeOut() + scaleOut()
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(sales, key = { it.id }) { sale ->
                        val animScale = animateFloatAsState(
                            targetValue = 1f,
                            animationSpec = tween(600), label = "cardScale"
                        )
                        SalesCard(
                            sale = sale,
                            clients = clients,
                            tables = tables,
                            modifier = Modifier
                                .graphicsLayer(scaleX = animScale.value, scaleY = animScale.value)
                                .clickable { onSaleClick(sale) }
                        )
                    }
                }
            }
        }
    }
    
    // Diálogo para fechas personalizadas
    if (showCustomDateDialog) {
        CustomDateRangeDialog(
            onDismiss = { showCustomDateDialog = false },
            onDateRangeSelected = { startDate, endDate ->
                viewModel.setDateFilter(DateFilterType.CUSTOM, startDate, endDate)
                showCustomDateDialog = false
            }
        )
    }
}

private data class SaleCardStyle(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconColor: Color,
    val badgeColor: Color,
    val badgeText: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SalesCard(
    sale: Sale,
    clients: List<Client> = emptyList(),
    tables: List<Table> = emptyList(),
    modifier: Modifier = Modifier
) {
    val client = clients.find { it.id == sale.clientId }
    val style = when (sale.type) {
        SaleType.TABLE -> SaleCardStyle(
            icon = Icons.Outlined.TableBar,
            iconColor = OrangeBright,
            badgeColor = OrangeBright,
            badgeText = "Mesa"
        )
        SaleType.EXTERNAL -> SaleCardStyle(
            icon = Icons.Outlined.LocalMall,
            iconColor = VerdeAcento,
            badgeColor = VerdeAcento,
            badgeText = "Externa"
        )
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp)
            .shadow(8.dp, shape = MaterialTheme.shapes.large)
            .border(1.dp, style.iconColor.copy(alpha = 0.15f), MaterialTheme.shapes.large),
        colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono principal
            Box(
                Modifier
                    .size(44.dp)
                    .background(style.iconColor.copy(alpha = 0.12f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = style.icon,
                    contentDescription = null,
                    tint = style.iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(Modifier.width(12.dp))
            
            // Información principal
            Column(Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Título y badges
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (sale.items.isNotEmpty()) {
                                "${sale.items.size} productos"
                            } else {
                                if (sale.isGameSale) "Partida" else "Venta"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold, 
                                color = Color.Black
                            )
                        )
                        
                        Spacer(Modifier.width(6.dp))
                        
                        // Badge tipo de venta
                        Badge(
                            containerColor = style.badgeColor,
                            contentColor = Color.White
                        ) {
                            Text(style.badgeText, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        // Badge estado de pago
                        Spacer(Modifier.width(4.dp))
                        Badge(
                            containerColor = if (sale.isPaid) VerdeAcento else RojoAcento,
                            contentColor = Color.White
                        ) {
                            Text(
                                if (sale.isPaid) "Pagado" else "Pendiente",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // Badge para ventas de partidas
                        if (sale.isGameSale) {
                            Spacer(Modifier.width(4.dp))
                            Badge(
                                containerColor = Color.Blue,
                                contentColor = Color.White
                            ) {
                                Text(
                                    "Partida",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    // Total de la venta
                    Text(
                        MathUtils.formatPrice(sale.totalAmount),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = style.iconColor
                        )
                    )
                }
                
                Spacer(Modifier.height(4.dp))
                
                // Información secundaria (solo la más importante)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Información básica
                    Column {
                        if (sale.tableId != null) {
                            // Buscar el nombre de la mesa en lugar del ID
                            val tableName = tables.find { it.id == sale.tableId }?.name ?: "Mesa ${sale.tableId}"
                            Text(
                                tableName,
                                color = GrisTextoSecundario,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        if (sale.clientId.isNotEmpty() && client != null) {
                            Text(
                                client.nombre,
                                color = GrisTextoSecundario,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Fecha (formato dd/MM/yyyy)
                    val fechaFormateada = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date(sale.date))
                    Text(
                        fechaFormateada,
                        color = GrisTextoSecundario,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
} 

@Composable
private fun CustomDateRangeDialog(
    onDismiss: () -> Unit,
    onDateRangeSelected: (Long, Long) -> Unit
) {
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    var startDateText by remember { mutableStateOf("") }
    var endDateText by remember { mutableStateOf("") }
    var showStartDateError by remember { mutableStateOf(false) }
    var showEndDateError by remember { mutableStateOf(false) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Título
                Text(
                    text = "Seleccionar rango de fechas",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = OrangeBright
                    )
                )
                
                // Información de ayuda
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AzulAcento.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "💡 Formato de fecha: DD/MM/YYYY",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AzulAcento
                            )
                        )
                        Text(
                            text = "Ejemplo: 15/12/2024",
                            style = MaterialTheme.typography.bodySmall,
                            color = AzulAcento
                        )
                    }
                }
                
                // Campo de fecha de inicio
                Column {
                    Text(
                        text = "Fecha de inicio:",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = startDateText,
                        onValueChange = { 
                            startDateText = it
                            showStartDateError = false
                            startDate = null
                        },
                        label = { Text("DD/MM/YYYY") },
                        placeholder = { Text("Ej: 01/12/2024") },
                        isError = showStartDateError,
                        supportingText = {
                            if (showStartDateError) {
                                Text(
                                    "Formato incorrecto. Use DD/MM/YYYY",
                                    color = RojoAcento
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangeBright,
                            unfocusedBorderColor = GrisTextoSecundario,
                            errorBorderColor = RojoAcento
                        ),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        )
                    )
                    
                    // Botones rápidos para fecha de inicio
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Button(
                            onClick = { 
                                val calendar = Calendar.getInstance()
                                calendar.add(Calendar.DAY_OF_YEAR, -7)
                                startDateText = formatDate(calendar.timeInMillis)
                                startDate = calendar.timeInMillis
                                showStartDateError = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AzulAcento),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Hace 7 días")
                        }
                        Button(
                            onClick = { 
                                val calendar = Calendar.getInstance()
                                calendar.add(Calendar.DAY_OF_YEAR, -30)
                                startDateText = formatDate(calendar.timeInMillis)
                                startDate = calendar.timeInMillis
                                showStartDateError = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VerdeAcento),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Hace 30 días")
                        }
                    }
                }
                
                // Campo de fecha de fin
                Column {
                    Text(
                        text = "Fecha de fin:",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = endDateText,
                        onValueChange = { 
                            endDateText = it
                            showEndDateError = false
                            endDate = null
                        },
                        label = { Text("DD/MM/YYYY") },
                        placeholder = { Text("Ej: 31/12/2024") },
                        isError = showEndDateError,
                        supportingText = {
                            if (showEndDateError) {
                                Text(
                                    "Formato incorrecto. Use DD/MM/YYYY",
                                    color = RojoAcento
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangeBright,
                            unfocusedBorderColor = GrisTextoSecundario,
                            errorBorderColor = RojoAcento
                        ),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        )
                    )
                    
                    // Botones rápidos para fecha de fin
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Button(
                            onClick = { 
                                val calendar = Calendar.getInstance()
                                endDateText = formatDate(calendar.timeInMillis)
                                endDate = calendar.timeInMillis
                                showEndDateError = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeBright),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Hoy")
                        }
                        Button(
                            onClick = { 
                                val calendar = Calendar.getInstance()
                                calendar.add(Calendar.DAY_OF_YEAR, 7)
                                endDateText = formatDate(calendar.timeInMillis)
                                endDate = calendar.timeInMillis
                                showEndDateError = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RojoAcento),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("En 7 días")
                        }
                    }
                }
                
                // Fechas seleccionadas
                if ((startDate != null || startDateText.isNotBlank()) || (endDate != null || endDateText.isNotBlank())) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = OrangeBright.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Rango seleccionado:",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OrangeBright
                                )
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (startDate != null || startDateText.isNotBlank()) {
                                    Text(
                                        "Desde: ${if (startDate != null) formatDate(startDate!!) else startDateText}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OrangeBright
                                    )
                                }
                                if (endDate != null || endDateText.isNotBlank()) {
                                    Text(
                                        "Hasta: ${if (endDate != null) formatDate(endDate!!) else endDateText}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OrangeBright
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            // Validar y procesar fechas
                            val finalStartDate = startDate ?: parseDate(startDateText)
                            val finalEndDate = endDate ?: parseDate(endDateText)
                            
                            if (finalStartDate != null && finalEndDate != null) {
                                if (finalStartDate <= finalEndDate) {
                                    onDateRangeSelected(finalStartDate, finalEndDate)
                                } else {
                                    showStartDateError = true
                                    showEndDateError = true
                                }
                            } else {
                                if (finalStartDate == null && startDateText.isNotBlank()) {
                                    showStartDateError = true
                                }
                                if (finalEndDate == null && endDateText.isNotBlank()) {
                                    showEndDateError = true
                                }
                            }
                        },
                        enabled = (startDate != null || startDateText.isNotBlank()) && 
                                 (endDate != null || endDateText.isNotBlank()),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeBright),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Confirmar")
                    }
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
    return dateFormat.format(java.util.Date(timestamp))
}

private fun parseDate(dateText: String): Long? {
    return try {
        val parts = dateText.split("/")
        if (parts.size == 3) {
            val day = parts[0].toInt()
            val month = parts[1].toInt() - 1 // Calendar.MONTH es 0-based
            val year = parts[2].toInt()
            
            if (day in 1..31 && month in 0..11 && year >= 1900) {
                val calendar = java.util.Calendar.getInstance()
                calendar.set(year, month, day, 0, 0, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            } else {
                val endDate = java.util.Calendar.getInstance()
                endDate.set(year, month, day, 23, 59, 59)
                endDate.set(java.util.Calendar.MILLISECOND, 999)
                endDate.timeInMillis
            }
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
} 
