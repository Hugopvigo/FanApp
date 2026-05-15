# Changelog

## Sprint 1 — Setup + Auth (2025-05-15)

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

### 🔧 Entorno
- Android SDK 34 instalado localmente
- Gradle 8.9 + Wrapper
- `local.properties` con ruta SDK
- `google-services.json` pendiente de añadir

### ✅ Build
- `./gradlew assembleDebug` — BUILD SUCCESSFUL (sin warnings)
