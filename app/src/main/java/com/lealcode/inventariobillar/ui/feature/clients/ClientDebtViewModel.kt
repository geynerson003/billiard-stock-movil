package com.lealcode.inventariobillar.ui.feature.clients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lealcode.inventariobillar.data.model.Client
import com.lealcode.inventariobillar.data.model.Sale
import com.lealcode.inventariobillar.data.model.Payment
import com.lealcode.inventariobillar.data.repository.ClientRepository
import com.lealcode.inventariobillar.data.repository.SalesRepository
import com.lealcode.inventariobillar.data.service.DebtCalculationService
import com.lealcode.inventariobillar.data.service.ClientDebtInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
/**
 * ViewModel especializado en el detalle de deuda de un cliente.
 */
class ClientDebtViewModel @Inject constructor(
    private val clientRepository: ClientRepository,
    private val salesRepository: SalesRepository,
    private val debtCalculationService: DebtCalculationService
) : ViewModel() {
    
    private val _client = MutableStateFlow<Client?>(null)
    val client: StateFlow<Client?> = _client.asStateFlow()
    
    private val _pendingSales = MutableStateFlow<List<Sale>>(emptyList())
    val pendingSales: StateFlow<List<Sale>> = _pendingSales.asStateFlow()
    
    private val _clientDebtInfo = MutableStateFlow<ClientDebtInfo?>(null)
    val clientDebtInfo: StateFlow<ClientDebtInfo?> = _clientDebtInfo.asStateFlow()
    
    private val _payments = MutableStateFlow<List<Payment>>(emptyList())
    val payments: StateFlow<List<Payment>> = _payments.asStateFlow()
    
    /**
     * Define el cliente activo y carga su estado de deuda.
     */
    fun setClient(client: Client) {
        _client.value = client
        loadPendingSales(client.id)
    }
    
    /**
     * Observa ventas pendientes, pagos y resumen de deuda del cliente seleccionado.
     */
    private fun loadPendingSales(clientId: String) {
        viewModelScope.launch {
            try {
                debtCalculationService.calculateClientDebt(clientId).collect { debtInfo ->
                    _clientDebtInfo.value = debtInfo
                    _pendingSales.value = debtInfo.pendingSales
                    _payments.value = debtInfo.payments
                    
                    android.util.Log.d("ClientDebtViewModel", "Información de deuda actualizada:")
                }
            } catch (e: Exception) {
                android.util.Log.e("ClientDebtViewModel", "Error loading debt info: ${e.message}", e)
            }
        }
    }
    
    /**
     * Registra un abono sobre la deuda actual del cliente.
     */
    fun payDebt(paymentAmount: Double, description: String = "", notes: String = "") {
        viewModelScope.launch {
            try {
                val currentClient = _client.value
                if (currentClient == null) return@launch
                
                val debtInfo = _clientDebtInfo.value
                if (debtInfo == null) return@launch
                

                // Registrar el pago en el sistema de pagos
                debtCalculationService.registerPayment(
                    clientId = currentClient.id,
                    amount = paymentAmount,
                    description = description.ifBlank { "Pago de deuda" },
                    notes = notes
                )
                
                // Actualizar el cliente local con la información más reciente
                val updatedClient = clientRepository.getClientById(currentClient.id)
                updatedClient?.let { client ->
                    _client.value = client
                }
                
                android.util.Log.d("ClientDebtViewModel", "Pago registrado exitosamente:")
                
            } catch (e: Exception) {
                android.util.Log.e("ClientDebtViewModel", "Error processing debt payment: ${e.message}", e)
            }
        }
    }
} 
