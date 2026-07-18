import SwiftUI

// MARK: - WhoopChrome
//
// The shared WHOOP page furniture that sits around the RingDial: the full-bleed slate
// canvas, the metric list rows, the "Today vs. prior 30 days" footer pill, the insight
// callout card, the two-up monitor tiles, and the tracked-caps label helper. All of it
// reads from StrandPalette / StrandFont only (no raw hexes), so a palette re-theme flows
// through automatically. Matches reference frames 01, 03 and 04.

// MARK: - TrackedCapsLabel
//
// Small ALL-CAPS Montserrat semibold with wide tracking: the WHOOP section/row label.
// Used by MetricRow, MonitorTile and any screen that needs a house caps label.

/// A tracked ALL-CAPS Montserrat-semibold label (the WHOOP row/section label).
public struct TrackedCapsLabel: View {
    public var text: String
    public var size: CGFloat
    public var color: Color

    public init(_ text: String, size: CGFloat = 11, color: Color = StrandPalette.textSecondary) {
        self.text = text
        self.size = size
        self.color = color
    }

    public var body: some View {
        Text(text)
            .font(StrandFont.text(size, weight: .semibold))
            .tracking(size * 0.12)
            .textCase(.uppercase)
            .foregroundStyle(color)
    }
}

// MARK: - CanvasBackground
//
// The full-bleed WHOOP slate gradient (#283339 top to #101518 bottom). Replaces BOTH the
// flat navy background and the old animated sky sunset header everywhere. Non-interactive
// and hidden from accessibility (pure decoration). Drop it in a `.background { }` or a ZStack.

/// The full-bleed WHOOP slate canvas gradient. Full-bleed by default (ignores safe area).
public struct CanvasBackground: View {
    public init() {}

    public var body: some View {
        StrandPalette.canvasGradient
            .ignoresSafeArea()
            .allowsHitTesting(false)
            .accessibilityHidden(true)
    }
}

// MARK: - MetricRow
//
// One row of a WHOOP metric list: a leading line icon, a tracked caps label, a right-aligned
// D-DIN value, and an up / down / steady trend mark. A 1px hairline separator sits under the
// row (suppressed on the last row via `showsSeparator: false`). Frames 03 / 04.

/// The direction of a metric's trend vs. its baseline.
public enum MetricTrend {
    case up      // green up triangle
    case down    // amber down triangle
    case steady  // neutral grey dot
}

/// A single WHOOP metric list row: icon + caps label + value + trend mark, over a hairline.
public struct MetricRow: View {
    public var systemImage: String?
    public var label: String
    public var value: String
    public var trend: MetricTrend?
    public var showsSeparator: Bool

    public init(
        label: String,
        value: String,
        systemImage: String? = nil,
        trend: MetricTrend? = nil,
        showsSeparator: Bool = true
    ) {
        self.label = label
        self.value = value
        self.systemImage = systemImage
        self.trend = trend
        self.showsSeparator = showsSeparator
    }

    public var body: some View {
        VStack(spacing: 0) {
            HStack(spacing: 12) {
                if let systemImage {
                    Image(systemName: systemImage)
                        .font(.system(size: 16, weight: .medium))
                        .foregroundStyle(StrandPalette.textSecondary)
                        .frame(width: 22, alignment: .center)
                }
                TrackedCapsLabel(label, size: 12, color: StrandPalette.textPrimary)
                Spacer(minLength: 12)
                Text(value)
                    .font(StrandFont.number(17, weight: .bold))
                    .foregroundStyle(StrandPalette.textPrimary)
                if let trend {
                    trendMark(trend)
                }
            }
            .padding(.vertical, 14)
            if showsSeparator {
                Rectangle()
                    .fill(StrandPalette.hairline)
                    .frame(height: 1)
            }
        }
        .accessibilityElement(children: .combine)
    }

    @ViewBuilder
    private func trendMark(_ trend: MetricTrend) -> some View {
        switch trend {
        case .up:
            Image(systemName: "arrowtriangle.up.fill")
                .font(.system(size: 10))
                .foregroundStyle(StrandPalette.statusPositive)
        case .down:
            Image(systemName: "arrowtriangle.down.fill")
                .font(.system(size: 10))
                .foregroundStyle(StrandPalette.statusWarning)
        case .steady:
            Circle()
                .fill(StrandPalette.textTertiary)
                .frame(width: 5, height: 5)
        }
    }
}

// MARK: - TrendFooterPill
//
// The dark rounded pill under a metric list: two trend triangles (green up, amber down)
// then a bold leading word and a secondary trailing phrase, e.g. "Today vs. prior 30 days".
// Frames 03 / 04.

/// The "Today vs. prior 30 days" footer pill: trend legend + bold/secondary caption.
public struct TrendFooterPill: View {
    public var leading: String
    public var trailing: String

    public init(leading: String = "Today", trailing: String = "vs. prior 30 days") {
        self.leading = leading
        self.trailing = trailing
    }

    public var body: some View {
        HStack(spacing: 8) {
            Image(systemName: "arrowtriangle.up.fill")
                .font(.system(size: 9))
                .foregroundStyle(StrandPalette.statusPositive)
            Image(systemName: "arrowtriangle.down.fill")
                .font(.system(size: 9))
                .foregroundStyle(StrandPalette.statusWarning)
            Text(leading)
                .font(StrandFont.text(13, weight: .semibold))
                .foregroundStyle(StrandPalette.textPrimary)
            Text(trailing)
                .font(StrandFont.text(13, weight: .regular))
                .foregroundStyle(StrandPalette.textSecondary)
            Spacer(minLength: 0)
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 10)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(
            RoundedRectangle(cornerRadius: 10, style: .continuous)
                .fill(StrandPalette.surfaceInset)
        )
        .accessibilityElement(children: .combine)
    }
}

