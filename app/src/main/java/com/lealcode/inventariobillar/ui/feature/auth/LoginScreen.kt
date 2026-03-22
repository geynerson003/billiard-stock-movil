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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lealcode.inventariobillar.data.model.AuthState
import com.lealcode.inventariobillar.ui.feature.auth.components.AuthButton
import com.lealcode.inventariobillar.ui.feature.auth.components.AuthTextField
import com.lealcode.inventariobillar.ui.theme.*

@Composable
/**
 * Pantalla de inicio de sesion.
 */
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()

    // Manejar el estado de autenticación
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                onLoginSuccess()
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
                text = "Inventario Billar",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = BlueVivid
            )

            Text(
                text = "Gestiona tu negocio de manera profesional",
                style = MaterialTheme.typography.bodyMedium,
                color = GrisTextoSecundario,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Card de login
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
                        text = "Iniciar Sesión",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = BlueVivid
                    )

                    Spacer(modifier = Modifier.height(24.dp))

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
                            viewModel.clearErrors()
                        },
                        label = "Contraseña",
                        leadingIcon = Icons.Filled.Lock,
                        isPassword = true,
                        imeAction = ImeAction.Done,
                        onImeAction = {
                            if (email.isNotBlank() && password.isNotBlank()) {
                                viewModel.login(email, password)
                            }
                        },
                        isError = passwordError != null,
                        errorMessage = passwordError,
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

                    // Botón de login
                    AuthButton(
                        text = "Iniciar Sesión",
                        onClick = { viewModel.login(email, password) },
                        isLoading = authState is AuthState.Loading,
                        enabled = email.isNotBlank() && password.isNotBlank()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = onNavigateToForgotPassword,
                        enabled = authState !is AuthState.Loading,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = "¿Olvidaste tu contraseña?",
                            color = BlueVivid,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

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

                    // Botón de registro
                    TextButton(
                        onClick = onNavigateToRegister,
                        enabled = authState !is AuthState.Loading
                    ) {
                        Text(
                            text = "¿No tienes cuenta? Regístrate aquí",
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
