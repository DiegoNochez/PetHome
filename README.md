# Pet Home — Etapa 2: Desarrollo Base del Proyecto

**Proyecto de Cátedra DSM941 · Equipo PremierCoders**

Núcleo funcional en Kotlin (modo consola) del sistema interno de personal para
**Pet Home, Centro Médico Veterinario** (organización real, ver el documento
de la Etapa 1). Esta app **no** es para clientes: es una herramienta de uso
exclusivo del personal (Administrador/Veterinario y Recepcionista/Cajero)
para resolver los tres problemas reales detectados en la entrevista de la
Etapa 1: el cierre de caja y los créditos fiscales manuales, la evidencia
clínica dispersa en WhatsApp, y el seguimiento post-tratamiento inconsistente.

## Módulos implementados

| # | Módulo | Lógica de negocio real |
|---|--------|-------------------------|
| 1 | Cierre de Caja | Calcula el total esperado (efectivo + tarjeta + transferencia) y lo concilia contra el conteo físico, mostrando el sobrante/faltante exacto. Incluye además un listado filtrado de cierres con faltante. |
| 2 | Créditos Fiscales | Asigna numeración correlativa automática (`CCF-AAAA-####`) y calcula el IVA (13%, El Salvador) sobre el subtotal. Incluye además un listado filtrado de créditos anulados. |
| 3 | Evidencia Clínica | Adjunta fotografías al registro del paciente, sustituyendo el envío disperso por WhatsApp. |
| 4 | Seguimiento Post-tratamiento | Calcula la fecha del próximo control (fecha del procedimiento + días indicados), mantiene un estado explícito Pendiente/Completado, y permite consultar los seguimientos próximos a vencer en los siguientes 7 días. |
| 5 | Reportes Financieros | Resume ingresos por método de pago, créditos fiscales emitidos, ingreso total y seguimientos pendientes. |

## Estructura del proyecto (arquitectura MVC + Gradle)

El proyecto usa Gradle con Kotlin DSL (requerido para compilar correctamente
dentro de IntelliJ IDEA):

```
PetHomeApp/
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew / gradlew.bat
└── src/main/kotlin/
    ├── Main.kt                        # Punto de entrada + menús por rol
    └── pethome/
        ├── modelo/                    # Modelo: clases de datos del dominio
        │   ├── Usuario.kt             # Clase abstracta + herencia (Administrador, Recepcionista)
        │   ├── CierreCaja.kt
        │   ├── CreditoFiscal.kt
        │   ├── EvidenciaClinica.kt
        │   ├── Seguimiento.kt
        │   └── Enums.kt               # MetodoPago, EstadoSeguimiento
        ├── controlador/                # Controlador: lógica de negocio y validación
        │   ├── GestorCierreCaja.kt
        │   ├── GestorCreditoFiscal.kt
        │   ├── GestorEvidenciaClinica.kt
        │   ├── GestorSeguimiento.kt
        │   └── GestorReportes.kt
        ├── vista/
        │   └── Consola.kt             # Vista: menús e inicio de sesión
        └── util/
            ├── Resumible.kt           # Interfaz implementada por las entidades del modelo
            ├── Validador.kt           # Validación de entradas + reintentos
            ├── LoggerErrores.kt       # Log de errores en archivo de texto (errores.log)
            └── DatosInvalidosException.kt
```

Esta misma separación Vista/Controlador/Modelo es la que se documentó en la
arquitectura de la Etapa 1, y es la base sobre la que se migrará a Jetpack
Compose + Firebase en la Etapa 3.

## Cómo compilar y ejecutar

**Opción recomendada — IntelliJ IDEA:**

1. Abrir la carpeta `PetHomeApp` como proyecto (IntelliJ detecta Gradle
   automáticamente y descarga las dependencias la primera vez).
2. Esperar a que termine el proceso de importación de Gradle (barra de
   progreso inferior).
3. Abrir `Main.kt` y presionar el botón ▶ verde junto a `fun main()`.

**Opción por terminal (dentro de la carpeta `PetHomeApp`):**

```bash
./gradlew run
```

En Windows (Git Bash / PowerShell), si `./gradlew` no se ejecuta directo:

```bash
gradlew.bat run
```

Al ejecutarse, el programa pide iniciar sesión eligiendo un rol
(Administrador/Veterinario o Recepcionista/Cajero); el menú que se muestra
después cambia según el rol, reflejando el control de acceso diferenciado
definido en la Etapa 1.

Cualquier error de validación (fecha, número, texto o correo mal ingresado)
se reintenta en consola y además se registra con fecha y hora en
`errores.log`, generado en el mismo directorio donde se ejecuta el programa.

## Requerimientos funcionales de la Etapa 2 — dónde están

1. **Módulo de gestión principal (CRUD)** → `GestorCreditoFiscal` y
   `GestorCierreCaja` (crear, listar, actualizar, eliminar/anular).
2. **Módulo de procesamiento o cálculo** → cálculo de IVA (13%) en
   `CreditoFiscal`, conciliación de caja en `CierreCaja`, y fecha de
   seguimiento calculada en `Seguimiento`.
3. **Visualización de resultados en consola** → todas las opciones de
   "Listar..." de cada menú.
4. **Generación de reporte o resumen del sistema** → opción 12 del menú de
   Administrador, implementada en `GestorReportes`.
5. **Actualización dinámica de datos durante la ejecución** → el menú
   corre en un bucle mientras el programa está activo: cualquier registro
   creado, actualizado, completado o anulado se refleja de inmediato en los
   listados y en el reporte, sin reiniciar el programa.

Estos 5 puntos fueron verificados manualmente ejecutando el flujo completo
como Administrador y como Recepcionista antes de la entrega.

## Trabajo colaborativo en GitHub

Cada integrante del equipo trabajó en su propia rama, con al menos una
funcionalidad real e independiente, luego fusionada a `main` vía Pull Request:

| Integrante | Rama | Aporte |
|---|---|---|
| Diego | `main` | Estructura base del proyecto (5 módulos), migración a Gradle |
| Nelson | `rama-nelson` | Validación de formato de correo electrónico al iniciar sesión; consulta de seguimientos próximos a vencer (7 días) |
| Jimmy | `rama-jimmy` | Listado de cierres de caja con faltante; listado de créditos fiscales anulados |

Flujo usado por cada integrante:

```bash
git checkout main
git pull
git checkout -b rama-nombre-integrante
# ... hacer cambios y probar en IntelliJ ...
git add .
git commit -m "Descripción clara del cambio"
git push -u origin rama-nombre-integrante
# Luego: abrir un Pull Request en GitHub hacia main y fusionarlo
```

Todos los integrantes del equipo PremierCoders aparecen como colaboradores
del repositorio, con su propia rama y al menos un commit propio.

## Próximos pasos (Etapa 3)

- Migrar esta lógica a una app Android con Jetpack Compose.
- Reemplazar las listas en memoria por Firebase Authentication + Firestore.
- Implementar la interfaz gráfica basada en los mockups de la Etapa 1.