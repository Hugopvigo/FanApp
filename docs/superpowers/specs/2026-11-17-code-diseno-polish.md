# FanApp — Pulido de código y diseño (nov 2026)

**Fecha:** 2026-11-17
**Estado:** Pendiente de aprobación por Hugo

## Contexto

FanApp MediaTracker está en ~80 % (Sprint 9 en curso, release candidate). En julio
se ejecutó un spec/plan de mejoras (Fases 1–3: búsqueda 3 columnas, verificación
de email, relevancia de libros, coherencia glass y animaciones) y está cerrado.

Ahora Hugo pide una nueva pasada con dos líneas de trabajo:

1. **Revisión de diseño:** pulir el diseño actual, buscar inconsistencias y
   realizar mejoras.
2. **Revisión de código y pulido:** eliminar bugs y errores, refactorizar y
   conseguir que la app vaya en general más rápido.

## Decisiones tomadas con Hugo (2026-11-17)

| Decisión | Elección |
|---|---|
| Orden de las fases | **Código/bugs primero**, diseño después. Un único plan con dos fases encadenadas; cada fase produce software verificable por separado. |
| Qué significa «más rápido» | Fluidez de UI y arranque + carga de datos e imágenes + consumo de red/batería. **No** incluye tiempos de build/CI. |
| Alcance de features | **Sin features nuevas.** Solo estabilidad, refactor, rendimiento y pulido visual. |
| Formato | Spec + plan en `docs/superpowers/`, según convención del repo. |

## Hallazgos que motivan el spec (verificados en el repo, 2026-11-17)

- `FanCardViewModel.kt:80` — único `!!` de `app/src/main` (prohibido por AGENT.md).
- `MediaTrackerApp.kt:43` → `ThemeRepository.migrateFromSharedPreferences()` lee
  `SharedPreferences` de forma síncrona en `onCreate` (main thread, solo primer arranque).
- `NetworkModule.kt` — los 3 clientes OkHttp (TMDB, Google Books, Open Library)
  no tienen `Cache`: toda respuesta de metadatos vuelve a la red.
- No hay `ImageLoaderFactory` de Coil configurada.
- `items(...)` sin `key` en listas dinámicas: `ImportScreen.kt:343` (errores) y
  `ProfileScreen.kt:474` (avatares).
- Composables gigantes (anti-pattern de AGENT.md, máx. 200 líneas):
  `ProfileScreen.kt` (~470 líneas en `ProfileScreen`), `DetailScreen.kt`
  (`DetailContent` ~230 líneas), `HomeScreen.kt` (`FeaturedCard` ~105).
- Colores literales fuera del sistema de tokens en `MediaCard.kt` (12 gradientes),
  `FanCardScreen.kt` (estrellas) y `FloatingBottomNav.kt` (2 colores).
- Strings en español hardcodeados en `ImportScreen.kt` («Cancelar», «Importar mas»,
  «Hecho», «OK») en una app con `values/`(EN) + `values-es/`(ES), 238 claves cada uno.
- `GlassBackHeader.kt:47` — botón atrás de 38 dp, por debajo del mínimo de 48 dp.
- Sin línea base ejecutada: no hay constancia de que `compileDebugKotlin`, tests y
  lint pasen en el estado actual.

## Criterios de aceptación

- `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest :app:lint` en verde
  (o con los avisos preexistentes documentados y aceptados en la línea base).
- Cero `!!`, cero `println`/`Log.d`, cero try/catch vacíos en `app/src/main`.
- Ninguna lectura de `SharedPreferences`/DataStore en el hilo principal detectada
  por StrictMode en debug.
- Clientes HTTP con caché configurada e `ImageLoader` compartida.
- Ningún color literal fuera de `presentation/theme` y ningún string de UI
  hardcodeado en composables.
- Botones táctiles de al menos 48 dp.
- Los 4 temas (Fantasy, Light, Purple, Dark) se mantienen y se revisan.

## Fuera de alcance

- No tocar la arquitectura de insets / `FloatingBottomNav` (bug de doble
  `navigationBarsPadding` en mayo 2026). Solo se cambian sus colores, no su layout.
- No eliminar ninguno de los 4 temas.
- No migrationes de base de datos, ni `fallbackToDestructiveMigration()` nuevo.
- No añadir dependencias de terceros salvo que la línea base demuestre que hacen
  falta (preferir lo ya presente: OkHttp, Coil, JUnit, MockK, Turbine).

## Riesgos

| ID | Riesgo | Mitigación |
|---|---|---|
| R1 | El refactor de composables cambie el comportamiento visual | Extraer sin cambiar lógica; comparar capturas antes/después en los 4 temas |
| R2 | La caché HTTP sirva datos obsoletos | Solo cachear GETs sin `Cache-Control` explícito; `max-age` corto; no cachear respuestas `no-store` |
| R3 | StrictMode en debug generate ruido de librerías de terceros | `penaltyLog()` (no `penaltyDeath()`) y solo en `BuildConfig.DEBUG` |
| R4 | Cambiar colores a tokens rompa contraste en algún tema | Revisar los 4 temas con las capturas de la auditoría |
