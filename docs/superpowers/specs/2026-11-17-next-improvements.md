# FanApp — Siguiente tanda de mejoras (nov 2026)

**Fecha:** 2026-11-17
**Estado:** Pendiente de aprobación por Hugo

## Contexto

Tras cerrar el pulido de código y diseño (PR #1, mergeado en `main`) y verificar
la APK v1.0.1 en dispositivo, Hugo ha pedido seguir mejorando la app. Se han
seleccionado tres areas independientes:

1. **CI + crash reporting**
2. **Arreglos rapidos** detectados en la revision
3. **Calidad visual y tests de UI**

Cada area tiene su propio plan, que produce software verificable por separado:

| Subproyecto | Plan |
|---|---|
| CI + Crashlytics | `plans/2026-11-17-ci-crashlytics.md` |
| Arreglos rapidos | `plans/2026-11-17-quick-fixes.md` |
| Calidad visual y UI tests | `plans/2026-11-17-ui-quality.md` |

## Decisiones tomadas con Hugo

| Decision | Eleccion |
|---|---|
| Crash reporting | Firebase Crashlytics (el stack ya usa Firebase BoM 34.13.0). No se anade Sentry. |
| SDK de Crashlytics | `com.google.firebase:firebase-crashlytics` (no `-ktx`: el modulo KTX esta deprecated segun documentacion oficial). |
| Dispositivo | Hugo conecta el movil para capturas y tests instrumentados en esta sesion. |
| Orden | CI y arreglos rapidos primero; calidad visual/UI despues. |

## Hallazgos verificados en el repo (2026-11-17)

- No existe `.github/`: el PR #1 se mergeo **sin ningun check automatico**.
- No hay Crashlytics ni ningun sistema de reporte de crashes.
- `MediaTrackerApp.kt:99` usa `BuildConfig.TMDB_API_KEY` como señal de "no hay
  configuracion de Firebase". Ademas, el init manual construye `FirebaseOptions`
  solo con applicationId y projectId (sin API key), por lo que Firebase Auth no
  podria funcionar si ese init se ejecutara. Con `google-services.json` presente,
  el plugin de Google Services ya inicializa Firebase automaticamente.
- `GoogleBooksRateLimiter.kt:44,61` usa `Thread.sleep` dentro de un interceptor
  de OkHttp: bloquea un hilo del dispatcher (2 s entre llamadas, hasta 14 s por
  reintentos tras un 429).
- `MainActivity.kt:49` inicializa `onboardingNeeded = false` y lo calcula en un
  `lifecycleScope`, asi que el primer frame puede mostrar Main antes de Onboarding.
- No hay ficheros en `app/src/androidTest/`. Solo hay 98 tests unitarios JVM.
- Hay 4 fotos JPG sueltas en `app/src/` y 99 avisos de lint (30 `UnusedResources`).
- `build.gradle.kts` tiene `isMinifyEnabled = true` pero no `signingConfig`, y
  nunca se ha ejecutado `assembleRelease` (riesgo R3 documentado en Sprint9.md).

## Criterios de aceptacion comunes

- `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest :app:lint` en verde,
  sin avisos nuevos respecto a la linea base (99).
- Sin dependencias nuevas salvo las explicitamente aprobadas en este spec.
- Sin features nuevas de producto (esto es infraestructura, correcciones y calidad).
- Commits pequenos por tarea; `git add <rutas>`, nunca `git add -A`.
- Los ficheros `.md` nuevos requieren `git add -f` (`.gitignore` ignora `*.md`).

## Fuera de alcance

- Sprint 10 de gamificacion (logros 45, retos, niveles, sesiones, resumen semanal).
- Paginacion de resultados de busqueda.
- Cambios de arquitectura de navegacion/insets.
- Publicar a Play Store ni crear keystore de release.

## Riesgos

| ID | Riesgo | Mitigacion |
|---|---|---|
| R1 | El plugin de Crashlytics exige `google-services.json` real para subir mapping | En CI se genera uno de ejemplo desde `app/google-services.json.example`; el mapping real se sube solo desde la maquina de Hugo |
| R2 | Quitar el init manual de Firebase rompa Auth/Firestore | Verificar en dispositivo login y lectura de Firestore tras el cambio |
| R3 | Los tests instrumentados necesitan Hilt y firebase | Usar composables stateless con estado falso, sin `hiltViewModel`, para los primeros tests |
| R4 | Cambiar el rate limiter altere la busqueda de libros | Test unitario de reintento; QA manual de busqueda con red real |
