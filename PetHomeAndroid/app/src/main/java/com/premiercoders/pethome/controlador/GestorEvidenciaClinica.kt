package com.premiercoders.pethome.controlador

import com.premiercoders.pethome.modelo.EvidenciaClinica
import com.premiercoders.pethome.util.LoggerErrores
import java.time.LocalDate

class GestorEvidenciaClinica {
    private val registros = mutableListOf<EvidenciaClinica>()
    private var siguienteId = 1

    fun crear(paciente: String, fecha: LocalDate, diagnostico: String): EvidenciaClinica {
        val evidencia = EvidenciaClinica(siguienteId++, paciente.trim(), fecha, diagnostico.trim())
        registros.add(evidencia)
        return evidencia
    }

    fun listar(): List<EvidenciaClinica> = registros.sortedWith(compareByDescending<EvidenciaClinica> { it.fecha }.thenByDescending { it.id })

    fun adjuntarFoto(id: Int, uriFoto: String): Boolean {
        val index = registros.indexOfFirst { it.id == id }
        if (index == -1) {
            LoggerErrores.registrar("Intento de adjuntar foto a una evidencia inexistente (id=$id)")
            return false
        }
        val actual = registros[index]
        registros[index] = actual.copy(fotosAdjuntas = actual.fotosAdjuntas + uriFoto)
        return true
    }

    fun eliminar(id: Int): Boolean {
        val eliminado = registros.removeIf { it.id == id }
        if (!eliminado) LoggerErrores.registrar("Intento de eliminar una evidencia inexistente (id=$id)")
        return eliminado
    }
}
