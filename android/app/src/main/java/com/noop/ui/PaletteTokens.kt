package com.noop.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

// MARK: - PaletteTokens — the per-scheme colour set behind `object Palette`
//
// Compose has no OS-dynamic colour (unlike iOS UIColor(light:dark:)), so the light theme is built
// the same way conceptually: ONE set of colour tokens, swapped wholesale per scheme. `Palette.active`
// is snapshot state, so every `Palette.X` read (in a composable OR a Canvas DrawScope) re-resolves
// automatically when the theme flips — ZERO call-site changes across the ~1,740 references.
//
// Dark values mirror StrandPalette.swift's dark; light values are the approved "Warm Paper" set
// (docs/superpowers/specs/2026-06-16-light-theme-design.md). Names/order match the Swift palette.

data class PaletteTokens(
    val surfaceBase: Color,
    val surfaceRaised: Color,
    val surfaceOverlay: Color,
    val surfaceInset: Color,
    val hairline: Color,
    val hairlineStrong: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val glowAmbient: Color,
    val accent: Color,
    val accentHover: Color,
    val accentMuted: Color,
    val focusRing: Color,
    val recovery000: Color,
    val recovery030: Color,
    val recovery055: Color,
    val recovery078: Color,
    val recovery100: Color,
    val strain000: Color,
    val strain033: Color,
    val strain066: Color,
    val strain100: Color,
    val sleepAwake: Color,
    val sleepLight: Color,
    val sleepDeep: Color,
    val sleepREM: Color,
    val zone1: Color,
    val zone2: Color,
    val zone3: Color,
    val zone4: Color,
    val zone5: Color,
    val statusPositive: Color,
    val statusWarning: Color,
    val statusCritical: Color,
    val metricCyan: Color,
    val metricPurple: Color,
    val metricAmber: Color,
    val metricRose: Color,
    val chargeColor: Color,
    val chargeDeep: Color,
    val chargeBright: Color,
    val chargeGlow: Color,
    val effortColor: Color,
    val effortDeep: Color,
    val effortBright: Color,
    val effortGlow: Color,
    val restColor: Color,
    val restDeep: Color,
    val restBright: Color,
    val restGlow: Color,
    val stressColor: Color,
    val stressDeep: Color,
    val stressBright: Color,
    val stressGlow: Color,
    val scenicCenter: Color,
    val scenicEdge: Color,
    val scenicStar: Color,
    val cardFillTop: Color,
    val cardFillBottom: Color,
    val gold: Color,
    val goldLight: Color,
    val goldDeep: Color,
    val goldDeepText: Color,
    val signalYellow: Color,
    val titaniumTop: Color,
    val titaniumMid: Color,
    val titaniumLow: Color,
    val titaniumDeep: Color,
    // The bright gauge-tip / sparkline-head core: white reads as a highlight on dark; on light it
    // would vanish into the white card, so it flips to a deep ink (crisp centre on the coloured bead).
    val tipCore: Color,
    // WHOOP slate canvas gradient endpoints (top lighter slate, bottom near-black). Added for the
    // WHOOP reskin; the vertical brush is built in Palette.canvasGradient().
    val canvasTop: Color,
    val canvasBottom: Color,
    // Recovery-domain data shown WITHOUT a verdict (no green/yellow/red judgement): WHOOP's calm blue.
    val recoveryNoJudgement: Color,
)

