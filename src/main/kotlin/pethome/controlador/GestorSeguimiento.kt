package pethome.controlador

import pethome.modelo.EstadoSeguimiento
import pethome.modelo.Seguimiento
import pethome.util.LoggerErrores
import java.time.LocalDate

class GestorSeguimiento {
    private val seguimientos = mutableListOf<Seguimiento>()
    private var siguienteId = 1

    fun crear(paciente: String, procedimiento: String, fechaProcedimiento: LocalDate, diasParaControl: Long): Seguimiento {
        val seguimiento = Seguimiento(siguienteId++, paciente, procedimiento, fechaProcedimiento, diasParaControl)
        seguimientos.add(seguimiento)
        return seguimiento
    }

    fun listar(): List<Seguimiento> = seguimientos.toList()

    fun pendientes(): List<Seguimiento> = seguimientos.filter { it.estado == EstadoSeguimiento.PENDIENTE }

    fun marcarCompletado(id: Int): Boolean {
        val seguimiento = seguimientos.find { it.id == id }
        if (seguimiento == null) {
            LoggerErrores.registrar("Intento de completar un seguimiento inexistente (id=$id)")
            return false
        }
        seguimiento.estado = EstadoSeguimiento.COMPLETADO
        return true
    }

    fun eliminar(id: Int): Boolean {
        val eliminado = seguimientos.removeIf { it.id == id }
        if (!eliminado) LoggerErrores.registrar("Intento de eliminar un seguimiento inexistente (id=$id)")
        return eliminado
    }

    fun proximosAVencer(dias: Int): List<Seguimiento> {
        val hoy = LocalDate.now()
        val limite = hoy.plusDays(dias.toLong())
        return seguimientos.filter {
            it.estado == EstadoSeguimiento.PENDIENTE && !it.fechaControl.isBefore(hoy) && !it.fechaControl.isAfter(limite)
        }
    }
}
