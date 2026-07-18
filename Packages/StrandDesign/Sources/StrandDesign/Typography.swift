import SwiftUI
#if !os(watchOS)
import CoreText
#endif

// MARK: - Strand Typography (WHOOP two-font rule)
//
// D-DIN for every NUMBER (scores, tiles, live values, sparklines) and Montserrat for
// every WORD (titles, prose, labels). Both faces are bundled under Resources/Fonts and
// registered with Core Text once, lazily, on first use. If registration fails (or on
// watchOS, where we do not register), each role falls back to the matching system font
// so text never disappears.
//
// PUBLIC API IS FROZEN: every symbol below is depended on by screens across macOS / iOS /
// watchOS, so names never change. Only the underlying face and the new `text(_:weight:)`
// helper were added.
//
// All numeric styles use `.monospacedDigit()` so live values do not reflow.

// MARK: - Font registration

/// Registers the bundled D-DIN and Montserrat faces with Core Text exactly once.
/// `didRegister` is a lazy static, so the work runs a single time and is thread-safe.
private enum StrandFontRegistry {

    /// True once at least one bundled face registered (or was already registered).
    static let didRegister: Bool = register()

    /// Font resource base names (PostScript names) shipped in Resources/Fonts as .otf.
    private static let faces = [
        "D-DIN", "D-DIN-Bold", "D-DINCondensed",
        "Montserrat-Regular", "Montserrat-Medium", "Montserrat-SemiBold", "Montserrat-Bold",
    ]

    private static func register() -> Bool {
        #if os(watchOS)
        // The watch app does not register custom faces; roles resolve to the system font.
        return false
        #else
        var ok = false
        for face in faces {
            let url = Bundle.module.url(forResource: face, withExtension: "otf")
                ?? Bundle.module.url(forResource: face, withExtension: "otf", subdirectory: "Fonts")
            guard let fontURL = url else { continue }
            var cfError: Unmanaged<CFError>?
            if CTFontManagerRegisterFontsForURL(fontURL as CFURL, .process, &cfError) {
                ok = true
            } else if let error = cfError?.takeRetainedValue(),
                      CFErrorGetCode(error) == CTFontManagerError.alreadyRegistered.rawValue {
                // A repeat registration of the same URL is not a real failure.
                ok = true
            }
        }
        return ok
        #endif
    }
}

public enum StrandFont {

    // MARK: Face resolution

    /// First read triggers the one-time Core Text registration.
    private static var registered: Bool { StrandFontRegistry.didRegister }

    /// D-DIN PostScript face name for a weight (the family ships Regular + Bold only).
    private static func numberFace(_ weight: Font.Weight) -> String {
        switch weight {
        case .bold, .heavy, .black: return "D-DIN-Bold"
        default:                    return "D-DIN"
        }
    }

    /// Montserrat PostScript face name for a weight.
    private static func textFace(_ weight: Font.Weight) -> String {
        switch weight {
        case .bold, .heavy, .black: return "Montserrat-Bold"
        case .semibold:             return "Montserrat-SemiBold"
        case .medium:               return "Montserrat-Medium"
        default:                    return "Montserrat-Regular"
        }
    }

    /// Fixed-size D-DIN numeral. Used by big gauge/tile numerals in fixed geometry where
    /// unbounded Dynamic-Type growth would overflow. Falls back to the system font.
    private static func numberFont(_ size: CGFloat, weight: Font.Weight) -> Font {
        registered ? .custom(numberFace(weight), size: size)
                   : .system(size: size, weight: weight)
    }

    /// D-DIN numeral that SCALES with Dynamic Type, anchored to a text style.
    private static func numberFont(_ size: CGFloat, weight: Font.Weight,
                                   relativeTo style: Font.TextStyle) -> Font {
        registered ? .custom(numberFace(weight), size: size, relativeTo: style)
                   : .system(size: size, weight: weight)
    }

    /// Fixed-size Montserrat word face. Falls back to the system font.
    private static func textFont(_ size: CGFloat, weight: Font.Weight) -> Font {
        registered ? .custom(textFace(weight), size: size)
                   : .system(size: size, weight: weight)
    }

    /// Montserrat word face that SCALES with Dynamic Type, anchored to a text style.
    private static func textFont(_ size: CGFloat, weight: Font.Weight,
                                 relativeTo style: Font.TextStyle) -> Font {
        registered ? .custom(textFace(weight), size: size, relativeTo: style)
                   : .system(size: size, weight: weight)
    }

    // MARK: Scale

    /// Display 64 to 80 / Bold, the gauge score number. D-DIN Bold with tight tracking
    /// (about -0.04em), tabular digits so a changing value never reflows.
    public static func display(_ size: CGFloat = 72) -> Font {
        numberFont(size, weight: .bold).monospacedDigit()
    }

    /// The tight tracking for big display numbers (about -0.04em). Apply alongside
    /// `display(_:)` at the use site, e.g. `.tracking(StrandFont.displayTracking(72))`.
    public static func displayTracking(_ size: CGFloat = 72) -> CGFloat {
        -size * 0.04
    }

    /// A D-DIN numeric style at an arbitrary size/weight, the house numeral. Tabular so
    /// live values align. Use anywhere a score/number is shown.
    public static func rounded(_ size: CGFloat, weight: Font.Weight = .bold) -> Font {
        numberFont(size, weight: weight).monospacedDigit()
    }

    /// Title1 28 / Bold. Scales with Dynamic Type.
    public static let title1 = textFont(28, weight: .bold, relativeTo: .title)

