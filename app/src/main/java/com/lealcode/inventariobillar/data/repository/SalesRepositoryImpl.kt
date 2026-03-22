package com.lealcode.inventariobillar.data.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.lealcode.inventariobillar.data.model.Product
import com.lealcode.inventariobillar.data.model.Sale
import com.lealcode.inventariobillar.data.model.SaleItem
import com.lealcode.inventariobillar.data.model.SaleType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Implementacion de [SalesRepository] que coordina ventas e impacto sobre inventario.
 */
class SalesRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : SalesRepository {

    private fun DocumentSnapshot.toSale(): Sale? {
        return try {
            val data = this.data ?: return null
            
            // Utilizar toObject para mapeo automático donde sea posible, 
            // pero manteniendo la robustez para campos calculados o complejos
            val id = this.id
            val date = when (val rawDate = data["date"]) {
                is Long -> rawDate
                is com.google.firebase.Timestamp -> rawDate.toDate().time
                is java.util.Date -> rawDate.time
                else -> System.currentTimeMillis()
            }
            
            val tableId = data["tableId"] as? String
            val typeStr = data["type"] as? String
            val type = typeStr?.let { try { SaleType.valueOf(it) } catch(e: Exception) { SaleType.EXTERNAL } } ?: SaleType.EXTERNAL
            
            val sellerId = data["sellerId"] as? String ?: ""
            val clientId = data["clientId"] as? String ?: ""
            val isPaid = data["paid"] as? Boolean ?: false
            
            // Items
            val itemsList = data["items"] as? List<Map<String, Any>>
            val items = itemsList?.map { itemData ->
                SaleItem(
                    productId = itemData["productId"] as? String ?: "",
                    productName = itemData["productName"] as? String ?: "",
                    quantity = (itemData["quantity"] as? Number)?.toInt() ?: 0,
                    unitPrice = (itemData["unitPrice"] as? Number)?.toDouble() ?: 0.0,
                    totalPrice = (itemData["totalPrice"] as? Number)?.toDouble() ?: 0.0,
                    saleByBasket = itemData["saleByBasket"] as? Boolean ?: false
                )
            } ?: emptyList()
            
            val totalAmount = (data["totalAmount"] as? Number)?.toDouble() ?: 0.0
            val profit = (data["profit"] as? Number)?.toDouble() ?: 0.0
            
            // Game specific
            val isGameSale = data["isGameSale"] as? Boolean ?: false
            val gameId = data["gameId"] as? String
            
            // Legacy compatibility
            val productId = data["productId"] as? String ?: ""
            val productName = data["productName"] as? String ?: ""
            val quantity = (data["quantity"] as? Number)?.toInt() ?: 0
            val price = (data["price"] as? Number)?.toDouble() ?: 0.0

            Sale(
                id = id,
                items = items,
                totalAmount = totalAmount,
                profit = profit,
                date = date,
                tableId = tableId,
                type = type,
                sellerId = sellerId,
                clientId = clientId,
                isPaid = isPaid,
                isGameSale = isGameSale,
                gameId = gameId,
                productId = productId,
                productName = productName,
                quantity = quantity,
                price = price
            )
        } catch (e: Exception) {
            android.util.Log.e("SalesRepositoryImpl", "Error deserializing sale: ${e.message}", e)
            null
        }
    }

    override fun getSales(type: SaleType?, tableId: String?): Flow<List<Sale>> = callbackFlow {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        
        var query: com.google.firebase.firestore.Query = firestore
            .collection("businesses")
            .document(userId)
            .collection("sales")
        
        if (type != null) {
            query = query.whereEqualTo("type", type.name)
        }
        if (tableId != null) {
            query = query.whereEqualTo("tableId", tableId)
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    android.util.Log.w("SalesRepo", "Permiso denegado en listener de ventas (posible logout)")
                    close()
                } else {
                    close(error)
                }
                return@addSnapshotListener
            }
            
