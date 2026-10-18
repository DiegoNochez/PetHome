package com.premiercoders.pethome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.premiercoders.pethome.navegacion.NavegacionPetHome
import com.premiercoders.pethome.util.LoggerErrores
import com.premiercoders.pethome.vista.tema.PetHomeTheme

/** Única Activity de la app: todas las pantallas son composables. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LoggerErrores.inicializar(applicationContext)
        enableEdgeToEdge()
        setContent {
            PetHomeTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    NavegacionPetHome()
                }
            }
        }
    }
}