    /// Title2 22 / Semibold. Scales with Dynamic Type.
    public static let title2 = textFont(22, weight: .semibold, relativeTo: .title2)

    /// Headline 17 / Semibold. Scales with Dynamic Type.
    public static let headline = textFont(17, weight: .semibold, relativeTo: .headline)

    /// Body 15 / Regular. Scales with Dynamic Type.
    public static let body = textFont(15, weight: .regular, relativeTo: .body)

    /// Subhead 13. Scales with Dynamic Type.
    public static let subhead = textFont(13, weight: .regular, relativeTo: .subheadline)

    /// Caption 12. Scales with Dynamic Type.
    public static let caption = textFont(12, weight: .regular, relativeTo: .caption)

    /// Footnote 11. Scales with Dynamic Type.
    public static let footnote = textFont(11, weight: .regular, relativeTo: .footnote)

    /// Overline 11 / Semibold, +1.4 tracking (apply `.tracking(1.4)` at use site;
    /// `overlineText(_:)` does it for you). Sparing ALL-CAPS labels. Scales with Dynamic Type.
    public static let overline = textFont(11, weight: .semibold, relativeTo: .caption2)

    /// `overline` at a custom point size, same Montserrat face, weight and Dynamic-Type
    /// scaling (relativeTo `.caption2`), just smaller. Passing 11 returns exactly `.overline`.
    public static func overlineScaled(_ size: CGFloat) -> Font {
        textFont(size, weight: .semibold, relativeTo: .caption2)
    }

    /// Mono 13 (SF Mono), raw / log views. Tabular by nature.
    public static let mono = Font.system(size: 13, weight: .regular, design: .monospaced)

    // MARK: Numeric variants (tabular digits)

    /// A D-DIN numeric style at an arbitrary size/weight, for live values. Tabular digits.
    /// This is the tile/value numeral.
    public static func number(_ size: CGFloat, weight: Font.Weight = .semibold) -> Font {
        numberFont(size, weight: weight).monospacedDigit()
    }

    /// A Montserrat word style at an arbitrary size/weight, for arbitrary prose/labels that
    /// need a specific point size outside the named scale.
    public static func text(_ size: CGFloat, weight: Font.Weight = .regular) -> Font {
        textFont(size, weight: weight)
    }

    /// D-DIN body number, for inline live values that should align. Scales with Dynamic Type
    /// alongside its sibling `body`/`caption` labels so a value and its label stay matched.
    public static let bodyNumber = numberFont(15, weight: .medium, relativeTo: .body).monospacedDigit()

    /// D-DIN caption number, for small live values (sparklines, chips). Scales with Dynamic Type.
    public static let captionNumber = numberFont(12, weight: .medium, relativeTo: .caption).monospacedDigit()

    /// Mono at an arbitrary size.
    public static func mono(_ size: CGFloat, weight: Font.Weight = .regular) -> Font {
        .system(size: size, weight: weight, design: .monospaced)
    }

    /// The recommended tracking for overline text (wide ALL-CAPS labels, about 0.13em).
    public static let overlineTracking: CGFloat = 1.4
}

// MARK: - Text helpers

public extension Text {
    /// Style as an overline label: ALL-CAPS, semibold, +1.4 tracking, secondary text.
    func strandOverline() -> some View {
        self.font(StrandFont.overline)
            .tracking(StrandFont.overlineTracking)
            .textCase(.uppercase)
            .foregroundStyle(StrandPalette.textSecondary)
    }
}

public extension View {
    /// Convenience: an overline-styled label string.
    static func strandOverline(_ string: String) -> some View {
        Text(string).strandOverline()
    }
}

#if DEBUG
#Preview("Typography") {
    ScrollView {
        VStack(alignment: .leading, spacing: 18) {
            Text("88").font(StrandFont.display(72)).tracking(StrandFont.displayTracking(72)).foregroundStyle(StrandPalette.textPrimary)
            Text("Title 1 / Bold 28").font(StrandFont.title1).foregroundStyle(StrandPalette.textPrimary)
            Text("Title 2 / Semibold 22").font(StrandFont.title2).foregroundStyle(StrandPalette.textPrimary)
            Text("Headline / Semibold 17").font(StrandFont.headline).foregroundStyle(StrandPalette.textPrimary)
            Text("Body / Regular 15, the thread of you, read in full.")
                .font(StrandFont.body).foregroundStyle(StrandPalette.textPrimary)
            Text("Subhead 13").font(StrandFont.subhead).foregroundStyle(StrandPalette.textSecondary)
            Text("Caption 12").font(StrandFont.caption).foregroundStyle(StrandPalette.textSecondary)
            Text("Footnote 11").font(StrandFont.footnote).foregroundStyle(StrandPalette.textTertiary)
            Text("Overline").strandOverline()
            Text("0xAA 41 00 1c crc32=f3a1  mono 13").font(StrandFont.mono).foregroundStyle(StrandPalette.textSecondary)
            HStack(spacing: 4) {
                Text("HRV").font(StrandFont.caption).foregroundStyle(StrandPalette.textSecondary)
                Text("62").font(StrandFont.bodyNumber).foregroundStyle(StrandPalette.textPrimary)
                Text("ms").font(StrandFont.caption).foregroundStyle(StrandPalette.textTertiary)
            }
        }
        .padding(28)
        .frame(maxWidth: .infinity, alignment: .leading)
    }
    .frame(width: 520, height: 620)
    .background(StrandPalette.surfaceBase)
    .preferredColorScheme(.dark)
}
#endif
