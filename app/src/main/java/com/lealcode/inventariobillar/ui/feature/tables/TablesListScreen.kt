package com.lealcode.inventariobillar.ui.feature.tables

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.lealcode.inventariobillar.data.model.Table
import com.lealcode.inventariobillar.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Pantalla que lista las mesas disponibles y sus acciones principales.
 */
fun TablesListScreen(
    onTableClick: (Table) -> Unit,
    onAddTable: () -> Unit,
    onStartGame: (String, String) -> Unit = { _, _ -> },
    onDeleteTable: (Table) -> Unit = {},
    viewModel: TablesViewModel = hiltViewModel()
) {
    val tables by viewModel.tables.collectAsState()
    var animateList by remember { mutableStateOf(false) }
    var showAddTableDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var tableToDelete by remember { mutableStateOf<Table?>(null) }

    LaunchedEffect(tables) {
        animateList = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mesas",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = OrangeBright
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FondoTarjeta)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTableDialog = true },
                containerColor = OrangeBright,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.shadow(12.dp, CircleShape)
            ) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = "Agregar mesa",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .background(FondoClaro)
                .padding(innerPadding)
        ) {
            // Vista vacía si no hay mesas
            AnimatedVisibility(
                visible = tables.isEmpty(),
                enter = fadeIn(tween(600)) + scaleIn(tween(600)),
                exit = fadeOut() + scaleOut()
            ) {
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Outlined.TableBar,
                        contentDescription = null,
                        tint = OrangeBright,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        "No hay mesas registradas",
                        color = OrangeBright,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Lista de mesas
            AnimatedVisibility(
                visible = tables.isNotEmpty() && animateList,
                enter = fadeIn(tween(700)) + scaleIn(tween(700)),
                exit = fadeOut() + scaleOut()
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    items(tables, key = { it.id }) { table ->
                        TableCard(
                            table = table,
                            hasSession = table.currentSessionId != null,
                            onDelete = { 
                                tableToDelete = table
                                showDeleteDialog = true
                            },
                            onStartGame = {
                                table.currentSessionId?.let { sessionId ->
                                    onStartGame(table.id, sessionId)
                                }
                            },
                            onStartSession = {
                                viewModel.startSession(table.id)
                            },
                            modifier = Modifier
                                .clickable { onTableClick(table) }
                        )
                    }
                }
            }
        }

        // Modal para agregar mesa
        if (showAddTableDialog) {
            Dialog(onDismissRequest = { showAddTableDialog = false }) {
                val animScale by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = tween(300), label = "scaleModal"
                )

                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    tonalElevation = 12.dp,
                    shadowElevation = 32.dp,
                    border = BorderStroke(2.dp, OrangeBright.copy(alpha = 0.18f)),
                    modifier = Modifier
                        .graphicsLayer(scaleX = animScale, scaleY = animScale)
                        .fillMaxWidth(0.98f)
                        .wrapContentHeight()
                        .padding(horizontal = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.verticalGradient(
                                    listOf(
                                        FondoTarjeta,
                                        FondoTarjeta.copy(alpha = 0.97f),
                                        OrangeBright.copy(alpha = 0.06f)
                                    )
                                ),
                                shape = MaterialTheme.shapes.extraLarge
                            )
                            .padding(horizontal = 0.dp)
                    ) {
                        TableFormScreen(
                            initialTable = null,
                            onSave = {
                                viewModel.addTable(it)
                                showAddTableDialog = false
                            },
                            onCancel = { showAddTableDialog = false }
                        )
                    }
                }
            }
        }
        
        // Diálogo de confirmación de eliminación
        if (showDeleteDialog && tableToDelete != null) {
            AlertDialog(
                onDismissRequest = { 
                    showDeleteDialog = false
                    tableToDelete = null
                },
                title = {
                    Text(
                        "Eliminar Mesa",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = RojoAcento
                        )
                    )
                },
                text = {
                    Text(
                        "¿Estás seguro de que quieres eliminar la mesa \"${tableToDelete?.name}\"? Esta acción no se puede deshacer.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoPrincipal
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            tableToDelete?.let { table ->
                                onDeleteTable(table)
                            }
                            showDeleteDialog = false
                            tableToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RojoAcento,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { 
                            showDeleteDialog = false
                            tableToDelete = null
                        }
                    ) {
                        Text("Cancelar")
                    }
                },
                containerColor = FondoTarjeta,
                shape = MaterialTheme.shapes.large
            )
        }
    }
}

@Composable
private fun TableCard(
    table: Table,
    hasSession: Boolean,
    onDelete: () -> Unit = {},
    onStartGame: () -> Unit = {},
    onStartSession: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val icon = Icons.Outlined.TableBar
    val iconColor = OrangeBright
    val badgeColor = if (hasSession) OrangeBright else VerdeAcento
    val badgeText = if (hasSession) "Habilitada" else "Disponible"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 90.dp)
            .shadow(8.dp, MaterialTheme.shapes.large)
            .border(1.dp, iconColor.copy(alpha = 0.15f), MaterialTheme.shapes.large),
        colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(48.dp)
                    .background(iconColor.copy(alpha = 0.12f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        table.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextoPrincipal
                        )
                    )
                    Spacer(Modifier.width(9.dp))
                    Badge(containerColor = badgeColor, contentColor = Color.White) {
                        Text(badgeText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    if (hasSession) "Habilitada para partidas" else "Disponible",
                    color = if (hasSession) OrangeBright else VerdeAcento,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón de iniciar sesión o partida
                if (hasSession) {
                    // Botón de habilitar mesa para partidas (cuando ya hay sesión)
                    IconButton(
                        onClick = onStartGame,
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = OrangeBright.copy(alpha = 0.1f),
                            contentColor = OrangeBright
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Start,
                            contentDescription = "Mesa habilitada para partidas",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    // Botón de iniciar sesión (cuando no hay sesión)
                    IconButton(
                        onClick = onStartSession,
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = VerdeAcento.copy(alpha = 0.1f),
                            contentColor = VerdeAcento
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PowerSettingsNew,
                            contentDescription = "Iniciar sesión",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Botón de eliminar
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = RojoAcento.copy(alpha = 0.1f),
                        contentColor = RojoAcento
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Eliminar mesa",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

        }
    }
}
