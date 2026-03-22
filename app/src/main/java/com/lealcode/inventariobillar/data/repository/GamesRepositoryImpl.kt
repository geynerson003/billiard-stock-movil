package com.lealcode.inventariobillar.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.lealcode.inventariobillar.data.model.Game
import com.lealcode.inventariobillar.data.model.GameBet
import com.lealcode.inventariobillar.data.model.GameParticipant
import com.lealcode.inventariobillar.data.model.GameStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.forEach

@Singleton
/**
 * Implementacion de [GamesRepository] para partidas, participantes y apuestas.
 */
class  GamesRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : GamesRepository {

    private fun getGamesCollection(): com.google.firebase.firestore.CollectionReference? {
        val userId = authRepository.getCurrentUserId() ?: return null
        return firestore.collection("businesses").document(userId).collection("games")
    }

    override fun getGamesBySession(sessionId: String): Flow<List<Game>> = callbackFlow {
        val gamesCollection = getGamesCollection()
        if (gamesCollection == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        
        val subscription = gamesCollection
            .whereEqualTo("sessionId", sessionId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        android.util.Log.w("GamesRepo", "Permiso denegado en listener de partidas por sesión (posible logout)")
                        close()
                    } else {
                        android.util.Log.e("GamesRepositoryImpl", "Error al buscar partidas: ${error.message}")
                        close(error)
                    }
                    return@addSnapshotListener
                }

                val games = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Game::class.java)?.copy(id = doc.id)
                }?.sortedByDescending { it.startTime } ?: emptyList()

                trySend(games)
            }
        
        awaitClose { subscription.remove() }
    }

    override fun getActiveGamesByTable(tableId: String): Flow<List<Game>> = callbackFlow {
        val gamesCollection = getGamesCollection()
        if (gamesCollection == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        
        val subscription = gamesCollection
            .whereEqualTo("tableId", tableId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        android.util.Log.w("GamesRepo", "Permiso denegado en listener de partidas de mesa (posible logout)")
                        close()
                    } else {
                        close(error)
                    }
                    return@addSnapshotListener
                }

                val games = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Game::class.java)?.copy(id = doc.id)
                }?.filter { it.status == GameStatus.ACTIVE }
                ?.sortedByDescending { it.startTime } ?: emptyList()
                
                trySend(games)
            }
        
        awaitClose { subscription.remove() }
    }

    override suspend fun createGame(game: Game): Game {
        val gamesCollection = getGamesCollection()
            ?: throw IllegalStateException("Usuario no autenticado")
        
        val newId = java.util.UUID.randomUUID().toString()
        val gameWithId = game.copy(id = newId)
        val docRef = gamesCollection.document(newId)
        docRef.set(gameWithId).await()
        return gameWithId
    }

    override suspend fun updateGame(game: Game) {
        val gamesCollection = getGamesCollection()
            ?: throw IllegalStateException("Usuario no autenticado")
        gamesCollection.document(game.id).set(game).await()
    }

    override suspend fun deleteGame(gameId: String) {
        val gamesCollection = getGamesCollection()
            ?: throw IllegalStateException("Usuario no autenticado")
        gamesCollection.document(gameId).delete().await()
    }

    override suspend fun addParticipant(gameId: String, participant: GameParticipant) {
        val gamesCollection = getGamesCollection()
            ?: throw IllegalStateException("Usuario no autenticado")


        val gameRef = gamesCollection.document(gameId)
        firestore.runTransaction { transaction ->
            val gameDoc = transaction.get(gameRef)
            val game = gameDoc.toObject(Game::class.java)
            if (game != null) {
                val updatedParticipants = game.participants + participant
                transaction.update(gameRef, "participants", updatedParticipants)
            } else {
                android.util.Log.e("GamesRepositoryImpl", "ERROR: No se encontró la partida con ID: ${gameId}")
            }
        }.await()

    }

    override suspend fun removeParticipant(gameId: String, clientId: String) {
        val gamesCollection = getGamesCollection()
            ?: throw IllegalStateException("Usuario no autenticado")
        val gameRef = gamesCollection.document(gameId)
        firestore.runTransaction { transaction ->
            val gameDoc = transaction.get(gameRef)
            val game = gameDoc.toObject(Game::class.java)
            if (game != null) {
                val updatedParticipants = game.participants.filter { it.clientId != clientId }
                transaction.update(gameRef, "participants", updatedParticipants)
            }
        }.await()
    }

    override suspend fun addBet(gameId: String, bet: GameBet) {
        val gamesCollection = getGamesCollection()
            ?: throw IllegalStateException("Usuario no autenticado")
        val gameRef = gamesCollection.document(gameId)
        firestore.runTransaction { transaction ->
            val gameDoc = transaction.get(gameRef)
            val game = gameDoc.toObject(Game::class.java)
            if (game != null) {
                val updatedBets = game.bets + bet
                transaction.update(gameRef, "bets", updatedBets)
            }
        }.await()
    }

    override suspend fun removeBet(gameId: String, betId: String) {
        val gamesCollection = getGamesCollection()
            ?: throw IllegalStateException("Usuario no autenticado")
        val gameRef = gamesCollection.document(gameId)
        firestore.runTransaction { transaction ->
            val gameDoc = transaction.get(gameRef)
            val game = gameDoc.toObject(Game::class.java)
            if (game != null) {
                val updatedBets = game.bets.filterIndexed { index, _ -> index.toString() != betId }
                transaction.update(gameRef, "bets", updatedBets)
            }
        }.await()
    }

    override suspend fun finishGame(
        gameId: String,
        loserIds: List<String>,
        isPaid: Boolean,
        totalAmount: Double,
        amountPerLoser: Double
    ) {
        // Deprecated: Use finishGameWithSales for atomic operations
        finishGameWithSales(gameId, loserIds, isPaid, totalAmount, amountPerLoser, emptyList())
    }

    override suspend fun finishGameWithSales(
        gameId: String,
        loserIds: List<String>,
        isPaid: Boolean,
        totalAmount: Double,
        amountPerLoser: Double,
        sales: List<com.lealcode.inventariobillar.data.model.Sale>
    ) {
        val userId = authRepository.getCurrentUserId()
            ?: throw IllegalStateException("Usuario no autenticado")
        
        val gamesCollection = getGamesCollection()!!
        val gameRef = gamesCollection.document(gameId)

        firestore.runTransaction { transaction ->

            /* =========================
             * 1️⃣ TODAS LAS LECTURAS
             * ========================= */

            // Game
            val gameSnap = transaction.get(gameRef)
            val game = gameSnap.toObject(Game::class.java)

            // Products cache
            val productSnapshots = mutableMapOf<String, Pair<Int, Int>>() // stock, unitsPerPackage

            // Clients cache
            val clientSnapshots = mutableMapOf<String, Triple<Double, Double, Double>>() // deudaOriginal, totalPagado, deuda

            sales.forEach { sale ->
                sale.items.forEach { item ->
                    val productRef = firestore.collection("businesses").document(userId).collection("products").document(item.productId)
                    if (!productSnapshots.containsKey(item.productId)) {
                        val snap = transaction.get(productRef)
                        if (snap.exists()) {
                            val stock = (snap.getLong("stock") ?: 0).toInt()
                            val units = (snap.getLong("unitsPerPackage") ?: 1).toInt()
                            productSnapshots[item.productId] = stock to units
                        }
                    }
                }

                if (!sale.isPaid && sale.clientId.isNotBlank()) {
                    val clientRef = firestore.collection("businesses").document(userId).collection("clients").document(sale.clientId)
                    if (!clientSnapshots.containsKey(sale.clientId)) {
                        val snap = transaction.get(clientRef)
                        if (snap.exists()) {
                            clientSnapshots[sale.clientId] = Triple(
                                snap.getDouble("deudaOriginal") ?: 0.0,
                                snap.getDouble("totalPagado") ?: 0.0,
                                snap.getDouble("deuda") ?: 0.0
                            )
                        }
                    }
                }
            }

            /* =========================
             * 2️⃣ TODAS LAS ESCRITURAS
             * ========================= */

            // Update Game
            game?.let {
                val updatedGame = it.copy(
                    endTime = System.currentTimeMillis(),
                    loserIds = loserIds,
                    amountPerLoser = amountPerLoser,
                    isPaid = isPaid,
                    status = GameStatus.FINISHED,
                    totalAmount = totalAmount
                )
                transaction.set(gameRef, updatedGame)
            }

            // Sales + Inventory + Clients
            sales.forEach { sale ->
                val saleRef = firestore.collection("businesses").document(userId).collection("sales")
                    .document(if (sale.id.isBlank()) java.util.UUID.randomUUID().toString() else sale.id)

                val finalSale = sale.copy(id = saleRef.id)

                transaction.set(saleRef, finalSale)

                // Inventory
                finalSale.items.forEach { item ->
                    val (stock, unitsPerPackage) = productSnapshots[item.productId] ?: return@forEach
                    val unitsToSubtract = if (item.saleByBasket) {
                        item.quantity * unitsPerPackage
                    } else item.quantity

                    val newStock = (stock - unitsToSubtract).coerceAtLeast(0)
                    val productRef = firestore.collection("businesses").document(userId).collection("products").document(item.productId)
                    transaction.update(productRef, "stock", newStock)
                    
                    // Actualizar el cache para que la siguiente venta use el stock correcto
                    productSnapshots[item.productId] = newStock to unitsPerPackage
                }

                // Client debt
                if (!finalSale.isPaid && finalSale.clientId.isNotBlank()) {
                    val (deudaOriginal, totalPagado, deuda) =
                        clientSnapshots[finalSale.clientId] ?: return@forEach

                    val saleAmount = if (finalSale.items.isNotEmpty())
                        finalSale.totalAmount else finalSale.price

                    val shouldReset = deuda <= 0.01
                    val newDeudaOriginal = deudaOriginal + saleAmount
                    val newDebt = if (shouldReset) {
                        newDeudaOriginal
                    } else {
                        (newDeudaOriginal - totalPagado).coerceAtLeast(0.0)
                    }

                    val clientRef = firestore.collection("businesses").document(userId).collection("clients").document(finalSale.clientId)
                    transaction.update(clientRef, mapOf(
                        "deuda" to newDebt,
                        "deudaOriginal" to newDeudaOriginal,
                        "totalPagado" to if (shouldReset) 0.0 else totalPagado
                    ))
                }
            }

            null
        }.await()
    }



    override suspend fun cancelGame(gameId: String) {
        val gamesCollection = getGamesCollection()
            ?: throw IllegalStateException("Usuario no autenticado")
        val gameRef = gamesCollection.document(gameId)
        firestore.runTransaction { transaction ->
            val gameDoc = transaction.get(gameRef)
            val game = gameDoc.toObject(Game::class.java)
            if (game != null) {
                val updatedGame = game.copy(
                    endTime = System.currentTimeMillis(),
                    status = GameStatus.CANCELLED
                )
                transaction.set(gameRef, updatedGame)
            }
        }.await()
    }
}
