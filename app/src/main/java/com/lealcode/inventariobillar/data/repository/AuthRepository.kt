package com.lealcode.inventariobillar.data.repository

import com.lealcode.inventariobillar.data.model.User

/**
 * Contrato de autenticacion de usuarios.
 */
interface AuthRepository {
    /**
     * Inicia sesion con correo y contrasena.
     */
    suspend fun login(email: String, password: String): Result<User>
    /**
     * Crea una nueva cuenta de negocio.
     */
    suspend fun register(email: String, password: String, businessName: String): Result<User>
    /**
     * Cierra la sesion actual.
     */
    suspend fun logout(): Result<Unit>
    /**
     * Devuelve el usuario autenticado actual, si existe.
     */
    suspend fun getCurrentUser(): User?
    /**
     * Indica si hay una sesion activa en memoria.
     */
    fun isUserLoggedIn(): Boolean
    /**
     * Devuelve el identificador del usuario autenticado.
     */
    fun getCurrentUserId(): String?
    /**
     * Expone los cambios del estado de autenticacion para reaccionar desde UI.
     */
    fun getAuthStateFlow(): kotlinx.coroutines.flow.Flow<String?>
    /**
     * Solicita el envio del correo de recuperacion de contrasena.
     */
    suspend fun resetPassword(email: String): Result<Unit>
}
