package com.lealcode.inventariobillar.data.repository

import com.lealcode.inventariobillar.data.model.Client
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de acceso a clientes.
 */
interface ClientRepository {
    /**
     * Crea un nuevo cliente.
     */
    suspend fun addClient(client: Client)
    /**
     * Recupera todos los clientes disponibles.
     */
    suspend fun getClients(): List<Client>
    /**
     * Busca un cliente por identificador.
     */
    suspend fun getClientById(id: String): Client?
    /**
     * Actualiza la informacion de un cliente.
     */
    suspend fun updateClient(client: Client)
    /**
     * Elimina un cliente por identificador.
     */
    suspend fun deleteClient(id: String)
    /**
     * Observa cambios en tiempo real sobre la coleccion de clientes.
     */
    fun getClientsRealtime(): Flow<List<Client>>
} 
