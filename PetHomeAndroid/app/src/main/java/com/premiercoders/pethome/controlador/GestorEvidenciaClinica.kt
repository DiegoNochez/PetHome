package com.premiercoders.pethome.controlador

import android.content.ContentResolver
import android.net.Uri
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.premiercoders.pethome.modelo.EvidenciaClinica
import com.premiercoders.pethome.util.DatosInvalidosException
import com.premiercoders.pethome.util.LoggerErrores
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

/**
 * Evidencias clínicas en Firestore, colección `evidencias` (Foro 2).
 * Las fotos van a Cloudinary con [ClienteCloudinary]; aquí solo se agrega
 * su URL al arreglo `fotos` del documento.
 */
class GestorEvidenciaClinica(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val cloudinary: ClienteCloudinary = ClienteCloudinary()
) {
    private val coleccion get() = db.collection(COLECCION)
    private var registros: List<EvidenciaClinica> = emptyList()

    fun escuchar(onCambio: () -> Unit, onError: (Exception) -> Unit): ListenerRegistration =
        coleccion.orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                registros = snapshot?.documents?.mapNotNull { aEvidencia(it) }.orEmpty()
                onCambio()
            }

    fun limpiar() {
        registros = emptyList()
    }

    suspend fun crear(paciente: String, fecha: LocalDate, diagnostico: String, uid: String) {
        coleccion.add(
            mapOf(
                "paciente" to paciente.trim(),
                "fecha" to fecha.toString(),
                "diagnostico" to diagnostico.trim(),
                "fotos" to emptyList<String>(),
                "creadoPor" to uid,
                "creadoEn" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    fun listar(): List<EvidenciaClinica> = registros

    /** Sube la foto a Cloudinary y guarda su URL en la evidencia. */
    suspend fun adjuntarFoto(id: String, resolver: ContentResolver, uri: Uri) {
        val evidencia = registros.find { it.id == id }
        if (evidencia != null && evidencia.fotosAdjuntas.size >= EvidenciaClinica.MAXIMO_FOTOS) {
            throw DatosInvalidosException("Máximo ${EvidenciaClinica.MAXIMO_FOTOS} fotos por registro")
        }
        val url = cloudinary.subirFoto(resolver, uri)
        coleccion.document(id).update("fotos", FieldValue.arrayUnion(url)).await()
    }

    suspend fun eliminar(id: String) {
        coleccion.document(id).delete().await()
    }

    @Suppress("UNCHECKED_CAST")
    private fun aEvidencia(doc: DocumentSnapshot): EvidenciaClinica? = try {
        EvidenciaClinica(
            id = doc.id,
            paciente = doc.getString("paciente").orEmpty(),
            fecha = LocalDate.parse(doc.getString("fecha")),
            diagnostico = doc.getString("diagnostico").orEmpty(),
            fotosAdjuntas = (doc.get("fotos") as? List<String>).orEmpty(),
            creadoPor = doc.getString("creadoPor").orEmpty()
        )
    } catch (e: Exception) {
        LoggerErrores.registrar("Evidencia ${doc.id} ignorada: ${e.message}")
        null
    }

    companion object {
        const val COLECCION = "evidencias"
    }
}
