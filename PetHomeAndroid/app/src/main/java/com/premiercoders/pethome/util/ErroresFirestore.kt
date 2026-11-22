package com.premiercoders.pethome.util

import com.google.firebase.firestore.FirebaseFirestoreException

/**
 * Convierte los errores de Firestore en mensajes en español para la pantalla.
 * El detalle técnico se guarda en el log de errores.
 */
object ErroresFirestore {

    /**
     * [requiereConexion] es true en operaciones que usan transacción (el
     * correlativo del crédito fiscal): Firestore no las guarda sin internet
     * ni las reintenta después, así que no se puede prometer que "se guardará".
     */
    fun mensaje(operacion: String, e: Exception, requiereConexion: Boolean = false): String {
        LoggerErrores.registrar("$operacion fallido: ${e::class.simpleName} - ${e.message}")
        val codigo = (e as? FirebaseFirestoreException)?.code
        return when (codigo) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> "Tu rol no tiene permiso para esta acción"
            FirebaseFirestoreException.Code.UNAVAILABLE ->
                if (requiereConexion) "Sin conexión. No se pudo $operacion: necesita internet. Inténtalo de nuevo"
                else "Sin conexión. Se guardará cuando vuelva el internet"
            FirebaseFirestoreException.Code.UNAUTHENTICATED -> "Tu sesión expiró. Vuelve a iniciar sesión"
            FirebaseFirestoreException.Code.ABORTED -> "Otro usuario guardó al mismo tiempo. Inténtalo de nuevo"
            else -> e.message?.takeIf { e is DatosInvalidosException } ?: "No se pudo $operacion. Inténtalo de nuevo"
        }
    }
}