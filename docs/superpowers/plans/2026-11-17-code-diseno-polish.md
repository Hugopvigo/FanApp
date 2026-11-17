# FanApp — Pulido de código y diseño Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Dejar FanApp estable, más rápido y visualmente coherente, en dos fases encadenadas: primero código/bugs/rendimiento y después diseño, sin añadir features nuevas.

**Architecture:** Fase A actúa en la capa de arranque (`MediaTrackerApp`, `MainActivity`), red (`NetworkModule`), imágenes (Coil `ImageLoaderFactory`) y separa los tres composables gigantes en archivos de components sin cambiar su lógica. Fase B actúa sobre los tokens del tema, los colores literales, los strings hardcodeados y los objetivos táctiles. No se toca la arquitectura de navegación/insets (`FloatingBottomNav`, `AppNavGraph`) más allá de sustituir dos constantes de color.

**Tech Stack:** Kotlin 2.x, Jetpack Compose + Material 3, Hilt, Room, Retrofit + OkHttp, Coil, Timber, JUnit4 + MockK + Turbine, Gradle 9 / JVM toolchain 17.

**Spec:** `docs/superpowers/specs/2026-11-17-code-diseno-polish.md` (léelo antes de ejecutar). Contexto adicional: `AGENT.md`, `Sprint9.md`, `docs/project_ios26_redesign.md`.

## Global Constraints

- Orden de ejecución: **Fase A completa antes de empezar Fase B**. Cada fase termina con build + tests + lint en verde y su propio commit de cierre.
- `AGENT.md`: UI → domain → data, nunca al revés. Máx. 40 líneas por función, composables < 200 líneas, `val` sobre `var`, cero `!!`, cero `println()`/`Log.d()`, cero try/catch vacíos, Timber para logging, DTOs nunca en `presentation`.
- **No tocar** la arquitectura de insets ni el layout de `FloatingBottomNav` / `AppNavGraph` (bug de doble `navigationBarsPadding`, mayo 2026). En `FloatingBottomNav` solo se cambian dos colores.
- Los **4 temas se mantienen**: Fantasy, Light, Purple, Dark. Ningún color nuevo sin su equivalente en los 4.
- i18n: `app/src/main/res/values/strings.xml` es la base EN y `values-es/strings.xml` el ES, 238 claves cada uno. Toda string nueva se añade a **ambos** con la misma clave.
- Compilar con Java 21 según documentación del repo; `gradle.properties` delega en `JAVA_HOME` y `build.gradle.kts` usa `jvmToolchain(17)`. Verificar en Task A1 y anotar la discrepancia si existe.
- Sin dependencias nuevas. Ya están JUnit, MockK, Turbine, OkHttp y Coil.
- Sin migraciones de Room ni `fallbackToDestructiveMigration()` nuevo.
- Git: trabajar en `main` con commits pequeños (sync entre dos máquinas). Usar `git add <rutas>` + `git commit`; **nunca** `git add -A` ni `git commit -am`. Si hay otra sesión en el repo, crear worktree propio antes de empezar (regla de `AGENTS.md` global).
- **Trampa del repo:** `.gitignore` ignora `*.md` salvo `README.md` y `CHANGELOG.md`. Los documentos nuevos (spec, plan, línea base, auditoría) no aparecen en `git status` y hay que añadirlos con `git add -f <ruta>`. Los `.png`/`.jpg` sí se añaden con `git add` normal.

## Review Focus

Estas cinco condiciones no están cubiertas por los tests actuales y son las más probables de romperse. Cada una lleva su prueba en la tarea que posee el código.

1. **Primer arranque (SharedPreferences sin migrar):** se espera que no haya lectura de disco en el hilo principal y que el tema guardado se aplique sin parpadeo. → Task A2 (StrictMode en debug + revisión del logcat en instalación fresca; no hay unit test porque la migración depende de `Context`).
2. **Sin red / red intermitente:** se espera que los metadatos ya vistos se sirvan desde caché. → Task A4 (test de `buildCache`; el `Cache-Control` se verifica con la inspección de la respuesta en QA manual).
3. **Biblioteca grande con cientos de ítems:** se espera que al cambiar el estado de un ítem la lista no reordene ni recicle la card equivocada, y que el scroll no salte. → Task A5 (keys estables en listas dinámicas).
4. **Locale inglés con pantallas auxiliares:** se espera que Importar no mezcle «Cancelar»/«Hecho» en inglés. → Task B3 (test de claves de strings en ambos `values`).
5. **Los 4 temas con el mismo componente:** se espera que los gradientes de `MediaCard`, las estrellas de `FanCard` y la nav flotante usen tokens y mantengan contraste en Light y Dark. → Task B2 (capturas en 4 temas + grep de `Color(0x`).

## File Structure

