package com.lealcode.inventariobillar.ui.feature.tables

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import com.lealcode.inventariobillar.ui.theme.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.shadow
import com.lealcode.inventariobillar.data.model.Table

@Composable
/**
 * Resumen operativo de una mesa y su sesion.
 */
fun TableSummaryScreen(
    tableSummaries: List<Pair<Table, Double>> // Cambia a Table para mostrar tipos de juego
) {
    Box(Modifier.fillMaxSize().background(FondoClaro)) {
        Column(Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.TableBar, contentDescription = null, tint = OrangeBright, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(8.dp))
                Text("Resumen de mesas", color = OrangeBright, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
            }
            Spacer(Modifier.height(16.dp))
            tableSummaries.forEach { (table, total) ->
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(1.dp, OrangeBright.copy(alpha = 0.10f), MaterialTheme.shapes.large)
                        .shadow(4.dp, MaterialTheme.shapes.large),
                    colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(table.name, color = TextoPrincipal, fontWeight = FontWeight.Bold)
                        }
                        Text("$${total}", color = TextoPrincipal)
                    }
                }
            }
        }
    }
} 
