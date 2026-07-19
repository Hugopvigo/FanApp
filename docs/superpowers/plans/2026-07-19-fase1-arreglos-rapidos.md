# Fase 1 — Arreglos rápidos: Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Búsqueda a 3 columnas en Descubrir, banner de verificación de email que desaparece al verificar, búsqueda de libros con mejor relevancia y deduplicación, y trending de libros basado en popularidad real.

**Architecture:** Cambios quirúrgicos sobre código existente: la rejilla de Descubrir pasa de ancho-adaptativo a 3 columnas fijas; `AuthDataSource` gana un refresco con `user.reload()`; el query builder de Google Books deja de generar frases exactas; la deduplicación se centraliza en un helper testeable en `BooksMapper`; el trending cambia de consultas por género a Open Library `trending/daily`.

**Tech Stack:** Kotlin + Jetpack Compose, Firebase Auth, Retrofit (Google Books / Open Library), JUnit4 + MockK + kotlinx-coroutines-test.

**Spec:** `docs/superpowers/specs/2026-07-19-mejoras-app-design.md`

**Notas de entorno:**
- Compilar SIEMPRE con Java 21 — ya configurado en `gradle.properties` (`org.gradle.java.home`).
- Tests: `./gradlew :app:testDebugUnitTest`. Compilación rápida: `./gradlew :app:compileDebugKotlin`.
- Trabajo directo en `main`, un commit por task.
- Fase 2 (coherencia glass) y Fase 3 (animaciones) tendrán planes propios cuando esta fase esté mergeada — son trabajo visual iterativo, no TDD.

---

### Task 1: Grid de 3 columnas en Descubrir

**Files:**
- Modify: `app/src/main/java/com/mediatracker/presentation/discover/DiscoverScreen.kt`

- [ ] **Step 1: Sustituir `AdaptiveGrid` por `MediaGrid` de columnas fijas**

En `DiscoverScreen.kt` (líneas ~291-325) reemplazar la función completa `AdaptiveGrid` por:

```kotlin
// ─── Fixed-column grid ────────────────────────────────────────────────────────
@Composable
private fun MediaGrid(
    items: List<MediaItem>,
    columns: Int,
    horizontalPadding: Dp,
    spacing: Dp,
    onItemClick: (MediaItem) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(spacing),
    ) {
        items.chunked(columns).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                rowItems.forEach { item ->
                    MediaCard(
                        item = item,
                        onClick = { onItemClick(item) },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(columns - rowItems.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}
```

Desaparecen `BoxWithConstraints`, `maxWidth` y el cálculo de `columns`. Si `BoxWithConstraints` ya no se usa en el fichero, eliminar su import.

- [ ] **Step 2: Actualizar los dos call-sites**

Llamada del trending (línea ~167):

```kotlin
else -> MediaGrid(
    items = state.trending.take(10),
    columns = 3,
    horizontalPadding = 16.dp,
    spacing = 12.dp,
    onItemClick = onItemClick,
)
```

Llamada de resultados de búsqueda (línea ~179):

```kotlin
MediaGrid(
    items = state.searchResults,
    columns = 3,
    horizontalPadding = 16.dp,
    spacing = 12.dp,
    onItemClick = onItemClick,
)
```

- [ ] **Step 3: Compilar**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/mediatracker/presentation/discover/DiscoverScreen.kt
git commit -m "feat(discover): rejilla de 3 columnas fijas como Inicio y Biblioteca"
```

---

### Task 2: Refresco de verificación de email

**Files:**
- Modify: `app/src/main/java/com/mediatracker/data/auth/AuthDataSource.kt` (junto a `isEmailVerified()`, línea ~133)
- Modify: `app/src/main/java/com/mediatracker/presentation/profile/ProfileViewModel.kt:86-88`
- Modify: `app/src/main/java/com/mediatracker/presentation/profile/ProfileScreen.kt:292-329` (banner)
- Modify: `app/src/main/res/values/strings.xml` y `app/src/main/res/values-es/strings.xml` (tras `profile_verify_email_sent`, línea ~273)
- Test: `app/src/test/java/com/mediatracker/data/auth/AuthDataSourceTest.kt`

- [ ] **Step 1: Escribir tests que fallan en `AuthDataSourceTest`**

Añadir imports al fichero de test:

```kotlin
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseUser
import io.mockk.verify
import kotlinx.coroutines.test.runTest
```

Añadir tests:

```kotlin
@Test
fun `refreshEmailVerified returns false when no user`() = runTest {
    every { auth.currentUser } returns null

    assertFalse(dataSource.refreshEmailVerified())
}