| Archivo | Responsabilidad |
|---|---|
| `docs/quality-baseline-2026-11-17.md` (nuevo) | Línea base de compile/test/lint antes de tocar nada |
| `docs/design-audit-2026-11-17.md` (nuevo) | Inventario de inconsistencias visuales con capturas |
| `app/src/main/java/com/mediatracker/MediaTrackerApp.kt` | StrictMode debug + `ImageLoaderFactory` |
| `app/src/main/java/com/mediatracker/data/local/ThemeRepository.kt` | Migración legacy en IO |
| `app/src/main/java/com/mediatracker/di/NetworkModule.kt` | Caché HTTP compartida + interceptor de `Cache-Control` |
| `app/src/test/java/com/mediatracker/di/NetworkCacheTest.kt` (nuevo) | Test de caché |
| `app/src/main/java/com/mediatracker/presentation/fancard/FanCardViewModel.kt` | Eliminar `!!`, extraer `averageRating` |
| `app/src/test/java/com/mediatracker/presentation/fancard/FanCardRatingTest.kt` (nuevo) | Test de `averageRating` |
| `app/src/main/java/com/mediatracker/presentation/csvimport/ImportScreen.kt` | Key de lista + strings a recursos |
| `app/src/main/java/com/mediatracker/presentation/profile/ProfileScreen.kt` | Key de avatares + extracción de secciones |
| `app/src/main/java/com/mediatracker/presentation/detail/components/DetailComponents.kt` (nuevo) | Componentes extraídos de Detail |
| `app/src/main/java/com/mediatracker/presentation/profile/components/ProfileSections.kt` (nuevo) | Secciones extraídas de Profile |
| `app/src/main/java/com/mediatracker/presentation/home/components/HomeComponents.kt` (nuevo) | Componentes extraídos de Home |
| `app/src/main/java/com/mediatracker/presentation/components/{MediaCard,FanCardScreen,FloatingBottomNav}.kt` | Sustituir colores literales por tokens |
| `app/src/main/res/values/strings.xml`, `values-es/strings.xml` | Nuevas claves EN/ES |

---

# FASE A — Código, bugs y rendimiento

### Task A1: Línea base verificable

**Files:**
- Create: `docs/quality-baseline-2026-11-17.md`

- [ ] **Step 1: Verificar entorno y estado de git**

Run: `java -version && echo "JAVA_HOME=$JAVA_HOME" && git -C /home/hugo/Dev/FanApp status --short && git -C /home/hugo/Dev/FanApp branch --show-current`
Expected: rama `main`, sin cambios pendientes (o anotar los cambios ajenos y no tocarlos). Si existe otra sesión en el repo, crear worktree propio antes de continuar.

- [ ] **Step 2: Compilar**

Run: `cd /home/hugo/Dev/FanApp && ./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL. Si falla, copiar el primer error en el documento de línea base y **parar** hasta que Hugo decida si se arregla aquí o se documenta como preexistente.

- [ ] **Step 3: Tests unitarios**

Run: `./gradlew :app:testDebugUnitTest`
Expected: BUILD SUCCESSFUL; anotar número de tests y fallos.

- [ ] **Step 4: Lint**

Run: `./gradlew :app:lint`
Expected: BUILD SUCCESSFUL; anotar número de errores y warnings. No corregir nada todavía: esto es solo la referencia.

- [ ] **Step 5: Escribir la línea base**

Crear `docs/quality-baseline-2026-11-17.md` con: versión de Java/JVM, rama y commit (`git rev-parse --short HEAD`), salida resumida de los tres comandos, y la lista de fallos/avisos preexistentes que se aceptan como estado inicial.

- [ ] **Step 6: Commit**

```bash
git add -f docs/quality-baseline-2026-11-17.md
git commit -m "docs: línea base de calidad 2026-11-17"
```

---

### Task A2: StrictMode en debug y migración de tema fuera del main thread

**Files:**
- Modify: `app/src/main/java/com/mediatracker/MediaTrackerApp.kt:14-22`
- Modify: `app/src/main/java/com/mediatracker/data/local/ThemeRepository.kt:57-68`

**Interfaces:**
- Consumes: `BuildConfig.DEBUG`, `ThemeRepository.migrateFromSharedPreferences()`
- Produces: `MediaTrackerApp` implementa `android.app.Application` con detección de disco en main solo en debug; `migrateFromSharedPreferences()` pasa a ser asíncrona (no bloquea `onCreate`).

- [ ] **Step 1: Añadir StrictMode en debug**

En `MediaTrackerApp.kt`, añadir `import android.os.StrictMode` y dentro del bloque `if (BuildConfig.DEBUG)` de `onCreate()`, antes de `Timber.plant`:

```kotlin
StrictMode.setThreadPolicy(
    StrictMode.ThreadPolicy.Builder()
        .detectDiskReads()
        .detectDiskWrites()
        .penaltyLog()
        .build(),
)
```

- [ ] **Step 2: Mover la migración de SharedPreferences a IO**

Sustituir el cuerpo de `migrateFromSharedPreferences()` en `ThemeRepository.kt` por:

```kotlin
fun migrateFromSharedPreferences() {
    migrationScope.launch {
        runCatching {
            val legacy = context.getSharedPreferences(PREFS_LEGACY, Context.MODE_PRIVATE)
            val saved = legacy.getString(KEY_LEGACY, null) ?: return@launch
            val theme = AppTheme.entries.find { it.name == saved } ?: return@launch
            legacy.edit().remove(KEY_LEGACY).apply()
            setAppTheme(theme)
        }.onFailure { Timber.e(it, "Theme migration failed") }
    }
}
```

Esto elimina la lectura síncrona de `SharedPreferences` en `MainActivity.onCreate` (`MainActivity.kt:43`) sin cambiar la firma pública del método.

- [ ] **Step 3: Verificar**

Run: `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest`
Expected: BUILD SUCCESSFUL, mismos tests que en A1.
Además, QA manual en debug: filtrar logcat por `StrictMode` tras una instalación fresca; no debe aparecer `SharedPreferences` en main thread.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/mediatracker/MediaTrackerApp.kt app/src/main/java/com/mediatracker/data/local/ThemeRepository.kt
git commit -m "fix(startup): migración de tema en IO y StrictMode en debug"
```

