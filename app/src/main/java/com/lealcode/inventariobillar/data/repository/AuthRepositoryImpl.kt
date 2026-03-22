package com.lealcode.inventariobillar.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import com.lealcode.inventariobillar.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
/**
 * Implementacion de [AuthRepository] respaldada por Firebase Authentication.
 */
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    companion object {
        private const val TAG = "AuthRepository"
        private const val USERS_COLLECTION = "users"
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: return Result.failure(
                Exception("Error al obtener el ID del usuario")
            )

            // Obtener datos del usuario desde Firestore
            val userDoc = firestore.collection(USERS_COLLECTION)
                .document(uid)
                .get()
                .await()

            val user = userDoc.toObject(User::class.java)?.copy(uid = uid)
                ?: return Result.failure(Exception("Usuario no encontrado en la base de datos"))

            Result.success(user)

        } catch (e: FirebaseAuthInvalidUserException) {
            Result.failure(Exception("Usuario no encontrado"))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("Email o contraseña incorrectos"))
        } catch (e: Exception) {
            Result.failure(Exception("Error al iniciar sesión: ${e.message}"))
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        businessName: String
    ): Result<User> {
        return try {
            // Crear usuario en Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: return Result.failure(
                Exception("Error al crear el usuario")
            )

            // Crear objeto User
            val user = User(
                uid = uid,
                email = email,
                businessName = businessName,
                createdAt = System.currentTimeMillis(),
                isActive = true
            )

            // Guardar datos del usuario en Firestore
            firestore.collection(USERS_COLLECTION)
                .document(uid)
                .set(user)
                .await()

            // Crear colección inicial del negocio
            initializeBusinessCollections(uid)

            Result.success(user)

        } catch (e: FirebaseAuthWeakPasswordException) {
            Result.failure(Exception("La contraseña es muy débil"))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("El email no es válido"))
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(Exception("Este email ya está registrado"))
        } catch (e: Exception) {
            Result.failure(Exception("Error al registrar: ${e.message}"))
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): User? {
        return try {
            val uid = auth.currentUser?.uid ?: return null

            val userDoc = firestore.collection(USERS_COLLECTION)
                .document(uid)
                .get()
                .await()

            userDoc.toObject(User::class.java)?.copy(uid = uid)
        } catch (e: Exception) {
            
            null
        }
    }

    override fun isUserLoggedIn(): Boolean {
        val oldPolicy = android.os.StrictMode.allowThreadDiskReads()
        return try {
            auth.currentUser != null
        } finally {
            android.os.StrictMode.setThreadPolicy(oldPolicy)
        }
    }

    override fun getCurrentUserId(): String? {
        val oldPolicy = android.os.StrictMode.allowThreadDiskReads()
        return try {
            auth.currentUser?.uid
        } finally {
            android.os.StrictMode.setThreadPolicy(oldPolicy)
        }
    }

    override fun getAuthStateFlow(): kotlinx.coroutines.flow.Flow<String?> = kotlinx.coroutines.flow.callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val userId = firebaseAuth.currentUser?.uid
            trySend(userId)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            
            Result.failure(Exception("Error al enviar el correo: ${e.message}"))
        }
    }

    private suspend fun initializeBusinessCollections(userId: String) {
        try {
            // Crear documento inicial en la colección del negocio para que exista
            val businessRef = firestore.collection("businesses")
                .document(userId)

            businessRef.set(mapOf("initialized" to true, "createdAt" to System.currentTimeMillis()))
                .await()

            
        } catch (e: Exception) {
            
            // No lanzamos la excepción porque el usuario ya fue creado
        }
    }
}
