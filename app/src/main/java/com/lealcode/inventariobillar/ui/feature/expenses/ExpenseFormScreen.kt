package com.lealcode.inventariobillar.ui.feature.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lealcode.inventariobillar.data.model.Expense
import com.lealcode.inventariobillar.ui.theme.*
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons

@Composable
/**
 * Formulario para crear o editar gastos.
 */
fun ExpenseFormScreen(
    initialExpense: Expense? = null,
    onSave: (Expense) -> Unit,
    onCancel: () -> Unit
) {
    var description by remember { mutableStateOf(initialExpense?.description ?: "") }
    var amount by remember { mutableStateOf(initialExpense?.amount?.toString() ?: "") }
    var category by remember { mutableStateOf(initialExpense?.category ?: "General") }

    Box(Modifier.fillMaxSize().background(FondoClaro)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .border(1.dp, RojoAcento.copy(alpha = 0.10f), MaterialTheme.shapes.large)
                .shadow(8.dp, MaterialTheme.shapes.large),
            colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.ReceiptLong, contentDescription = null, tint = RojoAcento, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (initialExpense == null) "Agregar gasto" else "Editar gasto",
                        color = RojoAcento,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        errorTextColor = Color.Black,
                        focusedBorderColor = RojoAcento,
                        unfocusedBorderColor = RojoAcento.copy(alpha = 0.5f),
                        focusedLabelColor = RojoAcento,
                        unfocusedLabelColor = Color.Black.copy(alpha = 0.6f)
                    )
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Monto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        errorTextColor = Color.Black,
                        focusedBorderColor = RojoAcento,
                        unfocusedBorderColor = RojoAcento.copy(alpha = 0.5f),
                        focusedLabelColor = RojoAcento,
                        unfocusedLabelColor = Color.Black.copy(alpha = 0.6f)
                    )
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoría") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = RojoAcento,
                        unfocusedBorderColor = RojoAcento.copy(alpha = 0.5f),
                        focusedLabelColor = RojoAcento,
                        unfocusedLabelColor = Color.Black.copy(alpha = 0.6f)
                    )
                )
                Spacer(Modifier.height(20.dp))
                Row {
                    Button(
                        onClick = {
                            val expense = Expense(
                                id = initialExpense?.id ?: java.util.UUID.randomUUID().toString(),
                                description = description,
                                amount = amount.toDoubleOrNull() ?: 0.0,
                                category = category,
                            )
                            onSave(expense)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RojoAcento, contentColor = Color.White)
                    ) {
                        Icon(Icons.Outlined.Check, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Guardar")
                    }
                    Spacer(Modifier.width(16.dp))
                    OutlinedButton(onClick = onCancel) {
                        Icon(Icons.Outlined.Close, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Cancelar")
                    }
                }
            }
        }
    }
} 
