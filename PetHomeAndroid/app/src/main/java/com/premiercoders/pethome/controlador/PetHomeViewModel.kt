package com.premiercoders.pethome.controlador

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.premiercoders.pethome.modelo.CierreCaja
import com.premiercoders.pethome.modelo.CreditoFiscal
import com.premiercoders.pethome.modelo.EvidenciaClinica
import com.premiercoders.pethome.modelo.ResumenFinanciero
import com.premiercoders.pethome.modelo.Rol
import com.premiercoders.pethome.modelo.Seguimiento
import com.premiercoders.pethome.modelo.Usuario
import com.premiercoders.pethome.util.DatosInvalidosException
import java.time.LocalDate

/**
 * Puente entre los gestores (controlador de la Etapa 2) y las pantallas de
 * Compose (vista). Guarda el estado que las pantallas observan: cuando una
 * lista cambia, Compose vuelve a dibujar solo lo que depende de ella.
 *
 * Al ser un ViewModel, los datos sobreviven a la rotación de pantalla.
 * Las funciones que pueden fallar devuelven el mensaje de error o `null`.
 */
class PetHomeViewModel : ViewModel() {

    private val gestorUsuarios = GestorUsuarios()
    private val gestorCaja = GestorCierreCaja()
    private val gestorCredito = GestorCreditoFiscal()
    private val gestorEvidencia = GestorEvidenciaClinica()
    private val gestorSeguimiento = GestorSeguimiento()
    private val gestorReportes = GestorReportes(gestorCaja, gestorCredito, gestorSeguimiento)

    var usuarioActual by mutableStateOf<Usuario?>(null)
        private set
    var cierres by mutableStateOf<List<CierreCaja>>(emptyList())
        private set
    var creditos by mutableStateOf<List<CreditoFiscal>>(emptyList())
        private set
    var siguienteNumeroCredito by mutableStateOf("")
        private set
    var evidencias by mutableStateOf<List<EvidenciaClinica>>(emptyList())
        private set
    var seguimientos by mutableStateOf<List<Seguimiento>>(emptyList())
        private set
    var resumen by mutableStateOf(gestorReportes.generarResumen())
        private set

    init {
        cargarDatosDemo()
        refrescar()
    }

    private fun refrescar() {
        cierres = gestorCaja.listar()
        creditos = gestorCredito.listar()
        siguienteNumeroCredito = gestorCredito.siguienteNumero()
        evidencias = gestorEvidencia.listar()
        seguimientos = gestorSeguimiento.listar()
        resumen = gestorReportes.generarResumen()
    }

    // ---------------- Sesión ----------------

    fun iniciarSesion(correo: String, contrasena: String): String? = try {
        usuarioActual = gestorUsuarios.iniciarSesion(correo, contrasena)
        null
    } catch (e: DatosInvalidosException) {
        e.message
    }

    fun registrar(nombre: String, correo: String, contrasena: String, rol: Rol): String? = try {
        usuarioActual = gestorUsuarios.registrar(nombre, correo, contrasena, rol)
        null
    } catch (e: DatosInvalidosException) {
        e.message
    }

    fun cerrarSesion() {
        usuarioActual = null
    }

    // ---------------- Cierre de caja ----------------

    fun registrarCierre(fecha: LocalDate, efectivo: Double, tarjeta: Double, transferencia: Double, contado: Double): CierreCaja {
        val responsable = usuarioActual?.nombre ?: "Sin sesión"
        val cierre = gestorCaja.registrar(fecha, efectivo, tarjeta, transferencia, contado, responsable)
        refrescar()
        return cierre
    }

    fun actualizarConteo(id: Int, nuevoMonto: Double) {
        gestorCaja.actualizarConteo(id, nuevoMonto)
        refrescar()
    }

    fun eliminarCierre(id: Int) {
        gestorCaja.eliminar(id)
        refrescar()
    }

    // ---------------- Créditos fiscales ----------------

    fun generarCredito(fecha: LocalDate, cliente: String, servicio: String, subtotal: Double): Result<CreditoFiscal> = try {
        val credito = gestorCredito.generar(fecha, cliente, servicio, subtotal)
        refrescar()
        Result.success(credito)
    } catch (e: DatosInvalidosException) {
        Result.failure(e)
    }

    fun anularCredito(numero: String) {
        gestorCredito.anular(numero)
        refrescar()
    }

    // ---------------- Evidencia clínica ----------------

    fun crearEvidencia(paciente: String, fecha: LocalDate, diagnostico: String): EvidenciaClinica {
        val evidencia = gestorEvidencia.crear(paciente, fecha, diagnostico)
        refrescar()
        return evidencia
    }

    fun adjuntarFoto(id: Int, uri: String) {
        gestorEvidencia.adjuntarFoto(id, uri)
        refrescar()
    }

    fun eliminarEvidencia(id: Int) {
        gestorEvidencia.eliminar(id)
        refrescar()
    }

    // ---------------- Seguimiento ----------------

    fun crearSeguimiento(paciente: String, procedimiento: String, fecha: LocalDate, dias: Long): Seguimiento {
        val seguimiento = gestorSeguimiento.crear(paciente, procedimiento, fecha, dias)
        refrescar()
        return seguimiento
    }

    fun completarSeguimiento(id: Int) {
        gestorSeguimiento.marcarCompletado(id)
        refrescar()
    }

    fun eliminarSeguimiento(id: Int) {
        gestorSeguimiento.eliminar(id)
        refrescar()
    }

    fun proximosAVencer(dias: Int = 7): List<Seguimiento> = gestorSeguimiento.proximosAVencer(dias)

    /**
     * Datos de ejemplo (mascotas ficticias) para que las pantallas no estén
     * vacías en la demostración. En el Foro 2 los datos vendrán de Firestore.
     */
    private fun cargarDatosDemo() {
        val hoy = LocalDate.now()
        gestorCaja.registrar(hoy.minusDays(2), 245.50, 180.00, 75.00, 495.50, "Recepción Pet Home")
        gestorCaja.registrar(hoy.minusDays(1), 310.00, 220.75, 40.00, 570.75, "Recepción Pet Home")
        gestorCredito.generar(hoy.minusDays(2), "Agroservicios El Campo S.A. de C.V.", "Consulta y vacuna antirrábica", 35.00)
        gestorCredito.generar(hoy.minusDays(1), "Distribuidora La Ceiba", "Profilaxis dental", 80.00)
        gestorEvidencia.crear("Max (canino)", hoy.minusDays(1), "Herida en miembro anterior izquierdo, en cicatrización")
        gestorSeguimiento.crear("Luna (felino)", "Profilaxis dental", hoy.minusDays(10), 7)
        gestorSeguimiento.crear("Rocky (canino)", "Castración", hoy.minusDays(5), 10)
        val vacuna = gestorSeguimiento.crear("Toby (canino)", "Vacuna polivalente", hoy.minusDays(30), 21)
        gestorSeguimiento.marcarCompletado(vacuna.id)
    }
}
