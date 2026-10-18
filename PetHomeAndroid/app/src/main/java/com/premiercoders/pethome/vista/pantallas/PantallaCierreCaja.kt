package com.premiercoders.pethome.vista.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.premiercoders.pethome.controlador.PetHomeViewModel
import com.premiercoders.pethome.modelo.CierreCaja
import com.premiercoders.pethome.util.Formato
import com.premiercoders.pethome.util.Validador
import com.premiercoders.pethome.vista.componentes.CampoTexto
import com.premiercoders.pethome.vista.componentes.DialogoConfirmacion
import com.premiercoders.pethome.vista.componentes.EstadoVacio
import com.premiercoders.pethome.vista.componentes.FilaValor
import com.premiercoders.pethome.vista.componentes.PantallaBase
import com.premiercoders.pethome.vista.componentes.TituloSeccion
import com.premiercoders.pethome.vista.tema.RojoAlerta
import com.premiercoders.pethome.vista.tema.VerdeOk
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Color según el resultado del cuadre: verde cuadrado, rojo faltante, ámbar sobrante. */
fun colorDiferencia(cierre: CierreCaja): Color = when {
    cierre.tieneFaltante -> RojoAlerta
    cierre.tieneSobrante -> Color(0xFFB26A00)
    else -> VerdeOk
}

/**
 * Cierre de caja: formulario con cálculo en vivo del total esperado y la
 * diferencia, y abajo el historial con filtro de faltantes, edición del
 * conteo físico y eliminación.
 */
@Composable
fun PantallaCierreCaja(vm: PetHomeViewModel, onVolver: () -> Unit) {
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var fecha by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var efectivo by rememberSaveable { mutableStateOf("") }
    var tarjeta by rememberSaveable { mutableStateOf("") }
    var transferencia by rememberSaveable { mutableStateOf("") }
    var contado by rememberSaveable { mutableStateOf("") }
    var errores by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    var soloFaltantes by rememberSaveable { mutableStateOf(false) }
    var cierreAEditar by remember { mutableStateOf<CierreCaja?>(null) }
    var cierreAEliminar by remember { mutableStateOf<CierreCaja?>(null) }

    // Vista previa: reutiliza la lógica del modelo con los valores que ya se pueden leer.
    val vistaPrevia = CierreCaja(
        id = 0,
        fecha = LocalDate.now(),
        efectivo = Validador.aNumero(efectivo) ?: 0.0,
        tarjeta = Validador.aNumero(tarjeta) ?: 0.0,
        transferencia = Validador.aNumero(transferencia) ?: 0.0,
        montoContado = Validador.aNumero(contado) ?: 0.0,
        responsable = ""
    )

    fun registrar() {
        val nuevos = mutableMapOf<String, String>()
        Validador.fecha(fecha)?.let { nuevos["fecha"] = it }
        Validador.monto(efectivo, "El efectivo")?.let { nuevos["efectivo"] = it }
        Validador.monto(tarjeta, "La tarjeta")?.let { nuevos["tarjeta"] = it }
        Validador.monto(transferencia, "La transferencia")?.let { nuevos["transferencia"] = it }
        Validador.monto(contado, "El monto contado")?.let { nuevos["contado"] = it }
        errores = nuevos
        if (nuevos.isNotEmpty()) return

        val cierre = vm.registrarCierre(
            Validador.aFecha(fecha)!!, vistaPrevia.efectivo, vistaPrevia.tarjeta,
            vistaPrevia.transferencia, vistaPrevia.montoContado
        )
        efectivo = ""; tarjeta = ""; transferencia = ""; contado = ""
        scope.launch { snackbar.showSnackbar("Cierre #${cierre.id} registrado: ${cierre.estadoDiferencia()}") }
    }

    val lista = if (soloFaltantes) vm.cierres.filter { it.tieneFaltante } else vm.cierres

    PantallaBase(titulo = "Cierre de Caja", onVolver = onVolver, snackbarHostState = snackbar) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).imePadding(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        TituloSeccion("Nuevo cierre")
                        CampoTexto(
                            fecha, { fecha = it; errores = errores - "fecha" }, "Fecha (AAAA-MM-DD)",
                            error = errores["fecha"], icono = Icons.Filled.CalendarMonth
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CampoTexto(
                                efectivo, { efectivo = it; errores = errores - "efectivo" }, "Efectivo $",
                                modifier = Modifier.weight(1f), error = errores["efectivo"], teclado = KeyboardType.Decimal
                            )
                            CampoTexto(
                                tarjeta, { tarjeta = it; errores = errores - "tarjeta" }, "Tarjeta $",
                                modifier = Modifier.weight(1f), error = errores["tarjeta"], teclado = KeyboardType.Decimal
                            )
                        }
                        CampoTexto(
                            transferencia, { transferencia = it; errores = errores - "transferencia" }, "Transferencia $",
                            error = errores["transferencia"], teclado = KeyboardType.Decimal
                        )
                        CampoTexto(
                            contado, { contado = it; errores = errores - "contado" }, "Monto contado físicamente $",
                            error = errores["contado"], teclado = KeyboardType.Decimal
                        )
                        HorizontalDivider(Modifier.padding(vertical = 6.dp))
                        FilaValor("Total esperado", Formato.dinero(vistaPrevia.totalEsperado), resaltar = true)
                        if (contado.isNotBlank()) {
                            FilaValor("Resultado", vistaPrevia.estadoDiferencia(), color = colorDiferencia(vistaPrevia), resaltar = true)
                        }
                        Button(onClick = { registrar() }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(48.dp)) {
                            Text("Registrar cierre")
                        }
                    }
                }
            }

            item {
                TituloSeccion("Historial (${lista.size})") {
                    FilterChip(selected = soloFaltantes, onClick = { soloFaltantes = !soloFaltantes }, label = { Text("Solo faltantes") })
                }
            }
            if (lista.isEmpty()) {
                item { EstadoVacio(if (soloFaltantes) "No hay cierres con faltante" else "Aún no hay cierres registrados") }
            }
            items(lista, key = { it.id }) { cierre ->
                TarjetaCierre(cierre, onEditar = { cierreAEditar = cierre }, onEliminar = { cierreAEliminar = cierre })
            }
        }
    }

    cierreAEditar?.let { cierre ->
        DialogoEditarConteo(
            cierre = cierre,
            onGuardar = { monto ->
                vm.actualizarConteo(cierre.id, monto)
                cierreAEditar = null
                scope.launch { snackbar.showSnackbar("Conteo del cierre #${cierre.id} actualizado") }
            },
            onCancelar = { cierreAEditar = null }
        )
    }
    cierreAEliminar?.let { cierre ->
        DialogoConfirmacion(
            titulo = "Eliminar cierre #${cierre.id}",
            mensaje = "Se eliminará el cierre del ${Formato.fecha(cierre.fecha)}. Esta acción no se puede deshacer.",
            textoConfirmar = "Eliminar",
            onConfirmar = { vm.eliminarCierre(cierre.id); cierreAEliminar = null },
            onCancelar = { cierreAEliminar = null }
        )
    }
}

