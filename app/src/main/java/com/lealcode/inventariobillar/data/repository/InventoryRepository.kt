package com.lealcode.inventariobillar.data.repository

import com.lealcode.inventariobillar.data.model.Product
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de persistencia para el inventario de productos.
 */
interface InventoryRepository {
    /**
     * Observa el listado de productos en tiempo real.
     */
    fun getProducts(): Flow<List<Product>>
    /**
     * Crea un nuevo producto.
     */
    suspend fun addProduct(product: Product)
    /**
     * Actualiza los datos de un producto existente.
     */
    suspend fun updateProduct(product: Product)
    /**
     * Elimina un producto por identificador.
     */
    suspend fun deleteProduct(productId: String)
} 
