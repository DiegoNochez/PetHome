package pethome.vista

import pethome.modelo.Administrador
import pethome.modelo.Recepcionista
import pethome.modelo.Usuario
import pethome.util.Validador

/**
 * Capa de Vista: solo se encarga de mostrar información y capturar la
 * selección de rol de inicio de sesión. Toda la lógica de negocio vive en
 * el paquete `controlador`.
 */
object Consola {

    fun tituloMenu(texto: String): String = "\n===== $texto =====\n"

    fun iniciarSesion(): Usuario {
        println("\n--- Inicio de sesion ---")
        println("1. Administrador / Veterinario (MVZ)")
        println("2. Recepcionista / Cajero")
        val rol = Validador.leerEntero("Selecciona tu rol: ", 1, 2)
        val nombre = Validador.leerTextoNoVacio("Nombre de usuario: ")
        val correo = Validador.leerCorreo("Correo electronico: ")
        return if (rol == 1) Administrador(nombre, correo) else Recepcionista(nombre, correo)
    }
}
