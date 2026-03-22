package com.lealcode.inventariobillar.ui.feature.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lealcode.inventariobillar.data.model.Product
import com.lealcode.inventariobillar.data.repository.InventoryRepository

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
/**
 * ViewModel del modulo de inventario.
 */
class InventoryViewModel @Inject constructor(
    private val repository: InventoryRepository
) : ViewModel() {
    val products: StateFlow<List<Product>> = repository.getProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Registra un nuevo producto.
     */
    fun addProduct(product: Product) {
        android.util.Log.d("InventoryViewModel", "Agregando producto:")
        viewModelScope.launch { 
            try {
                repository.addProduct(product)
                android.util.Log.d("InventoryViewModel", "Producto agregado exitosamente")
            } catch (e: Exception) {
                android.util.Log.e("InventoryViewModel", "Error al agregar producto: ${e.message}", e)
            }
        }
    }

    /**
     * Actualiza la informacion de un producto.
     */
    fun updateProduct(product: Product) {
        viewModelScope.launch { repository.updateProduct(product) }
    }

    /**
     * Elimina un producto por identificador.
     */
    fun deleteProduct(productId: String) {
        android.util.Log.d("InventoryViewModel", "Eliminando producto:")
        viewModelScope.launch { 
            try {
                repository.deleteProduct(productId)
                android.util.Log.d("InventoryViewModel", "Producto eliminado exitosamente")
            } catch (e: Exception) {
                android.util.Log.e("InventoryViewModel", "Error al eliminar producto: ${e.message}", e)
            }
        }
    }


} 