@Test
fun `refreshEmailVerified reloads user and returns fresh flag`() = runTest {
    val user: FirebaseUser = mockk {
        every { reload() } returns Tasks.forResult(null)
        every { isEmailVerified } returns true
    }
    every { auth.currentUser } returns user

    assertTrue(dataSource.refreshEmailVerified())
    verify { user.reload() }
}

@Test
fun `refreshEmailVerified survives reload failure and returns cached flag`() = runTest {
    val user: FirebaseUser = mockk {
        every { reload() } returns Tasks.forException(RuntimeException("network"))
        every { isEmailVerified } returns false
    }
    every { auth.currentUser } returns user

    assertFalse(dataSource.refreshEmailVerified())
}
```

- [ ] **Step 2: Verificar que fallan (no compilan por método inexistente)**

Run: `./gradlew :app:testDebugUnitTest --tests "com.mediatracker.data.auth.AuthDataSourceTest"`
Expected: FAIL — `unresolved reference: refreshEmailVerified`

- [ ] **Step 3: Implementar `refreshEmailVerified` en `AuthDataSource`**

Debajo de `isEmailVerified()` (línea ~133):

```kotlin
suspend fun refreshEmailVerified(): Boolean {
    val firebaseAuth = auth ?: return false
    val user = firebaseAuth.currentUser ?: return false
    // reload() puede fallar sin red; en ese caso devolvemos el flag cacheado
    runCatching { user.reload().await() }
        .onFailure { Timber.w(it, "Email verification reload failed") }
    return firebaseAuth.currentUser?.isEmailVerified ?: false
}
```

- [ ] **Step 4: Verificar que los tests pasan**

Run: `./gradlew :app:testDebugUnitTest --tests "com.mediatracker.data.auth.AuthDataSourceTest"`
Expected: PASS (todos, incluidos los preexistentes)

- [ ] **Step 5: Usar el refresco en `ProfileViewModel`**

Reemplazar (líneas 86-88):

```kotlin
fun refreshEmailVerificationStatus() {
    _isEmailVerified.value = authDataSource.isEmailVerified()
}
```

por:

```kotlin
fun refreshEmailVerificationStatus() {
    viewModelScope.launch {
        _isEmailVerified.value = authDataSource.refreshEmailVerified()
    }
}
```

(El `init` ya llama a `refreshEmailVerificationStatus()`; no cambia.)

- [ ] **Step 6: Añadir strings del botón "Ya lo he verificado"**

`values/strings.xml`, tras `profile_verify_email_sent`:

```xml
<string name="profile_verify_email_check">I\'ve verified it</string>
```

`values-es/strings.xml`, misma posición:

```xml
<string name="profile_verify_email_check">Ya lo he verificado</string>
```

- [ ] **Step 7: Reestructurar el banner en `ProfileScreen`**

Reemplazar el bloque del banner (líneas 292-329) por una `Column` con los textos arriba y los botones debajo (evita overflow horizontal con dos botones):

```kotlin
if (!isEmailVerified && userEmail != null) {
    Spacer(Modifier.height(12.dp))
    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        radius = 18.dp,
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("⚠️", fontSize = 20.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.profile_verify_email_title),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = if (emailVerificationSent) stringResource(R.string.profile_verify_email_sent)
                               else stringResource(R.string.profile_verify_email_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = { viewModel.refreshEmailVerificationStatus() }) {
                    Text(stringResource(R.string.profile_verify_email_check))
                }
                if (!emailVerificationSent) {
                    OutlinedButton(
                        onClick = { viewModel.sendVerificationEmail() },
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(stringResource(R.string.profile_verify_email_button))
                    }
                }
            }
        }
    }
