package com.lealcode.inventariobillar.ui.feature.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lealcode.inventariobillar.ui.theme.*
import com.lealcode.inventariobillar.data.model.ChartFilterType
import com.lealcode.inventariobillar.ui.feature.dashboard.ProfitChart
import com.lealcode.inventariobillar.ui.feature.dashboard.ChartFilterSelector
import com.lealcode.inventariobillar.util.MathUtils

@Composable
/**
 * Pantalla principal del dashboard con metricas clave del negocio.
 */
fun DashboardScreen(
    onLogout: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val summary by viewModel.summary.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedChartFilter by viewModel.selectedChartFilter.collectAsState()
    var animateDashboard by remember { mutableStateOf(false) }
    
    // Controlar carga inicial para evitar recomposición infinita
    var hasInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animateDashboard = true
        // Eliminar llamada a loadSummary
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoClaro)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Panel de control",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = AzulAcento
                )
            )
            
            IconButton(
                onClick = onLogout,
                modifier = Modifier
                    .shadow(4.dp, CircleShape)
                    .background(Color.White, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.ExitToApp,
                    contentDescription = "Cerrar sesión",
                    tint = RojoAcento
                )
            }
        }

        AnimatedVisibility(visible = error != null, enter = fadeIn(), exit = fadeOut()) {
            error?.let { errorMessage ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, shape = MaterialTheme.shapes.medium)
                        .clip(MaterialTheme.shapes.medium),
                    colors = CardDefaults.cardColors(containerColor = RojoAcento.copy(alpha = 0.95f)),
                    border = BorderStroke(1.dp, RojoAcento)
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Error", color = Color.White, fontWeight = FontWeight.Bold)
                            Text(errorMessage, color = Color.White)
                        }
                        Button(
                            onClick = {
                                viewModel.clearError()
                                // Eliminar llamada a refreshData
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = RojoAcento)
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }
        }

        AnimatedVisibility(visible = isLoading, enter = fadeIn(), exit = fadeOut()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                CircularProgressIndicator(color = AzulAcento)
                Spacer(Modifier.height(8.dp))
                Text("Cargando datos...", color = AzulAcento, fontWeight = FontWeight.Bold)
            }
        }

        AnimatedVisibility(
            visible = !isLoading && summary.totalIncome == 0.0 && error == null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.Inventory, contentDescription = null, tint = AzulAcento, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(8.dp))
                Text("No hay datos disponibles", color = AzulAcento, fontWeight = FontWeight.Bold)
                Button(
                    onClick = { /* Eliminar llamada a refreshData */ },
                    colors = ButtonDefaults.buttonColors(containerColor = AzulAcento, contentColor = Color.White)
                ) {
                    Text("Cargar datos")
                }
            }
        }

        AnimatedVisibility(
            visible = !isLoading && error == null && animateDashboard,
            enter = fadeIn(tween(800)) + scaleIn(tween(800)),
            exit = fadeOut() + scaleOut()
        ) {
            DashboardContent(
                summary = summary,
                selectedChartFilter = selectedChartFilter,
                onFilterChanged = { viewModel.changeChartFilter(it) }
            )
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

@Composable
private fun DashboardContent(
    summary: com.lealcode.inventariobillar.data.model.DashboardSummary,
    selectedChartFilter: ChartFilterType,
    onFilterChanged: (ChartFilterType) -> Unit
) {
    val animIncome = animateFloatAsState(targetValue = 1f, animationSpec = tween(700), label = "income")
    val animExpenses = animateFloatAsState(targetValue = 1f, animationSpec = tween(900), label = "expenses")
    val animProfit = animateFloatAsState(targetValue = 1f, animationSpec = tween(1100), label = "profit")

    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            DashboardCard(
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer(scaleX = animIncome.value, scaleY = animIncome.value),
                icon = Icons.Outlined.TrendingUp,
                iconColor = VerdeAcento,
                title = "Ingresos",
                value = MathUtils.formatPrice(summary.totalIncome),
                borderColor = VerdeAcento
            )
            DashboardCard(
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer(scaleX = animExpenses.value, scaleY = animExpenses.value),
                icon = Icons.Outlined.TrendingDown,
                iconColor = RojoAcento,
                title = "Gastos",
                value = MathUtils.formatPrice(summary.totalExpenses),
                borderColor = RojoAcento
            )
            DashboardCard(
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer(scaleX = animProfit.value, scaleY = animProfit.value),
                icon = Icons.Outlined.Savings,
                iconColor = DoradoAcento,
                title = "Ganancia",
                value = MathUtils.formatPrice(summary.netProfit),
                borderColor = DoradoAcento
            )
        }

        // Selector de filtro para el gráfico
        ChartFilterSelector(
            selectedFilter = selectedChartFilter,
            onFilterChanged = onFilterChanged,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Gráfico circular de ganancias
        ProfitChart(
            chartData = summary.profitChartData,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )

        if (summary.lowStockAlerts.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, shape = MaterialTheme.shapes.medium)
                    .clip(MaterialTheme.shapes.medium),
                colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
                border = BorderStroke(1.dp, RojoAcento.copy(alpha = 0.4f))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Inventory, contentDescription = null, tint = RojoAcento, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Alertas de stock bajo",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = RojoAcento
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    summary.lowStockAlerts.forEach { product ->
                        Text("• $product", color = RojoAcento)
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, shape = MaterialTheme.shapes.medium)
                .clip(MaterialTheme.shapes.medium),
            colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
            border = BorderStroke(1.dp, AzulAcento.copy(alpha = 0.2f))
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Inventory, contentDescription = null, tint = AzulAcento, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Productos más vendidos",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = AzulAcento
                    )
                }
                Spacer(Modifier.height(8.dp))
                summary.topProducts.forEach { (name, qty) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Star, contentDescription = null, tint = DoradoAcento, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("$name: $qty vendidos", color = AzulAcento)
                    }
                }
            }
        }

    }
}

@Composable
private fun DashboardCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconColor: Color,
    title: String,
    value: String,
    borderColor: Color
) {
    Card(
        modifier = modifier
            .shadow(8.dp, shape = MaterialTheme.shapes.medium)
            .clip(MaterialTheme.shapes.medium),
        colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
        border = BorderStroke(2.dp, borderColor.copy(alpha = 0.25f))
    ) {
        Column(
            Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconColor.copy(alpha = 0.12f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold), color = iconColor)
        }
    }
}
