# Pet Home — Etapa 3, Foro 1 (Jetpack Compose)

**Proyecto de Cátedra DSM941 · Equipo PremierCoders**

App Android interna para el personal de **Pet Home, Centro Médico Veterinario**.
Migra a Jetpack Compose la lógica de la Etapa 2 (consola), manteniendo la
arquitectura MVC y los mismos 5 módulos.

## Cómo abrirla
1. Android Studio → **File → Open** → seleccionar la carpeta `PetHomeAndroid` (no la raíz del repo).
2. Esperar a que termine **Gradle Sync** (la primera vez descarga dependencias, tarda varios minutos).
3. Elegir un emulador o teléfono y presionar ▶ **Run 'app'**.

> **Foro 2:** antes de compilar hay que configurar Firebase (ver la sección siguiente).
> Las cuentas de prueba locales del Foro 1 ya no existen: el acceso es con Firebase.

## Configuración de Firebase (Foro 2)
La app usa **Firebase Authentication** (correo/contraseña y Google) y **Cloud Firestore**.
El archivo `app/google-services.json` contiene las claves del proyecto y **no se sube al
repositorio** (está en `.gitignore`). Cada integrante debe:

1. Pedir acceso al proyecto **PetHome** en [Firebase Console](https://console.firebase.google.com)
   (rol Editor) y descargar `google-services.json` desde
   **Configuración del proyecto → General → Tus apps → Pet Home Android**.
2. Copiarlo en `PetHomeAndroid/app/google-services.json`.
3. Registrar su propia huella **SHA-1** (necesaria para iniciar sesión con Google):
   en la terminal de Android Studio ejecutar `./gradlew signingReport`, copiar el
   SHA1 de la variante `debug` y agregarlo en **Tus apps → Add fingerprint**.
4. Usar un emulador con **Google Play** y una cuenta de Google iniciada.

### Fotos clínicas (Cloudinary)
Las fotos de Evidencia Clínica se suben a [Cloudinary](https://cloudinary.com) (plan gratuito) y en
Firestore solo se guarda su URL. Para configurarlo:
1. Crear una cuenta gratuita y copiar el **Cloud name** del panel.
2. En **Settings → Upload → Upload presets → Add upload preset**, elegir *Signing mode: Unsigned* y guardar.
3. Agregar en `PetHomeAndroid/local.properties` (no se sube al repositorio):
   ```
   cloudinary.cloudName=TU_CLOUD_NAME
   cloudinary.uploadPreset=TU_PRESET
   ```
4. Sincronizar Gradle y volver a compilar.

### Roles
- Toda cuenta nueva (registro o primer ingreso con Google) se crea en Firestore,
  en `usuarios/{uid}`, con rol **RECEPCIONISTA**.
- El rol **ADMINISTRADOR** se asigna solo desde la consola: Firestore → `usuarios` →
  documento del usuario → cambiar el campo `rol`. Las reglas impiden que la app lo cambie.

### Colecciones de Firestore
| Colección | Contenido | Quién escribe |
| --- | --- | --- |
| `usuarios/{uid}` | nombre, correo, rol | cada usuario crea el suyo (rol Recepcionista) |
| `cierresCaja/{id}` | fecha, efectivo, tarjeta, transferencia, montoContado, responsable, creadoPor | los dos roles; eliminar solo Administrador |
| `creditosFiscales/{CCF-AAAA-####}` | cliente, NIT, NRC, servicio, subtotal, anulado, creadoPor | los dos roles; anular solo Administrador |
| `contadores/creditos-AAAA` | siguiente correlativo | transacción al emitir un crédito |
| `evidencias/{id}` | paciente, fecha, diagnóstico, fotos (URL de Cloudinary), creadoPor | solo Administrador |
| `seguimientos/{id}` | paciente, procedimiento, fechaProcedimiento, diasParaControl, estado, creadoPor | solo Administrador |

Las listas de caja y créditos se actualizan en tiempo real (`addSnapshotListener`) y el
historial se puede filtrar por periodo (hoy, últimos 7 días, este mes).

### Reglas de seguridad
Las reglas están en [`firestore.rules`](firestore.rules) y se publican en
**Firestore Database → Reglas**. Cada usuario solo puede leer y crear su propio perfil,
no puede registrarse como administrador y todo lo no permitido queda bloqueado.

## Requisitos del Foro 1 → dónde están
| Requisito | Implementación |
|-----------|----------------|
| Login / registro funcional | `PantallaLogin`, `PantallaRegistro`, `GestorUsuarios` (correo duplicado, contraseña incorrecta) |
| Módulo principal en Compose | `PantallaPanel` + 5 módulos: Cierre de Caja, Créditos Fiscales, Evidencia Clínica, Seguimiento, Reportes |
| Navegación entre pantallas | `navegacion/NavegacionPetHome.kt` (Navigation Compose, rutas protegidas por sesión y por rol) |
| Validación de formularios | `util/Validador.kt`: error debajo de cada campo (correo, contraseña, montos, fechas, rangos) |

## Arquitectura (MVC)
```
app/src/main/java/com/premiercoders/pethome/
├── MainActivity.kt              # única Activity, carga el tema y la navegación
├── modelo/                      # Modelo: data classes de la Etapa 2 (ahora inmutables)
│   ├── Usuario.kt               # abstracta → Administrador / Recepcionista (puedeAcceder polimórfico)
│   ├── CierreCaja.kt  CreditoFiscal.kt  EvidenciaClinica.kt  Seguimiento.kt
│   ├── ResumenFinanciero.kt     # datos del reporte
│   └── Enums.kt                 # Rol, Modulo, EstadoSeguimiento, MetodoPago
├── controlador/                 # Controlador: lógica de negocio
│   ├── Gestor*.kt               # misma lógica de la Etapa 2 (IVA 13 %, correlativo, diferencia de caja…)
│   ├── GestorUsuarios.kt        # login y registro locales (Foro 2 → Firebase Auth)
│   └── PetHomeViewModel.kt      # expone el estado a Compose y sobrevive a la rotación
├── navegacion/                  # rutas y control de acceso
├── util/                        # Validador, LoggerErrores (errores.log + Logcat), Formato
└── vista/                       # Vista: Jetpack Compose
    ├── tema/                    # paleta teal de los mockups
    ├── componentes/             # CampoTexto, PantallaBase, TarjetaDato, diálogos
    └── pantallas/               # 8 pantallas
```

## Permisos por rol
- **Administrador / MVZ:** los 5 módulos.
- **Recepcionista / Cajero:** Cierre de Caja y Créditos Fiscales. Si intenta abrir otro módulo ve "Acceso restringido".

## Notas
- Los datos viven en memoria (se reinician al cerrar la app) y arrancan con algunos registros de ejemplo con mascotas ficticias. En el Foro 2 se reemplazan por Firebase Authentication + Firestore.
- Las fotos de evidencia se eligen con el selector de fotos de Android (no pide permisos). En el emulador puedes arrastrar una imagen a la ventana para que aparezca en la galería.
- Los errores se registran en `errores.log` (memoria interna de la app) y en **Logcat** con la etiqueta `PetHome`.

## Versiones
Android Gradle Plugin 8.7.3 · Gradle 8.11.1 · Kotlin 2.0.21 · Compose BOM 2024.12.01 · minSdk 26 · targetSdk 35.
Si Android Studio ofrece actualizar el Android Gradle Plugin, se puede posponer: el proyecto funciona con estas versiones.

## Casos de prueba (Foro 1)

Pruebas manuales realizadas en el emulador (Medium Phone API 37) para
verificar los requisitos del Foro 1. Para repetirlas, usa las cuentas de
prueba de la tabla de arriba.

### Autenticación y registro
| # | Caso | Pasos | Resultado esperado |
|---|------|-------|--------------------|
| 1 | Campos vacíos | Presionar **Ingresar** sin escribir nada | Mensaje en rojo debajo de correo y contraseña |
| 2 | Correo inválido | Escribir `admin@pethome` | "Correo inválido" |
| 3 | Contraseña incorrecta | `admin@pethome.com` + `hola` | "Contraseña incorrecta" |
| 4 | Correo no registrado | `nadie@pethome.com` | "No existe una cuenta con ese correo" |
| 5 | Contraseña débil | Registro con `abcdef` | "Debe incluir letras y números" |
| 6 | Correo duplicado | Registro con `admin@pethome.com` | "Ya existe una cuenta con ese correo" |
| 7 | Permisos por rol | Entrar como Recepción | Solo ve Cierre de Caja y Créditos Fiscales |

### Módulos
| # | Caso | Pasos | Resultado esperado |
|---|------|-------|--------------------|
| 8 | Monto inválido | Efectivo = `abc`, `-5` o `10.555` | Error debajo del campo; no se registra |
| 9 | Fecha futura | Fecha de cierre posterior a hoy | "La fecha no puede ser futura" |
| 10 | Faltante de caja | Contado menor que el total esperado | "Faltante de $X" en rojo |
| 11 | IVA 13 % | Crédito con subtotal `50.00` | IVA $6.50, total $56.50 y correlativo siguiente |
| 12 | Anular crédito | Botón **Anular** → confirmar | Queda tachado y no suma en Reportes |
| 13 | Foto clínica | **Adjuntar foto** en una evidencia | Aparece la miniatura; al tocarla se amplía |
| 14 | Días de control | Días = `0` o `400` | "Debe estar entre 1 y 365" |
| 15 | Paciente vencido | Abrir Seguimiento | Luna aparece como "Vencido" en rojo |

### Comportamiento general
| # | Caso | Pasos | Resultado esperado |
|---|------|-------|--------------------|
| 16 | Rotación | Rotar el emulador con un formulario a medias | No se pierden los datos ni lo escrito |
| 17 | Cerrar sesión | Cerrar sesión y presionar Atrás | No regresa al panel |
