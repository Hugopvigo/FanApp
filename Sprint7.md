# Sprint 7 — Engagement, Social y Gamificación

> **Objetivo:** Hacer de FanApp la mejor app de tracking de series, películas y libros en Google Play y App Store.
> **Duración:** 4 semanas
> **Dependencias:** Sprint 6 completado ✓

---

## Visión de Producto

**Propuesta de valor:** FanApp es Letterboxd + Goodreads + Trakt, con la estética iOS26 glass y el social que ninguno tiene.

**Competidores y sus problemas:**
- Letterboxd — sin libros
- Goodreads — UI anticuada, Amazon, los usuarios lo odian
- Trakt — funcional pero feo
- Simkl — correcto pero sin personalidad

**Qué hace que una app arrase en stores:**

| Pilar | Qué aporta |
|-------|------------|
| **Social** | Viralidad orgánica — sin social no hay crecimiento |
| **FanCard compartible** | Cada share = anuncio gratuito en redes |
| **Import desde Letterboxd/Goodreads** | Usuario con 200 títulos importados no desinstala nunca |
| **Widgets** | Apple y Google featured activamente apps con widgets |
| **Onboarding en 3 clics** | El 60-70% abandona en la primera sesión |
| **Offline + sin crashes** | Las reviews de 5⭐ lo mencionan siempre |

---

## Tareas por Prioridad

### Alta — Semana 1-2

---

#### T7: Google Sign-In funcional
**Pendiente desde Sprint 1. El botón existe pero su onClick está vacío.**

Archivos: `LoginScreen.kt`, `AuthDataSource.kt`, `AuthViewModel.kt`

```kotlin
// AuthDataSource — nuevo método
suspend fun signInWithGoogle(idToken: String): Result<AuthResult> {
    val credential = GoogleAuthProvider.getCredential(idToken, null)
    return runCatching { firebaseAuth.signInWithCredential(credential) }
        .map { AuthResult(isLoggedIn = true) }
}

// LoginScreen — onClick del botón Google
val launcher = rememberLauncherForActivityResult(StartActivityForResult()) { result ->
    val account = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        .getResult(ApiException::class.java)
    viewModel.onGoogleSignIn(account.idToken)
}
onClick = {
    val gso = GoogleSignInOptions.Builder(DEFAULT_SIGN_IN)
        .requestIdToken(getString(R.string.default_web_client_id))
        .requestEmail().build()
    launcher.launch(GoogleSignIn.getClient(context, gso).signInIntent)
}
```

**Prerrequisito:** OAuth Client ID creado en Google Cloud Console con el SHA-1 del debug keystore.

- [x] Selector de cuentas Google se abre
- [x] Login y registro automático en Firebase
- [x] displayName se toma de la cuenta Google
- [x] Manejo de cancelación y error de red

---

#### T6: Botón Add Central (Quick Add)
**FAB central en la bottom bar → bottom sheet de búsqueda rápida.**

Archivos a crear: `QuickAddSheet.kt`, `QuickAddViewModel.kt`
Archivos afectados: `FloatingBottomNav.kt`, `AppNavGraph.kt`

```
FloatingBottomNav
┌── 🏠 Home ── 🔍 Discover ── ➕ ── 📚 Library ── 👤 Profile ──┐
                               ↑ FAB destacado

QuickAddSheet
├── SearchBar (foco automático al abrir)
├── Resultados en tiempo real (tabs: Series / Pelis / Libros)
│   └── Poster + título + año + botón "+"
├── Si ya existe → badge con estado actual
└── Se cierra al añadir o al pulsar fuera
```

- [x] Botón Add central visible en bottom bar
- [x] Sheet se abre con teclado
- [x] "+" añade a Watchlist directamente sin ir a Detail
- [x] Items ya existentes muestran su estado

---

#### T4: Valoraciones y Notas (1-5 estrellas)
**Room migration v2 → v3.**

Archivos: `UserItem.kt`, `UserItemEntity.kt`, `EntityMappers.kt`, `DetailScreen.kt`, `LibraryItemCard.kt`

