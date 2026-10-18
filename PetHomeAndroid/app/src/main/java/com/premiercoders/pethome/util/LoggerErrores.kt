package com.premiercoders.pethome.util

import android.content.Context
import android.util.Log
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Log de errores heredado de la Etapa 2. En Android no se puede escribir en
 * la carpeta actual como en consola, así que el archivo `errores.log` se crea
 * en la memoria interna de la app (`filesDir`) y además cada error se envía
 * al Logcat de Android Studio con la etiqueta "PetHome".
 */
object LoggerErrores {
    private const val TAG = "PetHome"
    private val formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private var archivo: File? = null

    /** Se llama una vez desde MainActivity. */
    fun inicializar(context: Context) {
        archivo = File(context.filesDir, "errores.log")
    }

    fun registrar(mensaje: String) {
        Log.w(TAG, mensaje)
        val marca = LocalDateTime.now().format(formato)
        try {
            archivo?.appendText("[$marca] $mensaje\n")
        } catch (e: IOException) {
            Log.e(TAG, "No se pudo escribir en errores.log", e)
        }
    }
}
