package com.premiercoders.pethome.controlador

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.premiercoders.pethome.BuildConfig
import com.premiercoders.pethome.util.DatosInvalidosException
import com.premiercoders.pethome.util.LoggerErrores
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/**
 * Sube las fotos clínicas a Cloudinary (plan gratuito) y devuelve la URL
 * pública. En Firestore solo se guarda esa URL, no la imagen.
 *
 * Se usa la subida "unsigned" con un upload preset, así la app no necesita
 * la clave secreta de Cloudinary. El nombre de la cuenta y el preset se
 * leen de local.properties (BuildConfig), que no se sube a GitHub.
 */
class ClienteCloudinary(
    private val cloudName: String = BuildConfig.CLOUDINARY_CLOUD_NAME,
    private val uploadPreset: String = BuildConfig.CLOUDINARY_UPLOAD_PRESET
) {

    /** Comprime la foto, la sube y devuelve su URL https. */
    suspend fun subirFoto(resolver: ContentResolver, uri: Uri): String = withContext(Dispatchers.IO) {
        if (cloudName.isBlank() || uploadPreset.isBlank()) {
            throw DatosInvalidosException("Falta configurar Cloudinary en local.properties")
        }
        val imagen = comprimir(resolver, uri)
        val limite = "----PetHome${UUID.randomUUID()}"
        val conexion = URL("https://api.cloudinary.com/v1_1/$cloudName/image/upload").openConnection() as HttpURLConnection
        try {
            conexion.requestMethod = "POST"
            conexion.doOutput = true
            conexion.connectTimeout = 15_000
            conexion.readTimeout = 30_000
            conexion.setRequestProperty("Content-Type", "multipart/form-data; boundary=$limite")

            DataOutputStream(conexion.outputStream).use { salida ->
                salida.writeBytes("--$limite\r\n")
                salida.writeBytes("Content-Disposition: form-data; name=\"upload_preset\"\r\n\r\n")
                salida.writeBytes("$uploadPreset\r\n")
                salida.writeBytes("--$limite\r\n")
                salida.writeBytes("Content-Disposition: form-data; name=\"folder\"\r\n\r\n")
                salida.writeBytes("pethome/evidencias\r\n")
                salida.writeBytes("--$limite\r\n")
                salida.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"foto.jpg\"\r\n")
                salida.writeBytes("Content-Type: image/jpeg\r\n\r\n")
                salida.write(imagen)
                salida.writeBytes("\r\n--$limite--\r\n")
            }

            val codigo = conexion.responseCode
            if (codigo !in 200..299) {
                val detalle = conexion.errorStream?.bufferedReader()?.use { it.readText() }
                LoggerErrores.registrar("Cloudinary respondió $codigo: $detalle")
                throw DatosInvalidosException("No se pudo subir la foto (error $codigo)")
            }
            val respuesta = conexion.inputStream.bufferedReader().use { it.readText() }
            JSONObject(respuesta).getString("secure_url")
        } finally {
            conexion.disconnect()
        }
    }

    /** Reduce la foto a 1600 px de lado mayor en JPEG 80 % para subir rápido. */
    private fun comprimir(resolver: ContentResolver, uri: Uri): ByteArray {
        val original = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
            ?: throw DatosInvalidosException("No se pudo leer la foto seleccionada")
        val escala = minOf(1f, LADO_MAXIMO / maxOf(original.width, original.height).toFloat())
        val ajustada = if (escala < 1f) {
            Bitmap.createScaledBitmap(original, (original.width * escala).toInt(), (original.height * escala).toInt(), true)
        } else {
            original
        }
        return ByteArrayOutputStream().use { bytes ->
            ajustada.compress(Bitmap.CompressFormat.JPEG, 80, bytes)
            bytes.toByteArray()
        }
    }

    companion object {
        private const val LADO_MAXIMO = 1600f
    }
}
