package com.premiercoders.pethome.controlador

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.premiercoders.pethome.modelo.EstadoSeguimiento
import com.premiercoders.pethome.modelo.Seguimiento
import com.premiercoders.pethome.util.LoggerErrores
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

/**
 * Seguimientos post-tratamiento en Firestore, colección `seguimientos`
 * (Foro 2). La fecha de control y los vencidos se siguen calculando en el
 * modelo, igual que en la Etapa 2.
 */
class GestorSeguimiento(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    private val coleccion get() = db.collection(COLECCION)
    private var seguimientos: List<Seguimiento> = emptyList()

    fun escuchar(onCambio: () -> Unit, onError: (Exception) -> Unit): ListenerRegistration =
        coleccion.addSnapshotListener { snapshot, error ->
            if (error != null) {
                onError(error)
                return@addSnapshotListener
            }
            seguimientos = snapshot?.documents?.mapNotNull { aSeguimiento(it) }.orEmpty()
            onCambio()
        }

    fun limpiar() {
        seguimientos = emptyList()
    }

    suspend fun crear(paciente: String, procedimiento: String, fechaProcedimiento: LocalDate, diasParaControl: Long, uid: String) {
        coleccion.add(
            mapOf(
                "paciente" to paciente.trim(),
                "procedimiento" to procedimiento.trim(),
                "fechaProcedimiento" to fechaProcedimiento.toString(),
                "diasParaControl" to diasParaControl,
                "estado" to EstadoSeguimiento.PENDIENTE.name,
                "creadoPor" to uid,
                "creadoEn" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    /** Ordenados por la fecha de control más cercana. */
    fun listar(): List<Seguimiento> = seguimientos.sortedBy { it.fechaControl }

    fun pendientes(): List<Seguimiento> = listar().filter { it.estado == EstadoSeguimiento.PENDIENTE }

    fun vencidos(hoy: LocalDate = LocalDate.now()): List<Seguimiento> = listar().filter { it.estaVencido(hoy) }

    suspend fun marcarCompletado(id: String) {
        coleccion.document(id).update(
            mapOf("estado" to EstadoSeguimiento.COMPLETADO.name, "completadoEn" to FieldValue.serverTimestamp())
        ).await()
    }

    suspend fun eliminar(id: String) {
        coleccion.document(id).delete().await()
    }

    fun proximosAVencer(dias: Int, hoy: LocalDate = LocalDate.now()): List<Seguimiento> {
        val limite = hoy.plusDays(dias.toLong())
        return listar().filter {
            it.estado == EstadoSeguimiento.PENDIENTE && !it.fechaControl.isBefore(hoy) && !it.fechaControl.isAfter(limite)
        }
    }

    private fun aSeguimiento(doc: DocumentSnapshot): Seguimiento? = try {
        Seguimiento(
            id = doc.id,
            paciente = doc.getString("paciente").orEmpty(),
            procedimiento = doc.getString("procedimiento").orEmpty(),
            fechaProcedimiento = LocalDate.parse(doc.getString("fechaProcedimiento")),
            diasParaControl = doc.getLong("diasParaControl") ?: 0L,
            estado = EstadoSeguimiento.entries.find { it.name == doc.getString("estado") } ?: EstadoSeguimiento.PENDIENTE,
            creadoPor = doc.getString("creadoPor").orEmpty()
        )
    } catch (e: Exception) {
        LoggerErrores.registrar("Seguimiento ${doc.id} ignorado: ${e.message}")
        null
    }

    companion object {
        const val COLECCION = "seguimientos"
    }
}