// WHOOP-faithful dark palette (2026-07 reskin). Values mirror StrandPalette.swift's DARK scheme
// from Task 1A: slate gradient canvas #283339 to #101518, WHOOP hard-band recovery
// red #FF0026 / yellow #FFDE00 / green #16EC06, strain blue #0093E7, sleep slate #7BA1BB, teal
// CTA accent #00F19F. The gold/titanium token names survive (public API frozen) but repoint to the
// WHOOP teal accent family; only VALUES changed.
val DarkTokens = PaletteTokens(
    surfaceBase = Color(0xFF101518), surfaceRaised = Color(0xFF1C2126), surfaceOverlay = Color(0xFF181D21),
    surfaceInset = Color(0xFF161B1F), hairline = Color(0xFF2A343A), hairlineStrong = Color(0xFF3A464E),
    textPrimary = Color(0xFFF4F6F8), textSecondary = Color(0xFFC8CFD8), textTertiary = Color(0xFF8A94A4),
    glowAmbient = Color(0xFF0E2B23),
    accent = Color(0xFF00F19F), accentHover = Color(0xFF4DF5BC), accentMuted = Color(0xFF0E2B23), focusRing = Color(0xFF00F19F),
    recovery000 = Color(0xFFFF0026), recovery030 = Color(0xFFFF0026), recovery055 = Color(0xFFFFDE00),
    recovery078 = Color(0xFF16EC06), recovery100 = Color(0xFF16EC06),
    strain000 = Color(0xFF4FB5EF), strain033 = Color(0xFF0093E7), strain066 = Color(0xFF0082CC), strain100 = Color(0xFF0071B3),
    sleepAwake = Color(0xFF8C95A3), sleepLight = Color(0xFF7BA1BB), sleepDeep = Color(0xFF4A6B85), sleepREM = Color(0xFF9FB9CC),
    zone1 = Color(0xFF8C95A3), zone2 = Color(0xFF16EC06), zone3 = Color(0xFFFFDE00), zone4 = Color(0xFFFF8A00), zone5 = Color(0xFFFF0026),
    statusPositive = Color(0xFF16EC06), statusWarning = Color(0xFFFFDE00), statusCritical = Color(0xFFFF0026),
    metricCyan = Color(0xFF0093E7), metricPurple = Color(0xFF7BA1BB), metricAmber = Color(0xFFFFDE00), metricRose = Color(0xFFFF0026),
    chargeColor = Color(0xFF16EC06), chargeDeep = Color(0xFF0EA004), chargeBright = Color(0xFF5FF255), chargeGlow = Color(0xFF16EC06),
    effortColor = Color(0xFF0093E7), effortDeep = Color(0xFF0071B3), effortBright = Color(0xFF4FB5EF), effortGlow = Color(0xFF0093E7),
    restColor = Color(0xFF7BA1BB), restDeep = Color(0xFF4A6B85), restBright = Color(0xFF9FB9CC), restGlow = Color(0xFF7BA1BB),
    stressColor = Color(0xFFFFDE00), stressDeep = Color(0xFF16EC06), stressBright = Color(0xFFFF0026), stressGlow = Color(0xFFFFDE00),
    scenicCenter = Color(0xFF283339), scenicEdge = Color(0xFF101518), scenicStar = Color(0xFF2A343A),
    cardFillTop = Color(0xFF1C2126), cardFillBottom = Color(0xFF101518),
    gold = Color(0xFF00F19F), goldLight = Color(0xFF4DF5BC), goldDeep = Color(0xFF0E2B23),
    goldDeepText = Color(0xFF101518), signalYellow = Color(0xFFFFDE00),
    titaniumTop = Color(0xFFF1F3F5), titaniumMid = Color(0xFFC9CFD4), titaniumLow = Color(0xFF969DA4), titaniumDeep = Color(0xFF6B737B),
    tipCore = Color(0xFFFFFFFF),
    canvasTop = Color(0xFF283339), canvasBottom = Color(0xFF101518),
    recoveryNoJudgement = Color(0xFF67AEE6),
)

val LightTokens = PaletteTokens(
    surfaceBase = Color(0xFFEAE3D4), surfaceRaised = Color(0xFFFFFFFF), surfaceOverlay = Color(0xFFFFFFFF),
    surfaceInset = Color(0xFFDFD8C8), hairline = Color(0xFFD8D0BD), hairlineStrong = Color(0xFFC7BCA4),
    textPrimary = Color(0xFF1A2230), textSecondary = Color(0xFF4C5564), textTertiary = Color(0xFF7C8696),
    glowAmbient = Color(0xFFF0E4C0),
    // Light chrome accent shifts to the deep brand blue (gold reserved for the recovery world + FAB).
    accent = Color(0xFF234F9E), accentHover = Color(0xFF1C3F80), accentMuted = Color(0xFFE4ECF6), focusRing = Color(0xFF2F6FCB),
    recovery000 = Color(0xFF8F6212), recovery030 = Color(0xFFA87718), recovery055 = Color(0xFFC28E26),
    recovery078 = Color(0xFFD2A23A), recovery100 = Color(0xFFE0B44C),
    strain000 = Color(0xFF7E460E), strain033 = Color(0xFFA4621B), strain066 = Color(0xFFC2792E), strain100 = Color(0xFFD89240),
    sleepAwake = Color(0xFF97A2B2), sleepLight = Color(0xFF3A80D6), sleepDeep = Color(0xFF234F9E), sleepREM = Color(0xFF5790DA),
    zone1 = Color(0xFF3A80D6), zone2 = Color(0xFF2E92B4), zone3 = Color(0xFFC28E26), zone4 = Color(0xFFC2792E), zone5 = Color(0xFFC84E1E),
    statusPositive = Color(0xFFB07D17), statusWarning = Color(0xFFC2792E), statusCritical = Color(0xFFC84E1E),
    metricCyan = Color(0xFF2E92B4), metricPurple = Color(0xFF3A80D6), metricAmber = Color(0xFFC2792E), metricRose = Color(0xFFC84E1E),
    chargeColor = Color(0xFFB88421), chargeDeep = Color(0xFF8F6212), chargeBright = Color(0xFFE0B44C), chargeGlow = Color(0xFFC8902F),
    effortColor = Color(0xFFB26A1C), effortDeep = Color(0xFF7E460E), effortBright = Color(0xFFD89240), effortGlow = Color(0xFFB26A1C),
    restColor = Color(0xFF3A80D6), restDeep = Color(0xFF234F9E), restBright = Color(0xFF5790DA), restGlow = Color(0xFF3A80D6),
    stressColor = Color(0xFFB88421), stressDeep = Color(0xFF3A80D6), stressBright = Color(0xFFC84E1E), stressGlow = Color(0xFFB88421),
    scenicCenter = Color(0xFFFBF6EA), scenicEdge = Color(0xFFEDE6D6), scenicStar = Color(0xFFD8CDB6),
    cardFillTop = Color(0xFFFFFFFF), cardFillBottom = Color(0xFFFAF7F0),
    gold = Color(0xFFDBA52A), goldLight = Color(0xFFECC766), goldDeep = Color(0xFF9A6B12),
    goldDeepText = Color(0xFF3A2708), signalYellow = Color(0xFFE8A800),
    titaniumTop = Color(0xFFDDE1E6), titaniumMid = Color(0xFFBBC2C9), titaniumLow = Color(0xFF98A0A8), titaniumDeep = Color(0xFF6B737B),
    tipCore = Color(0xFF241B06),
    // Light scheme untouched by the WHOOP dark reskin; the added tokens take warm-paper-appropriate values.
    canvasTop = Color(0xFFEAE3D4), canvasBottom = Color(0xFFDFD8C8),
    recoveryNoJudgement = Color(0xFF3A80D6),
)