---

### Task A3: Eliminar el `!!` de FanCard con test

**Files:**
- Modify: `app/src/main/java/com/mediatracker/presentation/fancard/FanCardViewModel.kt:76-80`
- Test: `app/src/test/java/com/mediatracker/presentation/fancard/FanCardRatingTest.kt` (nuevo)

**Interfaces:**
- Consumes: `UserItem` (`domain/model`), campo `userRating: Int?`
- Produces: `internal fun averageRating(items: List<UserItem>): Float` — devuelve `0f` si no hay valoraciones.

- [ ] **Step 1: Escribir el test que falla**

Crear `app/src/test/java/com/mediatracker/presentation/fancard/FanCardRatingTest.kt`:

```kotlin
package com.mediatracker.presentation.fancard

import com.mediatracker.domain.model.ItemStatus
import com.mediatracker.domain.model.MediaType
import com.mediatracker.domain.model.UserItem
import org.junit.Assert.assertEquals
import org.junit.Test

class FanCardRatingTest {

    private fun item(rating: Int?) = UserItem(
        id = "x", mediaType = MediaType.MOVIE, apiId = "1",
        title = "Título", posterUrl = null,
        status = ItemStatus.COMPLETED, favorite = false,
        addedAt = 0L, updatedAt = 0L, userRating = rating,
    )

    @Test
    fun `averageRating ignora items sin valorar`() {
        assertEquals(4f, averageRating(listOf(item(3), item(5), item(null))), 0.001f)
    }

    @Test
    fun `averageRating devuelve cero sin valoraciones`() {
        assertEquals(0f, averageRating(listOf(item(null))), 0.001f)
    }

    @Test
    fun `averageRating devuelve cero con lista vacia`() {
        assertEquals(0f, averageRating(emptyList()), 0.001f)
    }
}
```

- [ ] **Step 2: Verificar que falla**

Run: `./gradlew :app:testDebugUnitTest --tests '*FanCardRatingTest'`
Expected: FAIL de compilación — `Unresolved reference: averageRating`.

- [ ] **Step 3: Extraer la función y usarla**

En `FanCardViewModel.kt`, reemplazar la línea 80:

```kotlin
val avg = averageRating(completed)
```

Y añadir al final del fichero, fuera de la clase:

```kotlin
internal fun averageRating(items: List<UserItem>): Float =
    items.mapNotNull { it.userRating }
        .takeIf { it.isNotEmpty() }
        ?.average()
        ?.toFloat()
        ?: 0f
```

Añadir el import `com.mediatracker.domain.model.UserItem` si falta. Queda **cero** `!!` en `app/src/main`.

- [ ] **Step 4: Verificar que pasa**

Run: `./gradlew :app:testDebugUnitTest --tests '*FanCardRatingTest' && ./gradlew :app:compileDebugKotlin`
Expected: 3 tests PASS y compilación OK.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/mediatracker/presentation/fancard/FanCardViewModel.kt app/src/test/java/com/mediatracker/presentation/fancard/FanCardRatingTest.kt
git commit -m "refactor(fancard): elimina !! y extrae averageRating con tests"
```

---

### Task A4: Caché HTTP en los tres clientes

**Files:**
- Modify: `app/src/main/java/com/mediatracker/di/NetworkModule.kt:31-86`
- Test: `app/src/test/java/com/mediatracker/di/NetworkCacheTest.kt` (nuevo)

**Interfaces:**
- Consumes: `@ApplicationContext Context` de Hilt.
- Produces: `NetworkModule.buildCache(directory: File): Cache` (10 MiB) y `NetworkModule.CACHE_SIZE_BYTES`; los clientes `@Named("tmdb")`, `@Named("books")` y `@Named("openLibrary")` pasan a tener `.cache(...)`.

- [ ] **Step 1: Escribir el test que falla**

Crear `app/src/test/java/com/mediatracker/di/NetworkCacheTest.kt`:

```kotlin
package com.mediatracker.di

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class NetworkCacheTest {

    @Test
    fun `buildCache usa diez MiB`() {
        val dir = File(System.getProperty("java.io.tmpdir"), "fanapp-cache-test")
        dir.mkdirs()
        val cache = NetworkModule.buildCache(dir)
        assertEquals(NetworkModule.CACHE_SIZE_BYTES, cache.maxSize())
        cache.evictAll()
    }
}
```

- [ ] **Step 2: Verificar que falla**

Run: `./gradlew :app:testDebugUnitTest --tests '*NetworkCacheTest'`
Expected: FAIL de compilación — `Unresolved reference: buildCache`.

- [ ] **Step 3: Implementar la caché**

En `NetworkModule.kt`, añadir imports: `android.content.Context`, `dagger.hilt.android.qualifiers.ApplicationContext`, `okhttp3.Cache`, `java.io.File`. Añadir dentro del `object`:

```kotlin
const val CACHE_SIZE_BYTES = 10L * 1024 * 1024

