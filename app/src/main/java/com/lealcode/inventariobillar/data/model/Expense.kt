package com.lealcode.inventariobillar.data.model

/**
 * Movimiento de gasto operativo del negocio.
 */
data class Expense(
    val id: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val category: String = "General",
    val date: String = System.currentTimeMillis().toString(),
) {
    constructor() : this("", "", 0.0, "General", System.currentTimeMillis().toString())
} 