// MARK: - InsightCallout
//
// A dark card with a thin accent border, a line of prose, and a tracked-caps link with a
// trailing arrow (the WHOOP "BREAK DOWN MY RECOVERY ->" affordance). The accent defaults to
// the CTA teal; recovery/strain screens can pass their domain tint. Frame 03 / 04.

/// A dark insight card: thin accent border, prose, and a caps link with a trailing arrow.
public struct InsightCallout: View {
    public var message: String
    public var linkTitle: String
    public var accent: Color
    public var action: (() -> Void)?

    public init(
        message: String,
        linkTitle: String,
        accent: Color = StrandPalette.accent,
        action: (() -> Void)? = nil
    ) {
        self.message = message
        self.linkTitle = linkTitle
        self.accent = accent
        self.action = action
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(message)
                .font(StrandFont.body)
                .foregroundStyle(StrandPalette.textPrimary)
                .fixedSize(horizontal: false, vertical: true)
            link
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(
            RoundedRectangle(cornerRadius: NoopMetrics.cardRadius, style: .continuous)
                .fill(StrandPalette.surfaceRaised)
        )
        .overlay(
            RoundedRectangle(cornerRadius: NoopMetrics.cardRadius, style: .continuous)
                .strokeBorder(accent.opacity(0.55), lineWidth: 1)
        )
    }

    private var link: some View {
        let content = HStack(spacing: 6) {
            TrackedCapsLabel(linkTitle, size: 12, color: accent)
            Image(systemName: "arrow.right")
                .font(.system(size: 12, weight: .semibold))
                .foregroundStyle(accent)
        }
        return Group {
            if let action {
                Button(action: action) { content }
                    .buttonStyle(.plain)
            } else {
                content
            }
        }
    }
}

// MARK: - MonitorTile
//
// A two-up caps-header tile (HEALTH MONITOR / STRESS MONITOR): a tracked caps title with an
// optional trailing chevron, then a content slot. Place two side by side in an HStack for the
// WHOOP two-up row. Frame 01.

/// A WHOOP monitor tile: caps header (optional chevron) over a content slot.
public struct MonitorTile<Content: View>: View {
    public var title: String
    public var showsChevron: Bool
    @ViewBuilder public var content: () -> Content

    public init(
        title: String,
        showsChevron: Bool = true,
        @ViewBuilder content: @escaping () -> Content
    ) {
        self.title = title
        self.showsChevron = showsChevron
        self.content = content
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 4) {
                TrackedCapsLabel(title, size: 11, color: StrandPalette.textSecondary)
                Spacer(minLength: 4)
                if showsChevron {
                    Image(systemName: "chevron.right")
                        .font(.system(size: 11, weight: .semibold))
                        .foregroundStyle(StrandPalette.textTertiary)
                }
            }
            content()
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(
            RoundedRectangle(cornerRadius: NoopMetrics.cardRadius, style: .continuous)
                .fill(StrandPalette.surfaceRaised)
        )
        .overlay(
            RoundedRectangle(cornerRadius: NoopMetrics.cardRadius, style: .continuous)
                .strokeBorder(StrandPalette.hairline, lineWidth: 1)
        )
    }
}

#if DEBUG
#Preview("WhoopChrome") {
    ScrollView {
        VStack(spacing: 20) {
            HStack(spacing: 12) {
                MonitorTile(title: "Health Monitor") {
                    HStack(spacing: 8) {
                        Image(systemName: "checkmark.circle.fill")
                            .foregroundStyle(StrandPalette.statusPositive)
                        VStack(alignment: .leading, spacing: 2) {
                            TrackedCapsLabel("Within Range", size: 10, color: StrandPalette.statusPositive)
                            Text("5/5 Metrics")
                                .font(StrandFont.subhead)
                                .foregroundStyle(StrandPalette.textSecondary)
                        }
                    }
                }
                MonitorTile(title: "Stress Monitor") {
                    HStack(spacing: 8) {
                        Text("1.5")
                            .font(StrandFont.number(15, weight: .bold))
                            .foregroundStyle(StrandPalette.stressColor)
                        VStack(alignment: .leading, spacing: 2) {
                            TrackedCapsLabel("Medium", size: 10, color: StrandPalette.textPrimary)
                            Text("4:31pm")
                                .font(StrandFont.subhead)
                                .foregroundStyle(StrandPalette.textSecondary)
                        }
                    }
                }
            }

            VStack(spacing: 0) {
                MetricRow(label: "Heart Rate Variability", value: "124",
                          systemImage: "waveform.path.ecg", trend: .up)
                MetricRow(label: "Resting Heart Rate", value: "49",
                          systemImage: "heart", trend: .up)
                MetricRow(label: "Respiratory Rate", value: "14.5",
                          systemImage: "lungs", trend: .steady, showsSeparator: false)
            }
            .padding(.horizontal, 16)
            .background(
                RoundedRectangle(cornerRadius: NoopMetrics.cardRadius, style: .continuous)
                    .fill(StrandPalette.surfaceRaised)
            )

            TrendFooterPill()

            InsightCallout(
                message: "Your HRV is elevated while your RHR, Respiratory Rate, and Sleep Performance are all typical, resulting in a higher Recovery today.",
                linkTitle: "Break down my recovery"
            )
        }
        .padding(20)
    }
    .frame(width: 420, height: 720)
    .background(CanvasBackground())
    .preferredColorScheme(.dark)
}
#endif
