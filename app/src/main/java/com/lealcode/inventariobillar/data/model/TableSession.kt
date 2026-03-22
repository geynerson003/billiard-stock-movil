package com.lealcode.inventariobillar.data.model

/**
 * Sesion operativa abierta para una mesa.
 *
 * Agrupa el periodo de uso de la mesa y las ventas relacionadas con esa sesion.
 */
data class TableSession(
    val id: String = "",
    val tableId: String = "",
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val sales: List<String> = emptyList(), // List of Sale IDs
    val total: Double = 0.0
) {
    constructor() : this("", "", System.currentTimeMillis(), null, emptyList(), 0.0)
} 
