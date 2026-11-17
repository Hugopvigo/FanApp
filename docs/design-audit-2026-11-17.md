# Auditoría visual — 2026-11-17

Inventario de inconsistencias de diseño previo a las tareas B2–B4 del plan
[2026-11-17-code-diseno-polish](superpowers/plans/2026-11-17-code-diseno-polish.md).

## Alcance de la auditoría

- ✅ Inventario automatizado de colores literales, strings hardcodeados y valores `dp` (sección Apéndice).
- ✅ Revisión de los archivos de tema (`Color.kt`, `Theme.kt`) y de los componentes compartidos.
- ⚠️ **Capturas en dispositivo pendientes.** No hay dispositivo/emulador conectado
  (`adb devices` → lista vacía). Las capturas de las 4 temas × 7 pantallas las debe
  hacer Hugo en su dispositivo; este documento no finge capturas que no existen.
  Las capturas antiguas `docs/screenshot_{home,discover,library,profile}.jpg` son
  de referencia visual del rediseño de mayo, no del estado actual.

## Hallazgos

### P1 — Inconsistencias visibles / contraste

| # | Archivo:línea | Hallazgo | Acción (tarea) |
|---|---|---|---|
| P1-1 | `presentation/components/MediaCard.kt:46-58` | 12 pares de gradientes con colores RGB fijos, ajenos al sistema de tokens de los 4 temas. En los temas Light y Purple pueden no respetar la paleta activa. | B2 |
| P1-2 | `presentation/fancard/FanCardScreen.kt:532,556` | Estrellas en amarillo fijo `0xFFFFC107`, mientras `LibraryItemCard.kt:206` usa `fanColors.gradientAccent.first()`. Misma semántica, colores distintos. | B2 |
| P1-3 | `presentation/navigation/FloatingBottomNav.kt:54,56` | Scrim y glow con RGB literales, en vez de tokens del tema. | B2 |
| P1-4 | `presentation/components/GlassBackHeader.kt:47` | Botón atrás de **38 dp**, por debajo del mínimo de 48 dp de objetivos táctiles. Afecta a las 5 sub-pantallas migradas en julio. | B4 |
| P1-5 | `presentation/csvimport/ImportScreen.kt:116,245,252,365,372` | Cinco textos en español hardcodeados en una app con EN base + ES. En inglés se mezclan idiomas. | B3 |

### P2 — Espaciado / tipografía

| # | Archivo | Hallazgo | Acción (tarea) |
|---|---|---|---|
| P2-1 | `presentation/home/HomeScreen.kt` + `home/components/HomeComponents.kt` | 62 + valores `dp` literales con mezcla de 4/5/6/8/10/12/16/18/20/24. Revisar que los espaciados de sección usen la misma escala. | B4 |
| P2-2 | `presentation/fancard/FanCardScreen.kt` | 61 valores `dp`; mismo criterio de escala. | B4 |
| P2-3 | `presentation/profile/components/ProfileSections.kt` | Secciones extraídas con paddings 14/18/20 dp heredados; verificar coherencia con el resto. | B4 |

### P3 — Detalles menores (no se corrigen en este plan)

- Emoji usados como iconos (`⭐`, `✨`, `🔥`, `⚡`, `⚠️`, `🌙`, `↩`, `›`). Son gráficos,
  no texto traducible, y forman parte del lenguaje visual actual de la app.
- El literal `"›"` en `ImportScreen.kt:196` y `ProfileSections.kt` es un separador
  visual, no contenido localizable.

## Coherencia confirmada

- Los 4 temas (Fantasy, Light, Purple, Dark) siguen presentes en `Theme.kt`, cada
  uno con `surfaceGlass`, `surfaceElev`, `hairlineColor`, `gradient1`, `gradient2`,
  `gradientAccent`, `navBarBg`, etc.
- `GlassSurface`, `GlassBackHeader` y `FloatingBottomNav` son los componentes base
  del estilo iOS26; las pantallas principales ya los usan.
- No quedan `Scaffold` + `TopAppBar` Material en sub-pantallas (migración de julio).

## Apéndice — inventario automatizado

Los ficheros completos están en `/tmp/opencode/design-colors.txt`,
`design-hardcoded-strings.txt` y `design-dp.txt` (temporales, no versionados).
Resumen:

- **16** colores RGB literales fuera de `presentation/theme`:
  12 en `MediaCard.kt`, 2 en `FanCardScreen.kt`, 2 en `FloatingBottomNav.kt`.
- **14** literales en `Text(...)`: 5 strings traducibles en `ImportScreen.kt` y
  9 emoji/separadores.
- **133** ficheros con valores `dp` (el 90 % son composables legítimos; solo se
  revisan los marcados como P2).