// MARK: - Chart style (data-viz colour mode) + the Classic throwback ramps

enum class ChartStyle(val storageValue: String, val label: String) {
    TITANIUM("titanium", "Titanium"),
    CLASSIC("classic", "Classic");

    companion object {
        fun fromStorage(raw: String?): ChartStyle = entries.firstOrNull { it.storageValue == raw } ?: TITANIUM
    }
}

/** Chart-colour preference, persisted in `noop_prefs` and mirrored in snapshot state so a flip
 *  re-colours every gauge/chart live (the Palette ramp accessors read [ChartStylePrefs.style]). */
object ChartStylePrefs {
    private const val FILE = "noop_prefs"
    private const val KEY = "chart.style"
    private fun prefs(ctx: Context): SharedPreferences =
        ctx.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    var style by mutableStateOf(ChartStyle.TITANIUM)
        private set

    fun load(ctx: Context) {
        style = ChartStyle.fromStorage(prefs(ctx).getString(KEY, ChartStyle.TITANIUM.storageValue))
    }

    fun set(ctx: Context, value: ChartStyle) {
        style = value
        prefs(ctx).edit().putString(KEY, value.storageValue).apply()
    }
}

/** The Classic (throwback) data ramps — light/dark tuned. Picked by the Palette accessors when
 *  ChartStylePrefs.style == CLASSIC. Surfaces/text/accent are never classic — only data encodings. */
data class ClassicRamp(
    val recovery: List<Pair<Float, Color>>,
    val strain: List<Pair<Float, Color>>,
    val stress: List<Pair<Float, Color>>,
    val sleepAwake: Color, val sleepLight: Color, val sleepDeep: Color, val sleepREM: Color,
    val zone1: Color, val zone2: Color, val zone3: Color, val zone4: Color, val zone5: Color,
    val statusPositive: Color, val statusWarning: Color, val statusCritical: Color,
    val metricCyan: Color, val metricPurple: Color, val metricAmber: Color, val metricRose: Color,
    val chargeColor: Color, val chargeDeep: Color, val chargeBright: Color,
    val effortColor: Color, val effortDeep: Color, val effortBright: Color,
    val restColor: Color, val restDeep: Color, val restBright: Color,
    val stressColor: Color, val stressDeep: Color, val stressBright: Color,
)

