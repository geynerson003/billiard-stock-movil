package com.lealcode.inventariobillar.data.repository

import com.lealcode.inventariobillar.data.model.Game
import com.lealcode.inventariobillar.data.model.GameBet
import com.lealcode.inventariobillar.data.model.GameParticipant
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de acceso a partidas y apuestas de mesa.
 */
interface GamesRepository {
    /**
     * Observa las partidas asociadas a una sesion.
     */
    fun getGamesBySession(sessionId: String): Flow<List<Game>>
    /**
     * Observa las partidas activas de una mesa.
     */
    fun getActiveGamesByTable(tableId: String): Flow<List<Game>>
    /**
     * Crea una nueva partida.
     */
    suspend fun createGame(game: Game): Game
    /**
     * Actualiza una partida existente.
     */
    suspend fun updateGame(game: Game)
    /**
     * Elimina una partida.
     */
    suspend fun deleteGame(gameId: String)
    /**
     * Agrega un participante a una partida.
     */
    suspend fun addParticipant(gameId: String, participant: GameParticipant)
    /**
     * Quita un participante de una partida.
     */
    suspend fun removeParticipant(gameId: String, clientId: String)
    /**
     * Registra una apuesta o consumo dentro de una partida.
     */
    suspend fun addBet(gameId: String, bet: GameBet)
    /**
     * Elimina una apuesta por identificador logico.
     */
    suspend fun removeBet(gameId: String, betId: String)
    /**
     * Finaliza una partida sin crear ventas adicionales.
     */
    suspend fun finishGame(gameId: String, loserIds: List<String>, isPaid: Boolean, totalAmount: Double,
                           amountPerLoser: Double)
    /**
     * Finaliza una partida y persiste en una sola operacion las ventas derivadas.
     */
    suspend fun finishGameWithSales(
        gameId: String,
        loserIds: List<String>,
        isPaid: Boolean,
        totalAmount: Double,
        amountPerLoser: Double,
        sales: List<com.lealcode.inventariobillar.data.model.Sale>
    )
    /**
     * Cancela una partida activa.
     */
    suspend fun cancelGame(gameId: String)
}