@Composable
private fun TarjetaCierre(cierre: CierreCaja, onEditar: () -> Unit, onEliminar: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(start = 16.dp, top = 8.dp, bottom = 12.dp, end = 4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Cierre #${cierre.id} · ${Formato.fecha(cierre.fecha)}", fontWeight = FontWeight.SemiBold)
                    Text(cierre.responsable, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onEditar) { Icon(Icons.Filled.Edit, contentDescription = "Editar conteo") }
                IconButton(onClick = onEliminar) { Icon(Icons.Filled.Delete, contentDescription = "Eliminar") }
            }
            Column(Modifier.padding(end = 12.dp)) {
                FilaValor("Efectivo / Tarjeta / Transf.", "${Formato.dinero(cierre.efectivo)} / ${Formato.dinero(cierre.tarjeta)} / ${Formato.dinero(cierre.transferencia)}")
                FilaValor("Esperado", Formato.dinero(cierre.totalEsperado))
                FilaValor("Contado", Formato.dinero(cierre.montoContado))
                FilaValor("Resultado", cierre.estadoDiferencia(), color = colorDiferencia(cierre), resaltar = true)
            }
        }
    }
}

@Composable
private fun DialogoEditarConteo(cierre: CierreCaja, onGuardar: (Double) -> Unit, onCancelar: () -> Unit) {
    var valor by remember { mutableStateOf(String.format(java.util.Locale.US, "%.2f", cierre.montoContado)) }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Actualizar conteo físico") },
        text = {
            Column {
                Text("Total esperado: ${Formato.dinero(cierre.totalEsperado)}", style = MaterialTheme.typography.bodyMedium)
                CampoTexto(valor, { valor = it; error = null }, "Nuevo monto contado $", error = error, teclado = KeyboardType.Decimal)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                error = Validador.monto(valor, "El monto")
                if (error == null) onGuardar(Validador.aNumero(valor)!!)
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}