```kotlin
// UserItem — campos nuevos
val rating: Int?,   // 1-5, null = sin valorar
val notes: String?, // texto libre, null = sin notas

// Room migration
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE user_items ADD COLUMN rating INTEGER DEFAULT NULL")
        db.execSQL("ALTER TABLE user_items ADD COLUMN notes TEXT DEFAULT NULL")
    }
}
```

DetailScreen: 5 estrellas interactivas + TextField de notas con guardado automático al perder foco.
LibraryItemCard: mostrar estrellas si `rating != null`, icono 📝 si `notes != null`.

- [x] Estrellas interactivas en Detail
- [x] Notas con guardado automático
- [x] Rating visible en LibraryItemCard
- [x] Migration v2→v3 sin pérdida de datos

---

#### T18: FanCard Compartible
**Generar imagen compartible con Canvas. Sin backend, viralidad inmediata.**

Archivos a crear: `FanCardScreen.kt`, `FanCardViewModel.kt`

```kotlin
// Capturar Composable como Bitmap
// Librería: dev.shreyaspatil:capturable (Compose-first, MIT)
// O: ComposeView.drawToBitmap()

val shareIntent = Intent(Intent.ACTION_SEND).apply {
    type = "image/png"
    putExtra(Intent.EXTRA_STREAM, uriBitmap)
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
}
context.startActivity(Intent.createChooser(shareIntent, "Compartir FanCard"))
```

Tipos de tarjeta:
- **Top del Mes** — top 3-5 completados + valoración media
- **Mi Perfil** — nivel, XP, géneros favoritos
- **Rating Card** — se ofrece automáticamente al valorar un item

Estilo glass con gradiente del tema activo.

- [x] Imagen generada y compartible por el sistema
- [x] Rating Card se ofrece al puntuar un item
- [x] Funciona con los 4 temas

---

#### T20: Import desde Letterboxd y Goodreads
**El usuario con 200 títulos importados no desinstala nunca.**

Archivos a crear: `ImportScreen.kt`, `ImportViewModel.kt`, `LetterboxdCsvParser.kt`, `GoodreadsCsvParser.kt`

| App | Formato | Cómo exportar |
|-----|---------|---------------|
| Letterboxd | CSV | Settings → Import & Export → Export Your Data |
| Goodreads | CSV | My Books → Import/Export |

```kotlin
// Mapeo de estados Letterboxd
"watched"   → ItemStatus.DONE
"watchlist" → ItemStatus.WANT

// Mapeo Goodreads
"read"              → ItemStatus.DONE
"to-read"           → ItemStatus.WANT
"currently-reading" → ItemStatus.IN_PROGRESS
"did-not-finish"    → ItemStatus.DROPPED
```

Flujo:
1. Usuario selecciona archivo CSV (picker del sistema)
2. Preview: "147 items encontrados — 89 completados, 23 watchlist, 35 abandonados"
3. Confirmar → import en background con progreso
4. Para cada item: buscar en API para obtener poster y metadatos
5. Resultado: "Importados 147 items (89 nuevos, 58 ya existían)"

- [x] Import CSV Letterboxd funcional
- [x] Import CSV Goodreads funcional
- [x] Preview antes de confirmar
- [x] Duplicados detectados y no re-importados
- [x] Import en background con barra de progreso

---

### Alta — Semana 2 (Gamificación)

---

#### T2: Achievements (Logros)
Archivos a crear: `AchievementsScreen.kt`, `CheckAchievementsUseCase.kt`
Archivos afectados: `UpdateItemStatusUseCase.kt`, `AddUserItemUseCase.kt`

| ID | Nombre | Condición |
|----|--------|-----------|
| `first_add` | Primer Paso | Añadir primer item |
| `first_complete` | Mission Accomplished | Completar primer item |
| `series_fan` | Serie Adicta | 5 series completadas |
| `movie_marathon` | Maratón de Cine | 10 películas completadas |
| `bookworm` | Rata de Biblioteca | 5 libros completados |
| `completionist` | Completista | 25 items completados |
| `centurion` | Centurión | 100 items completados |
| `explorer` | Explorador | Items de los 3 tipos |
| `curator` | Curador | 10 favoritos |
| `streak_7` | Racha Semanal | 7 días consecutivos |
| `streak_30` | Imparable | 30 días consecutivos |
| `diverse` | Polímata | 3 completados de cada tipo |

