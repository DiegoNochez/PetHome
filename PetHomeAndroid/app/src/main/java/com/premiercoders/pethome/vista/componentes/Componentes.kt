package com.premiercoders.pethome.vista.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Estructura común de las pantallas internas: barra superior teal con botón
 * de volver, acciones opcionales y espacio para avisos (Snackbar).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaBase(
    titulo: String,
    onVolver: (() -> Unit)? = null,
    snackbarHostState: SnackbarHostState? = null,
    acciones: @Composable RowScope.() -> Unit = {},
    contenido: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    if (onVolver != null) {
                        IconButton(onClick = onVolver) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                },
                actions = acciones,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = {
            if (snackbarHostState != null) SnackbarHost(snackbarHostState)
        },
        containerColor = MaterialTheme.colorScheme.background,
        content = contenido
    )
}

/**
 * Campo de texto con validación: si [error] no es nulo, el campo se pinta en
 * rojo y muestra el mensaje debajo. Con [esContrasena] agrega el botón de ojo.
 */
@Composable
fun CampoTexto(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    error: String? = null,
    ayuda: String? = null,
    icono: ImageVector? = null,
    teclado: KeyboardType = KeyboardType.Text,
    esContrasena: Boolean = false,
    unaLinea: Boolean = true
) {
    var visible by remember { mutableStateOf(false) }
    val mensaje = error ?: ayuda
    val botonOjo: (@Composable () -> Unit)? = if (esContrasena) {
        {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (visible) "Ocultar contraseña" else "Mostrar contraseña"
                )
            }
        }
    } else {
        null
    }
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        singleLine = unaLinea,
        minLines = if (unaLinea) 1 else 3,
        isError = error != null,
        supportingText = mensaje?.let { texto -> { Text(texto) } },
        leadingIcon = icono?.let { imagen -> { Icon(imagen, contentDescription = null) } },
        trailingIcon = botonOjo,
        visualTransformation = if (esContrasena && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        modifier = modifier
    )
}

/** Tarjeta pequeña con un dato destacado (usada en el panel y en reportes). */
@Composable
fun TarjetaDato(
    titulo: String,
    valor: String,
    icono: ImageVector,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icono, contentDescription = null, tint = color)
            Text(valor, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Text(titulo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Título de sección dentro de una pantalla. */
@Composable
fun TituloSeccion(texto: String, modifier: Modifier = Modifier, extra: @Composable RowScope.() -> Unit = {}) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            texto,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        extra()
    }
}

/** Fila "etiqueta ........ valor" para vistas previas de cálculos. */
@Composable
fun FilaValor(etiqueta: String, valor: String, color: Color = MaterialTheme.colorScheme.onSurface, resaltar: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(etiqueta, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(
            valor,
            color = color,
            fontWeight = if (resaltar) FontWeight.Bold else FontWeight.Normal,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/** Mensaje para listas vacías. */
@Composable
fun EstadoVacio(texto: String) {
    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.Inbox,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp)
            )
            Text(
                texto,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/** Diálogo de confirmación para acciones que no se pueden deshacer. */
@Composable
fun DialogoConfirmacion(
    titulo: String,
    mensaje: String,
    textoConfirmar: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = { Text(mensaje) },
        confirmButton = {
            TextButton(onClick = onConfirmar) { Text(textoConfirmar, color = MaterialTheme.colorScheme.error) }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}
