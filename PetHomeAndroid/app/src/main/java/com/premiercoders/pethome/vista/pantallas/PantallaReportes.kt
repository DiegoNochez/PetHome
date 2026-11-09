package com.premiercoders.pethome.vista.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.premiercoders.pethome.modelo.IngresoDiario
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
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

/**
 * Reportes financieros: solo lectura, calculados por GestorReportes.
 * Foro 2: los datos vienen de Firestore y se agrega una segunda gráfica,
 * los ingresos de caja de los últimos 7 días.
 */
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

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TituloSeccion("Ingresos de los últimos 7 días")
                    GraficoIngresosDiarios(resumen.ingresosPorDia)
                    FilaValor("Total de la semana", Formato.dinero(resumen.ingresosPorDia.sumOf { it.total }), resaltar = true)
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

/**
 * Gráfica de barras de los ingresos diarios. Cada barra es proporcional al
 * día con más ingresos; el día de hoy va en el color más oscuro.
 */
@Composable
private fun GraficoIngresosDiarios(datos: List<IngresoDiario>) {
    if (datos.isEmpty()) return
    val maximo = datos.maxOf { it.total }.takeIf { it > 0 } ?: 1.0
    val hoy = LocalDate.now()
    val espanol = Locale("es", "SV")
    Row(
        modifier = Modifier.fillMaxWidth().height(190.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        datos.forEach { dia ->
            val alto = (130 * (dia.total / maximo)).toFloat().coerceAtLeast(2f)
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (dia.total > 0) "\$${dia.total.toInt()}" else "",
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                Spacer(Modifier.height(2.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(alto.dp)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(if (dia.fecha == hoy) TealOscuro else Teal.copy(alpha = 0.55f))
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    dia.fecha.dayOfWeek.getDisplayName(TextStyle.SHORT, espanol).take(3),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (dia.fecha == hoy) FontWeight.Bold else FontWeight.Normal
                )
                Text("${dia.fecha.dayOfMonth}", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
