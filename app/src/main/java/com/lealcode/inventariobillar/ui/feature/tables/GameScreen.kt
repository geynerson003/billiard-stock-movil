package com.lealcode.inventariobillar.ui.feature.tables

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.lealcode.inventariobillar.data.model.*
import com.lealcode.inventariobillar.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Pantalla de gestion de una partida en curso.
 */
fun GameScreen(
    tableId: String,
    sessionId: String,
    onBack: () -> Unit,
    viewModel: GameViewModel = hiltViewModel()
) {
    val game by viewModel.currentGame.collectAsState()
    val clients by viewModel.clients.collectAsState()
    val products by viewModel.products.collectAsState()
    var showAddParticipantDialog by remember { mutableStateOf(false) }
    var showAddBetDialog by remember { mutableStateOf(false) }
    var showFinishGameDialog by remember { mutableStateOf(false) }
    var showRestartGameDialog by remember { mutableStateOf(false) }

    LaunchedEffect(tableId, sessionId) {
        viewModel.loadGame(tableId, sessionId)
        viewModel.loadClients()
        viewModel.loadProducts()
        // NO cargar partidas activas automáticamente - solo cuando el usuario inicie una partida
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Partida en ${viewModel.tableName}",
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
                            contentDescription = "Volver",
                            tint = TextoPrincipal
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FondoTarjeta)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(FondoClaro)
        ) {
            // Estado de la partida
            game?.let { currentGame ->
                item {
                    GameStatusCard(currentGame)
                }
                
                // Participantes
                item {
                    ParticipantsSection(
                        participants = currentGame.participants,
                        clients = clients,
                        onAddParticipant = { showAddParticipantDialog = true },
                        onRemoveParticipant = { clientId ->
                            viewModel.removeParticipant(currentGame.id, clientId)
                        }
                    )
                }
                
                // Apuestas
                if (currentGame.participants.isNotEmpty()) {
                    item {
                        BetsSection(
                            bets = currentGame.bets,
                            products = products,
                            onAddBet = { showAddBetDialog = true },
                            onRemoveBet = { betIndex ->
                                viewModel.removeBet(currentGame.id, betIndex)
                            }
                        )
                    }
                }
                
                // Resumen
                item {
                    GameSummaryCard(currentGame)
                }
            }
            
            // Espacio flexible
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Botones del reproductor en paralelo
            item {
                GameControlButtons(
                    game = game,
                    onStartGame = { viewModel.startGame(tableId, sessionId) },
                    onEndGame = { showFinishGameDialog = true },
                    onRestartGame = { showRestartGameDialog = true }
                )
            }
            
            // Espacio adicional al final para evitar que el contenido se corte
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Diálogo para agregar participantes
        if (showAddParticipantDialog) {
            AddParticipantDialog(
                clients = clients,
                currentParticipants = game?.participants ?: emptyList(),
                onAddParticipants = { selectedClients ->
                    game?.let { currentGame ->
                        viewModel.addParticipants(currentGame.id, selectedClients)
                    }
                    showAddParticipantDialog = false
                },
                onDismiss = { showAddParticipantDialog = false }
            )
        }

        // Diálogo para agregar apuesta
        if (showAddBetDialog) {
            AddBetDialog(
                products = products,
                participants = game?.participants ?: emptyList(),
                onAddBet = { bet ->
                    game?.let { currentGame ->
                        viewModel.addBet(currentGame.id, bet)
                    }
                    showAddBetDialog = false
                },
                onDismiss = { showAddBetDialog = false }
            )
        }

        // Diálogo para finalizar partida
        if (showFinishGameDialog) {
            FinishGameDialog(
                game = game,
                onFinishGame = { loserIds, isPaid ->
                    game?.let { currentGame ->
                        viewModel.endGame(currentGame.id, loserIds, isPaid)
                    }
                    showFinishGameDialog = false
                },
                onDismiss = { showFinishGameDialog = false }
            )
        }

        // Diálogo para reiniciar partida
        if (showRestartGameDialog) {
            RestartGameDialog(
                onRestart = {
                    viewModel.restartGame(tableId, sessionId)
                    showRestartGameDialog = false
                },
                onDismiss = { showRestartGameDialog = false }
            )
        }
    }
}

