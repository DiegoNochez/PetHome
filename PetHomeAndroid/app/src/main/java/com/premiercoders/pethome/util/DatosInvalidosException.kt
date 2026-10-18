package com.premiercoders.pethome.util

/**
 * Excepción propia del dominio para datos de negocio inválidos (por ejemplo,
 * un correo ya registrado o un subtotal en cero). El mensaje se muestra tal
 * cual al usuario en la pantalla correspondiente.
 */
class DatosInvalidosException(mensaje: String) : Exception(mensaje)
