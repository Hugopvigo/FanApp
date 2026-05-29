# Sprint 8 — Progreso Detallado, Logros y Niveles Avanzados

> **Objetivo:** Convertir FanApp en la app de tracking más granular del mercado. Progreso de página en libros, progreso de episodio/temporada visual en series, sistema de logros expandido para heavy users, y niveles adicionales que den motivación a largo plazo.
> **Duración:** 4 semanas
> **Dependencias:** Sprint 7 parcialmente completado (T8 Trackeo T/E base implementado, T2 Achievements base implementado)

---

## Contexto del Proyecto

### Sprints Anteriores

| Sprint | Nombre | Estado |
|--------|--------|--------|
| Sprint 1 | Setup + Auth | Completado |
| Sprint 2 | Upgrade de Versiones | Completado |
| Sprint 3 | APIs + Data Layer + Pantallas | Completado |
| Sprint 4 | Temas, Pulido y UX | Completado |
| Sprint 5 | Bugs, Mejoras y Pendientes | Completado |
| Sprint 6 | Diseño Visual: Hero, Featured y Polished UX | Completado |
| Sprint 7 | Engagement, Social y Gamificación | Parcialmente completado |
| **Sprint 8** | **Progreso Detallado, Logros y Niveles Avanzados** | **Planificado** |

### Por qué este sprint ahora

Sprint 7 introdujo el trackeo básico de temporada/episodio (T8) y 12 logros (T2), pero quedan lagunas críticas:

1. **Libros sin progreso de página** — un lector no sabe por dónde va ni qué % lleva. Es la feature #1 que pide cualquier usuario de Goodreads que migre.
2. **Series sin progreso visual** — hay steppers de T/E pero no hay barra de progreso, ni vista de qué temporadas has completado, ni sentido de avance.
3. **Solo 12 logros** — un heavy user los desbloquea todos en 2 semanas. No hay logros de progreso (ej. "lee 1000 páginas"), de constancia (ej. "completa un libro por mes durante 6 meses"), ni de retos.
4. **Solo 6 niveles** — Novato a Leyenda. Un usuario con 100 completados ya es Leyenda y se queda sin meta. Faltan niveles intermedios y de endgame.

---

## Tareas por Prioridad

### Alta — Semana 1-2

---

#### T1: Progreso de Página en Libros

**Room migration v7 → v8.**

Archivos afectados: `UserItem.kt`, `UserItemEntity.kt`, `EntityMappers.kt`, `MediaItem.kt`, `AppDatabase.kt`
Archivos a crear: `UpdateBookProgressUseCase.kt`

```kotlin
// UserItem — campos nuevos (solo BOOK)
val currentPage: Int? = null,
val totalPages: Int? = null,

// MediaItem.extraData ya contiene "pageCount" desde Google Books API.
// Al añadir un libro a IN_PROGRESS, copiar pageCount → UserItem.totalPages.
```

```kotlin
// Room migration v7 → v8
val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE user_items ADD COLUMN currentPage INTEGER DEFAULT NULL")
        db.execSQL("ALTER TABLE user_items ADD COLUMN totalPages INTEGER DEFAULT NULL")
    }
}
```

**Lógica:**

- Al cambiar un libro a `IN_PROGRESS`: si `extraData["pageCount"]` existe, copiarlo a `totalPages` en `UserItem`.
- `currentPage` lo actualiza el usuario manualmente con stepper o input numérico.
- Si `totalPages > 0` y `currentPage > 0`: calcular `% = (currentPage / totalPages) * 100`.
- Si `currentPage >= totalPages`: sugerir automáticamente cambiar a `COMPLETED` (Snackbar).
- XP bonus al completar un libro: +10 XP por cada 100 páginas (ej. 350 páginas = +30 XP extra).

**UI — DetailScreen (libros IN_PROGRESS):**

```
┌─────────────────────────────────────────┐
│  📖 El Nombre del Viento                │
│  ─────────────────────────────────────  │
│  Página: [◄] 247 / 662 [►]             │
│  ████████████░░░░░░░░░░░░  37%         │
│                                         │
│  Tiempo estimado: ~7h 30min restantes   │
│  (a 2min/página)                        │
└─────────────────────────────────────────┘
```

**UI — LibraryItemCard (libros IN_PROGRESS):**

Badge sobre el poster: `📄 37%` o `📄 247/662`

- [x] Campos `currentPage` y `totalPages` en UserItem y Entity
- [x] Migration v8→v9 sin pérdida de datos
- [x] Al poner libro en IN_PROGRESS, se copia pageCount a totalPages
- [x] Stepper/input de página en DetailScreen para libros
- [x] Barra de progreso visual con % en DetailScreen
- [x] Badge de % en LibraryItemCard
- [x] Sugerencia de completar al llegar al final
- [ ] XP bonus por páginas completadas

---

#### T2: Progreso Visual de Series — Barra y Vista de Temporadas

**Extiende T8 de Sprint 7 (steppers básicos). No requiere nueva migration.**

Archivos afectados: `DetailScreen.kt`, `DetailViewModel.kt`, `LibraryItemCard.kt`
Archivos a crear: `SeriesProgressView.kt` (Composable reutilizable)

**Datos ya disponibles:**
- `MediaItem.extraData["numberOfSeasons"]` y `["numberOfEpisodes"]` desde TMDB.
- `UserItem.currentSeason` y `currentEpisode` ya existen.

**Lógica de progreso:**

```kotlin
// Progreso estimado de la serie
// Fórmula: episodios vistos / episodios totales
// Para series en emisión: usar numberOfSeasons y numberOfEpisodes del momento
// Para series terminadas: datos fijos

val episodesWatched = (currentSeason - 1) * avgEpisodesPerSeason + currentEpisode
val progressPercent = (episodesWatched.toFloat() / numberOfEpisodes) * 100
```

**UI — DetailScreen (series IN_PROGRESS):**

```
┌─────────────────────────────────────────┐
│  📺 Breaking Bad                        │
│  ─────────────────────────────────────  │
│  Temporada: [◄] 3 / 5 [►]              │
│  Episodio:  [◄] 7 / 13 [►]             │
│  ████████████████░░░░░░░  62%          │
│                                         │
│  ┌─ T1 ✅ ─ T2 ✅ ─ T3 🔵 ─ T4 ⬜ ─ T5 ⬜ ─┐
│  │  Completada  Completada  En curso   Pendiente  Pendiente │
│  └─────────────────────────────────────────┘
│                                         │
│  34 / 62 episodios vistos               │
└─────────────────────────────────────────┘
```

Leyenda chips de temporadas:
- ✅ Completada (todos los episodios vistos)
- 🔵 En curso (temporada actual)
- ⬜ Pendiente (no empezada)

**UI — LibraryItemCard (series IN_PROGRESS):**

Badge sobre el poster: `📺 62%` o `📺 T3E7`

- [ ] Barra de progreso con % en DetailScreen para series
- [ ] Chips de estado por temporada (completada / en curso / pendiente)
- [ ] Contador "X / Y episodios vistos"
- [x] Badge de % en LibraryItemCard para series
- [x] Lógica de progreso calculada desde season/episode vs totales
- [x] Auto-avance de temporada al pasar del último episodio

---

#### T3: Auto-completar y Auto-avance Inteligente

