package com.lealcode.inventariobillar.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.lealcode.inventariobillar.data.model.Product

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

/**
 * Implementacion de [InventoryRepository] para la coleccion de productos.
 */
class InventoryRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : InventoryRepository {
    
    companion object {
        private const val TAG = "InventoryRepositoryImpl"
    }
    
    private val products = MutableStateFlow<List<Product>>(emptyList())

    override fun getProducts(): Flow<List<Product>> = callbackFlow {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            Log.e(TAG, "Usuario no autenticado")
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore
            .collection("businesses")
            .document(userId)
            .collection("products")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        Log.w(TAG, "Permiso denegado en listener de productos (posible logout)")
                        close()
                    } else {
                        Log.e(TAG, "Error en listener de productos: ${error.message}", error)
                        close(error)
                    }
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val products = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(Product::class.java)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error al deserializar producto: ${e.message}", e)
                            null
                        }
                    }
                    Log.d(TAG, "Productos obtenidos: ${products.size}")
                    trySend(products)
                }
            }
        awaitClose { 
            Log.d(TAG, "Cerrando listener de productos")
            listener.remove() 
        }
    }

    override suspend fun addProduct(product: Product) {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            Log.e(TAG, "Usuario no autenticado")
            throw IllegalStateException("Usuario no autenticado")
        }
        
        try {

            val productsCollection = firestore
                .collection("businesses")
                .document(userId)
                .collection("products")
            
            val productWithId = if (product.id.isBlank()) {
                product.copy(id = productsCollection.document().id)
            } else {
                product
            }

            productsCollection.document(productWithId.id).set(productWithId).await()

        } catch (e: Exception) {
            Log.e(TAG, "Error al agregar producto: ${e.message}", e)
            throw e
        }
    }

    override suspend fun updateProduct(product: Product) {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            Log.e(TAG, "Usuario no autenticado")
            throw IllegalStateException("Usuario no autenticado")
        }
        
        firestore
            .collection("businesses")
            .document(userId)
            .collection("products")
            .document(product.id)
            .set(product)
            .await()
    }

    override suspend fun deleteProduct(productId: String) {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            Log.e(TAG, "Usuario no autenticado")
            throw IllegalStateException("Usuario no autenticado")
        }
        
        try {

            firestore
                .collection("businesses")
                .document(userId)
                .collection("products")
                .document(productId)
                .delete()
                .await()
            Log.d(TAG, "Producto eliminado exitosamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar producto: ${e.message}", e)
            throw e
        }
    }


} 
