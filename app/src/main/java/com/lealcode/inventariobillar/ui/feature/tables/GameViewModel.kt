package com.lealcode.inventariobillar.ui.feature.tables

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lealcode.inventariobillar.data.model.*
import com.lealcode.inventariobillar.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
/**
 * ViewModel responsable del flujo de partida en una mesa.
 *
 * Administra participantes, apuestas, inventario disponible y el cierre de la partida.
 */
class GameViewModel @Inject constructor(
    private val gamesRepository: GamesRepository,
    private val clientsRepository: ClientRepository,
    private val productsRepository: InventoryRepository,
    private val salesRepository: SalesRepository,
    private val tablesRepository: TablesRepository
) : ViewModel() {

    private val _currentGame = MutableStateFlow<Game?>(null)
    val currentGame: StateFlow<Game?> = _currentGame.asStateFlow()

    private val _clients = MutableStateFlow<List<Client>>(emptyList())
    val clients: StateFlow<List<Client>> = _clients.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _tableName = MutableStateFlow("")
    val tableName: String get() = _tableName.value

    private val _gameHistory = MutableStateFlow<List<Game>>(emptyList())
    val gameHistory: StateFlow<List<Game>> = _gameHistory.asStateFlow()

    private var activeGameListener: kotlinx.coroutines.Job? = null

    /**
     * Observa la partida activa de una mesa dentro de una sesion.
     */
    fun loadGame(tableId: String, sessionId: String) {
        // Cancelar cualquier listener anterior para evitar fugas
        activeGameListener?.cancel()

        viewModelScope.launch {
            // Cargar información de la mesa
            tablesRepository.getTables().firstOrNull()?.find { it.id == tableId }?.let { table ->
                _tableName.value = table.name
            }
        }

        // Escuchar en tiempo real la partida activa de la mesa y sesión
        activeGameListener = viewModelScope.launch {
            gamesRepository.getGamesBySession(sessionId).collect { games ->
                val activeGame = games.find { it.status == GameStatus.ACTIVE && it.tableId == tableId }
                if (activeGame != null) {
                    _currentGame.value = activeGame
                    android.util.Log.d("GameViewModel", "[Realtime] Partida activa encontrada y cargada:")
                } else {
                    _currentGame.value = null
                    android.util.Log.d("GameViewModel", "[Realtime] No hay partida activa para la mesa: y sesión: ")
                }
            }
        }
    }

    /**
     * Carga en tiempo real los clientes disponibles para agregarlos a la partida.
     */
    fun loadClients() {
        viewModelScope.launch {
            android.util.Log.d("GameViewModel", "=== CARGANDO CLIENTES ===")
            clientsRepository.getClientsRealtime().collect { clients: List<Client> ->
                _clients.value = clients
                android.util.Log.d("GameViewModel", "Clientes cargados:")

            }
        }
    }

    /**
     * Carga los productos que pueden apostarse o consumirse durante la partida.
     */
    fun loadProducts() {
        viewModelScope.launch {
            productsRepository.getProducts().collect { products: List<Product> ->
                _products.value = products
            }
        }
    }

    /**
     * Agrega uno o varios clientes como participantes de la partida.
     */
    fun addParticipants(gameId: String, clients: List<Client>) {
        viewModelScope.launch {
            
            try {
                // Agregar cada participante individualmente
                clients.forEach { client ->
                    val participant = GameParticipant(
                        clientId = client.id,
                        clientName = client.nombre
                    )
                    gamesRepository.addParticipant(gameId, participant)
                    android.util.Log.d("GameViewModel", "Participante agregado:")
                }
                
                // Actualizar el juego actual con todos los nuevos participantes
                _currentGame.value?.let { game ->
                    val newParticipants = clients.map { client ->
                        GameParticipant(
                            clientId = client.id,
                            clientName = client.nombre
                        )
                    }
                    val updatedGame = game.copy(
                        participants = game.participants + newParticipants
                    )
                    _currentGame.value = updatedGame
                    android.util.Log.d("GameViewModel", "Partida actualizada localmente - Participantes:")
                } ?: run {
                    android.util.Log.e("GameViewModel", "ERROR: No hay partida actual cargada")
                }
                
                android.util.Log.d("GameViewModel", "Todos los participantes agregados exitosamente")
            } catch (e: Exception) {
                android.util.Log.e("GameViewModel", "Error al agregar participantes: ${e.message}")
            }
            
        }
    }

    /**
     * Retira un participante de la partida actual.
     */
    fun removeParticipant(gameId: String, clientId: String) {
        viewModelScope.launch {
            gamesRepository.removeParticipant(gameId, clientId)
            
            // Actualizar el juego actual
            _currentGame.value?.let { game ->
                _currentGame.value = game.copy(
                    participants = game.participants.filter { it.clientId != clientId }
                )
            }
        }
    }

    /**
     * Registra una apuesta o consumo asociado a la partida.
     */
    fun addBet(gameId: String, bet: GameBet) {
        viewModelScope.launch {
            gamesRepository.addBet(gameId, bet)
            
            // Actualizar el juego actual
            _currentGame.value?.let { game ->
                _currentGame.value = game.copy(
                    bets = game.bets + bet
                )
            }
        }
    }

    /**
     * Elimina una apuesta usando su posicion en la lista local.
     */
    fun removeBet(gameId: String, betIndex: Int) {
        viewModelScope.launch {
            gamesRepository.removeBet(gameId, betIndex.toString())
            
            // Actualizar el juego actual
            _currentGame.value?.let { game ->
                val updatedBets = game.bets.toMutableList()
                if (betIndex < updatedBets.size) {
                    updatedBets.removeAt(betIndex)
                    _currentGame.value = game.copy(bets = updatedBets)
                }
            }
        }
    }


    /**
     * Crea una nueva partida sobre la misma mesa y sesion.
     */
    fun restartGame(tableId: String, sessionId: String) {
        viewModelScope.launch {
            // Obtener información de la mesa para el precio por partida
            val table = tablesRepository.getTables().firstOrNull()?.find { it.id == tableId }
            val newGame = Game(
                tableId = tableId,
                sessionId = sessionId,
                pricePerGame = table?.pricePerGame ?: 0.0
            )
            val createdGame = gamesRepository.createGame(newGame)
            _currentGame.value = createdGame
            
            android.util.Log.d("GameViewModel", "Partida reiniciada: Nueva partida creada")
        }
    }
    /**
     * Inicia una partida nueva o recupera la partida activa ya existente.
     */
    fun startGame(tableId: String, sessionId: String) {
        viewModelScope.launch {
            // Verificar si ya hay una partida ACTIVA para esta mesa y sesión
            val existingGames = gamesRepository.getGamesBySession(sessionId).firstOrNull() ?: emptyList()
            val activeGame = existingGames.find { it.status == GameStatus.ACTIVE && it.tableId == tableId }

            if (activeGame == null) {
                // Crear una nueva partida
                val table = tablesRepository.getTables().firstOrNull()?.find { it.id == tableId }
                val pricePerGame = table?.pricePerGame ?: 0.0

                val newGame = Game(
                    id = "",
                    tableId = tableId,
                    sessionId = sessionId,
                    pricePerGame = pricePerGame,
                    status = GameStatus.ACTIVE
                )
                val createdGame = gamesRepository.createGame(newGame)
                _currentGame.value = createdGame

                android.util.Log.d("GameViewModel", "Nueva partida iniciada:")
            } else {
                // Ya hay una partida activa para esta mesa - cargarla
                _currentGame.value = activeGame
                android.util.Log.d("GameViewModel", "Partida activa cargada:")
            }

        }
    }




    /**
     * Finaliza la partida, calcula el total y delega la persistencia atomica al repositorio.
     */
    fun endGame(gameId: String, loserIds: List<String>, isPaid: Boolean) {
        viewModelScope.launch {
            _currentGame.value?.let { game ->
                // Actualizar apuestas (si hay perdedores)
                val updatedBets = if (loserIds.isNotEmpty()) {
                    game.bets.map { bet ->
                        bet.copy(betByClientIds = loserIds)
                    }
                } else {
                    game.bets
                }

                // 🔹 Calcular total
                val totalAmount = game.pricePerGame + updatedBets.sumOf { it.totalPrice }
                val amountPerLoser = if (loserIds.isNotEmpty()) totalAmount / loserIds.size else 0.0

                // Crear copia del juego actualizado (para estado local)
                val updatedGame = game.copy(
                    endTime = System.currentTimeMillis(),
                    loserIds = loserIds,
                    amountPerLoser = amountPerLoser,
                    isPaid = isPaid,
                    status = GameStatus.FINISHED,
                    bets = updatedBets,
                    totalAmount = totalAmount
                )

                // Preparar ventas
                val salesToCreate = prepareGameSales(updatedGame, totalAmount, amountPerLoser)

                // Guardar en Firestore de forma atómica (Juego + Ventas + Inventario + Deudas)
                gamesRepository.finishGameWithSales(
                    gameId, 
                    loserIds, 
                    isPaid, 
                    totalAmount, 
                    amountPerLoser,
                    salesToCreate
                )
                
                _currentGame.value = updatedGame

            }
        }
    }



    /**
     * Construye las ventas derivadas del cierre de la partida.
     */
    private fun prepareGameSales(game: Game, totalAmount: Double, amountPerLoser: Double): List<Sale> {
        try {
            android.util.Log.d("GameViewModel", "=== PREPARANDO VENTAS ===")
            
            if (totalAmount <= 0) return emptyList()
            if (game.tableId.isBlank() || game.id.isBlank()) return emptyList()

            // Construir lista de items (a partir de las apuestas)
            val originalItems: List<SaleItem> = game.bets.map { bet ->
                SaleItem(
                    productId = bet.productId,
                    productName = bet.productName,
                    quantity = bet.quantity,
                    unitPrice = bet.unitPrice,
                    totalPrice = bet.totalPrice,
                    saleByBasket = false
                )
            }

            // Calcular el costo total de los productos apostados
            val products = _products.value
            val productMap = products.associateBy { it.id }
            val productsCostTotal = originalItems.sumOf { item ->
                val product = productMap[item.productId]
                if (product != null && product.unitsPerPackage > 0) {
                    val supplierPricePerUnit = product.supplierPrice / product.unitsPerPackage
                    supplierPricePerUnit * item.quantity
                } else 0.0
            }

            // Determinar receptores
            val recipients: List<String>
            if (game.loserIds.isNotEmpty()) {
                recipients = game.loserIds
            } else if (game.participants.isNotEmpty()) {
                recipients = game.participants.map { it.clientId }
            } else {
                recipients = emptyList()
            }

            val recipientsCount = recipients.size

            // Helper: dividir items
            fun buildItemsForRecipient(recipientIndex: Int, totalRecipients: Int): List<SaleItem> {
                if (totalRecipients <= 0) return originalItems.map { it.copy() }
                val perRecipientItems = mutableListOf<SaleItem>()

                for (item in originalItems) {
                    val baseQty = item.quantity / totalRecipients
                    val remainder = item.quantity % totalRecipients
                    val assignedQty = baseQty + if (recipientIndex < remainder) 1 else 0
                    if (assignedQty > 0) {
                        val unitPrice = item.unitPrice
                        val assignedTotalPrice = unitPrice * assignedQty
                        perRecipientItems.add(
                            SaleItem(
                                productId = item.productId,
                                productName = item.productName,
                                quantity = assignedQty,
                                unitPrice = unitPrice,
                                totalPrice = assignedTotalPrice,
                                saleByBasket = false
                            )
                        )
                    }
                }
                return perRecipientItems
            }

            // Calcular profit por venta
            val profitPerSale = if (recipientsCount > 0) {
                val totalPerRecipient = totalAmount / recipientsCount
                val productsCostPerRecipient = productsCostTotal / recipientsCount
                totalPerRecipient - productsCostPerRecipient
            } else {
                totalAmount - productsCostTotal
            }

            val salesList = mutableListOf<Sale>()

            if (recipientsCount > 0) {
                val totalPerRecipient = totalAmount / recipientsCount
                recipients.forEachIndexed { index, clientId ->
                    val itemsForThis = buildItemsForRecipient(index, recipientsCount)
                    val sale = Sale(
                        items = itemsForThis,
                        totalAmount = totalPerRecipient,
                        profit = profitPerSale,
                        date = System.currentTimeMillis(),
                        tableId = game.tableId,
                        type = SaleType.TABLE,
                        sellerId = "system",
                        clientId = clientId,
                        isPaid = game.isPaid, // Usar el estado de pago del juego
                        isGameSale = true,
                        gameId = game.id
                    )
                    salesList.add(sale)
                }
            } else {
                // Venta única
                val sale = Sale(
                    items = originalItems,
                    totalAmount = totalAmount,
                    profit = profitPerSale,
                    date = System.currentTimeMillis(),
                    tableId = game.tableId,
                    type = SaleType.TABLE,
                    sellerId = "system",
                    isPaid = game.isPaid,
                    isGameSale = true,
                    gameId = game.id
                )
                salesList.add(sale)
            }
            
            return salesList

        } catch (e: Exception) {
            android.util.Log.e("GameViewModel", " Error inesperado en prepareGameSales: ${e.message}", e)
            return emptyList()
        }
    }



}