// Both chart styles now resolve to WHOOP on dark (Task 1A step 2): the Classic dark ramp mirrors the
// Titanium WHOOP values so the toggle is visually a no-op on the canonical dark look. Recovery is the
// WHOOP hard three-band traffic light with duplicated locations (red 0..0.335, yellow 0.335..0.665,
// green 0.665..1) so the band switches instead of blending.
val ClassicDark = ClassicRamp(
    recovery = listOf(
        0.0f to Color(0xFFFF0026), 0.335f to Color(0xFFFF0026),
        0.335f to Color(0xFFFFDE00), 0.665f to Color(0xFFFFDE00),
        0.665f to Color(0xFF16EC06), 1.0f to Color(0xFF16EC06),
    ),
    strain = listOf(0.0f to Color(0xFF4FB5EF), 0.33f to Color(0xFF0093E7), 0.66f to Color(0xFF0082CC), 1.0f to Color(0xFF0071B3)),
    stress = listOf(0.0f to Color(0xFF16EC06), 0.5f to Color(0xFFFFDE00), 1.0f to Color(0xFFFF0026)),
    sleepAwake = Color(0xFF8C95A3), sleepLight = Color(0xFF7BA1BB), sleepDeep = Color(0xFF4A6B85), sleepREM = Color(0xFF9FB9CC),
    zone1 = Color(0xFF8C95A3), zone2 = Color(0xFF16EC06), zone3 = Color(0xFFFFDE00), zone4 = Color(0xFFFF8A00), zone5 = Color(0xFFFF0026),
    statusPositive = Color(0xFF16EC06), statusWarning = Color(0xFFFFDE00), statusCritical = Color(0xFFFF0026),
    metricCyan = Color(0xFF0093E7), metricPurple = Color(0xFF7BA1BB), metricAmber = Color(0xFFFFDE00), metricRose = Color(0xFFFF0026),
    chargeColor = Color(0xFF16EC06), chargeDeep = Color(0xFF0EA004), chargeBright = Color(0xFF5FF255),
    effortColor = Color(0xFF0093E7), effortDeep = Color(0xFF0071B3), effortBright = Color(0xFF4FB5EF),
    restColor = Color(0xFF7BA1BB), restDeep = Color(0xFF4A6B85), restBright = Color(0xFF9FB9CC),
    stressColor = Color(0xFFFFDE00), stressDeep = Color(0xFF16EC06), stressBright = Color(0xFFFF0026),
)

val ClassicLight = ClassicRamp(
    recovery = listOf(0.0f to Color(0xFFCB3A2F), 0.30f to Color(0xFFD87328), 0.55f to Color(0xFFCFA528), 0.78f to Color(0xFF74A53A), 1.0f to Color(0xFF2E9E4F)),
    strain = listOf(0.0f to Color(0xFF5E92D6), 0.33f to Color(0xFF3A74C4), 0.66f to Color(0xFF284F9C), 1.0f to Color(0xFF1C3E80)),
    stress = listOf(0.0f to Color(0xFF2E9E4F), 0.5f to Color(0xFFCFA528), 1.0f to Color(0xFFCB3A2F)),
    sleepAwake = Color(0xFF8C95A3), sleepLight = Color(0xFF3A80D6), sleepDeep = Color(0xFF203E73), sleepREM = Color(0xFF6A4FC0),
    zone1 = Color(0xFF828D9B), zone2 = Color(0xFF2E9E4F), zone3 = Color(0xFFCFA528), zone4 = Color(0xFFD87328), zone5 = Color(0xFFCB3A2F),
    statusPositive = Color(0xFF2E9E4F), statusWarning = Color(0xFFCFA528), statusCritical = Color(0xFFCB3A2F),
    metricCyan = Color(0xFF2E92B4), metricPurple = Color(0xFF6A4FC0), metricAmber = Color(0xFFCFA528), metricRose = Color(0xFFCB3A2F),
    chargeColor = Color(0xFF2E9E4F), chargeDeep = Color(0xFF207A3C), chargeBright = Color(0xFF5FBE6E),
    effortColor = Color(0xFF3A74C4), effortDeep = Color(0xFF284F9C), effortBright = Color(0xFF5E92D6),
    restColor = Color(0xFF3A80D6), restDeep = Color(0xFF203E73), restBright = Color(0xFF6A4FC0),
    stressColor = Color(0xFFCFA528), stressDeep = Color(0xFF2E9E4F), stressBright = Color(0xFFCB3A2F),
)

// MARK: - Appearance preference (System / Light / Dark)

enum class AppearanceMode(val storageValue: String, val label: String) {
    SYSTEM("system", "System"),
    LIGHT("light", "Light"),
    DARK("dark", "Dark");

    companion object {
        fun fromStorage(raw: String?): AppearanceMode =
            entries.firstOrNull { it.storageValue == raw } ?: SYSTEM
    }
}

/** Theme preference, persisted in `noop_prefs` and mirrored in snapshot state so the toggle is live.
 *  [load] is called once from MainActivity before first composition (no flash); [set] writes + flips. */
object AppearancePrefs {
    private const val FILE = "noop_prefs"
    private const val KEY = "theme.appearance"

    private fun prefs(ctx: Context): SharedPreferences =
        ctx.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    /** Live appearance mode read by NoopTheme; defaults to System until [load] runs. */
    var mode by mutableStateOf(AppearanceMode.SYSTEM)
        private set

    fun load(ctx: Context) {
        mode = AppearanceMode.fromStorage(prefs(ctx).getString(KEY, AppearanceMode.SYSTEM.storageValue))
    }

    fun set(ctx: Context, value: AppearanceMode) {
        mode = value
        prefs(ctx).edit().putString(KEY, value.storageValue).apply()
    }
}
