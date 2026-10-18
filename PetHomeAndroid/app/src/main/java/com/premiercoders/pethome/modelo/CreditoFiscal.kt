package com.premiercoders.pethome.modelo

import com.premiercoders.pethome.util.Resumible
import java.time.LocalDate
import java.util.Locale

/**
 * Módulo de Créditos Fiscales. El correlativo lo asigna
 * [com.premiercoders.pethome.controlador.GestorCreditoFiscal]; aquí se calcula
 * el IVA (13 %, El Salvador) y el total del comprobante.
 *
 * Es inmutable (`val`): anular un crédito crea una copia con `anulado = true`.
 * Así Compose detecta el cambio y vuelve a dibujar la lista.
 */
data class CreditoFiscal(
    val numeroCorrelativo: String,
    val fecha: LocalDate,
    val cliente: String,
    val servicio: String,
    val subtotal: Double,
    val anulado: Boolean = false
) : Resumible {
    companion object {
        const val TASA_IVA = 0.13

        fun calcularIva(subtotal: Double): Double = subtotal * TASA_IVA
    }

    val iva: Double
        get() = calcularIva(subtotal)

    val total: Double
        get() = subtotal + iva

    override fun resumen(): String {
        val estado = if (anulado) "ANULADO" else "vigente"
        return String.format(
            Locale.US, "%s [%s] %s | %s | Subtotal: $%.2f + IVA: $%.2f = $%.2f (%s)",
            numeroCorrelativo, fecha, cliente, servicio, subtotal, iva, total, estado
        )
    }
}
