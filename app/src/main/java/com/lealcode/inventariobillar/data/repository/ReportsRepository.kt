package com.lealcode.inventariobillar.data.repository

import com.lealcode.inventariobillar.data.model.ReportFilter
import com.lealcode.inventariobillar.data.model.ReportResult
import kotlinx.coroutines.flow.Flow

/**
 * Contrato para la generacion de reportes agregados.
 */
interface ReportsRepository {
    /**
     * Construye un reporte reactivo a partir del filtro indicado.
     */
    fun getReport(filter: ReportFilter): Flow<ReportResult>
} 
