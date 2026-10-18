package com.premiercoders.pethome.navegacion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.premiercoders.pethome.controlador.PetHomeViewModel
import com.premiercoders.pethome.modelo.Modulo
import com.premiercoders.pethome.modelo.Usuario
import com.premiercoders.pethome.vista.componentes.PantallaBase
import com.premiercoders.pethome.vista.pantallas.PantallaCierreCaja
import com.premiercoders.pethome.vista.pantallas.PantallaCreditoFiscal
import com.premiercoders.pethome.vista.pantallas.PantallaEvidenciaClinica
import com.premiercoders.pethome.vista.pantallas.PantallaLogin
import com.premiercoders.pethome.vista.pantallas.PantallaPanel
import com.premiercoders.pethome.vista.pantallas.PantallaRegistro
import com.premiercoders.pethome.vista.pantallas.PantallaReportes
import com.premiercoders.pethome.vista.pantallas.PantallaSeguimiento

/** Rutas de todas las pantallas de la app. */
object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val PANEL = "panel"
    const val CIERRE_CAJA = "cierre_caja"
    const val CREDITOS = "creditos_fiscales"
    const val EVIDENCIA = "evidencia_clinica"
    const val SEGUIMIENTO = "seguimiento"
    const val REPORTES = "reportes"
}

fun Modulo.ruta(): String = when (this) {
    Modulo.CIERRE_CAJA -> Rutas.CIERRE_CAJA
    Modulo.CREDITOS_FISCALES -> Rutas.CREDITOS
    Modulo.EVIDENCIA_CLINICA -> Rutas.EVIDENCIA
    Modulo.SEGUIMIENTO -> Rutas.SEGUIMIENTO
    Modulo.REPORTES -> Rutas.REPORTES
}

fun Modulo.icono(): ImageVector = when (this) {
    Modulo.CIERRE_CAJA -> Icons.Filled.PointOfSale
    Modulo.CREDITOS_FISCALES -> Icons.Filled.ReceiptLong
    Modulo.EVIDENCIA_CLINICA -> Icons.Filled.PhotoCamera
    Modulo.SEGUIMIENTO -> Icons.Filled.EventAvailable
    Modulo.REPORTES -> Icons.Filled.Assessment
}

/**
 * Grafo de navegación. Un solo ViewModel compartido por todas las pantallas,
 * así los datos registrados en un módulo se ven en el panel y en reportes.
 */
@Composable
fun NavegacionPetHome(
    navController: NavHostController = rememberNavController(),
    vm: PetHomeViewModel = viewModel()
) {
    val irAlPanel: () -> Unit = {
        navController.navigate(Rutas.PANEL) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }
    val volver: () -> Unit = { navController.popBackStack() }

    NavHost(navController = navController, startDestination = Rutas.LOGIN) {
        composable(Rutas.LOGIN) {
            PantallaLogin(
                onIniciarSesion = vm::iniciarSesion,
                onExito = irAlPanel,
                onIrARegistro = { navController.navigate(Rutas.REGISTRO) }
            )
        }
        composable(Rutas.REGISTRO) {
            PantallaRegistro(
                onRegistrar = vm::registrar,
                onExito = irAlPanel,
                onVolver = volver
            )
        }
        composable(Rutas.PANEL) {
            ConSesion(vm, navController, modulo = null) { usuario ->
                PantallaPanel(
                    usuario = usuario,
                    resumen = vm.resumen,
                    onAbrirModulo = { modulo -> navController.navigate(modulo.ruta()) },
                    onCerrarSesion = {
                        vm.cerrarSesion()
                        navController.navigate(Rutas.LOGIN) {
                            popUpTo(navController.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
        composable(Rutas.CIERRE_CAJA) {
            ConSesion(vm, navController, Modulo.CIERRE_CAJA) { PantallaCierreCaja(vm, volver) }
        }
        composable(Rutas.CREDITOS) {
            ConSesion(vm, navController, Modulo.CREDITOS_FISCALES) { PantallaCreditoFiscal(vm, volver) }
        }
        composable(Rutas.EVIDENCIA) {
            ConSesion(vm, navController, Modulo.EVIDENCIA_CLINICA) { PantallaEvidenciaClinica(vm, volver) }
        }
        composable(Rutas.SEGUIMIENTO) {
            ConSesion(vm, navController, Modulo.SEGUIMIENTO) { PantallaSeguimiento(vm, volver) }
        }
        composable(Rutas.REPORTES) {
            ConSesion(vm, navController, Modulo.REPORTES) { PantallaReportes(vm.resumen, volver) }
        }
    }
}

/**
 * Protege las pantallas internas: sin sesión regresa al login, y si el rol
 * no tiene permiso para el módulo muestra "Acceso restringido".
 */
@Composable
private fun ConSesion(
    vm: PetHomeViewModel,
    navController: NavHostController,
    modulo: Modulo?,
    contenido: @Composable (Usuario) -> Unit
) {
    val usuario = vm.usuarioActual
    if (usuario == null) {
        LaunchedEffect(Unit) {
            navController.navigate(Rutas.LOGIN) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    } else if (modulo != null && !usuario.puedeAcceder(modulo)) {
        AccesoRestringido(modulo) { navController.popBackStack() }
    } else {
        contenido(usuario)
    }
}

@Composable
private fun AccesoRestringido(modulo: Modulo, onVolver: () -> Unit) {
    PantallaBase(titulo = modulo.titulo, onVolver = onVolver) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
            Text("Acceso restringido", style = MaterialTheme.typography.titleLarge)
            Text("Tu rol no tiene permiso para usar este módulo.", textAlign = TextAlign.Center)
            Button(onClick = onVolver) { Text("Volver al panel") }
        }
    }
}