Archivos afectados: `DetailViewModel.kt`, `UpdateItemStatusUseCase.kt`, `UpdateSeasonEpisodeUseCase.kt`, `UpdateBookProgressUseCase.kt`

**Libros:**
- Si `currentPage >= totalPages` → Snackbar: "¡Has terminado el libro! ¿Marcar como completado?"
- Botón "Completar" en la Snackbar → cambia status a COMPLETED automáticamente.

**Series:**
- Si `currentEpisode >= episodesInCurrentSeason` → Snackbar: "¡Temporada X completada! ¿Avanzar a T{X+1}E1?"
- Si es la última temporada → Snackbar: "¡Serie completada! ¿Marcar como completado?"
- Botón "Avanzar" / "Completar" en la Snackbar.

- [x] Auto-sugerencia de completar libro al llegar a la última página
- [x] Auto-avance de temporada al terminar el último episodio
- [x] Auto-sugerencia de completar serie al terminar la última temporada
- [ ] XP de completado se otorga al confirmar

---

### Alta — Semana 2-3

---

#### T4: Expansión de Logros — 33 Nuevos Achievements (12 → 45)

**Tres horizontes temporales: semanas (enganchar), meses (construir hábito), años (identidad).**
**Más una capa de logros secretos para crear sorpresa y descubrimiento orgánico.**

Archivos afectados: `Gamification.kt`, `CheckAchievementsUseCase.kt`, `AchievementsScreen.kt`
Nuevos enums en `AchievementCondition`.

---

**CATEGORÍA: VOLUMEN — Completados totales**

El backbond de cualquier tracker. La progresión llena los huecos entre los 12 logros existentes
y añade metas de largo plazo reales.

| ID | Nombre | Condición | Target | Icono | Horizonte |
|----|--------|-----------|--------|-------|-----------|
| `devotee` | El Devoto | 50 completados | 50 | 🎗️ | 6 meses |
| `grand_collector` | Gran Coleccionista | 200 completados | 200 | 🏺 | 2 años |
| `archivist` | El Archivero | 500 completados | 500 | 🗃️ | 5+ años |

> Los existentes COMPLETIONIST(25) y CENTURION(100) ya cubren los tramos cortos. Estos llenan
> el endgame a largo plazo.

---

**CATEGORÍA: LECTURA — Libros y páginas**

El usuario de libros es el más fiel a largo plazo. Hay que darle metas en páginas (que se acumulan
aunque lea despacio) y en hábito mensual.

| ID | Nombre | Condición | Target | Icono | Horizonte |
|----|--------|-----------|--------|-------|-----------|
| `pages_1k` | Mil Páginas | 1.000 páginas totales leídas | 1.000 | 📃 | 1 mes |
| `pages_5k` | Cinco Mil | 5.000 páginas totales | 5.000 | 📜 | 3-6 meses |
| `pages_10k` | Diez Mil | 10.000 páginas totales | 10.000 | 🏛️ | 1 año |
| `pages_50k` | Biblioteca Viviente | 50.000 páginas totales | 50.000 | 📚 | 5+ años |
| `speed_reader` | Velocista | Completar un libro en <72h con sesiones registradas | 1 | ⚡ | cualquier |
| `monthly_reader` | Lector del Mes | 1 libro/mes durante 6 meses consecutivos | 6 | 📅 | 6 meses |
| `book_streak_7` | Páginas Cada Día | Registrar sesión de lectura 7 días seguidos | 7 | 📖🔥 | 1-2 semanas |

---

**CATEGORÍA: SERIES — El maratonista**

Las series son el mayor enganche de retorno: el usuario que sigue una serie vuelve semana a semana.

| ID | Nombre | Condición | Target | Icono | Horizonte |
|----|--------|-----------|--------|-------|-----------|
| `series_marathon` | Maratón Total | 20 series completadas | 20 | 📺 | 1-2 años |
| `season_master` | Maestro de Temporadas | 15 temporadas completadas | 15 | 🎬 | 6-12 meses |
| `epic_watcher` | El Épico | Completar una serie con 5+ temporadas | 1 | 🏟️ | variable |

---

**CATEGORÍA: CINE — El cinéfilo**

Escalera clara que da metas progresivas sin que parezca imposible.

| ID | Nombre | Condición | Target | Icono | Horizonte |
|----|--------|-----------|--------|-------|-----------|
| `cinephile` | Cinéfilo | 25 películas completadas | 25 | 🎥 | 3-6 meses |
| `blockbuster` | El Blockbuster | 50 películas completadas | 50 | 🎞️ | 6-12 meses |
| `auteur_fan` | Fan del Autor | 5 películas del mismo director completadas | 5 | 🎭 | variable |

---

**CATEGORÍA: CONSTANCIA — Rachas y hábito**

La racha es el mecanismo de retención más potente. Perder 100 días de racha duele.
El logro "El Fénix" es clave: convierte la pérdida de racha en una nueva meta.

| ID | Nombre | Condición | Target | Icono | Horizonte |
|----|--------|-----------|--------|-------|-----------|
| `streak_100` | Centurión de Fuego | 100 días consecutivos | 100 | 🔥 | 3 meses |
| `streak_365` | Año Entero | 365 días consecutivos de actividad | 365 | ☀️ | 1 año |
| `phoenix` | El Fénix | Recuperar una racha de >50 días después de perderla | 1 | 🦅 | variable |
| `weekly_habit` | Hábito Sólido | Activo al menos una vez por semana durante 8 semanas | 8 | 📆 | 2 meses |

> **El Fénix** es el logro anti-churn más importante. Si el usuario pierde su racha de 60 días
> y luego ve que existe un logro por recuperarla, tiene un incentivo concreto para volver.

---

**CATEGORÍA: DIVERSIDAD — Exploración**

Motivar a salir de la zona de comfort prolonga el tiempo activo en la app y descubre nuevas series/libros.

| ID | Nombre | Condición | Target | Icono | Horizonte |
|----|--------|-----------|--------|-------|-----------|
| `decade_hopper` | Viajero en el Tiempo | Items completados de 4 décadas distintas | 4 | ⏳ | variable |
| `world_citizen` | Ciudadano del Mundo | Items en 3+ idiomas originales distintos | 3 | 🌍 | variable |
| `genre_polymath` | Polímata | Completados en 6+ géneros distintos | 6 | 🎭 | 6-12 meses |
| `triangular` | Triangular | 10 completados de cada tipo (series, pelis, libros) | 10/tipo | 🔺 | 6-12 meses |

---

**CATEGORÍA: CALIDAD — Ratings y notas**

Estos logros tienen doble propósito: dar XP al usuario y generar datos ricos en la app.

| ID | Nombre | Condición | Target | Icono | Horizonte |
|----|--------|-----------|--------|-------|-----------|
| `critic` | El Crítico | Valorar 25 items | 25 | ⭐ | 1-3 meses |
| `chronicler` | Cronista | Escribir nota personal en 10 items | 10 | ✍️ | 1-3 meses |
| `perfectionist` | El Perfeccionista | Dar valoración máxima (5.0) a 5 items distintos | 5 | 🌟 | variable |

---

**CATEGORÍA: FIDELIDAD — Aniversarios de uso**

