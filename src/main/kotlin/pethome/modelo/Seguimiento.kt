package pethome.modelo

import pethome.util.Resumible
import java.time.LocalDate

/**
 * Módulo de Seguimiento Post-tratamiento (Etapa 1, sección 5.2). La fecha de
 * control se calcula automáticamente sumando [diasParaControl] a la fecha
 * del procedimiento, y el estado queda explícito (PENDIENTE/COMPLETADO) en
 * lugar de depender solo de un recordatorio pasivo.
 */
data class Seguimiento(
    val id: Int,
    val paciente: String,
    val procedimiento: String,
    val fechaProcedimiento: LocalDate,
    val diasParaControl: Long,
    var estado: EstadoSeguimiento = EstadoSeguimiento.PENDIENTE
) : Resumible {

    val fechaControl: LocalDate
        get() = fechaProcedimiento.plusDays(diasParaControl)

    override fun resumen(): String =
        "Seguimiento #%d - %s (%s) | Procedimiento: %s | Control: %s | Estado: %s"
            .format(id, paciente, procedimiento, fechaProcedimiento, fechaControl, estado)
}
