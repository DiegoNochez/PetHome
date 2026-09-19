package pethome.modelo

import pethome.util.Resumible

/**
 * Clase base abstracta de los dos roles reales de Pet Home (ver Etapa 1).
 * Demuestra herencia: [Administrador] y [Recepcionista] extienden esta clase
 * y solo difieren en su descripción de rol y, en Main.kt, en las opciones de
 * menú a las que tienen acceso.
 */
abstract class Usuario(val nombre: String, val correo: String) : Resumible {
    abstract val rolDescripcion: String

    override fun resumen(): String = "$nombre - $rolDescripcion ($correo)"
}

class Administrador(nombre: String, correo: String) : Usuario(nombre, correo) {
    override val rolDescripcion: String = "Administrador / Veterinario (MVZ)"
}

class Recepcionista(nombre: String, correo: String) : Usuario(nombre, correo) {
    override val rolDescripcion: String = "Recepcionista / Cajero"
}