fun buildCache(directory: File): Cache = Cache(directory, CACHE_SIZE_BYTES)

@Provides
@Singleton
fun provideHttpCache(@ApplicationContext context: Context): Cache =
    buildCache(context.cacheDir.resolve("http_cache"))

private val cacheControlInterceptor = Interceptor { chain ->
    val response = chain.proceed(chain.request())
    if (chain.request().method == "GET" &&
        response.code == 200 &&
        response.header("Cache-Control") == null
    ) {
        response.newBuilder()
            .header("Cache-Control", "public, max-age=3600")
            .build()
    } else {
        response
    }
}
```

Añadir el parámetro `cache: Cache` a `provideTmdbOkHttpClient`, `provideBooksOkHttpClient` y `provideOpenLibraryOkHttpClient`, y encadenar en los tres builders:

```kotlin
        .addInterceptor(cacheControlInterceptor)
        .cache(cache)
```

Colocar `.addInterceptor(cacheControlInterceptor)` **después** de los interceptores existentes en cada cliente. No cachear respuestas con `Cache-Control` propio ni errores.

- [ ] **Step 4: Verificar que pasa**

Run: `./gradlew :app:testDebugUnitTest --tests '*NetworkCacheTest' && ./gradlew :app:compileDebugKotlin`
Expected: test PASS, compilación OK.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/mediatracker/di/NetworkModule.kt app/src/test/java/com/mediatracker/di/NetworkCacheTest.kt
git commit -m "perf(network): caché HTTP de 10 MiB para TMDB, Books y Open Library"
```

---

### Task A5: ImageLoader compartida y keys estables en listas

**Files:**
- Modify: `app/src/main/java/com/mediatracker/MediaTrackerApp.kt:14`
- Modify: `app/src/main/java/com/mediatracker/presentation/csvimport/ImportScreen.kt:343`
- Modify: `app/src/main/java/com/mediatracker/presentation/profile/ProfileScreen.kt:474`

**Interfaces:**
- Consumes: `MediaTrackerApp` de Task A2 (mismo `onCreate`).
- Produces: `ImageLoaderFactory` global de Coil; `errors: List<String>` y `AVATARS: List<String>` con keys por valor.

- [ ] **Step 1: Configurar Coil**

En `MediaTrackerApp.kt` cambiar la declaración e implementar el factory:

```kotlin
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache

class MediaTrackerApp : Application(), ImageLoaderFactory {
    // ... onCreate existente ...

    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .crossfade(true)
        .memoryCache { MemoryCache.Builder(this).maxSizePercent(0.25).build() }
        .diskCache {
            DiskCache.Builder()
                .directory(cacheDir.resolve("image_cache"))
                .maxSizeBytes(64L * 1024 * 1024)
                .build()
        }
        .build()
}
```

No tocar las llamadas `AsyncImage` individuales: heredan el `ImageLoader` global.

- [ ] **Step 2: Añadir keys**

En `ImportScreen.kt:343`:

```kotlin
items(result.errors, key = { it }) { error ->
```

En `ProfileScreen.kt:474`:

```kotlin
items(AVATARS, key = { it }) { emoji ->
```

- [ ] **Step 3: Verificar**

