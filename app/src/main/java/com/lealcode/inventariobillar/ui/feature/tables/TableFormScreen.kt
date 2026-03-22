package com.lealcode.inventariobillar.ui.feature.tables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lealcode.inventariobillar.data.model.Table
import com.lealcode.inventariobillar.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Formulario para crear o editar mesas.
 */
fun TableFormScreen(
    initialTable: Table? = null,
    onSave: (Table) -> Unit,
    onCancel: () -> Unit
) {
    var tableName by rememberSaveable { mutableStateOf(initialTable?.name ?: "") }
    var pricePerGame by rememberSaveable { mutableStateOf(initialTable?.pricePerGame?.toString() ?: "") }
    
    // Si estamos editando, cargar los datos de la mesa
    LaunchedEffect(initialTable) {
        if (initialTable != null) {
            tableName = initialTable.name
            pricePerGame = initialTable.pricePerGame.toString()
        }
    }

    val isFormValid = tableName.trim().isNotEmpty() && pricePerGame.trim().isNotEmpty()
    val isEditMode = initialTable != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "Editar Mesa" else "Agregar Mesa",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = OrangeBright
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
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
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGradientEnd)
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // ICONO Y TÍTULO
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(OrangeBright.copy(alpha = 0.1f), CircleShape)
                        .shadow(6.dp, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.TableBar,
                        contentDescription = null,
                        tint = OrangeBright,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isEditMode) "Editar Mesa" else "Nueva Mesa",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = OrangeBright
                    )
                )
            }

            // FORMULARIO
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .defaultMinSize(minHeight = 200.dp),
                colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
                border = BorderStroke(1.dp, OrangeBright.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    OutlinedTextField(
                        value = tableName,
                        onValueChange = { tableName = it },
                        label = { Text("Nombre de la mesa") },
                        placeholder = { Text("Ej: Mesa 1, VIP, etc.") },
                        leadingIcon = {
                            Icon(Icons.Outlined.TableBar, contentDescription = null, tint = OrangeBright)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
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
                    
                    OutlinedTextField(
                        value = pricePerGame,
                        onValueChange = { pricePerGame = it },
                        label = { Text("Precio por partida") },
                        placeholder = { Text("Ej: 5000") },
                        leadingIcon = {
                            Icon(Icons.Outlined.AttachMoney, contentDescription = null, tint = OrangeBright)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
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
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // BOTONES CENTRADOS GRANDES
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .height(52.dp),
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = OrangeBright
                        ),
                        border = BorderStroke(2.dp, OrangeBright),
                        shape = MaterialTheme.shapes.large,
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Cancel,
                            contentDescription = "Cancelar",
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cancelar",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    Button(
                        onClick = {
                            val tableToSave = Table(
                                id = initialTable?.id ?: java.util.UUID.randomUUID().toString(),
                                name = tableName.trim(),
                                pricePerGame = pricePerGame.trim().toDoubleOrNull() ?: 0.0
                            )
                            onSave(tableToSave)
                        },
                        enabled = isFormValid,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangeBright,
                            contentColor = Color.White
                        ),
                        shape = MaterialTheme.shapes.large,
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = "Guardar",
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Guardar",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
