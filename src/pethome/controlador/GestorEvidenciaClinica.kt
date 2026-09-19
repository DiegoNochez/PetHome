package pethome.controlador

import pethome.modelo.EvidenciaClinica
import pethome.util.LoggerErrores
import java.time.LocalDate

class GestorEvidenciaClinica {
    private val registros = mutableListOf<EvidenciaClinica>()
    private var siguienteId = 1

    fun crear(paciente: String, fecha: LocalDate, diagnostico: String): EvidenciaClinica {
        val evidencia = EvidenciaClinica(siguienteId++, paciente, fecha, diagnostico)
        registros.add(evidencia)
        return evidencia
    }

    fun listar(): List<EvidenciaClinica> = registros.toList()

    fun adjuntarFoto(id: Int, nombreArchivo: String): Boolean {
        val evidencia = registros.find { it.id == id }
        if (evidencia == null) {
            LoggerErrores.registrar("Intento de adjuntar foto a una evidencia inexistente (id=$id)")
            return false
        }
        evidencia.fotosAdjuntas.add(nombreArchivo)
        return true
    }

    fun eliminar(id: Int): Boolean {
        val eliminado = registros.removeIf { it.id == id }
        if (!eliminado) LoggerErrores.registrar("Intento de eliminar una evidencia clínica inexistente (id=$id)")
        return eliminado
    }
}
