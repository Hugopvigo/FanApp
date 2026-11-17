# Arreglos rápidos Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development o superpowers:executing-plans para ejecutar este plan tarea a tarea.

**Goal:** Corregir cuatro defectos concretos detectados en la revision: init manual de Firebase, backoff bloqueante en Google Books, parpadeo de onboarding y ficheros sueltos en el repo.

**Architecture:** Se elimina el init manual de Firebase (el plugin de Google Services ya lo hace), se sustituye el `Thread.sleep` del interceptor por reintentos `suspend` en la capa de repositorio, y se introduce un estado nullable para no composear hasta conocer el estado de onboarding.

**Tech Stack:** Kotlin coroutines, OkHttp 5.3.2, Retrofit 3, Firebase Auth/Firestore, Hilt.

**Spec:** `docs/superpowers/specs/2026-11-17-next-improvements.md`

## Global Constraints

- No cambiar versionCode/versionName (ya subio a 2 / 1.0.1).
- No tocar la arquitectura de navegacion/insets.
- `AGENT.md`: cero `!!`, Timber, funciones < 40 lineas, UI -> domain -> data.
- Verificar en dispositivo login y Firestore tras quitar el init manual de Firebase.

---

### Task Q1: Eliminar el init manual de Firebase

**Files:**
- Modify: `app/src/main/java/com/mediatracker/MediaTrackerApp.kt`

- [ ] **Step 1: Quitar el metodo y su llamada**

En `onCreate()`, eliminar la linea `initializeFirebaseIfNeeded()`. Eliminar el metodo `initializeFirebaseIfNeeded()` completo (declara `FirebaseApp`, `FirebaseOptions` y `BuildConfig.TMDB_API_KEY` como señal de configuracion). Quitar los imports que queden sin uso: `com.google.firebase.FirebaseApp`, `com.google.firebase.FirebaseOptions`, `com.mediatracker.BuildConfig`.

`BuildConfig.DEBUG` sigue usandose para StrictMode y Crashlytics, asi que se mantiene el import de `BuildConfig` si el plan de Crashlytics ya lo anadio; si no, anadirlo.

- [ ] **Step 2: Verificar**

Run: `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest`
Expected: BUILD SUCCESSFUL, 98/98 tests.

- [ ] **Step 3: QA en dispositivo**

Con el movil conectado: `adb install -r app/build/outputs/apk/debug/app-debug.apk`; entrar con Google y con email/password, y comprobar que Perfil carga datos de Firestore. Si Firebase no inicializa, el fallo aparecera en logcat con `FirebaseApp is not initialized`.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/mediatracker/MediaTrackerApp.kt
git commit -m "fix(firebase): elimina init manual redundante y incompleto"
```

---

### Task Q2: Backoff de Google Books sin bloquear hilos

**Files:**
- Modify: `app/src/main/java/com/mediatracker/data/remote/books/GoogleBooksRateLimiter.kt`
- Modify: `app/src/main/java/com/mediatracker/data/repository/MediaRepositoryImpl.kt`
- Test: `app/src/test/java/com/mediatracker/data/remote/books/BooksRetryTest.kt` (nuevo)

**Interfaces:**
- Consumes: `GoogleBooksApi.searchBooks(...)` (suspend, lanza `retrofit2.HttpException` en 429).
- Produces: `internal suspend fun <T> withBooksRetry(delayMs: Long = 2_000L, maxAttempts: Int = 3, block: suspend () -> T): T` en `MediaRepositoryImpl.kt`; reintenta `maxAttempts` veces con `delay(delayMs shl attempt)`. El parametro `delayMs` existe para que los tests usen 0 sin esperar.

- [ ] **Step 1: Escribir el test que falla**

```kotlin
package com.mediatracker.data.remote.books

