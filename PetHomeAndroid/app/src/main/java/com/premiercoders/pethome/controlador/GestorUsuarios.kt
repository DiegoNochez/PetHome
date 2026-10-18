package com.premiercoders.pethome.controlador

import com.premiercoders.pethome.modelo.Administrador
import com.premiercoders.pethome.modelo.Recepcionista
import com.premiercoders.pethome.modelo.Rol
import com.premiercoders.pethome.modelo.Usuario
import com.premiercoders.pethome.util.DatosInvalidosException
import com.premiercoders.pethome.util.LoggerErrores

/**
 * Inicio de sesión y registro locales (Foro 1). Arranca con una cuenta de
 * prueba por rol. En el Foro 2 esta clase se reemplaza por Firebase Auth sin
 * tocar las pantallas, porque la vista solo conoce al ViewModel.
 */
class GestorUsuarios {
    private val usuarios = mutableListOf<Usuario>(
        Administrador("Dr. Luis Cruz", "admin@pethome.com", "admin123"),
        Recepcionista("Recepción Pet Home", "recepcion@pethome.com", "recep123")
    )

    fun iniciarSesion(correo: String, contrasena: String): Usuario {
        val usuario = usuarios.find { it.correo.equals(correo.trim(), ignoreCase = true) }
        if (usuario == null) {
            LoggerErrores.registrar("Login fallido: correo no registrado ($correo)")
            throw DatosInvalidosException("No existe una cuenta con ese correo")
        }
        if (!usuario.verificarContrasena(contrasena)) {
            LoggerErrores.registrar("Login fallido: contraseña incorrecta ($correo)")
            throw DatosInvalidosException("Contraseña incorrecta")
        }
        return usuario
    }

    fun registrar(nombre: String, correo: String, contrasena: String, rol: Rol): Usuario {
        val correoLimpio = correo.trim()
        if (usuarios.any { it.correo.equals(correoLimpio, ignoreCase = true) }) {
            LoggerErrores.registrar("Registro rechazado: correo duplicado ($correoLimpio)")
            throw DatosInvalidosException("Ya existe una cuenta con ese correo")
        }
        val nuevo = when (rol) {
            Rol.ADMINISTRADOR -> Administrador(nombre.trim(), correoLimpio, contrasena)
            Rol.RECEPCIONISTA -> Recepcionista(nombre.trim(), correoLimpio, contrasena)
        }
        usuarios.add(nuevo)
        return nuevo
    }
}
