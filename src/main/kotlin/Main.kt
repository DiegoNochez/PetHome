import pethome.controlador.GestorCierreCaja
import pethome.controlador.GestorCreditoFiscal
import pethome.controlador.GestorEvidenciaClinica
import pethome.controlador.GestorReportes
import pethome.controlador.GestorSeguimiento
import pethome.modelo.Administrador
import pethome.modelo.Recepcionista
import pethome.util.DatosInvalidosException
import pethome.util.LoggerErrores
import pethome.util.Validador
import pethome.vista.Consola

/**
 * Pet Home - Etapa 2: Desarrollo Base del Proyecto (DSM941)
 * Nucleo funcional en Kotlin, modo consola, de los 5 modulos reales
 * propuestos en la Etapa 1: Cierre de Caja, Creditos Fiscales, Evidencia
 * Clinica, Seguimiento Post-tratamiento y Reportes Financieros.
 */
fun main() {
    println("======================================================")
    println(" PET HOME -- Sistema interno de personal (modo consola)")
    println(" Etapa 2 - DSM941 - Equipo PremierCoders")
    println("======================================================")

    val gestorCaja = GestorCierreCaja()
    val gestorCredito = GestorCreditoFiscal()
    val gestorEvidencia = GestorEvidenciaClinica()
    val gestorSeguimiento = GestorSeguimiento()
    val gestorReportes = GestorReportes(gestorCaja, gestorCredito, gestorSeguimiento)

    val usuario = Consola.iniciarSesion()
    println("\nSesion iniciada: ${usuario.resumen()}")

    when (usuario) {
        is Administrador -> menuAdministrador(usuario, gestorCaja, gestorCredito, gestorEvidencia, gestorSeguimiento, gestorReportes)
        is Recepcionista -> menuRecepcionista(usuario, gestorCaja, gestorCredito)
    }

    println("\nSesion finalizada. Gracias por usar Pet Home.")
}

// ---------------------------------------------------------------------
// Menus por rol (control de acceso diferenciado, Etapa 1 seccion 5.1)
// ---------------------------------------------------------------------

fun menuRecepcionista(usuario: Recepcionista, gestorCaja: GestorCierreCaja, gestorCredito: GestorCreditoFiscal) {
    var salir = false
    while (!salir) {
        println(Consola.tituloMenu("RECEPCIONISTA / CAJERO -- ${usuario.nombre}"))
        println("1. Registrar cierre de caja")
        println("2. Listar cierres de caja")
        println("3. Actualizar conteo fisico de un cierre")
        println("4. Eliminar un cierre de caja")
        println("5. Generar credito fiscal")
        println("6. Listar creditos fiscales")
        println("7. Anular un credito fiscal")
        println("0. Cerrar sesion")
        val opcion = Validador.leerEntero("Selecciona una opcion: ", 0, 7)
        try {
            when (opcion) {
                1 -> registrarCierreCaja(gestorCaja, usuario.nombre)
                2 -> listarCierresCaja(gestorCaja)
                3 -> actualizarConteoCaja(gestorCaja)
                4 -> eliminarCierreCaja(gestorCaja)
                5 -> generarCreditoFiscal(gestorCredito)
                6 -> listarCreditosFiscales(gestorCredito)
                7 -> anularCreditoFiscal(gestorCredito)
                0 -> salir = true
            }
        } catch (e: Exception) {
            LoggerErrores.registrar("Error inesperado en menu Recepcionista: ${e.message}")
            println("  > Ocurrio un error inesperado. Se registro en errores.log")
        }
    }
}

