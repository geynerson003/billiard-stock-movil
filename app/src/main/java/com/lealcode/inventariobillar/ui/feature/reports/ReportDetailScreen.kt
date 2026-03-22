package com.lealcode.inventariobillar.ui.feature.reports

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
import com.lealcode.inventariobillar.data.model.ReportResult
import com.lealcode.inventariobillar.ui.theme.*
import com.lealcode.inventariobillar.util.MathUtils
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons

@Composable
/**
 * Pantalla de detalle para un reporte ya generado.
 */
fun ReportDetailScreen(
    report: ReportResult,
    onBack: () -> Unit
) {
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
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Assessment, contentDescription = null, tint = AzulAcento, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Detalle de reporte", color = AzulAcento, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                }
                Spacer(Modifier.height(16.dp))
                Text("Ventas por mesa:", color = GrisTextoSecundario)
                report.salesByTable.forEach { (table, total) ->
                    Text("$table: ${MathUtils.formatPrice(total)}", color = Color.Black)
                }
                Spacer(Modifier.height(8.dp))
                Text("Ventas por producto:", color = GrisTextoSecundario)
                report.salesByProduct.forEach { (product, total) ->
                    Text("$product: ${MathUtils.formatPrice(total)}", color = Color.Black)
                }
                Spacer(Modifier.height(8.dp))
                Text("Productos más vendidos:", color = GrisTextoSecundario)
                report.topProducts.forEach { (product, qty) ->
                    Text("$product: $qty vendidos", color = Color.Black)
                }
                Spacer(Modifier.height(20.dp))
                OutlinedButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Volver")
                }
            }
        }
    }
} 
