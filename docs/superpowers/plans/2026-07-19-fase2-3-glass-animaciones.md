# Fases 2+3 — Coherencia glass y animaciones: Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Todas las pantallas secundarias con el estilo glass/iOS26, ficha de detalle con secciones glass, y cards con micro-animación de pulsación que llenan su celda en las rejillas.

**Architecture:** Un componente compartido `GlassBackHeader` sustituye los `Scaffold`+`TopAppBar` de las 5 pantallas Material planas (Notifications, Theme, Privacy, ChangePassword, Import). `DetailScreen` agrupa sus secciones interactivas en `GlassSurface`. `MediaCard` gana escala al pulsar (`graphicsLayer` + `collectIsPressedAsState`) y póster fluido (`fillMaxWidth().aspectRatio(2/3)`).

**Tech Stack:** Jetpack Compose Material3, tokens glass existentes (`fanAppColors`: surfaceGlass/surfaceElev/borderColor/hairlineColor).

**Contexto ya verificado:**
- Transiciones de navegación YA existen en `AppNavGraph.kt` (crossfade tabs, slide Detail, sheet sub-pantallas) — no tocar.
- Achievements, Leaderboard, Stats, Home, Discover, Profile, Library ya son glass.
- DetailScreen ya tiene hero iOS26 correcto (póster + gradiente + badges + back flotante).
- Home usa `LazyRow`s horizontales de `MediaCard` (ancho fijo 120dp correcto ahí).
- Restricción crítica: NO tocar arquitectura de insets/FloatingBottomNav (bug doble navigationBarsPadding, mayo 2026).

---

### Task 1: Componente `GlassBackHeader`

**Files:**
- Create: `app/src/main/java/com/mediatracker/presentation/components/GlassBackHeader.kt`

- [ ] **Step 1: Crear el componente**

```kotlin
package com.mediatracker.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediatracker.R
import com.mediatracker.presentation.theme.DisplayFontFamily

/**
 * Cabecera glass/iOS26 para sub-pantallas: botón atrás circular glass +
 * título display. Sustituye al TopAppBar Material en el rediseño.
 * La pantalla debe poner Spacer(Modifier.statusBarsPadding()) antes.
 */
@Composable
fun GlassBackHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        GlassSurface(radius = 999.dp) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = DisplayFontFamily,
                letterSpacing = (-0.4).sp,
            ),
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        trailing?.invoke()
    }
}
```

- [ ] **Step 2: Compilar** — `./gradlew :app:compileDebugKotlin` → BUILD SUCCESSFUL

- [ ] **Step 3: Commit** — `feat(ui): componente GlassBackHeader para sub-pantallas`

---

### Task 2: Migrar las 5 pantallas Material a glass

**Files:**
- Modify: `app/src/main/java/com/mediatracker/presentation/profile/NotificationsScreen.kt`
- Modify: `app/src/main/java/com/mediatracker/presentation/theme/ThemeScreen.kt`
- Modify: `app/src/main/java/com/mediatracker/presentation/profile/PrivacyScreen.kt`
- Modify: `app/src/main/java/com/mediatracker/presentation/profile/ChangePasswordScreen.kt`
- Modify: `app/src/main/java/com/mediatracker/presentation/csvimport/ImportScreen.kt`

Patrón de migración (idéntico en las 5):

- [ ] **Step 1: Reemplazar el esqueleto `Scaffold` + `TopAppBar` de cada pantalla**

ANTES (patrón común):

```kotlin
Scaffold(
    topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.xxx_title)) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, ...) } },
            colors = TopAppBarDefaults.topAppBarColors(...),
        )
    },
    containerColor = MaterialTheme.colorScheme.background,
) { padding ->
    Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)...) { <contenido> }
}
```

DESPUÉS:

```kotlin
Column(modifier = Modifier.fillMaxSize()) {
    Spacer(Modifier.statusBarsPadding())
    GlassBackHeader(
        title = stringResource(R.string.xxx_title),
        onBack = onBack,
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
    ) { <contenido sin cambios> }
}
```

Reglas por pantalla:
- El `<contenido>` interior NO se toca (switches, listas, formularios).
- Si la pantalla tenía `verticalScroll`, se conserva; si usaba `LazyColumn`, la `LazyColumn` va donde iba la `Column` interior.
- Si el `TopAppBar` tenía `actions`, pasan al slot `trailing` de `GlassBackHeader`.
- Eliminar imports muertos: `Scaffold`, `TopAppBar`, `TopAppBarDefaults`, `IconButton`, `ExperimentalMaterial3Api` (si queda sin uso), `Icons`/`ArrowBack` (si quedan sin uso).
- Añadir imports: `statusBarsPadding`, `GlassBackHeader`.

- [ ] **Step 2: Compilar tras cada pantalla** — `./gradlew :app:compileDebugKotlin` → BUILD SUCCESSFUL

