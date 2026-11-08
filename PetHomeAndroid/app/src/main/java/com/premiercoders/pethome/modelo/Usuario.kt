package com.premiercoders.pethome.modelo

import com.premiercoders.pethome.util.Resumible

/**
 * Clase base abstracta de los dos roles de Pet Home (misma jerarquía de la
 * Etapa 2). El método polimórfico [puedeAcceder] decide qué módulos ve cada
 * rol en el panel.
 *
 * Foro 2: la contraseña ya no se guarda en la app. Firebase Authentication
 * verifica la identidad y entrega el [uid]; el nombre y el rol se leen del
 * documento `usuarios/{uid}` de Firestore.
 */
abstract class Usuario(
    val uid: String,
    val nombre: String,
    val correo: String
) : Resumible {
    abstract val rol: Rol

    abstract fun puedeAcceder(modulo: Modulo): Boolean

    fun modulosPermitidos(): List<Modulo> = Modulo.entries.filter { puedeAcceder(it) }

    override fun resumen(): String = "$nombre - ${rol.descripcion} ($correo)"

    companion object {
        /** Crea la subclase que corresponde al rol guardado en Firestore. */
        fun crear(uid: String, nombre: String, correo: String, rol: Rol): Usuario = when (rol) {
            Rol.ADMINISTRADOR -> Administrador(uid, nombre, correo)
            Rol.RECEPCIONISTA -> Recepcionista(uid, nombre, correo)
        }
    }
}

/** Ve los 5 módulos, incluidos los clínicos y los reportes. */
class Administrador(uid: String, nombre: String, correo: String) : Usuario(uid, nombre, correo) {
    override val rol: Rol = Rol.ADMINISTRADOR
    override fun puedeAcceder(modulo: Modulo): Boolean = true
}

/** Solo trabaja con caja y créditos fiscales, igual que en la Etapa 2. */
class Recepcionista(uid: String, nombre: String, correo: String) : Usuario(uid, nombre, correo) {
    override val rol: Rol = Rol.RECEPCIONISTA
    override fun puedeAcceder(modulo: Modulo): Boolean =
        modulo == Modulo.CIERRE_CAJA || modulo == Modulo.CREDITOS_FISCALES
}