Los aniversarios son los logros más poderosos para retención multi-año. El usuario que lleva
2 años en la app ya tiene identidad construida aquí; estos logros la refuerzan.

La condición se calcula con `min(userItems.addedAt)` comparado con la fecha actual.

| ID | Nombre | Condición | Target | Icono | Horizonte |
|----|--------|-----------|--------|-------|-----------|
| `anniversary_1` | Primer Aniversario | 1 año desde el primer item añadido | 365 días | 🎂 | 1 año |
| `anniversary_2` | Veterano | 2 años en la app | 730 días | 🎖️ | 2 años |
| `anniversary_3` | Veterano de Guerra | 3 años en la app | 1095 días | 🏅 | 3 años |

---

**CATEGORÍA: SECRETOS — Easter eggs ocultos**

No aparecen en la lista hasta que se desbloquean (se muestran como `???`).
Crean sorpresa y recompensa el comportamiento orgánico.
Cuando se desbloquea uno, aparece en pantalla completa con animación especial.

| ID | Nombre real | Condición oculta | Descripción al desbloquear | Icono |
|----|-------------|-----------------|----------------------------|-------|
| `night_owl` | El Noctámbulo | Registrar actividad entre 00:00 y 04:00 en 3 noches distintas | "Parece que eres más productivo de noche..." | 🦉 |
| `domino_effect` | Efecto Dominó | Completar 5 items en 5 días consecutivos distintos | "Una pieza cae y arrastra a las demás" | 🎲 |
| `the_return` | El Retorno | Mover un item de ABANDONED a IN_PROGRESS y completarlo | "Nada está perdido para siempre" | 🔄 |
| `one_more_chapter` | Un Capítulo Más | Registrar sesión de lectura con timestamp ≥ 23:30 | "Solo uno más... y luego otro..." | 🕯️ |
| `clean_slate` | Página en Blanco | Añadir 10 items a Watchlist sin completar ninguno todavía | "La lista de pendientes también es un sueño" | 📋 |

---

**Resumen total: 12 existentes + 33 nuevos = 45 logros**

```
Corto plazo (semanas):  book_streak_7, pages_1k, critic, chronicler, weekly_habit
Medio plazo (meses):    pages_5k, cinephile, series_marathon, season_master, genre_polymath,
                        triangular, speed_reader, monthly_reader, streak_100, grand_collector
Largo plazo (años):     pages_10k, pages_50k, blockbuster, archivist, decade_hopper,
                        world_citizen, streak_365, epic_watcher, anniversary_1/2/3
Secretos (cualquier):   night_owl, domino_effect, the_return, one_more_chapter, clean_slate
```

**Nuevo modelo:**

```kotlin
enum class AchievementCondition {
    // Existentes (12)
    FIRST_ADD, FIRST_COMPLETE, SERIES_FAN, MOVIE_MARATHON, BOOKWORM,
    COMPLETIONIST, CENTURION, EXPLORER, CURATOR, STREAK_7, STREAK_30, DIVERSE,
    // Volumen
    DEVOTEE, GRAND_COLLECTOR, ARCHIVIST,
    // Lectura
    PAGES_1K, PAGES_5K, PAGES_10K, PAGES_50K,
    SPEED_READER, MONTHLY_READER, BOOK_STREAK_7,
    // Series
    SERIES_MARATHON, SEASON_MASTER, EPIC_WATCHER,
    // Cine
    CINEPHILE, BLOCKBUSTER, AUTEUR_FAN,
    // Constancia
    STREAK_100, STREAK_365, PHOENIX, WEEKLY_HABIT,
    // Diversidad
    DECADE_HOPPER, WORLD_CITIZEN, GENRE_POLYMATH, TRIANGULAR,
    // Calidad
    CRITIC, CHRONICLER, PERFECTIONIST,
    // Fidelidad
    ANNIVERSARY_1, ANNIVERSARY_2, ANNIVERSARY_3,
    // Secretos
    NIGHT_OWL, DOMINO_EFFECT, THE_RETURN, ONE_MORE_CHAPTER, CLEAN_SLATE,
}
```

Nuevo campo en `AchievementDef`:

```kotlin
data class AchievementDef(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val condition: AchievementCondition,
    val target: Int,
    val isSecret: Boolean = false,    // oculto hasta desbloquear
    val category: AchievementCategory,
)

enum class AchievementCategory {
    VOLUME, READING, SERIES, CINEMA,
    CONSISTENCY, DIVERSITY, QUALITY, LOYALTY, SECRET
}
```

**AchievementsScreen — UX renovada:**

```
┌─────────────────────────────────────────────────────────┐
│  🏆 Logros  (18 / 45 desbloqueados)                     │
│  ────────────────────────────────────────────────────── │
│  [ Todos ] [ Lectura ] [ Series ] [ Cine ]              │
│  [ Constancia ] [ Diversidad ] [ Calidad ] [ Fidelidad ]│
│                                                         │
│  ── Desbloqueados ─────────────────────────────────── ──│
│  ┌──────┐ ┌──────┐ ┌──────┐                            │
│  │ ✅   │ │ ✅   │ │ ✅   │                            │
│  │Primer│ │Mission│ │Racha │                            │
│  │Paso  │ │Accom. │ │Sem.  │                            │
│  └──────┘ └──────┘ └──────┘                            │
│                                                         │
│  ── En progreso ───────────────────────────────────── ──│
│  ┌──────┐ ┌──────┐ ┌──────┐                            │
│  │ 🔒   │ │ 🔒   │ │ 🔒   │                            │
│  │Mil   │ │Cinéf.│ │Crit. │                            │
│  │Págs. │ │      │ │      │                            │
│  │647/1K│ │18/25 │ │ 9/25 │                            │
│  └──────┘ └──────┘ └──────┘                            │
│                                                         │
│  ── Secretos ─────────────────────────────────────── ──│
│  ┌──────┐ ┌──────┐ ┌──────┐                            │
│  │  ??? │ │  ??? │ │  ??? │                            │
│  │      │ │      │ │      │                            │
│  │Secre-│ │Secre-│ │Secre-│                            │
│  │  to  │ │  to  │ │  to  │                            │
│  └──────┘ └──────┘ └──────┘                            │
└─────────────────────────────────────────────────────────┘
```

- Los secretos no desbloqueados se muestran como `???` sin pista de condición.
- Al desbloquear un secreto: animación de "revelación" (fade + partículas doradas).
- Barra de progreso en tiempo real para logros con target numérico.
- Sección "En progreso" muestra solo los 3 más cercanos a completarse.

- [ ] 33 nuevos `AchievementCondition` en el enum
- [ ] Campo `isSecret` y `category` en `AchievementDef`
- [ ] 33 nuevas `AchievementDef` en `ACHIEVEMENT_DEFS`
- [ ] `CheckAchievementsUseCase` evalúa condiciones de páginas, rachas, aniversarios, timestamps nocturnos
- [ ] Track de `totalPagesRead` acumulado para logros `PAGES_*`
- [ ] Track de `firstItemDate` para logros `ANNIVERSARY_*`
- [ ] Lógica del Fénix: guardar `longestLostStreak` en `StreakEntity`
- [ ] Tabs de categoría en `AchievementsScreen` con scroll horizontal
- [ ] Sección "Secretos" con cards opacas y efecto `???`
- [ ] Animación de revelación especial para logros secretos
- [ ] Animación de desbloqueo estándar mejorada (escala + brillo + notificación)

