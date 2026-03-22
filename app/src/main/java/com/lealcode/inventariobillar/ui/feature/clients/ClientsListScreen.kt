package com.lealcode.inventariobillar.ui.feature.clients

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lealcode.inventariobillar.data.model.Client
import com.lealcode.inventariobillar.util.MathUtils
import com.lealcode.inventariobillar.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Pantalla principal del modulo de clientes.
 */
fun ClientsListScreen(
    clients: List<Client>,
    clientDebts: Map<String, Double>,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    onAddClient: () -> Unit,
    onEditClient: (Client) -> Unit,
    onDeleteClient: (Client) -> Unit,
    onClientClick: (Client) -> Unit
) {
    val displayClients = clients
    var isSearchExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoClaro)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            TopAppBar(
                title = {
                    Text(
                        "Clientes",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = PurpleFun
                        )
                    )
                },
                actions = {
                    // Icono de búsqueda en la TopAppBar
                    IconButton(
                        onClick = { 
                            isSearchExpanded = !isSearchExpanded
                            if (!isSearchExpanded) {
                                onSearchQueryChange("")
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Buscar clientes",
                            tint = PurpleFun,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FondoTarjeta)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Campo de búsqueda expandible
            if (isSearchExpanded) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .shadow(4.dp, shape = RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        placeholder = {
                            Text(
                                "Buscar por nombre o teléfono...",
                                color = GrisTextoSecundario
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Buscar",
                                tint = PurpleFun
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Clear,
                                        contentDescription = "Limpiar búsqueda",
                                        tint = GrisTextoSecundario
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurpleFun,
                            unfocusedBorderColor = GrisTextoSecundario,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(displayClients, key = { it.id }) { client ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, shape = MaterialTheme.shapes.large)
                            .background(FondoTarjeta)
                            .padding(vertical = 2.dp)
                            .clickable { onClientClick(client) },
                        colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = client.nombre,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "Tel: ${client.telefono}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GrisTextoSecundario
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                // Priorizar la deuda almacenada en el cliente si existe,
                                // en caso contrario usar la deuda calculada dinámicamente
                                val deudaAlmacenada = client.deuda
                                val deudaCalculada = clientDebts[client.id] ?: 0.0
                                val deuda = if (deudaAlmacenada > 0.0) deudaAlmacenada else deudaCalculada
                                Text(
                                    text = if (deuda > 0) "Deuda: ${MathUtils.formatPrice(deuda)}" else "Sin deuda",
                                    color = if (deuda > 0) MaterialTheme.colorScheme.error else PurpleFun,
                                    fontWeight = FontWeight.Bold
                                )
                                Row {
                                    TextButton(onClick = { onEditClient(client) }) {
                                        Text("Editar")
                                    }
                                    TextButton(onClick = { onDeleteClient(client) }) {
                                        Text("Eliminar")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddClient,
            containerColor = PurpleFun,
            contentColor = Color.White,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(12.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .shadow(12.dp, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Outlined.PersonAdd,
                contentDescription = "Agregar cliente",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
