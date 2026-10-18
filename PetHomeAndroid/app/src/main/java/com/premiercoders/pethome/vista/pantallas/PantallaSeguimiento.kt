package com.premiercoders.pethome.vista.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import com.premiercoders.pethome.modelo.EstadoSeguimiento
import com.premiercoders.pethome.modelo.Seguimiento
import com.premiercoders.pethome.util.Formato
import com.premiercoders.pethome.util.Validador
import com.premiercoders.pethome.vista.componentes.CampoTexto
import com.premiercoders.pethome.vista.componentes.DialogoConfirmacion
import com.premiercoders.pethome.vista.componentes.EstadoVacio
import com.premiercoders.pethome.vista.componentes.FilaValor
import com.premiercoders.pethome.vista.componentes.PantallaBase
import com.premiercoders.pethome.vista.componentes.TituloSeccion
import com.premiercoders.pethome.vista.tema.AmbarAviso
import com.premiercoders.pethome.vista.tema.RojoAlerta
import com.premiercoders.pethome.vista.tema.VerdeOk
import kotlinx.coroutines.launch
import java.time.LocalDate

private enum class FiltroSeguimiento(val etiqueta: String) {
    PENDIENTES("Pendientes"), PROXIMOS("Próximos 7 días"), VENCIDOS("Vencidos"), TODOS("Todos")
}

/**
 * Seguimiento post-tratamiento: calcula la fecha del control mientras se
 * escribe y resalta en rojo a los pacientes que no regresaron (el caso real
 * de la entrevista con Pet Home).
 */