Run: `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest`
Expected: BUILD SUCCESSFUL. QA manual: abrir Importar con errores y el picker de avatar, cambiar el estado de un ítem de la biblioteca y comprobar que la card no salta.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/mediatracker/MediaTrackerApp.kt app/src/main/java/com/mediatracker/presentation/csvimport/ImportScreen.kt app/src/main/java/com/mediatracker/presentation/profile/ProfileScreen.kt
git commit -m "perf(ui): ImageLoader compartida y keys en listas dinámicas"
```

---

### Task A6: Extraer componentes de DetailScreen

**Files:**
- Create: `app/src/main/java/com/mediatracker/presentation/detail/components/DetailComponents.kt`
- Modify: `app/src/main/java/com/mediatracker/presentation/detail/DetailScreen.kt:435-723`

- [ ] **Step 1: Crear el archivo de components**

Mover a `presentation/detail/components/DetailComponents.kt` (con el paquete `com.mediatracker.presentation.detail.components` y los imports que exijan) estas funciones privadas, **sin cambiar ni una línea de su lógica**: `HeroBadge` (435), `ExtraInfo` (450), `StarRatingBar` (477), `NotesField` (509), `SeasonEpisodeStepper` (532), `GoToPageDialog` (570), `PageProgressStepper` (604), `StepperControl` (672). Cambiar su visibilidad a `internal` para poder llamarlas desde `DetailScreen.kt`.

- [ ] **Step 2: Actualizar DetailScreen**

Eliminar esas funciones de `DetailScreen.kt` y añadir `import com.mediatracker.presentation.detail.components.*` (o imports explícitos). `DetailContent` queda reducido a la composición de secciones. No tocar el hero ni los `GlassSurface` de julio.

- [ ] **Step 3: Verificar**

Run: `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest --tests '*DetailViewModelTest'`
Expected: BUILD SUCCESSFUL. QA manual: abrir detalle de serie, libro y película; mismas cards, mismos steppers.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/mediatracker/presentation/detail/components/DetailComponents.kt app/src/main/java/com/mediatracker/presentation/detail/DetailScreen.kt
git commit -m "refactor(detail): extrae componentes de DetailScreen"
```

---

### Task A7: Extraer secciones de ProfileScreen

**Files:**
- Create: `app/src/main/java/com/mediatracker/presentation/profile/components/ProfileSections.kt`
- Modify: `app/src/main/java/com/mediatracker/presentation/profile/ProfileScreen.kt:57-530`

- [ ] **Step 1: Crear el archivo de secciones**

Extraer a `presentation/profile/components/ProfileSections.kt` los bloques marcados en el propio fichero, sin cambiar lógica: banner (`// ── Profile Banner`, línea 101), stats tiles (línea 196), bloque de ajustes (línea 345) y los diálogos de editar nombre (437) y elegir avatar (463). Dejar `SettingRow` (552) y `LanguageOption` (531) en el nuevo archivo también, ya que solo los usa el bloque de ajustes.

- [ ] **Step 2: Reducir ProfileScreen**

`ProfileScreen` (58-530) queda como orquestador: `Column` + llamadas a las secciones extraídas. `AVATARS` (55) se mueve al nuevo archivo si solo lo usan los diálogos; si lo usa la lista de la línea 474, se exported como `internal val AVATARS` desde ahí.

- [ ] **Step 3: Verificar**

Run: `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest`
Expected: BUILD SUCCESSFUL. QA manual: Perfil completo + los 4 diálogos; comparar con captura previa.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/mediatracker/presentation/profile/components/ProfileSections.kt app/src/main/java/com/mediatracker/presentation/profile/ProfileScreen.kt
git commit -m "refactor(profile): extrae secciones de ProfileScreen"
```

---

### Task A8: Extraer componentes de HomeScreen

**Files:**
- Create: `app/src/main/java/com/mediatracker/presentation/home/components/HomeComponents.kt`
- Modify: `app/src/main/java/com/mediatracker/presentation/home/HomeScreen.kt:202-535`

- [ ] **Step 1: Crear el archivo de components**

Mover a `presentation/home/components/HomeComponents.kt`, sin cambiar lógica: `HomeHeader` (202), `GreetingLine` (267), `FeaturedCard` (311), `SectionHeader` (418), `ContinueRow` (452), `ContinueCard` (467), `HomeLoadingSkeleton` (536), `SectionHeaderSkeleton` (561).

- [ ] **Step 2: Actualizar HomeScreen**

`HomeScreenContent` (108) pasa a componer esos componentes. Previews (576+) se dejan en `HomeScreen.kt` o se mueven al nuevo archivo si da error de visibilidad.

- [ ] **Step 3: Verificar**

Run: `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest`
Expected: BUILD SUCCESSFUL. QA manual: Home con y sin datos, con racha y sin racha, skeleton de carga.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/mediatracker/presentation/home/components/HomeComponents.kt app/src/main/java/com/mediatracker/presentation/home/HomeScreen.kt
git commit -m "refactor(home): extrae componentes de HomeScreen"
```

---

### Task A9: Cierre de Fase A

- [ ] **Step 1: Comprobaciones de reglas**

Run: `cd /home/hugo/Dev/FanApp && echo "!! -> $(grep -rIn '!!' app/src/main --include='*.kt' | wc -l)" && echo "println/Log -> $(grep -rInE 'println\(|Log\.[dwvei]\(' app/src/main --include='*.kt' | wc -l)" && echo "TODO -> $(grep -rInE 'TODO|FIXME' app/src/main --include='*.kt' | wc -l)"`
Expected: `0`, `0`, `0`.

- [ ] **Step 2: Verificación completa**

Run: `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest :app:lint`
Expected: BUILD SUCCESSFUL; comparar warnings de lint con `docs/quality-baseline-2026-11-17.md`. Cualquier warning nuevo se corrige antes de pasar a Fase B.

- [ ] **Step 3: Actualizar CHANGELOG**

Añadir sección `## Pulido de código (2026-11-17)` con los cambios de A1–A8 y la referencia al spec.

