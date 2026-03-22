package com.lealcode.inventariobillar.data.repository

import android.util.Log
import com.lealcode.inventariobillar.data.model.Table
import com.lealcode.inventariobillar.data.model.TableSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose

/**
 * Implementacion de [TablesRepository] para mesas y sesiones activas.
 */
class TablesRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : TablesRepository {
    private val tables = MutableStateFlow<List<Table>>(emptyList())
    private val sessions = MutableStateFlow<List<TableSession>>(emptyList())

    override fun getTables(): Flow<List<Table>> = flow {
        val userId = authRepository.getCurrentUserId()
        if (userId != null) {
            val snapshot = firestore.collection("businesses").document(userId).collection("tables").get().await()
            emit(snapshot.documents.mapNotNull { it.toObject(Table::class.java) })
        } else {
            emit(emptyList())
        }
    }

    override suspend fun addTable(table: Table) {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            throw IllegalStateException("Usuario no autenticado")
        }
        firestore.collection("businesses").document(userId).collection("tables").document(table.id).set(table).await()
        Log.d("DEBUG:", " Table saved to Firestore")
    }

    override suspend fun updateTable(table: Table) {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            throw IllegalStateException("Usuario no autenticado")
        }
        Log.d("DEBUG:",  " Updating table")
        firestore.collection("businesses").document(userId).collection("tables").document(table.id).set(table).await()
        Log.d("DEBUG:", " Table updated in Firestore")
    }

    override suspend fun deleteTable(tableId: String) {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            throw IllegalStateException("Usuario no autenticado")
        }
        firestore.collection("businesses").document(userId).collection("tables").document(tableId).delete().await()
    }



    override suspend fun startSession(tableId: String): TableSession {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            throw IllegalStateException("Usuario no autenticado")
        }
        
        val sessionsCollection = firestore.collection("businesses").document(userId).collection("table_sessions")
        val session = TableSession(id = sessionsCollection.document().id, tableId = tableId)
        sessionsCollection.document(session.id).set(session).await()
        
        val tableDoc = firestore.collection("businesses").document(userId).collection("tables").document(tableId)
        val table = tableDoc.get().await().toObject(Table::class.java)
        if (table != null) {
            tableDoc.set(table.copy(currentSessionId = session.id)).await()
        }
        return session
    }



    override fun getTablesRealtime(): Flow<List<Table>> = callbackFlow {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        
        val listener = firestore.collection("businesses").document(userId).collection("tables").addSnapshotListener { snapshot, error ->
            if (error != null) {
                if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    Log.w("TablesRepo", "Permiso denegado en listener de mesas (posible logout.)")
                    close()
                } else {
                    close(error)
                }
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val tables = snapshot.documents.mapNotNull { doc ->
                    try {
                        val table = doc.toObject(Table::class.java)
                        table
                    } catch (e: Exception) {
                        Log.d("DEBUG:" , " Error deserializing table: ${e.message}")
                        null
                    }
                }
                Log.d("DEBUG:", " Retrieved tables: ${tables.map { it.name }}")
                trySend(tables)
            }
        }
        awaitClose { listener.remove() }
    }
}
