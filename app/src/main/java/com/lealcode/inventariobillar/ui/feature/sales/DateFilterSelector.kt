package com.lealcode.inventariobillar.ui.feature.sales

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lealcode.inventariobillar.data.model.DateFilterType
import com.lealcode.inventariobillar.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Selector visual de rangos de fecha para el listado de ventas.
 */
fun DateFilterSelector(
    selectedFilter: DateFilterType,
    onFilterSelected: (DateFilterType, Long?, Long?) -> Unit,
    customStartDate: Long? = null,
    customEndDate: Long? = null,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showCustomDateDialog by remember { mutableStateOf(false) }
    
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(300)
    )
    
    Column(modifier = modifier) {
        // Botón principal del filtro
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = when (selectedFilter) {
                            DateFilterType.TODAY -> Icons.Outlined.Today
                            DateFilterType.WEEK -> Icons.Outlined.DateRange
                            DateFilterType.MONTH -> Icons.Outlined.CalendarMonth
                            DateFilterType.CUSTOM -> Icons.Outlined.EditCalendar
                        },
                        contentDescription = null,
                        tint = OrangeBright,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Column {
                        Text(
                            text = "Filtro de fecha",
                            style = MaterialTheme.typography.bodySmall,
                            color = GrisTextoSecundario
                        )
                        Text(
                            text = when (selectedFilter) {
                                DateFilterType.TODAY -> "Hoy"
                                DateFilterType.WEEK -> "Esta semana"
                                DateFilterType.MONTH -> "Este mes"
                                DateFilterType.CUSTOM -> {
                                    if (customStartDate != null && customEndDate != null) {
                                        "${formatDate(customStartDate)} - ${formatDate(customEndDate)}"
                                    } else {
                                        "Personalizado"
                                    }
                                }
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        )
                    }
                }
                
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowDown,
                    contentDescription = "Expandir filtro",
                    tint = OrangeBright,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotation)
                )
            }
        }
        
        // Menú desplegable
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(
                animationSpec = tween(300),
                expandFrom = Alignment.Top
            ) + fadeIn(animationSpec = tween(300)),
            exit = shrinkVertically(
                animationSpec = tween(300),
                shrinkTowards = Alignment.Top
            ) + fadeOut(animationSpec = tween(300))
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Opción: Hoy
                    FilterOption(
                        icon = Icons.Outlined.Today,
                        title = "Hoy",
                        subtitle = "Ventas del día actual",
                        isSelected = selectedFilter == DateFilterType.TODAY,
                        onClick = {
                            onFilterSelected(DateFilterType.TODAY, null, null)
                            expanded = false
                        }
                    )
                    
                    // Opción: Esta semana
                    FilterOption(
                        icon = Icons.Outlined.DateRange,
                        title = "Esta semana",
                        subtitle = "Ventas de la semana actual",
                        isSelected = selectedFilter == DateFilterType.WEEK,
                        onClick = {
                            onFilterSelected(DateFilterType.WEEK, null, null)
                            expanded = false
                        }
                    )
                    
                    // Opción: Este mes
                    FilterOption(
                        icon = Icons.Outlined.CalendarMonth,
                        title = "Este mes",
                        subtitle = "Ventas del mes actual",
                        isSelected = selectedFilter == DateFilterType.MONTH,
                        onClick = {
                            onFilterSelected(DateFilterType.MONTH, null, null)
                            expanded = false
                        }
                    )
                    
                    // Opción: Personalizado
                    FilterOption(
                        icon = Icons.Outlined.EditCalendar,
                        title = "Personalizado",
                        subtitle = "Seleccionar fechas específicas",
                        isSelected = selectedFilter == DateFilterType.CUSTOM,
                        onClick = {
                            showCustomDateDialog = true
                            expanded = false
                        }
                    )
                }
            }
        }
    }
    
    // Diálogo para fechas personalizadas
    if (showCustomDateDialog) {
        CustomDateRangeDialog(
            onDismiss = { showCustomDateDialog = false },
            onDateRangeSelected = { startDate, endDate ->
                onFilterSelected(DateFilterType.CUSTOM, startDate, endDate)
                showCustomDateDialog = false
            }
        )
    }
}