Al desbloquear: Snackbar/Dialog + notificación local.

Firestore: `/users/{userId}/gamification/achievements/{achievementId}/`

- [x] 12 logros funcionales con desbloqueo automático
- [x] Grid en AchievementsScreen (desbloqueados + bloqueados con progreso)
- [x] Feedback visual al desbloquear
- [x] Contador visible en Profile

---

#### T3: Streaks (Rachas)
Archivos a crear: `UpdateStreakUseCase.kt`
Archivos afectados: `HomeScreen.kt`, `ProfileScreen.kt`, `HomeViewModel.kt`

```kotlin
data class UserStreak(
    val currentStreak: Int,
    val longestStreak: Int,
    val lastActiveDate: String, // "2026-05-25"
)
// Día consecutivo = fecha de hoy es lastActiveDate + 1
// Día saltado = racha a 0
// Bonus XP: 7d → +50, 30d → +200, 100d → +1000
```

Trigger: `HomeViewModel.init` comprueba la racha al abrir la app.
UI: 🔥 en HomeScreen + racha actual/récord en Profile.

- [x] Racha se calcula correctamente al abrir la app
- [x] HomeScreen muestra 🔥 racha actual
- [x] Profile muestra racha actual + mejor racha
- [x] Bonus XP en hitos

---

#### T1: Ranking Global y Leaderboard
Archivos a crear: `LeaderboardScreen.kt`, `LeaderboardViewModel.kt`
Archivos afectados: `ProfileScreen.kt`, `AppNavGraph.kt`

```
LeaderboardScreen
├── Tabs: [ Anual ] [ All-Time ] [ Series ] [ Pelis ] [ Libros ]
├── Lista top 50: posición + avatar + nombre + nivel + completados
│   └── Top 3: medallas 🥇🥈🥉
└── Tu posición (sticky al fondo)
```

Firestore: `/rankings/yearly/{year}/users/{userId}/`
Escritura directa desde el cliente al completar un item (sin Cloud Functions de momento).

- [x] Top 50 por categoría
- [x] Tu posición destacada sticky
- [x] Se actualiza al completar items
- [x] Acceso desde Profile

---

### Media — Semana 3

---

#### T8: Trackeo Temporada/Episodio
**Room migration v3 → v4** (requiere T4 primero).

```kotlin
// UserItem — solo para SERIES
val currentSeason: Int?,
val currentEpisode: Int?,

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE user_items ADD COLUMN currentSeason INTEGER DEFAULT NULL")
        db.execSQL("ALTER TABLE user_items ADD COLUMN currentEpisode INTEGER DEFAULT NULL")
    }
}
```

DetailScreen (series IN_PROGRESS): steppers +/- para temporada y episodio + barra de progreso.
LibraryItemCard: badge "T3E7" sobre el poster para series en progreso.

- [x] Steppers en Detail para series en progreso
- [x] Badge TXEX en LibraryItemCard
- [x] Migration v3→v4 sin pérdida de datos

---

#### T5: Estadísticas Detalladas
Archivos a crear: `StatsScreen.kt`, `StatsViewModel.kt`

Métricas (calculadas desde Room, sin llamadas API):
- Total completados / en progreso / abandonados
- Distribución Series vs Pelis vs Libros (barchart)
- Actividad mensual últimos 12 meses (linechart)
- Top géneros
- Tiempo estimado invertido (series: ×45min, pelis: runtime API, libros: ×2min/pág)
- Botón "Compartir estadísticas" → FanCard de stats

Librería gráficos: **Vico** (`com.patrykandpatrick.vico`, Compose-first, MIT).

- [ ] 4+ métricas resumen visibles
- [ ] Gráfico distribución por tipo
- [ ] Gráfico actividad mensual
- [ ] Acceso desde Profile

---

