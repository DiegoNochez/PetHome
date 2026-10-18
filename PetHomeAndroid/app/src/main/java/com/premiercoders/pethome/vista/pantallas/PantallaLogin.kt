package com.premiercoders.pethome.vista.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.premiercoders.pethome.util.Validador
import com.premiercoders.pethome.vista.componentes.CampoTexto
import com.premiercoders.pethome.vista.tema.PetHomeTheme
import com.premiercoders.pethome.vista.tema.Teal
import com.premiercoders.pethome.vista.tema.TealOscuro

/**
 * Inicio de sesión. Valida el formato antes de consultar al controlador y
 * muestra el error de cada campo debajo de él.
 *
 * @param onIniciarSesion devuelve el mensaje de error, o `null` si entró.
 */
@Composable
fun PantallaLogin(
    onIniciarSesion: (String, String) -> String?,
    onExito: () -> Unit,
    onIrARegistro: () -> Unit
) {
    var correo by rememberSaveable { mutableStateOf("") }
    var contrasena by rememberSaveable { mutableStateOf("") }
    var errorCorreo by rememberSaveable { mutableStateOf<String?>(null) }
    var errorContrasena by rememberSaveable { mutableStateOf<String?>(null) }
    var errorGeneral by rememberSaveable { mutableStateOf<String?>(null) }

    fun intentarIngreso() {
        errorCorreo = Validador.correo(correo)
        errorContrasena = if (contrasena.isBlank()) "La contraseña es obligatoria" else null
        errorGeneral = null
        if (errorCorreo != null || errorContrasena != null) return
        val error = onIniciarSesion(correo, contrasena)
        if (error == null) onExito() else errorGeneral = error
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(TealOscuro, Teal)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            Box(
                modifier = Modifier.size(84.dp).background(Color.White.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Pets, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text("Pet Home", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Text("Centro Médico Veterinario · Personal", color = Color.White.copy(alpha = 0.85f))
            Spacer(Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Iniciar sesión", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    CampoTexto(
                        valor = correo,
                        onCambio = { correo = it; errorCorreo = null; errorGeneral = null },
                        etiqueta = "Correo",
                        error = errorCorreo,
                        icono = Icons.Filled.Email,
                        teclado = KeyboardType.Email
                    )
                    CampoTexto(
                        valor = contrasena,
                        onCambio = { contrasena = it; errorContrasena = null; errorGeneral = null },
                        etiqueta = "Contraseña",
                        error = errorContrasena,
                        icono = Icons.Filled.Lock,
                        teclado = KeyboardType.Password,
                        esContrasena = true
                    )
                    errorGeneral?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                    }
                    Button(onClick = { intentarIngreso() }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                        Text("Ingresar")
                    }
                    TextButton(onClick = onIrARegistro, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text("¿Personal nuevo? Crear cuenta")
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f))
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text("Cuentas de prueba", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text("Administrador: admin@pethome.com / admin123", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    Text("Recepción: recepcion@pethome.com / recep123", color = Color.White, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VistaPreviaLogin() {
    PetHomeTheme {
        PantallaLogin(onIniciarSesion = { _, _ -> null }, onExito = {}, onIrARegistro = {})
    }
}
