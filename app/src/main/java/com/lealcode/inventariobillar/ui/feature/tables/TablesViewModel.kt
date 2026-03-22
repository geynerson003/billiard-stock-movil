package com.lealcode.inventariobillar.ui.feature.tables

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lealcode.inventariobillar.data.model.Table
import com.lealcode.inventariobillar.data.model.TableSession
import com.lealcode.inventariobillar.data.repository.TablesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
/**
 * ViewModel del modulo de mesas y sesiones.
 */
class TablesViewModel @Inject constructor(
    private val repository: TablesRepository
) : ViewModel() {
    val tables: StateFlow<List<Table>> = repository.getTablesRealtime()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Registra una nueva mesa.
     */
    fun addTable(table: Table) {
        viewModelScope.launch { repository.addTable(table) }
    }

    /**
     * Actualiza la configuracion de una mesa.
     */
    fun updateTable(table: Table) {
        viewModelScope.launch { repository.updateTable(table) }
    }

    /**
     * Elimina una mesa por identificador.
     */
    fun deleteTable(tableId: String) {
        viewModelScope.launch { repository.deleteTable(tableId) }
    }

    /**
     * Inicia una nueva sesion operativa para la mesa indicada.
     */
    fun startSession(tableId: String) {
        viewModelScope.launch {
            try {
                repository.startSession(tableId)
            } catch (e: Exception) {
                android.util.Log.e("TablesViewModel", "Error al iniciar sesión: ${e.message}", e)
                // Aquí se podría actualizar un estado de error para mostrar en la UI
            }
        }
    }

} 
