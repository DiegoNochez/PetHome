package com.premiercoders.pethome.controlador

import com.premiercoders.pethome.modelo.CierreCaja
import com.premiercoders.pethome.util.LoggerErrores
import java.time.LocalDate

/** CRUD + lógica de los cierres de caja (misma lógica de la Etapa 2). */
class GestorCierreCaja {
    private val cierres = mutableListOf<CierreCaja>()
    private var siguienteId = 1

    fun registrar(
        fecha: LocalDate, efectivo: Double, tarjeta: Double,
        transferencia: Double, montoContado: Double, responsable: String
    ): CierreCaja {
        val cierre = CierreCaja(siguienteId++, fecha, efectivo, tarjeta, transferencia, montoContado, responsable)
        cierres.add(cierre)
        return cierre
    }

    /** Del más reciente al más antiguo. */
    fun listar(): List<CierreCaja> = cierres.sortedWith(compareByDescending<CierreCaja> { it.fecha }.thenByDescending { it.id })

    fun buscarPorId(id: Int): CierreCaja? = cierres.find { it.id == id }

    fun actualizarConteo(id: Int, nuevoMontoContado: Double): Boolean {
        val index = cierres.indexOfFirst { it.id == id }
        if (index == -1) {
            LoggerErrores.registrar("Intento de actualizar un cierre inexistente (id=$id)")
            return false
        }
        cierres[index] = cierres[index].copy(montoContado = nuevoMontoContado)
        return true
    }

    fun eliminar(id: Int): Boolean {
        val eliminado = cierres.removeIf { it.id == id }
        if (!eliminado) LoggerErrores.registrar("Intento de eliminar un cierre inexistente (id=$id)")
        return eliminado
    }

    fun cierresConFaltante(): List<CierreCaja> = listar().filter { it.tieneFaltante }

    fun totalFaltante(): Double = cierres.filter { it.tieneFaltante }.sumOf { -it.diferencia }

    fun cantidad(): Int = cierres.size

    /** Ingresos acumulados por método de pago, para el reporte financiero. */
    fun totalIngresosPorMetodo(): Triple<Double, Double, Double> = Triple(
        cierres.sumOf { it.efectivo },
        cierres.sumOf { it.tarjeta },
        cierres.sumOf { it.transferencia }
    )
}
