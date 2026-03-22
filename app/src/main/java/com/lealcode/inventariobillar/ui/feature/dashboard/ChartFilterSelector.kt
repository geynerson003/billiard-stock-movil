 package com.lealcode.inventariobillar.ui.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lealcode.inventariobillar.data.model.ChartFilterType
import com.lealcode.inventariobillar.ui.theme.AzulAcento

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Selector de periodicidad para los graficos del dashboard.
 */
fun ChartFilterSelector(
    selectedFilter: ChartFilterType,
    onFilterChanged: (ChartFilterType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        Text(
            text = "Filtrar por:",
            style = MaterialTheme.typography.titleSmall,
            color = AzulAcento
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = getFilterTypeText(selectedFilter),
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AzulAcento,
                    unfocusedBorderColor = AzulAcento.copy(alpha = 0.5f)
                )
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                // Solo mostrar las opciones que queremos (sin DAILY)
                listOf(ChartFilterType.WEEKLY, ChartFilterType.MONTHLY, ChartFilterType.YEARLY).forEach { filterType ->
                    DropdownMenuItem(
                        text = { Text(getFilterTypeText(filterType)) },
                        onClick = {
                            onFilterChanged(filterType)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun getFilterTypeText(filterType: ChartFilterType): String {
    return when (filterType) {
        ChartFilterType.WEEKLY -> "Semana"
        ChartFilterType.MONTHLY -> "Mes"
        ChartFilterType.YEARLY -> "Año"
        else -> "Semana" // Fallback para DAILY si existe
    }
} 