@Composable
fun PantallaSeguimiento(vm: PetHomeViewModel, onVolver: () -> Unit) {
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var paciente by rememberSaveable { mutableStateOf("") }
    var procedimiento by rememberSaveable { mutableStateOf("") }
    var fecha by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var dias by rememberSaveable { mutableStateOf("7") }
    var errores by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var filtro by rememberSaveable { mutableStateOf(FiltroSeguimiento.PENDIENTES) }
    var seguimientoAEliminar by remember { mutableStateOf<Seguimiento?>(null) }

    val fechaPrevia = Validador.aFecha(fecha)
    val diasPrevios = dias.trim().toLongOrNull()
    val controlPrevio = if (fechaPrevia != null && diasPrevios != null && diasPrevios in 1..365) fechaPrevia.plusDays(diasPrevios) else null

    fun registrar() {
        val nuevos = mutableMapOf<String, String>()
        Validador.textoObligatorio(paciente, "El paciente")?.let { nuevos["paciente"] = it }
        Validador.textoObligatorio(procedimiento, "El procedimiento", minimo = 3)?.let { nuevos["procedimiento"] = it }
        Validador.fecha(fecha)?.let { nuevos["fecha"] = it }
        Validador.entero(dias, "Los días", 1, 365)?.let { nuevos["dias"] = it }
        errores = nuevos
        if (nuevos.isNotEmpty()) return
        val nuevo = vm.crearSeguimiento(paciente, procedimiento, fechaPrevia!!, diasPrevios!!)
        paciente = ""; procedimiento = ""
        scope.launch { snackbar.showSnackbar("Control de ${nuevo.paciente} programado para el ${Formato.fecha(nuevo.fechaControl)}") }
    }

    val lista = when (filtro) {
        FiltroSeguimiento.PENDIENTES -> vm.seguimientos.filter { it.estado == EstadoSeguimiento.PENDIENTE }
        FiltroSeguimiento.PROXIMOS -> vm.proximosAVencer(7)
        FiltroSeguimiento.VENCIDOS -> vm.seguimientos.filter { it.estaVencido() }
        FiltroSeguimiento.TODOS -> vm.seguimientos
    }

    PantallaBase(titulo = "Seguimiento", onVolver = onVolver, snackbarHostState = snackbar) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).imePadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        TituloSeccion("Programar control")
                        CampoTexto(paciente, { paciente = it; errores = errores - "paciente" }, "Paciente (nombre y especie)", error = errores["paciente"], icono = Icons.Filled.Pets)
                        CampoTexto(procedimiento, { procedimiento = it; errores = errores - "procedimiento" }, "Procedimiento realizado", error = errores["procedimiento"], icono = Icons.Filled.MedicalServices)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CampoTexto(
                                fecha, { fecha = it; errores = errores - "fecha" }, "Fecha procedimiento",
                                modifier = Modifier.weight(1.4f), error = errores["fecha"], icono = Icons.Filled.CalendarMonth
                            )
                            CampoTexto(
                                dias, { dias = it; errores = errores - "dias" }, "Días",
                                modifier = Modifier.weight(0.8f), error = errores["dias"], teclado = KeyboardType.Number
                            )
                        }
                        HorizontalDivider(Modifier.padding(vertical = 6.dp))
                        FilaValor(
                            "Fecha de control",
                            controlPrevio?.let { Formato.fecha(it) } ?: "—",
                            resaltar = true,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Button(onClick = { registrar() }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(48.dp)) {
                            Text("Programar seguimiento")
                        }
                    }
                }
            }
            item {
                Column {
                    TituloSeccion("Seguimientos (${lista.size})")
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FiltroSeguimiento.entries.forEach { opcion ->
                            FilterChip(selected = filtro == opcion, onClick = { filtro = opcion }, label = { Text(opcion.etiqueta) })
                        }
                    }
                }
            }
            if (lista.isEmpty()) {
                item { EstadoVacio("No hay seguimientos en esta lista") }
            }
            items(lista, key = { it.id }) { seguimiento ->
                TarjetaSeguimiento(
                    seguimiento,
                    onCompletar = {
                        vm.completarSeguimiento(seguimiento.id)
                        scope.launch { snackbar.showSnackbar("${seguimiento.paciente}: control completado") }
                    },
                    onEliminar = { seguimientoAEliminar = seguimiento }
                )
            }
        }
    }

    seguimientoAEliminar?.let { s ->
        DialogoConfirmacion(
            titulo = "Eliminar seguimiento",
            mensaje = "Se eliminará el seguimiento de ${s.paciente} (${s.procedimiento}).",
            textoConfirmar = "Eliminar",
            onConfirmar = { vm.eliminarSeguimiento(s.id); seguimientoAEliminar = null },
            onCancelar = { seguimientoAEliminar = null }
        )
    }
}

@Composable
private fun TarjetaSeguimiento(seguimiento: Seguimiento, onCompletar: () -> Unit, onEliminar: () -> Unit) {
    val (estadoTexto, estadoColor) = when {
        seguimiento.estado == EstadoSeguimiento.COMPLETADO -> "Completado" to VerdeOk
        seguimiento.estaVencido() -> "Vencido: no ha regresado" to RojoAlerta
        else -> "Pendiente" to AmbarAviso
    }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(start = 16.dp, top = 8.dp, end = 4.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(seguimiento.paciente, fontWeight = FontWeight.SemiBold)
                    Text(seguimiento.procedimiento, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onEliminar) { Icon(Icons.Filled.Delete, contentDescription = "Eliminar") }
            }
            Column(Modifier.padding(end = 12.dp)) {
                FilaValor("Procedimiento", Formato.fecha(seguimiento.fechaProcedimiento))
                FilaValor("Control (+${seguimiento.diasParaControl} días)", Formato.fecha(seguimiento.fechaControl), resaltar = true)
                FilaValor("Estado", estadoTexto, color = estadoColor, resaltar = true)
                if (seguimiento.estado == EstadoSeguimiento.PENDIENTE) {
                    OutlinedButton(onClick = onCompletar, modifier = Modifier.padding(top = 6.dp)) {
                        Text("Marcar como completado", color = Color.Unspecified)
                    }
                }
            }
        }
    }
}
