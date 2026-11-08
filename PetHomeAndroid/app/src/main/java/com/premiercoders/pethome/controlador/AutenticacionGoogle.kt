package com.premiercoders.pethome.controlador

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.premiercoders.pethome.R
import com.premiercoders.pethome.util.DatosInvalidosException
import com.premiercoders.pethome.util.LoggerErrores
import kotlinx.coroutines.CancellationException

/**
 * Obtiene el token de Google con Credential Manager (API recomendada por
 * Android desde 2024). El token se entrega a [GestorUsuarios], que lo cambia
 * por una sesión de Firebase.
 *
 * Necesita el contexto de la Activity porque muestra el selector de cuentas.
 * `default_web_client_id` lo genera el plugin de Google Services a partir de
 * google-services.json, así el ID no queda escrito en el código.
 */
class AutenticacionGoogle(private val context: Context) {

    private val credentialManager = CredentialManager.create(context)

    suspend fun obtenerIdToken(): String {
        val opcionGoogle = GetSignInWithGoogleOption
            .Builder(context.getString(R.string.default_web_client_id))
            .build()
        val solicitud = GetCredentialRequest.Builder()
            .addCredentialOption(opcionGoogle)
            .build()

        return try {
            val credencial = credentialManager.getCredential(context, solicitud).credential
            if (credencial is CustomCredential &&
                credencial.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                GoogleIdTokenCredential.createFrom(credencial.data).idToken
            } else {
                throw DatosInvalidosException("La cuenta seleccionada no es de Google")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: GetCredentialCancellationException) {
            throw DatosInvalidosException("Inicio con Google cancelado")
        } catch (e: NoCredentialException) {
            LoggerErrores.registrar("Google: no hay cuentas en el dispositivo")
            throw DatosInvalidosException("No hay cuentas de Google en el dispositivo. Agrega una en Ajustes")
        } catch (e: GetCredentialException) {
            LoggerErrores.registrar("Google: ${e.type} - ${e.message}")
            throw DatosInvalidosException("No se pudo iniciar sesión con Google")
        } catch (e: GoogleIdTokenParsingException) {
            LoggerErrores.registrar("Google: token inválido - ${e.message}")
            throw DatosInvalidosException("No se pudo leer la cuenta de Google")
        }
    }

    /** Olvida la cuenta elegida para que el próximo ingreso vuelva a preguntar. */
    suspend fun limpiar() {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            LoggerErrores.registrar("Google: no se pudo limpiar la credencial - ${e.message}")
        }
    }
}
