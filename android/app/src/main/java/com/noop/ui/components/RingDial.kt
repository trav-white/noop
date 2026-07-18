package com.noop.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noop.ui.Motion
import com.noop.ui.NoopType
import com.noop.ui.Palette
import com.noop.ui.rememberReduceMotion

// MARK: - RingDial (the WHOOP score ring, Compose parity with StrandDesign/RingDial.swift)
//
// The single WHOOP-faithful ring that REPLACES the old LiquidVessel gauge across the app. It mirrors the
// Swift `RingDial` contract exactly:
//
//   public struct RingDial: View {
//       public enum Size { case trio, hero, mini }   // 96pt, 260pt, 44pt
//       let value: Double        // 0...1 fill fraction
//       let display: String      // "85%" / "14.2"
//       let label: String        // "RECOVERY" tracked caps
//       let tint: Color          // band colour from palette
//       let size: Size
//   }
//
// Anatomy (matches the Swift view and the reference screenshots):
//   1. a thick ROUNDED-CAP arc, stroke about 10% of the diameter,
//   2. sitting on a full-circle TRACK at Palette.hairline (#2A343A) drawn at 30% opacity (a hairline well),
//   3. starting at 12 o'clock and sweeping CLOCKWISE,
//   4. animating its fill 0 to [value] on first composition over 0.8s ease-out (honouring Reduce Motion),
//   5. a centred D-DIN numeral ([display], hero about 72sp) with the tracked-caps [label] below it in
//      Montserrat SemiBold (the WHOOP two-font rule: numerals D-DIN, words Montserrat).
//
// Colour comes ONLY from palette tokens: [tint] is a band/domain colour the caller samples from
// Palette (e.g. Palette.recoveryBand(score) or Palette.effortColor); the track + text are palette tokens.

/** The three ring sizes, matching the Swift `RingDial.Size` (dp == the Swift pt values). */
enum class RingDialSize(val diameter: Dp) {
    /** Home-screen trio (Rest / Charge / Effort). 96dp. */
    Trio(96.dp),

    /** Detail-screen hero. 260dp. */
    Hero(260.dp),

    /** Mini card accent. 44dp. */
    Mini(44.dp),
}

/**
 * A WHOOP score ring. [value] is the 0..1 fill fraction; [display] is the centred numeral text
 * ("85%" / "14.2"); [label] is the tracked-caps word below it ("RECOVERY"); [tint] is the arc's band
 * colour (a Palette token the caller samples). [size] picks the trio / hero / mini geometry.
 *
 * The arc starts at 12 o'clock, sweeps clockwise, and animates 0 to [value] on first appearance over 0.8s
 * ease-out (snaps instantly under Reduce Motion). Mirrors the iOS `RingDial` view.
 */
@Composable
fun RingDial(
    value: Double,
    display: String,
    label: String,
    tint: Color,
    size: RingDialSize,
    modifier: Modifier = Modifier,
    showsLabel: Boolean = true,
) {
    val diameter = size.diameter
    val target = value.toFloat().coerceIn(0f, 1f)
    val reduced = rememberReduceMotion()

    // Fill animates 0 to value on FIRST composition (WHOOP's draw-in), over 0.8s ease-out. Under Reduce
    // Motion it lands immediately (tween 0). Mirrors iOS `.animation(.easeOut(duration: 0.8))` on appear.
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val animatedFraction by animateFloatAsState(
        targetValue = if (started) target else 0f,
        animationSpec = if (reduced) tween(0) else tween(durationMillis = 800, easing = Motion.easeOut),
        label = "ringDialFill",
    )

    // Track colour: the WHOOP dark-grey hairline well at 30% opacity (design contract "#2A343A at 30%").
    val trackColor = Palette.hairline.copy(alpha = 0.30f)

    Box(
        modifier = modifier
            .size(diameter)
            .semantics { contentDescription = if (showsLabel) "$label $display" else display },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(diameter)
                .drawWithCache {
                    // Stroke about 10% of the diameter (thick WHOOP arc), round caps on both track and fill.
                    val stroke = size.minDimension * 0.10f
                    val inset = stroke / 2f
                    val d = size.minDimension
                    val arcSize = Size(d - stroke, d - stroke)
                    val topLeft = Offset(
                        (size.width - d) / 2f + inset,
                        (size.height - d) / 2f + inset,
                    )
                    val capStroke = Stroke(width = stroke, cap = StrokeCap.Round)
                    onDrawBehind {
                        // Full-circle track (the carved well the arc sits in), static, so the fraction-driven
                        // arc below is the only per-frame draw.
                        drawArc(
                            color = trackColor,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = capStroke,
                        )
                        // The fill arc, from 12 o'clock (-90 degrees) clockwise. Only drawn once there is
                        // actual progress: a near-zero round-capped arc otherwise renders as a stray dot at 12
                        // o'clock on Android's Canvas (the empty state reads as the clean track alone).
                        val sweep = animatedFraction.coerceIn(0f, 1f) * 360f
                        if (animatedFraction > 0.001f) {
                            drawArc(
                                color = tint,
                                startAngle = -90f,
                                sweepAngle = sweep,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = capStroke,
                            )
                        }
                    }
                },
        )

        // Centred numeral (D-DIN) + tracked caps label (Montserrat SemiBold), scaled to the ring size.
        val numberSp = when (size) {
            RingDialSize.Hero -> 72f
            RingDialSize.Trio -> 26f
            RingDialSize.Mini -> 15f
        }
        val labelSp = when (size) {
            RingDialSize.Hero -> 13f
            RingDialSize.Trio -> 10f
            RingDialSize.Mini -> 8f
        }
        // The mini ring is too small to carry a word under the numeral; it shows the numeral alone.
        val labelVisible = showsLabel && size != RingDialSize.Mini && label.isNotEmpty()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(PaddingValues(horizontal = diameter * 0.12f)),
        ) {
            Text(
                text = display,
                style = NoopType.number(numberSp, FontWeight.Bold),
                color = Palette.textPrimary,
                maxLines = 1,
                textAlign = TextAlign.Center,
            )
            if (labelVisible) {
                Text(
                    text = label.uppercase(),
                    style = NoopType.text(labelSp, FontWeight.SemiBold).copy(letterSpacing = (labelSp * 0.14f).sp),
                    color = Palette.textSecondary,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = diameter * 0.02f),
                )
            }
        }
    }
}