```bash
git add CHANGELOG.md
git commit -m "docs: cierre fase A — código, bugs y rendimiento"
```

---

### Task A10: Corregir los 12 errores de lint preexistentes

> Añadida durante la ejecución (ruling de A1): la línea base reveló 12 errores de
> lint que el plan original no contemplaba. El spec exige lint en verde, así que
> se corrigen antes del cierre de Fase A.

**Files:**
- Modify: `app/src/main/java/com/mediatracker/presentation/widget/FanAppWidget.kt:62,72,77,95,105,110,116,121`
- Modify: `app/src/main/java/com/mediatracker/presentation/auth/LoginScreen.kt:285`
- Modify: `app/src/main/java/com/mediatracker/presentation/navigation/AppNavGraph.kt:165`

- [ ] **Step 1: Glance — dp explícitos y supresión de RestrictedApi**

En `FanAppWidget.kt`, sustituir los literales numéricos de `padding`/`height` por
`Dp`: `.padding(16)` → `.padding(16.dp)`, `.height(8)` → `.height(8.dp)`,
`.padding(12)` → `.padding(12.dp)`, `.height(6)` → `.height(6.dp)`,
`.height(4)` → `.height(4.dp)`. Añadir `import androidx.compose.ui.unit.dp`.

Añadir `@SuppressLint("RestrictedApi")` encima de `EmptyState` (línea 60) y de
`ItemContent` (línea 85) con `import android.annotation.SuppressLint`. Esto cubre
los 3 errores `RestrictedApi` de `ColorProvider` (77, 110, 121) y los 2
`ResourceType` de los colores RGB (77, 110). **No cambiar ningún color ni layout.**

- [ ] **Step 2: LoginScreen — `stringResource` en vez de `context.getString`**

En `LoginScreen.kt`, declarar `val webClientId = stringResource(R.string.default_web_client_id)`
justo antes del `OutlinedButton` (línea 282) y usar `.requestIdToken(webClientId)`
en el `onClick`. Añadir `import androidx.compose.ui.res.stringResource` si falta.
`stringResource` no se puede llamar dentro del lambda `onClick` porque es composable.

- [ ] **Step 3: AppNavGraph — consumir el padding del Scaffold**

En `AppNavGraph.kt:165`, cambiar `) { _ ->` por `) { innerPadding ->` y aplicar
`Modifier.padding(innerPadding)` al `Box` de la línea 166. Como
`contentWindowInsets = WindowInsets(0, 0, 0, 0)`, el padding es cero y el layout
no cambia: **no tocar los `navigationBarsPadding` ni los insets del NavHost**.

- [ ] **Step 4: Verificar lint en verde**

