package com.lealcode.inventariobillar.ui.feature.tables

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lealcode.inventariobillar.data.model.TableSession
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import com.lealcode.inventariobillar.ui.theme.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.outlined.*
import com.lealcode.inventariobillar.data.model.Table

@Composable
/**
 * Pantalla de sesion activa de una mesa.
 */
fun TableSessionScreen(
    session: TableSession,
    table: Table, // Nuevo parámetro para mostrar tipos de juego
    onEndSession: () -> Unit,
    onStartGame: () -> Unit = {},
    onBack: () -> Unit
) {
    Box(Modifier.fillMaxSize().background(FondoClaro)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .border(1.dp, OrangeBright.copy(alpha = 0.10f), MaterialTheme.shapes.large)
                .shadow(8.dp, MaterialTheme.shapes.large),
            colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.TableBar, contentDescription = null, tint = OrangeBright, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Sesión de mesa",
                        color = OrangeBright,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(Modifier.height(8.dp))
                // Eliminar tipos de juego
                Spacer(Modifier.height(16.dp))
                Text("Inicio: ${session.startTime}", color = TextoPrincipal)
                Text("Fin: ${session.endTime ?: "En progreso"}", color = TextoPrincipal)
                Text("Total: $${session.total}", color = TextoPrincipal)
                Text("Precio por partida: $${table.pricePerGame}", color = TextoPrincipal)
                Spacer(Modifier.height(20.dp))
                
                // Botón para iniciar partida
                Button(
                    onClick = onStartGame,
                    enabled = session.endTime == null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangeBright, 
                        contentColor = Color.White,
                        disabledContainerColor = DisabledButtonContainer,
                        disabledContentColor = DisabledButtonContent
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Iniciar Partida")
                }
                
                Spacer(Modifier.height(8.dp))
                
                Button(
                    onClick = onEndSession,
                    enabled = session.endTime == null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VerdeAcento, 
                        contentColor = Color.White,
                        disabledContainerColor = DisabledButtonContainer,
                        disabledContentColor = DisabledButtonContent
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.StopCircle, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Finalizar sesión")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Volver")
                }
            }
        }
    }
} 