---

#### T5: Sistema de Niveles — De 6 a 15 Niveles con Narrativa

Archivos afectados: `GetUserStatsUseCase.kt`, `Gamification.kt`, `ProfileScreen.kt`, `FanCardScreen.kt`

**Filosofía:** Los títulos no son solo palabras, son una narrativa. El usuario empieza siendo
un "Curioso" y puede aspirar a convertirse en un "Oráculo Cultural" si usa la app durante
10+ años. Cada título tiene que sentirse ganado.

**Con 6 niveles actuales, "Leyenda" llega a los 100 completados (~1 año de uso activo). Después
no hay nada. Con 15 niveles, el endgame queda a años de distancia — sin ser inalcanzable.**

---

**Nueva tabla de niveles — El Viaje del Fan**

| N | Título | Icono | Min Items | XP Inicio | Tiempo estimado* |
|---|--------|-------|-----------|-----------|-----------------|
| 1 | Curioso | 👁️ | 0 | 0 | Día 1 |
| 2 | Iniciado | 🎟️ | 3 | 100 | Semana 1 |
| 3 | Aficionado | 📱 | 8 | 350 | Mes 1 |
| 4 | Entusiasta | ⭐ | 15 | 800 | Mes 2-3 |
| 5 | Seguidor | 🎭 | 25 | 1.500 | Mes 4-5 |
| 6 | Conocedor | 🔍 | 40 | 2.800 | Mes 6-8 |
| 7 | Experto | 🏆 | 60 | 4.800 | Mes 9-12 |
| 8 | Crítico | 📝 | 90 | 8.000 | Año 1-1.5 |
| 9 | Analista | 🧠 | 130 | 13.000 | Año 1.5-2 |
| 10 | Erudito | 📚 | 200 | 20.000 | Año 2-3 |
| 11 | Maestro | 💎 | 300 | 31.000 | Año 3-4 |
| 12 | Virtuoso | 🎯 | 450 | 47.000 | Año 4-6 |
| 13 | Leyenda | 👑 | 650 | 68.000 | Año 6-8 |
| 14 | Mítico | 🌟 | 900 | 95.000 | Año 8-10 |
| 15 | Oráculo | 🔮 | 1.200+ | 130.000 | Año 10+ |

*Estimado para un usuario activo que completa ~8 items/mes.*

> Los niveles 13-15 no son para todos — son el "Monte Everest" de FanApp.
> El usuario que los alcanza lleva años de vida cultural registrada aquí.
> Eso tiene valor real y debe sentirse como un logro extraordinario.

---

**Sistema XP — Multiplicador de Racha**

La novedad clave: las rachas no solo dan XP al llegar a hitos, sino que multiplican todo el XP
ganado mientras están activas. Romper una racha larga duele económicamente, lo que crea el
mayor incentivo de retención que existe.

| Acción | XP Base |
|--------|---------|
| Añadir item | +5 |
| Poner en progreso | +10 |
| Completar item | +25 |
| Completar con rating ≥ 4.0 | +30 (bonus +5 por calidad) |
| Favorito | +5 |
| Registrar sesión de lectura | +0.5 por página leída |
| Valorar item | +3 |
| Escribir nota | +5 |
| Desbloquear logro estándar | +15 |
| Desbloquear logro secreto | +50 |
| Completar reto semanal | +30 |
| Completar reto mensual | +150 |
| Completar reto de temporada | +500 |
| Completar reto anual | +1.000 |

**Multiplicadores activos según racha:**

| Racha activa | Multiplicador XP |
|--------------|------------------|
| < 7 días | × 1.0 (base) |
| 7-29 días | × 1.1 (+10%) |
| 30-99 días | × 1.25 (+25%) |
| 100-364 días | × 1.5 (+50%) |
| 365+ días | × 2.0 (double XP) |

> Una racha de un año dobla todo el XP ganado. Si el usuario la rompe, pierde no solo los
> días acumulados sino también la ventaja económica futura — un incentivo poderoso para volver
> rápido y reconstruir.

**Hitos de racha con XP bonus único (además del multiplicador):**

| Hito | Bonus único |
|------|-------------|
| 7 días | +50 XP |
| 30 días | +200 XP |
| 100 días | +1.000 XP |
| 365 días | +5.000 XP |

---

**UI — ProfileScreen (vista completa):**

```
┌─────────────────────────────────────────────┐
│  👤 Hugo Pérez                              │
│                                             │
│  Nivel 8 — Crítico 📝                       │
│  ██████████████░░░░░  9.240 / 13.000 XP    │
│  Siguiente: Analista 🧠  (falta 3.760 XP)  │
│                                             │
│  🔥 Racha: 34 días  ×1.25 XP activo        │
│                                             │
│  📊 92 completados   🏆 21 logros           │
│  📖 12.450 páginas leídas                  │
│  🗓️ Miembro desde mayo 2025                │
└─────────────────────────────────────────────┘
```

**FanCard — nueva info de nivel:**

```
┌──────────────────────────────┐
│  ⬛  Hugo Pérez              │
│     Crítico · Nivel 8 📝     │
│                              │
│  92 items · 21 🏆 · 34🔥    │
│  📖 12.450 páginas           │
│  ──────────────────────────  │
│  Series 38 · Pelis 31 · 📖 23│
└──────────────────────────────┘
```

---

**Código — LevelDef actualizado:**

```kotlin
private data class LevelDef(
    val level: Int,
    val title: String,
    val icon: String,
    val minItems: Int,
    val xpAtLevelStart: Int,
)

private val LEVEL_TABLE = listOf(
    LevelDef(1,  "Curioso",    "👁️",  0,      0),
    LevelDef(2,  "Iniciado",   "🎟️",  3,      100),
    LevelDef(3,  "Aficionado", "📱",  8,      350),
    LevelDef(4,  "Entusiasta", "⭐",  15,     800),
    LevelDef(5,  "Seguidor",   "🎭",  25,     1_500),
    LevelDef(6,  "Conocedor",  "🔍",  40,     2_800),
    LevelDef(7,  "Experto",    "🏆",  60,     4_800),
    LevelDef(8,  "Crítico",    "📝",  90,     8_000),
    LevelDef(9,  "Analista",   "🧠",  130,    13_000),
    LevelDef(10, "Erudito",    "📚",  200,    20_000),
    LevelDef(11, "Maestro",    "💎",  300,    31_000),
    LevelDef(12, "Virtuoso",   "🎯",  450,    47_000),
    LevelDef(13, "Leyenda",    "👑",  650,    68_000),
    LevelDef(14, "Mítico",     "🌟",  900,    95_000),
    LevelDef(15, "Oráculo",    "🔮",  1200,   130_000),
)
```

**XP calculation con multiplicador:**

```kotlin
fun calculateXpWithStreakMultiplier(baseXp: Int, currentStreak: Int): Int {
    val multiplier = when {
        currentStreak >= 365 -> 2.0f
        currentStreak >= 100 -> 1.5f
        currentStreak >= 30  -> 1.25f
        currentStreak >= 7   -> 1.1f
        else                 -> 1.0f
    }
    return (baseXp * multiplier).toInt()
}
```

