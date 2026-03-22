package com.lealcode.inventariobillar.ui.feature.clients

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lealcode.inventariobillar.data.model.Client
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import com.lealcode.inventariobillar.ui.theme.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Check

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Formulario para crear o editar clientes.
 */
fun ClientFormScreen(
    onSave: (Client) -> Unit,
    onCancel: () -> Unit,
    initialClient: Client? = null
) {
    var nombre by remember { mutableStateOf(TextFieldValue(initialClient?.nombre ?: "")) }
    var telefono by remember { mutableStateOf(TextFieldValue(initialClient?.telefono ?: "")) }
    // Elimino el campo de deuda

    Box(Modifier.fillMaxSize().background(FondoClaro)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .shadow(8.dp, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.PersonAdd, contentDescription = null, tint = PurpleFun, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (initialClient == null) "Agregar Cliente" else "Editar Cliente",
                        color = PurpleFun,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        errorTextColor = Color.Black,
                        focusedBorderColor = PurpleFun,
                        unfocusedBorderColor = PurpleFun.copy(alpha = 0.5f),
                        focusedLabelColor = PurpleFun,
                        unfocusedLabelColor = Color.Black.copy(alpha = 0.6f)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = PurpleFun,
                        unfocusedBorderColor = PurpleFun.copy(alpha = 0.5f),
                        focusedLabelColor = PurpleFun,
                        unfocusedLabelColor = Color.Black.copy(alpha = 0.6f)
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onCancel) {
                        Icon(Icons.Outlined.Close, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(onClick = {
                        val client = Client(
                            id = initialClient?.id ?: "",
                            nombre = nombre.text,
                            telefono = telefono.text,
                            deuda = 0.0 // Siempre 0.0, ya no se edita aquí
                        )
                        onSave(client)
                    }, colors = ButtonDefaults.buttonColors(containerColor = PurpleFun, contentColor = Color.White)) {
                        Icon(Icons.Outlined.Check, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Guardar")
                    }
                }
            }
        }
    }
} 
