package com.lealcode.inventariobillar.data.model

/**
 * Criterios usados para construir un reporte financiero u operativo.
 */
data class ReportFilter(
    val type: ReportType = ReportType.DAILY,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val tableId: String? = null,
    val saleType: String? = null,
    val expenseCategory: String? = null
) {
    constructor() : this(ReportType.DAILY, null, null, null, null, null)
} 
