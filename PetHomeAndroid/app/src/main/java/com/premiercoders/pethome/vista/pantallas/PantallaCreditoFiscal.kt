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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.premiercoders.pethome.controlador.PetHomeViewModel
import com.premiercoders.pethome.modelo.CreditoFiscal
import com.premiercoders.pethome.modelo.Rol
import com.premiercoders.pethome.util.Formato
import com.premiercoders.pethome.util.Validador
import com.premiercoders.pethome.vista.componentes.AvisoSincronizacion
import com.premiercoders.pethome.vista.componentes.CampoTexto
import com.premiercoders.pethome.vista.componentes.FiltroPeriodo
import com.premiercoders.pethome.vista.componentes.PeriodoHistorial
import com.premiercoders.pethome.vista.componentes.DialogoConfirmacion
import com.premiercoders.pethome.vista.componentes.EstadoVacio
import com.premiercoders.pethome.vista.componentes.FilaValor
import com.premiercoders.pethome.vista.componentes.PantallaBase
import com.premiercoders.pethome.vista.componentes.TituloSeccion
import com.premiercoders.pethome.vista.tema.RojoAlerta
import kotlinx.coroutines.launch
import java.time.LocalDate

private enum class FiltroCredito(val etiqueta: String) { TODOS("Todos"), VIGENTES("Vigentes"), ANULADOS("Anulados") }

/**
 * Créditos fiscales: muestra el correlativo que se va a emitir y calcula el
 * IVA y el total mientras se escribe el subtotal. Los créditos no se borran,
 * se anulan (conservan su número, como exige la práctica fiscal).
 *
 * Foro 2: se piden NIT y NRC del cliente, los créditos se guardan en
 * Firestore y el historial se filtra por estado y por periodo. Solo el
 * administrador puede anular.
 */