```

(El cierre del `if` y los `Spacer` posteriores no cambian. Añadir import de `androidx.compose.material3.TextButton` si falta.)

- [ ] **Step 8: Compilar todo y correr suite completa**

Run: `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest`
Expected: BUILD SUCCESSFUL, todos los tests PASS

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/com/mediatracker/data/auth/AuthDataSource.kt \
        app/src/main/java/com/mediatracker/presentation/profile/ProfileViewModel.kt \
        app/src/main/java/com/mediatracker/presentation/profile/ProfileScreen.kt \
        app/src/main/res/values/strings.xml app/src/main/res/values-es/strings.xml \
        app/src/test/java/com/mediatracker/data/auth/AuthDataSourceTest.kt
git commit -m "fix(profile): el banner de verificación de email se refresca con user.reload()"
```

**QA manual (post-fase):** con una cuenta de prueba sin verificar, pulsar el enlace del correo y reabrir Perfil → el banner debe desaparecer; con el botón "Ya lo he verificado" debe desaparecer al instante.

---

### Task 3: Query builder de libros sin frase exacta

**Files:**
- Modify: `app/src/main/java/com/mediatracker/data/remote/books/GoogleBooksSearchHelper.kt`
- Modify: `app/src/main/java/com/mediatracker/data/remote/books/GoogleBooksApi.kt:10-18`
- Create: `app/src/test/java/com/mediatracker/data/remote/books/GoogleBooksSearchHelperTest.kt`

- [ ] **Step 1: Escribir tests que fallan**

Crear `GoogleBooksSearchHelperTest.kt`:

```kotlin
package com.mediatracker.data.remote.books

import org.junit.Assert.assertEquals
import org.junit.Test

class GoogleBooksSearchHelperTest {

    @Test
    fun `isbn13 with hyphens becomes isbn query`() {
        assertEquals("isbn:9788445077528", buildGoogleBooksQuery("978-84-450-7752-8"))
    }

    @Test
    fun `isbn10 becomes isbn query`() {
        assertEquals("isbn:8445077521", buildGoogleBooksQuery("8445077521"))
    }

    @Test
    fun `isbn prefix is stripped`() {
        assertEquals("isbn:9788445077528", buildGoogleBooksQuery("ISBN: 978-84-450-7752-8"))
    }

    @Test
    fun `author-like query is sent as-is, not exact phrase`() {
        assertEquals("Stephen King", buildGoogleBooksQuery("Stephen King"))
    }

    @Test
    fun `capitalized title query is sent as-is`() {
        assertEquals("La Sombra Del Viento", buildGoogleBooksQuery("La Sombra Del Viento"))
    }

    @Test
    fun `lowercase query is sent as-is`() {
        assertEquals("el nombre del viento", buildGoogleBooksQuery("el nombre del viento"))
    }

    @Test
    fun `blank query returns empty`() {
        assertEquals("", buildGoogleBooksQuery("   "))
    }
}
```

- [ ] **Step 2: Verificar que falla el test de frase exacta**

Run: `./gradlew :app:testDebugUnitTest --tests "com.mediatracker.data.remote.books.GoogleBooksSearchHelperTest"`
Expected: FAIL — `author-like query...` espera `Stephen King` pero recibe `"Stephen King"` (con comillas)

- [ ] **Step 3: Simplificar `buildGoogleBooksQuery`**

Reemplazar desde el bloque 3 (heurístico de autor) hasta el final de la función por:

```kotlin
    // 3. General query: send as-is. Google Books full-text relevance handles
    //    authors and titles well; forcing an exact phrase ("Stephen King")
    //    restricted results, and no heuristic can tell an author name from a
    //    capitalized title ("La Sombra Del Viento").
    return trimmed
}
```

(Se eliminan `words`, `looksLikeAuthor` y el `if/else` final. `getBookSearchLang()` no se toca.)

- [ ] **Step 4: Verificar que pasan**

Run: `./gradlew :app:testDebugUnitTest --tests "com.mediatracker.data.remote.books.GoogleBooksSearchHelperTest"`
Expected: PASS (7 tests)

- [ ] **Step 5: `orderBy=relevance` explícito en `searchBooks`**

En `GoogleBooksApi.searchBooks`, añadir el parámetro tras `langRestrict`:

```kotlin
@Query("orderBy") orderBy: String = "relevance",
```

- [ ] **Step 6: Compilar y commit**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