fun menuAdministrador(
    usuario: Administrador,
    gestorCaja: GestorCierreCaja,
    gestorCredito: GestorCreditoFiscal,
    gestorEvidencia: GestorEvidenciaClinica,
    gestorSeguimiento: GestorSeguimiento,
    gestorReportes: GestorReportes
) {
    var salir = false
    while (!salir) {
        println(Consola.tituloMenu("ADMINISTRADOR / VETERINARIO (MVZ) -- ${usuario.nombre}"))
        println("1. Registrar cierre de caja")
        println("2. Listar cierres de caja")
        println("3. Generar credito fiscal")
        println("4. Listar creditos fiscales")
        println("5. Anular un credito fiscal")
        println("6. Registrar evidencia clinica")
        println("7. Adjuntar foto a una evidencia clinica")
        println("8. Listar evidencias clinicas")
        println("9. Registrar seguimiento post-tratamiento")
        println("10. Marcar seguimiento como completado")
        println("11. Listar seguimientos pendientes")
        println("12. Ver reporte financiero")
        println("13. Ver seguimientos proximos a vencer (7 dias)")
        println("0. Cerrar sesion")
        val opcion = Validador.leerEntero("Selecciona una opcion: ", 0, 13)
        try {
            when (opcion) {
                1 -> registrarCierreCaja(gestorCaja, usuario.nombre)
                2 -> listarCierresCaja(gestorCaja)
                3 -> generarCreditoFiscal(gestorCredito)
                4 -> listarCreditosFiscales(gestorCredito)
                5 -> anularCreditoFiscal(gestorCredito)
                6 -> registrarEvidenciaClinica(gestorEvidencia)
                7 -> adjuntarFotoEvidencia(gestorEvidencia)
                8 -> listarEvidenciasClinicas(gestorEvidencia)
                9 -> registrarSeguimiento(gestorSeguimiento)
                10 -> completarSeguimiento(gestorSeguimiento)
                11 -> listarSeguimientosPendientes(gestorSeguimiento)
                12 -> println(gestorReportes.generarResumen())
                13 -> listarProximosAVencer(gestorSeguimiento)
                0 -> salir = true
            }
        } catch (e: Exception) {
            LoggerErrores.registrar("Error inesperado en menu Administrador: ${e.message}")
            println("  > Ocurrio un error inesperado. Se registro en errores.log")
        }
    }
}

// ---------------------------------------------------------------------
// Modulo de Cierre de Caja
// ---------------------------------------------------------------------

fun registrarCierreCaja(gestor: GestorCierreCaja, responsable: String) {
    println("\n--- Registrar cierre de caja ---")
    val fecha = Validador.leerFecha("Fecha del cierre")
    val efectivo = Validador.leerDoublePositivo("Monto en efectivo: $")
    val tarjeta = Validador.leerDoublePositivo("Monto en tarjeta: $")
    val transferencia = Validador.leerDoublePositivo("Monto en transferencia: $")
    val contado = Validador.leerDoublePositivo("Monto contado fisicamente en caja: $")
    val cierre = gestor.registrar(fecha, efectivo, tarjeta, transferencia, contado, responsable)
    println("Cierre registrado exitosamente:")
    println("  " + cierre.resumen())
}

fun listarCierresCaja(gestor: GestorCierreCaja) {
    println("\n--- Cierres de caja registrados ---")
    val lista = gestor.listar()
    if (lista.isEmpty()) {
        println("  (No hay cierres registrados todavia)")
        return
    }
    lista.forEach { println("  " + it.resumen()) }
}

fun actualizarConteoCaja(gestor: GestorCierreCaja) {
    listarCierresCaja(gestor)
    val id = Validador.leerEntero("\nID del cierre a actualizar: ", 1)
    val nuevoMonto = Validador.leerDoublePositivo("Nuevo monto contado: $")
    if (gestor.actualizarConteo(id, nuevoMonto)) {
        println("Cierre actualizado. Nueva diferencia: " + gestor.buscarPorId(id)?.estadoDiferencia())
    } else {
        println("No se encontro un cierre con ese ID.")
    }
}

fun eliminarCierreCaja(gestor: GestorCierreCaja) {
    listarCierresCaja(gestor)
    val id = Validador.leerEntero("\nID del cierre a eliminar: ", 1)
    println(if (gestor.eliminar(id)) "Cierre eliminado." else "No se encontro un cierre con ese ID.")
}

// ---------------------------------------------------------------------
// Modulo de Creditos Fiscales
// ---------------------------------------------------------------------

fun generarCreditoFiscal(gestor: GestorCreditoFiscal) {
    println("\n--- Generar credito fiscal ---")
    val fecha = Validador.leerFecha("Fecha del comprobante")
    val cliente = Validador.leerTextoNoVacio("Nombre del cliente: ")
    val servicio = Validador.leerTextoNoVacio("Servicio prestado: ")
    val subtotal = Validador.leerDoublePositivo("Subtotal del servicio (sin IVA): $")
    try {
        val credito = gestor.generar(fecha, cliente, servicio, subtotal)
        println("Credito fiscal generado:")
        println("  " + credito.resumen())
    } catch (e: DatosInvalidosException) {
        LoggerErrores.registrar("Datos invalidos al generar credito fiscal: ${e.message}")
        println("  > No se pudo generar el credito fiscal: ${e.message}")
    }
}

