package pethome.controlador

import pethome.modelo.CierreCaja
import pethome.util.LoggerErrores
import java.time.LocalDate

/**
 * Gestión principal (CRUD) + lógica de negocio de los Cierres de Caja.
 * Usa una MutableList como colección en memoria (Etapa 2: manejo de
 * colecciones); en la Etapa 3 esta lista se reemplaza por Firestore.
 */
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

    fun listar(): List<CierreCaja> = cierres.toList()

    fun buscarPorId(id: Int): CierreCaja? = cierres.find { it.id == id }

    fun actualizarConteo(id: Int, nuevoMontoContado: Double): Boolean {
        val index = cierres.indexOfFirst { it.id == id }
        if (index == -1) {
            LoggerErrores.registrar("Intento de actualizar un cierre de caja inexistente (id=$id)")
            return false
        }
        cierres[index] = cierres[index].copy(montoContado = nuevoMontoContado)
        return true
    }

    fun eliminar(id: Int): Boolean {
        val eliminado = cierres.removeIf { it.id == id }
        if (!eliminado) LoggerErrores.registrar("Intento de eliminar un cierre de caja inexistente (id=$id)")
        return eliminado
    }

    /** Ingresos acumulados por método de pago, usados por el reporte financiero. */
    fun totalIngresosPorMetodo(): Triple<Double, Double, Double> {
        val efectivo = cierres.sumByDouble { it.efectivo }
        val tarjeta = cierres.sumByDouble { it.tarjeta }
        val transferencia = cierres.sumByDouble { it.transferencia }
        return Triple(efectivo, tarjeta, transferencia)
    }
}
