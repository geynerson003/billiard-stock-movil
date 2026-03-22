package com.lealcode.inventariobillar.data.model

/**
 * Registra un pago aplicado a la deuda de un cliente.
 */
data class Payment(
    val id: String = "",
    val clientId: String = "",
    val amount: Double = 0.0,
    val date: Long = System.currentTimeMillis(),
    val description: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val relatedSales: List<String> = emptyList(), // IDs de ventas relacionadas (opcional)
    val isPartialPayment: Boolean = true,
    val notes: String = ""
) {
    constructor() : this("", "", 0.0, System.currentTimeMillis(), "", PaymentMethod.CASH, emptyList(), true, "")
}

/**
 * Medios de pago disponibles para registrar abonos o cancelaciones.
 */
enum class PaymentMethod {
    CASH,
    CARD,
    TRANSFER,
    OTHER
}
