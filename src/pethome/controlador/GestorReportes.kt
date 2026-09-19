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
        sb.appendln("========== REPORTE FINANCIERO - PET HOME ==========")
        sb.appendln("Ingresos por metodo de pago (cierres de caja):")
        sb.appendln("  Efectivo:      $%.2f".format(efectivo))
        sb.appendln("  Tarjeta:       $%.2f".format(tarjeta))
        sb.appendln("  Transferencia: $%.2f".format(transferencia))
        sb.appendln("  Total caja:    $%.2f".format(ingresoCaja))
        sb.appendln()
        sb.appendln("Creditos fiscales emitidos (vigentes): $creditosEmitidos")
        sb.appendln("Ingreso total por creditos fiscales (incluye IVA): $%.2f".format(ingresoCreditos))
        sb.appendln()
        sb.appendln("Seguimientos post-tratamiento pendientes: $pendientes")
        sb.appendln("=====================================================")
        return sb.toString()
    }
}
