package com.lealcode.inventariobillar.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.lealcode.inventariobillar.data.model.Payment
import com.lealcode.inventariobillar.data.model.PaymentMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import javax.inject.Inject

/**
 * Implementacion de [PaymentRepository] almacenada en Firestore.
 */
class PaymentRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : PaymentRepository {

    private fun com.google.firebase.firestore.DocumentSnapshot.toPayment(): Payment? {
        return try {
            val data = this.data ?: return null
            
            val id = data["id"] as? String ?: ""
            val clientId = data["clientId"] as? String ?: ""
            val amount = data["amount"] as? Double ?: 0.0
            val date = data["date"] as? Long ?: System.currentTimeMillis()
            val description = data["description"] as? String ?: ""
            val paymentMethod = (data["paymentMethod"] as? String)?.let { PaymentMethod.valueOf(it) } ?: PaymentMethod.CASH
            val relatedSales = data["relatedSales"] as? List<String> ?: emptyList()
            val isPartialPayment = data["isPartialPayment"] as? Boolean ?: true
            val notes = data["notes"] as? String ?: ""
            
            Payment(
                id = id,
                clientId = clientId,
                amount = amount,
                date = date,
                description = description,
                paymentMethod = paymentMethod,
                relatedSales = relatedSales,
                isPartialPayment = isPartialPayment,
                notes = notes
            )
        } catch (e: Exception) {
            android.util.Log.e("PaymentRepositoryImpl", "Error al deserializar pago: ${e.message}", e)
            null
        }
    }

    override fun getPaymentsByClient(clientId: String): Flow<List<Payment>> = callbackFlow {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        
        val query = firestore.collection("businesses").document(userId).collection("payments").whereEqualTo("clientId", clientId)
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    android.util.Log.w("PaymentRepo", "Permiso denegado en listener de pagos por cliente (posible logout)")
                    close()
                } else {
                    close(error)
                }
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val payments = snapshot.documents.mapNotNull { doc ->
                    doc.toPayment()
                }
                trySend(payments)
            }
        }
        awaitClose { listener.remove() }
    }

    override fun getAllPayments(): Flow<List<Payment>> = callbackFlow {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        
        val query = firestore.collection("businesses").document(userId).collection("payments")
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    android.util.Log.w("PaymentRepo", "Permiso denegado en listener de todos los pagos (posible logout)")
                    close()
                } else {
                    close(error)
                }
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val payments = snapshot.documents.mapNotNull { doc ->
                    doc.toPayment()
                }
                trySend(payments)
            }
        }
        awaitClose { listener.remove() }
    }

    override suspend fun addPayment(payment: Payment) {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            throw IllegalStateException("Usuario no autenticado")
        }
        
        try {
            val paymentsCollection = firestore.collection("businesses").document(userId).collection("payments")
            val paymentWithId = if (payment.id.isBlank()) {
                val docRef = paymentsCollection.document()
                payment.copy(id = docRef.id)
            } else {
                payment
            }
            
            val paymentData = mapOf(
                "id" to paymentWithId.id,
                "clientId" to paymentWithId.clientId,
                "amount" to paymentWithId.amount,
                "date" to paymentWithId.date,
                "description" to paymentWithId.description,
                "paymentMethod" to paymentWithId.paymentMethod.name,
                "relatedSales" to paymentWithId.relatedSales,
                "isPartialPayment" to paymentWithId.isPartialPayment,
                "notes" to paymentWithId.notes
            )
            
            paymentsCollection.document(paymentWithId.id).set(paymentData).await()

        } catch (e: Exception) {
            android.util.Log.e("PaymentRepositoryImpl", "Error al agregar pago: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getPaymentById(id: String): Payment? {
        val userId = authRepository.getCurrentUserId() ?: return null
        val doc = firestore.collection("businesses").document(userId).collection("payments").document(id).get().await()
        return doc.toPayment()
    }

    override suspend fun deletePayment(id: String) {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            throw IllegalStateException("Usuario no autenticado")
        }
        
        try {
            firestore.collection("businesses").document(userId).collection("payments").document(id).delete().await()
            android.util.Log.d("PaymentRepositoryImpl", "Pago eliminado exitosamente: $id")
        } catch (e: Exception) {
            android.util.Log.e("PaymentRepositoryImpl", "Error al eliminar pago: ${e.message}", e)
            throw e
        }
    }

    override suspend fun updatePayment(payment: Payment) {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            throw IllegalStateException("Usuario no autenticado")
        }
        
        try {
            val paymentData = mapOf(
                "id" to payment.id,
                "clientId" to payment.clientId,
                "amount" to payment.amount,
                "date" to payment.date,
                "description" to payment.description,
                "paymentMethod" to payment.paymentMethod.name,
                "relatedSales" to payment.relatedSales,
                "isPartialPayment" to payment.isPartialPayment,
                "notes" to payment.notes
            )
            
            firestore.collection("businesses").document(userId).collection("payments").document(payment.id).set(paymentData).await()
            android.util.Log.d("PaymentRepositoryImpl", "Pago actualizado exitosamente:")
        } catch (e: Exception) {
            android.util.Log.e("PaymentRepositoryImpl", "Error al actualizar pago: ${e.message}", e)
            throw e
        }
    }
}
