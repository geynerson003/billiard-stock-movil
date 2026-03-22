package com.lealcode.inventariobillar.data.model

import com.google.firebase.firestore.PropertyName

/**
 * Item individual que compone una venta.
 */
data class SaleItem(
    val productId: String = "",
    val productName: String = "",
    val quantity: Int = 0,
    val unitPrice: Double = 0.0,
    val totalPrice: Double = 0.0,
    val saleByBasket: Boolean = false // Indica si es venta unitaria o por canasta
)

/**
 * Registro de una venta realizada por el negocio.
 *
 * Soporta el modelo actual basado en multiples items y conserva campos legacy para
 * compatibilidad con datos historicos.
 */
data class Sale(
    val id: String = "",
    val items: List<SaleItem> = emptyList(), // Lista de productos en la venta
    val totalAmount: Double = 0.0, // Total de la venta (suma de todos los items)
    val profit: Double = 0.0, // Ganancia real de la venta

    // Fecha en milisegundos (Epoch millis)
    val date: Long = System.currentTimeMillis(),

    val tableId: String? = null, // null para ventas externas
    val type: SaleType = SaleType.EXTERNAL,
    val sellerId: String = "",
    val clientId: String = "", // Nuevo campo: ID del cliente
    @PropertyName("paid") val isPaid: Boolean = false, // Mapear "paid" en Firestore a "isPaid" en el modelo
    val isGameSale: Boolean = false, // Indica si es una venta de partida
    val gameId: String? = null, // ID de la partida si es una venta de partida

    // Campos de compatibilidad con la estructura anterior
    @Deprecated("Use items instead")
    val productId: String = "",
    @Deprecated("Use items instead")
    val productName: String = "",
    @Deprecated("Use items instead")
    val quantity: Int = 0,
    @Deprecated("Use items instead")
    val price: Double = 0.0
)

/**
 * Tipo funcional de venta soportado por la aplicacion.
 */
enum class SaleType { TABLE, EXTERNAL }

/**
 * Rangos predefinidos para filtrar ventas por fecha.
 */
enum class DateFilterType {
    TODAY,
    WEEK,
    MONTH,
    CUSTOM
}
