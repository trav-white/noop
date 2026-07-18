import SwiftUI

// MARK: - RingDial (WHOOP score dial)
//
// The WHOOP score ring: a thick, rounded-cap arc on a faint dark-grey track, starting
// at 12 o'clock and filling clockwise to `value` (0...1). A D-DIN numeral sits centred,
// with a tracked ALL-CAPS Montserrat label. The arc draws in from empty over 0.8s
// ease-out on appear (snaps instantly under Reduce Motion). No bloom, WHOOP-flat.
//
// Three sizes match the reference frames:
//   trio  96pt, the home three-up row (Rest / Charge / Effort). Label sits BELOW the ring.
//   hero  260pt, the detail-screen headline dial. Numeral about 72pt, label INSIDE the ring.
//   mini  44pt, small card/tile dials. Numeral only.
//
// Replaces the LiquidVessel gauge as the app's primary score glyph. Public API is stable:
// screens construct it by (value, display, label, tint, size).

public struct RingDial: View {

    /// The three canonical dial sizes.
    public enum Size {
        case trio   // 96pt, home three-up
        case hero   // 260pt, detail hero
        case mini   // 44pt, card/tile

        /// Outer diameter of the ring.
        var diameter: CGFloat {
            switch self {
            case .trio: return 96
            case .hero: return 260
            case .mini: return 44
            }
        }

        /// Stroke thickness: a thick arc at about 10% of the diameter (WHOOP-faithful).
        var lineWidth: CGFloat { diameter * 0.10 }

        /// Centre numeral point size (D-DIN). Hero is the ~72pt headline score.
        var numberSize: CGFloat {
            switch self {
            case .trio: return 26
            case .hero: return 72
            case .mini: return 15
            }
        }

        /// Caps-label point size (Montserrat semibold).
        var labelSize: CGFloat {
            switch self {
            case .trio: return 10
            case .hero: return 13
            case .mini: return 8
            }
        }

        /// Tracking for the caps label (about 0.12em).
        var labelTracking: CGFloat { labelSize * 0.12 }

        /// The hero carries its label INSIDE the ring beneath the numeral; the smaller
        /// dials place it below the ring so the centre stays uncrowded.
        var labelInsideRing: Bool { self == .hero }

        /// The mini dial shows the numeral only (label reserved for accessibility).
        var showsLabel: Bool { self != .mini }
    }

    /// Fill fraction 0...1 (e.g. recoveryScore / 100, or dayStrain / 21).
    public var value: Double
    /// The centred read-out string, e.g. "85%" or "14.2".
    public var display: String
    /// The tracked ALL-CAPS label, e.g. "RECOVERY" / "DAY STRAIN".
    public var label: String
    /// Arc tint: a band/domain colour from `StrandPalette`.
    public var tint: Color
    /// Which of the three canonical sizes to render.
    public var size: Size

    public init(value: Double, display: String, label: String, tint: Color, size: Size) {
        self.value = value
        self.display = display
        self.label = label
        self.tint = tint
        self.size = size
    }

    @State private var animatedFraction: Double = 0
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    private var fraction: Double { min(max(value, 0), 1) }

    public var body: some View {
        Group {
            if size.showsLabel && !size.labelInsideRing {
                VStack(spacing: 8) {
                    ring
                    capsLabel
                }
            } else {
                ring
            }
        }
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(Text(label))
        .accessibilityValue(Text(display))
        .onAppear {
            if reduceMotion {
                animatedFraction = fraction
            } else {
                withAnimation(.easeOut(duration: 0.8)) { animatedFraction = fraction }
            }
        }
        .onChangeCompat(of: fraction) { newValue in
            if reduceMotion {
                animatedFraction = newValue
            } else {
                withAnimation(.easeOut(duration: 0.8)) { animatedFraction = newValue }
            }
        }
    }

    // MARK: Ring + centre content

