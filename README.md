# 📱 MediaTracker

> **🚧 En Desarrollo** — Este proyecto está en fase activa de construcción.
> Todo lo que ves aquí puede cambiar, romperse, o evolucionar.
> Bienvenido al proceso.

---

## 🎯 ¿Qué es MediaTracker?

Una app Android que unifica el seguimiento de **series**, **películas** y **libros** en un solo lugar.

| 🎬 Series | 🎥 Películas | 📖 Libros |
|-----------|-------------|-----------|

Olvídate de tener 3 apps distintas. Todo tu contenido, un solo sitio.

> Inspirado en Letterboxd, Goodreads y TVTime… pero todo junto.

---

## 🧭 Pantallas

```
┌──────────────────────────────────────────────────────┐
│   🏠 Home    🔍 Discover    📚 Library    👤 Profile  │
└──────────────────────────────────────────────────────┘
```

| Pantalla | Qué hace |
|----------|----------|
| 🏠 **Home** | Resumen: continúa donde lo dejaste, novedades, favoritos, últimos guardados |
| 🔍 **Discover** | Busca series, películas o libros por separado + ranking de trending |
| 📚 **Library** | Tus listas filtradas por estado: quiero ver, en progreso, completado, abandonado |
| 👤 **Profile** | Contadores, listas completas, ajustes de idioma, cerrar sesión |

> 🚧 Pantallas, nombres y flujos pueden cambiar durante el desarrollo.

---

## ⚙️ Stack Técnico

| Componente | Tecnología |
|------------|-----------|
| 🧠 Lenguaje | Kotlin |
| 🎨 UI | Jetpack Compose + Material 3 |
| 🏗️ Arquitectura | Clean Architecture + MVVM |
| 🔐 Auth | Firebase Auth (Google + Email/Password) |
| ☁️ DB Remota | Firebase Firestore |
| 💾 DB Local | Room (offline-first) |
| 🌐 APIs | TMDB (series/pelis) + Google Books |
| 🖼️ Imágenes | Coil |
| 🔌 Networking | Retrofit + OkHttp |
| 💉 DI | Hilt |
| 🧭 Navegación | Navigation Compose |
| 🌍 Idiomas | Español / English |

> 🚧 Stack puede ajustarse según necesidades que surjan durante el desarrollo.

---

## 🧱 Cómo se organiza el código

```
📦 com.mediatracker/
├── 🏗️ di/           → Hilt modules
├── 📡 data/          → APIs, Room, Firestore, repositorios
├── 🧠 domain/        → Modelos, interfaces, casos de uso
├── 🖥️ presentation/  → Screens, ViewModels, componentes
└── 🔧 core/          → Utilidades transversales
```

Regla de oro: la UI nunca habla directamente con APIs ni bases de datos.

---

## 📊 Estados de tu contenido

```
📋 Quiero ver/leer  →  🔄 En progreso  →  ✅ Completado  →  ❌ Abandonado
                        ⭐ Favorito (toggle independiente)
```

Cada serie, película o libro puede estar en un estado. Y puedes marcarlo como favorito aparte.

---

## 🗺️ Roadmap

```
📦 Fase 1 — MVP (en desarrollo)
├── Auth + setup del proyecto
├── Búsqueda + detalle de contenido
├── Listas del usuario sincronizadas
├── Home + Perfil con contadores
└── ¡Primera build funcional!

🚀 Fase 2 — Engagement
├── Botón Add rápido
├── Trackeo de temporadas en series
├── Notificaciones
├── Valoraciones y notas
└── Estadísticas

🤝 Fase 3 — Social
├── Perfiles públicos
├── Comparar listas con amigos
├── Recomendaciones
└── Compartir
```

> 🚧 Fechas, fases y features pueden cambiar.
> El roadmap es una guía, no un contrato.

---

## 🔑 Estado actual

```
████████░░░░░░░░░░░░  25% — Sprint 1: Setup + Auth
```

Estamos construyendo la base. Si quieres seguir el progreso, mira los commits y los ficheros del proyecto.

---

## ⚡ Quick Start (para developers)

```bash
git clone <repo>
# Abrir en Android Studio
# Añadir tus API keys en local.properties
TMDB_API_KEY=tu_key
GOOGLE_BOOKS_API_KEY=tu_key
# Poner google-services.json en app/
./gradlew assembleDebug
```

---

<div align="center">

**MediaTracker** — Unifica lo que consumes.

🚧 En Desarrollo • MVP en construcción • 2025

</div>
