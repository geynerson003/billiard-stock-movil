package com.lealcode.inventariobillar.data.repository

import com.lealcode.inventariobillar.data.model.Table
import com.lealcode.inventariobillar.data.model.TableSession
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de acceso a mesas y sesiones de uso.
 */
interface TablesRepository {
    /**
     * Obtiene el listado de mesas.
     */
    fun getTables(): Flow<List<Table>>
    /**
     * Observa las mesas en tiempo real.
     */
    fun getTablesRealtime(): Flow<List<Table>>
    /**
     * Registra una nueva mesa.
     */
    suspend fun addTable(table: Table)
    /**
     * Actualiza la configuracion de una mesa.
     */
    suspend fun updateTable(table: Table)
    /**
     * Elimina una mesa.
     */
    suspend fun deleteTable(tableId: String)
    /**
     * Inicia una nueva sesion operativa para la mesa indicada.
     */
    suspend fun startSession(tableId: String): TableSession
} 
