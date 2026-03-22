package com.lealcode.inventariobillar.data.model

/**
 * Resumen agregado mostrado en el dashboard principal.
 */
data class DashboardSummary(
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netProfit: Double = 0.0,
    val lowStockAlerts: List<String> = emptyList(),
    val topProducts: List<Pair<String, Int>> = emptyList(),
    val profitChartData: ProfitChartData? = null
) {
    constructor() : this(0.0, 0.0, 0.0, emptyList(), emptyList(), null)
} 