- [ ] `LEVEL_TABLE` expandida de 6 a 15 niveles en `GetUserStatsUseCase`
- [ ] Nuevos títulos e iconos visibles en Profile y FanCard
- [ ] XP con multiplicador de racha calculado en `calculateRetroactiveXp`
- [ ] XP por páginas (+0.5/página con multiplicador activo)
- [ ] XP por valorar (+3), nota (+5), logro estándar (+15), logro secreto (+50)
- [ ] Badge `×1.25 XP activo` visible en ProfileScreen cuando hay racha ≥ 30 días
- [ ] Barra de XP con animación smooth en ProfileScreen
- [ ] Páginas totales y fecha de primer uso visibles en Profile
- [ ] FanCard actualizada con nivel, páginas y multiplicador activo

---

### Media — Semana 3

---

#### T6: Tracking de Páginas Diarias y Velocidad de Lectura

Archivos a crear: `ReadingSessionDao.kt`, `ReadingSessionEntity.kt`, `LogReadingSessionUseCase.kt`

**Nuevo modelo — sesiones de lectura:**

```kotlin
@Entity(tableName = "reading_sessions")
data class ReadingSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userItemId: String,
    val startPage: Int,
    val endPage: Int,
    val pagesRead: Int, // endPage - startPage
    val timestamp: Long,
)
```

**Room migration v8 → v9.**

**UI — DetailScreen (libros IN_PROGRESS):**

Botón "Registrar lectura" → dialogo:

```
┌─────────────────────────────────────────┐
│  📖 Registrar lectura                   │
│                                         │
│  De página: [247]                       │
│  A página:  [290]                       │
│                                         │
│  Páginas leídas: 43                     │
│  Tiempo: ~1h 26min (a 2min/pág)        │
│                                         │
│  [ Cancelar ]        [ Guardar ]        │
└─────────────────────────────────────────┘
```

**Beneficios:**
- Calcula velocidad de lectura real del usuario (páginas/día promedio).
- Estimación de tiempo restante precisa basada en velocidad real.
- Logros como `book_rapid` (completar libro en <3 días) y `pages_*` (páginas totales).
- Datos para estadísticas futuras (Sprint 7 T5).

- [ ] ReadingSessionEntity + DAO + migration v8→v9
- [ ] Diálogo de registrar lectura en DetailScreen
- [ ] Velocidad de lectura calculada (páginas/día)
- [ ] Estimación de tiempo restante basada en velocidad real
- [ ] Logro `book_rapid` detectable con datos de sesión

---

#### T7: Resumen Semanal de Actividad

Archivos a crear: `WeeklySummaryUseCase.kt`, `WeeklySummaryNotification.kt`

**Resumen generado cada domingo a las 20:00 (WorkManager):**

```
┌─────────────────────────────────────────┐
│  📊 Tu semana en FanApp                 │
│                                         │
│  📖 120 páginas leídas (3 sesiones)     │
│  📺 5 episodios vistos                  │
│  🎬 2 películas completadas             │
│  🔥 Racha: 12 días                     │
│  🏆 1 logro desbloqueado                │
│                                         │
│  XP ganado esta semana: +145           │
│                                         │
│  [ Ver detalle ]     [ Compartir ]      │
└─────────────────────────────────────────┘
```

- Notificación push con el resumen.
- Tappable → pantalla de estadísticas (T5 de Sprint 7 si existe, o Home si no).
- "Compartir" → FanCard semanal.

- [ ] WorkManager schedula resumen semanal
- [ ] Notificación con resumen de actividad
- [ ] Cálculo de XP semanal
- [ ] Opción de compartir como FanCard semanal

---

### Baja — Semana 4

---

#### T8: Badges de Progreso en Library — Sistema Expresivo

Archivos afectados: `LibraryItemCard.kt`, `LibraryScreen.kt`

**Los badges no son solo etiquetas — son la manera en que la Library "habla" al usuario.
Deben dar información útil de un vistazo y reflejar el estado emocional del item.**

---

**Badges de estado (esquina inferior derecha del poster):**

| Tipo | Estado | Datos disponibles | Badge | Color |
|------|--------|-------------------|-------|-------|
| Libro | IN_PROGRESS | Páginas registradas | `📄 37%` | Azul activo |
| Libro | IN_PROGRESS | Sin páginas | `📖 Leyendo` | Azul suave |
| Serie | IN_PROGRESS | T + E registrados | `📺 T3·E7` | Violeta activo |
| Serie | IN_PROGRESS | Solo temporada | `📺 T3` | Violeta suave |
| Serie | IN_PROGRESS | Sin T/E | `📺 Viendo` | Violeta suave |
| Peli | IN_PROGRESS | — | `🎬 Pendiente` | Amarillo |
| Libro | COMPLETED | Con rating | `⭐ 4.5` | Dorado |
| Serie | COMPLETED | Con rating | `⭐ 4.5` | Dorado |
| Peli | COMPLETED | Con rating | `⭐ 4.5` | Dorado |
| Cualquiera | COMPLETED | Sin rating | `✅` | Verde |
| Cualquiera | WATCHLIST | — | `🔖` | Gris |
| Cualquiera | ABANDONED | — | `✕` | Rojo suave |

**Badges de contexto (esquina superior izquierda — máximo 1 simultáneo):**

| Condición | Badge | Descripción |
|-----------|-------|-------------|
| Añadido/completado hoy | `🔥 Hoy` | Item tocado en las últimas 24h |
| Completado esta semana | `✨ Esta semana` | Fresco en memoria |
| Favorito | `♥` | Marcado como favorito |
| Parte de racha activa | `🔥` (pulso) | Racha en curso y este item contribuyó |

> Solo se muestra UN badge de contexto (por prioridad: `Hoy` > `Esta semana` > `♥` > `🔥`).

**Animaciones:**
- Items `IN_PROGRESS` con porcentaje: pulso suave cada 3s en el badge de % (para que destaquen
  en la grid y recuerden al usuario que están a medias).
- Al llegar a COMPLETED: breve animación de "explosión" verde sobre el poster (1 frame, escala 0→1.2→1).
- Badge `Hoy`: brillo intermitente muy sutil (no molesto).

**Color coding del borde del poster:**
```
IN_PROGRESS  → borde izquierdo 2dp color Primary (azul/violeta según tipo)
COMPLETED    → sin borde destacado (visual limpio)
WATCHLIST    → borde izquierdo 2dp gris
ABANDONED    → overlay semitransparente oscuro + ✕ centrado
```

**Implementación — `BadgeData` sealed class:**

```kotlin
sealed class LibraryBadge {
    data class Progress(val percent: Int, val mediaType: MediaType) : LibraryBadge()
    data class EpisodeProgress(val season: Int, val episode: Int?) : LibraryBadge()
    data class Rating(val score: Float) : LibraryBadge()
    object Completed : LibraryBadge()
    object Watchlist : LibraryBadge()
    object Abandoned : LibraryBadge()
    object InProgressNoData : LibraryBadge()
}

sealed class ContextBadge {
    object Today : ContextBadge()
    object ThisWeek : ContextBadge()
    object Favorite : ContextBadge()
    object StreakActive : ContextBadge()
}

fun UserItem.toLibraryBadge(): LibraryBadge = when (status) { ... }
fun UserItem.toContextBadge(nowMs: Long): ContextBadge? { ... }
```

