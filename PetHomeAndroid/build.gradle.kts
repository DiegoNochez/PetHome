// Archivo de compilación raíz: solo declara los plugins; cada módulo los aplica.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    // Lee app/google-services.json (no se sube al repo, ver README).
    alias(libs.plugins.google.services) apply false
}
