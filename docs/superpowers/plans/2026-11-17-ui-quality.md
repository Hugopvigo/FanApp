# Calidad visual y UI tests Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development o superpowers:executing-plans para ejecutar este plan tarea a tarea.

**Goal:** Verificar visualmente los 4 temas en dispositivo y cubrir con tests instrumentados los componentes de UI mas visibles.

**Architecture:** Se usan composables stateless ya extraidos (`MediaCard`, `StatusChip`, `ProfileSections`) con estado falso, para no necesitar Hilt ni Firebase en los tests. Las capturas se automatizan con `adb screencap` y se revisan en el Review pane.

**Tech Stack:** Jetpack Compose UI Test (JUnit4), `androidx.compose.ui:ui-test-junit4`, `androidx.test:runner`, Coil, Material 3.

**Spec:** `docs/superpowers/specs/2026-11-17-next-improvements.md`

## Global Constraints

- Requiere dispositivo conectado (`adb devices` no vacio). Si no lo esta, bloquear las tareas U2-U3 y avisar.
- No anadir Robolectric: se prefiere dispositivo real.
- No usar `hiltViewModel` en tests: usar composables con parametros.
- Las capturas se guardan en `docs/capturas/` con el mismo convenio que las existentes.
- `.md` nuevos requieren `git add -f`; las imagenes no.

---

### Task U1: Dependencias de UI test

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `gradle/libs.versions.toml`

- [ ] **Step 1: Anadir dependencias**

En `androidTestImplementation`, sustituir la linea actual:

```kotlin
androidTestImplementation(libs.compose.ui.test)
```

por:

```kotlin
androidTestImplementation(libs.compose.ui.test.junit4)
androidTestImplementation(libs.compose.ui.test.manifest)
androidTestImplementation(libs.androidx.test.runner)
androidTestImplementation(libs.espresso.core)
androidTestImplementation("androidx.test:rules:1.6.2")
```

En el version catalog, bajo `[libraries]`, anadir los alias si no existen:

```toml
compose-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
```

Además, en `[versions]` añadir `testRunner = "1.7.0"` y `espresso = "3.7.0"`, y en `[libraries]` los alias `androidx-test-runner` y `espresso-core` con esas versiones. Espresso se fija explícitamente porque la versión transitiva falla en HyperOS.

- [ ] **Step 2: Verificar**

Run: `./gradlew :app:assembleDebugAndroidTest 2>&1 | tail -12`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/build.gradle.kts gradle/libs.versions.toml
git commit -m "test(ui): añade dependencias de Compose UI test"
```

---

### Task U2: Comprobar dispositivo y capturas base

**Files:**
- Create: `docs/capturas/2026-11-17-<tema>-<pantalla>.png` (una por tema y pantalla)

- [ ] **Step 1: Verificar conexion**

Run: `adb devices -l`
Expected: exactamente un dispositivo `device`. Si dice `unauthorized`, abrir el movil y aceptar la huella/depuracion USB. Si la lista esta vacia, **parar** y pedir a Hugo que conecte el cable.

- [ ] **Step 2: Instalar y abrir**

Run: `adb install -r app/build/outputs/apk/debug/app-debug.apk && adb shell am start -n com.mediatracker/.MainActivity`
Expected: la app abre en el dispositivo.

- [ ] **Step 3: Capturar una pantilla por tema**

Para cada tema (Fantasy, Light, Purple, Dark) y pantalla (Home, Descubrir, Biblioteca, Detalle, Perfil, Notificaciones): navegar desde el movil y ejecutar:

```bash
adb shell screencap -p /sdcard/captura.png
adb pull /sdcard/captura.png docs/capturas/2026-11-17-<tema>-<pantalla>.png
```

- [ ] **Step 4: Revisar y documentar**

Abrir las capturas con `tools.browser.preview` y comparar con `docs/design-audit-2026-11-17.md`. Anadir un apartado "Verificacion en dispositivo" con: temas revisados, contraste de estrellas, gradientes y nav flotante, y cualquier hallazgo nuevo.

```bash
git add docs/capturas/2026-11-17-*.png
git add -f docs/design-audit-2026-11-17.md
git commit -m "docs: capturas de los 4 temas en dispositivo"
```

---

### Task U3: UI tests de componentes

**Files:**
- Create: `app/src/androidTest/java/com/mediatracker/presentation/components/MediaCardTest.kt`
- Create: `app/src/androidTest/java/com/mediatracker/presentation/components/StatusChipTest.kt`

- [ ] **Step 1: Test de MediaCard**

```kotlin
package com.mediatracker.presentation.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.mediatracker.domain.model.MediaItem
import com.mediatracker.domain.model.MediaType
import com.mediatracker.presentation.theme.MediaTrackerTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MediaCardTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun `MediaCard muestra el titulo y propaga el clic`() {
        var clicked = false
        val item = MediaItem(
            id = "1", mediaType = MediaType.MOVIE, title = "Inception",
            overview = "", posterUrl = "", releaseDate = "2010",
            rating = 8.8f, genres = emptyList(),
        )
        composeRule.setContent {
            MediaTrackerTheme {
                MediaCard(item = item, onClick = { clicked = true })
            }
        }
        composeRule.onNodeWithText("Inception").performClick()
        assertTrue(clicked)
    }
}
```

- [ ] **Step 2: Test de StatusChip**

```kotlin
package com.mediatracker.presentation.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.mediatracker.domain.model.ItemStatus
import com.mediatracker.presentation.theme.MediaTrackerTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class StatusChipTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun `StatusChip propaga el clic`() {
        var clicked = false
        composeRule.setContent {
            MediaTrackerTheme {
                StatusChip(status = ItemStatus.WATCHLIST, selected = false, onClick = { clicked = true })
            }
        }
        composeRule.onNodeWithText("WATCHLIST").performClick()
        assertTrue(clicked)
    }
}
```

Nota: `StatusChip` usa `status.label()` que puede no ser literalmente `WATCHLIST`. Si el test no encuentra el nodo, usar `onRoot().performClick()` y comprobar solo el flag.

- [ ] **Step 3: Ejecutar en dispositivo**

Run: `./gradlew :app:connectedDebugAndroidTest`
Expected: BUILD SUCCESSFUL, 2 tests. Si falla por `INSTALL_FAILED_...`, verificar que no haya otra version de la app con firma distinta.

- [ ] **Step 4: Commit**

```bash
git add app/src/androidTest/java/com/mediatracker/presentation/components/
git commit -m "test(ui): tests instrumentados de MediaCard y StatusChip"
```

---

### Task U4: Cierre

- [ ] **Step 1: Verificacion completa**

Run: `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest :app:lint :app:connectedDebugAndroidTest`
Expected: todo en verde, 99 unit tests + 2 UI tests, 0 errores lint.

- [ ] **Step 2: Actualizar CHANGELOG y commit final**

```bash
git add CHANGELOG.md
git commit -m "docs: cierre calidad visual y UI tests"
```
