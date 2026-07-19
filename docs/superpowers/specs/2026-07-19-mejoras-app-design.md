# Mejoras FanApp — diseño, email, libros y búsqueda

**Fecha:** 2026-07-19
**Estado:** Aprobado por Hugo

## Contexto

Cuatro molestias detectadas en la app:

1. La búsqueda en Descubrir muestra 2 columnas; Inicio y Biblioteca usan 3.
2. El banner "⚠️ Verifica tu email" del Perfil no desaparece nunca, aunque el
   usuario ya haya verificado. Causa: `AuthDataSource.isEmailVerified()` lee el
   valor cacheado de `FirebaseAuth.currentUser` sin hacer `reload()`.
3. Catálogo de libros: la búsqueda por autor genera frase exacta en vez de
   `inauthor:`, la deduplicación por título en minúsculas pierde/duplica obras,
   y "populares" son consultas rotatorias por género, no popularidad real.
4. Varias pantallas secundarias (Notificaciones, ajustes) siguen en Material
   plano en vez del estilo glass/iOS26 del resto de la app.

Alcance decidido con Hugo: diseño completo (coherencia glass + detalle +
animaciones + refresh Home/Discover), libros centrado en relevancia y trending
real, y verificación de email limitada a arreglar el banner (sin bloquear
funciones ni enviar correo al registrarse).

## Fase 1 — Arreglos rápidos

### 1a. Búsqueda a 3 columnas
- `DiscoverScreen.kt` → `AdaptiveGrid` pasa a 3 columnas fijas, en línea con
  `LibraryScreen` (`GridCells.Fixed(3)`). Aplica a resultados de búsqueda y al
  grid de trending. Las cards ya escalan con `weight(1f)`.

### 1b. Banner de verificación de email
- `AuthDataSource`: nuevo `suspend fun refreshEmailVerified(): Boolean` que
  hace `user.reload().await()` y devuelve `isEmailVerified` fresco.
- `ProfileViewModel.refreshEmailVerificationStatus()` pasa a ser suspend/launch
  y usa el nuevo método (se llama en `init`).
- Banner del Perfil: botón adicional "Ya lo he verificado" que fuerza el
  refresco inmediato. Se mantiene el botón de reenviar.

### 1c. Relevancia de búsqueda de libros
- `buildGoogleBooksQuery`: se elimina la conversión a frase exacta de las
  consultas "tipo autor" — el heurístico no puede distinguir "Stephen King" de
  "La Sombra Del Viento" y la frase exacta restringe resultados. La consulta se
  envía tal cual (la relevancia full-text de Google Books gestiona bien autores
  y títulos). Se mantiene la detección de ISBN.
- `MediaRepositoryImpl.searchBooks`: deduplicación por (título normalizado +
  primer autor) — la proyección `lite` no incluye ISBN. `orderBy=relevance`
  explícito en la API de búsqueda.

### 1d. Trending real de libros
- `getTrendingBooks`: Open Library `trending/daily` como fuente primaria
  (hoy es fallback; su mapper ya descarta items sin portada). Google Books
  (`subject:fiction`) queda como complemento/fallback si Open Library falla o
  devuelve pocos resultados. Se eliminan las `BOOKS_TRENDING_QUERIES`
  rotatorias. Trade-off aceptado: el trending de Open Library es global
  (mayoría en inglés).

### Tests Fase 1
- Unit tests: query builder (autor/ISBN/genérico) y deduplicación.
- Banner de email: prueba manual con cuenta de test.

## Fase 2 — Coherencia visual glass

1. Inventario de pantallas no-glass (mínimo: NotificationsScreen).
2. Migración al patrón iOS26 documentado: `Spacer(statusBarsPadding)` al
   inicio, header propio (sin `TopAppBar` Material), cards en `GlassSurface`.
3. Pantalla de detalle: hero con póster + gradiente, chips de estado glass,
   secciones en `GlassSurface`.

## Fase 3 — Animaciones y refresh Home/Discover

1. Transiciones de navegación en `AppNavGraph` (fade + slide sutil).
2. Micro-interacción de escala al pulsar en `MediaCard`.
3. Repaso de jerarquía visual y espaciados en Home y Discover.

## Restricciones

- No tocar la arquitectura de navegación (insets/FloatingBottomNav) sin releer
  el contexto del rediseño de mayo (doble navigationBarsPadding bug).
- Compilar con Java 21 (`org.gradle.java.home` ya configurado).
- Trabajo en `main` con commits pequeños por fase (sync entre dos máquinas).
