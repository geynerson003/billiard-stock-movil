package com.lealcode.inventariobillar.ui.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lealcode.inventariobillar.data.model.AuthState
import com.lealcode.inventariobillar.data.repository.AuthRepository
import com.lealcode.inventariobillar.utils.EmailValidator
import com.lealcode.inventariobillar.utils.PasswordValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
/**
 * ViewModel responsable del ciclo de autenticacion y de la validacion de formularios.
 */
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val currentUserId: Flow<String?> = authRepository.getAuthStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    private val _businessNameError = MutableStateFlow<String?>(null)
    val businessNameError: StateFlow<String?> = _businessNameError.asStateFlow()

    private val _confirmPasswordError = MutableStateFlow<String?>(null)
    val confirmPasswordError: StateFlow<String?> = _confirmPasswordError.asStateFlow()

    private val _passwordStrength = MutableStateFlow(PasswordValidator.PasswordStrength.WEAK)
    val passwordStrength: StateFlow<PasswordValidator.PasswordStrength> = _passwordStrength.asStateFlow()

    /**
     * Intenta iniciar sesion si las validaciones locales son satisfactorias.
     */
    fun login(email: String, password: String) {
        // Validar campos
        if (!validateLoginFields(email, password)) {
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            val result = authRepository.login(email, password)
            
            _authState.value = result.fold(
                onSuccess = { user -> AuthState.Success(user) },
                onFailure = { error -> AuthState.Error(error.message ?: "Error desconocido") }
            )
        }
    }

    /**
     * Registra un nuevo usuario y negocio.
     */
    fun register(email: String, password: String, confirmPassword: String, businessName: String) {
        // Validar campos
        if (!validateRegisterFields(email, password, confirmPassword, businessName)) {
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            val result = authRepository.register(email, password, businessName)
            
            _authState.value = result.fold(
                onSuccess = { user -> AuthState.Success(user) },
                onFailure = { error -> AuthState.Error(error.message ?: "Error desconocido") }
            )
        }
    }

    /**
     * Cierra la sesion activa y restablece el estado local.
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _authState.value = AuthState.Idle
        }
    }

    /**
     * Valida el correo ingresado y actualiza el mensaje de error visible.
     */
    fun validateEmail(email: String) {
        _emailError.value = EmailValidator.getError(email)
    }

    /**
     * Valida la contrasena y actualiza tanto el error como el indicador de fortaleza.
     */
    fun validatePassword(password: String) {
        val validation = PasswordValidator.validate(password)
        _passwordError.value = if (validation.isValid) null else validation.errors.firstOrNull()
        _passwordStrength.value = PasswordValidator.getStrength(password)
    }

    /**
     * Valida el nombre del negocio.
     */
    fun validateBusinessName(name: String) {
        _businessNameError.value = when {
            name.isBlank() -> "El nombre del negocio es requerido"
            name.length < 3 -> "El nombre debe tener al menos 3 caracteres"
            else -> null
        }
    }

    /**
     * Verifica que la confirmacion coincida con la contrasena original.
     */
    fun validateConfirmPassword(password: String, confirmPassword: String) {
        _confirmPasswordError.value = when {
            confirmPassword.isBlank() -> "Confirma tu contraseña"
            password != confirmPassword -> "Las contraseñas no coinciden"
            else -> null
        }
    }

    /**
     * Limpia el estado de autenticacion para reutilizar la pantalla.
     */
    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }

    /**
     * Solicita el envio del correo de recuperacion de contrasena.
     */
    fun resetPassword(email: String) {
        if (email.isBlank() || EmailValidator.getError(email) != null) {
            _emailError.value = "Ingresa un correo electrónico válido"
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.resetPassword(email)
            _authState.value = result.fold(
                onSuccess = { AuthState.Success(null) }, // Success sin usuario
                onFailure = { error -> AuthState.Error(error.message ?: "Error al enviar correo") }
            )
        }
    }

    private fun validateLoginFields(email: String, password: String): Boolean {
        var isValid = true

        val emailError = EmailValidator.getError(email)
        if (emailError != null) {
            _emailError.value = emailError
            isValid = false
        }

        if (password.isBlank()) {
            _passwordError.value = "La contraseña es requerida"
            isValid = false
        }

        return isValid
    }

    private fun validateRegisterFields(
        email: String,
        password: String,
        confirmPassword: String,
        businessName: String
    ): Boolean {
        var isValid = true

        // Validar email
        val emailError = EmailValidator.getError(email)
        if (emailError != null) {
            _emailError.value = emailError
            isValid = false
        }

        // Validar contraseña
        val passwordValidation = PasswordValidator.validate(password)
        if (!passwordValidation.isValid) {
            _passwordError.value = passwordValidation.errors.firstOrNull()
            isValid = false
        }

        // Validar confirmación de contraseña
        if (password != confirmPassword) {
            _confirmPasswordError.value = "Las contraseñas no coinciden"
            isValid = false
        }

        // Validar nombre del negocio
        if (businessName.isBlank()) {
            _businessNameError.value = "El nombre del negocio es requerido"
            isValid = false
        } else if (businessName.length < 3) {
            _businessNameError.value = "El nombre debe tener al menos 3 caracteres"
            isValid = false
        }

        return isValid
    }

    /**
     * Limpia errores de validacion mostrados en UI.
     */
    fun clearErrors() {
        _emailError.value = null
        _passwordError.value = null
        _businessNameError.value = null
        _confirmPasswordError.value = null
    }
}
