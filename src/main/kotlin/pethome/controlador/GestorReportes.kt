package pethome.controlador

/**
 * Módulo de Reportes Financieros (Etapa 1, sección 5.2). Procesa los datos
 * ya registrados en los otros gestores -- no mantiene su propia colección --
 * para producir un resumen del sistema (requerimiento funcional 4 de la
 * Etapa 2).
 */
class GestorReportes(
    private val gestorCaja: GestorCierreCaja,
    private val gestorCredito: GestorCreditoFiscal,
    private val gestorSeguimiento: GestorSeguimiento
) {
    fun generarResumen(): String {
        val (efectivo, tarjeta, transferencia) = gestorCaja.totalIngresosPorMetodo()
        val ingresoCaja = efectivo + tarjeta + transferencia
        val creditosEmitidos = gestorCredito.totalEmitidos()
        val ingresoCreditos = gestorCredito.ingresoTotal()
        val pendientes = gestorSeguimiento.pendientes().size

        val sb = StringBuilder()
        sb.appendLine("========== REPORTE FINANCIERO - PET HOME ==========")
        sb.appendLine("Ingresos por metodo de pago (cierres de caja):")
        sb.appendLine("  Efectivo:      $%.2f".format(efectivo))
        sb.appendLine("  Tarjeta:       $%.2f".format(tarjeta))
        sb.appendLine("  Transferencia: $%.2f".format(transferencia))
        sb.appendLine("  Total caja:    $%.2f".format(ingresoCaja))
        sb.appendLine()
        sb.appendLine("Creditos fiscales emitidos (vigentes): $creditosEmitidos")
        sb.appendLine("Ingreso total por creditos fiscales (incluye IVA): $%.2f".format(ingresoCreditos))
        sb.appendLine()
        sb.appendLine("Seguimientos post-tratamiento pendientes: $pendientes")
        sb.appendLine("=====================================================")
        return sb.toString()
    }
}
