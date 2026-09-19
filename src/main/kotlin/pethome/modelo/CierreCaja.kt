package pethome.modelo

import pethome.util.Resumible
import java.time.LocalDate

/**
 * Módulo de Cierre de Caja (Etapa 1, sección 5.2).
 * La lógica de negocio real está en las propiedades calculadas
 * [totalEsperado] y [diferencia]: concilian el efectivo esperado según los
 * ingresos del día contra el conteo físico reportado por el recepcionista.
 */
data class CierreCaja(
    val id: Int,
    val fecha: LocalDate,
    val efectivo: Double,
    val tarjeta: Double,
    val transferencia: Double,
    val montoContado: Double,
    val responsable: String
) : Resumible {

    val totalEsperado: Double
        get() = efectivo + tarjeta + transferencia

    val diferencia: Double
        get() = montoContado - totalEsperado

    fun estadoDiferencia(): String = when {
        diferencia > 0.005 -> "Sobrante de $%.2f".format(diferencia)
        diferencia < -0.005 -> "Faltante de $%.2f".format(-diferencia)
        else -> "Caja cuadrada exactamente"
    }

    override fun resumen(): String =
        "Cierre #%d [%s] responsable: %s | Esperado: $%.2f | Contado: $%.2f | %s"
            .format(id, fecha, responsable, totalEsperado, montoContado, estadoDiferencia())
}