#### T11: Language Toggle funcional → **Movido a Sprint 9**

---

### Alta — Semana 4 (Plataforma)

---

#### T19: Widgets de Pantalla de Inicio → **Movido a Sprint 9**

---

### Media/Baja — Semana 4 (Pulido)

---

#### T10: Push Notifications (FCM) → **Movido a Sprint 9**

---

#### T9: Avatar Personalizado (Firebase Storage) → **Movido a Sprint 9**

---

#### T12: Auto Theme Switch → **Movido a Sprint 9**

---

#### T13: Email Verification → **Movido a Sprint 9**

---

#### T14: Tests Unitarios → **Movido a Sprint 9**

---

### Tareas diferidas a Sprint 9

Las siguientes tareas se han movido a **Sprint 9 — Plataforma, Pulido y Preparación para Producción**:
- T9 Avatar → S9
- T10 FCM → S9
- T11 Language Toggle → S9
- T12 Auto Theme → S9
- T13 Email Verification → S9
- T14 Tests → S9
- T19 Widgets → S9

---

## Orden de Implementación

```
Semana 1: T7 Google Sign-In → T6 Quick Add → T4 Valoraciones → T18 FanCard
Semana 2: T2 Achievements → T3 Streaks → T1 Ranking → T5 Stats
Semana 3: T20 Import → T8 Trackeo T/E → T11 Language
Semana 4: T19 Widgets → T10 FCM → T9 Avatar → T12 Auto Theme → T13 Email → T14 Tests
```

**Dependencias Room** — respetar este orden exacto:
```
Migration v1→v2 (Sprint 5: title, posterUrl)
Migration v2→v3 (T4: rating, notes) ← hacer antes que T8
Migration v3→v4 (T8: currentSeason, currentEpisode) ← hecho como v6→v7
Migration v4→v5 (T2: achievements)
Migration v5→v6 (T3: streaks)
Migration v6→v7 (T8: currentSeason, currentEpisode — posición real)
```

---

## Estimación

| Tarea | Prioridad | Tiempo |
|-------|-----------|--------|
| T7 Google Sign-In | Alta | 0.5-1 día |
| T6 Quick Add | Alta | 1-2 días |
| T4 Valoraciones | Alta | 2-3 días |
| T18 FanCard | Alta | 1-2 días |
| T20 Import Letterboxd/Goodreads | Alta | 2-3 días |
| T2 Achievements | Alta | 2-3 días |
| T3 Streaks | Media | 1-2 días |
| T1 Ranking | Alta | 2-3 días |
| T5 Estadísticas | Media | 2-3 días |
| T8 Trackeo T/E | Media | 1-2 días |
| T11 Language | Media | 0.5-1 día |
| T19 Widgets | Alta | 2-3 días |
| T10 FCM | Media | 2-3 días |
| T9 Avatar Storage | Baja | 1-2 días |
| T12 Auto Theme | Baja | 0.5 días |
| T13 Email Verification | Baja | 0.5 días |
| T14 Tests | Media | 2-3 días |
| **Total** | | **25-38 días** |

---

## Definición de Done

- [x] Google Sign-In funcional
- [x] Quick Add funciona desde cualquier pantalla
- [x] Valoraciones y notas se guardan correctamente
- [x] Achievements se desbloquean automáticamente
- [x] FanCard generada y compartible
- [ ] Import desde Letterboxd/Goodreads funcional
- [ ] Bonus XP en hitos de racha
- [ ] Estadísticas detalladas accesibles desde Profile
- [x] Strings EN/ES en archivos nuevos

*(Widget 2x2, FCM, Language, Avatar, Auto Theme, Email Verification y Tests movidos a Sprint 9)*

---

## Nota sobre priorización

Si el tiempo aprieta:

**Imprescindible:** T7 ✅ + T6 ✅ + T4 ✅ + T18 ✅ + T2 ✅
**Muy recomendable:** T3 (bonus XP) + T1 ✅ + T20 (Import) + T5 (Stats)
**Movidos a Sprint 9:** T9 + T10 + T11 + T12 + T13 + T14 + T19
