package com.premiercoders.pethome.vista.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import com.premiercoders.pethome.modelo.Rol
import com.premiercoders.pethome.util.Validador
import com.premiercoders.pethome.vista.componentes.CampoTexto
import com.premiercoders.pethome.vista.componentes.PantallaBase
import com.premiercoders.pethome.vista.tema.PetHomeTheme

/**
 * Registro de personal nuevo. Todos los campos se validan juntos al pulsar
 * "Crear cuenta"; el correo duplicado lo detecta el controlador.
 */
@Composable
fun PantallaRegistro(
    onRegistrar: (String, String, String, Rol) -> String?,
    onExito: () -> Unit,
    onVolver: () -> Unit
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var contrasena by rememberSaveable { mutableStateOf("") }
    var confirmacion by rememberSaveable { mutableStateOf("") }
    var rol by rememberSaveable { mutableStateOf(Rol.RECEPCIONISTA) }

    var errorNombre by rememberSaveable { mutableStateOf<String?>(null) }
    var errorCorreo by rememberSaveable { mutableStateOf<String?>(null) }
    var errorContrasena by rememberSaveable { mutableStateOf<String?>(null) }
    var errorConfirmacion by rememberSaveable { mutableStateOf<String?>(null) }

    fun crearCuenta() {
        errorNombre = Validador.textoObligatorio(nombre, "El nombre", minimo = 3)
        errorCorreo = Validador.correo(correo)
        errorContrasena = Validador.contrasena(contrasena)
        errorConfirmacion = Validador.confirmacion(contrasena, confirmacion)
        if (listOf(errorNombre, errorCorreo, errorContrasena, errorConfirmacion).any { it != null }) return
        val error = onRegistrar(nombre, correo, contrasena, rol)
        if (error == null) onExito() else errorCorreo = error
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
            CampoTexto(correo, { correo = it; errorCorreo = null }, "Correo", error = errorCorreo, icono = Icons.Filled.Email, teclado = KeyboardType.Email)
            CampoTexto(
                contrasena, { contrasena = it; errorContrasena = null }, "Contraseña",
                error = errorContrasena, ayuda = "Mínimo 6 caracteres, con letras y números",
                icono = Icons.Filled.Lock, teclado = KeyboardType.Password, esContrasena = true
            )
            CampoTexto(
                confirmacion, { confirmacion = it; errorConfirmacion = null }, "Confirmar contraseña",
                error = errorConfirmacion, icono = Icons.Filled.Lock, teclado = KeyboardType.Password, esContrasena = true
            )

            Text("Rol", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Rol.entries.forEach { opcion ->
                    FilterChip(
                        selected = rol == opcion,
                        onClick = { rol = opcion },
                        label = { Text(if (opcion == Rol.ADMINISTRADOR) "Administrador / MVZ" else "Recepción / Caja") },
                        leadingIcon = if (rol == opcion) {
                            { Icon(Icons.Filled.Check, contentDescription = null) }
                        } else {
                            null
                        }
                    )
                }
            }
            Text(
                if (rol == Rol.ADMINISTRADOR) "Acceso a los 5 módulos, incluidos los clínicos y los reportes."
                else "Acceso a Cierre de Caja y Créditos Fiscales.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(onClick = { crearCuenta() }, modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(48.dp)) {
                Text("Crear cuenta")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VistaPreviaRegistro() {
    PetHomeTheme {
        PantallaRegistro(onRegistrar = { _, _, _, _ -> null }, onExito = {}, onVolver = {})
    }
}
