package com.lealcode.inventariobillar.data.repository

import com.lealcode.inventariobillar.data.model.Sale
import com.lealcode.inventariobillar.data.model.SaleType
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de acceso a ventas y cuentas por cobrar.
 */
interface SalesRepository {
    /**
     * Observa las ventas con filtros opcionales por tipo y mesa.
     */
    fun getSales(type: SaleType? = null, tableId: String? = null): Flow<List<Sale>>
    /**
     * Registra una nueva venta.
     */
    suspend fun addSale(sale: Sale)
    /**
     * Busca una venta por su identificador.
     */
    suspend fun getSaleById(id: String): Sale?
    /**
     * Elimina una venta y revierte sus efectos asociados cuando aplique.
     */
    suspend fun deleteSale(id: String)
    /**
     * Marca una venta como pagada.
     */
    suspend fun markSaleAsPaid(id: String)
} 
