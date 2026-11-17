# CI y Crashlytics Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development o superpowers:executing-plans para ejecutar este plan tarea a tarea.

**Goal:** Que cada PR pase lint + tests + build automaticamente y que la app reporte crashes a Firebase Crashlytics.

**Architecture:** Un workflow de GitHub Actions genera un `google-services.json` de ejemplo (el real esta ignorado), corre las tres comprobaciones y sube la APK como artifact. En paralelo, el version catalog anade el plugin de Crashlytics y la dependencia no-KTX; Firebase sigue inicializandose automaticamente via el plugin de Google Services.

**Tech Stack:** GitHub Actions, Gradle 9.5.1, AGP 9.2.1, Kotlin 2.3.21, Firebase BoM 34.13.0, Crashlytics Gradle plugin 3.0.8.

**Spec:** `docs/superpowers/specs/2026-11-17-next-improvements.md`

## Global Constraints

- El `google-services.json` real esta en `.gitignore`; **nunca** subirlo al repo.
- El modulo `firebase-crashlytics-ktx` esta deprecated: usar `firebase-crashlytics`.
- La version del plugin Gradle de Crashlytics es `3.0.6` (verificable en `./gradlew buildEnvironment`).
- Sin features nuevas. Sin cambiar versionCode/versionName.
- Ficheros `.md` nuevos requieren `git add -f`.

## File Structure

| Archivo | Responsabilidad |
|---|---|
| `gradle/libs.versions.toml` | Versiones y alias del plugin/dependencia Crashlytics |
| `build.gradle.kts` (raiz) | Declarar el plugin sin aplicarlo en la raiz |
| `app/build.gradle.kts` | Aplicar el plugin y anadir la dependencia |
| `.github/workflows/android.yml` (nuevo) | CI de PRs |
| `app/src/main/java/com/mediatracker/MediaTrackerApp.kt` | Inicializacion de Crashlytics en debug |

---

### Task C1: Version catalog y plugin de Crashlytics

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `build.gradle.kts`
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: Anadir version y alias**

En `[versions]` de `gradle/libs.versions.toml`, anadir `crashlyticsGradle = "3.0.8"`.
En `[plugins]`, anadir:

```toml
firebase-crashlytics = { id = "com.google.firebase.crashlytics", version.ref = "crashlyticsGradle" }
```

En `[libraries]`, anadir:

```toml
firebase-crashlytics = { group = "com.google.firebase", name = "firebase-crashlytics" }
```

- [ ] **Step 2: Declarar y aplicar el plugin**

En el `build.gradle.kts` de la raiz, dentro de `plugins { }`:

```kotlin
alias(libs.plugins.firebase.crashlytics) apply false
```

En `app/build.gradle.kts`, dentro de `plugins { }`:

```kotlin
alias(libs.plugins.firebase.crashlytics)
```

Y en `dependencies { }`, junto al resto de Firebase:

```kotlin
implementation(libs.firebase.crashlytics)
```

- [ ] **Step 3: Verificar resolucion**

Run: `cd /home/hugo/Dev/FanApp && ./gradlew :app:compileDebugKotlin 2>&1 | tail -15`
Expected: BUILD SUCCESSFUL. Si el plugin no resuelve, comprobar con `./gradlew buildEnvironment | grep -i crashlytics` que la version 3.0.6 existe en `google()`; ajustar solo la version en el catalog.

- [ ] **Step 4: Commit**

```bash
git add gradle/libs.versions.toml build.gradle.kts app/build.gradle.kts
git commit -m "build: añade Firebase Crashlytics al proyecto"
```

---

### Task C2: Configurar recoleccion en debug

**Files:**
- Modify: `app/src/main/java/com/mediatracker/MediaTrackerApp.kt`

- [ ] **Step 1: Activar recoleccion solo en debug**

Anadir imports:

```kotlin
import com.google.firebase.crashlytics.FirebaseCrashlytics
```

Dentro del bloque `if (BuildConfig.DEBUG)` de `onCreate()`, despues de `Timber.plant`:

```kotlin
FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = true
```

En release la recoleccion queda con el valor por defecto (`true`), que es lo deseado. No anadir `setCustomKey` con datos personales.

- [ ] **Step 2: Verificar**

Run: `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest`
Expected: BUILD SUCCESSFUL, 98/98 tests.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/mediatracker/MediaTrackerApp.kt
git commit -m "feat(crashlytics): activa recolección en builds debug"
```

---

### Task C3: Workflow de GitHub Actions

**Files:**
- Create: `.github/workflows/android.yml`

- [ ] **Step 1: Crear el workflow**

```yaml
name: Android CI

on:
  pull_request:
    branches: [main]
  push:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'

      - name: Set up Android SDK
        uses: android-actions/setup-android@v3

      - name: Generate google-services.json for CI
        run: cp app/google-services.json.example app/google-services.json

      - name: Gradle cache
        uses: gradle/actions/setup-gradle@v4

      - name: Lint, unit tests and assemble
        run: ./gradlew :app:lint :app:testDebugUnitTest :app:assembleDebug

      - name: Upload debug APK
        uses: actions/upload-artifact@v4
        with:
          name: fanapp-debug
          path: app/build/outputs/apk/debug/app-debug.apk
          if-no-files-found: error
```

Nota: `app/google-services.json.example` ya existe y tiene `package_name = com.mediatracker`, suficiente para que el plugin procese recursos en CI. No contiene credenciales reales.

- [ ] **Step 2: Verificar el workflow localmente**

Run: `cd /home/hugo/Dev/FanApp && python3 -c "import yaml,sys; yaml.safe_load(open('.github/workflows/android.yml')); print('YAML OK')"`
Expected: `YAML OK`. Si `yaml` no esta instalado, verificar con `ruby -e "require 'yaml'; YAML.load_file('.github/workflows/android.yml'); puts 'YAML OK'"`.

Reproducir los mismos tres comandos en local con el JSON de ejemplo:

```bash
cp app/google-services.json.example /tmp/opencode/google-services.json
# NO sobrescribir el real: usar un worktree temporal o restaurar al terminar
```

Si el JSON de ejemplo difiere del real en algo mas que credenciales, documentarlo antes de subir el workflow.

- [ ] **Step 3: Commit y verificar en GitHub**

```bash
git add .github/workflows/android.yml
git commit -m "ci: lint, tests y APK en cada PR"
git push origin HEAD:main
```

Despues: `gh run list --limit 1` debe mostrar un run del workflow. Si falla, leer el log con `gh run view <id> --log-failed` y corregir antes de seguir.

- [ ] **Step 4: Commit de cierre**

```bash
git add CHANGELOG.md
git commit -m "docs: cierre CI y Crashlytics"
```

---

## Verificacion final

- `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest :app:lint` en verde, 98/98 tests, 99 avisos.
- `gh run list --limit 1` muestra el workflow en verde sobre `main`.
- `FirebaseCrashlytics` aparece en el dex de una APK de debug recien generada.
