package online.codyuhi.desksetup.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import online.codyuhi.desksetup.R

val CedarSerif = FontFamily(
    Font(R.font.source_serif_4_semibold, FontWeight.SemiBold),
    Font(R.font.source_serif_4_bold, FontWeight.Bold),
)
val CedarSans = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)
val CedarMono = FontFamily(Font(R.font.jetbrains_mono_regular, FontWeight.Normal))

/** Mirrors the Cedar typographic scale: serif headings, sans UI and body, mono data. */
val CedarTypography = Typography(
    headlineMedium = TextStyle(fontFamily = CedarSerif, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = (-0.25).sp),
    headlineSmall = TextStyle(fontFamily = CedarSerif, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    titleLarge = TextStyle(fontFamily = CedarSerif, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp),
    titleMedium = TextStyle(fontFamily = CedarSans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 21.sp),
    titleSmall = TextStyle(fontFamily = CedarSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 18.sp),
    bodyLarge = TextStyle(fontFamily = CedarSans, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = CedarSans, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = CedarSans, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = CedarSans, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = CedarSans, fontWeight = FontWeight.Medium, fontSize = 12.sp),
    labelSmall = TextStyle(fontFamily = CedarSans, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 0.9.sp),
)

/** Eyebrow / section label: uppercase in UI. */
val Eyebrow = TextStyle(fontFamily = CedarSans, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 0.9.sp)
val Mono = TextStyle(fontFamily = CedarMono, fontSize = 13.sp)
val MonoSmall = TextStyle(fontFamily = CedarMono, fontSize = 11.sp)

/** Cedar radii: 4dp controls, 6dp cards, 8dp sheets and dialogs. */
val CedarShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(6.dp),
    large = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(8.dp),
)

private fun CedarColors.toMaterial(): ColorScheme {
    val base = if (dark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = brand, onPrimary = Color.White,
        primaryContainer = brand, onPrimaryContainer = Color.White,
        secondary = brandAccent, onSecondary = if (dark) canvas else Color.White,
        secondaryContainer = inset, onSecondaryContainer = textPrimary,
        tertiary = ochre,
        background = canvas, onBackground = textPrimary,
        surface = surface, onSurface = textPrimary,
        surfaceVariant = inset, onSurfaceVariant = textSecondary,
        surfaceContainer = surface, surfaceContainerLow = inset, surfaceContainerLowest = canvas,
        surfaceContainerHigh = hover, surfaceContainerHighest = hover,
        surfaceTint = Color.Transparent,
        outline = borderDistinct, outlineVariant = borderSubtle,
        error = alert, onError = Color.White,
        inverseSurface = textPrimary, inverseOnSurface = surface,
    )
}

@Composable
fun CedarTheme(colors: CedarColors, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalCedarColors provides colors) {
        MaterialTheme(
            colorScheme = colors.toMaterial(),
            typography = CedarTypography,
            shapes = CedarShapes,
            content = content,
        )
    }
}

object Cedar {
    val colors: CedarColors
        @Composable @ReadOnlyComposable get() = LocalCedarColors.current
}
