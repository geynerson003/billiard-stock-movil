package com.lealcode.inventariobillar.ui.feature.sales

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lealcode.inventariobillar.data.model.Sale
import com.lealcode.inventariobillar.ui.theme.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import com.lealcode.inventariobillar.data.model.Product
import com.lealcode.inventariobillar.data.model.Client
import com.lealcode.inventariobillar.data.model.Table
import com.lealcode.inventariobillar.util.MathUtils

@Composable
/**
 * Pantalla de detalle de una venta puntual.
 */
fun SaleDetailScreen(
    sale: Sale,
    onDelete: () -> Unit,
    onBack: () -> Unit,
    products: List<Product> = emptyList(),
    clients: List<Client> = emptyList(),
    tables: List<Table> = emptyList()
) {
    val client = clients.find { it.id == sale.clientId }
    
    Box(Modifier.fillMaxSize().background(FondoClaro)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
                .border(1.dp, OrangeBright.copy(alpha = 0.10f), MaterialTheme.shapes.large)
                .shadow(8.dp, MaterialTheme.shapes.large),
            colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.PointOfSale, contentDescription = null, tint = OrangeBright, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Detalle de venta",
                        color = OrangeBright,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(Modifier.height(16.dp))
                
                // Información general de la venta
                Text("Tipo: ${sale.type}", color = Color.Black, fontWeight = FontWeight.Medium)
                val tableName = if (sale.tableId != null) {
                    tables.find { it.id == sale.tableId }?.name ?: "Mesa ${sale.tableId}"
                } else {
                    "Externa"
                }
                Text("Mesa: $tableName", color = Color.Black)
                
                // Mostrar información del cliente
                if (sale.clientId.isNotEmpty()) {
                    Text(
                        "Cliente: ${client?.nombre ?: "Cliente no encontrado"}", 
                        color = Color.Black,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text("Cliente: Sin asignar", color = GrisTextoSecundario)
                }
                
                // Mostrar estado de pago
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (sale.isPaid) Icons.Outlined.CheckCircle else Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = if (sale.isPaid) VerdeAcento else RojoAcento,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (sale.isPaid) "Pagado" else "Pendiente",
                        color = if (sale.isPaid) VerdeAcento else RojoAcento,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Mostrar productos
                if (sale.items.isNotEmpty()) {
                    // Nueva estructura con múltiples productos
                    Text(
                        "Productos (${sale.items.size})",
                        color = OrangeBright,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(Modifier.height(8.dp))
                    
                    sale.items.forEach { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, OrangeBright.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = item.productName,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    text = "Cantidad: ${item.quantity}",
                                    color = GrisTextoSecundario
                                )
                                Text(
                                    text = "Precio unitario: ${MathUtils.formatPrice(item.unitPrice)}${if (item.saleByBasket) " (canasta)" else ""}",
                                    color = GrisTextoSecundario
                                )
                                Text(
                                    text = "Subtotal: ${MathUtils.formatPrice(item.totalPrice)}",
                                    fontWeight = FontWeight.Bold,
                                    color = OrangeBright
                                )
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
                                text = MathUtils.formatPrice(sale.totalAmount),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = OrangeBright
                            )
                        }
                    }
                } else {
                    // Estructura anterior (compatibilidad)
                    Text(
                        "Producto (estructura anterior)",
                        color = OrangeBright,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(Modifier.height(8.dp))
                    
                    Text("Producto: ${sale.productName}", color = Color.Black)
                    Text("Cantidad: ${sale.quantity}", color = Color.Black)
                    
                    // Detectar tipo de venta
                    val product = products.find { it.name == sale.productName }
                    val tipoVenta = when {
                        product?.saleBasketPrice != null && sale.quantity == 1 && sale.price == product.saleBasketPrice -> "Canasta"
                        product?.saleBasketPrice != null && sale.quantity > 1 && sale.price == (product.saleBasketPrice * sale.quantity) -> "Canasta"
                        else -> "Unitario"
                    }
                    Text("Venta: $tipoVenta", color = OrangeBright, fontWeight = FontWeight.Bold)
                    
                    // Mostrar precio por unidad y total
                    product?.let { prod ->
                        val precioPorUnidad = if (tipoVenta == "Canasta") {
                            prod.saleBasketPrice ?: 0.0
                        } else {
                            prod.salePrice
                        }
                        Text("Precio por ${if (tipoVenta == "Canasta") "canasta" else "unidad"}: ${MathUtils.formatPrice(precioPorUnidad)}", color = Color.Black)
                    }
                    Text("Total: ${MathUtils.formatPrice(sale.price)}", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                
                Spacer(Modifier.height(20.dp))
                Row {
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(containerColor = RojoAcento, contentColor = Color.White)
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Eliminar")
                    }
                    Spacer(Modifier.width(16.dp))
                    OutlinedButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Volver")
                    }
                }
            }
        }
    }
}
