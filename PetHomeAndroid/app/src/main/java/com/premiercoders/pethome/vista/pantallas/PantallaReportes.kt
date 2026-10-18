package com.premiercoders.pethome.vista.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.premiercoders.pethome.modelo.ResumenFinanciero
import com.premiercoders.pethome.util.Formato
import com.premiercoders.pethome.vista.componentes.FilaValor
import com.premiercoders.pethome.vista.componentes.PantallaBase
import com.premiercoders.pethome.vista.componentes.TarjetaDato
import com.premiercoders.pethome.vista.componentes.TituloSeccion
import com.premiercoders.pethome.vista.tema.AmbarAviso
import com.premiercoders.pethome.vista.tema.RojoAlerta
import com.premiercoders.pethome.vista.tema.Teal
import com.premiercoders.pethome.vista.tema.TealOscuro

/** Reportes financieros: solo lectura, calculados por GestorReportes. */
@Composable
fun PantallaReportes(resumen: ResumenFinanciero, onVolver: () -> Unit) {
    PantallaBase(titulo = "Reportes", onVolver = onVolver) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)) {
                Column(Modifier.fillMaxWidth().padding(18.dp)) {
                    Text("Total en caja", color = MaterialTheme.colorScheme.onPrimary)
                    Text(
                        Formato.dinero(resumen.totalCaja),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        "${resumen.cierresRegistrados} cierre(s) registrados",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TituloSeccion("Ingresos por método de pago")
                    BarraMetodo("Efectivo", resumen.efectivo, resumen.totalCaja, TealOscuro)
                    BarraMetodo("Tarjeta", resumen.tarjeta, resumen.totalCaja, Teal)
                    BarraMetodo("Transferencia", resumen.transferencia, resumen.totalCaja, Color(0xFF5BB8AC))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaDato("Créditos vigentes", resumen.creditosVigentes.toString(), Icons.Filled.ReceiptLong, Modifier.weight(1f))
                TarjetaDato("Ingreso por créditos", Formato.dinero(resumen.ingresoCreditos), Icons.Filled.AttachMoney, Modifier.weight(1f))
            }
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp)) {
                    TituloSeccion("Créditos fiscales")
                    FilaValor("IVA 13 % generado", Formato.dinero(resumen.ivaCreditos), resaltar = true)
                    FilaValor("Créditos anulados", resumen.creditosAnulados.toString())
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaDato(
                    "Cierres con faltante", resumen.cierresConFaltante.toString(), Icons.Filled.TrendingDown,
                    Modifier.weight(1f), color = if (resumen.cierresConFaltante > 0) RojoAlerta else TealOscuro
                )
                TarjetaDato("Total faltante", Formato.dinero(resumen.totalFaltante), Icons.Filled.TrendingDown, Modifier.weight(1f), color = RojoAlerta)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaDato("Seguimientos pendientes", resumen.seguimientosPendientes.toString(), Icons.Filled.EventNote, Modifier.weight(1f), color = AmbarAviso)
                TarjetaDato("Pacientes sin regresar", resumen.seguimientosVencidos.toString(), Icons.Filled.EventBusy, Modifier.weight(1f), color = RojoAlerta)
            }
        }
    }
}

@Composable
private fun BarraMetodo(nombre: String, monto: Double, total: Double, color: Color) {
    val proporcion = if (total > 0) (monto / total).toFloat() else 0f
    Column {
        Row(Modifier.fillMaxWidth()) {
            Text(nombre, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Text("${Formato.dinero(monto)}  (${(proporcion * 100).toInt()} %)", style = MaterialTheme.typography.bodyMedium)
        }
        LinearProgressIndicator(
            progress = { proporcion },
            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
