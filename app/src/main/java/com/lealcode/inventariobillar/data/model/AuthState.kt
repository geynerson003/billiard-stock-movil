package com.lealcode.inventariobillar.data.model

/**
 * Estado observable del flujo de autenticacion.
 */
sealed class AuthState {
    /**
     * Estado inicial sin operaciones pendientes.
     */
    object Idle : AuthState()
    /**
     * Estado temporal mientras se procesa una accion de autenticacion.
     */
    object Loading : AuthState()
    /**
     * Estado exitoso. Puede incluir un usuario o venir vacio en operaciones como reset password.
     */
    data class Success(val user: User? = null) : AuthState()
    /**
     * Estado de error con un mensaje listo para mostrar en UI.
     */
    data class Error(val message: String) : AuthState()
}
