package com.lealcode.inventariobillar.ui.feature.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.lealcode.inventariobillar.data.model.ChartData
import com.lealcode.inventariobillar.data.model.ChartFilterType
import com.lealcode.inventariobillar.data.model.ProfitChartData
import com.lealcode.inventariobillar.ui.theme.*
import androidx.compose.ui.unit.dp
import com.lealcode.inventariobillar.util.MathUtils

@Composable
/**
 * Grafico de ganancias usado por el dashboard principal.
 */
fun ProfitChart(
    chartData: ProfitChartData?,
    modifier: Modifier = Modifier
) {
    if (chartData == null || chartData.data.isEmpty()) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(300.dp),
            colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
            border = BorderStroke(1.dp, AzulAcento.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay datos disponibles para el gráfico",
                    color = AzulAcento,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        return
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp),
        colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
        border = BorderStroke(1.dp, AzulAcento.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            // Título
            Text(
                text = "Ganancias por ${getFilterTypeText(chartData.filterType)}",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = AzulAcento,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Gráfico de barras
            BarChart(
                data = chartData.data,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
        }
    }
}

@Composable
private fun BarChart(
    data: List<ChartData>,
    modifier: Modifier = Modifier
) {
    val maxValue = data.maxOfOrNull { it.value } ?: 0.0
    
    Column(modifier = modifier) {
        // Gráfico sin scroll
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEachIndexed { index, item ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    // Barra del gráfico
                    BarItem(
                        data = item,
                        maxValue = maxValue,
                        colorIndex = index,
                        modifier = Modifier
                            .width(20.dp)
                            .height(140.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Etiqueta
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = AzulAcento,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun BarItem(
    data: ChartData,
    maxValue: Double,
    colorIndex: Int,
    modifier: Modifier = Modifier
) {
    val animatedHeight by animateFloatAsState(
        targetValue = if (maxValue > 0) (data.value / maxValue).toFloat() else 0f,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "barHeight"
    )
    
    // Colores diferentes para cada barra
    val barColor = when (colorIndex % 6) {
        0 -> VerdeAcento
        1 -> AzulAcento
        2 -> DoradoAcento
        3 -> RojoAcento
        4 -> PurpleFun
        5 -> OrangeBright
        else -> VerdeAcento
    }
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomCenter
    ) {
        // Barra con gradiente y sombra
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height((140 * animatedHeight).dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            barColor,
                            barColor.copy(alpha = 0.7f)
                        )
                    ),
                    shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                )
                .shadow(
                    elevation = 3.dp,
                    shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                )
        )
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
