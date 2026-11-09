package com.premiercoders.pethome.modelo

import com.premiercoders.pethome.util.Resumible
import java.time.LocalDate

/**
 * Módulo de Evidencia Clínica: sustituye el envío de fotos por WhatsApp.
 *
 * Foro 2: se guarda en Firestore (colección `evidencias`). Las fotos se suben
 * a Cloudinary y [fotosAdjuntas] guarda sus URL públicas, no las imágenes.
 */
data class EvidenciaClinica(
    val id: String,
    val paciente: String,
    val fecha: LocalDate,
    val diagnostico: String,
    val fotosAdjuntas: List<String> = emptyList(),
    val creadoPor: String = ""
) : Resumible {
    override fun resumen(): String =
        "Evidencia $id - $paciente [$fecha]: $diagnostico | Fotos: ${fotosAdjuntas.size}"

    companion object {
        /** Límite de fotos por registro, el mismo que validan las reglas. */
        const val MAXIMO_FOTOS = 10
    }
}
