import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

// Datos de Cloudinary leídos de local.properties (no se sube al repositorio).
val propiedadesLocales = Properties().apply {
    val archivo = rootProject.file("local.properties")
    if (archivo.exists()) archivo.inputStream().use { load(it) }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.premiercoders.pethome"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.premiercoders.pethome"
        // API 26 (Android 8.0) permite usar java.time (LocalDate) sin librerías extra.
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "2.0-foro2"

        // Foro 2: fotos clínicas en Cloudinary (subida sin firma con upload preset).
        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"${propiedadesLocales.getProperty("cloudinary.cloudName", "")}\"")
        buildConfigField("String", "CLOUDINARY_UPLOAD_PRESET", "\"${propiedadesLocales.getProperty("cloudinary.uploadPreset", "")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // Muestra las fotos de evidencia clínica a partir de su URI.
    implementation(libs.coil.compose)

    // Foro 2: Firebase Authentication (correo y Google) y Firestore.
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.kotlinx.coroutines.play.services)

    // Inicio de sesión con Google mediante Credential Manager.
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.googleid)

    debugImplementation(libs.androidx.ui.tooling)
}
