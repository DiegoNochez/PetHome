package pethome.util

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Log de errores en archivo de texto, requerido por la Etapa 2.
 * Es un `object` (singleton de Kotlin) porque solo debe existir una
 * instancia del logger durante toda la ejecución del programa.
 */
object LoggerErrores {
    private val archivo = File("errores.log")
    private val formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun registrar(mensaje: String) {
        val marca = LocalDateTime.now().format(formato)
        archivo.appendText("[$marca] $mensaje\n")
    }
}
