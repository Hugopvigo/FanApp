# Changelog

## Sprint 5 — Bugs, Mejoras y Pendientes (2026-05-18–…)

### T1 — Fix Google Books Trending
- `GoogleBooksApi.kt`: query por defecto cambiada de `*` a `subject:fiction+subject:bestseller`
- `MediaRepositoryImpl.kt`: queries curatoras rotativas por día (5 géneros: ficción, fantasía, romance, thriller, no ficción)

### T10 — ProfileScreen reactivo
- `ProfileViewModel.kt`: inyecta `AuthDataSource`, expone `userName` y `userEmail` como `StateFlow` via `authStateFlow()` con valor inicial inmediato
- `ProfileScreen.kt`: elimina parámetros `userEmail`/`userName`, los colecta del ViewModel con `collectAsStateWithLifecycle()`
- `AppNavGraph.kt`: elimina `.value` imperativo, ya no pasa datos de auth a ProfileScreen

### T9 — Externalizar strings hardcodeados
- `values/strings.xml` + `values-es/strings.xml`: añadidos `status_*` (4 estados) y `media_type_*` (3 tipos)
- `StatusChip.kt`: `displayLabel: String` reemplazado por `@Composable fun label()` con `stringResource()`
- `LibraryItemCard.kt`: etiquetas de tipo de medio usan `stringResource(R.string.media_type_*)`

### T3 — Iconos en tabs de Library
- `StatusChip.kt`: añadida extension `tabIcon: ImageVector` en `ItemStatus` (Bookmark, PlayCircle, CheckCircle, Cancel)
- `LibraryScreen.kt`: tabs usan `icon` + `text` con Material Icons Outlined (16dp)

### T2 — Fix Library portadas/títulos
- `UserItem.kt`: añadidos campos `title: String` y `posterUrl: String?`
- `UserItemEntity`: añadidas columnas `title` y `posterUrl`
- `AppDatabase.kt`: versión 1→2, migración `MIGRATION_1_2` (ALTER TABLE con DEFAULT)
- `DatabaseModule.kt`: registrada la migración
- `EntityMappers.kt`: mappers actualizados; fallback `title = apiId` para items pre-Sprint5
- `UserRepository` + `UserRepositoryImpl` + `AddUserItemUseCase`: propagan `title`/`posterUrl`
- `FirestoreDataSource.kt`: guarda y lee `title`/`posterUrl` en Firestore
- `DetailViewModel.kt`: pasa `title`/`posterUrl` del `MediaItem` al añadir a lista
- `LibraryItemCard.kt`: muestra `userItem.title` y `userItem.posterUrl` (parámetro externo eliminado)

---

## Sprint 3 — APIs + Data Layer + Búsqueda + Library (2026-05-15–16)

### 🌐 APIs externas
- TMDB API: Retrofit interface + DTOs (`@Serializable`) + Mapper → `MediaItem`
  - Endpoints: search TV/movie, trending TV/movie, TV detail, movie detail
- Google Books API: Retrofit interface + DTOs + Mapper → `MediaItem`
  - Endpoints: search books, popular books, book detail
- `NetworkModule`: OkHttpClient (logging + API key interceptor) + 2 instancias Retrofit + `Json(ignoreUnknownKeys=true)`

### 🗃️ Base de datos local (Room)
- `Entities.kt`: `MediaItemEntity` + `UserItemEntity`
- `MediaItemDao.kt` + `UserItemDao.kt`
- `AppDatabase.kt` (versión 1)
- `EntityMappers.kt`: Entity ↔ Domain model
- `DatabaseModule.kt` implementado

### ☁️ Firestore
- `FirestoreDataSource.kt`: CRUD completo de `UserItem` en `/users/{userId}/items/{itemId}`

### 🏗️ Repositorios
- `MediaRepositoryImpl.kt`: search y trending con caché Room (TTL: trending 1h, búsqueda 30min, detalle 24h)
- `UserRepositoryImpl.kt`: offline-first (Room inmediato + sync Firestore en background)
- `RepositoryModule.kt` implementado

### 🧠 Use Cases
- `SearchMediaUseCase`, `GetTrendingUseCase`, `GetMediaDetailUseCase`
- `GetUserItemsUseCase`, `AddUserItemUseCase`, `UpdateItemStatusUseCase`
- `ToggleFavoriteUseCase`, `RemoveUserItemUseCase`, `GetUserStatsUseCase`
- `SyncUserItemsUseCase` (sincronización explícita Firestore → Room)

