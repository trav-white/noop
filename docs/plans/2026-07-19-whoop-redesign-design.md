# NOOP WHOOP-Faithful Redesign: Design

Date: 2026-07-19
Status: Approved by Trav (fidelity, platforms, scope, approach, fonts all confirmed)

## Goal

Replace NOOP's "Liquid Metal" design language with a pixel-faithful replication of the
WHOOP app's design (2025-2026, post-5.0 era) across macOS, iOS, Android, watchOS and
widgets, in a single full-app pass.

## Decisions (locked)

| Decision | Choice |
|---|---|
| Fidelity | Full pixel-faithful clone of WHOOP's design |
| Platforms | All (Mac, iOS, Android, watch/widgets inherit) |
| Scope | Full app pass, every screen |
| Approach | Token sweep first, then component swap, then screen restyles |
| Fonts | D-DIN (numbers) + Montserrat (words), free licences, committed to repo |
| Naming | NOOP naming and wordmark stay (Charge/Effort/Rest); design only |

Risk accepted knowingly: this is a public repo that WHOOP is aware of; cloning their
trade dress invites a takedown. Trav has chosen this deliberately.

## Reference material

- WHOOP Brand & Design Guidelines PDF (developer.whoop.com): exact palette hexes.
- fonts.whoop.com: confirms DINPro (numbers) + Proxima Nova (words) two-font rule.
- 10 App Store screenshots saved locally at `~/Documents/whoop-design-refs/`
  (deliberately NOT committed to this repo).

## 1. Tokens

Single source: `Packages/StrandDesign/Sources/StrandDesign/Palette.swift` (Mac/iOS,
drives all screens) and `android/.../ui/PaletteTokens.kt` + `Theme.kt` (Android).
Dark scheme values change to documented WHOOP hexes; light scheme values untouched
(WHOOP has no light mode; dark is the canonical look).

- Canvas: slate gradient #283339 (top) to #101518 (bottom). Replaces flat navy AND
  the animated LiquidSky sunset header everywhere.
- Cards / raised surfaces: near #1C2126 dark grey, subtle hairlines.
- Recovery/Charge ramp: hard three-band traffic light, band switch not blend:
  green #16EC06 (67-100), yellow #FFDE00 (34-66), red #FF0026 (0-33).
- Recovery-no-judgement blue: #67AEE6 (recovery-domain data without a verdict).
- Effort/Strain: #0093E7. Rest/Sleep: #7BA1BB. CTA/positive accent: teal #00F19F.
- The Classic/Titanium chart-style toggle stays wired; both resolve to WHOOP ramps.

## 2. Typography

- D-DIN for every numeral (hero scores ~72pt bold), Montserrat for words.
- Two helpers exposed from StrandDesign and the Android theme: `numberFont(size,
  weight)` and `textFont(size, weight)`. All hardcoded system fonts migrate.
- Labels: small uppercase, wide tracking, Montserrat semibold. Secondary text mid-grey.

## 3. Components

- Ring dial (replaces LiquidVessel): thick rounded-cap arc on dark grey track, starts
  12 o'clock, animated fill on appear, centred numeral + caps label. Three sizes:
  home trio, hero detail, mini card.
- LiquidSky deleted from all screens; headers sit on the slate gradient.
- Metric row: icon + tracked caps label + right value + up/down trend triangle,
  hairline separators, "Today vs. prior 30 days" footer pill.
- Insight callout: dark card, thin teal/blue border, caps link with arrow.
- Monitor tiles: two-up HEALTH MONITOR / STRESS MONITOR pattern.
- Motion: keep NOOP's existing haptics and count-up animations under the new skin
  (WHOOP publishes no motion spec).

## 4. Screens and IA

- Tab bar: Home, Health, white circular + quick-add, coach button (opens CoachView),
  More.
- Home rebuilt to reference: avatar top-left, centred day pager pill, battery
  top-right, dial trio (Rest, Charge, Effort in WHOOP's Sleep/Recovery/Strain order),
  insight card, monitor tiles, My Day, customisable dashboard (existing cards editor
  survives, restyled).
- Detail screens: hero dial + metric list + insight callout scaffold.
- All remaining screens (~60 settings/utility/wizard views): tokens and fonts flow
  automatically, each gets a visual sweep against the system.

## 5. Execution order

Shared Swift package first, then Mac screens, then iOS, then Android (Compose theme +
same components), watch/widgets inherit. Bug-hunter per platform at the end, plus
side-by-side comparison against the reference PNGs. Build via Fable-orchestrated
workflows: Opus for build/screen agents, Sonnet for mechanical sweeps, write agents
capped at 3 concurrent, orchestrator commits between phases.
