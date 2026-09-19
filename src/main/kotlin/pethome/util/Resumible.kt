package pethome.util

/**
 * Interfaz que deben implementar todas las entidades del dominio que pueden
 * mostrarse como una línea de resumen en la consola (Cierre de Caja,
 * Crédito Fiscal, Evidencia Clínica, Seguimiento, Usuario, etc.).
 *
 * Cumple el requisito de la Etapa 2 de aplicar interfaces en el diseño POO.
 */
interface Resumible {
    fun resumen(): String
}