### 🖥️ Pantallas
- `DiscoverScreen`: 3 tabs (Series / Películas / Libros) con SearchBar + trending
- `DetailScreen`: póster, sinopsis, rating, selector de estado, toggle favorito, info extra
- `LibraryScreen`: tabs por estado + filtro por tipo + grid de items
- `LibraryItemCard`: card compacta para items del usuario en Library

### 🧩 Componentes reutilizables
- `MediaCard`, `MediaRow`, `SearchBar`, `StatusChip`, `FavoriteToggle`, `LoadingState`

### 🧭 Navegación
- `Route.Detail` actualizada: `Detail(apiId: String, mediaType: MediaType)`

### ✅ Build
- `./gradlew assembleDebug` — BUILD SUCCESSFUL

---

## Sprint 2 — Upgrade de Versiones (2026-05-15)

### 📦 Versiones actualizadas

| Dep | Antes | Después |
|-----|-------|---------|
| Gradle | 8.9 | 9.5.1 |
| AGP | 8.5.2 | 9.2.1 |
| Kotlin | 2.0.0 | 2.3.21 |
| Retrofit | 2.11.0 | 3.0.0 |
| OkHttp | 4.12.0 | 5.3.2 |
| Compose BOM | 2024.06.00 | 2026.05.00 |
| Room | 2.6.1 | 2.8.4 |
| Hilt | 2.51.1 | 2.59.2 |
| Firebase BOM | 33.1.2 | 34.13.0 |
| KSP | 2.0.0-1.0.22 | 2.3.8 |
| Navigation | 2.8.3 | 2.9.8 |
| Lifecycle | 2.8.3 | 2.10.0 |
| Coroutines | 1.8.1 | 1.11.0 |

### 🔧 Cambios adicionales
- Plugin `kotlin-android` eliminado (built-in en AGP 9.0+)
- Bloque `kotlinOptions` eliminado (deprecated en AGP 9.x)
- Moshi eliminado → Retrofit 3 usa `kotlinx.serialization` built-in
- Firebase `-ktx` artifacts unificados en BOM 34.x (non-ktx)
- `compileSdk` y `targetSdk` subidos de 34 → 36
- Mejora de manejo de errores en `AuthDataSource` (mapeo de excepciones Firebase)
- Integración de colores custom (`MediaBlue`, `MediaOrange`, etc.) en `MaterialTheme`
- Tests unitarios base: `AuthDataSourceTest`, `AuthViewModelTest`, tests de modelos

### ✅ Build
- `./gradlew assembleDebug` — BUILD SUCCESSFUL

---

## Sprint 1 — Setup + Auth (2026-05-15)

### 🏗️ Proyecto
- Creación del proyecto Android con Clean Architecture
- Configuración de Gradle con Version Catalog (`libs.versions.toml`)
- Build tools: AGP 8.5.2, Kotlin 2.0.0, Compose BOM 2024.06.00
- Hilt para inyección de dependencias

### 🔐 Auth
- Firebase Authentication integrado (Google + Email/Password)
- `AuthDataSource` en capa de datos con login y registro
- `AuthViewModel` con estados Loading/Success/Error
- Auth guard en navegación (redirige a Login si no hay sesión)

### 🧭 Navegación
- Navigation Compose 2.8.3 con type-safe routes (`@Serializable`)
- Bottom Navigation con 4 tabs: Inicio, Buscar, Biblioteca, Perfil
- Auth guard: Login ↔ Main graph

### 🎨 UI
- Material 3 Theme con soporte Dark Mode y Dynamic Colors
- Pantalla de Login (Google + Email/Password) con formulario completo
- Placeholders para Home, Discover, Library, Profile
- Soporte multiidioma: `strings.xml` ES + EN

### 📦 Estructura
```
com.mediatracker/
├── di/              → Hilt modules (Firebase, Network, Database, Repository)
├── data/
│   ├── auth/        → Firebase Auth DataSource
│   └── ...          → Preparado para Room, Retrofit, Firestore
├── domain/
│   └── model/       → MediaType, ItemStatus, MediaItem, UserItem
├── presentation/
│   ├── navigation/  → Rutas + NavGraph con auth guard
│   ├── theme/       → Material 3 Theme, Color, Type
│   ├── auth/        → LoginScreen + AuthViewModel
│   ├── home/        → HomeScreen (placeholder)
│   ├── discover/    → DiscoverScreen (placeholder)
│   ├── library/     → LibraryScreen (placeholder)
│   └── profile/     → ProfileScreen (placeholder)
└── core/            → Utilidades
```

### ✅ Build
- `./gradlew assembleDebug` — BUILD SUCCESSFUL (sin warnings)
