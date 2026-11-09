package com.premiercoders.pethome.controlador

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.premiercoders.pethome.modelo.CreditoFiscal
import com.premiercoders.pethome.util.DatosInvalidosException
import com.premiercoders.pethome.util.LoggerErrores
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.util.Locale

/**
 * Créditos fiscales en Firestore, colección `creditosFiscales` (Foro 2).
 *
 * El correlativo CCF-AAAA-#### se asigna con una transacción sobre el
 * documento `contadores/creditos-AAAA`: si dos personas emiten al mismo
 * tiempo, Firestore reintenta una de ellas y nunca se repite un número.
 * El número es además el ID del documento.
 */
class GestorCreditoFiscal(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    private val coleccion get() = db.collection(COLECCION)
    private var creditos: List<CreditoFiscal> = emptyList()
    private val anio get() = LocalDate.now().year

    fun escuchar(onCambio: () -> Unit, onError: (Exception) -> Unit): ListenerRegistration =
        coleccion.orderBy("numeroCorrelativo", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                creditos = snapshot?.documents?.mapNotNull { aCredito(it) }.orEmpty()
                onCambio()
            }

    fun limpiar() {
        creditos = emptyList()
    }

    /**
     * Número que probablemente recibirá el próximo crédito (vista previa).
     * El definitivo lo asigna la transacción al emitir.
     */
    fun siguienteNumero(): String {
        val prefijo = "CCF-$anio-"
        val ultimo = creditos.filter { it.numeroCorrelativo.startsWith(prefijo) }
            .maxOfOrNull { it.numeroCorrelativo.removePrefix(prefijo).toIntOrNull() ?: 0 } ?: 0
        return formato(ultimo + 1L)
    }

    /** Emite el crédito y devuelve el correlativo asignado. */
    suspend fun generar(
        fecha: LocalDate, cliente: String, nit: String, nrc: String,
        servicio: String, subtotal: Double, uid: String
    ): String {
        if (subtotal <= 0.0) {
            LoggerErrores.registrar("Crédito fiscal rechazado: subtotal $subtotal")
            throw DatosInvalidosException("El subtotal debe ser mayor a cero")
        }
        val contador = db.collection(COLECCION_CONTADORES).document("creditos-$anio")
        return db.runTransaction { tx ->
            val siguiente = tx.get(contador).getLong("siguiente") ?: 1L
            val numero = formato(siguiente)
            tx.set(
                coleccion.document(numero),
                mapOf(
                    "numeroCorrelativo" to numero,
                    "fecha" to fecha.toString(),
                    "cliente" to cliente.trim(),
                    "nit" to nit.trim(),
                    "nrc" to nrc.trim(),
                    "servicio" to servicio.trim(),
                    "subtotal" to subtotal,
                    "anulado" to false,
                    "creadoPor" to uid,
                    "creadoEn" to FieldValue.serverTimestamp()
                )
            )
            tx.set(contador, mapOf("siguiente" to siguiente + 1))
            numero
        }.await()
    }

    fun listar(): List<CreditoFiscal> = creditos

    fun buscarPorNumero(numero: String): CreditoFiscal? = creditos.find { it.numeroCorrelativo == numero }

    /** No se borra: un comprobante fiscal emitido se anula y conserva su número. */
    suspend fun anular(numero: String, uid: String) {
        coleccion.document(numero).update(
            mapOf("anulado" to true, "anuladoPor" to uid, "anuladoEn" to FieldValue.serverTimestamp())
        ).await()
    }

    fun listarAnulados(): List<CreditoFiscal> = creditos.filter { it.anulado }

    fun listarVigentes(): List<CreditoFiscal> = creditos.filter { !it.anulado }

    fun totalEmitidos(): Int = creditos.count { !it.anulado }

    fun totalAnulados(): Int = creditos.count { it.anulado }

    fun ingresoTotal(): Double = creditos.filter { !it.anulado }.sumOf { it.total }

    fun ivaTotal(): Double = creditos.filter { !it.anulado }.sumOf { it.iva }

    private fun formato(numero: Long): String = String.format(Locale.US, "CCF-%d-%04d", anio, numero)

    private fun aCredito(doc: DocumentSnapshot): CreditoFiscal? = try {
        CreditoFiscal(
            numeroCorrelativo = doc.getString("numeroCorrelativo") ?: doc.id,
            fecha = LocalDate.parse(doc.getString("fecha")),
            cliente = doc.getString("cliente").orEmpty(),
            nit = doc.getString("nit").orEmpty(),
            nrc = doc.getString("nrc").orEmpty(),
            servicio = doc.getString("servicio").orEmpty(),
            subtotal = doc.getDouble("subtotal") ?: 0.0,
            anulado = doc.getBoolean("anulado") ?: false,
            creadoPor = doc.getString("creadoPor").orEmpty()
        )
    } catch (e: Exception) {
        LoggerErrores.registrar("Crédito ${doc.id} ignorado: ${e.message}")
        null
    }

    companion object {
        const val COLECCION = "creditosFiscales"
        const val COLECCION_CONTADORES = "contadores"
    }
}
