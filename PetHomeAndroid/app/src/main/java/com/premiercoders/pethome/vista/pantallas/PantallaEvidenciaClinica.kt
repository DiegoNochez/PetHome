package com.premiercoders.pethome.vista.pantallas

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.premiercoders.pethome.controlador.PetHomeViewModel
import com.premiercoders.pethome.modelo.EvidenciaClinica
import com.premiercoders.pethome.util.Formato
import com.premiercoders.pethome.util.Validador
import com.premiercoders.pethome.vista.componentes.CampoTexto
import com.premiercoders.pethome.vista.componentes.DialogoConfirmacion
import com.premiercoders.pethome.vista.componentes.EstadoVacio
import com.premiercoders.pethome.vista.componentes.PantallaBase
import com.premiercoders.pethome.vista.componentes.TituloSeccion
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Evidencia clínica: reemplaza el envío de fotos por WhatsApp. Cada registro
 * del paciente recibe sus fotos con el selector de fotos del sistema
 * (Photo Picker), que no necesita pedir permisos de almacenamiento.
 */
@Composable
fun PantallaEvidenciaClinica(vm: PetHomeViewModel, onVolver: () -> Unit) {
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var paciente by rememberSaveable { mutableStateOf("") }
    var fecha by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var diagnostico by rememberSaveable { mutableStateOf("") }
    var errores by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    var idParaFoto by rememberSaveable { mutableStateOf<Int?>(null) }
    var fotoAmpliada by remember { mutableStateOf<String?>(null) }
    var evidenciaAEliminar by remember { mutableStateOf<EvidenciaClinica?>(null) }

    val selectorFotos = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        val id = idParaFoto
        if (uri != null && id != null) {
            vm.adjuntarFoto(id, uri.toString())
            scope.launch { snackbar.showSnackbar("Foto adjuntada a la evidencia #$id") }
        }
        idParaFoto = null
    }

    fun registrar() {
        val nuevos = mutableMapOf<String, String>()
        Validador.textoObligatorio(paciente, "El paciente")?.let { nuevos["paciente"] = it }
        Validador.fecha(fecha)?.let { nuevos["fecha"] = it }
        Validador.textoObligatorio(diagnostico, "El diagnóstico", minimo = 5)?.let { nuevos["diagnostico"] = it }
        errores = nuevos
        if (nuevos.isNotEmpty()) return
        val evidencia = vm.crearEvidencia(paciente, Validador.aFecha(fecha)!!, diagnostico)
        paciente = ""; diagnostico = ""
        scope.launch { snackbar.showSnackbar("Evidencia #${evidencia.id} creada. Ya puedes adjuntarle fotos.") }
    }

    PantallaBase(titulo = "Evidencia Clínica", onVolver = onVolver, snackbarHostState = snackbar) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).imePadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        TituloSeccion("Nuevo registro")
                        CampoTexto(paciente, { paciente = it; errores = errores - "paciente" }, "Paciente (nombre y especie)", error = errores["paciente"], icono = Icons.Filled.Pets)
                        CampoTexto(fecha, { fecha = it; errores = errores - "fecha" }, "Fecha (AAAA-MM-DD)", error = errores["fecha"], icono = Icons.Filled.CalendarMonth)
                        CampoTexto(diagnostico, { diagnostico = it; errores = errores - "diagnostico" }, "Diagnóstico / observaciones", error = errores["diagnostico"], unaLinea = false)
                        Button(onClick = { registrar() }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(48.dp)) {
                            Text("Crear registro")
                        }
                    }
                }
            }
            item { TituloSeccion("Registros (${vm.evidencias.size})") }
            if (vm.evidencias.isEmpty()) {
                item { EstadoVacio("Aún no hay evidencias clínicas") }
            }
            items(vm.evidencias, key = { it.id }) { evidencia ->
                TarjetaEvidencia(
                    evidencia = evidencia,
                    onAdjuntar = {
                        idParaFoto = evidencia.id
                        selectorFotos.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    onVerFoto = { fotoAmpliada = it },
                    onEliminar = { evidenciaAEliminar = evidencia }
                )
            }
        }
    }

    fotoAmpliada?.let { uri ->
        Dialog(onDismissRequest = { fotoAmpliada = null }) {
            AsyncImage(
                model = uri,
                contentDescription = "Foto clínica ampliada",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            )
        }
    }
    evidenciaAEliminar?.let { evidencia ->
        DialogoConfirmacion(
            titulo = "Eliminar evidencia #${evidencia.id}",
            mensaje = "Se eliminará el registro de ${evidencia.paciente} y sus ${evidencia.fotosAdjuntas.size} foto(s).",
            textoConfirmar = "Eliminar",
            onConfirmar = { vm.eliminarEvidencia(evidencia.id); evidenciaAEliminar = null },
            onCancelar = { evidenciaAEliminar = null }
        )
    }
}

@Composable
private fun TarjetaEvidencia(
    evidencia: EvidenciaClinica,
    onAdjuntar: () -> Unit,
    onVerFoto: (String) -> Unit,
    onEliminar: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(start = 16.dp, top = 8.dp, end = 4.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(evidencia.paciente, fontWeight = FontWeight.SemiBold)
                    Text(
                        "#${evidencia.id} · ${Formato.fecha(evidencia.fecha)} · ${evidencia.fotosAdjuntas.size} foto(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onEliminar) { Icon(Icons.Filled.Delete, contentDescription = "Eliminar") }
            }
            Text(evidencia.diagnostico, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(end = 12.dp))
            if (evidencia.fotosAdjuntas.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(evidencia.fotosAdjuntas) { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = "Foto clínica de ${evidencia.paciente}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(84.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onVerFoto(uri) }
                        )
                    }
                }
            }
            OutlinedButton(onClick = onAdjuntar) {
                Icon(Icons.Filled.AddAPhoto, contentDescription = null)
                Text("  Adjuntar foto")
            }
        }
    }
}
