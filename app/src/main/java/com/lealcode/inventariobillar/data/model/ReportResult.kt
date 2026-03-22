package com.lealcode.inventariobillar.data.model

/**
 * Resultado agregado de la generacion de reportes.
 */
data class ReportResult(
    val totalSales: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netProfit: Double = 0.0,
    val totalClientDebt: Double = 0.0,
    val salesByTable: Map<String, Double> = emptyMap(),
    val salesByProduct: Map<String, Double> = emptyMap(),
    val topProducts: List<Pair<String, Int>> = emptyList()
) {
    constructor() : this(0.0, 0.0, 0.0, 0.0, emptyMap(), emptyMap(), emptyList())
} 
