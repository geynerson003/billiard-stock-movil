package com.lealcode.inventariobillar.utils

import android.util.Patterns

/**
 * Utilidad para validar correos electronicos de formularios de autenticacion.
 */
object EmailValidator {
    
    /**
     * Indica si el correo tiene formato valido.
     */
    fun isValid(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    /**
     * Devuelve un mensaje de validacion legible para UI o `null` si no hay error.
     */
    fun getError(email: String): String? {
        return when {
            email.isBlank() -> "El email es requerido"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Email inválido"
            else -> null
        }
    }
}
