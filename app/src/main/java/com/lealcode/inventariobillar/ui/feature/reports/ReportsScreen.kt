package com.lealcode.inventariobillar.ui.feature.reports

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lealcode.inventariobillar.data.model.ReportFilter
import com.lealcode.inventariobillar.data.model.ReportType
import com.lealcode.inventariobillar.ui.theme.*
import androidx.compose.foundation.border
import androidx.compose.material.icons.outlined.*
import com.lealcode.inventariobillar.util.MathUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Pantalla de configuracion y visualizacion de reportes.
 */
fun ReportsScreen(
    onReportDetail: (com.lealcode.inventariobillar.data.model.ReportResult) -> Unit,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val filter by viewModel.filter.collectAsState()
    val report by viewModel.report.collectAsState()
    var animateCard by remember { mutableStateOf(false) }

    LaunchedEffect(report) { animateCard = true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Reportes",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = AzulAcento
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FondoTarjeta
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onReportDetail(report) },
                containerColor = VerdeAcento,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(12.dp),
                modifier = Modifier.shadow(12.dp, CircleShape)
            ) {
                Icon(Icons.Outlined.BarChart, contentDescription = "Ver detalles", modifier = Modifier.size(32.dp))
            }
        }
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().background(FondoClaro).padding(innerPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Tipo:", fontWeight = FontWeight.Bold, color = AzulAcento)
                Spacer(Modifier.width(8.dp))
                ReportTypeDropdown(filter.type, onTypeSelected = {
                    viewModel.setFilter(filter.copy(type = it))
                })
            }
            Spacer(Modifier.height(16.dp))
            AnimatedVisibility(
                visible = animateCard,
                enter = fadeIn(tween(700)) + scaleIn(tween(700)),
                exit = fadeOut() + scaleOut()
            ) {
                ReportSummaryCard(reportType = filter.type, report = report)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportSummaryCard(reportType: ReportType, report: com.lealcode.inventariobillar.data.model.ReportResult) {
    val (icon, badgeColor, badgeText) = when (reportType) {
        ReportType.DAILY ->
            Triple(Icons.Outlined.Assessment, AzulAcento, "Diario")
        ReportType.WEEKLY ->
            Triple(Icons.Outlined.PieChart, PurpleFun, "Semanal")
        ReportType.MONTHLY ->
            Triple(Icons.Outlined.BarChart, OrangeBright, "Mensual")
        ReportType.CUSTOM ->
            Triple(Icons.Outlined.Assessment, PinkPop, "Personalizado")
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .shadow(8.dp, shape = MaterialTheme.shapes.large)
            .border(1.dp, badgeColor.copy(alpha = 0.15f), MaterialTheme.shapes.large),
        colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(48.dp)
                    .background(badgeColor.copy(alpha = 0.12f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(Modifier.width(20.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Resumen $badgeText",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color.Black)
                    )
                    Spacer(Modifier.width(8.dp))
                    Badge(
                        containerColor = badgeColor,
                        contentColor = Color.White
                    ) {
                        Text(badgeText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("Ventas: ${MathUtils.formatPrice(report.totalSales)}", color = VerdeAcento, fontWeight = FontWeight.Bold)
                Text("Gastos: ${MathUtils.formatPrice(report.totalExpenses)}", color = RojoAcento, fontWeight = FontWeight.Bold)
                Text("Ganancia: ${MathUtils.formatPrice(report.netProfit)}", color = AzulAcento, fontWeight = FontWeight.Bold)
                Text("Deuda clientes: ${MathUtils.formatPrice(report.totalClientDebt)}", color = PinkPop, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ReportTypeDropdown(selected: ReportType, onTypeSelected: (ReportType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(selected.name.let {
                when (it) {
                    "DAILY" -> "Diario"
                    "WEEKLY" -> "Semanal"
                    "MONTHLY" -> "Mensual"
                    "CUSTOM" -> "Personalizado"
                    else -> it
                }
            })
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ReportType.values().forEach { item ->
                DropdownMenuItem(text = { Text(item.name.let {
                    when (it) {
                        "DAILY" -> "Diario"
                        "WEEKLY" -> "Semanal"
                        "MONTHLY" -> "Mensual"
                        "CUSTOM" -> "Personalizado"
                        else -> it
                    }
                }) }, onClick = { onTypeSelected(item); expanded = false })
            }
        }
    }
}

// Utilidad para destructuración de 4 valores
private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D) 
