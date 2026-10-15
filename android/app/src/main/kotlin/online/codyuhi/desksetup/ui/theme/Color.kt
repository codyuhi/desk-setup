package online.codyuhi.desksetup.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Hex values copied from cedar-design-system → "Semantic Color Token Reference", plus the
// app's scene and blueprint tokens from app/src/style.css (--scene-*, --blueprint-*).

@Immutable
data class CedarColors(
    val dark: Boolean,
    val canvas: Color,
    val surface: Color,
    val inset: Color,
    val hover: Color,
    val focal: Color,
    val brand: Color,
    val brandAccent: Color,
    val alert: Color,
    val ochre: Color,
    val textEmphasis: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val borderSubtle: Color,
    val borderDistinct: Color,
    val successBg: Color, val successText: Color,
    val warningBg: Color, val warningText: Color,
    val errorBg: Color, val errorText: Color,
    val infoBg: Color, val infoText: Color,
    val scene: SceneColors,
    val blueprint: BlueprintColors,
)

@Immutable
data class SceneColors(
    val background: Color,
    val floor: Color,
    val wall: Color,
    val gridMajor: Color,
    val gridMinor: Color,
    val select: Color,
)

@Immutable
data class BlueprintColors(
    val background: Color,
    val desk: Color,
    val accent: Color,
    val label: Color,
    val item: Color,
    val itemStroke: Color,
    val itemLabel: Color,
    val selectFill: Color,
    val grid: Color,
)

val CedarLight = CedarColors(
    dark = false,
    canvas = Color(0xFFEBE7DF),
    surface = Color(0xFFF5F3EC),
    inset = Color(0xFFE4DFD4),
    hover = Color(0xFFDBD5C7),
    focal = Color(0xFFFFFFFF),
    brand = Color(0xFF1F513F),
    brandAccent = Color(0xFF235C48),
    alert = Color(0xFFC7370F),
    ochre = Color(0xFFB8860B),
    textEmphasis = Color(0xFF141412),
    textPrimary = Color(0xFF242320),
    textSecondary = Color(0xFF48453D),
    textMuted = Color(0xFF666257),
    borderSubtle = Color(0xFFCDC7BA),
    borderDistinct = Color(0xFFABA496),
    successBg = Color(0xFFD8E8DC), successText = Color(0xFF1F513F),
    warningBg = Color(0xFFEDE4CE), warningText = Color(0xFF854714),
    errorBg = Color(0xFFEDDCD5), errorText = Color(0xFF811823),
    infoBg = Color(0xFFD6E4EE), infoText = Color(0xFF1B437E),
    scene = SceneColors(
        background = Color(0xFFE4DFD4),
        floor = Color(0xFFCFC6B4),
        wall = Color(0xFFDDD6C8),
        gridMajor = Color(0xFF1F513F),
        gridMinor = Color(0xFFB9B09E),
        select = Color(0xFFB8860B),
    ),
    blueprint = BlueprintColors(
        background = Color(0xFFF5F3EC),
        desk = Color(0xFFDED6C6),
        accent = Color(0xFF1F513F),
        label = Color(0xFF48453D),
        item = Color(0xEBFBFAF6),
        itemStroke = Color(0xFF8A8375),
        itemLabel = Color(0xFF242320),
        selectFill = Color(0x291F513F),
        grid = Color(0x2948453D),
    ),
)

val CedarDark = CedarColors(
    dark = true,
    canvas = Color(0xFF161614),
    surface = Color(0xFF22221F),
    inset = Color(0xFF1B1B18),
    hover = Color(0xFF2D2C28),
    focal = Color(0xFF2D2C28),
    brand = Color(0xFF235C48),
    brandAccent = Color(0xFF48A97C),
    alert = Color(0xFFE0532B),
    ochre = Color(0xFFD89F28),
    textEmphasis = Color(0xFFFFFFFF),
    textPrimary = Color(0xFFF4F2ED),
    textSecondary = Color(0xFFD5CFC3),
    textMuted = Color(0xFF9E998E),
    borderSubtle = Color(0xFF383630),
    borderDistinct = Color(0xFF545149),
    successBg = Color(0x382E6B34), successText = Color(0xFF4ADE80),
    warningBg = Color(0x38854714), warningText = Color(0xFFFBBF24),
    errorBg = Color(0x38811823), errorText = Color(0xFFF87171),
    infoBg = Color(0x381B437E), infoText = Color(0xFF60A5FA),
    scene = SceneColors(
        background = Color(0xFF1E1E1B),
        floor = Color(0xFF2D2C28),
        wall = Color(0xFF262522),
        gridMajor = Color(0xFF48A97C),
        gridMinor = Color(0xFF383630),
        select = Color(0xFFD89F28),
    ),
    blueprint = BlueprintColors(
        background = Color(0xFF1E1E1B),
        desk = Color(0xFF2D2C28),
        accent = Color(0xFF48A97C),
        label = Color(0xFFD5CFC3),
        item = Color(0xEB22221F),
        itemStroke = Color(0xFF9E998E),
        itemLabel = Color(0xFFF4F2ED),
        selectFill = Color(0x4048A97C),
        grid = Color(0x80545149),
    ),
)

val LocalCedarColors = staticCompositionLocalOf { CedarLight }
