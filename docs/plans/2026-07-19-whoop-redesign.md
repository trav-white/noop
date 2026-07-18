# WHOOP-Faithful Redesign Implementation Plan

> **For Claude:** Executed via Fable-orchestrated Workflow phases. Opus agents build,
> Sonnet agents sweep, orchestrator commits between phases. Design contract:
> `docs/plans/2026-07-19-whoop-redesign-design.md`. Reference screenshots:
> `~/Documents/whoop-design-refs/` (never commit these).

**Goal:** Reskin NOOP to a pixel-faithful WHOOP design across Mac, iOS, Android,
watch and widgets.

**Architecture:** All Mac/iOS colour flows through
`Packages/StrandDesign/Sources/StrandDesign/Palette.swift` (public API frozen: change
values, add tokens, never rename). Android mirrors it in
`android/app/src/main/java/com/noop/ui/PaletteTokens.kt` + `Theme.kt`. Components in
`Strand/Liquid/` are the non-WHOOP layer to replace. Screens live in `Strand/Screens/`
(~60 files), `Strand/App/RootView.swift` (tab bar), `Strand/Liquid/LiquidTodayView.swift`
(home).

**Verification:** No Xcode.app or Android SDK on this machine. Local check is
`xcrun swiftc -parse <file>` per touched Swift file (Command Line Tools parse fine)
plus careful code review; the real gate is CI on push: `.github/workflows/app-build.yml`,
`android.yml`, `swift-packages.yml`. Push the branch after each phase and watch
`gh run list --branch whoop-redesign`.

**Branch:** `whoop-redesign`. Orchestrator commits between phases (agents edit only,
never commit, avoids index races). No Co-Authored-By lines.

---

## Phase 1: Tokens + fonts (2 Opus agents in parallel, disjoint trees)

### Task 1A (Swift agent)

**Files:**
- Modify: `Packages/StrandDesign/Sources/StrandDesign/Palette.swift`
- Create: `Packages/StrandDesign/Sources/StrandDesign/Typography.swift`
- Create: `Packages/StrandDesign/Sources/StrandDesign/Resources/Fonts/` (font files)
- Modify: `Packages/StrandDesign/Package.swift` (declare resources)

**Steps:**
1. Download fonts (OFL licensed, commit licence files alongside):
   - D-DIN family: `https://github.com/google/fonts/tree/main/ofl/... ` is NOT the
     home of D-DIN; use the Fontsquirrel D-DIN package mirror
     (`https://www.fontsquirrel.com/fonts/download/d-din` zip) or the datto/d-din
     GitHub mirror. Need: D-DIN.otf, D-DIN-Bold.otf, D-DINCondensed.otf, licence txt.
   - Montserrat: `https://github.com/JulietaUla/Montserrat` (OFL): Regular, Medium,
     SemiBold, Bold + OFL.txt.
2. Palette.swift dark-scheme values (light untouched), keeping every existing name:
   - `surfaceBase` dark → `#101518`; `surfaceRaised` → `#1C2126`;
     `surfaceOverlay` → `#181D21`; `surfaceInset` → `#161B1F`;
     hairlines → desaturated slate (`#2A343A` / `#3A464E`).
   - Add: `canvasTop = #283339`, `canvasBottom = #101518`, plus
     `canvasGradient: LinearGradient` (top to bottom).
   - `accent` dark → `#00F19F` teal; `accentHover` → `#4DF5BC`;
     `accentMuted` → dark teal tint `#0E2B23`; `focusRing` → `#00F19F`.
   - Recovery: `recovery000 = #FF0026`, `recovery055 = #FFDE00`,
     `recovery100 = #16EC06`; `recoveryStops` become hard bands with duplicated
     locations: red 0→0.335, yellow 0.335→0.665, green 0.665→1. Add
     `recoveryBand(_ score: Double) -> Color` (0-33 red, 34-66 yellow, 67-100 green)
     and `recoveryNoJudgement = #67AEE6`.
   - Strain ramp anchors on `#0093E7` (lighter `#4FB5EF` low end, deeper `#0071B3`
     high end). Sleep colours anchor on `#7BA1BB` (awake grey `#8C95A3` stays,
     light `#7BA1BB`, deep `#4A6B85`, REM `#9FB9CC`).
   - HR zones: keep semantic grey/green/yellow/orange/red but use WHOOP
     green/yellow/red hexes for zones 2/3/5.
   - Classic ramps (`cRecovery*` etc.) resolve to the same WHOOP values (both chart
     styles now WHOOP).
3. Typography.swift: register bundled fonts once via
   `CTFontManagerRegisterFontsForURL` (iterate Bundle.module font URLs, works on
   macOS + iOS, guard `#if !os(watchOS)`), expose
   `StrandFont.number(_ size: CGFloat, weight: Weight) -> Font` (D-DIN) and
   `StrandFont.text(_ size:weight:)` (Montserrat), with system-font fallback if
   registration fails. Add `Package.swift` `resources: [.process("Resources")]`.
4. `xcrun swiftc -parse` both files (expect only missing-import noise, no syntax
   errors). Return list of touched files.

### Task 1B (Android agent)

**Files:**
- Modify: `android/app/src/main/java/com/noop/ui/PaletteTokens.kt`, `Theme.kt`
- Create: `android/app/src/main/res/font/` (d_din*.otf, montserrat_*.ttf renamed to
  Android resource rules: lowercase, underscores) + `Type.kt` additions
