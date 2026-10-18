package com.premiercoders.pethome.util

import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * Validación de formularios. En la Etapa 2 cada función leía de la consola y
 * reintentaba; en Compose el usuario corrige el campo en pantalla, así que
 * cada función devuelve el mensaje de error (o `null` si el valor es válido)
 * y la vista lo muestra debajo del campo.
 */
object Validador {

    private val regexCorreo = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun textoObligatorio(valor: String, campo: String, minimo: Int = 2): String? = when {
        valor.isBlank() -> "$campo es obligatorio"
        valor.trim().length < minimo -> "$campo debe tener al menos $minimo caracteres"
        else -> null
    }

    fun correo(valor: String): String? = when {
        valor.isBlank() -> "El correo es obligatorio"
        !regexCorreo.matches(valor.trim()) -> "Correo inválido (ej. nombre@pethome.com)"
        else -> null
    }

    fun contrasena(valor: String): String? = when {
        valor.isBlank() -> "La contraseña es obligatoria"
        valor.length < 6 -> "Debe tener al menos 6 caracteres"
        valor.none { it.isLetter() } || valor.none { it.isDigit() } -> "Debe incluir letras y números"
        else -> null
    }

    fun confirmacion(contrasena: String, confirmacion: String): String? = when {
        confirmacion.isBlank() -> "Confirma la contraseña"
        confirmacion != contrasena -> "Las contraseñas no coinciden"
        else -> null
    }

    /** Acepta punto o coma decimal ("25.50" o "25,50"). */
    fun aNumero(valor: String): Double? = valor.trim().replace(',', '.').toDoubleOrNull()

    fun monto(valor: String, campo: String, permitirCero: Boolean = true): String? {
        if (valor.isBlank()) return "$campo es obligatorio"
        val numero = aNumero(valor) ?: return "Ingresa un número válido (ej. 25.50)"
        val decimales = valor.trim().replace(',', '.').substringAfter('.', "")
        return when {
            numero < 0 -> "$campo no puede ser negativo"
            !permitirCero && numero == 0.0 -> "$campo debe ser mayor a 0"
            decimales.length > 2 -> "Usa máximo 2 decimales"
            numero > 1_000_000 -> "El monto es demasiado alto"
            else -> null
        }
    }

    fun entero(valor: String, campo: String, minimo: Int, maximo: Int): String? {
        if (valor.isBlank()) return "$campo es obligatorio"
        val numero = valor.trim().toIntOrNull() ?: return "Ingresa un número entero"
        return if (numero < minimo || numero > maximo) "Debe estar entre $minimo y $maximo" else null
    }

    fun aFecha(valor: String): LocalDate? = try {
        LocalDate.parse(valor.trim())
    } catch (e: DateTimeParseException) {
        null
    }

    fun fecha(valor: String, permitirFutura: Boolean = false): String? {
        if (valor.isBlank()) return "La fecha es obligatoria"
        val fecha = aFecha(valor) ?: return "Formato inválido, usa AAAA-MM-DD"
        return if (!permitirFutura && fecha.isAfter(LocalDate.now())) "La fecha no puede ser futura" else null
    }
}
