package com.lealcode.inventariobillar.data.model

import com.google.firebase.firestore.PropertyName

/**
 * Partida jugada dentro de una sesion de mesa.
 */
data class Game(
    @PropertyName("id")
    val id: String = "",
    @PropertyName("tableId")
    val tableId: String = "",
    @PropertyName("sessionId")
    val sessionId: String = "",
    @PropertyName("startTime")
    val startTime: Long = System.currentTimeMillis(),
    @PropertyName("endTime")
    val endTime: Long? = null,
    @PropertyName("pricePerGame")
    val pricePerGame: Double = 0.0,
    @PropertyName("participants")
    val participants: List<GameParticipant> = emptyList(),
    @PropertyName("bets")
    val bets: List<GameBet> = emptyList(),
    @PropertyName("loserIds")
    val loserIds: List<String> = emptyList(),
    @PropertyName("amountPerLoser")
    val amountPerLoser: Double = 0.0,
    @PropertyName("isPaid")
    val isPaid: Boolean = false,
    @PropertyName("status")
    val status: GameStatus = GameStatus.ACTIVE,
    @PropertyName("totalAmount")
    val totalAmount: Double = 0.0

) {
    constructor() : this("", "", "", System.currentTimeMillis(), null, 0.0, emptyList(), emptyList(), emptyList(), 0.0, false, GameStatus.ACTIVE)
}

/**
 * Cliente inscrito como participante de una partida.
 */
data class GameParticipant(
    @PropertyName("clientId")
    val clientId: String = "",
    @PropertyName("clientName")
    val clientName: String = "",
    @PropertyName("joinedAt")
    val joinedAt: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", System.currentTimeMillis())
}

/**
 * Producto apostado o consumido durante una partida.
 */
data class GameBet(
    @PropertyName("productId")
    val productId: String = "",
    @PropertyName("productName")
    val productName: String = "",
    @PropertyName("quantity")
    val quantity: Int = 0,
    @PropertyName("unitPrice")
    val unitPrice: Double = 0.0,
    @PropertyName("totalPrice")
    val totalPrice: Double = 0.0,
    @PropertyName("betByClientIds")
    val betByClientIds: List<String> = emptyList()
) {
    constructor() : this("", "", 0, 0.0, 0.0, emptyList())
}

/**
 * Estados de ciclo de vida de una partida.
 */
enum class GameStatus {
    ACTIVE,
    FINISHED,
    CANCELLED
}