- Modify: whatever Typography object `Theme.kt` exposes

**Steps:** mirror 1A values exactly; add `numberFont`/`textFont` FontFamily vals;
recovery band function; canvas gradient brush (`Brush.verticalGradient(#283339,
#101518)`). Kotlin files must stay parseable (no compiler locally; be strict).

**Phase gate:** orchestrator reviews diffs, commits
`Retheme palette and typography to WHOOP reference`, pushes, checks
`swift-packages.yml` + `android.yml` CI.

---

## Phase 2: Components (2 Opus agents in parallel, disjoint trees)

### Task 2A (Swift)

**Files:**
- Create: `Packages/StrandDesign/Sources/StrandDesign/RingDial.swift`
- Create: `Packages/StrandDesign/Sources/StrandDesign/WhoopChrome.swift`
  (CanvasBackground view = slate gradient; MetricRow; InsightCallout; MonitorTile;
  TrackedCapsLabel)
- Modify: `Strand/Liquid/LiquidSky.swift` call sites: every `LiquidSky(...)` usage
  (grep repo) replaced by `CanvasBackground()`. Delete LiquidSky.swift and the
  starfield after call sites are clean.
- Keep `LiquidVessel` compiling this phase (screens still reference it; swapped in
  Phase 3/4).

**RingDial contract:**
```swift
public struct RingDial: View {
    public enum Size { case trio, hero, mini }   // 96pt, 260pt, 44pt
    let value: Double        // 0...1 fill fraction
    let display: String      // "85%" / "14.2"
    let label: String        // "RECOVERY" tracked caps
    let tint: Color          // band colour from palette
    let size: Size
}
```
Thick rounded-cap arc (stroke ~10% of diameter), track `#2A343A` at 30% opacity,
starts 12 o'clock clockwise, animates fill 0→value on appear (0.8s ease-out),
D-DIN numeral centred (hero ~72pt), caps label below in Montserrat semibold tracked.

### Task 2B (Android)
Same components in Compose under `android/.../ui/components/`: `RingDial.kt`,
`WhoopChrome.kt`. Match the Swift contract and metrics.

**Phase gate:** commit `Add WHOOP ring dial and chrome components`, push, CI.

---

## Phase 3: Core screens (2 Opus agents, disjoint trees)

### Task 3A (Swift, the visible product)
- `Strand/App/RootView.swift`: tab bar → Home, Health, circular white + button
  (quick-add menu: start live workout, log manual workout), W-style coach button
  (opens existing `CoachView`), More. Keep existing navigation plumbing.
- `Strand/Liquid/LiquidTodayView.swift` → rebuild body to reference frame 01:
  avatar top-left (existing `ProfileAvatarView`), centred day-pager pill (existing
  date navigation), strap battery top-right, NOOP wordmark, dial trio
  Rest / Charge / Effort (RingDial .trio, WHOOP's Sleep/Recovery/Strain order),
  insight card, HEALTH MONITOR + STRESS MONITOR tiles (wire to existing
  `VitalSignsSummary` + `StressView` data), My Day section, existing customisable
  dashboard cards restyled.
- Detail screens `Strand/Screens/`: `SleepView`, `TrendsView`, plus the
  Charge/Effort hosts (`V5PillarHosts.swift`): hero RingDial + MetricRow list +
  "Today vs. prior 30 days" pill + InsightCallout, per frames 02/03/04.
- `HealthView.swift` becomes the Health tab root hosting Fitness Age / Vitality /
  vital signs (frame 05 Healthspan layout).
- Replace every `LiquidVessel` usage in these files with `RingDial`.

### Task 3B (Android)
Mirror on Android home + sleep/recovery/strain detail screens under
`android/.../ui/` (agent maps the existing screen files first, then edits).

**Phase gate:** commit `Rebuild core screens to WHOOP layout`, push, CI, and a
macOS release-workflow artifact if available for visual check.

---

## Phase 4: Full-app sweep (Sonnet agents, max 3 concurrent writers)

Partition `Strand/Screens/*.swift` (minus files done in Phase 3) into 3 batches
alphabetically; one agent per batch, plus a 4th batch (queued after) for
`StrandiOS/`, `NOOPWatch/`, `NOOPWatchComplications/`, `StrandiOSWidgets/`,
`Strand/MenuBar/`. Per file: swap remaining LiquidVessel/LiquidTube/liquid-thread
usages to RingDial/plain charts, migrate hardcoded fonts to
`StrandFont.number/text`, kill any leftover sky/sunset references, verify tokens
only (no raw hexes outside the palette), `swiftc -parse`. Android gets one agent
sweeping remaining `ui/` files the same way.

**Phase gate:** commit `Sweep remaining screens to WHOOP design system`, push, CI.

---

## Phase 5: Verification and polish (orchestrator + agents)

1. All three CI workflows green on `whoop-redesign`; fix loop until they are.
2. Download the macOS artifact from `app-build.yml` (or local `xcodegen` +
   note: no local Xcode, so artifact is the check), install, and Trav (or a
   screenshot pass once Screen Recording permission is granted) compares against
   `~/Documents/whoop-design-refs/` frame by frame.
3. Run bug-hunter on the repo.
4. Marketing assets (`docs/assets/`, README screenshots) intentionally NOT
   regenerated in this pass.
5. PR `whoop-redesign` → `main` on `trav-white/noop`; Trav merges after visual
   sign-off.
