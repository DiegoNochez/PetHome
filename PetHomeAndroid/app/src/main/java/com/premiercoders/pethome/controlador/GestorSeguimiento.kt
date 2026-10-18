package com.premiercoders.pethome.controlador

import com.premiercoders.pethome.modelo.EstadoSeguimiento
import com.premiercoders.pethome.modelo.Seguimiento
import com.premiercoders.pethome.util.LoggerErrores
import java.time.LocalDate

class GestorSeguimiento {
    private val seguimientos = mutableListOf<Seguimiento>()
    private var siguienteId = 1

    fun crear(paciente: String, procedimiento: String, fechaProcedimiento: LocalDate, diasParaControl: Long): Seguimiento {
        val seguimiento = Seguimiento(siguienteId++, paciente.trim(), procedimiento.trim(), fechaProcedimiento, diasParaControl)
        seguimientos.add(seguimiento)
        return seguimiento
    }

    /** Ordenados por la fecha de control más cercana. */
    fun listar(): List<Seguimiento> = seguimientos.sortedBy { it.fechaControl }

    fun pendientes(): List<Seguimiento> = listar().filter { it.estado == EstadoSeguimiento.PENDIENTE }

    fun vencidos(hoy: LocalDate = LocalDate.now()): List<Seguimiento> = listar().filter { it.estaVencido(hoy) }

    fun marcarCompletado(id: Int): Boolean {
        val index = seguimientos.indexOfFirst { it.id == id }
        if (index == -1) {
            LoggerErrores.registrar("Intento de completar un seguimiento inexistente (id=$id)")
            return false
        }
        seguimientos[index] = seguimientos[index].copy(estado = EstadoSeguimiento.COMPLETADO)
        return true
    }

    fun eliminar(id: Int): Boolean {
        val eliminado = seguimientos.removeIf { it.id == id }
        if (!eliminado) LoggerErrores.registrar("Intento de eliminar un seguimiento inexistente (id=$id)")
        return eliminado
    }

    fun proximosAVencer(dias: Int, hoy: LocalDate = LocalDate.now()): List<Seguimiento> {
        val limite = hoy.plusDays(dias.toLong())
        return listar().filter {
            it.estado == EstadoSeguimiento.PENDIENTE && !it.fechaControl.isBefore(hoy) && !it.fechaControl.isAfter(limite)
        }
    }
}