import com.mediatracker.data.repository.withBooksRetry
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class BooksRetryTest {
    private fun http429(): HttpException =
        HttpException(Response.error<Any>(429, okhttp3.ResponseBody.create(null, "")))

    @Test
    fun `reintenta tras 429 y devuelve el resultado`() = runTest {
        var attempts = 0
        val result = withBooksRetry(delayMs = 0) {
            attempts++
            if (attempts < 3) throw http429() else "ok"
        }
        assertEquals("ok", result)
        assertEquals(3, attempts)
    }

    @Test
    fun `no reintenta errores que no son 429`() = runTest {
        var attempts = 0
        try {
            withBooksRetry(delayMs = 0) {
                attempts++
                throw http429().let { HttpException(Response.error<Any>(500, okhttp3.ResponseBody.create(null, ""))) }
            }
        } catch (_: HttpException) {
            // esperado
        }
        assertEquals(1, attempts)
    }
}
```

- [ ] **Step 2: Verificar que falla**

Run: `./gradlew :app:testDebugUnitTest --tests '*BooksRetryTest'`
Expected: FAIL de compilacion — `Unresolved reference: withBooksRetry`.

- [ ] **Step 3: Implementar el helper**

En `MediaRepositoryImpl.kt`, anadir al final del fichero:

```kotlin
internal suspend fun <T> withBooksRetry(
    delayMs: Long = 2_000L,
    maxAttempts: Int = 3,
    block: suspend () -> T,
): T {
    var attempt = 0
    while (true) {
        try {
            return block()
        } catch (e: retrofit2.HttpException) {
            if (e.code() != 429 || attempt >= maxAttempts) throw e
            kotlinx.coroutines.delay(delayMs shl attempt)
            attempt++
        }
    }
}
```

En `GoogleBooksRateLimiter.kt`: eliminar `rateLimitIfNeeded()`, `retryWithBackoff()` y todos los `Thread.sleep`. El interceptor pasa a ser trivial:

```kotlin
override fun intercept(chain: Interceptor.Chain): Response = chain.proceed(chain.request())
```

Envolver la llamada de `searchBooks()` (linea 100) con `withBooksRetry { ... }`. Hacer lo mismo en `getTrendingBooks()` y en la funcion de detalle de libro si usan la misma API.

- [ ] **Step 4: Verificar**

Run: `./gradlew :app:testDebugUnitTest --tests '*BooksRetryTest' && ./gradlew :app:compileDebugKotlin :app:testDebugUnitTest`
Expected: tests PASS y suite 100/100 (98 + 2 nuevos).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/mediatracker/data/remote/books/GoogleBooksRateLimiter.kt app/src/main/java/com/mediatracker/data/repository/MediaRepositoryImpl.kt app/src/test/java/com/mediatracker/data/remote/books/BooksRetryTest.kt
git commit -m "perf(books): reintentos suspend en vez de Thread.sleep en el interceptor"
```

---

### Task Q3: Evitar el parpadeo de onboarding

**Files:**
- Modify: `app/src/main/java/com/mediatracker/MainActivity.kt`

- [ ] **Step 1: Estado nullable y splash**

Cambiar `var onboardingNeeded by mutableStateOf(false)` por `var onboardingNeeded by mutableStateOf<Boolean?>(null)`.

Dentro del `setContent`, antes de `AppNavGraph`, envolver en:

```kotlin
if (onboardingNeeded == null) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
} else {
    AppNavGraph(onboardingNeeded = onboardingNeeded ?: false, /* ... resto igual */)
}
```

Anadir imports: `androidx.compose.foundation.layout.Box`, `androidx.compose.foundation.layout.fillMaxSize`, `androidx.compose.material3.CircularProgressIndicator`, `androidx.compose.ui.Alignment`.

- [ ] **Step 2: Verificar**

Run: `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest`
Expected: BUILD SUCCESSFUL, 99/99 tests. QA en dispositivo: abrir en frio y comprobar que no aparece Main antes del onboarding en una instalacion fresca.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/mediatracker/MainActivity.kt
git commit -m "fix(startup): espera al estado de onboarding antes de composear"
```

---

### Task Q4: Limpieza

**Files:**
- Delete: `app/src/photo_2026-05-30_12-29-05.jpg` y las otras 3 fotos sueltas en `app/src/`.

- [ ] **Step 1: Borrar los ficheros sueltos**

Run: `cd /home/hugo/Dev/FanApp && git rm 'app/src/photo_2026-05-30_12-29-07 (2).jpg' app/src/photo_2026-05-30_12-29-07.jpg app/src/photo_2026-05-30_12-29-05.jpg app/src/photo_2026-05-30_12-29-08.jpg`
Expected: 4 ficheros eliminados. No estan referenciados en codigo (verificado con grep).

- [ ] **Step 2: Revisar UnusedResources sin borrar a ciegas**

Run: `grep 'UnusedResources' app/build/intermediates/lint_intermediate_text_report/debug/lintReportDebug/lint-results-debug.txt | head -10`
Expected: lista de recursos. **No eliminar** ninguno que pueda usarse por reflexion o por el widget Glance; documentar los obviamente muertos en `docs/quality-baseline-2026-11-17.md`.

- [ ] **Step 3: Verificar y commit**

Run: `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest :app:lint`
Expected: en verde, 99 avisos o menos.

```bash
git add -f docs/quality-baseline-2026-11-17.md
git commit -m "chore: elimina fotos sueltas y documenta recursos no usados"
```