```bash
git add app/src/main/java/com/mediatracker/data/remote/books/GoogleBooksSearchHelper.kt \
        app/src/main/java/com/mediatracker/data/remote/books/GoogleBooksApi.kt \
        app/src/test/java/com/mediatracker/data/remote/books/GoogleBooksSearchHelperTest.kt
git commit -m "fix(books): la búsqueda deja de forzar frase exacta y ordena por relevancia"
```

---

### Task 4: Deduplicación de libros por título + autor

**Files:**
- Modify: `app/src/main/java/com/mediatracker/data/remote/books/BooksMapper.kt` (al final)
- Modify: `app/src/main/java/com/mediatracker/data/repository/MediaRepositoryImpl.kt:118-146` (`searchBooks`)
- Create: `app/src/test/java/com/mediatracker/data/remote/books/BooksMapperTest.kt`

- [ ] **Step 1: Escribir tests que fallan**

Crear `BooksMapperTest.kt`:

```kotlin
package com.mediatracker.data.remote.books

import com.mediatracker.domain.model.MediaItem
import com.mediatracker.domain.model.MediaType
import org.junit.Assert.assertEquals
import org.junit.Test

class BooksMapperTest {

    private fun book(id: String, title: String, authors: String?) = MediaItem(
        id = id,
        mediaType = MediaType.BOOK,
        title = title,
        overview = "",
        posterUrl = "",
        releaseDate = "",
        rating = 0f,
        genres = emptyList(),
        extraData = authors?.let { mapOf("authors" to it) },
    )

    @Test
    fun `same work from two sources is deduplicated`() {
        val items = listOf(
            book("book_g1", "It", "Stephen King"),
            book("book_ol_1", "it", "Stephen King"),
        )
        assertEquals(1, items.dedupeBooks().size)
    }

    @Test
    fun `same title by different authors is kept`() {
        val items = listOf(
            book("book_g1", "It", "Stephen King"),
            book("book_g2", "It", "Alexa Chung"),
        )
        assertEquals(2, items.dedupeBooks().size)
    }

    @Test
    fun `only first author matters for the key`() {
        val items = listOf(
            book("book_g1", "Good Omens", "Terry Pratchett, Neil Gaiman"),
            book("book_g2", "Good Omens", "Terry Pratchett"),
        )
        assertEquals(1, items.dedupeBooks().size)
    }

    @Test
    fun `missing authors falls back to title-only key`() {
        val items = listOf(
            book("book_g1", "Anónimo", null),
            book("book_g2", "Anónimo", null),
        )
        assertEquals(1, items.dedupeBooks().size)
    }

    @Test
    fun `first occurrence wins`() {
        val items = listOf(
            book("book_g1", "It", "Stephen King"),
            book("book_ol_1", "It", "Stephen King"),
        )
        assertEquals("book_g1", items.dedupeBooks().single().id)
    }
}
```

- [ ] **Step 2: Verificar que no compila**

Run: `./gradlew :app:testDebugUnitTest --tests "com.mediatracker.data.remote.books.BooksMapperTest"`
Expected: FAIL — `unresolved reference: dedupeBooks`

- [ ] **Step 3: Implementar `dedupeBooks` en `BooksMapper.kt`**

Añadir al final del fichero:

```kotlin
/**
 * Deduplicates book results: the same work often appears as several editions
 * (reissues, publishers, Google Books + Open Library). Key: normalized title
 * plus first author. extraData is nullable in MediaItem.
 */
fun List<MediaItem>.dedupeBooks(): List<MediaItem> =
    distinctBy { item ->
        val title = item.title.trim().lowercase()
        val firstAuthor = item.extraData?.get("authors")
            ?.substringBefore(",")?.trim()?.lowercase()
            .orEmpty()
        "$title|$firstAuthor"
    }
```

- [ ] **Step 4: Verificar que pasan**

Run: `./gradlew :app:testDebugUnitTest --tests "com.mediatracker.data.remote.books.BooksMapperTest"`
Expected: PASS (5 tests)

- [ ] **Step 5: Usar `dedupeBooks` en `MediaRepositoryImpl.searchBooks`**

Reemplazar el cuerpo del `try` (líneas ~120-136):