Run: `cd /home/hugo/Dev/FanApp && ./gradlew :app:lint 2>&1 | tail -8`
Expected: BUILD SUCCESSFUL (pueden quedar los 99 avisos preexistentes, pero 0 errores).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/mediatracker/presentation/widget/FanAppWidget.kt app/src/main/java/com/mediatracker/presentation/auth/LoginScreen.kt app/src/main/java/com/mediatracker/presentation/navigation/AppNavGraph.kt
git commit -m "fix(lint): corrige los 12 errores preexistentes de lint"
```

---

# FASE B — Revisión y pulido de diseño

### Task B1: Auditoría visual en los 4 temas

**Files:**
- Create: `docs/design-audit-2026-11-17.md`

- [ ] **Step 1: Inventario automatizado**

Run: `cd /home/hugo/Dev/FanApp && grep -rIn 'Color(0x' app/src/main --include='*.kt' | grep -v '/theme/' > /tmp/opencode/design-colors.txt && grep -rInE 'Text\("' app/src/main --include='*.kt' > /tmp/opencode/design-hardcoded-strings.txt && grep -rcE '[0-9]+\.dp' app/src/main --include='*.kt' | sort -t: -k2 -rn > /tmp/opencode/design-dp.txt`
Expected: tres listas. Añadirlas como apéndice al documento de auditoría.

- [ ] **Step 2: Capturas por tema**

Para cada uno de los 4 temas (Fantasy, Light, Purple, Dark), capturar en dispositivo/emulador: Home, Descubrir, Biblioteca, Detalle, Perfil, Importar y Ajustes. Adjuntar las capturas a `docs/` con nombre `audit_<tema>_<pantalla>.png` y referenciarlas en el documento.

- [ ] **Step 3: Registrar hallazgos**

En `docs/design-audit-2026-11-17.md`, clasificar cada hallazgo como P1 (inconsistencia visible/contraste), P2 (espaciado/tipografía) o P3 (detalle menor), con archivo:línea y captura de referencia. Incluir al menos los ya detectados: gradientes de `MediaCard.kt:47-58`, estrellas de `FanCardScreen.kt:532,556`, colores de `FloatingBottomNav.kt:54,56`, botón atrás de 38 dp en `GlassBackHeader.kt:47` y strings de `ImportScreen.kt`.

- [ ] **Step 4: Commit**

```bash
git add -f docs/design-audit-2026-11-17.md docs/audit_*.png
git commit -m "docs: auditoría visual de diseño en 4 temas"
```

---

### Task B2: Tokenizar los colores literales

**Files:**
- Modify: `app/src/main/java/com/mediatracker/presentation/theme/Color.kt`
- Modify: `app/src/main/java/com/mediatracker/presentation/theme/Theme.kt`
- Modify: `app/src/main/java/com/mediatracker/presentation/components/MediaCard.kt:47-58`
- Modify: `app/src/main/java/com/mediatracker/presentation/fancard/FanCardScreen.kt:532,556`
- Modify: `app/src/main/java/com/mediatracker/presentation/navigation/FloatingBottomNav.kt:54,56`

**Interfaces:**
- Consumes: `FanAppColors` (4 temas), que **ya tiene** `gradient1`, `gradient2`, `gradientAccent`, `sparkleColor` y `navBarBg` (`Theme.kt:38-47`). Reutilizarlos antes que crear tokens nuevos.
- Produces: solo dos tokens nuevos en `FanAppColors`: `posterGradients: List<List<Color>>` y `navScrim: Color`, con valores por tema.

- [ ] **Step 1: Añadir los dos tokens al sistema**

En `FanAppColors` (`Theme.kt:38-47`) añadir únicamente `posterGradients` y `navScrim`. En `Color.kt` migrar los 12 pares de `POSTER_GRADIENTS` a una constante por tema (p. ej. `FantasyPosterGradients`, `LightPosterGradients`, …) reutilizando los colores ya definidos en cada tema siempre que el resultado sea equivalente. `navScrim` = `Color(0x80000000)` para los 4 temas (es una sombra neutra, no depende del tema). Rellenar las cuatro instancias de `FanAppColors` (Fantasy ~184, Light ~216, Purple ~238, Dark).

- [ ] **Step 2: Sustituir usos**

`MediaCard.kt`: borrar la constante `POSTER_GRADIENTS` (46-58) y usar `MaterialTheme.fanAppColors.posterGradients` en `gradientColorFor` (64) y `GradientPoster` (75). `FanCardScreen.kt:532,556`: `Color(0xFFFFC107)` → `MaterialTheme.fanAppColors.gradientAccent.first()`, igual que ya hace `LibraryItemCard.kt:206`. `FloatingBottomNav.kt:54`: literal → `fanColors.navScrim`; línea 56: → `fanColors.gradientAccent.first().copy(alpha = 0.19f)` (equivalente a `0x2E` / 255 ≈ 0.18). **No cambiar ningún modifier ni layout de la nav.**

- [ ] **Step 3: Verificar**

Run: `cd /home/hugo/Dev/FanApp && ./gradlew :app:compileDebugKotlin && echo "literales fuera de theme: $(grep -rIn 'Color(0x' app/src/main --include='*.kt' | grep -v '/theme/' | wc -l)"`
Expected: compila y el contador es `0`. Comparar las capturas de B1 en los 4 temas: mismos colores y contraste, sin regresiones.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/mediatracker/presentation/theme/Color.kt app/src/main/java/com/mediatracker/presentation/theme/Theme.kt app/src/main/java/com/mediatracker/presentation/components/MediaCard.kt app/src/main/java/com/mediatracker/presentation/fancard/FanCardScreen.kt app/src/main/java/com/mediatracker/presentation/navigation/FloatingBottomNav.kt
git commit -m "style(theme): colores de cards, estrellas y nav pasan a tokens"
```

---

### Task B3: Localizar los strings hardcodeados

**Files:**
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-es/strings.xml`
- Modify: `app/src/main/java/com/mediatracker/presentation/csvimport/ImportScreen.kt:116,245,252,365,372`

Los `Text("⭐")` de `MediaCard.kt:231`, `Text("✨"/"🔥"/"⚡"/"⚠️")` de `ProfileScreen.kt`, `Text("🌙")` de `ThemeScreen.kt` y `Text("↩ ...")` de `ProfileScreen.kt:431` son emoji o combinaciones de emoji con recursos ya localizados: **no se tocan**.

- [ ] **Step 1: Añadir claves en ambos idiomas**

Con el mismo nombre en `values/strings.xml` (EN) y `values-es/strings.xml` (ES):

```xml
<string name="common_ok">OK</string>
<string name="import_cancel">Cancelar</string>
<string name="import_import_items">Importar %1$d items</string>
<string name="import_more">Importar más</string>
<string name="import_done">Hecho</string>
```

`common_ok` → `OK` / `OK`; `import_cancel` → `Cancel` / `Cancelar`; `import_import_items` → `Import %1$d items` / `Importar %1$d items`; `import_more` → `Import more` / `Importar más`; `import_done` → `Done` / `Hecho`.

- [ ] **Step 2: Sustituir en los composables**

`ImportScreen.kt`: `Text("OK")` → `Text(stringResource(R.string.common_ok))`, `Text("Cancelar")` → `R.string.import_cancel`, `Text("Importar ${preview.totalItems} items")` → `Text(stringResource(R.string.import_import_items, preview.totalItems))`, `Text("Importar mas")` → `R.string.import_more`, `Text("Hecho")` → `R.string.import_done`. No hay más texto literal traducible en composables: el resto son emoji.

- [ ] **Step 3: Verificar claves pareadas**

Run: `cd /home/hugo/Dev/FanApp && diff <(grep -o 'name="[^"]*"' app/src/main/res/values/strings.xml | sort) <(grep -o 'name="[^"]*"' app/src/main/res/values-es/strings.xml | sort)`
Expected: sin salida (mismo conjunto de claves). Luego `./gradlew :app:compileDebugKotlin`.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/values/strings.xml app/src/main/res/values-es/strings.xml app/src/main/java/com/mediatracker/presentation/csvimport/ImportScreen.kt
git commit -m "i18n: strings de Importar pasan a recursos EN/ES"
```

