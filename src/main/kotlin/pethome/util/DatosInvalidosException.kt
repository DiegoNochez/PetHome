package pethome.util

/**
 * Excepción propia del dominio (hereda de Exception) para señalar datos de
 * negocio inválidos, distinta de los errores de formato de entrada que ya
 * maneja [Validador]. Ejemplo: un crédito fiscal con subtotal en cero.
 */
class DatosInvalidosException(mensaje: String) : Exception(mensaje)
