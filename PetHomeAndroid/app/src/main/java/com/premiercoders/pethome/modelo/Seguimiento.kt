package com.premiercoders.pethome.modelo

import com.premiercoders.pethome.util.Resumible
import java.time.LocalDate

/**
 * Seguimiento post-tratamiento. La fecha de control se calcula sumando
 * [diasParaControl] a la fecha del procedimiento, y [estaVencido] detecta el
 * caso real de la entrevista: un paciente que no regresó a su control.
 */
data class Seguimiento(
    val id: Int,
    val paciente: String,
    val procedimiento: String,
    val fechaProcedimiento: LocalDate,
    val diasParaControl: Long,
    val estado: EstadoSeguimiento = EstadoSeguimiento.PENDIENTE
) : Resumible {

    val fechaControl: LocalDate
        get() = fechaProcedimiento.plusDays(diasParaControl)

    fun estaVencido(hoy: LocalDate = LocalDate.now()): Boolean =
        estado == EstadoSeguimiento.PENDIENTE && fechaControl.isBefore(hoy)

    override fun resumen(): String =
        "Seguimiento #$id - $paciente ($procedimiento) | Control: $fechaControl | $estado"
}
