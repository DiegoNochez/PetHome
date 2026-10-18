package com.premiercoders.pethome.controlador

import com.premiercoders.pethome.modelo.ResumenFinanciero

/**
 * Reportes financieros: no guarda datos propios, procesa lo que ya está en
 * los demás gestores (igual que en la Etapa 2).
 */
class GestorReportes(
    private val gestorCaja: GestorCierreCaja,
    private val gestorCredito: GestorCreditoFiscal,
    private val gestorSeguimiento: GestorSeguimiento
) {
    fun generarResumen(): ResumenFinanciero {
        val (efectivo, tarjeta, transferencia) = gestorCaja.totalIngresosPorMetodo()
        return ResumenFinanciero(
            efectivo = efectivo,
            tarjeta = tarjeta,
            transferencia = transferencia,
            cierresRegistrados = gestorCaja.cantidad(),
            cierresConFaltante = gestorCaja.cierresConFaltante().size,
            totalFaltante = gestorCaja.totalFaltante(),
            creditosVigentes = gestorCredito.totalEmitidos(),
            creditosAnulados = gestorCredito.totalAnulados(),
            ingresoCreditos = gestorCredito.ingresoTotal(),
            ivaCreditos = gestorCredito.ivaTotal(),
            seguimientosPendientes = gestorSeguimiento.pendientes().size,
            seguimientosVencidos = gestorSeguimiento.vencidos().size
        )
    }
}
