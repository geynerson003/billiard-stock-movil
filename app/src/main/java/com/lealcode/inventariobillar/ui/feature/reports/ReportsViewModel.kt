package com.lealcode.inventariobillar.ui.feature.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lealcode.inventariobillar.data.model.ReportFilter
import com.lealcode.inventariobillar.data.model.ReportResult
import com.lealcode.inventariobillar.data.repository.ReportsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
/**
 * ViewModel del modulo de reportes.
 *
 * Mantiene el filtro activo y expone un reporte reactivo derivado de este.
 */
class ReportsViewModel @Inject constructor(
    private val repository: ReportsRepository
) : ViewModel() {
    private val _filter = MutableStateFlow(ReportFilter())
    val filter: StateFlow<ReportFilter> = _filter.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val report: StateFlow<ReportResult> = _filter
        .flatMapLatest { currentFilter ->
            repository.getReport(currentFilter)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportResult())

    /**
     * Reemplaza el filtro actual y dispara el recalculo del reporte.
     */
    fun setFilter(filter: ReportFilter) {
        _filter.value = filter
    }
} 
