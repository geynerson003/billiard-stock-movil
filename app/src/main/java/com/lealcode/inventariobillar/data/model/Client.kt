package com.lealcode.inventariobillar.data.model

/**
 * Representa un cliente del negocio y su estado financiero acumulado.
 */
data class Client(
    val id: String = "",           // Identificador único (puede ser UUID o generado por la base de datos)
    val nombre: String = "",             // Nombre del cliente
    val telefono: String = "",           // Teléfono del cliente
    val deuda: Double = 0.0,         // Monto de deuda actual
    val deudaOriginal: Double = 0.0,    // Deuda original acumulada (se resetea cuando se paga completamente)
    val totalPagado: Double = 0.0       // Total pagado (se resetea cuando se paga completamente)
) {
    constructor() : this("", "", "", 0.0, 0.0, 0.0)
} 