            val sales = snapshot?.documents?.mapNotNull { it.toSale() } ?: emptyList()
            trySend(sales)
        }
        awaitClose { listener.remove() }
    }

    override suspend fun addSale(sale: Sale) {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            throw IllegalStateException("Usuario no autenticado")
        }
        
        try {
            val salesCollection = firestore
                .collection("businesses")
                .document(userId)
                .collection("sales")
            
            val saleRef = if (sale.id.isBlank())
                salesCollection.document()
            else
                salesCollection.document(sale.id)

            val finalSale = sale.copy(id = saleRef.id)

            firestore.runTransaction { transaction ->

                /* =========================
                 * 1️⃣ TODAS LAS LECTURAS
                 * ========================= */

                val productSnapshots = mutableMapOf<String, DocumentSnapshot>()
                val productRefs = mutableMapOf<String, com.google.firebase.firestore.DocumentReference>()

                if (!finalSale.isGameSale) {
                    if (finalSale.items.isNotEmpty()) {
                        finalSale.items.forEach { item ->
                            val ref = firestore.collection("businesses").document(userId).collection("products").document(item.productId)
                            productRefs[item.productId] = ref
                            productSnapshots[item.productId] = transaction.get(ref)
                        }
                    } else if (finalSale.productId.isNotBlank()) {
                        val ref = firestore.collection("businesses").document(userId).collection("products").document(finalSale.productId)
                        productRefs[finalSale.productId] = ref
                        productSnapshots[finalSale.productId] = transaction.get(ref)
                    }
                }

                val clientRef = if (!finalSale.isPaid && finalSale.clientId.isNotBlank())
                    firestore.collection("businesses").document(userId).collection("clients").document(finalSale.clientId)
                else null

                val clientSnapshot = clientRef?.let { transaction.get(it) }

                /* =========================
                 * 2️⃣ PREPARAR DATOS DE VENTA
                 * ========================= */

                val saleData: HashMap<String, Any> = hashMapOf(
                    "id" to finalSale.id,
                    "date" to finalSale.date,
                    "tableId" to (finalSale.tableId ?: ""),
                    "type" to finalSale.type.name,
                    "sellerId" to finalSale.sellerId,
                    "clientId" to finalSale.clientId,
                    "paid" to finalSale.isPaid,
                    "totalAmount" to finalSale.totalAmount,
                    "profit" to finalSale.profit,
                    "isGameSale" to finalSale.isGameSale,
                    "gameId" to (finalSale.gameId ?: ""),
                    "items" to finalSale.items.map { item ->
                        mapOf(
                            "productId" to item.productId,
                            "productName" to item.productName,
                            "quantity" to item.quantity,
                            "unitPrice" to item.unitPrice,
                            "totalPrice" to item.totalPrice,
                            "saleByBasket" to item.saleByBasket
                        )
                    },
                    // Legacy
                    "productId" to finalSale.productId,
                    "productName" to finalSale.productName,
                    "quantity" to finalSale.quantity,
                    "price" to finalSale.price
                )

                /* =========================
                 * 3️⃣ ESCRITURAS
                 * ========================= */

                // 🔹 Actualizar inventario
                if (!finalSale.isGameSale) {
                    if (finalSale.items.isNotEmpty()) {
                        finalSale.items.forEach { item ->
                            val snapshot = productSnapshots[item.productId] ?: return@forEach
                            if (snapshot.exists()) {
                                val stock = (snapshot.getLong("stock") ?: 0).toInt()
                                val unitsPerPackage = (snapshot.getLong("unitsPerPackage") ?: 1).toInt()

                                val unitsToSubtract = if (item.saleByBasket) {
                                    item.quantity * unitsPerPackage
                                } else {
                                    item.quantity
                                }

                                val newStock = (stock - unitsToSubtract).coerceAtLeast(0)
                                transaction.update(productRefs[item.productId]!!, "stock", newStock)
                            }
                        }
                    } else if (finalSale.productId.isNotBlank()) {
                        val snapshot = productSnapshots[finalSale.productId]
                        if (snapshot != null && snapshot.exists()) {
                            val stock = (snapshot.getLong("stock") ?: 0).toInt()
                            val newStock = (stock - finalSale.quantity).coerceAtLeast(0)
                            transaction.update(productRefs[finalSale.productId]!!, "stock", newStock)
                        }
                    }
                }

                // 🔹 Guardar venta
                transaction.set(saleRef, saleData)

                // 🔹 Actualizar deuda del cliente
                if (clientSnapshot != null && clientSnapshot.exists()) {
                    val deudaOriginal = clientSnapshot.getDouble("deudaOriginal") ?: 0.0
                    val totalPagado = clientSnapshot.getDouble("totalPagado") ?: 0.0
                    val deudaActual = clientSnapshot.getDouble("deuda") ?: 0.0

                    val saleAmount = if (finalSale.items.isNotEmpty())
                        finalSale.totalAmount
                    else
                        finalSale.price

                    val resetPagado = deudaActual <= 0.01
                    val newDeudaOriginal = deudaOriginal + saleAmount
                    val newDeuda = if (resetPagado) {
                        newDeudaOriginal
                    } else {
                        (newDeudaOriginal - totalPagado).coerceAtLeast(0.0)
                    }

                    transaction.update(clientRef!!, mapOf(
                        "deuda" to newDeuda,
                        "deudaOriginal" to newDeudaOriginal,
                        "totalPagado" to if (resetPagado) 0.0 else totalPagado
                    ))
                }

                null
            }.await()

            android.util.Log.d("SalesRepositoryImpl", "Sale added successfully:")

        } catch (e: Exception) {
            android.util.Log.e("SalesRepositoryImpl", "Error adding sale: ${e.message}", e)
            throw e
        }
    }


    override suspend fun getSaleById(id: String): Sale? {
        val userId = authRepository.getCurrentUserId() ?: return null
        val doc = firestore.collection("businesses").document(userId).collection("sales").document(id).get().await()
        return doc.toSale()
    }

    override suspend fun deleteSale(id: String) {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            throw IllegalStateException("Usuario no autenticado")
        }
        
        try {
            firestore.runTransaction { transaction ->
                val saleRef = firestore.collection("businesses").document(userId).collection("sales").document(id)
                val saleSnapshot = transaction.get(saleRef)
                val sale = saleSnapshot.toSale() ?: return@runTransaction

                // Restore Inventory
                if (!sale.isGameSale) {
                     if (sale.items.isNotEmpty()) {
                        sale.items.forEach { item ->
                            val productRef = firestore.collection("businesses").document(userId).collection("products").document(item.productId)
                            val productSnapshot = transaction.get(productRef)
                            if (productSnapshot.exists()) {
                                val currentStock = (productSnapshot.getLong("stock") ?: 0).toInt()
                                val unitsPerPackage = (productSnapshot.getLong("unitsPerPackage") ?: 1).toInt()
                                
                                val unitsToRestore = if (item.saleByBasket) {
                                    item.quantity * unitsPerPackage
                                } else {
                                    item.quantity
                                }
                                
                                val newStock = currentStock + unitsToRestore
                                transaction.update(productRef, "stock", newStock)
                            }
                        }
                    } else if (sale.productId.isNotBlank()) {
                        val productRef = firestore.collection("businesses").document(userId).collection("products").document(sale.productId)
                        val productSnapshot = transaction.get(productRef)
                        if (productSnapshot.exists()) {
                            val currentStock = (productSnapshot.getLong("stock") ?: 0).toInt()
                            val newStock = currentStock + sale.quantity
                            transaction.update(productRef, "stock", newStock)
                        }
                    }
                }

                // Restore Client Debt (if unpaid)
                if (!sale.isPaid && sale.clientId.isNotBlank()) {
                    val clientRef = firestore.collection("businesses").document(userId).collection("clients").document(sale.clientId)
                    val clientSnapshot = transaction.get(clientRef)
                    if (clientSnapshot.exists()) {
                        val currentDeudaOriginal = clientSnapshot.getDouble("deudaOriginal") ?: 0.0
                        val currentTotalPagado = clientSnapshot.getDouble("totalPagado") ?: 0.0
                        
                        val saleAmount = if (sale.items.isNotEmpty()) sale.totalAmount else sale.price
                        
                        val newDeudaOriginal = (currentDeudaOriginal - saleAmount).coerceAtLeast(0.0)
                        val newDebt = (newDeudaOriginal - currentTotalPagado).coerceAtLeast(0.0)
                        
                        transaction.update(clientRef, "deuda", newDebt)
                        transaction.update(clientRef, "deudaOriginal", newDeudaOriginal)
                    }
                }

                // Delete Sale
                transaction.delete(saleRef)
            }.await()
            
            android.util.Log.d("SalesRepositoryImpl", "Sale deleted successfully:")
        } catch (e: Exception) {
            android.util.Log.e("SalesRepositoryImpl", "Error deleting sale: ${e.message}", e)
            throw e
        }
    }

    override suspend fun markSaleAsPaid(id: String) {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            throw IllegalStateException("Usuario no autenticado")
        }
        
        try {
            firestore.collection("businesses").document(userId).collection("sales").document(id).update("paid", true).await()
        } catch (e: Exception) {
            android.util.Log.e("SalesRepositoryImpl", "Error marking sale as paid: ${e.message}", e)
            throw e
        }
    }
} 
