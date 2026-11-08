package com.premiercoders.pethome.vista.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.premiercoders.pethome.util.Validador
import com.premiercoders.pethome.vista.componentes.CampoTexto
import com.premiercoders.pethome.vista.componentes.PantallaBase
import com.premiercoders.pethome.vista.tema.PetHomeTheme

/**
 * Registro de personal nuevo con Firebase Authentication. Todos los campos se
 * validan juntos al pulsar "Crear cuenta"; el correo duplicado lo detecta
 * Firebase y llega en [errorGeneral].
 *
 * Por seguridad ya no se elige el rol: toda cuenta nueva es Recepción y el
 * administrador asigna el rol de Veterinario desde la consola de Firebase.
 */
@Composable
fun PantallaRegistro(
    cargando: Boolean,
    errorGeneral: String?,
    onRegistrar: (String, String, String) -> Unit,
    onVolver: () -> Unit,
    onLimpiarError: () -> Unit
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var contrasena by rememberSaveable { mutableStateOf("") }
    var confirmacion by rememberSaveable { mutableStateOf("") }

    var errorNombre by rememberSaveable { mutableStateOf<String?>(null) }
    var errorCorreo by rememberSaveable { mutableStateOf<String?>(null) }
    var errorContrasena by rememberSaveable { mutableStateOf<String?>(null) }
    var errorConfirmacion by rememberSaveable { mutableStateOf<String?>(null) }

    fun crearCuenta() {
        errorNombre = Validador.textoObligatorio(nombre, "El nombre", minimo = 3)
        errorCorreo = Validador.correo(correo)
        errorContrasena = Validador.contrasena(contrasena)
        errorConfirmacion = Validador.confirmacion(contrasena, confirmacion)
        onLimpiarError()
        if (listOf(errorNombre, errorCorreo, errorContrasena, errorConfirmacion).any { it != null }) return
        onRegistrar(nombre, correo, contrasena)
    }

    PantallaBase(titulo = "Crear cuenta", onVolver = onVolver) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                "Registra al personal de Pet Home que usará la app.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            CampoTexto(nombre, { nombre = it; errorNombre = null }, "Nombre completo", error = errorNombre, icono = Icons.Filled.Badge)
            CampoTexto(correo, { correo = it; errorCorreo = null; onLimpiarError() }, "Correo", error = errorCorreo, icono = Icons.Filled.Email, teclado = KeyboardType.Email)
            CampoTexto(
                contrasena, { contrasena = it; errorContrasena = null }, "Contraseña",
                error = errorContrasena, ayuda = "Mínimo 6 caracteres, con letras y números",
                icono = Icons.Filled.Lock, teclado = KeyboardType.Password, esContrasena = true
            )
            CampoTexto(
                confirmacion, { confirmacion = it; errorConfirmacion = null }, "Confirmar contraseña",
                error = errorConfirmacion, icono = Icons.Filled.Lock, teclado = KeyboardType.Password, esContrasena = true
            )

            Text(
                "La cuenta se crea con rol de Recepción (Cierre de Caja y Créditos Fiscales). " +
                    "El administrador de la clínica asigna el rol de Veterinario.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            errorGeneral?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Button(
                onClick = { crearCuenta() },
                enabled = !cargando,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(48.dp)
            ) {
                if (cargando) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Text("Crear cuenta")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VistaPreviaRegistro() {
    PetHomeTheme {
        PantallaRegistro(
            cargando = false,
            errorGeneral = null,
            onRegistrar = { _, _, _ -> },
            onVolver = {},
            onLimpiarError = {}
        )
    }
}