    private var ring: some View {
        ZStack {
            // Faint dark-grey full-circle track (hairline #2A343A at 30% opacity).
            RingDialArc(fraction: 1, lineWidth: size.lineWidth)
                .stroke(
                    StrandPalette.hairline.opacity(0.30),
                    style: StrokeStyle(lineWidth: size.lineWidth, lineCap: .round)
                )

            // Filled arc: solid tint, rounded caps, 12 o'clock start, clockwise.
            RingDialArc(fraction: animatedFraction, lineWidth: size.lineWidth)
                .stroke(
                    tint,
                    style: StrokeStyle(lineWidth: size.lineWidth, lineCap: .round)
                )

            // Centre read-out: D-DIN numeral, plus the caps label for the hero.
            VStack(spacing: size == .hero ? 4 : 1) {
                Text(display)
                    .font(StrandFont.number(size.numberSize, weight: .bold))
                    .tracking(-size.numberSize * 0.02)
                    .foregroundStyle(StrandPalette.textPrimary)
                    .minimumScaleFactor(0.6)
                    .lineLimit(1)
                if size.labelInsideRing {
                    capsLabel
                }
            }
            .padding(.horizontal, size.lineWidth)
        }
        .frame(width: size.diameter, height: size.diameter)
    }

    private var capsLabel: some View {
        Text(label)
            .font(StrandFont.text(size.labelSize, weight: .semibold))
            .tracking(size.labelTracking)
            .textCase(.uppercase)
            .foregroundStyle(StrandPalette.textSecondary)
            .lineLimit(1)
            .allowsHitTesting(false)
    }
}

// MARK: - Ring arc shape
//
// A closed-track fill arc: starts at 12 o'clock and sweeps CLOCKWISE by `fraction`
// of the full circle. `clockwise: false` in `addArc` draws clockwise on screen because
// SwiftUI's y-axis points down (same convention as `RecoveryArc`).

/// The RingDial fill arc: 12 o'clock start, clockwise, `fraction` of 360 degrees.
public struct RingDialArc: Shape {
    public var fraction: Double
    public var lineWidth: CGFloat

    public init(fraction: Double, lineWidth: CGFloat) {
        self.fraction = fraction
        self.lineWidth = lineWidth
    }

    public var animatableData: Double {
        get { fraction }
        set { fraction = newValue }
    }

    public func path(in rect: CGRect) -> Path {
        let radius = (min(rect.width, rect.height) - lineWidth) / 2
        let center = CGPoint(x: rect.midX, y: rect.midY)
        let start = Angle.degrees(-90)   // 12 o'clock
        let end = Angle.degrees(-90 + 360 * min(max(fraction, 0), 1))
        var path = Path()
        path.addArc(
            center: center,
            radius: radius,
            startAngle: start,
            endAngle: end,
            clockwise: false
        )
        return path
    }
}

#if DEBUG
#Preview("RingDial sizes") {
    VStack(spacing: 40) {
        HStack(spacing: 24) {
            RingDial(value: 0.80, display: "80%", label: "Rest",
                     tint: StrandPalette.restColor, size: .trio)
            RingDial(value: 0.85, display: "85%", label: "Charge",
                     tint: StrandPalette.recoveryBand(85), size: .trio)
            RingDial(value: 0.68, display: "14.2", label: "Effort",
                     tint: StrandPalette.strainColor(68), size: .trio)
        }
        RingDial(value: 0.85, display: "85%", label: "Recovery",
                 tint: StrandPalette.recoveryBand(85), size: .hero)
        HStack(spacing: 16) {
            RingDial(value: 0.55, display: "55", label: "Mini",
                     tint: StrandPalette.recoveryBand(55), size: .mini)
            RingDial(value: 0.30, display: "30", label: "Mini",
                     tint: StrandPalette.recoveryBand(30), size: .mini)
        }
    }
    .padding(40)
    .frame(width: 520, height: 640)
    .background(StrandPalette.canvasGradient)
    .preferredColorScheme(.dark)
}
#endif
