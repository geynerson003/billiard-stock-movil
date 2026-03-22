package com.lealcode.inventariobillar.data.repository

import android.util.Log
import com.lealcode.inventariobillar.data.model.Expense
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

/**
 * Implementacion de [ExpensesRepository] para gastos operativos.
 */
class ExpensesRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : ExpensesRepository {
    
    companion object {
        private const val TAG = "ExpensesRepositoryImpl"
    }
    
    private val expenses = MutableStateFlow<List<Expense>>(emptyList())

    override fun getExpenses(): Flow<List<Expense>> = callbackFlow {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        

        val listener = firestore.collection("businesses").document(userId).collection("expenses").addSnapshotListener { snapshot, error ->
            if (error != null) {
                if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    Log.w(TAG, "Permiso denegado en listener de gastos (posible logout)")
                    close()
                } else {
                    Log.e(TAG, "Error en listener de gastos: ${error.message}", error)
                    close(error)
                }
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val expenses = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Expense::class.java)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al deserializar gasto: ${e.message}", e)
                        null
                    }
                }

                trySend(expenses)
            }
        }
        awaitClose {
            listener.remove() 
        }
    }

    override suspend fun addExpense(expense: Expense) {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            throw IllegalStateException("Usuario no autenticado")
        }
        
        try {
            val expensesCollection = firestore.collection("businesses").document(userId).collection("expenses")
            val expenseWithId = if (expense.id.isBlank()) {
                expense.copy(id = expensesCollection.document().id)
            } else {
                expense
            }
            expensesCollection.document(expenseWithId.id).set(expenseWithId).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error al agregar gasto: ${e.message}", e)
            throw e
        }
    }

    override suspend fun updateExpense(expense: Expense) {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            throw IllegalStateException("Usuario no autenticado")
        }
        
        try {
            firestore.collection("businesses").document(userId).collection("expenses").document(expense.id).set(expense).await()
            Log.d(TAG, "Gasto actualizado exitosamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar gasto: ${e.message}", e)
            throw e
        }
    }

    override suspend fun deleteExpense(expenseId: String) {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            throw IllegalStateException("Usuario no autenticado")
        }
        
        try {
            firestore.collection("businesses").document(userId).collection("expenses").document(expenseId).delete().await()
            Log.d(TAG, "Gasto eliminado exitosamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar gasto: ${e.message}", e)
            throw e
        }
    }


} 
