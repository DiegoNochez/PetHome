package com.premiercoders.pethome.controlador

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.premiercoders.pethome.modelo.CierreCaja
import com.premiercoders.pethome.modelo.IngresoDiario
import com.premiercoders.pethome.util.LoggerErrores
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

/**
 * Cierres de caja guardados en Firestore, colección `cierresCaja` (Foro 2).
 *
 * La lista local [cierres] es una copia que mantiene al día un listener en
 * tiempo real: si otra recepcionista registra un cierre en otro teléfono,
 * aparece aquí sin recargar. Los cálculos (faltantes, totales por método)
 * siguen igual que en la Etapa 2 y trabajan sobre esa copia.
 */
class GestorCierreCaja(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    private val coleccion get() = db.collection(COLECCION)
    private var cierres: List<CierreCaja> = emptyList()

    /** Empieza a escuchar la colección; [onCambio] se llama con cada actualización. */
    fun escuchar(onCambio: () -> Unit, onError: (Exception) -> Unit): ListenerRegistration =
        coleccion.orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                cierres = snapshot?.documents?.mapNotNull { aCierre(it) }.orEmpty()
                onCambio()
            }

    fun limpiar() {
        cierres = emptyList()
    }

    suspend fun registrar(
        fecha: LocalDate, efectivo: Double, tarjeta: Double,
        transferencia: Double, montoContado: Double, responsable: String, uid: String
    ) {
        val datos = mapOf(
            "fecha" to fecha.toString(),
            "efectivo" to efectivo,
            "tarjeta" to tarjeta,
            "transferencia" to transferencia,
            "montoContado" to montoContado,
            "responsable" to responsable,
            "creadoPor" to uid,
            "creadoEn" to FieldValue.serverTimestamp()
        )
        coleccion.add(datos).await()
    }

    /** Del más reciente al más antiguo. */
    fun listar(): List<CierreCaja> = cierres

    fun buscarPorId(id: String): CierreCaja? = cierres.find { it.id == id }

    suspend fun actualizarConteo(id: String, nuevoMontoContado: Double) {
        coleccion.document(id).update(
            mapOf("montoContado" to nuevoMontoContado, "actualizadoEn" to FieldValue.serverTimestamp())
        ).await()
    }

    suspend fun eliminar(id: String) {
        coleccion.document(id).delete().await()
    }

    fun cierresConFaltante(): List<CierreCaja> = cierres.filter { it.tieneFaltante }

    fun totalFaltante(): Double = cierres.filter { it.tieneFaltante }.sumOf { -it.diferencia }

    fun cantidad(): Int = cierres.size

    /** Ingresos acumulados por método de pago, para el reporte financiero. */
    fun totalIngresosPorMetodo(): Triple<Double, Double, Double> = Triple(
        cierres.sumOf { it.efectivo },
        cierres.sumOf { it.tarjeta },
        cierres.sumOf { it.transferencia }
    )

    /** Total cobrado por día en los últimos [dias] días (incluye días sin cierres). */
    fun ingresosPorDia(dias: Int = 7, hoy: LocalDate = LocalDate.now()): List<IngresoDiario> =
        (dias - 1 downTo 0).map { atras ->
            val fecha = hoy.minusDays(atras.toLong())
            IngresoDiario(fecha, cierres.filter { it.fecha == fecha }.sumOf { it.totalEsperado })
        }

    /** Convierte un documento en [CierreCaja]; si está incompleto lo ignora. */
    private fun aCierre(doc: DocumentSnapshot): CierreCaja? = try {
        CierreCaja(
            id = doc.id,
            fecha = LocalDate.parse(doc.getString("fecha")),
            efectivo = doc.getDouble("efectivo") ?: 0.0,
            tarjeta = doc.getDouble("tarjeta") ?: 0.0,
            transferencia = doc.getDouble("transferencia") ?: 0.0,
            montoContado = doc.getDouble("montoContado") ?: 0.0,
            responsable = doc.getString("responsable").orEmpty(),
            creadoPor = doc.getString("creadoPor").orEmpty()
        )
    } catch (e: Exception) {
        LoggerErrores.registrar("Cierre ${doc.id} ignorado: ${e.message}")
        null
    }

    companion object {
        const val COLECCION = "cierresCaja"
    }
}
