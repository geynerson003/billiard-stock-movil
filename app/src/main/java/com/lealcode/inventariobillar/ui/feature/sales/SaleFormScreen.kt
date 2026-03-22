package com.lealcode.inventariobillar.ui.feature.sales

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lealcode.inventariobillar.data.model.Sale
import com.lealcode.inventariobillar.data.model.SaleItem
import com.lealcode.inventariobillar.data.model.SaleType
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import com.lealcode.inventariobillar.ui.theme.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.outlined.*
import com.lealcode.inventariobillar.data.model.Product
import com.lealcode.inventariobillar.data.model.Client
import com.lealcode.inventariobillar.util.MathUtils
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Formulario para registrar una nueva venta.
 */
fun SaleFormScreen(
    products: List<Product>,
    clients: List<Client>,
    tables: List<String>,
    onSave: (Sale) -> Unit,
    onCancel: () -> Unit
) {
    // Estados para agregar productos
    var productSearch by remember { mutableStateOf("") }
    var showProductDropdown by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var quantity by remember { mutableStateOf("1") }
    var saleByBasket by remember { mutableStateOf(false) }

    // Lista de productos agregados a la venta
    var saleItems by remember { mutableStateOf<List<SaleItem>>(emptyList()) }

    // Estados generales de la venta
    var saleType by remember { mutableStateOf(SaleType.EXTERNAL) }
    var tableId by remember { mutableStateOf(tables.firstOrNull() ?: "") }

    // --- Cliente ---
    var clientSearch by remember { mutableStateOf("") }
    var selectedClient by remember { mutableStateOf<Client?>(null) }
    var showClientDropdown by remember { mutableStateOf(false) }

    // --- Estado de pago ---
    var isPaid by remember { mutableStateOf(true) } // Por defecto pagado

    // Calcular total de la venta
    val totalAmount = saleItems.sumOf { it.totalPrice }



    Box(Modifier.fillMaxSize().background(FondoClaro)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Contenido principal con scroll
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, OrangeBright.copy(alpha = 0.10f), MaterialTheme.shapes.large)
                    .shadow(8.dp, MaterialTheme.shapes.large),
                colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.PointOfSale, contentDescription = null, tint = OrangeBright, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Registrar venta",
                            color = OrangeBright,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(Modifier.height(16.dp))

                    // Sección para agregar productos
                    Text(
                        "Agregar productos",
                        color = OrangeBright,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(Modifier.height(8.dp))

                    val filteredProducts = products.filter { it.name.contains(productSearch, ignoreCase = true) }

                    ExposedDropdownMenuBox(
                        expanded = showProductDropdown && filteredProducts.isNotEmpty(),
                        onExpandedChange = { showProductDropdown = !showProductDropdown },
                    ) {
                        OutlinedTextField(
                            value = productSearch,
                            onValueChange = {
                                productSearch = it
                                showProductDropdown = it.isNotBlank()
                            },
                            label = { Text("Buscar producto") },
                            singleLine = true,
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                errorTextColor = Color.Black,
                                focusedBorderColor = OrangeBright,
                                unfocusedBorderColor = OrangeBright.copy(alpha = 0.5f),
                                focusedLabelColor = OrangeBright,
                                unfocusedLabelColor = Color.Black.copy(alpha = 0.6f)
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = showProductDropdown && filteredProducts.isNotEmpty(),
                            onDismissRequest = { showProductDropdown = false },

                            ) {
                            filteredProducts.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item.name) },
                                    onClick = {
                                        selectedProduct = item
                                        productSearch = item.name
                                        showProductDropdown = false
                                        saleByBasket = false // reset tipo de venta
                                    }
                                )
                            }
                        }
                    }
                    // Búsqueda y selección de producto


                    Spacer(Modifier.height(8.dp))

                    // --- Tipo de venta (Mesa/Externa) ---
                    Text("Tipo de venta:", color = GrisTextoSecundario, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilterChip(
                            selected = saleType == SaleType.EXTERNAL,
                            onClick = { saleType = SaleType.EXTERNAL },
                            label = { Text("Externa") },
                            leadingIcon = {
                                Icon(Icons.Outlined.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        )
                        Spacer(Modifier.width(8.dp))
                        FilterChip(
                            selected = saleType == SaleType.TABLE,
                            onClick = { saleType = SaleType.TABLE },
                            label = { Text("Por Mesa") },
                            leadingIcon = {
                                Icon(Icons.Outlined.EventSeat, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        )
                    }
                    Spacer(Modifier.height(8.dp))

                    // Si el producto tiene precio por canasta, mostrar opción
                    if (selectedProduct?.saleBasketPrice != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Tipo de producto:", color = GrisTextoSecundario)
                            Spacer(Modifier.width(8.dp))
                            FilterChip(selected = !saleByBasket, onClick = { saleByBasket = false }, label = { Text("Unitario") })
                            Spacer(Modifier.width(8.dp))
                            FilterChip(selected = saleByBasket, onClick = { saleByBasket = true }, label = { Text("Canasta") })
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    // Selección de mesa si es venta por mesa
                    if (saleType == SaleType.TABLE) {
                        Text("Seleccionar mesa:", color = GrisTextoSecundario, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        var showTableDropdown by remember { mutableStateOf(false) }
                        OutlinedTextField(
                            value = tableId.ifEmpty { "Seleccionar mesa..." },
                            onValueChange = { },
                            label = { Text("Mesa") },
                            readOnly = true,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { showTableDropdown = true }) {
                                    Icon(Icons.Outlined.ArrowDropDown, contentDescription = "Seleccionar mesa")
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                errorTextColor = Color.Black,
                                focusedBorderColor = OrangeBright,
                                unfocusedBorderColor = OrangeBright.copy(alpha = 0.5f),
                                focusedLabelColor = OrangeBright,
                                unfocusedLabelColor = Color.Black.copy(alpha = 0.6f)
                            )
                        )
                        DropdownMenu(
                            expanded = showTableDropdown,
                            onDismissRequest = { showTableDropdown = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            tables.forEach { table ->
                                DropdownMenuItem(
                                    text = { Text(table) },
                                    onClick = {
                                        tableId = table
                                        showTableDropdown = false
                                    }
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    // Cantidad y botón agregar producto
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                            label = { Text("Cantidad") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                errorTextColor = Color.Black,
                                focusedBorderColor = OrangeBright,
                                unfocusedBorderColor = OrangeBright.copy(alpha = 0.5f),
                                focusedLabelColor = OrangeBright,
                                unfocusedLabelColor = Color.Black.copy(alpha = 0.6f)
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                selectedProduct?.let { product ->
                                    val quantityInt = quantity.toIntOrNull() ?: 1

                                    // Número de canastas (si es canasta) y número de unidades vendidas
                                    val basketsCount = quantityInt
                                    val unitsCount = if (saleByBasket) {
                                        quantityInt * (product.unitsPerPackage ?: 1)
                                    } else {
                                        quantityInt
                                    }

                                    // Precio según el tipo de venta (unitario o por canasta)
                                    val unitPrice = if (saleByBasket) {
                                        product.saleBasketPrice ?: 0.0
                                    } else {
                                        product.salePrice
                                    }

                                    // TotalPrice: si es canasta -> precio por canasta * número de canastas,
                                    // si es unidad -> precio por unidad * número de unidades
                                    val totalPrice = if (saleByBasket) {
                                        unitPrice * basketsCount
                                    } else {
                                        unitPrice * unitsCount
                                    }

                                    // IMPORTANT: guardar `quantity` como número de canastas cuando es venta por canasta
                                    // y como número de unidades cuando es venta por unidad. Esto mantiene la consistencia
                                    // con funciones de cálculo que esperan `quantity` en estas unidades.
                                    val itemQuantity = if (saleByBasket) basketsCount else unitsCount

                                    val newItem = SaleItem(
                                        productId = product.id,
                                        productName = product.name,
                                        quantity = itemQuantity,
                                        unitPrice = unitPrice,
                                        totalPrice = totalPrice,
                                        saleByBasket = saleByBasket
                                    )

                                    saleItems = saleItems + newItem

                                    // Limpiar campos
                                    selectedProduct = null
                                    productSearch = ""
                                    quantity = "1"
                                    saleByBasket = false
                                }
                            },
                            enabled = selectedProduct != null,
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeBright),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Icon(Icons.Outlined.Add, contentDescription = null)
                            Text("Agregar")
                        }
                    }
                    Spacer(Modifier.height(8.dp))

                    // Mostrar preview del producto a agregar
                    selectedProduct?.let { product ->
                        val quantityValue = quantity.toIntOrNull() ?: 1
                        val unitPrice = if (saleByBasket) (product.saleBasketPrice ?: 0.0) else product.salePrice
                        val totalPrice = unitPrice * quantityValue

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = OrangeBright.copy(alpha = 0.1f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "Preview del producto",
                                    fontWeight = FontWeight.Bold,
                                    color = OrangeBright
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Precio por ${if (saleByBasket) "canasta" else "unidad"}: ${MathUtils.formatPrice(unitPrice)}",
                                    color = Color.Black
                                )
                                Text(
                                    text = "Cantidad: $quantityValue",
                                    color = Color.Black
                                )
                                Text(
                                    text = "Total: ${MathUtils.formatPrice(totalPrice)}",
                                    fontWeight = FontWeight.Bold,
                                    color = OrangeBright
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    // Lista de productos agregados
                    if (saleItems.isNotEmpty()) {
                        Text(
                            "Productos en la venta",
                            color = OrangeBright,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(Modifier.height(8.dp))

                        saleItems.forEachIndexed { index, item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, OrangeBright.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.productName,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.Black
                                        )
                                        Text(
                                            text = "${item.quantity} x ${MathUtils.formatPrice(item.unitPrice)}${if (item.saleByBasket) " (canasta)" else ""}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = GrisTextoSecundario
                                        )
                                        Text(
                                            text = "Total: ${MathUtils.formatPrice(item.totalPrice)}",
                                            fontWeight = FontWeight.Bold,
                                            color = OrangeBright
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            saleItems = saleItems.toMutableList().apply { removeAt(index) }
                                        }
                                    ) {
                                        Icon(
                                            Icons.Outlined.Delete,
                                            contentDescription = "Eliminar producto",
                                            tint = RojoAcento
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }

                        // Total de la venta
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = OrangeBright.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total de la venta:",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    text = MathUtils.formatPrice(totalAmount),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = OrangeBright
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    // --- Búsqueda de cliente mejorada ---

                    ExposedDropdownMenuBox(
                        expanded = showClientDropdown, onExpandedChange = { showClientDropdown = !showClientDropdown }
                    ) {
                        OutlinedTextField(
                            value = clientSearch,
                            onValueChange = {
                                clientSearch = it
                                showClientDropdown = it.isNotBlank()
                            },
                            label = { Text("Buscar cliente") },
                            placeholder = { Text("Escribe el nombre o teléfono del cliente...") },
                            singleLine = true,
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            trailingIcon = {
                                if (clientSearch.isNotBlank()) {
                                    IconButton(onClick = {
                                        clientSearch = ""
                                        selectedClient = null
                                        showClientDropdown = false
                                    }) {
                                        Icon(Icons.Outlined.Clear, contentDescription = "Limpiar búsqueda")
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedBorderColor = OrangeBright,
                                unfocusedBorderColor = OrangeBright.copy(alpha = 0.5f),
                                focusedLabelColor = OrangeBright,
                                unfocusedLabelColor = Color.Black.copy(alpha = 0.6f)
                            )
                        )

                        // Lista filtrada de clientes con búsqueda mejorada
                        val filteredClients = clients.filter { client ->
                            client.nombre.contains(clientSearch, ignoreCase = true) ||
                                    client.telefono.contains(clientSearch, ignoreCase = true)
                        }
                        ExposedDropdownMenu(
                            expanded = showClientDropdown && (filteredClients.isNotEmpty() || clientSearch.isNotBlank()),
                            onDismissRequest = { showClientDropdown = false },
                        ) {
                            if (filteredClients.isEmpty() && clientSearch.isNotBlank()) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "No se encontraron clientes",
                                            color = GrisTextoSecundario,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    },
                                    onClick = { }
                                )
                            } else {
                                filteredClients.forEach { client ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    text = client.nombre,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                if (client.telefono.isNotBlank()) {
                                                    Text(
                                                        text = client.telefono,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = GrisTextoSecundario
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            selectedClient = client
                                            clientSearch = client.nombre
                                            showClientDropdown = false
                                            android.util.Log.d("SaleFormScreen", "Cliente seleccionado: ${client.nombre} (ID: ${client.id})")
                                        }
                                    )
                                }
                            }
                        }
                    }


                    // Mostrar cliente seleccionado
                    selectedClient?.let { client ->
                        Spacer(Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = OrangeBright.copy(alpha = 0.1f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Outlined.Person,
                                        contentDescription = null,
                                        tint = OrangeBright,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = client.nombre,
                                            fontWeight = FontWeight.Medium,
                                            color = OrangeBright
                                        )
                                        if (client.telefono.isNotBlank()) {
                                            Text(
                                                text = client.telefono,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = GrisTextoSecundario
                                            )
                                        }
                                    }
                                }

                                // Mostrar deuda del cliente si existe
                                if (client.deuda > 0) {
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Outlined.Warning,
                                            contentDescription = null,
                                            tint = RojoAcento,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = "Deuda pendiente: ${MathUtils.formatPrice(client.deuda)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = RojoAcento,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // --- Estado de pago ---
                    Spacer(Modifier.height(16.dp))
                    Text("Estado de pago:", color = GrisTextoSecundario, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilterChip(
                            selected = isPaid,
                            onClick = {
                                isPaid = true
                                android.util.Log.d("SaleFormScreen", "Usuario seleccionó: Pagado")
                            },
                            label = { Text("Pagado") },
                            leadingIcon = {
                                Icon(Icons.Outlined.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        )
                        Spacer(Modifier.width(8.dp))
                        FilterChip(
                            selected = !isPaid,
                            onClick = {
                                isPaid = false
                                android.util.Log.d("SaleFormScreen", "Usuario seleccionó: Pendiente")
                            },
                            label = { Text("Pendiente") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        )
                    }

                    // Debug: Mostrar el estado actual
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Estado actual: ${if (isPaid) "Pagado" else "Pendiente"}",
                        color = if (isPaid) VerdeAcento else RojoAcento,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )

                    // Mostrar advertencia si hay deuda y se marca como pagado
                    selectedClient?.let { client ->
                        if (client.deuda > 0 && isPaid) {
                            Spacer(Modifier.height(8.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = RojoAcento.copy(alpha = 0.1f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Outlined.Info,
                                        contentDescription = null,
                                        tint = RojoAcento,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "Cliente con deuda pendiente. Verificar pago.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = RojoAcento,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Botones fuera de la Card para que estén siempre visibles
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        // Debug: Log del estado de pago
                        android.util.Log.d("SaleFormScreen", "Guardando venta con isPaid: $isPaid")
                        android.util.Log.d("SaleFormScreen", "Cliente seleccionado: ${selectedClient?.nombre ?: "Ninguno"}")
                        android.util.Log.d("SaleFormScreen", "Total de productos: ${saleItems.size}")
                        android.util.Log.d("SaleFormScreen", "Total de la venta: $totalAmount")

                        // Calcular profit: suma de (precio de venta - costo proveedor) por cada item
                        val productMap = products.associateBy { it.id }
                        val profit = saleItems.sumOf { item ->
                            val product = productMap[item.productId]
                            if (product != null) {
                                if (item.saleByBasket) {
                                    // Para ventas por canasta: ganancia = (precio venta - precio proveedor) * cantidad
                                    val profitPerBasket = MathUtils.calculateProfitPerBasket(item.unitPrice, product.supplierPrice)
                                    profitPerBasket * item.quantity
                                } else {
                                    // Para ventas por unidad: ganancia = (precio venta - (precio proveedor / unidades)) * cantidad
                                    if (product.unitsPerPackage > 0) {
                                        val supplierPricePerUnit = product.supplierPrice / product.unitsPerPackage
                                        val profitPerUnit = MathUtils.calculateProfitPerUnit(item.unitPrice, supplierPricePerUnit)
                                        profitPerUnit * item.quantity
                                    } else 0.0
                                }
                            } else 0.0
                        }
                        onSave(
                            Sale(
                                id = "",
                                items = saleItems,
                                totalAmount = totalAmount,
                                profit = profit,
                                tableId = if (saleType == SaleType.TABLE) tableId else null,
                                type = saleType,
                                clientId = selectedClient?.id ?: "",
                                isPaid = isPaid
                            )
                        )
                    },
                    enabled = saleItems.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeBright, contentColor = Color.White),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.Check, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Guardar")
                }
                Spacer(Modifier.width(16.dp))
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.Close, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Cancelar")
                }
            }
        }
    }
}
