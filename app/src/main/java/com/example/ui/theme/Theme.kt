package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = AdmiralNavy,
    onPrimary = CrispWhite,
    primaryContainer = NavyContainerLight,
    onPrimaryContainer = OnNavyContainerLight,
    secondary = BrassGold,
    onSecondary = DeepBridgeNavy,
    secondaryContainer = BrassContainerLight,
    onSecondaryContainer = OnBrassContainerLight,
    tertiary = StatusEmerald,
    onTertiary = CrispWhite,
    tertiaryContainer = StatusEmeraldBg,
    onTertiaryContainer = StatusEmerald,
    background = SailIvory,
    onBackground = TextNavyPrimary,
    surface = CrispWhite,
    onSurface = TextNavyPrimary,
    surfaceVariant = DeckSlateLight,
    onSurfaceVariant = TextSlateSecondary,
    outline = BorderSlateLight,
    outlineVariant = DeckSlateLight,
    error = StatusCrimson,
    onError = CrispWhite,
    errorContainer = StatusCrimsonBg,
    onErrorContainer = StatusCrimson,
)

private val DarkColorScheme = darkColorScheme(
    primary = BrassGoldBright,
    onPrimary = DeepBridgeNavy,
    primaryContainer = MidshipNavy,
    onPrimaryContainer = TextIvoryPrimary,
    secondary = BrassGold,
    onSecondary = DeepBridgeNavy,
    secondaryContainer = BrassContainerDark,
    onSecondaryContainer = OnBrassContainerDark,
    tertiary = StatusEmerald,
    onTertiary = CrispWhite,
    tertiaryContainer = StatusEmeraldDarkBg,
    onTertiaryContainer = StatusEmeraldBg,
    background = DeepBridgeNavy,
    onBackground = TextIvoryPrimary,
    surface = DarkDeckSurface,
    onSurface = TextIvoryPrimary,
    surfaceVariant = DarkDeckElevated,
    onSurfaceVariant = TextMistSecondary,
    outline = DarkDeckBorder,
    outlineVariant = DarkDeckElevated,
    error = StatusCrimson,
    onError = CrispWhite,
    errorContainer = StatusCrimsonDarkBg,
    onErrorContainer = StatusCrimsonBg,
)

val AimsShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun AimsBanquetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AimsTypography,
        shapes = AimsShapes,
        content = content,
    )
}
