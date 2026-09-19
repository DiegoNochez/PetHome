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
| 1 | Cierre de Caja | Calcula el total esperado (efectivo + tarjeta + transferencia) y lo concilia contra el conteo físico, mostrando el sobrante/faltante exacto. |
| 2 | Créditos Fiscales | Asigna numeración correlativa automática (`CCF-AAAA-####`) y calcula el IVA (13%, El Salvador) sobre el subtotal. |
| 3 | Evidencia Clínica | Adjunta fotografías al registro del paciente, sustituyendo el envío disperso por WhatsApp. |
| 4 | Seguimiento Post-tratamiento | Calcula la fecha del próximo control (fecha del procedimiento + días indicados) y mantiene un estado explícito Pendiente/Completado. |
| 5 | Reportes Financieros | Resume ingresos por método de pago, créditos fiscales emitidos, ingreso total y seguimientos pendientes. |

## Estructura del proyecto (arquitectura MVC)

```
src/
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

Requiere el compilador de Kotlin (`kotlinc`) y Java 11+ instalados.

```bash
# Compilar todo el proyecto en un .jar ejecutable
kotlinc src -include-runtime -d PetHome.jar

# Ejecutar
java -jar PetHome.jar
```

Alternativamente, se puede abrir la carpeta `src/` como proyecto de Kotlin
puro en IntelliJ IDEA / Android Studio y ejecutar `Main.kt` directamente.

Al ejecutarse, el programa pide iniciar sesión eligiendo un rol
(Administrador/Veterinario o Recepcionista/Cajero); el menú que se muestra
después cambia según el rol, reflejando el control de acceso diferenciado
definido en la Etapa 1.

Cualquier error de validación (fecha, número o texto mal ingresado) se
reintenta en consola y además se registra con fecha y hora en
`errores.log`, generado en el mismo directorio donde se ejecuta el programa.

## Requerimientos funcionales de la Etapa 2 — dónde están

1. **Módulo de gestión principal (CRUD)** → `GestorCreditoFiscal` y
   `GestorCierreCaja` (crear, listar, actualizar/anular, eliminar).
2. **Módulo de procesamiento o cálculo** → cálculo de IVA (13%) en
   `CreditoFiscal`, conciliación de caja en `CierreCaja`, y fecha de
   seguimiento calculada en `Seguimiento`.
3. **Visualización de resultados en consola** → todas las opciones de
   "Listar..." de cada menú.
4. **Generación de reporte o resumen del sistema** → opción 12 del menú de
   Administrador, implementada en `GestorReportes`.
5. **Actualización dinámica de datos durante la ejecución** → el menú
   corre en un bucle mientras el programa está activo: cualquier registro
   creado, actualizado o anulado se refleja de inmediato en los listados y
   en el reporte, sin reiniciar el programa.

## Trabajo colaborativo en GitHub

Este repositorio se entrega con la rama `main` ya lista. Para cumplir con
el requisito de una rama por integrante:

```bash
# Cada integrante, desde su propia cuenta de GitHub:
git clone <URL-del-repositorio>
cd PetHome-Etapa2
git checkout -b nombre-del-integrante
# ... hacer cambios o agregar funcionalidad ...
git add .
git commit -m "Descripción clara del cambio"
git push origin nombre-del-integrante
```

Todos los integrantes del equipo PremierCoders deben aparecer como
colaboradores del repositorio (Settings → Collaborators en GitHub) y tener
al menos un commit propio en su rama; de lo contrario, según la rúbrica,
ese integrante recibe nota de cero automáticamente en esta etapa.

## Próximos pasos (Etapa 3)

- Migrar esta lógica a una app Android con Jetpack Compose.
- Reemplazar las listas en memoria por Firebase Authentication + Firestore.
- Implementar la interfaz gráfica basada en los mockups de la Etapa 1.
