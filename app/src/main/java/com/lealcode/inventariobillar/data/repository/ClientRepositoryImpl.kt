package com.lealcode.inventariobillar.data.repository

import com.lealcode.inventariobillar.data.model.Client
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose

/**
 * Implementacion de [ClientRepository] respaldada por Firestore.
 */
class ClientRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : ClientRepository {

    private val clients = mutableListOf<Client>()
    private val mutex = Mutex()

    override suspend fun addClient(client: Client) {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            throw IllegalStateException("Usuario no autenticado")
        }
        
        val clientsCollection = firestore.collection("businesses").document(userId).collection("clients")
        val clientWithId = if (client.id.isBlank()) {
            client.copy(id = clientsCollection.document().id)
        } else {
            client
        }
        clientsCollection.document(clientWithId.id).set(clientWithId).await()
    }

    override suspend fun getClients(): List<Client> {
        val userId = authRepository.getCurrentUserId() ?: return emptyList()
        val snapshot = firestore.collection("businesses").document(userId).collection("clients").get().await()
        return snapshot.documents.mapNotNull { it.toObject(Client::class.java) }
    }

    override suspend fun getClientById(id: String): Client? {
        val userId = authRepository.getCurrentUserId() ?: return null
        val doc = firestore.collection("businesses").document(userId).collection("clients").document(id).get().await()
        val client = doc.toObject(Client::class.java)

        return client
    }

    override suspend fun updateClient(client: Client) {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            throw IllegalStateException("Usuario no autenticado")
        }

        firestore.collection("businesses").document(userId).collection("clients").document(client.id).set(client).await()
        
        android.util.Log.d("ClientRepositoryImpl", "Cliente actualizado exitosamente en Firestore")
    }

    override suspend fun deleteClient(id: String) {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            throw IllegalStateException("Usuario no autenticado")
        }
        firestore.collection("businesses").document(userId).collection("clients").document(id).delete().await()
    }

    override fun getClientsRealtime(): Flow<List<Client>> = callbackFlow {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        
        val listener = firestore.collection("businesses").document(userId).collection("clients").addSnapshotListener { snapshot, error ->
            if (error != null) {
                if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    android.util.Log.w("ClientRepo", "Permiso denegado en listener de clientes (posible logout)")
                    close()
                } else {
                    close(error)
                }
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val clients = snapshot.documents.mapNotNull { it.toObject(Client::class.java) }
                trySend(clients)
            }
        }
        awaitClose { listener.remove() }
    }
}
