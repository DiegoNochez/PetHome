package com.premiercoders.pethome.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Formatos de presentación compartidos por todas las pantallas. */
object Formato {
    private val formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun dinero(valor: Double): String = String.format(Locale.US, "$%,.2f", valor)

    fun fecha(fecha: LocalDate): String = fecha.format(formatoFecha)
}
