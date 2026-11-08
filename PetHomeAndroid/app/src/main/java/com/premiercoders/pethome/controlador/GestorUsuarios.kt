package com.premiercoders.pethome.controlador

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.premiercoders.pethome.modelo.Rol
import com.premiercoders.pethome.modelo.Usuario
import com.premiercoders.pethome.util.DatosInvalidosException
import com.premiercoders.pethome.util.LoggerErrores
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

/**
 * Autenticación del personal con Firebase (Foro 2). Reemplaza la lista local
 * del Foro 1 sin cambiar el contrato con el ViewModel.
 *
 * - Firebase Authentication verifica la identidad (correo/contraseña o Google).
 * - El perfil y el rol viven en Firestore, en `usuarios/{uid}`.
 * - Toda cuenta nueva nace como RECEPCIONISTA. Solo un administrador puede
 *   cambiar el rol desde la consola de Firebase; las reglas de seguridad
 *   impiden que la app lo modifique (ver firestore.rules).
 */
class GestorUsuarios(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val usuarios get() = db.collection(COLECCION)

    /**
     * Si Firebase recuerda una sesión abierta (la app se cerró sin cerrar
     * sesión), devuelve su perfil para entrar directo al panel.
     */
    suspend fun restaurarSesion(): Usuario? {
        val actual = auth.currentUser ?: return null
        return try {
            cargarPerfil(actual, actual.displayName)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Sin red o sin perfil: se muestra el login en lugar de fallar.
            LoggerErrores.registrar("Restaurar sesión fallido: ${e::class.simpleName} - ${e.message}")
            null
        }
    }

    suspend fun iniciarSesion(correo: String, contrasena: String): Usuario = ejecutar("Login con correo") {
        val resultado = auth.signInWithEmailAndPassword(correo.trim(), contrasena).await()
        cargarPerfil(resultado.user ?: error("Firebase no devolvió el usuario"), null)
    }

    suspend fun registrar(nombre: String, correo: String, contrasena: String): Usuario = ejecutar("Registro") {
        val resultado = auth.createUserWithEmailAndPassword(correo.trim(), contrasena).await()
        val usuario = resultado.user ?: error("Firebase no devolvió el usuario")
        val perfil = UserProfileChangeRequest.Builder().setDisplayName(nombre.trim()).build()
        usuario.updateProfile(perfil).await()
        crearPerfil(usuario, nombre.trim())
    }

    suspend fun iniciarSesionConGoogle(idToken: String): Usuario = ejecutar("Login con Google") {
        val credencial = GoogleAuthProvider.getCredential(idToken, null)
        val resultado = auth.signInWithCredential(credencial).await()
        val usuario = resultado.user ?: error("Firebase no devolvió el usuario")
        cargarPerfil(usuario, usuario.displayName)
    }

    fun cerrarSesion() = auth.signOut()

    /** Lee `usuarios/{uid}`; si no existe (primer ingreso con Google), lo crea. */
    private suspend fun cargarPerfil(usuario: FirebaseUser, nombrePorDefecto: String?): Usuario {
        val documento = usuarios.document(usuario.uid).get().await()
        if (!documento.exists()) {
            return crearPerfil(usuario, nombrePorDefecto ?: usuario.email ?: "Personal Pet Home")
        }
        val rol = Rol.entries.find { it.name == documento.getString("rol") } ?: Rol.RECEPCIONISTA
        return Usuario.crear(
            uid = usuario.uid,
            nombre = documento.getString("nombre") ?: nombrePorDefecto ?: "Personal Pet Home",
            correo = documento.getString("correo") ?: usuario.email.orEmpty(),
            rol = rol
        )
    }

    private suspend fun crearPerfil(usuario: FirebaseUser, nombre: String): Usuario {
        val correo = usuario.email.orEmpty()
        val datos = mapOf(
            "nombre" to nombre,
            "correo" to correo,
            "rol" to Rol.RECEPCIONISTA.name,
            "creadoEn" to FieldValue.serverTimestamp()
        )
        usuarios.document(usuario.uid).set(datos).await()
        return Usuario.crear(usuario.uid, nombre, correo, Rol.RECEPCIONISTA)
    }

    /**
     * Ejecuta una operación de Firebase y convierte cualquier error en un
     * mensaje claro en español. El detalle técnico va al log de errores.
     * Si algo falla a medias, cierra la sesión para no dejar un estado roto.
     */
    private suspend fun <T> ejecutar(operacion: String, bloque: suspend () -> T): T = try {
        bloque()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        LoggerErrores.registrar("$operacion fallido: ${e::class.simpleName} - ${e.message}")
        auth.signOut()
        throw DatosInvalidosException(traducir(e))
    }

    private fun traducir(e: Exception): String = when (e) {
        is FirebaseAuthWeakPasswordException -> "La contraseña es muy débil (mínimo 6 caracteres)"
        is FirebaseAuthUserCollisionException -> "Ya existe una cuenta con ese correo"
        is FirebaseAuthInvalidUserException -> "La cuenta no existe o fue deshabilitada"
        is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrectos"
        is FirebaseTooManyRequestsException -> "Demasiados intentos. Espera unos minutos"
        is FirebaseNetworkException -> "Sin conexión a internet. Revisa tu red e inténtalo de nuevo"
        is FirebaseFirestoreException ->
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) "No tienes permiso para acceder a tu perfil"
            else "No se pudo leer tu perfil. Inténtalo de nuevo"
        else -> "No se pudo completar la operación. Inténtalo de nuevo"
    }

    companion object {
        const val COLECCION = "usuarios"
    }
}
