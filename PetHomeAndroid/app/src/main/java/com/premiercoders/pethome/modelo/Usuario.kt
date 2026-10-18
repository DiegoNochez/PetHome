package com.premiercoders.pethome.modelo

import com.premiercoders.pethome.util.Resumible

/**
 * Clase base abstracta de los dos roles de Pet Home (misma jerarquía de la
 * Etapa 2). En el Foro 1 se agrega la contraseña para el login local y el
 * método polimórfico [puedeAcceder], que la vista usa para mostrar solo los
 * módulos permitidos a cada rol.
 *
 * Nota: la contraseña se guarda en memoria solo para esta entrega; en el
 * Foro 2 la autenticación pasa a Firebase Authentication.
 */
abstract class Usuario(
    val nombre: String,
    val correo: String,
    private val contrasena: String
) : Resumible {
    abstract val rol: Rol

    abstract fun puedeAcceder(modulo: Modulo): Boolean

    fun verificarContrasena(intento: String): Boolean = intento == contrasena

    fun modulosPermitidos(): List<Modulo> = Modulo.entries.filter { puedeAcceder(it) }

    override fun resumen(): String = "$nombre - ${rol.descripcion} ($correo)"
}

/** Ve los 5 módulos, incluidos los clínicos y los reportes. */
class Administrador(nombre: String, correo: String, contrasena: String) :
    Usuario(nombre, correo, contrasena) {
    override val rol: Rol = Rol.ADMINISTRADOR
    override fun puedeAcceder(modulo: Modulo): Boolean = true
}

/** Solo trabaja con caja y créditos fiscales, igual que en la Etapa 2. */
class Recepcionista(nombre: String, correo: String, contrasena: String) :
    Usuario(nombre, correo, contrasena) {
    override val rol: Rol = Rol.RECEPCIONISTA
    override fun puedeAcceder(modulo: Modulo): Boolean =
        modulo == Modulo.CIERRE_CAJA || modulo == Modulo.CREDITOS_FISCALES
}
