package com.premiercoders.pethome.controlador

import com.premiercoders.pethome.modelo.CreditoFiscal
import com.premiercoders.pethome.util.DatosInvalidosException
import com.premiercoders.pethome.util.LoggerErrores
import java.time.LocalDate
import java.util.Locale

/** Créditos fiscales: correlativo automático CCF-AAAA-#### e IVA 13 %. */
class GestorCreditoFiscal {
    private val creditos = mutableListOf<CreditoFiscal>()
    private var correlativo = 1
    private val anio = LocalDate.now().year

    /** Número que recibirá el próximo crédito; la pantalla lo muestra antes de emitir. */
    fun siguienteNumero(): String = String.format(Locale.US, "CCF-%d-%04d", anio, correlativo)

    fun generar(fecha: LocalDate, cliente: String, servicio: String, subtotal: Double): CreditoFiscal {
        if (subtotal <= 0.0) {
            LoggerErrores.registrar("Crédito fiscal rechazado: subtotal $subtotal")
            throw DatosInvalidosException("El subtotal debe ser mayor a cero")
        }
        val credito = CreditoFiscal(siguienteNumero(), fecha, cliente.trim(), servicio.trim(), subtotal)
        correlativo++
        creditos.add(credito)
        return credito
    }

    fun listar(): List<CreditoFiscal> = creditos.sortedByDescending { it.numeroCorrelativo }

    fun buscarPorNumero(numero: String): CreditoFiscal? = creditos.find { it.numeroCorrelativo == numero }

    /** No se borra: un comprobante fiscal emitido se anula y conserva su número. */
    fun anular(numero: String): Boolean {
        val index = creditos.indexOfFirst { it.numeroCorrelativo == numero }
        if (index == -1) {
            LoggerErrores.registrar("Intento de anular un crédito inexistente ($numero)")
            return false
        }
        creditos[index] = creditos[index].copy(anulado = true)
        return true
    }

    fun listarAnulados(): List<CreditoFiscal> = listar().filter { it.anulado }

    fun listarVigentes(): List<CreditoFiscal> = listar().filter { !it.anulado }

    fun totalEmitidos(): Int = creditos.count { !it.anulado }

    fun totalAnulados(): Int = creditos.count { it.anulado }

    fun ingresoTotal(): Double = creditos.filter { !it.anulado }.sumOf { it.total }

    fun ivaTotal(): Double = creditos.filter { !it.anulado }.sumOf { it.iva }
}
