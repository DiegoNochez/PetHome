package com.premiercoders.pethome.modelo

/**
 * Datos ya calculados del reporte financiero. En la Etapa 2 el reporte era
 * un texto para la consola; ahora es un objeto para que la vista lo muestre
 * en tarjetas y barras.
 */
data class ResumenFinanciero(
    val efectivo: Double,
    val tarjeta: Double,
    val transferencia: Double,
    val cierresRegistrados: Int,
    val cierresConFaltante: Int,
    val totalFaltante: Double,
    val creditosVigentes: Int,
    val creditosAnulados: Int,
    val ingresoCreditos: Double,
    val ivaCreditos: Double,
    val seguimientosPendientes: Int,
    val seguimientosVencidos: Int,
    /** Foro 2: ingresos de caja de los últimos 7 días, para la segunda gráfica. */
    val ingresosPorDia: List<IngresoDiario> = emptyList()
) {
    val totalCaja: Double
        get() = efectivo + tarjeta + transferencia
}

/** Total cobrado en caja en un día (efectivo + tarjeta + transferencia). */
data class IngresoDiario(val fecha: java.time.LocalDate, val total: Double)
