package com.lealcode.inventariobillar.data.model

import androidx.compose.ui.graphics.Color

/**
 * Punto de datos listo para representarse en un grafico.
 */
data class ChartData(
    val label: String = "",
    val value: Double = 0.0,
    val color: Color = Color.Unspecified
) {
    constructor() : this("", 0.0, Color.Unspecified)
}

/**
 * Periodicidad disponible para la visualizacion de graficos.
 */
enum class ChartFilterType {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

/**
 * Coleccion de puntos que alimenta el grafico de ganancias del dashboard.
 */
data class ProfitChartData(
    val filterType: ChartFilterType = ChartFilterType.WEEKLY,
    val data: List<ChartData> = emptyList()
) {
    constructor() : this(ChartFilterType.WEEKLY, emptyList())
} 
