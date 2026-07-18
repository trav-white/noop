package com.noop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// MARK: - WhoopScreenSky, the WHOOP slate backdrop for every reskinned screen's top region
//
// Replaces the old LiquidScreenSky (the animated day-of-sky backdrop) app-wide. Every screen that used to
// pass topBackground = { LiquidScreenSky() } now passes { WhoopScreenSky() } instead (see TodayScreen.kt,
// SleepScreen.kt, and the rest of the screens in this package). LiquidScreenSky itself has been removed as
// dead code now that no caller references it; LiquidSkyStatic/LiquidSky (LiquidSky.kt) remain for now,
// pending their own dedicated review.
//
// HOW IT PLUGS IN: pass this as the scaffold's topBackground slot:
//
//   LazyScreenScaffold(
//       ...
//       topBackground = if (showDayCycleBackground) { { WhoopScreenSky() } } else null,
//   ) { ... }
//
// The existing ScreenScaffold / LazyScreenScaffold topBackground machinery (Components.kt) already does
// the screen-level plumbing this backdrop needs: it anchors the slot to the TOP, bleeds it full-width UP
// behind the status bar (offset by the status-bar inset), and promotes it to its OWN compositing layer (an
// empty graphicsLayer {}) so a static backdrop rasterises ONCE and replays as a texture on every scroll
// frame. So this composable only has to paint one flat gradient, top-aligned, at a header height.
//
// Non-interactive and accessibility-hidden, it is pure decoration (the scaffold slot never receives taps).
// The flat WHOOP canvas gradient is #283339 slate at the top, fading to #101518 near-black at the bottom,
// from Palette.canvasGradient(), so the header, rings and cards float on the canonical flat WHOOP canvas
// with no per-frame cost (a static brush).

/** The WHOOP slate backdrop for a reskinned screen's top region. Drop into a scaffold's `topBackground`
 *  slot. [height] is the slate band; the gradient settles into the theme canvas below it. */
@Composable
fun WhoopScreenSky(height: Dp = 340.dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(Palette.canvasGradient())
            .clearAndSetSemantics {}, // decorative, invisible to TalkBack
    )
}