- [ ] **Step 3: Commit** — `feat(ui): sub-pantallas migradas al estilo glass iOS26`

---

### Task 3: Secciones glass en DetailScreen

**Files:**
- Modify: `app/src/main/java/com/mediatracker/presentation/detail/DetailScreen.kt:300-392` (columna de contenido de `DetailContent`)

- [ ] **Step 1: Agrupar secciones en `GlassSurface`**

Dentro de la `Column(modifier = Modifier.padding(horizontal = 16.dp), ...)`:

1. La fila de `StatusChip` + botón quitar: sin cambios (chips ya son glass).
2. Envolver el bloque de usuario (FavoriteToggle + StarRatingBar + NotesField) en una card:

```kotlin
if (userItem != null) {
    GlassSurface(modifier = Modifier.fillMaxWidth(), radius = 18.dp) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            FavoriteToggle(
                isFavorite = userItem.favorite,
                onToggle = onToggleFavorite,
                enabled = userItem.status != ItemStatus.ABANDONED,
            )
            StarRatingBar(
                rating = userItem.userRating,
                onRatingChanged = onRatingChanged,
            )
            NotesField(
                notes = userItem.notes.orEmpty(),
                onNotesChanged = onNotesChanged,
            )
        }
    }
}
```

3. Envolver el bloque de tracking de series (SeasonEpisodeStepper + EpisodeTracker) en otra `GlassSurface` igual (mismo patrón: Column padding 14, spacedBy 14), manteniendo intacta la lógica de `epsPerSeason`.
4. Envolver `PageProgressStepper` (libros) en otra `GlassSurface` igual.
5. Envolver overview + `ExtraInfo` en una última `GlassSurface` (Column padding 14, spacedBy 10).
6. Import de `GlassSurface` si falta.

- [ ] **Step 2: Compilar y correr tests de Detail**

Run: `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest` → BUILD SUCCESSFUL

- [ ] **Step 3: Commit** — `feat(detail): secciones agrupadas en cards glass`

---

### Task 4: MediaCard — escala al pulsar y póster fluido

**Files:**
- Modify: `app/src/main/java/com/mediatracker/presentation/components/MediaCard.kt:160-239`

- [ ] **Step 1: Añadir animación de pulsación y póster que llena la celda**

```kotlin
@Composable
fun MediaCard(
    item: MediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showFavBadge: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "cardPressScale",
    )

    Column(
        modifier = modifier
            .width(120.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box {
            if (item.posterUrl.isNotBlank()) {
                AsyncImage(
                    model = item.posterUrl,
                    contentDescription = item.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f / 3f)
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop,
                )
            } else {
                GradientPoster(
                    title = item.title,
                    kind = item.mediaType,
                    rating = item.rating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f / 3f),
                )
            }
            // ... badge sin cambios
        }
        // ... textos sin cambios
    }
}
```

Notas:
- En `LazyRow` (Home) la Column mide 120dp → póster 120x180, como antes.
- En rejillas con `weight(1f)` las constraints fijas de la celda anulan `width(120.dp)` → el póster llena la celda con ratio 2:3 en cualquier pantalla.
- Imports nuevos: `androidx.compose.animation.core.Spring`, `animateFloatAsState`, `spring`, `androidx.compose.foundation.interaction.MutableInteractionSource`, `collectIsPressedAsState`, `androidx.compose.foundation.layout.aspectRatio`, `androidx.compose.runtime.getValue`, `remember`, `androidx.compose.ui.draw.clip` (ya está), `androidx.compose.ui.graphics.graphicsLayer`.

- [ ] **Step 2: Compilar** — `./gradlew :app:compileDebugKotlin` → BUILD SUCCESSFUL

- [ ] **Step 3: Commit** — `feat(ui): MediaCard con escala al pulsar y póster fluido en rejillas`

---

### Task 5: Pulido Discover — "Ver más" hardcodeado

**Files:**
- Modify: `app/src/main/java/com/mediatracker/presentation/discover/DiscoverScreen.kt` (llamada a `SectionRow`)

- [ ] **Step 1: Quitar el action "Ver más"** — es un literal en español hardcodeado y no es clicable (no hace nada). Cambiar:

```kotlin
SectionRow(
    title = stringResource(R.string.discover_top_trending),
)
```

- [ ] **Step 2: Compilar y commit** — `fix(discover): elimina action "Ver más" no funcional y hardcodeado`

---

### Task 6: Verificación final

- [ ] **Step 1:** `./gradlew :app:testDebugUnitTest :app:assembleDebug` → BUILD SUCCESSFUL
- [ ] **Step 2:** Actualizar CHANGELOG.md (sección Fases 2+3) y commit.
- [ ] **Step 3:** QA manual de Hugo en dispositivo: abrir Notificaciones/Tema/Privacidad/Cambiar contraseña/Importar (header glass, sin doble status bar), ficha de detalle (cards glass), pulsar cards (escala), rejillas 3 columnas con pósters llenando la celda.
