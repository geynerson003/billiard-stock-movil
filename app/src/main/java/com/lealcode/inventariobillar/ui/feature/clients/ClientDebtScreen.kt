package com.lealcode.inventariobillar.ui.feature.clients

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lealcode.inventariobillar.data.model.Client
import com.lealcode.inventariobillar.data.model.Sale
import com.lealcode.inventariobillar.data.model.Payment
import com.lealcode.inventariobillar.data.model.Table
import com.lealcode.inventariobillar.data.service.ClientDebtInfo
import com.lealcode.inventariobillar.ui.theme.*
import com.lealcode.inventariobillar.util.MathUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Pantalla de detalle y gestion de deuda de un cliente.
 */
fun ClientDebtScreen(
    client: Client,
    pendingSales: List<Sale>,
    debtInfo: ClientDebtInfo?,
    payments: List<Payment>,
    onBack: () -> Unit,
    onPayDebt: (Double, String, String) -> Unit,
    tables: List<Table>
) {
    var paymentAmount by remember { mutableStateOf("") }
    var paymentDescription by remember { mutableStateOf("") }
    var paymentNotes by remember { mutableStateOf("") }
    
    val totalDebt = debtInfo?.remainingDebt ?: 0.0
    
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Deudas de ${client.nombre}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AzulAcento
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FondoTarjeta
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(FondoClaro),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Resumen de deuda
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .shadow(8.dp, shape = MaterialTheme.shapes.large),
                    colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.Person,
                                contentDescription = null,
                                tint = AzulAcento,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = client.nombre,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                if (client.telefono.isNotBlank()) {
                                    Text(
                                        text = client.telefono,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = GrisTextoSecundario
                                    )
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Deuda Restante",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GrisTextoSecundario
                                )
                                Text(
                                    text = MathUtils.formatPrice(totalDebt),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = RojoAcento
                                    )
                                )
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Total Pagado",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GrisTextoSecundario
                                )
                                Text(
                                    text = MathUtils.formatPrice(debtInfo?.totalPaid ?: 0.0),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = VerdeAcento
                                    )
                                )
                            }
                        }
                        
                        if (debtInfo != null && debtInfo.totalDebt > 0) {
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Deuda Original",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GrisTextoSecundario
                                    )
                                    Text(
                                        text = MathUtils.formatPrice(debtInfo.totalDebt),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = GrisTextoSecundario
                                        )
                                    )
                                }
                                
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Ventas Pendientes",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GrisTextoSecundario
                                    )
                                    Text(
                                        text = "${pendingSales.size}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = OrangeBright
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Opciones de pago
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .shadow(8.dp, shape = MaterialTheme.shapes.large),
                    colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Opciones de Pago",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AzulAcento
                            )
                        )
                        
                        Spacer(Modifier.height(8.dp))
                        
                        Text(
                            text = "Ingresa el monto que deseas pagar:",
                            style = MaterialTheme.typography.bodySmall,
                            color = GrisTextoSecundario
                        )
                        
                        Spacer(Modifier.height(12.dp))
                        
                        // Campo para monto personalizado
                        OutlinedTextField(
                            value = paymentAmount,
                            onValueChange = { 
                                paymentAmount = it.filter { char -> char.isDigit() || char == '.' }
                            },
                            label = { Text("Monto a pagar") },
                            placeholder = { Text("Ingrese el monto (máx: ${MathUtils.formatPrice(totalDebt)})") },
                            enabled = totalDebt > 0,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.AttachMoney,
                                    contentDescription = null,
                                    tint = AzulAcento
                                )
                            }
                        )
                        
                        Spacer(Modifier.height(8.dp))
                        
                        // Campo para descripción del pago
                        OutlinedTextField(
                            value = paymentDescription,
                            onValueChange = { paymentDescription = it },
                            label = { Text("Descripción (opcional)") },
                            placeholder = { Text("Ej: Pago parcial, Abono, etc.") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Description,
                                    contentDescription = null,
                                    tint = AzulAcento
                                )
                            }
                        )
                        
                        Spacer(Modifier.height(8.dp))
                        
                        // Campo para notas del pago
                        OutlinedTextField(
                            value = paymentNotes,
                            onValueChange = { paymentNotes = it },
                            label = { Text("Notas (opcional)") },
                            placeholder = { Text("Información adicional del pago") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Note,
                                    contentDescription = null,
                                    tint = AzulAcento
                                )
                            }
                        )
                        
                        Spacer(Modifier.height(12.dp))
                        
                        // Botón para pagar
                        Button(
                            onClick = {
                                val amount = paymentAmount.toDoubleOrNull() ?: totalDebt
                                if (amount > 0 && amount <= totalDebt) {
                                    onPayDebt(amount, paymentDescription, paymentNotes)
                                    paymentAmount = "" // Limpiar campos después del pago
                                    paymentDescription = ""
                                    paymentNotes = ""
                                }
                            },
                            enabled = totalDebt > 0 && (paymentAmount.isBlank() || (paymentAmount.toDoubleOrNull() ?: 0.0) > 0),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VerdeAcento,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Outlined.Payment,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Registrar Pago",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            // Tabs
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = FondoTarjeta,
                    contentColor = AzulAcento
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Ventas Pendientes (${pendingSales.size})") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Historial de Pagos (${payments.size})") }
                    )
                }
            }
            
            if (selectedTab == 0) {
                if (pendingSales.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay ventas pendientes",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GrisTextoSecundario
                            )
                        }
                    }
                } else {
                    items(pendingSales, key = { it.id }) { sale ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            PendingSaleCard(sale = sale, tables = tables)
                        }
                    }
                }
            } else {
                if (payments.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay historial de pagos",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GrisTextoSecundario
                            )
                        }
                    }
                } else {
                    items(payments.sortedByDescending { it.date }, key = { it.id }) { payment ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            PaymentCard(payment = payment)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PendingSaleCard(
    sale: Sale,
    tables: List<Table>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, shape = MaterialTheme.shapes.medium),
        colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Schedule,
                contentDescription = null,
                tint = RojoAcento,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                if (sale.items.isNotEmpty()) {
                    // Nueva estructura con items
                    val firstItem = sale.items.first()
                    Text(
                        text = firstItem.productName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    if (sale.items.size > 1) {
                        Text(
                            text = "Y ${sale.items.size - 1} productos más",
                            style = MaterialTheme.typography.bodySmall,
                            color = GrisTextoSecundario
                        )
                    } else {
                        Text(
                            text = "Cantidad: ${firstItem.quantity}",
                            style = MaterialTheme.typography.bodySmall,
                            color = GrisTextoSecundario
                        )
                    }
                } else {
                    // Estructura anterior (deprecated)
                    @Suppress("DEPRECATION")
                    Text(
                        text = sale.productName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    @Suppress("DEPRECATION")
                    Text(
                        text = "Cantidad: ${sale.quantity}",
                        style = MaterialTheme.typography.bodySmall,
                        color = GrisTextoSecundario
                    )
                }
                
                if (sale.tableId != null) {
                    val tableName = tables.find { it.id == sale.tableId }?.name ?: "Mesa ${sale.tableId}"
                    Text(
                        text = "Mesa: $tableName",
                        style = MaterialTheme.typography.bodySmall,
                        color = GrisTextoSecundario
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                @Suppress("DEPRECATION")
                val saleAmount = if (sale.items.isNotEmpty()) sale.totalAmount else sale.price
                Text(
                    text = MathUtils.formatPrice(saleAmount),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = RojoAcento
                    )
                )
                val fechaFormateada = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date(sale.date))
                Text(
                    text = fechaFormateada,
                    style = MaterialTheme.typography.bodySmall,
                    color = GrisTextoSecundario
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentCard(
    payment: Payment
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, shape = MaterialTheme.shapes.medium),
        colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Payment,
                contentDescription = null,
                tint = VerdeAcento,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = payment.description.ifBlank { "Pago de deuda" },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Método: ${payment.paymentMethod.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = GrisTextoSecundario
                )
                if (payment.notes.isNotBlank()) {
                    Text(
                        text = payment.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = GrisTextoSecundario
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = MathUtils.formatPrice(payment.amount),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = VerdeAcento
                    )
                )
                Text(
                    text = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date(payment.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = GrisTextoSecundario
                )
            }
        }
    }
} 
