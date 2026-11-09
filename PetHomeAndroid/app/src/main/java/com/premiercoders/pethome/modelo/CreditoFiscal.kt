package com.premiercoders.pethome.modelo

import com.premiercoders.pethome.util.Resumible
import java.time.LocalDate
import java.util.Locale

/**
 * Módulo de Créditos Fiscales. El correlativo lo asigna
 * [com.premiercoders.pethome.controlador.GestorCreditoFiscal]; aquí se calcula
 * el IVA (13 %, El Salvador) y el total del comprobante.
 *
 * Foro 2: se guarda en Firestore (colección `creditosFiscales`) usando el
 * correlativo como ID del documento, así no puede repetirse. Se agregan el
 * NIT y el NRC del cliente, que el comprobante de crédito fiscal exige.
 */
data class CreditoFiscal(
    val numeroCorrelativo: String,
    val fecha: LocalDate,
    val cliente: String,
    val nit: String,
    val nrc: String,
    val servicio: String,
    val subtotal: Double,
    val anulado: Boolean = false,
    val creadoPor: String = ""
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
            Locale.US, "%s [%s] %s (NRC %s) | %s | Subtotal: $%.2f + IVA: $%.2f = $%.2f (%s)",
            numeroCorrelativo, fecha, cliente, nrc, servicio, subtotal, iva, total, estado
        )
    }
}
