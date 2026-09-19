package pethome.modelo

import pethome.util.Resumible
import java.time.LocalDate

/**
 * Módulo de Evidencia Clínica (Etapa 1, sección 5.2). Sustituye el envío de
 * fotografías por WhatsApp: las fotos quedan adjuntas al registro del
 * paciente dentro del propio sistema.
 */
data class EvidenciaClinica(
    val id: Int,
    val paciente: String,
    val fecha: LocalDate,
    val diagnostico: String,
    val fotosAdjuntas: MutableList<String> = mutableListOf()
) : Resumible {
    override fun resumen(): String =
        "Evidencia #%d - %s [%s]: %s | Fotos adjuntas: %d %s"
            .format(id, paciente, fecha, diagnostico, fotosAdjuntas.size, fotosAdjuntas)
}
