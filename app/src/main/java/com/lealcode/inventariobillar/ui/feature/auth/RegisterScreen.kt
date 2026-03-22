package com.lealcode.inventariobillar.ui.feature.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lealcode.inventariobillar.data.model.AuthState
import com.lealcode.inventariobillar.ui.feature.auth.components.AuthButton
import com.lealcode.inventariobillar.ui.feature.auth.components.AuthTextField
import com.lealcode.inventariobillar.ui.feature.auth.components.PasswordStrengthIndicator
import com.lealcode.inventariobillar.ui.theme.*

@Composable
/**
 * Pantalla de registro de una nueva cuenta de negocio.
 */
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var businessName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val businessNameError by viewModel.businessNameError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()
    val confirmPasswordError by viewModel.confirmPasswordError.collectAsState()
    val passwordStrength by viewModel.passwordStrength.collectAsState()

    // Manejar el estado de autenticación
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                onRegisterSuccess()
                viewModel.resetAuthState()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BackgroundGradientStart,
                        BackgroundGradientEnd
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo y título
            Icon(
                imageVector = Icons.Filled.SportsBar,
                contentDescription = "Logo",
                modifier = Modifier.size(80.dp),
                tint = BlueVivid
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Crear Cuenta",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = BlueVivid
            )

            Text(
                text = "Registra tu negocio y comienza a gestionar",
                style = MaterialTheme.typography.bodyMedium,
                color = GrisTextoSecundario,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Card de registro
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = FondoTarjeta
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Información del Negocio",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = BlueVivid
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Campo de nombre del negocio
                    AuthTextField(
                        value = businessName,
                        onValueChange = {
                            businessName = it
                            viewModel.validateBusinessName(it)
                        },
                        label = "Nombre del Negocio",
                        leadingIcon = Icons.Filled.Business,
                        imeAction = ImeAction.Next,
                        isError = businessNameError != null,
                        errorMessage = businessNameError,
                        enabled = authState !is AuthState.Loading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo de email
                    AuthTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            viewModel.validateEmail(it)
                        },
                        label = "Email",
                        leadingIcon = Icons.Filled.Email,
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                        isError = emailError != null,
                        errorMessage = emailError,
                        enabled = authState !is AuthState.Loading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo de contraseña
                    AuthTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            viewModel.validatePassword(it)
                            if (confirmPassword.isNotBlank()) {
                                viewModel.validateConfirmPassword(it, confirmPassword)
                            }
                        },
                        label = "Contraseña",
                        leadingIcon = Icons.Filled.Lock,
                        isPassword = true,
                        imeAction = ImeAction.Next,
                        isError = passwordError != null,
                        errorMessage = passwordError,
                        enabled = authState !is AuthState.Loading
                    )

                    // Indicador de fortaleza de contraseña
                    if (password.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        PasswordStrengthIndicator(
                            strength = passwordStrength,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo de confirmar contraseña
                    AuthTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            viewModel.validateConfirmPassword(password, it)
                        },
                        label = "Confirmar Contraseña",
                        leadingIcon = Icons.Filled.LockOpen,
                        isPassword = true,
                        imeAction = ImeAction.Done,
                        onImeAction = {
                            if (email.isNotBlank() && businessName.isNotBlank() && 
                                password.isNotBlank() && confirmPassword.isNotBlank()) {
                                viewModel.register(email, password, confirmPassword, businessName)
                            }
                        },
                        isError = confirmPasswordError != null,
                        errorMessage = confirmPasswordError,
                        enabled = authState !is AuthState.Loading
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Mostrar error general
                    AnimatedVisibility(
                        visible = authState is AuthState.Error,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = RedAccent.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Error,
                                    contentDescription = null,
                                    tint = RedAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = (authState as? AuthState.Error)?.message ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = RedAccent
                                )
                            }
                        }
                    }

                    // Información de seguridad
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = BlueVivid.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = BlueVivid,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Requisitos de contraseña:",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = BlueVivid
                                )
                                Text(
                                    text = "• Mínimo 8 caracteres\n• Al menos una mayúscula\n• Al menos un número\n• Al menos un carácter especial",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BlueVivid
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón de registro
                    AuthButton(
                        text = "Crear Cuenta",
                        onClick = { 
                            viewModel.register(email, password, confirmPassword, businessName)
                        },
                        isLoading = authState is AuthState.Loading,
                        enabled = email.isNotBlank() && businessName.isNotBlank() && 
                                password.isNotBlank() && confirmPassword.isNotBlank()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = BordeTarjeta)
                        Text(
                            text = "  o  ",
                            style = MaterialTheme.typography.bodySmall,
                            color = GrisTextoSecundario
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = BordeTarjeta)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botón de login
                    TextButton(
                        onClick = onNavigateToLogin,
                        enabled = authState !is AuthState.Loading
                    ) {
                        Text(
                            text = "¿Ya tienes cuenta? Inicia sesión aquí",
                            color = BlueVivid,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer
            Text(
                text = "© 2026 Inventario Billar",
                style = MaterialTheme.typography.bodySmall,
                color = GrisTextoSecundario
            )
        }
    }
}
