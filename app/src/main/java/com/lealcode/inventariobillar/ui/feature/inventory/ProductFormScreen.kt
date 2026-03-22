package com.lealcode.inventariobillar.ui.feature.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lealcode.inventariobillar.data.model.Product
import com.lealcode.inventariobillar.ui.theme.*
import com.lealcode.inventariobillar.util.MathUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Formulario para crear o editar productos del inventario.
 */
fun ProductFormScreen(
    initialProduct: Product? = null,
    onSave: (Product) -> Unit,
    onCancel: () -> Unit
) {
    val isEditing = initialProduct != null

    var name by remember { mutableStateOf("") }
    var supplierPrice by remember { mutableStateOf("") }
    var salePrice by remember { mutableStateOf("") }
    var minStock by remember { mutableStateOf("") }
    var saleBasketPrice by remember { mutableStateOf("") }
    var unitsPerPackage by remember { mutableStateOf("1") }

    /** STOCK BASE (REAL) */
    var stockBase by remember { mutableStateOf(0) }

    /** PAQUETES A AGREGAR */
    var paquetes by remember { mutableStateOf("") }

    /** CARGA CORRECTA AL EDITAR */
    LaunchedEffect(initialProduct) {
        initialProduct?.let {
            name = it.name
            stockBase = it.stock
            supplierPrice = it.supplierPrice.toString()
            salePrice = it.salePrice.toString()
            minStock = it.minStock.toString()
            saleBasketPrice = it.saleBasketPrice?.toString() ?: ""
            unitsPerPackage = it.unitsPerPackage.toString()
            paquetes = ""
        }
    }

    /**  NUEVO STOCK CALCULADO  */
    val stockCalculado = run {
        val paquetesInt = paquetes.toIntOrNull() ?: 0
        val unidades = unitsPerPackage.toIntOrNull() ?: 0
        if (paquetesInt > 0 && unidades > 0) {
            stockBase + (paquetesInt * unidades)
        } else {
            stockBase
        }
    }

    /** CÁLCULOS */
    val supplierPricePerUnit = MathUtils.calculateSupplierPricePerUnit(
        MathUtils.safeStringToDouble(supplierPrice),
        MathUtils.safeStringToInt(unitsPerPackage)
    )

    val profitPerUnit = MathUtils.calculateProfitPerUnit(
        MathUtils.safeStringToDouble(salePrice),
        supplierPricePerUnit
    )

    val profitMarginPerUnit = MathUtils.calculateProfitMarginPerUnit(
        MathUtils.safeStringToDouble(salePrice),
        supplierPricePerUnit
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Inventory2, null, tint = AzulAcento)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (isEditing) "Editar producto" else "Agregar producto",
                            color = AzulAcento,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FondoTarjeta)
            )
        }
    ) { padding ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .padding(24.dp)
                .border(1.dp, AzulAcento.copy(0.1f))
                .shadow(8.dp),
            colors = CardDefaults.cardColors(containerColor = FondoTarjeta)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        errorTextColor = Color.Black,
                        focusedBorderColor = AzulAcento,
                        unfocusedBorderColor = AzulAcento.copy(alpha = 0.5f),
                        focusedLabelColor = AzulAcento,
                        unfocusedLabelColor = Color.Black.copy(alpha = 0.6f)
                    )
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = unitsPerPackage,
                    onValueChange = { unitsPerPackage = it.filter(Char::isDigit) },
                    label = { Text("Unidades por paquete") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        errorTextColor = Color.Black,
                        focusedBorderColor = AzulAcento,
                        unfocusedBorderColor = AzulAcento.copy(alpha = 0.5f),
                        focusedLabelColor = AzulAcento,
                        unfocusedLabelColor = Color.Black.copy(alpha = 0.6f)
                    )
                )
                Spacer(Modifier.height(8.dp))


                OutlinedTextField(
                    value = paquetes,
                    onValueChange = { paquetes = it.filter(Char::isDigit) },
                    label = { Text("Cantidad de paquetes") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        errorTextColor = Color.Black,
                        focusedBorderColor = AzulAcento,
                        unfocusedBorderColor = AzulAcento.copy(alpha = 0.5f),
                        focusedLabelColor = AzulAcento,
                        unfocusedLabelColor = Color.Black.copy(alpha = 0.6f)
                    )
                )
                Spacer(Modifier.height(8.dp))


                OutlinedTextField(
                    value = stockCalculado.toString(),
                    onValueChange = {},
                    label = { Text("Stock total resultante") },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        disabledTextColor = Color.Black.copy(alpha = 0.6f),
                        disabledBorderColor = AzulAcento.copy(alpha = 0.3f),
                        disabledLabelColor = AzulAcento.copy(alpha = 0.6f)
                    )
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = supplierPrice,
                    onValueChange = { supplierPrice = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Precio proveedor") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        errorTextColor = Color.Black,
                        focusedBorderColor = AzulAcento,
                        unfocusedBorderColor = AzulAcento.copy(alpha = 0.5f),
                        focusedLabelColor = AzulAcento,
                        unfocusedLabelColor = Color.Black.copy(alpha = 0.6f)
                    )
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = salePrice,
                    onValueChange = { salePrice = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Precio de venta") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        errorTextColor = Color.Black,
                        focusedBorderColor = AzulAcento,
                        unfocusedBorderColor = AzulAcento.copy(alpha = 0.5f),
                        focusedLabelColor = AzulAcento,
                        unfocusedLabelColor = Color.Black.copy(alpha = 0.6f)
                    )
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = saleBasketPrice,
                    onValueChange = { saleBasketPrice = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Precio por canasta (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        errorTextColor = Color.Black,
                        focusedBorderColor = AzulAcento,
                        unfocusedBorderColor = AzulAcento.copy(alpha = 0.5f),
                        focusedLabelColor = AzulAcento,
                        unfocusedLabelColor = Color.Black.copy(alpha = 0.6f)
                    )
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = minStock,
                    onValueChange = { minStock = it.filter(Char::isDigit) },
                    label = { Text("Stock mínimo") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        errorTextColor = Color.Black,
                        focusedBorderColor = AzulAcento,
                        unfocusedBorderColor = AzulAcento.copy(alpha = 0.5f),
                        focusedLabelColor = AzulAcento,
                        unfocusedLabelColor = Color.Black.copy(alpha = 0.6f)
                    )
                )
                Spacer(Modifier.height(16.dp))

                if (supplierPrice.isNotEmpty() && salePrice.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AzulAcento.copy(0.05f)),
                        border = BorderStroke(1.dp, AzulAcento.copy(0.2f))
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Cálculos", fontWeight = FontWeight.Bold, color = AzulAcento)
                            Text("Proveedor/unidad: ${MathUtils.formatPrice(supplierPricePerUnit)}")
                            Text("Ganancia/unidad: ${MathUtils.formatPrice(profitPerUnit)}")
                            Text("Margen: ${MathUtils.formatPercentage(profitMarginPerUnit)}")
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row {
                    Button(onClick = {
                        onSave(
                            Product(
                                id = initialProduct?.id ?: System.currentTimeMillis().toString(),
                                name = name.trim(),
                                stock = stockCalculado,
                                supplierPrice = supplierPrice.toDoubleOrNull() ?: 0.0,
                                salePrice = salePrice.toDoubleOrNull() ?: 0.0,
                                minStock = minStock.toIntOrNull() ?: 0,
                                saleBasketPrice = saleBasketPrice.toDoubleOrNull(),
                                unitsPerPackage = unitsPerPackage.toIntOrNull() ?: 1
                            )
                        )
                    }) {
                        Icon(Icons.Filled.Check, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Guardar")
                    }

                    Spacer(Modifier.width(16.dp))

                    OutlinedButton(onClick = onCancel) {
                        Icon(Icons.Filled.Close, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Cancelar")
                    }
                }
            }
        }
    }
}

