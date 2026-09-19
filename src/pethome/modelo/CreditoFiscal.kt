package pethome.modelo

import pethome.util.Resumible
import java.time.LocalDate

/**
 * Módulo de Créditos Fiscales (Etapa 1, sección 5.2).
 * La numeración correlativa la asigna [pethome.controlador.GestorCreditoFiscal];
 * aquí solo se calcula el IVA (13%, El Salvador) y el total del comprobante.
 */
data class CreditoFiscal(
    val numeroCorrelativo: String,
    val fecha: LocalDate,
    val cliente: String,
    val servicio: String,
    val subtotal: Double,
    var anulado: Boolean = false
) : Resumible {
    companion object {
        const val TASA_IVA = 0.13
    }

    val iva: Double
        get() = subtotal * TASA_IVA

    val total: Double
        get() = subtotal + iva

    override fun resumen(): String {
        val estado = if (anulado) "ANULADO" else "vigente"
        return "%s [%s] %s | %s | Subtotal: $%.2f + IVA(13%%): $%.2f = Total: $%.2f (%s)"
            .format(numeroCorrelativo, fecha, cliente, servicio, subtotal, iva, total, estado)
    }
}
