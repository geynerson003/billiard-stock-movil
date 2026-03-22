package com.lealcode.inventariobillar.utils

/**
 * Reglas de validacion y fuerza para contrasenas del modulo de autenticacion.
 */
object PasswordValidator {
    
    private const val MIN_LENGTH = 8
    
    /**
     * Resultado de la validacion estructural de una contrasena.
     */
    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String> = emptyList()
    )
    
    /**
     * Ejecuta las reglas minimas de seguridad definidas por la aplicacion.
     */
    fun validate(password: String): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (password.length < MIN_LENGTH) {
            errors.add("La contraseña debe tener al menos $MIN_LENGTH caracteres")
        }
        
        if (!password.any { it.isUpperCase() }) {
            errors.add("Debe contener al menos una letra mayúscula")
        }
        
        if (!password.any { it.isDigit() }) {
            errors.add("Debe contener al menos un número")
        }
        
        if (!password.any { !it.isLetterOrDigit() }) {
            errors.add("Debe contener al menos un carácter especial")
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    /**
     * Estima la fortaleza de una contrasena para retroalimentacion visual.
     */
    fun getStrength(password: String): PasswordStrength {
        var score = 0
        
        if (password.length >= MIN_LENGTH) score++
        if (password.length >= 12) score++
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++
        
        return when {
            score < 3 -> PasswordStrength.WEAK
            score < 5 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.STRONG
        }
    }
    
    /**
     * Niveles de fortaleza usados por la UI.
     */
    enum class PasswordStrength {
        WEAK, MEDIUM, STRONG
    }
}