- [ ] `LibraryBadge` y `ContextBadge` sealed classes
- [ ] Función `toLibraryBadge()` en `UserItem`
- [ ] Función `toContextBadge()` con lógica de "hoy/esta semana/favorito"
- [ ] `LibraryItemCard` renderiza ambos badges correctamente
- [ ] Borde izquierdo coloreado según estado
- [ ] Overlay semitransparente para ABANDONED
- [ ] Pulso animado en badges IN_PROGRESS con porcentaje
- [ ] Animación de completado al cambiar a COMPLETED desde la Library

---

#### T9: Sistema de Retos — 4 Capas para Retención a Todos los Plazos

Archivos a crear: `ChallengeEntity.kt`, `ChallengeDao.kt`, `ChallengeScreen.kt`,
`ChallengeViewModel.kt`, `ChallengeRepository.kt`, `WeeklyChallengePool.kt`, `AnnualGoalEntity.kt`

**El error más común en los sistemas de retos es que son estáticos. El usuario los ve una vez,
los completa (o no) y deja de mirar esa pantalla. La solución: retos que ROTAN y tienen
temas reales ligados al calendario. La app debe sentirse "viva" cada semana.**

---

**Arquitectura: 4 capas de reto, cada una con un propósito distinto**

| Capa | Rotación | XP | Propósito |
|------|----------|-----|-----------|
| Semanales (3 activos) | Cada lunes | +30/reto | Vuelve esta semana |
| Mensual (1 activo) | Cada mes | +150 | Objetivo del mes |
| Temporada (1 activo) | Cada 3 meses | +500 + badge | Meta trimestral |
| Anual Personal | 1 enero, elige el usuario | +1.000 | Identidad anual |
| Personal (libre) | El usuario controla | +50 | Objetivos propios |

---

**CAPA 1 — Retos Semanales Rotativos**

3 retos activos cada semana. Rotan cada lunes a las 00:00. El pool tiene 24 retos agrupados
en 8 grupos de 3 — se selecciona el grupo usando `weekOfYear % 8`. Sin servidor: puro
determinismo local basado en la fecha.

```kotlin
object WeeklyChallengePool {
    val groups = listOf(
        // Grupo 0 — Semanas 0, 8, 16, 24, 32, 40, 48
        listOf(
            WeeklyChallengeDef("w_complete_1",    "Completa 1 item esta semana",         ChallengeType.COMPLETE_ANY, 1),
            WeeklyChallengeDef("w_add_3",         "Añade 3 items a tu lista",             ChallengeType.ADD_ANY,      3),
            WeeklyChallengeDef("w_rate_5",        "Valora 5 items",                       ChallengeType.RATE_ANY,     5),
        ),
        // Grupo 1
        listOf(
            WeeklyChallengeDef("w_pages_100",     "Lee 100 páginas esta semana",          ChallengeType.READ_PAGES,   100),
            WeeklyChallengeDef("w_complete_series","Completa 1 serie",                    ChallengeType.COMPLETE_SERIES, 1),
            WeeklyChallengeDef("w_streak_3",      "Mantén la racha 3 días seguidos",      ChallengeType.STREAK_DAYS,  3),
        ),
        // Grupo 2
        listOf(
            WeeklyChallengeDef("w_complete_movie", "Completa 2 películas",                ChallengeType.COMPLETE_MOVIE, 2),
            WeeklyChallengeDef("w_note_3",         "Escribe una nota en 3 items",         ChallengeType.WRITE_NOTES,  3),
            WeeklyChallengeDef("w_book_session",   "Registra 3 sesiones de lectura",      ChallengeType.BOOK_SESSIONS, 3),
        ),
        // Grupo 3
        listOf(
            WeeklyChallengeDef("w_complete_2",    "Completa 2 items esta semana",         ChallengeType.COMPLETE_ANY, 2),
            WeeklyChallengeDef("w_watchlist_5",   "Añade 5 items a Watchlist",            ChallengeType.ADD_WATCHLIST, 5),
            WeeklyChallengeDef("w_pages_200",     "Lee 200 páginas",                      ChallengeType.READ_PAGES,   200),
        ),
        // Grupo 4
        listOf(
            WeeklyChallengeDef("w_complete_book", "Completa 1 libro",                     ChallengeType.COMPLETE_BOOK, 1),
            WeeklyChallengeDef("w_streak_5",      "Racha de 5 días seguidos",             ChallengeType.STREAK_DAYS,   5),
            WeeklyChallengeDef("w_favorite_2",    "Añade 2 items a favoritos",            ChallengeType.ADD_FAVORITE,  2),
        ),
        // Grupo 5
        listOf(
            WeeklyChallengeDef("w_complete_3",    "Completa 3 items esta semana",         ChallengeType.COMPLETE_ANY,  3),
            WeeklyChallengeDef("w_rate_10",       "Valora 10 items (al día o acumulados)",ChallengeType.RATE_ANY,      10),
            WeeklyChallengeDef("w_complete_peli", "Completa 1 película",                  ChallengeType.COMPLETE_MOVIE, 1),
        ),
        // Grupo 6
        listOf(
            WeeklyChallengeDef("w_pages_150",     "Lee 150 páginas",                      ChallengeType.READ_PAGES,   150),
            WeeklyChallengeDef("w_add_series",    "Añade 2 series a tu lista",            ChallengeType.ADD_SERIES,   2),
            WeeklyChallengeDef("w_note_5",        "Escribe notas en 5 items",             ChallengeType.WRITE_NOTES,  5),
        ),
        // Grupo 7
        listOf(
            WeeklyChallengeDef("w_complete_all",  "Completa 1 de cada tipo esta semana",  ChallengeType.COMPLETE_ALL_TYPES, 1),
            WeeklyChallengeDef("w_streak_7",      "Racha de 7 días seguidos",             ChallengeType.STREAK_DAYS,   7),
            WeeklyChallengeDef("w_in_progress_2", "Pon 2 items en progreso",              ChallengeType.SET_IN_PROGRESS, 2),
        ),
    )

    fun forWeek(weekOfYear: Int) = groups[weekOfYear % groups.size]
}
```

Los retos semanales NO se guardan en Room — se generan en memoria. Solo se persiste
si el usuario los completó (para dar el XP y mostrar historial).

---

**CAPA 2 — Reto Mensual**

Un reto por mes, temático. Definidos estáticamente por número de mes (1-12).
Más exigentes que los semanales. Se activan el día 1 del mes y expiran el último día.

| Mes | Emoji | Título | Condición | Target |
|-----|-------|--------|-----------|--------|
| Enero | 📚 | Comienzo de Año | Completa 5 items en enero | 5 |
| Febrero | 🎬 | Mes del Cine | 4 películas completadas | 4 |
| Marzo | 🌱 | Despertar Lector | 1 libro/semana × 3 semanas | 3 libros |
| Abril | 📖 | Lectura Profunda | 1.500 páginas en abril | 1.500 pág |
| Mayo | 📺 | Maratón de Primavera | 2 series completadas | 2 |
| Junio | 🌊 | Verano que Empieza | 8 completados antes del verano | 8 |
| Julio | 🏖️ | Lectura de Verano | 2 libros en julio | 2 |
| Agosto | 🎭 | Festival de Cine | 6 películas en agosto | 6 |
| Septiembre | 📚 | Regreso a las Páginas | 1.000 páginas en septiembre | 1.000 pág |
| Octubre | 🎃 | Mes del Terror | 5 completados de género terror/thriller | 5 |
| Noviembre | 📖 | NovemberReads | 4 libros en noviembre | 4 |
| Diciembre | 🎁 | Gran Final de Año | 10 completados para cerrar el año | 10 |

