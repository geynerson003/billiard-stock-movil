package com.lealcode.inventariobillar.ui.feature.auth.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lealcode.inventariobillar.ui.theme.*
import com.lealcode.inventariobillar.utils.PasswordValidator

@Composable
/**
 * Indicador visual de fortaleza para contrasenas ingresadas por el usuario.
 */
fun PasswordStrengthIndicator(
    strength: PasswordValidator.PasswordStrength,
    modifier: Modifier = Modifier
) {
    val (color, text, progress) = when (strength) {
        PasswordValidator.PasswordStrength.WEAK -> Triple(RedAccent, "Débil", 0.33f)
        PasswordValidator.PasswordStrength.MEDIUM -> Triple(OrangeBright, "Media", 0.66f)
        PasswordValidator.PasswordStrength.STRONG -> Triple(VerdeAcento, "Fuerte", 1f)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300),
        label = "progress"
    )

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Fortaleza de contraseña:",
                style = MaterialTheme.typography.bodySmall,
                color = GrisTextoSecundario
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = color
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = color,
            trackColor = BordeTarjeta,
        )
    }
}