```kotlin
val googleItems = googleBooksApi.searchBooks(
    query = buildGoogleBooksQuery(query),
    langRestrict = lang,
    key = BuildConfig.GOOGLE_BOOKS_API_KEY,
).items.filterQualityBooks()
    .map { it.toMediaItem() }
    .dedupeBooks()

if (googleItems.size >= MIN_BOOKS_THRESHOLD) {
    googleItems
} else {
    Timber.d("Google Books search below threshold (${googleItems.size}), complementing with Open Library")
    val olItems = openLibraryApi.searchBooks(query = query).docs.toMediaItems()
    (googleItems + olItems)
        .dedupeBooks()
        .take(20)
}
```

Añadir import `com.mediatracker.data.remote.books.dedupeBooks`. El `distinctBy { it.title.lowercase() }` desaparece.

- [ ] **Step 6: Suite completa y commit**

Run: `./gradlew :app:testDebugUnitTest`
Expected: PASS

```bash
git add app/src/main/java/com/mediatracker/data/remote/books/BooksMapper.kt \
        app/src/main/java/com/mediatracker/data/repository/MediaRepositoryImpl.kt \
        app/src/test/java/com/mediatracker/data/remote/books/BooksMapperTest.kt
git commit -m "feat(books): deduplicación de ediciones por título y primer autor"
```

---

### Task 5: Trending real con Open Library

**Files:**
- Modify: `app/src/main/java/com/mediatracker/data/repository/MediaRepositoryImpl.kt:38-51` (companion) y `:88-116` (`getTrendingBooks`)

- [ ] **Step 1: Eliminar las consultas rotatorias**

Borrar del `companion object` la lista `BOOKS_TRENDING_QUERIES` y la función `trendingBooksQuery()` (líneas ~38-51).

- [ ] **Step 2: Invertir las fuentes en `getTrendingBooks`**

Reemplazar la función completa por:

```kotlin
private suspend fun getTrendingBooks(): List<MediaItem> {
    val olItems = try {
        openLibraryApi.getTrendingBooks().works.toMediaItems()
    } catch (e: Exception) {
        Timber.w(e, "Open Library trending failed, falling back to Google Books")
        emptyList()
    }
    if (olItems.size >= MIN_BOOKS_THRESHOLD) return olItems.take(20)

    val googleItems = try {
        val lang = localeRepository.googleBooksLang(localeRepository.getLanguageCode())
        googleBooksApi.getPopularBooks(
            langRestrict = lang,
            key = BuildConfig.GOOGLE_BOOKS_API_KEY,
        ).items.filterQualityBooks(minYear = MIN_BOOK_PUBLICATION_YEAR)
            .map { it.toMediaItem() }
    } catch (e: Exception) {
        Timber.e(e, "Google Books trending also failed")
        emptyList()
    }
    return (olItems + googleItems).dedupeBooks().take(20)
}
```

(`getPopularBooks` ya tiene `query = "subject:fiction"` y `orderBy = "relevance"` por defecto en la interfaz Retrofit.)

- [ ] **Step 3: Compilar y correr suite**

Run: `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest`
Expected: BUILD SUCCESSFUL, tests PASS

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/mediatracker/data/repository/MediaRepositoryImpl.kt
git commit -m "feat(books): trending basado en Open Library trending/daily"
```

---

### Task 6: Verificación final de la fase

- [ ] **Step 1: Suite completa + build de release-lint básico**

Run: `./gradlew :app:testDebugUnitTest :app:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Añadir entrada al CHANGELOG.md**

Bajo el encabezado más reciente, sección nueva:

```markdown
## [Unreleased]
### Fixed
- El aviso "Verifica tu email" desaparece tras verificar (reload de Firebase) y añade botón "Ya lo he verificado".
- Búsqueda de libros: sin frase exacta forzada, orden por relevancia y deduplicación de ediciones.
### Changed
- Descubrir: rejilla de 3 columnas (igual que Inicio y Biblioteca).
- Libros populares: Open Library trending/daily en vez de consultas por género.
```

- [ ] **Step 3: Commit final**

```bash
git add CHANGELOG.md
git commit -m "docs: changelog Fase 1 — arreglos rápidos"
```

**QA manual en dispositivo (Hugo, desde Android Studio/Windows):** búsqueda "Stephen King" en Libros (resultados relevantes, sin duplicados), pestaña Libros sin búsqueda (populares reales), Descubrir a 3 columnas, y flujo completo del banner de email.