@Composable
private fun GameStatusCard(game: Game) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
        border = BorderStroke(1.dp, OrangeBright.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Estado: ${game.status.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when (game.status) {
                        GameStatus.ACTIVE -> OrangeBright
                        GameStatus.FINISHED -> VerdeAcento
                        GameStatus.CANCELLED -> RojoAcento
                    }
                )
                
                Text(
                    "Precio: $${game.pricePerGame}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OrangeBright
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Inicio: ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(game.startTime))}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            
            if (game.endTime != null) {
                Text(
                    "Fin: ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(game.endTime))}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun ParticipantsSection(
    participants: List<GameParticipant>,
    clients: List<Client>,
    onAddParticipant: () -> Unit,
    onRemoveParticipant: (String) -> Unit
) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Participantes (${participants.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextoPrincipal
                )
                
                IconButton(onClick = onAddParticipant) {
                    Icon(
                        Icons.Outlined.Add,
                        contentDescription = "Agregar participante",
                        tint = OrangeBright
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (participants.isEmpty()) {
                Text(
                    "No hay participantes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            } else {
                participants.forEach { participant ->
                    val client = clients.find { it.id == participant.clientId }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            client?.nombre ?: participant.clientName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextoPrincipal
                        )
                        
                        IconButton(
                            onClick = { onRemoveParticipant(participant.clientId) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Remove,
                                contentDescription = "Remover participante",
                                tint = RojoAcento,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BetsSection(
    bets: List<GameBet>,
    products: List<Product>,
    onAddBet: () -> Unit,
    onRemoveBet: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
        border = BorderStroke(1.dp, OrangeBright.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Apuestas (${bets.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextoPrincipal
                )
                
                IconButton(onClick = onAddBet) {
                    Icon(
                        Icons.Outlined.Add,
                        contentDescription = "Agregar apuesta",
                        tint = OrangeBright
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (bets.isEmpty()) {
                Text(
                    "No hay apuestas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            } else {
                bets.forEachIndexed { index, bet ->
                    val product = products.find { it.id == bet.productId }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                product?.name ?: bet.productName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextoPrincipal
                            )
                            Text(
                                "Cantidad: ${bet.quantity} - $${bet.totalPrice}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        
                        IconButton(
                            onClick = { onRemoveBet(index) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Remove,
                                contentDescription = "Remover apuesta",
                                tint = RojoAcento,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameSummaryCard(game: Game) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
        border = BorderStroke(1.dp, OrangeBright.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Resumen",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextoPrincipal
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Precio por partida:", color = TextoPrincipal)
                Text("$${game.pricePerGame}", fontWeight = FontWeight.Bold, color = TextoPrincipal)
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total apuestas:", color = TextoPrincipal)
                Text("$${game.bets.sumOf { it.totalPrice }}", fontWeight = FontWeight.Bold, color = TextoPrincipal)
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BordeTarjeta)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total:", fontWeight = FontWeight.Bold, color = TextoPrincipal)
                Text(
                    "$${game.pricePerGame + game.bets.sumOf { it.totalPrice }}",
                    fontWeight = FontWeight.Bold,
                    color = OrangeBright
                )
            }
        }
    }
}

@Composable
private fun AddParticipantDialog(
    clients: List<Client>,
    currentParticipants: List<GameParticipant>,
    onAddParticipants: (List<Client>) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedClients by remember { mutableStateOf(setOf<String>()) }
    
    // Log para debuggear
    LaunchedEffect(clients, currentParticipants) {
        android.util.Log.d("AddParticipantDialog", "=== DIÁLOGO AGREGAR PARTICIPANTES ===")
        android.util.Log.d("AddParticipantDialog", "Total de clientes disponibles: ${clients.size}")
        android.util.Log.d("AddParticipantDialog", "Participantes actuales: ${currentParticipants.size}")
        android.util.Log.d("AddParticipantDialog", "Clientes disponibles después del filtro: ${clients.filter { client -> !currentParticipants.any { it.clientId == client.id } }.size}")
        android.util.Log.d("AddParticipantDialog", "==========================================")
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp),
            colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Agregar Participantes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = OrangeBright
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "Selecciona los clientes que quieres agregar como participantes:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                val availableClients = clients.filter { client ->
                    !currentParticipants.any { it.clientId == client.id }
                }
                
                if (availableClients.isEmpty()) {
                    Text(
                        "No hay clientes disponibles",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(availableClients) { client ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        selectedClients = if (selectedClients.contains(client.id)) {
                                            selectedClients - client.id
                                        } else {
                                            selectedClients + client.id
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedClients.contains(client.id),
                                    onCheckedChange = { checked ->
                                        selectedClients = if (checked) {
                                            selectedClients + client.id
                                        } else {
                                            selectedClients - client.id
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = OrangeBright,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    client.nombre,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextoPrincipal
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Contador de seleccionados
                if (selectedClients.isNotEmpty()) {
                    Text(
                        "${selectedClients.size} cliente(s) seleccionado(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = OrangeBright,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val selectedClientList = clients.filter { it.id in selectedClients }
                            onAddParticipants(selectedClientList)
                        },
                        enabled = selectedClients.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeBright)
                    ) {
                        Text("Agregar ${selectedClients.size} participante(s)")
                    }
                }
            }
        }
    }
}

@Composable
private fun AddBetDialog(
    products: List<Product>,
    participants: List<GameParticipant>,
    onAddBet: (GameBet) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var quantity by remember { mutableStateOf("1") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Agregar Apuesta",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = OrangeBright
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Selector de producto
                Text(
                    "Producto:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextoPrincipal
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(
                    modifier = Modifier.height(300.dp)
                ) {
                    items(products) { product ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedProduct = product }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedProduct?.id == product.id,
                                onClick = { selectedProduct = product }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    product.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextoPrincipal
                                )
                                Text(
                                    "$${product.salePrice}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Cantidad
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Cantidad") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangeBright,
                        unfocusedBorderColor = OrangeBright.copy(alpha = 0.5f),
                        focusedLabelColor = OrangeBright
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val qty = quantity.toIntOrNull() ?: 1
                            val product = selectedProduct
                            
                            if (product != null) {
                                // Crear apuesta sin asignar a ningún participante específico
                                val bet = GameBet(
                                    productId = product.id,
                                    productName = product.name,
                                    quantity = qty,
                                    unitPrice = product.salePrice,
                                    totalPrice = product.salePrice * qty,
                                    betByClientIds = emptyList() // Se asignará al finalizar la partida
                                )
                                onAddBet(bet)
                                onDismiss() // Cerrar el diálogo después de agregar la apuesta
                            }
                        },
                        enabled = selectedProduct != null,
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeBright)
                    ) {
                        Text("Aceptar")
                    }
                }
            }
        }
    }
}

@Composable
private fun FinishGameDialog(
    game: Game?,
    onFinishGame: (List<String>, Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    if (game == null) return

    var selectedLosers by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isPaid by remember { mutableStateOf(false) }

    val participants = game.participants
    val bets = game.bets
    val pricePerGame = game.pricePerGame

    // ✅ Ahora el botón se habilita también si pricePerGame > 0 o hay apuestas
    val canFinish = selectedLosers.isNotEmpty() || pricePerGame > 0.0 || bets.isNotEmpty()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Finalizar Partida",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = OrangeBright
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Resumen
                    item {
                        Column {
                            Text("Resumen:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextoPrincipal)
                            Text("Precio por partida: $${pricePerGame}", color = TextoPrincipal)
                            Text("Total apuestas: $${bets.sumOf { it.totalPrice }}", color = TextoPrincipal)
                            Text("Total: $${pricePerGame + bets.sumOf { it.totalPrice }}", color = TextoPrincipal)
                        }
                    }

                    // Solo si hay participantes
                    if (participants.isNotEmpty()) {
                        item {
                            Column {
                                Text("Perdedores:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextoPrincipal)
                                Text(
                                    "La cuenta se dividirá equitativamente entre los perdedores seleccionados",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }

                        items(participants) { participant ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedLosers = if (selectedLosers.contains(participant.clientId)) {
                                            selectedLosers - participant.clientId
                                        } else {
                                            selectedLosers + participant.clientId
                                        }
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedLosers.contains(participant.clientId),
                                    onCheckedChange = { checked ->
                                        selectedLosers = if (checked) {
                                            selectedLosers + participant.clientId
                                        } else {
                                            selectedLosers - participant.clientId
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(participant.clientName, style = MaterialTheme.typography.bodyMedium, color = TextoPrincipal)
                            }
                        }

                        if (selectedLosers.isNotEmpty()) {
                            item {
                                val totalAmount = pricePerGame + bets.sumOf { it.totalPrice }
                                val amountPerLoser = totalAmount / selectedLosers.size
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = OrangeBright.copy(alpha = 0.1f)),
                                    border = BorderStroke(1.dp, OrangeBright.copy(alpha = 0.3f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("División de Cuenta:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = OrangeBright)
                                        Text("Total: $${totalAmount}", color = TextoPrincipal)
                                        Text("Perdedores: ${selectedLosers.size}", color = TextoPrincipal)
                                        Text("Monto por perdedor: $${amountPerLoser}", color = TextoPrincipal)
                                    }
                                }
                            }
                        }
                    }

                    // Checkbox de pago - mostrar siempre que haya un monto a pagar
                    if (pricePerGame > 0.0 || bets.isNotEmpty()) {
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = isPaid, onCheckedChange = { isPaid = it })
                                Spacer(modifier = Modifier.width(8.dp))
                                val paymentText = when {
                                    bets.isNotEmpty() && pricePerGame > 0.0 -> "¿El total ya fue pagado?"
                                    bets.isNotEmpty() -> "¿Las apuestas ya fueron pagadas?"
                                    else -> "¿La partida ya fue pagada?"
                                }
                                Text(paymentText, style = MaterialTheme.typography.bodyMedium, color = TextoPrincipal)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onFinishGame(selectedLosers.toList(), isPaid) },
                        enabled = canFinish, // ✅ ya no depende solo de selectedLosers
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeBright)
                    ) {
                        Text("Aceptar")
                    }
                }
            }
        }
    }
}

@Composable
private fun RestartGameDialog(
    onRestart: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 200.dp),
            colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "¿Estás seguro de que quieres reiniciar la partida?",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = TextoPrincipal
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onRestart,
                        colors = ButtonDefaults.buttonColors(containerColor = RojoAcento)
                    ) {
                        Text("Reiniciar")
                    }
                }
            }
        }
    }
}

@Composable
private fun GameControlButtons(
    game: Game?,
    onStartGame: () -> Unit,
    onEndGame: () -> Unit,
    onRestartGame: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
        border = BorderStroke(1.dp, OrangeBright.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Control de Partida",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OrangeBright
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Tres botones en paralelo como un reproductor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Botón Iniciar Partida
                Button(
                    onClick = onStartGame,
                    enabled = game == null || game.status != GameStatus.ACTIVE,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VerdeAcento,
                        contentColor = Color.White,
                        disabledContainerColor = DisabledButtonContainer,
                        disabledContentColor = DisabledButtonContent
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Outlined.PlayArrow,
                            contentDescription = "Iniciar partida",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Iniciar",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Botón Terminar Partida
                Button(
                    onClick = onEndGame,
                    enabled = game?.status == GameStatus.ACTIVE,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RojoAcento,
                        contentColor = Color.White,
                        disabledContainerColor = DisabledButtonContainer,
                        disabledContentColor = DisabledButtonContent
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Outlined.Stop,
                            contentDescription = "Terminar partida",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Terminar",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Botón Reiniciar Partida
                Button(
                    onClick = onRestartGame,
                    enabled = game?.status == GameStatus.ACTIVE,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangeBright,
                        contentColor = Color.White,
                        disabledContainerColor = DisabledButtonContainer,
                        disabledContentColor = DisabledButtonContent
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Outlined.Refresh,
                            contentDescription = "Reiniciar partida",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Reiniciar",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Estado actual de la partida
            Text(
                "Estado: ${game?.status?.name ?: "Sin partida"}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
