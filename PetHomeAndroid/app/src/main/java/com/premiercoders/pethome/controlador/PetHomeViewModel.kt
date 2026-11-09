package com.premiercoders.pethome.controlador

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import com.premiercoders.pethome.modelo.CierreCaja
import com.premiercoders.pethome.modelo.CreditoFiscal
import android.content.ContentResolver
import android.net.Uri
import com.premiercoders.pethome.modelo.EvidenciaClinica
import com.premiercoders.pethome.modelo.Modulo
import com.premiercoders.pethome.modelo.ResumenFinanciero
import com.premiercoders.pethome.modelo.Seguimiento
import com.premiercoders.pethome.modelo.Usuario
import com.premiercoders.pethome.util.DatosInvalidosException
import com.premiercoders.pethome.util.ErroresFirestore
import com.premiercoders.pethome.util.Formato
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Puente entre los gestores (controlador de la Etapa 2) y las pantallas de
 * Compose (vista). Guarda el estado que las pantallas observan: cuando una
 * lista cambia, Compose vuelve a dibujar solo lo que depende de ella.
 *
 * Al ser un ViewModel, los datos sobreviven a la rotación de pantalla.
 *
 * Foro 2: al iniciar sesión se abren listeners de Firestore para caja y
 * créditos; cada cambio en la nube actualiza las listas y la UI se redibuja
 * sola. Al cerrar sesión los listeners se cierran.
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

    /** `true` mientras se revisa si Firebase recuerda una sesión abierta. */
    var verificandoSesion by mutableStateOf(true)
        private set

    /** `true` mientras se espera la respuesta de Firebase (deshabilita botones). */
    var procesandoAuth by mutableStateOf(false)
        private set

    /** Último error de login/registro, ya traducido al español. */
    var errorAuth by mutableStateOf<String?>(null)
        private set
    /** `true` mientras se guarda algo en Firestore (deshabilita botones). */
    var guardando by mutableStateOf(false)
        private set

    /** Error de sincronización con Firestore (sin permiso, sin red...). */
    var errorSincronizacion by mutableStateOf<String?>(null)
        private set

    private val escuchas = mutableListOf<ListenerRegistration>()

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
        refrescar()
        viewModelScope.launch {
            establecerUsuario(gestorUsuarios.restaurarSesion())
            verificandoSesion = false
        }
    }

    /** Guarda el usuario y abre (o cierra) la sincronización con Firestore. */
    private fun establecerUsuario(usuario: Usuario?) {
        usuarioActual = usuario
        detenerSincronizacion()
        if (usuario != null) iniciarSincronizacion()
    }

    private fun iniciarSincronizacion() {
        errorSincronizacion = null
        val alCambiar = { refrescar() }
        val alFallar = { e: Exception -> errorSincronizacion = ErroresFirestore.mensaje("sincronizar los datos", e) }
        escuchas += gestorCaja.escuchar(alCambiar, alFallar)
        escuchas += gestorCredito.escuchar(alCambiar, alFallar)
        // Los módulos clínicos solo los lee el administrador (las reglas lo exigen).
        if (usuarioActual?.puedeAcceder(Modulo.EVIDENCIA_CLINICA) == true) {
            escuchas += gestorEvidencia.escuchar(alCambiar, alFallar)
            escuchas += gestorSeguimiento.escuchar(alCambiar, alFallar)
        }
    }

    private fun detenerSincronizacion() {
        escuchas.forEach { it.remove() }
        escuchas.clear()
        gestorCaja.limpiar()
        gestorCredito.limpiar()
        gestorEvidencia.limpiar()
        gestorSeguimiento.limpiar()
        refrescar()
    }

    override fun onCleared() {
        escuchas.forEach { it.remove() }
        super.onCleared()
    }

    /**
     * Ejecuta una escritura en Firestore en segundo plano. [bloque] devuelve
     * el mensaje de éxito; si falla, se muestra el error traducido.
     */
    private fun guardar(
        operacion: String,
        onResultado: (String) -> Unit,
        onExito: () -> Unit = {},
        bloque: suspend (Usuario) -> String
    ) {
        val usuario = usuarioActual ?: return onResultado("Tu sesión expiró. Vuelve a iniciar sesión")
        if (guardando) return
        guardando = true
        viewModelScope.launch {
            try {
                val mensaje = bloque(usuario)
                onExito()
                onResultado(mensaje)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                onResultado(ErroresFirestore.mensaje(operacion, e))
            } finally {
                guardando = false
            }
        }
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

    fun iniciarSesion(correo: String, contrasena: String) =
        autenticar { gestorUsuarios.iniciarSesion(correo, contrasena) }

    fun registrar(nombre: String, correo: String, contrasena: String) =
        autenticar { gestorUsuarios.registrar(nombre, correo, contrasena) }

    fun iniciarSesionConGoogle(google: AutenticacionGoogle) =
        autenticar { gestorUsuarios.iniciarSesionConGoogle(google.obtenerIdToken()) }

    fun limpiarErrorAuth() {
        errorAuth = null
    }

    /**
     * Cierra la sesión en Firebase y olvida la cuenta de Google elegida, así
     * nadie puede volver a entrar con el botón Atrás ni reutilizar la sesión.
     */
    fun cerrarSesion(google: AutenticacionGoogle? = null) {
        gestorUsuarios.cerrarSesion()
        establecerUsuario(null)
        errorAuth = null
        if (google != null) viewModelScope.launch { google.limpiar() }
    }

    /** Ejecuta una operación de Firebase en segundo plano sin congelar la UI. */
    private fun autenticar(operacion: suspend () -> Usuario) {
        if (procesandoAuth) return
        errorAuth = null
        procesandoAuth = true
        viewModelScope.launch {
            try {
                establecerUsuario(operacion())
            } catch (e: DatosInvalidosException) {
                errorAuth = e.message
            } finally {
                procesandoAuth = false
            }
        }
    }

    // ---------------- Cierre de caja (Firestore) ----------------

    fun registrarCierre(
        fecha: LocalDate, efectivo: Double, tarjeta: Double, transferencia: Double, contado: Double,
        onExito: () -> Unit, onResultado: (String) -> Unit
    ) = guardar("registrar el cierre", onResultado, onExito) { usuario ->
        gestorCaja.registrar(fecha, efectivo, tarjeta, transferencia, contado, usuario.nombre, usuario.uid)
        "Cierre del ${Formato.fecha(fecha)} guardado"
    }

    fun actualizarConteo(id: String, nuevoMonto: Double, onResultado: (String) -> Unit) =
        guardar("actualizar el conteo", onResultado) {
            gestorCaja.actualizarConteo(id, nuevoMonto)
            "Conteo actualizado"
        }

    fun eliminarCierre(id: String, onResultado: (String) -> Unit) =
        guardar("eliminar el cierre", onResultado) {
            gestorCaja.eliminar(id)
            "Cierre eliminado"
        }

    // ---------------- Créditos fiscales (Firestore) ----------------

    fun generarCredito(
        fecha: LocalDate, cliente: String, nit: String, nrc: String, servicio: String, subtotal: Double,
        onExito: () -> Unit, onResultado: (String) -> Unit
    ) = guardar("emitir el crédito fiscal", onResultado, onExito) { usuario ->
        val numero = gestorCredito.generar(fecha, cliente, nit, nrc, servicio, subtotal, usuario.uid)
        "$numero emitido"
    }

    fun anularCredito(numero: String, onResultado: (String) -> Unit) =
        guardar("anular el crédito", onResultado) { usuario ->
            gestorCredito.anular(numero, usuario.uid)
            "$numero anulado"
        }

    // ---------------- Evidencia clínica (Firestore + Cloudinary) ----------------

    /** `true` mientras se sube una foto a Cloudinary. */
    var subiendoFoto by mutableStateOf(false)
        private set

    fun crearEvidencia(paciente: String, fecha: LocalDate, diagnostico: String, onExito: () -> Unit, onResultado: (String) -> Unit) =
        guardar("crear la evidencia", onResultado, onExito) { usuario ->
            gestorEvidencia.crear(paciente, fecha, diagnostico, usuario.uid)
            "Registro de ${paciente.trim()} creado. Ya puedes adjuntarle fotos."
        }

    fun adjuntarFoto(id: String, resolver: ContentResolver, uri: Uri, onResultado: (String) -> Unit) {
        if (guardando) return
        subiendoFoto = true
        guardar("subir la foto", { mensaje -> subiendoFoto = false; onResultado(mensaje) }) {
            gestorEvidencia.adjuntarFoto(id, resolver, uri)
            "Foto subida a la nube y adjuntada"
        }
    }

    fun eliminarEvidencia(id: String, onResultado: (String) -> Unit) =
        guardar("eliminar la evidencia", onResultado) {
            gestorEvidencia.eliminar(id)
            "Evidencia eliminada"
        }

    // ---------------- Seguimiento (Firestore) ----------------

    fun crearSeguimiento(
        paciente: String, procedimiento: String, fecha: LocalDate, dias: Long,
        onExito: () -> Unit, onResultado: (String) -> Unit
    ) = guardar("programar el seguimiento", onResultado, onExito) { usuario ->
        gestorSeguimiento.crear(paciente, procedimiento, fecha, dias, usuario.uid)
        "Control de ${paciente.trim()} programado para el ${Formato.fecha(fecha.plusDays(dias))}"
    }

    fun completarSeguimiento(id: String, onResultado: (String) -> Unit) =
        guardar("completar el seguimiento", onResultado) {
            gestorSeguimiento.marcarCompletado(id)
            "Control marcado como completado"
        }

    fun eliminarSeguimiento(id: String, onResultado: (String) -> Unit) =
        guardar("eliminar el seguimiento", onResultado) {
            gestorSeguimiento.eliminar(id)
            "Seguimiento eliminado"
        }

    fun proximosAVencer(dias: Int = 7): List<Seguimiento> = gestorSeguimiento.proximosAVencer(dias)
}