@Composable
fun PantallaCreditoFiscal(vm: PetHomeViewModel, onVolver: () -> Unit) {
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var fecha by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var cliente by rememberSaveable { mutableStateOf("") }
    var nit by rememberSaveable { mutableStateOf("") }
    var nrc by rememberSaveable { mutableStateOf("") }
    var servicio by rememberSaveable { mutableStateOf("") }
    var subtotal by rememberSaveable { mutableStateOf("") }
    var errores by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var filtro by rememberSaveable { mutableStateOf(FiltroCredito.TODOS) }
    var creditoAAnular by remember { mutableStateOf<CreditoFiscal?>(null) }
    var periodo by rememberSaveable { mutableStateOf(PeriodoHistorial.TODOS) }
    val esAdmin = vm.usuarioActual?.rol == Rol.ADMINISTRADOR
    val avisar: (String) -> Unit = { texto -> scope.launch { snackbar.showSnackbar(texto) } }

    val subtotalNumero = Validador.aNumero(subtotal) ?: 0.0
    val iva = CreditoFiscal.calcularIva(subtotalNumero)

    fun emitir() {
        val nuevos = mutableMapOf<String, String>()
        Validador.fecha(fecha)?.let { nuevos["fecha"] = it }
        Validador.textoObligatorio(cliente, "El cliente", minimo = 3)?.let { nuevos["cliente"] = it }
        Validador.nit(nit)?.let { nuevos["nit"] = it }
        Validador.nrc(nrc)?.let { nuevos["nrc"] = it }
        Validador.textoObligatorio(servicio, "El servicio", minimo = 3)?.let { nuevos["servicio"] = it }
        Validador.monto(subtotal, "El subtotal", permitirCero = false)?.let { nuevos["subtotal"] = it }
        errores = nuevos
        if (nuevos.isNotEmpty()) return

        val total = Formato.dinero(subtotalNumero + iva)
        vm.generarCredito(
            Validador.aFecha(fecha)!!, cliente, nit, nrc, servicio, subtotalNumero,
            onExito = { cliente = ""; nit = ""; nrc = ""; servicio = ""; subtotal = "" }
        ) { mensaje -> avisar("$mensaje por $total") }
    }

    val lista = vm.creditos
        .filter { periodo.contiene(it.fecha) }
        .filter {
            when (filtro) {
                FiltroCredito.TODOS -> true
                FiltroCredito.VIGENTES -> !it.anulado
                FiltroCredito.ANULADOS -> it.anulado
            }
        }

    PantallaBase(titulo = "Créditos Fiscales", onVolver = onVolver, snackbarHostState = snackbar) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).imePadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { AvisoSincronizacion(vm.errorSincronizacion) }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        TituloSeccion("Nuevo crédito fiscal") {
                            AssistChip(
                                onClick = {},
                                label = { Text(vm.siguienteNumeroCredito) },
                                colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            )
                        }
                        CampoTexto(fecha, { fecha = it; errores = errores - "fecha" }, "Fecha (AAAA-MM-DD)", error = errores["fecha"], icono = Icons.Filled.CalendarMonth)
                        CampoTexto(cliente, { cliente = it; errores = errores - "cliente" }, "Cliente / razón social", error = errores["cliente"], icono = Icons.Filled.Business)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CampoTexto(
                                nit, { nit = it; errores = errores - "nit" }, "NIT", modifier = Modifier.weight(1.3f),
                                error = errores["nit"], icono = Icons.Filled.Badge, teclado = KeyboardType.Phone
                            )
                            CampoTexto(
                                nrc, { nrc = it; errores = errores - "nrc" }, "NRC", modifier = Modifier.weight(1f),
                                error = errores["nrc"], teclado = KeyboardType.Phone
                            )
                        }
                        CampoTexto(servicio, { servicio = it; errores = errores - "servicio" }, "Servicio prestado", error = errores["servicio"], icono = Icons.Filled.MedicalServices)
                        CampoTexto(subtotal, { subtotal = it; errores = errores - "subtotal" }, "Subtotal (sin IVA) $", error = errores["subtotal"], teclado = KeyboardType.Decimal)
                        HorizontalDivider(Modifier.padding(vertical = 6.dp))
                        FilaValor("Subtotal", Formato.dinero(subtotalNumero))
                        FilaValor("IVA 13 %", Formato.dinero(iva))
                        FilaValor("Total", Formato.dinero(subtotalNumero + iva), resaltar = true, color = MaterialTheme.colorScheme.primary)
                        Button(onClick = { emitir() }, enabled = !vm.guardando, modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(48.dp)) {
                            Text("Emitir crédito fiscal")
                        }
                    }
                }
            }

            item {
                Column {
                    TituloSeccion("Emitidos (${lista.size})")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FiltroCredito.entries.forEach { opcion ->
                            FilterChip(selected = filtro == opcion, onClick = { filtro = opcion }, label = { Text(opcion.etiqueta) })
                        }
                    }
                    FiltroPeriodo(periodo) { periodo = it }
                }
            }
            if (lista.isEmpty()) {
                item { EstadoVacio("No hay créditos fiscales en esta lista") }
            }
            items(lista, key = { it.numeroCorrelativo }) { credito ->
                TarjetaCredito(credito, onAnular = if (esAdmin) ({ creditoAAnular = credito }) else null)
            }
        }
    }

    creditoAAnular?.let { credito ->
        DialogoConfirmacion(
            titulo = "Anular ${credito.numeroCorrelativo}",
            mensaje = "El crédito quedará anulado y no contará en los reportes. Su número no se reutiliza.",
            textoConfirmar = "Anular",
            onConfirmar = { vm.anularCredito(credito.numeroCorrelativo, avisar); creditoAAnular = null },
            onCancelar = { creditoAAnular = null }
        )
    }
}

@Composable
private fun TarjetaCredito(credito: CreditoFiscal, onAnular: (() -> Unit)?) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    credito.numeroCorrelativo,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (credito.anulado) TextDecoration.LineThrough else null,
                    modifier = Modifier.weight(1f)
                )
                Text(Formato.fecha(credito.fecha), style = MaterialTheme.typography.bodySmall)
            }
            Text(credito.cliente, style = MaterialTheme.typography.bodyMedium)
            if (credito.nit.isNotBlank()) {
                Text(
                    "NIT ${credito.nit} · NRC ${credito.nrc}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(credito.servicio, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FilaValor("Subtotal + IVA", "${Formato.dinero(credito.subtotal)} + ${Formato.dinero(credito.iva)}")
            FilaValor("Total", Formato.dinero(credito.total), resaltar = true)
            if (credito.anulado) {
                Text("ANULADO", color = RojoAlerta, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            } else if (onAnular != null) {
                OutlinedButton(onClick = onAnular, modifier = Modifier.padding(top = 6.dp)) { Text("Anular") }
            }
        }
    }
}