---

### Task B4: Objetivos táctiles y jerarquía visual

**Files:**
- Modify: `app/src/main/java/com/mediatracker/presentation/components/GlassBackHeader.kt:47`
- Modify: `app/src/main/java/com/mediatracker/presentation/components/StatusChip.kt`
- Modify: `app/src/main/java/com/mediatracker/presentation/components/FavoriteToggle.kt`
- Modificar solo los archivos que B1 marque como P1/P2.

- [ ] **Step 1: Botón atrás de 48 dp**

En `GlassBackHeader.kt:47`, cambiar `.size(38.dp)` por `.size(48.dp)` y el icono de `20.dp` a `24.dp` para mantener proporción. No tocar paddings del `Row` ni el título.

- [ ] **Step 2: Revisar el resto de controles**

Para cada control interactivo listado en B1 como P1, comprobar que su área clicable es ≥ 48 dp. Si un `GlassSurface` con `clickable` mide menos, añadir `.sizeIn(minWidth = 48.dp, minHeight = 48.dp)` al modifier del elemento clicable, no al contenedor padre. Ajustar espaciados de la lista P2 de B1 usando multiples de 4 dp ya presentes en el fichero (4/8/12/16/20/24), sin introducir valores nuevos.

- [ ] **Step 3: Verificar**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL. QA manual: recorrer las pantallas de B1 en los 4 temas y comparar con las capturas de referencia; el botón atrás debe seguir centrado y sin recortes.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/mediatracker/presentation/components/GlassBackHeader.kt app/src/main/java/com/mediatracker/presentation/components/StatusChip.kt app/src/main/java/com/mediatracker/presentation/components/FavoriteToggle.kt
git commit -m "style(a11y): objetivos tactiles de 48 dp y espaciados coherentes"
```

---

### Task B5: Verificación final y cierre

- [ ] **Step 1: Verificación completa**

Run: `cd /home/hugo/Dev/FanApp && ./gradlew :app:compileDebugKotlin :app:testDebugUnitTest :app:lint`
Expected: BUILD SUCCESSFUL y sin warnings nuevos respecto a la línea base.

- [ ] **Step 2: Auditoría de reglas**

Run: `echo "color literal fuera de theme: $(grep -rIn 'Color(0x' app/src/main --include='*.kt' | grep -v '/theme/' | wc -l)" && echo "!!: $(grep -rIn '!!' app/src/main --include='*.kt' | wc -l)" && diff <(grep -o 'name="[^"]*"' app/src/main/res/values/strings.xml | sort) <(grep -o 'name="[^"]*"' app/src/main/res/values-es/strings.xml | sort)`
Expected: `0`, `0`, sin diff.

- [ ] **Step 3: Actualizar CHANGELOG y README**

Añadir sección `## Pulido de diseño (2026-11-17)` al CHANGELOG con el resumen de B1–B4 y enlaces a los dos documentos nuevos. No cambiar el porcentaje de avance del README.

- [ ] **Step 4: Commit final**

```bash
git add CHANGELOG.md
git commit -m "docs: cierre fase B —Revision de diseno y pulido"
```

---

## Self-review del plan (comprobado antes de publicar)

- **Cobertura del spec:** los 5 criterios de aceptación tienen tarea: build/test/lint (A1, A9, B5), `!!`/logs/catch (A3, A9), StrictMode (A2), caché HTTP + Coil (A4, A5), colores y strings (B2, B3), 4 temas (B1, B2, B4).
- **Placeholders:** no hay «TBD», «implementar después» ni «añadir manejo de errores» genérico. Las únicas decisiones diferidas son las que B1 documenta con evidencia, y B4 las cierra con reglas concretas.
- **Consistencia de tipos:** `averageRating(List<UserItem>): Float`, `buildCache(File): Cache`, `CACHE_SIZE_BYTES`, `posterGradients` y `navScrim` se definen antes de usarse y conservan el mismo nombre en todas las tareas; el resto reutiliza tokens existentes (`gradientAccent`, `gradient1`, `gradient2`, `navBarBg`, `sparkleColor`).
- **Review Focus:** las cinco condiciones están asignadas a A2, A4, A5, B3 y B2 respectivamente.
