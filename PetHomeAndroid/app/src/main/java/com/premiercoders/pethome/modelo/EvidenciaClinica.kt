package com.premiercoders.pethome.modelo

import com.premiercoders.pethome.util.Resumible
import java.time.LocalDate

/**
 * Módulo de Evidencia Clínica: sustituye el envío de fotos por WhatsApp.
 * [fotosAdjuntas] guarda la URI de cada imagen elegida con el selector de
 * fotos de Android (en el Foro 2 se subirán a Firebase Storage).
 */
data class EvidenciaClinica(
    val id: Int,
    val paciente: String,
    val fecha: LocalDate,
    val diagnostico: String,
    val fotosAdjuntas: List<String> = emptyList()
) : Resumible {
    override fun resumen(): String =
        "Evidencia #$id - $paciente [$fecha]: $diagnostico | Fotos: ${fotosAdjuntas.size}"
}
