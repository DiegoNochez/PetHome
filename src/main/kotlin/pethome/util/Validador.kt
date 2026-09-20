package pethome.util

import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * Centraliza la validación de entradas de consola y el manejo de errores de
 * formato (Etapa 2: "Validación de entradas y manejo de errores/excepciones").
 * Cada función reintenta hasta obtener un valor válido y registra en el log
 * de errores cada intento fallido.
 */
object Validador {

    fun leerTextoNoVacio(prompt: String): String {
        while (true) {
            print(prompt)
            val texto = readLine()?.trim() ?: ""
            if (texto.isNotEmpty()) return texto
            println("  > El texto no puede estar vacio. Intenta de nuevo.")
        }
    }

    fun leerDoublePositivo(prompt: String): Double {
        while (true) {
            print(prompt)
            val entrada = readLine()?.trim() ?: ""
            try {
                val valor = entrada.toDouble()
                if (valor < 0) throw NumberFormatException("el monto no puede ser negativo")
                return valor
            } catch (e: NumberFormatException) {
                LoggerErrores.registrar("Entrada numerica invalida: '$entrada' -> ${e.message}")
                println("  > Ingresa un numero valido mayor o igual a 0 (ej. 25.50).")
            }
        }
    }

    fun leerEntero(prompt: String, minimo: Int = Int.MIN_VALUE, maximo: Int = Int.MAX_VALUE): Int {
        while (true) {
            print(prompt)
            val entrada = readLine()?.trim() ?: ""
            try {
                val valor = entrada.toInt()
                if (valor < minimo || valor > maximo) throw NumberFormatException("fuera del rango permitido")
                return valor
            } catch (e: NumberFormatException) {
                LoggerErrores.registrar("Entero invalido: '$entrada' -> ${e.message}")
                println("  > Ingresa un numero entero valido entre $minimo y $maximo.")
            }
        }
    }

    fun leerFecha(prompt: String): LocalDate {
        while (true) {
            print("$prompt (formato yyyy-MM-dd): ")
            val entrada = readLine()?.trim() ?: ""
            try {
                return LocalDate.parse(entrada)
            } catch (e: DateTimeParseException) {
                LoggerErrores.registrar("Fecha invalida: '$entrada' -> ${e.message}")
                println("  > Formato de fecha invalido. Usa yyyy-MM-dd, ej: 2026-09-19")
            }
        }
    }

    fun leerCorreo(prompt: String): String {
        while (true) {
            print(prompt)
            val correo = readLine()?.trim() ?: ""
            if (correo.contains("@") && correo.contains(".") && correo.indexOf("@") < correo.lastIndexOf(".")) {
                return correo
            }
            LoggerErrores.registrar("Correo invalido ingresado: '$correo'")
            println("  > Correo invalido. Debe contener '@' y un dominio valido (ej. nombre@dominio.com).")
        }
    }
}
