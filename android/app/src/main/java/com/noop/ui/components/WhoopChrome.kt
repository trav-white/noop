package com.noop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noop.ui.Metrics
import com.noop.ui.NoopType
import com.noop.ui.Palette

// MARK: - WhoopChrome (the shared WHOOP layout furniture, Compose parity with StrandDesign/WhoopChrome.swift)
//
// The WHOOP "chrome" that every restyled screen composes on top of the RingDial:
//   1. CanvasBackground  the slate vertical-gradient page background (replaces LiquidSky everywhere).
//   2. TrackedCapsLabel  the small uppercase, wide-tracking Montserrat SemiBold label used app-wide.
//   3. MetricRow         icon + tracked caps label + right value + up/down trend triangle, hairline rule.
//   4. TrendFooterPill   the "Today vs. prior 30 days" comparison pill under a metric list.
//   5. InsightCallout    a dark card with a thin teal/blue border and a caps link with an arrow.
//   6. MonitorTile       the two-up HEALTH MONITOR / STRESS MONITOR tile pattern.
//
// Colour is ONLY palette tokens (no raw hexes). Mirrors the Swift contract and the reference screenshots.

// MARK: - CanvasBackground

/**
 * The WHOOP slate page background: a vertical gradient from lighter slate (#283339) at the top to
 * near-black (#101518) at the bottom, built from [Palette.canvasGradient]. Fills its box and lays
 * [content] over the gradient. Replaces the old animated LiquidSky header AND the flat navy canvas on
 * every screen. Mirrors the Swift `CanvasBackground` view.
 */
@Composable
fun CanvasBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Palette.canvasGradient()),
    ) {
        content()
    }
}

// MARK: - TrackedCapsLabel

/**
 * The WHOOP tracked-caps label: uppercase, wide letter-tracking, Montserrat SemiBold, secondary grey by
 * default. The one building block behind every caps label in the chrome (metric rows, monitor tiles,
 * footer pills). [size] is the point size; tracking scales with it. Mirrors the Swift `TrackedCapsLabel`.
 */
@Composable
fun TrackedCapsLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Palette.textSecondary,
    size: Float = 11f,
    weight: FontWeight = FontWeight.SemiBold,
) {
    Text(
        text = text.uppercase(),
        style = NoopType.text(size, weight).copy(letterSpacing = (size * 0.14f).sp),
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

// MARK: - MetricRow

/** The direction of a [MetricRow]'s trend triangle. */
enum class MetricTrend {
    /** Rising: a green up triangle. */
    Up,

    /** Falling: a red down triangle. */
    Down,

    /** Flat / neutral: a muted dash. */
    Flat,

    /** No trend shown at all. */
    None,
}

/**
 * A single WHOOP metric row: an optional leading [icon], the tracked-caps [label], then the right-aligned
 * [value] (D-DIN) and an optional up/down [trend] triangle. A hairline rule sits under the row unless
 * [showDivider] is false (the last row in a list drops it). Mirrors the Swift `MetricRow`.
 */
@Composable
fun MetricRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    trend: MetricTrend = MetricTrend.None,
    valueColor: Color = Palette.textPrimary,
    showDivider: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (showDivider) {
                    Modifier.drawBehind {
                        val y = size.height
                        drawLine(
                            color = Palette.hairline,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = Metrics.divider.toPx(),
                        )
                    }
                } else {
                    Modifier
                },
            )
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Palette.textSecondary,
                modifier = Modifier.size(Metrics.iconSmall),
            )
        }
        TrackedCapsLabel(text = label, modifier = Modifier.weight(1f))
        Text(
            text = value,
            style = NoopType.number(17f, FontWeight.SemiBold),
            color = valueColor,
            maxLines = 1,
        )
        val triangle: Pair<String, Color>? = when (trend) {
            MetricTrend.Up -> "▲" to Palette.statusPositive
            MetricTrend.Down -> "▼" to Palette.statusCritical
            MetricTrend.Flat -> "-" to Palette.textTertiary
            MetricTrend.None -> null
        }
        if (triangle != null) {
            Text(
                text = triangle.first,
                style = NoopType.captionNumber.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                color = triangle.second,
            )
        }
    }
}

// MARK: - TrendFooterPill

/**
 * The comparison pill that sits under a metric list, e.g. "Today vs. prior 30 days". A rounded, inset pill
 * with a tracked-caps label centred inside a hairline border. Mirrors the Swift `TrendFooterPill`.
 */
@Composable
fun TrendFooterPill(
    text: String = "Today vs. prior 30 days",
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(Metrics.cornerPill)
    Box(
        modifier = modifier
            .clip(shape)
            .background(Palette.surfaceInset)
            .border(1.dp, Palette.hairline, shape)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        TrackedCapsLabel(text = text, color = Palette.textTertiary, size = 10f)
    }
}

// MARK: - InsightCallout

/**
 * A WHOOP insight callout: a dark card with a thin [tint] (teal / blue) border carrying a body [text] and
 * an optional caps [linkLabel] with a trailing arrow. Tapping fires [onClick] (only wired when a link is
 * present). Mirrors the Swift `InsightCallout`.
 */
@Composable
fun InsightCallout(
    text: String,
    modifier: Modifier = Modifier,
    linkLabel: String? = null,
    tint: Color = Palette.accent,
    onClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(Metrics.cardRadius)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Palette.surfaceRaised)
            .border(1.dp, tint.copy(alpha = 0.55f), shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = text,
            style = NoopType.subhead,
            color = Palette.textSecondary,
        )
        if (linkLabel != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                TrackedCapsLabel(text = linkLabel, color = tint, size = 11f, weight = FontWeight.Bold)
                Text(
                    text = "→",
                    style = NoopType.caption.copy(fontWeight = FontWeight.Bold),
                    color = tint,
                )
            }
        }
    }
}

// MARK: - MonitorTile

/**
 * A WHOOP monitor tile: the tracked-caps [title] (e.g. "HEALTH MONITOR"), a big D-DIN [value] and an
 * optional [caption]. An optional [content] slot sits below the value for a sparkline or status row. A
 * [tint] colours the title toward its domain world. Compose two side by side in a weighted Row for the
 * two-up HEALTH MONITOR / STRESS MONITOR pattern. Mirrors the Swift `MonitorTile`.
 */
@Composable
fun MonitorTile(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    caption: String? = null,
    tint: Color = Palette.accent,
    valueColor: Color = Palette.textPrimary,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(Metrics.cardRadius)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Palette.surfaceRaised)
            .border(1.dp, Palette.hairline, shape)
            .padding(Metrics.cardPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TrackedCapsLabel(text = title, color = tint, size = 11f)
        Text(
            text = value,
            style = NoopType.number(26f, FontWeight.Bold),
            color = valueColor,
            maxLines = 1,
        )
        if (caption != null) {
            Text(
                text = caption,
                style = NoopType.footnote,
                color = Palette.textTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (content != null) {
            content()
        }
    }
}