> El Reto de Octubre con genre=Terror crea una oportunidad viral ("¿cuánto terror has visto?").
> Noviembre es un guiño al NaNoWriMo que los lectores conocen.

---

**CAPA 3 — Retos de Temporada (4/año)**

Uno por temporada, activo 3 meses. XP alto + badge exclusivo que no se puede conseguir
después. La exclusividad temporal crea FOMO real.

| Temporada | Activo | Título | Condición | Badge exclusivo |
|-----------|--------|--------|-----------|----------------|
| Invierno | Dic-Feb | Maratón de Invierno | 20 completados | ❄️ Guardián del Invierno |
| Primavera | Mar-May | Despertar Cultural | Items en 4 géneros distintos | 🌸 Explorador Primaveral |
| Verano | Jun-Ago | Atracón de Verano | 5 series completas | ☀️ Rey del Binge |
| Otoño | Sep-Nov | Otoño Literario | 6 libros + 2.000 páginas | 🍂 Lector Otoñal |

El badge de temporada aparece visible en el perfil del usuario durante ese trimestre.
Después del período, queda en los logros históricos pero ya no se puede ganar.

---

**CAPA 4 — Reto Anual Personal (el más importante)**

Inspirado en el Reading Challenge de Goodreads. El 1 de enero (o al instalar la app),
el usuario elige cuántos items quiere completar ese año. Esto crea un compromiso público
(visible en el Leaderboard) y una meta personal que dura todo el año.

```
┌─────────────────────────────────────────────┐
│  🎯 Tu Reto de 2026                         │
│                                             │
│  Meta: 52 completados (1 por semana)        │
│  ████████████████░░░░░░░░  34 / 52          │
│  Llevas 34 — ¡vas en ritmo!                 │
│                                             │
│  Proyección: terminarás el año en 58        │
│                                             │
│  📺 Series: 18   🎬 Pelis: 11   📖 Libros: 5│
│                                             │
│  [ Cambiar meta ]                           │
└─────────────────────────────────────────────┘
```

El usuario puede elegir una meta predefinida o introducir un número propio:
- 🟢 Relajado: 12 (1 al mes)
- 🟡 Normal: 30 (casi 1 cada 2 semanas)
- 🟠 Ambicioso: 52 (1 por semana)
- 🔴 Extremo: 100+
- ✏️ Personalizado: el usuario escribe su número

El progreso anual es visible en el Leaderboard junto al nivel del usuario.

---

**CAPA 5 — Retos Personales (siempre disponibles)**

El usuario crea sus propios retos en cualquier momento. Sin fecha límite opcional.

```
[ + Crear reto ]
  Tipo: [ Series ] [ Pelis ] [ Libros ] [ Todo ]
  Meta: [ 5 ] items  o  [ 500 ] páginas
  Plazo: [ 30 días ] o [ Sin límite ]
```

XP al completar: +50 (independientemente del tipo).

---

**Modelo de datos:**

```kotlin
@Entity(tableName = "challenge_completions")
data class ChallengeCompletionEntity(
    @PrimaryKey val id: String,          // "weekly_2026_w12_g3_0" / "monthly_2026_04" / etc.
    val type: String,                    // "WEEKLY" | "MONTHLY" | "SEASONAL" | "ANNUAL" | "PERSONAL"
    val title: String,
    val completedAt: Long,
    val xpAwarded: Int,
)

@Entity(tableName = "annual_goals")
data class AnnualGoalEntity(
    @PrimaryKey val year: Int,
    val targetCount: Int,
    val currentCount: Int,
    val completedAt: Long? = null,
)

@Entity(tableName = "personal_challenges")
data class PersonalChallengeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val targetType: String,         // "BOOK" | "SERIES" | "MOVIE" | "ALL" | "PAGES"
    val targetCount: Int,
    val currentCount: Int,
    val deadlineAt: Long?,
    val createdAt: Long,
    val completedAt: Long?,
)
```

**Room migration v9 → v10:**

```kotlin
val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""CREATE TABLE challenge_completions (
            id TEXT PRIMARY KEY NOT NULL,
            type TEXT NOT NULL,
            title TEXT NOT NULL,
            completedAt INTEGER NOT NULL,
            xpAwarded INTEGER NOT NULL
        )""")
        db.execSQL("""CREATE TABLE annual_goals (
            year INTEGER PRIMARY KEY NOT NULL,
            targetCount INTEGER NOT NULL,
            currentCount INTEGER NOT NULL,
            completedAt INTEGER
        )""")
        db.execSQL("""CREATE TABLE personal_challenges (
            id TEXT PRIMARY KEY NOT NULL,
            title TEXT NOT NULL,
            targetType TEXT NOT NULL,
            targetCount INTEGER NOT NULL,
            currentCount INTEGER NOT NULL,
            deadlineAt INTEGER,
            createdAt INTEGER NOT NULL,
            completedAt INTEGER
        )""")
    }
}
```

---

**UI — ChallengeScreen:**

```
┌─────────────────────────────────────────────────────────┐
│  🎯 Retos  [ Semanales ] [ Mensual ] [ Temporada ]      │
│            [ Anual ]    [ Mis Retos ]                   │
│─────────────────────────────────────────────────────────│
│  ── Esta semana (rota el lunes) ─────────────────────── │
│  ┌──────────────────────────────────────────────────┐   │
│  │  📖 Lee 100 páginas esta semana          +30 XP  │   │
│  │  ██████░░░░░░░░░░░░  64 / 100                    │   │
│  │  Quedan 5 días                                   │   │
│  └──────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────┐   │
│  │  ✅ Añade 3 items a tu lista             +30 XP  │   │
│  │  ████████████████████  3 / 3   Completado!       │   │
│  └──────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────┐   │
│  │  ⭐ Valora 5 items                       +30 XP  │   │
│  │  ████████░░░░░░░░░░░░  2 / 5                     │   │
│  └──────────────────────────────────────────────────┘   │
│─────────────────────────────────────────────────────────│
│  ── Reto Mensual — Junio ────────────────────────────── │
│  🌊 Verano que Empieza · 8 completados antes del verano │
│  ████████░░░░░░░░░░░░  5 / 8  · Quedan 18 días · +150 XP│
│─────────────────────────────────────────────────────────│
│  ── Temporada de Verano ─────────────────────────────── │
│  ☀️ Rey del Binge · 5 series completas · Jun-Ago        │
│  ██░░░░░░░░░░░░░░░░░░  1 / 5  · Quedan 78 días · +500 XP│
│─────────────────────────────────────────────────────────│
│  ── Tu Reto Anual ────────────────────────────────────  │
│  🎯 Meta 2026: 52 completados                           │
│  ████████████████░░░░  34 / 52  · en ritmo ✓           │
└─────────────────────────────────────────────────────────┘
```

