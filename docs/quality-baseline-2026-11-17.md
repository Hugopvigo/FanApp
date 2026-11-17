# Línea base de calidad — 2026-11-17

Snapshot del estado del repo **antes** de empezar el plan de pulido de código y diseño.
Sirve como referencia para distinguir fallos preexistentes de regresiones.

## Entorno

| Dato | Valor |
|---|---|
| Commit | `7b654ec` (rama `main`) |
| JVM por defecto del shell | OpenJDK 25.0.5 (Debian) |
| `JAVA_HOME` | `/usr/lib/jvm/java-21-openjdk-amd64` |
| Gradle | 9.5.1 |
| AGP / Kotlin | ver `libs.versions.toml` (warnings de obsolete API y plugin deprecated, ver abajo) |
| Fecha | 2026-11-17 |

Nota: el shell tiene Java 25 en el `PATH` pero el build usa `JAVA_HOME` (Java 21),
que es lo que realmente se compila. La documentación del repo menciona Java 21;
no hay discrepancia efectiva.

## Resultados

| Comando | Resultado |
|---|---|
| `./gradlew :app:compileDebugKotlin` | ✅ BUILD SUCCESSFUL (9 tareas, todas up-to-date) |
| `./gradlew :app:testDebugUnitTest` | ✅ BUILD SUCCESSFUL — **93 tests, 0 fallos, 0 errores, 0 omitidos** (18 ficheros XML) |
| `./gradlew :app:lint` | ❌ BUILD FAILED — **12 errores, 99 avisos** |

## Errores de lint preexistentes (12)

Todos existen en `7b654ec`, antes de cualquier cambio de este plan.

| Archivo | Línea | Regla | Descripción |
|---|---|---|---|
| `presentation/widget/FanAppWidget.kt` | 62, 72, 95, 105, 116 | `ResourceType` | `padding(16)` con literal en vez de recurso `dimen` (Glance) |
| `presentation/widget/FanAppWidget.kt` | 77, 110 | `ResourceType` | RGB integer pasado donde lint espera `R.color` |
| `presentation/widget/FanAppWidget.kt` | 77, 110, 121 | `RestrictedApi` | `ColorProvider` de `androidx.glance` restringido a su grupo |
| `presentation/auth/LoginScreen.kt` | 285 | `LocalContextGetResourceValueCall` | Consulta de recursos vía `LocalContext.current` |
| `presentation/navigation/AppNavGraph.kt` | 165 | `UnusedMaterial3ScaffoldPaddingParameter` | Parámetro de padding del `Scaffold` ignorado |

Los 10 primeros son propios del uso de Glance (sus `ColorProvider`/valores `dp`
no encajan con las reglas de lint de recursos clásicas); los 2 últimos son reales y
se corrigen en la tarea A10.

## Avisos de lint preexistentes (99)

Principales categorías: `UnusedResources` (30), `GradleDependency` (18),
`NewerVersionAvailable` (12), `IconLauncherShape` (10), `IconDuplicates` (6),
`UseTomlInstead` (5), y el resto (28) de menor frecuencia: `UnusedAttribute`,
`TypographyEllipsis`, `MonochromeLauncherIcon`, `AndroidGradlePluginVersion`,
`UseKtx`, `PluralsCandidate`, `Overdraw`, `OldTargetApi`, `ObsoleteSdkInt`, etc.

Ninguno bloquea el build y ninguno es causado por este plan.

## Warnings de compilación preexistentes

- API `unitTestVariants` / `testVariants` obsolete (se eliminará en AGP 10).
- Plugin `org.jetbrains.kotlin.android` deprecated desde AGP 9.0.
- Funciones Gradle deprecated (incompatibles con Gradle 10).
- Sugerencia de habilitar configuration cache.

## Cómo usar esta línea base

Tras cada tarea, `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest` debe
seguir en verde con **al menos** los mismos 93 tests. En el cierre de Fase A se
comprueba que no aparezcan errores de lint **nuevos**; los 12 de arriba son el
punto de partida y se abordan en A10.
