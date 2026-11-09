package com.premiercoders.pethome.util

import com.google.firebase.firestore.FirebaseFirestoreException

/**
 * Convierte los errores de Firestore en mensajes en español para la pantalla.
 * El detalle técnico se guarda en el log de errores.
 */
object ErroresFirestore {

    fun mensaje(operacion: String, e: Exception): String {
        LoggerErrores.registrar("$operacion fallido: ${e::class.simpleName} - ${e.message}")
        val codigo = (e as? FirebaseFirestoreException)?.code
        return when (codigo) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> "Tu rol no tiene permiso para esta acción"
            FirebaseFirestoreException.Code.UNAVAILABLE -> "Sin conexión. Se guardará cuando vuelva el internet"
            FirebaseFirestoreException.Code.UNAUTHENTICATED -> "Tu sesión expiró. Vuelve a iniciar sesión"
            FirebaseFirestoreException.Code.ABORTED -> "Otro usuario guardó al mismo tiempo. Inténtalo de nuevo"
            else -> e.message?.takeIf { e is DatosInvalidosException } ?: "No se pudo $operacion. Inténtalo de nuevo"
        }
    }
}
