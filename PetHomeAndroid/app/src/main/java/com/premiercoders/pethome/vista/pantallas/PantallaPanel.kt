package com.premiercoders.pethome.vista.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.premiercoders.pethome.modelo.Modulo
import com.premiercoders.pethome.modelo.ResumenFinanciero
import com.premiercoders.pethome.modelo.Usuario
import com.premiercoders.pethome.navegacion.icono
import com.premiercoders.pethome.util.Formato
import com.premiercoders.pethome.vista.componentes.DialogoConfirmacion
import com.premiercoders.pethome.vista.componentes.PantallaBase
import com.premiercoders.pethome.vista.componentes.TarjetaDato
import com.premiercoders.pethome.vista.componentes.TituloSeccion
import com.premiercoders.pethome.vista.tema.AmbarAviso
import com.premiercoders.pethome.vista.tema.RojoAlerta

/**
 * Panel principal. Los módulos se generan a partir de
 * [Usuario.modulosPermitidos], así cada rol ve solo lo que le corresponde.
 */
@Composable
fun PantallaPanel(
    usuario: Usuario,
    resumen: ResumenFinanciero,
    onAbrirModulo: (Modulo) -> Unit,
    onCerrarSesion: () -> Unit
) {
    var confirmarSalida by remember { mutableStateOf(false) }
    val modulos = usuario.modulosPermitidos()
    val esAdmin = usuario.puedeAcceder(Modulo.SEGUIMIENTO)

    PantallaBase(
        titulo = "Pet Home",
        acciones = {
            IconButton(onClick = { confirmarSalida = true }) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text("Hola, ${usuario.nombre}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                AssistChip(onClick = {}, label = { Text(usuario.rol.descripcion) })
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaDato(
                    "Caja acumulada", Formato.dinero(resumen.totalCaja), Icons.Filled.PointOfSale,
                    modifier = Modifier.weight(1f)
                )
                TarjetaDato(
                    "Créditos vigentes", resumen.creditosVigentes.toString(), Icons.Filled.ReceiptLong,
                    modifier = Modifier.weight(1f)
                )
            }
            if (resumen.cierresConFaltante > 0) {
                AvisoPanel("${resumen.cierresConFaltante} cierre(s) con faltante por ${Formato.dinero(resumen.totalFaltante)}")
            }
            if (esAdmin && resumen.seguimientosVencidos > 0) {
                AvisoPanel("${resumen.seguimientosVencidos} paciente(s) no han vuelto a su control")
            }

            TituloSeccion("Módulos")
            modulos.chunked(2).forEach { fila ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    fila.forEach { modulo ->
                        TarjetaModulo(modulo, Modifier.weight(1f)) { onAbrirModulo(modulo) }
                    }
                    if (fila.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }

    if (confirmarSalida) {
        DialogoConfirmacion(
            titulo = "Cerrar sesión",
            mensaje = "¿Deseas salir de tu cuenta?",
            textoConfirmar = "Salir",
            onConfirmar = { confirmarSalida = false; onCerrarSesion() },
            onCancelar = { confirmarSalida = false }
        )
    }
}

@Composable
private fun TarjetaModulo(modulo: Modulo, modifier: Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier.height(138.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(modulo.icono(), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Text(modulo.titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(modulo.descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AvisoPanel(texto: String) {
    Card(colors = CardDefaults.cardColors(containerColor = AmbarAviso.copy(alpha = 0.12f)), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.EventBusy, contentDescription = null, tint = RojoAlerta)
            Spacer(Modifier.size(10.dp))
            Text(texto, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
