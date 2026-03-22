package com.lealcode.inventariobillar.ui.feature.auth

import androidx.compose.animation.*
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
import com.lealcode.inventariobillar.ui.theme.*

@Composable
/**
 * Pantalla para solicitar recuperacion de contrasena por correo.
 */
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()
    val emailError by viewModel.emailError.collectAsState()

    var showSuccessMessage by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            showSuccessMessage = true
            // No resetAuthState aquí para mantener el mensaje de éxito visible un momento
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
            // Icono
            Icon(
                imageVector = Icons.Filled.LockReset,
                contentDescription = "Restablecer",
                modifier = Modifier.size(80.dp),
                tint = BlueVivid
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Recuperar Contraseña",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = BlueVivid,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Te enviaremos un correo con instrucciones para restablecer tu contraseña",
                style = MaterialTheme.typography.bodyMedium,
                color = GrisTextoSecundario,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Card
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
                    if (showSuccessMessage) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = VerdeAcento,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "¡Correo enviado!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = VerdeAcento
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Revisa tu bandeja de entrada o SPAM y sigue los pasos.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GrisTextoSecundario,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        AuthButton(
                            text = "Volver al Inicio Sesi\u00f3n",
                            onClick = onNavigateBack,
                            isLoading = false
                        )
                    } else {
                        AuthTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                viewModel.validateEmail(it)
                            },
                            label = "Email",
                            leadingIcon = Icons.Filled.Email,
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done,
                            onImeAction = {
                                if (email.isNotBlank() && emailError == null) {
                                    viewModel.resetPassword(email)
                                }
                            },
                            isError = emailError != null,
                            errorMessage = emailError,
                            enabled = authState !is AuthState.Loading
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Error general
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

                        AuthButton(
                            text = "Enviar Enlace",
                            onClick = { viewModel.resetPassword(email) },
                            isLoading = authState is AuthState.Loading,
                            enabled = email.isNotBlank() && emailError == null
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        TextButton(onClick = onNavigateBack) {
                            Text(
                                text = "Volver al Inicio Sesi\u00f3n",
                                color = BlueVivid,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
