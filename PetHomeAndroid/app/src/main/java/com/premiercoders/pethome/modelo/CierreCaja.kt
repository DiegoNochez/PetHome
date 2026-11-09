package com.premiercoders.pethome.modelo

import com.premiercoders.pethome.util.Resumible
import java.time.LocalDate
import java.util.Locale

/**
 * Módulo de Cierre de Caja. La lógica de negocio está en [totalEsperado] y
 * [diferencia]: concilian lo que debería haber según los ingresos del día
 * contra el conteo físico del recepcionista.
 *
 * Foro 2: el [id] es el ID del documento en Firestore (colección
 * `cierresCaja`) y [creadoPor] es el uid de quien registró el cierre.
 */
data class CierreCaja(
    val id: String,
    val fecha: LocalDate,
    val efectivo: Double,
    val tarjeta: Double,
    val transferencia: Double,
    val montoContado: Double,
    val responsable: String,
    val creadoPor: String = ""
) : Resumible {

    val totalEsperado: Double
        get() = efectivo + tarjeta + transferencia

    val diferencia: Double
        get() = montoContado - totalEsperado

    val tieneFaltante: Boolean
        get() = diferencia < -TOLERANCIA

    val tieneSobrante: Boolean
        get() = diferencia > TOLERANCIA

    fun estadoDiferencia(): String = when {
        tieneSobrante -> String.format(Locale.US, "Sobrante de $%.2f", diferencia)
        tieneFaltante -> String.format(Locale.US, "Faltante de $%.2f", -diferencia)
        else -> "Caja cuadrada"
    }

    override fun resumen(): String = String.format(
        Locale.US, "Cierre %s [%s] %s | Esperado: $%.2f | Contado: $%.2f | %s",
        id, fecha, responsable, totalEsperado, montoContado, estadoDiferencia()
    )

    companion object {
        /** Medio centavo: evita marcar como faltante un error de redondeo. */
        const val TOLERANCIA = 0.005
    }
}
