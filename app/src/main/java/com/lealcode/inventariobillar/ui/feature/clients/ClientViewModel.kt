package com.lealcode.inventariobillar.ui.feature.clients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lealcode.inventariobillar.data.model.Client
import com.lealcode.inventariobillar.data.model.Sale
import com.lealcode.inventariobillar.data.repository.ClientRepository
import com.lealcode.inventariobillar.data.repository.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@HiltViewModel
/**
 * ViewModel del modulo de clientes.
 *
 * Gestiona CRUD, filtros de busqueda y calculos basicos de deuda.
 */
class ClientViewModel @Inject constructor(
    private val repository: ClientRepository,
    private val salesRepository: SalesRepository
) : ViewModel() {

    private val _clients = MutableStateFlow<List<Client>>(emptyList())
    val clients: StateFlow<List<Client>> = repository.getClientsRealtime()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Clientes filtrados basados en la búsqueda
    val filteredClients: StateFlow<List<Client>> = combine(
        clients,
        searchQuery
    ) { allClients, query ->
        if (query.isBlank()) {
            allClients
        } else {
            allClients.filter { client ->
                client.nombre.contains(query, ignoreCase = true) ||
                client.telefono.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _clientDebts = MutableStateFlow<Map<String, Double>>(emptyMap())
    val clientDebts: StateFlow<Map<String, Double>> = _clientDebts.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val debts = calculateClientDebts()
                _clientDebts.value = debts
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    // Eliminar loadClients y la llamada en init, ya que ahora es reactivo
    // Eliminar recarga manual tras add/update/delete

    /**
     * Registra un nuevo cliente.
     */
    fun addClient(client: Client) {
        viewModelScope.launch {
            try {
                repository.addClient(client)
                // loadClients() // Eliminado
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    /**
     * Actualiza la informacion de un cliente.
     */
    fun updateClient(client: Client) {
        viewModelScope.launch {
            try {
                repository.updateClient(client)
                // loadClients() // Eliminado
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    /**
     * Elimina un cliente por identificador.
     */
    fun deleteClient(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteClient(id)
                // loadClients() // Eliminado
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    /**
     * Obtiene un cliente puntual y lo entrega por callback.
     */
    fun getClientById(id: String, onResult: (Client?) -> Unit) {
        viewModelScope.launch {
            try {
                val client = repository.getClientById(id)
                onResult(client)
            } catch (e: Exception) {
                _error.value = e.message
                onResult(null)
            }
        }
    }
    
    /**
     * Calcula las deudas de los clientes basándose en las ventas pendientes
     */
    suspend fun calculateClientDebts(): Map<String, Double> {
        return try {
            val sales = salesRepository.getSales().first()
            val pendingSales = sales.filter { !it.isPaid && it.clientId.isNotBlank() }
            
            pendingSales.groupBy { it.clientId }
                .mapValues { entry -> 
                    entry.value.sumOf { sale ->
                        // Usar totalAmount si es nueva estructura, sino usar price
                        if (sale.items.isNotEmpty()) sale.totalAmount else sale.price
                    }
                }
        } catch (e: Exception) {
            _error.value = e.message
            emptyMap()
        }
    }
    
    /**
     * Actualiza la deuda de un cliente específico
     */
    suspend fun updateClientDebt(clientId: String, newDebt: Double) {
        try {
            val client = repository.getClientById(clientId)
            client?.let {
                val updatedClient = it.copy(deuda = newDebt)
                repository.updateClient(updatedClient)
            }
        } catch (e: Exception) {
            _error.value = e.message
        }
    }

    /**
     * Actualiza el termino de busqueda usado por la lista.
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Limpia el termino de busqueda actual.
     */
    fun clearSearch() {
        _searchQuery.value = ""
    }
}