Al completar cualquier reto: animación de confeti + Snackbar con XP ganado.

- [ ] `WeeklyChallengePool` con 24 retos en 8 grupos
- [ ] Selección determinista de grupo semanal por `weekOfYear % 8`
- [ ] 12 retos mensuales temáticos en un objeto estático
- [ ] 4 retos de temporada con badge exclusivo y fecha de expiración
- [ ] `AnnualGoalEntity` + DAO + selector de meta el 1 de enero
- [ ] `PersonalChallengeEntity` + DAO + UI de creación
- [ ] `ChallengeScreen` con tabs para cada capa
- [ ] `ChallengeRepository` con lógica de evaluación de progreso
- [ ] XP correcto para cada capa (30 / 150 / 500 / 1000 / 50)
- [ ] Badge de temporada visible en ProfileScreen durante el período activo
- [ ] Progreso del Reto Anual visible en Leaderboard junto al nivel
- [ ] Migration v9→v10 con 3 tablas nuevas
- [ ] Acceso a ChallengeScreen desde Profile y desde FloatingBottomNav

---

## Orden de Implementación

```
Semana 1: T1 Progreso de página (libros) → T2 Progreso visual (series)
Semana 2: T3 Auto-completar/auto-avance → T5 Niveles (15 niveles + XP multiplicador)
Semana 3: T4 Logros (45 total) → T6 Tracking sesiones de lectura
Semana 4: T8 Badges Library → T9 Sistema de Retos → T7 Resumen semanal
```

**Dependencias Room — respetar este orden estricto:**
```
Migration v7→v8  (T1: currentPage, totalPages)
Migration v8→v9  (T6: reading_sessions)
Migration v9→v10 (T9: challenge_completions, annual_goals, personal_challenges)
```

**Dependencias lógicas:**
- T4 (logros de páginas) requiere T6 (sesiones de lectura que acumulan páginas)
- T9 (reto anual) es independiente del resto, puede implementarse en paralelo con T7
- T5 (multiplicador de XP) debe estar antes de T4 (los logros secretos otorgan +50 XP)

---

## Estimación

| Tarea | Prioridad | Tiempo |
|-------|-----------|--------|
| T1 Progreso de página en libros | Alta | 2-3 días |
| T2 Progreso visual de series | Alta | 2-3 días |
| T3 Auto-completar y auto-avance | Alta | 1-2 días |
| T4 Expansión de logros (33 nuevos, 45 total) | Alta | 3-4 días |
| T5 Niveles (6→15) + multiplicador de XP | Alta | 2 días |
| T6 Tracking de sesiones de lectura | Media | 2-3 días |
| T7 Resumen semanal de actividad | Media | 1-2 días |
| T8 Badges expresivos en Library | Media | 1-2 días |
| T9 Sistema de Retos (4 capas) | Media | 3-4 días |
| **Total** | | **17-25 días** |

---

## Definición de Done

**Tracking (T1-T3):**
- [ ] Libros muestran progreso de página y % en DetailScreen y LibraryItemCard
- [ ] Series muestran barra de progreso y estado por temporada (chips T1✅ T2✅ T3🔵 T4⬜)
- [ ] Auto-completar sugerido al terminar un libro o llegar a la última temporada

**Gamificación (T4-T5):**
- [ ] 45 logros funcionales (12 originales + 33 nuevos) evaluados en `CheckAchievementsUseCase`
- [ ] Logros secretos se muestran como `???` hasta desbloquear con animación especial
- [ ] 3 logros de aniversario detectados por antigüedad del primer item
- [ ] El Fénix detectado correctamente (racha perdida >50 días + nueva racha)
- [ ] 15 niveles disponibles (Curioso → Oráculo)
- [ ] Multiplicador de XP activo según días de racha (×1.1 / ×1.25 / ×1.5 / ×2.0)
- [ ] Badge de multiplicador visible en ProfileScreen cuando racha ≥ 7 días
- [ ] XP incluye páginas (+0.5/pág), valoraciones (+3), notas (+5), logros (+15/+50)
- [ ] Barra de XP animada en ProfileScreen con nivel, título e icono
- [ ] FanCard actualizada con nivel, páginas totales y multiplicador activo

**Badges (T8):**
- [ ] Badges informativos en Library para los 3 tipos de media con todos los estados
- [ ] Badge de contexto (Hoy / Esta semana / Favorito) en esquina superior izquierda
- [ ] Borde izquierdo coloreado por estado (IN_PROGRESS / WATCHLIST)
- [ ] Overlay oscuro + ✕ para items ABANDONED
- [ ] Pulso animado en badges IN_PROGRESS con porcentaje

**Retos (T9):**
- [ ] Retos semanales (3 activos) rotan automáticamente cada lunes por `weekOfYear % 8`
- [ ] 12 retos mensuales temáticos activos en su mes correspondiente
- [ ] 4 retos de temporada activos con badge exclusivo y expiración real
- [ ] Reto Anual Personal configurable el 1 de enero (o al instalar)
- [ ] Retos personalizados con tipo + cantidad + plazo opcional
- [ ] XP correcto por capa: +30 / +150 / +500 / +1.000 / +50
- [ ] `ChallengeScreen` con tabs y progreso en tiempo real
- [ ] Progreso del Reto Anual visible en Leaderboard

**Técnico:**
- [ ] Resumen semanal push notification (WorkManager, domingos 20:00)
- [ ] Migration chain v7→v8→v9→v10 sin pérdida de datos
- [ ] `./gradlew assembleDebug` sin warnings
- [ ] `./gradlew test` pasa
- [ ] Strings EN/ES en todos los archivos nuevos

---

## Nota sobre priorización

Si el tiempo aprieta:

**Imprescindible (MVP del sprint):**
T1 + T2 + T3 + T5 + T4 (tracking completo + niveles 15 + 45 logros con secretos)

**Muy recomendable:**
T8 + T9-capa1 (badges expresivos + retos semanales rotativos — son los de mayor impacto en retención a corto plazo)

**Diferir a Sprint 9:**
T6 (sesiones lectura), T7 (resumen semanal), T9-capas 2-4 (mensual / temporada / anual)
El Reto Anual especialmente puede vivir en Sprint 9 si se lanza en enero, que es cuando tiene más impacto.

---

## Impacto esperado por capa en retención

| Mecanismo | Horizonte | Efecto |
|-----------|-----------|--------|
| Badges de % en Library | Inmediato | El usuario ve su progreso de un vistazo y quiere completar |
| Retos semanales | 7 días | "Hay nuevos retos el lunes" → vuelve el lunes |
| Multiplicador de racha | 30+ días | Perder 30 días de racha duele — no lo pierdas |
| Logros de páginas (1K, 5K, 10K) | 1-12 meses | Meta siempre a la vista para lectores |
| Logros secretos | Variable | Sorpresa y boca a boca ("¡Hey, existe El Noctámbulo!") |
| Reto Anual Personal | 12 meses | "Voy a 34/52 — no puedo parar ahora" |
| Logros de aniversario | 1-3 años | La app celebra que llevas X años — identidad |
| Niveles 13-15 (Leyenda/Mítico/Oráculo) | 5-10 años | La meta que siempre existe aunque esté lejos |
