package pethome.controlador

import pethome.modelo.CreditoFiscal
import pethome.util.DatosInvalidosException
import pethome.util.LoggerErrores
import java.time.LocalDate

/**
 * Gestión principal (CRUD) de Créditos Fiscales: numeración correlativa
 * automática y cálculo de IVA (13%), la lógica de negocio central de este
 * módulo (ver Etapa 1, sección 5.2).
 */
class GestorCreditoFiscal {
    private val creditos = mutableListOf<CreditoFiscal>()
    private var correlativo = 1
    private val anio = LocalDate.now().year

    fun generar(fecha: LocalDate, cliente: String, servicio: String, subtotal: Double): CreditoFiscal {
        if (subtotal <= 0.0) {
            throw DatosInvalidosException("El subtotal de un credito fiscal debe ser mayor a cero")
        }
        val numero = "CCF-%d-%04d".format(anio, correlativo++)
        val credito = CreditoFiscal(numero, fecha, cliente, servicio, subtotal)
        creditos.add(credito)
        return credito
    }

    fun listar(): List<CreditoFiscal> = creditos.toList()

    fun buscarPorNumero(numero: String): CreditoFiscal? = creditos.find { it.numeroCorrelativo == numero }

    fun anular(numero: String): Boolean {
        val credito = buscarPorNumero(numero)
        if (credito == null) {
            LoggerErrores.registrar("Intento de anular un credito fiscal inexistente ($numero)")
            return false
        }
        credito.anulado = true
        return true
    }

    fun eliminar(numero: String): Boolean {
        val eliminado = creditos.removeIf { it.numeroCorrelativo == numero }
        if (!eliminado) LoggerErrores.registrar("Intento de eliminar un credito fiscal inexistente ($numero)")
        return eliminado
    }

    fun listarAnulados(): List<CreditoFiscal> = creditos.filter { it.anulado }

    fun totalEmitidos(): Int = creditos.count { !it.anulado }

    fun ingresoTotal(): Double = creditos.filter { !it.anulado }.sumByDouble { it.total }
}
