package com.lealcode.inventariobillar.ui.feature.tables

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lealcode.inventariobillar.data.model.*
import com.lealcode.inventariobillar.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Pantalla de historico de partidas asociadas a una mesa o sesion.
 */
fun GameHistoryScreen(
    tableId: String,
    onBack: () -> Unit,
    viewModel: GameViewModel = hiltViewModel()
) {
    val gameHistory by viewModel.gameHistory.collectAsState()
    var animateList by remember { mutableStateOf(false) }


    LaunchedEffect(gameHistory) { 
        animateList = true 
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Historial de Partidas - ${viewModel.tableName}",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = OrangeBright
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FondoTarjeta)
            )
        }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(FondoClaro)
                .padding(innerPadding)
        ) {
            if (gameHistory.isEmpty()) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(700)) + scaleIn(tween(700)),
                    exit = fadeOut() + scaleOut()
                ) {
                    Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Outlined.History, 
                            contentDescription = null, 
                            tint = OrangeBright, 
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            "No hay partidas registradas", 
                            color = OrangeBright, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                AnimatedVisibility(
                    visible = gameHistory.isNotEmpty() && animateList,
                    enter = fadeIn(tween(700)) + scaleIn(tween(700)),
                    exit = fadeOut() + scaleOut()
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(gameHistory, key = { it.id }) { game ->
                            GameHistoryCard(game = game)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameHistoryCard(game: Game) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
        border = BorderStroke(1.dp, OrangeBright.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header con estado y precio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Partida #${game.id.take(8)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Badge(
                    containerColor = when (game.status) {
                        GameStatus.ACTIVE -> OrangeBright
                        GameStatus.FINISHED -> VerdeAcento
                        GameStatus.CANCELLED -> RojoAcento
                    },
                    contentColor = Color.White
                ) {
                    Text(
                        when (game.status) {
                            GameStatus.ACTIVE -> "Activa"
                            GameStatus.FINISHED -> "Finalizada"
                            GameStatus.CANCELLED -> "Cancelada"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Información de tiempo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Inicio: ${SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(game.startTime))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    if (game.endTime != null) {
                        Text(
                            "Fin: ${SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(game.endTime))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                
                Text(
                    "$${game.pricePerGame}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OrangeBright
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Información de participantes y apuestas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Participantes: ${game.participants.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    "Apuestas: ${game.bets.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            // Total de apuestas si hay
            if (game.bets.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Total apuestas: $${game.bets.sumOf { it.totalPrice }}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray
                )
            }
            
            // Total general
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Total: $${game.pricePerGame + game.bets.sumOf { it.totalPrice }}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = OrangeBright
            )
            
            // Estado de pago si la partida está finalizada
            if (game.status == GameStatus.FINISHED) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (game.isPaid) Icons.Outlined.CheckCircle else Icons.Outlined.Pending,
                        contentDescription = null,
                        tint = if (game.isPaid) VerdeAcento else RojoAcento,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (game.isPaid) "Pagado" else "Pendiente",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (game.isPaid) VerdeAcento else RojoAcento,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

