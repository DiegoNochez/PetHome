package com.premiercoders.pethome.vista.tema

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta teal de los mockups de la Etapa 1.
val Teal = Color(0xFF129C8C)
val TealOscuro = Color(0xFF0D7C70)
val TealClaro = Color(0xFFD7F2EE)
val FondoApp = Color(0xFFF4F8F7)
val VerdeOk = Color(0xFF1E8E3E)
val RojoAlerta = Color(0xFFC62828)
val AmbarAviso = Color(0xFFB26A00)

private val EsquemaPetHome = lightColorScheme(
    primary = TealOscuro,
    onPrimary = Color.White,
    primaryContainer = TealClaro,
    onPrimaryContainer = Color(0xFF00382F),
    secondary = Teal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3F4F1),
    onSecondaryContainer = Color(0xFF0B3D36),
    background = FondoApp,
    surface = Color.White,
    surfaceVariant = Color(0xFFE6EFED),
    error = RojoAlerta
)

/**
 * Tema único de la app. Se desactiva el color dinámico de Android 12+ para
 * que la identidad teal de Pet Home se vea igual en todos los teléfonos.
 */
@Composable
fun PetHomeTheme(contenido: @Composable () -> Unit) {
    MaterialTheme(colorScheme = EsquemaPetHome, content = contenido)
}
