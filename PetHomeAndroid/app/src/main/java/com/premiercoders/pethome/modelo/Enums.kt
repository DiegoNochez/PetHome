package com.premiercoders.pethome.modelo

enum class MetodoPago { EFECTIVO, TARJETA, TRANSFERENCIA }

enum class EstadoSeguimiento { PENDIENTE, COMPLETADO }

/** Los dos roles reales del personal de Pet Home (Etapa 1). */
enum class Rol(val descripcion: String) {
    ADMINISTRADOR("Administrador / Veterinario (MVZ)"),
    RECEPCIONISTA("Recepcionista / Cajero")
}

/**
 * Los 5 módulos de la app. Cada [Usuario] decide, por polimorfismo, a cuáles
 * tiene acceso (ver [Usuario.puedeAcceder]).
 */
enum class Modulo(val titulo: String, val descripcion: String) {
    CIERRE_CAJA("Cierre de Caja", "Concilia ingresos contra el conteo físico"),
    CREDITOS_FISCALES("Créditos Fiscales", "Correlativo automático e IVA 13%"),
    EVIDENCIA_CLINICA("Evidencia Clínica", "Fotos clínicas adjuntas al paciente"),
    SEGUIMIENTO("Seguimiento", "Controles post-tratamiento"),
    REPORTES("Reportes", "Resumen financiero del sistema")
}