fun listarCreditosFiscales(gestor: GestorCreditoFiscal) {
    println("\n--- Creditos fiscales emitidos ---")
    val lista = gestor.listar()
    if (lista.isEmpty()) {
        println("  (No hay creditos fiscales emitidos todavia)")
        return
    }
    lista.forEach { println("  " + it.resumen()) }
}

fun anularCreditoFiscal(gestor: GestorCreditoFiscal) {
    listarCreditosFiscales(gestor)
    val numero = Validador.leerTextoNoVacio("\nNumero de comprobante a anular: ")
    println(if (gestor.anular(numero)) "Credito fiscal anulado." else "No se encontro ese comprobante.")
}

// ---------------------------------------------------------------------
// Modulo de Evidencia Clinica
// ---------------------------------------------------------------------

fun registrarEvidenciaClinica(gestor: GestorEvidenciaClinica) {
    println("\n--- Registrar evidencia clinica ---")
    val paciente = Validador.leerTextoNoVacio("Nombre del paciente (mascota): ")
    val fecha = Validador.leerFecha("Fecha de la consulta")
    val diagnostico = Validador.leerTextoNoVacio("Diagnostico: ")
    val evidencia = gestor.crear(paciente, fecha, diagnostico)
    println("Evidencia registrada: " + evidencia.resumen())
}

fun adjuntarFotoEvidencia(gestor: GestorEvidenciaClinica) {
    listarEvidenciasClinicas(gestor)
    val id = Validador.leerEntero("\nID de la evidencia: ", 1)
    val nombreArchivo = Validador.leerTextoNoVacio("Nombre del archivo de la foto (ej. herida1.jpg): ")
    println(if (gestor.adjuntarFoto(id, nombreArchivo)) "Foto adjuntada." else "No se encontro esa evidencia.")
}

fun listarEvidenciasClinicas(gestor: GestorEvidenciaClinica) {
    println("\n--- Evidencias clinicas registradas ---")
    val lista = gestor.listar()
    if (lista.isEmpty()) {
        println("  (No hay evidencias registradas todavia)")
        return
    }
    lista.forEach { println("  " + it.resumen()) }
}

// ---------------------------------------------------------------------
// Modulo de Seguimiento Post-tratamiento
// ---------------------------------------------------------------------

fun registrarSeguimiento(gestor: GestorSeguimiento) {
    println("\n--- Registrar seguimiento post-tratamiento ---")
    val paciente = Validador.leerTextoNoVacio("Nombre del paciente: ")
    val procedimiento = Validador.leerTextoNoVacio("Procedimiento realizado: ")
    val fecha = Validador.leerFecha("Fecha del procedimiento")
    val dias = Validador.leerEntero("Dias hasta el control de seguimiento: ", 1, 365).toLong()
    val seguimiento = gestor.crear(paciente, procedimiento, fecha, dias)
    println("Seguimiento programado: " + seguimiento.resumen())
}

fun completarSeguimiento(gestor: GestorSeguimiento) {
    listarSeguimientosPendientes(gestor)
    val id = Validador.leerEntero("\nID del seguimiento a marcar como completado: ", 1)
    println(if (gestor.marcarCompletado(id)) "Seguimiento marcado como completado." else "No se encontro ese seguimiento.")
}

fun listarSeguimientosPendientes(gestor: GestorSeguimiento) {
    println("\n--- Seguimientos pendientes ---")
    val lista = gestor.pendientes()
    if (lista.isEmpty()) {
        println("  (No hay seguimientos pendientes)")
        return
    }
    lista.forEach { println("  " + it.resumen()) }
}

fun listarProximosAVencer(gestor: GestorSeguimiento) {
    println("\n--- Seguimientos proximos a vencer (7 dias) ---")
    val lista = gestor.proximosAVencer(7)
    if (lista.isEmpty()) {
        println("  (No hay seguimientos proximos a vencer en los proximos 7 dias)")
        return
    }
    lista.forEach { println("  " + it.resumen()) }
}