@Composable
private fun FilterOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp)
            .background(
                color = if (isSelected) OrangeBright.copy(alpha = 0.1f) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) OrangeBright else GrisTextoSecundario,
            modifier = Modifier.size(20.dp)
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) OrangeBright else Color.Black
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = GrisTextoSecundario
            )
        }
        
        if (isSelected) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = "Seleccionado",
                tint = OrangeBright,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun CustomDateRangeDialog(
    onDismiss: () -> Unit,
    onDateRangeSelected: (Long, Long) -> Unit
) {
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    var startDateText by remember { mutableStateOf("") }
    var endDateText by remember { mutableStateOf("") }
    var showStartDateError by remember { mutableStateOf(false) }
    var showEndDateError by remember { mutableStateOf(false) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Título
                Text(
                    text = "Seleccionar rango de fechas",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = OrangeBright
                    )
                )
                
                // Información de ayuda
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AzulAcento.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "💡 Formato de fecha: DD/MM/YYYY",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AzulAcento
                            )
                        )
                        Text(
                            text = "Ejemplo: 15/12/2024",
                            style = MaterialTheme.typography.bodySmall,
                            color = AzulAcento
                        )
                    }
                }
                
                // Campo de fecha de inicio
                Column {
                    Text(
                        text = "Fecha de inicio:",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = startDateText,
                        onValueChange = { 
                            startDateText = it
                            showStartDateError = false
                            startDate = null
                        },
                        label = { Text("DD/MM/YYYY") },
                        placeholder = { Text("Ej: 01/12/2024") },
                        isError = showStartDateError,
                        supportingText = {
                            if (showStartDateError) {
                                Text(
                                    "Formato incorrecto. Use DD/MM/YYYY",
                                    color = RojoAcento
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangeBright,
                            unfocusedBorderColor = GrisTextoSecundario,
                            errorBorderColor = RojoAcento
                        ),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        )
                    )
                    
                    // Botones rápidos para fecha de inicio
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Button(
                            onClick = { 
                                val calendar = Calendar.getInstance()
                                calendar.add(Calendar.DAY_OF_YEAR, -7)
                                startDateText = formatDate(calendar.timeInMillis)
                                startDate = calendar.timeInMillis
                                showStartDateError = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AzulAcento),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Hace 7 días")
                        }
                        Button(
                            onClick = { 
                                val calendar = Calendar.getInstance()
                                calendar.add(Calendar.DAY_OF_YEAR, -30)
                                startDateText = formatDate(calendar.timeInMillis)
                                startDate = calendar.timeInMillis
                                showStartDateError = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VerdeAcento),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Hace 30 días")
                        }
                    }
                }
                
                // Campo de fecha de fin
                Column {
                    Text(
                        text = "Fecha de fin:",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = endDateText,
                        onValueChange = { 
                            endDateText = it
                            showEndDateError = false
                            endDate = null
                        },
                        label = { Text("DD/MM/YYYY") },
                        placeholder = { Text("Ej: 31/12/2024") },
                        isError = showEndDateError,
                        supportingText = {
                            if (showEndDateError) {
                                Text(
                                    "Formato incorrecto. Use DD/MM/YYYY",
                                    color = RojoAcento
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangeBright,
                            unfocusedBorderColor = GrisTextoSecundario,
                            errorBorderColor = RojoAcento
                        ),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        )
                    )
                    
                    // Botones rápidos para fecha de fin
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Button(
                            onClick = { 
                                val calendar = Calendar.getInstance()
                                endDateText = formatDate(calendar.timeInMillis)
                                endDate = calendar.timeInMillis
                                showEndDateError = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeBright),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Hoy")
                        }
                        Button(
                            onClick = { 
                                val calendar = Calendar.getInstance()
                                calendar.add(Calendar.DAY_OF_YEAR, 7)
                                endDateText = formatDate(calendar.timeInMillis)
                                endDate = calendar.timeInMillis
                                showEndDateError = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RojoAcento),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("En 7 días")
                        }
                    }
                }
                
                // Fechas seleccionadas
                if ((startDate != null || startDateText.isNotBlank()) || (endDate != null || endDateText.isNotBlank())) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = OrangeBright.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Rango seleccionado:",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OrangeBright
                                )
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (startDate != null || startDateText.isNotBlank()) {
                                    Text(
                                        "Desde: ${if (startDate != null) formatDate(startDate!!) else startDateText}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OrangeBright
                                    )
                                }
                                if (endDate != null || endDateText.isNotBlank()) {
                                    Text(
                                        "Hasta: ${if (endDate != null) formatDate(endDate!!) else endDateText}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OrangeBright
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            // Validar y procesar fechas
                            val finalStartDate = startDate ?: parseDate(startDateText)
                            val finalEndDate = endDate ?: parseDate(endDateText)
                            
                            if (finalStartDate != null && finalEndDate != null) {
                                if (finalStartDate <= finalEndDate) {
                                    onDateRangeSelected(finalStartDate, finalEndDate)
                                } else {
                                    showStartDateError = true
                                    showEndDateError = true
                                }
                            } else {
                                if (finalStartDate == null && startDateText.isNotBlank()) {
                                    showStartDateError = true
                                }
                                if (finalEndDate == null && endDateText.isNotBlank()) {
                                    showEndDateError = true
                                }
                            }
                        },
                        enabled = (startDate != null || startDateText.isNotBlank()) && 
                                 (endDate != null || endDateText.isNotBlank()),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeBright),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Confirmar")
                    }
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}

private fun parseDate(dateText: String): Long? {
    return try {
        val parts = dateText.split("/")
        if (parts.size == 3) {
            val day = parts[0].toInt()
            val month = parts[1].toInt() - 1 // Calendar.MONTH es 0-based
            val year = parts[2].toInt()
            
            if (day in 1..31 && month in 0..11 && year >= 1900) {
                val calendar = Calendar.getInstance()
                calendar.set(year, month, day, 0, 0, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            } else {
                val endDate = Calendar.getInstance()
                endDate.set(year, month, day, 23, 59, 59)
                endDate.set(Calendar.MILLISECOND, 999)
                endDate.timeInMillis
            }
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}
