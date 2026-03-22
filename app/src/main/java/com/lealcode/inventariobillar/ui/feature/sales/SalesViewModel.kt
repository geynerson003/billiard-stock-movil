package com.lealcode.inventariobillar.ui.feature.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lealcode.inventariobillar.data.model.Sale
import com.lealcode.inventariobillar.data.model.SaleType
import com.lealcode.inventariobillar.data.model.DateFilterType
import com.lealcode.inventariobillar.data.repository.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
/**
 * ViewModel del modulo de ventas.
 *
 * Mantiene el conjunto completo de ventas en memoria y aplica filtros de negocio desde UI.
 */
class SalesViewModel @Inject constructor(
    private val repository: SalesRepository
) : ViewModel() {

    private val _filterType = MutableStateFlow<SaleType?>(null)
    private val _filterTableId = MutableStateFlow<String?>(null)
    private val _dateFilterType = MutableStateFlow(DateFilterType.TODAY)
    private val _customStartDate = MutableStateFlow<Long?>(null)
    private val _customEndDate = MutableStateFlow<Long?>(null)

    // Todas las ventas
    private val _allSales = MutableStateFlow<List<Sale>>(emptyList())

    // Ventas filtradas
    private val _filteredSales = MutableStateFlow<List<Sale>>(emptyList())
    val filteredSales: StateFlow<List<Sale>> = _filteredSales.asStateFlow()

    // Exponer filtros
    val currentDateFilterType: StateFlow<DateFilterType> = _dateFilterType.asStateFlow()
    val currentCustomStartDate: StateFlow<Long?> = _customStartDate.asStateFlow()
    val currentCustomEndDate: StateFlow<Long?> = _customEndDate.asStateFlow()

    init {
        android.util.Log.d("SalesViewModel", "=== INICIALIZANDO SalesViewModel ===")
        loadSales()
    }

    /**
     * Inicia la observacion reactiva de ventas desde el repositorio.
     */
    private fun loadSales() {
        viewModelScope.launch {
            repository.getSales(type = null, tableId = null).collect { sales ->
                android.util.Log.d("SalesViewModel", "Ventas cargadas:")
                _allSales.value = sales
                applyFilters()
            }
        }
    }

    /**
     * Reaplica todos los filtros activos sobre el conjunto completo de ventas.
     */
    private fun applyFilters() {
        val allSales = _allSales.value
        val type = _filterType.value
        val tableId = _filterTableId.value
        val dateFilterType = _dateFilterType.value
        val customStartDate = _customStartDate.value
        val customEndDate = _customEndDate.value

        var filtered = allSales

        // Filtro por tipo
        if (type != null) {
            filtered = filtered.filter { it.type == type }
        }

        // Filtro por mesa
        if (tableId != null) {
            filtered = filtered.filter { it.tableId == tableId }
        }

        // Filtro por fecha
        filtered = filtered.filter { sale ->
            val saleDateMillis = sale.date

            when (dateFilterType) {
                DateFilterType.TODAY -> {
                    val startDay = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val endDay = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }
                    saleDateMillis in startDay.timeInMillis..endDay.timeInMillis
                }

                DateFilterType.WEEK -> {
                    val now = Calendar.getInstance()
                    val last7DaysStart = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, -6)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val todayEnd = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }
                    saleDateMillis in last7DaysStart.timeInMillis..todayEnd.timeInMillis
                }

                DateFilterType.MONTH -> {
                    val now = Calendar.getInstance()
                    val monthStart = Calendar.getInstance().apply {
                        set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), 1, 0, 0, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val monthEnd = Calendar.getInstance().apply {
                        set(
                            now.get(Calendar.YEAR),
                            now.get(Calendar.MONTH),
                            getActualMaximum(Calendar.DAY_OF_MONTH),
                            23, 59, 59
                        )
                        set(Calendar.MILLISECOND, 999)
                    }
                    saleDateMillis in monthStart.timeInMillis..monthEnd.timeInMillis
                }

                DateFilterType.CUSTOM -> {
                    when {
                        customStartDate != null && customEndDate != null ->
                            saleDateMillis in customStartDate..customEndDate

                        customStartDate != null ->
                            saleDateMillis >= customStartDate

                        customEndDate != null ->
                            saleDateMillis <= customEndDate

                        else -> true
                    }
                }
            }
        }

        _filteredSales.value = filtered
    }

    /**
     * Actualiza el filtro por tipo de venta y mesa.
     */
    fun setFilter(type: SaleType?, tableId: String?) {
        _filterType.value = type
        _filterTableId.value = tableId
        applyFilters()
    }

    /**
     * Actualiza el rango de fechas usado por la vista de ventas.
     */
    fun setDateFilter(
        dateFilterType: DateFilterType,
        customStartDate: Long? = null,
        customEndDate: Long? = null
    ) {
        _dateFilterType.value = dateFilterType
        _customStartDate.value = customStartDate
        _customEndDate.value = customEndDate
        applyFilters()
    }

    /**
     * Fuerza la reaplicacion de los filtros actuales.
     */
    fun refreshFilters() {
        applyFilters()
    }

    /**
     * Registra una nueva venta.
     */
    fun addSale(sale: Sale) {
        viewModelScope.launch {
            repository.addSale(sale)
        }
    }

    /**
     * Elimina una venta existente.
     */
    fun deleteSale(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteSale(id)
            } catch (e: Exception) {
                android.util.Log.e("SalesViewModel", "Error al eliminar venta: ${e.message}", e)
            }
        }
    }

    /**
     * Marca una venta como pagada.
     */
    fun markSaleAsPaid(id: String) {
        viewModelScope.launch {
            try {
                repository.markSaleAsPaid(id)
            } catch (e: Exception) {
                android.util.Log.e("SalesViewModel", "Error al marcar venta como pagada: ${e.message}", e)
            }
        }
    }

    /**
     * Expone una venta puntual como StateFlow para pantallas de detalle.
     */
    fun getSaleById(id: String): StateFlow<Sale?> {
        return _allSales.map { sales ->
            sales.find { it.id == id }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    }

    /**
     * Devuelve el filtro de fecha actualmente activo.
     */
    fun getCurrentDateFilterType(): DateFilterType = _dateFilterType.value
    /**
     * Devuelve la fecha inicial custom actualmente seleccionada.
     */
    fun getCurrentCustomStartDate(): Long? = _customStartDate.value
    /**
     * Devuelve la fecha final custom actualmente seleccionada.
     */
    fun getCurrentCustomEndDate(): Long? = _customEndDate.value
}
