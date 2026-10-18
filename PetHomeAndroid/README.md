# Pet Home — Etapa 3, Foro 1 (Jetpack Compose)

**Proyecto de Cátedra DSM941 · Equipo PremierCoders**

App Android interna para el personal de **Pet Home, Centro Médico Veterinario**.
Migra a Jetpack Compose la lógica de la Etapa 2 (consola), manteniendo la
arquitectura MVC y los mismos 5 módulos.

## Cómo abrirla
1. Android Studio → **File → Open** → seleccionar la carpeta `PetHomeAndroid` (no la raíz del repo).
2. Esperar a que termine **Gradle Sync** (la primera vez descarga dependencias, tarda varios minutos).
3. Elegir un emulador o teléfono y presionar ▶ **Run 'app'**.

Cuentas de prueba:

| Rol | Correo | Contraseña |
|-----|--------|------------|
| Administrador / MVZ | admin@pethome.com | admin123 |
| Recepcionista / Cajero | recepcion@pethome.com | recep123 |

También se puede crear una cuenta nueva desde **"¿Personal nuevo? Crear cuenta"**.

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