@Composable
/**
 * Pantalla de detalle de un producto individual.
 */
fun ProductDetailScreen(
    product: Product,
    onEdit: (Product) -> Unit,
    onBack: () -> Unit
) {
    // Calcular precio por unidad del proveedor usando el campo unitsPerPackage del producto
    val supplierPricePerUnit = MathUtils.calculateSupplierPricePerUnit(product.supplierPrice, product.unitsPerPackage)

    // Calcular ganancia por unidad
    val gananciaPorUnidad = MathUtils.calculateProfitPerUnit(product.salePrice, supplierPricePerUnit)

    // Calcular margen de ganancia por unidad
    val margenGananciaUnidad = MathUtils.calculateProfitMarginPerUnit(product.salePrice, supplierPricePerUnit)

    // Calcular ganancia por paquete/canasta
    val gananciaPorPaquete = product.saleBasketPrice?.let { basketPrice ->
        MathUtils.calculateProfitPerBasket(basketPrice, product.supplierPrice)
    } ?: 0.0

    // Calcular margen de ganancia por paquete/canasta
    val margenGananciaPaquete = product.saleBasketPrice?.let { basketPrice ->
        MathUtils.calculateProfitMarginPerBasket(basketPrice, product.supplierPrice)
    } ?: 0.0

    // Calcular valores del inventario
    val valorInventarioProveedor = MathUtils.calculateInventoryValueAtSupplierPrice(product.stock, supplierPricePerUnit)
    val valorInventarioVenta = MathUtils.calculateInventoryValueAtSalePrice(product.stock, product.salePrice)
    val gananciaPotencialTotal = MathUtils.calculateTotalPotentialProfit(product.stock, gananciaPorUnidad)

    Box(Modifier.fillMaxSize().background(FondoClaro)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .border(1.dp, AzulAcento.copy(alpha = 0.10f), MaterialTheme.shapes.large)
                .shadow(8.dp, MaterialTheme.shapes.large),
            colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            val scrollState = rememberScrollState()
            Column(
                Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Inventory2, contentDescription = null, tint = AzulAcento, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Detalle del producto",
                        color = AzulAcento,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(Modifier.height(16.dp))

                // Información básica del producto
                Text("Nombre: ${product.name}", color = Color.Black, fontWeight = FontWeight.Medium)
                Text("Stock: ${product.stock} unidades", color = Color.Black)
                Text("Unidades por paquete: ${product.unitsPerPackage}", color = Color.Black)
                Text("Precio proveedor por paquete: ${MathUtils.formatPrice(product.supplierPrice)}", color = Color.Black)
                Text("Precio de venta por unidad: ${MathUtils.formatPrice(product.salePrice)}", color = Color.Black)
                Text("Precio por canasta: ${product.saleBasketPrice?.let { MathUtils.formatPrice(it) } ?: "-"}", color = Color.Black)
                Text("Stock mínimo: ${product.minStock}", color = Color.Black)

                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))

                // Cálculos por unidad
                Text("=== CÁLCULOS POR UNIDAD ===", color = AzulAcento, fontWeight = FontWeight.Bold)
                Text("Precio proveedor por unidad: ${MathUtils.formatPrice(supplierPricePerUnit)}", color = Color(0xFF1976D2), fontWeight = FontWeight.Medium)
                Text("Ganancia por unidad: ${MathUtils.formatPrice(gananciaPorUnidad)}", color = Color(0xFF388E3C), fontWeight = FontWeight.Bold)
                Text("Margen de ganancia por unidad: ${MathUtils.formatPercentage(margenGananciaUnidad)}", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(8.dp))

                // Cálculos por paquete/canasta
                if (product.saleBasketPrice != null) {
                    Text("=== CÁLCULOS POR PAQUETE/CANASTA ===", color = AzulAcento, fontWeight = FontWeight.Bold)
                    Text("Ganancia por paquete: ${MathUtils.formatPrice(gananciaPorPaquete)}", color = Color(0xFF388E3C), fontWeight = FontWeight.Bold)
                    Text("Margen de ganancia por paquete: ${MathUtils.formatPercentage(margenGananciaPaquete)}", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                }

                // Valores del inventario
                Text("=== VALORES DEL INVENTARIO ===", color = AzulAcento, fontWeight = FontWeight.Bold)
                Text("Valor al precio proveedor: ${MathUtils.formatPrice(valorInventarioProveedor)}", color = Color(0xFF1976D2), fontWeight = FontWeight.Medium)
                Text("Valor al precio de venta: ${MathUtils.formatPrice(valorInventarioVenta)}", color = Color(0xFF388E3C), fontWeight = FontWeight.Medium)
                Text("Ganancia potencial total: ${MathUtils.formatPrice(gananciaPotencialTotal)}", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(20.dp))
                Row {
                    Button(
                        onClick = { onEdit(product) },
                        colors = ButtonDefaults.buttonColors(containerColor = AzulAcento, contentColor = Color.White)
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Editar")
                    }
                    Spacer(Modifier.width(16.dp))
                    OutlinedButton(onClick = onBack) {
                        Icon(Icons.Filled.Close, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Volver")
                    }
                }
            }
        }
    }
}
